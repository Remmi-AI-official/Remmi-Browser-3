package com.remmi.browser.security

import android.net.Uri
import android.util.Log
import okhttp3.Dns
import okhttp3.OkHttpClient
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.Proxy
import java.net.URI
import java.util.concurrent.TimeUnit

/**
 * NetworkRouteAuthority
 *
 * Centralized, authoritative factory and validator for all auxiliary network clients
 * (OkHttpClient, URL connections, background translators, fetchers, and DNS).
 *
 * INVARIANTS:
 * 1. Fail-Closed on Ghost/Tor: If Ghost mode is active or an .onion destination is requested,
 *    traffic MUST be routed through the verified 127.0.0.1 Tor SOCKS5 proxy.
 *    A Ghost/Tor-required client is ALLOWED ONLY IF ALL OF THE FOLLOWING ARE TRUE:
 *      - Ghost active (CurrentTorRoute.isGhostActive)
 *      - Valid SOCKS port (> 0)
 *      - Valid route generation
 *      - Tor isVerified == true
 *      - failoverDirect == false
 *    If any condition is false, connection creation FAILS IMMEDIATELY (throwing an IllegalStateException)
 *    to prevent clearnet IP leakage.
 * 2. Strict .onion Detection: Analyzes parsed hostnames with boundary checks to prevent spoofing
 *    (e.g., example.onion.attacker.com is recognized as clearnet attacker.com).
 * 3. Route Generation Pinning: SOCKS proxy configurations are bound to the authoritative
 *    CurrentTorRoute generation to prevent stale routing across mode flips.
 * 4. DNS Leaks Prohibited: In Ghost mode, remote DNS resolution over Tor is enforced.
 */
object NetworkRouteAuthority {
  private const val TAG = "NetworkRouteAuthority"

  /**
   * Shared DNS rebinding guard for non-Tor auxiliary clients. A hostile DNS
   * answer must not turn a harmless hostname into an SSRF path to localhost,
   * RFC1918, link-local, metadata, multicast, or other restricted addresses.
   */
  private val antiRebindingDns = object : Dns {
    override fun lookup(hostname: String): List<InetAddress> {
      val addresses = Dns.SYSTEM.lookup(hostname)
      for (address in addresses) {
        val (isSafe, reason) = RedirectInspector.isInetAddressSafe(address)
        if (!isSafe) {
          Log.w(TAG, "Blocked unsafe DNS answer for '$hostname': ${address.hostAddress} ($reason)")
          throw SecurityException("Unsafe DNS answer for '$hostname': $reason")
        }
      }
      return addresses
    }
  }

  /**
   * Safely extracts the lowercased parsed hostname from any URL or domain string.
   */
  fun extractHostname(url: String): String? {
    val clean = url.trim().lowercase()
    if (clean.isEmpty()) return null

    return try {
      val candidate = if (clean.contains("://")) clean else "http://$clean"
      val parsedUri = URI(candidate)
      val parsedHost = parsedUri.host
      if (!parsedHost.isNullOrEmpty()) {
        parsedHost.trimEnd('.')
      } else {
        val uri = Uri.parse(candidate)
        uri.host?.trimEnd('.')
      }
    } catch (_: Exception) {
      val withoutScheme = clean.substringAfter("://")
      val hostCandidate = withoutScheme.substringBefore('/').substringBefore(':').substringBefore('?').substringBefore('#').trimEnd('.')
      val cleanHost = if (hostCandidate.contains('@')) hostCandidate.substringAfter('@') else hostCandidate
      if (cleanHost.isNotEmpty() && !cleanHost.contains('/')) cleanHost else null
    }
  }

  /**
   * Returns whether the given URL target is a legitimate Tor hidden service (.onion).
   * Distinguishes valid .onion domains (e.g., duckduckgo.onion, sub.example.onion)
   * from spoofed clearnet domains (e.g., example.onion.attacker.com) or paths.
   */
  fun isOnionDestination(url: String): Boolean {
    val host = extractHostname(url) ?: return false
    if (!host.endsWith(".onion")) return false
    if (host == "onion" || host == ".onion") return false
    val label = host.removeSuffix(".onion").substringAfterLast('.')
    return label.isNotEmpty()
  }

