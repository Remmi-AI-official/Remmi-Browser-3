# Remmi Browser Production Release Verification Report

## Overview & Status

- **App Name**: Remmi Browser
- **Package ID**: `com.remmi.browser`
- **Target Version Name**: `1.0.3`
- **Target Version Code**: `4`
- **Compile SDK**: `36`
- **Target SDK**: `36`
- **Min SDK**: `26`
- **GeckoView Runtime**: `154.0.20260824154132`
- **Release Verification Status**: **PASSED (JVM & Source Invariants)**

---

## Hardening Invariants & Validation

### 1. Network Route Authority & Auxiliary Isolation
- **Reader Extraction**: Fully integrated with `NetworkRouteAuthority.createHttpClient(...)`. Auxiliary requests made in Ghost mode or targeting `.onion` domains are strictly routed through Tor's verified SOCKS5 port. If Tor is not ready, extraction fails closed immediately.
- **Fail-Closed Gate**: Gecko fallback is disabled for Ghost and `.onion` reader extraction, preventing clearnet leakage.
- **Memory & Response Bounds**: Implemented `readBodyBounded` and `readInputStreamBounded` capped at 2MB (`MAX_RESPONSE_BYTES`) to prevent memory exhaustion and DoS from oversized payload responses.
- **No Stale Fallbacks**: Removed all `currentSocksPort ?: 9050` and hardcoded `127.0.0.1:9050` socket references from production source files.

### 2. Signing & Build Determinism
- **Deterministic Versioning**: `release-version.properties` defines `versionName=1.0.3` and `versionCode=4`. Overrides via `-PversionCode` and `-PversionName` supported for CI flexibility.
- **Debug Signing Cleaned**: Standard AGP built-in debug signing configuration is utilized for development without custom keystore hacks.
- **Fail-Closed Release Signing**: Release builds require valid `KEYSTORE_PATH`, `STORE_PASSWORD`, `KEY_ALIAS`, and `KEY_PASSWORD` environment variables and fail closed with clear exceptions if missing.
- **CI Secret Names Aligned**: Release CI workflow uses project-named secrets (`REMMI_RELEASE_KEYSTORE_B64`, `REMMI_RELEASE_STORE_PASSWORD`, `REMMI_RELEASE_KEY_ALIAS`, `REMMI_RELEASE_KEY_PASSWORD`).

### 3. CI Pipeline & Verification Gates
- **Early Keystore Verification**: Pre-build keytool verification checks keystore integrity and key alias decryption.
- **Automated Manifest Analysis**: `apkanalyzer` checks application ID (`com.remmi.browser`), target SDK (36), version code, and version name.
- **Signature & Checksum Gates**: `apksigner` and `jarsigner` run full verbose verification; SHA-256 checksums are recorded in `SHA256SUMS.txt`.
- **Automated Source Release Gate**: CI validates that zero hardcoded Tor fallback ports or debug keystore dependencies exist in the repository.

---

## Local JVM Verification Commands

```bash
# Run complete unit and security regression test suite
gradle :app:testDebugUnitTest

# Run full project compilation check
compile_applet
```