  /**
   * Checks whether the given onion hostname corresponds to the obsolete 16-character v2 onion format.
   */
  fun isV2OnionHostname(hostname: String): Boolean {
    val clean = hostname.trim().lowercase().trimEnd('.')
    if (!clean.endsWith(".onion")) return false
    val label = clean.removeSuffix(".onion").substringAfterLast('.')
    return label.length == 16 && label.all { (it in 'a'..'z') || (it in '2'..'7') }
  }

  /**
   * Returns whether the given URL target is an obsolete v2 .onion address.
   */
  fun isV2Onion(url: String): Boolean {
    val host = extractHostname(url) ?: return false
    return isV2OnionHostname(host)
  }

  /**
   * Authoritative check for whether a verified Tor route is ready for onion traffic.
   */
  fun isVerifiedOnionRouteReady(): Boolean = CurrentTorRoute.isVerifiedOnionRouteReady()

  /**
   * Produces an OkHttpClient configured strictly according to current route authority.
   *
   * @param isGhost Whether this request must be routed through the Tor network.
   * @param targetUrl Optional destination URL. If it ends in .onion, Tor routing is mandatory.
   * @param connectTimeoutSeconds Connect timeout in seconds.
   * @param readTimeoutSeconds Read timeout in seconds.
   * @param followRedirects Whether OkHttp should follow HTTP redirects automatically.
   * @param customConfig Optional block for custom OkHttpClient builder tuning.
   *
   * @throws IllegalStateException if Ghost/Tor routing is required but Tor is not fully verified and active.
   */
  fun createHttpClient(
    isGhost: Boolean,
    targetUrl: String? = null,
    connectTimeoutSeconds: Long = 15L,
    readTimeoutSeconds: Long = 20L,
    followRedirects: Boolean = true
  ): OkHttpClient {
    val isOnion = targetUrl != null && isOnionDestination(targetUrl)
    val requiresTor = isGhost || isOnion

    val builder = OkHttpClient.Builder()
      .connectTimeout(connectTimeoutSeconds, TimeUnit.SECONDS)
      .readTimeout(readTimeoutSeconds, TimeUnit.SECONDS)
      .followRedirects(followRedirects)

    if (!requiresTor) {
      builder.dns(antiRebindingDns)
    }

    if (requiresTor) {
      val route = CurrentTorRoute.route.value
      val socksPort = route.socksPort
      val isRouteValid = route.isGhostActive &&
        socksPort != null &&
        socksPort > 0 &&
        route.isVerified &&
        !route.failoverDirect &&
        route.generation > 0L

      if (!isRouteValid) {
        val errorMsg = "Fail-Closed Network Hardening: Ghost/Onion route requested but Tor is not fully verified/active (ghostActive=${route.isGhostActive}, port=$socksPort, verified=${route.isVerified}, failover=${route.failoverDirect}, gen=${route.generation})."
        Log.e(TAG, errorMsg)
        throw IllegalStateException(errorMsg)
      }

      // Enforce SOCKS proxy directly to the verified local Tor daemon
      builder.proxy(Proxy(Proxy.Type.SOCKS, InetSocketAddress("127.0.0.1", socksPort)))
      Log.d(TAG, "Configured OkHttpClient via verified Tor SOCKS5 proxy on 127.0.0.1:$socksPort (gen=${route.generation})")
    }

    return builder.build()
  }

  /**
   * Safely attempts to build an auxiliary OkHttpClient, returning null instead of throwing
   * if Tor routing is unavailable.
   */
  fun createHttpClientOrNull(
    isGhost: Boolean,
    targetUrl: String? = null,
    connectTimeoutSeconds: Long = 15L,
    readTimeoutSeconds: Long = 20L,
    followRedirects: Boolean = true
  ): OkHttpClient? {
    return try {
      createHttpClient(
        isGhost = isGhost,
        targetUrl = targetUrl,
        connectTimeoutSeconds = connectTimeoutSeconds,
        readTimeoutSeconds = readTimeoutSeconds,
        followRedirects = followRedirects
      )
    } catch (e: Exception) {
      Log.w(TAG, "Failed to create route-authorized HTTP client: ${e.message}")
      null
    }
  }
}
