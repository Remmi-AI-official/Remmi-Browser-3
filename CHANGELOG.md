# Changelog

## 1.0.3

Production release hardening, deterministic versioning, and GeckoView upgrade:

- Upgraded GeckoView runtime dependency to `154.0.20260824154132`.
- Standardized Reader mode extraction onto `NetworkRouteAuthority.createHttpClient(...)` with 2MB bounded body reading and fail-closed handling for Ghost/Onion requests.
- Added `release-version.properties` as the project-wide single source of truth for version code and name.
- Hardened release CI workflow (`release.yml`) with automated manifest verification via `apkanalyzer`, SHA-256 generation, `apksigner` validation, and project secret naming (`REMMI_RELEASE_KEYSTORE_B64`).
- Cleaned Gradle signing configuration to rely on AGP's built-in debug signing and fail-closed release signing.
- Added comprehensive unit and source regression test suites covering Tor routing invariants, Reader isolation, and release versioning.

## Jitter / GeckoView stability fix

- Prevent transient Gecko `about:blank` callbacks from swapping the Compose content to the New Tab page during real navigation.
- Guard against concurrent `GeckoView` attach calls from lifecycle/recomposition races.
- Suppress no-op `TabManager` state emissions to reduce unnecessary BrowserScreen recompositions during heavy tracker blocking.
- Preserve explicit Home and Back-to-Home behavior through an intent-based New Tab state.

## 1.0.2

Release-readiness hardening for the first public beta/production candidate.

- Routed favicon and Reader translation auxiliary traffic through the central network authority.
- Added DNS rebinding protection for normal-mode auxiliary HTTP clients.
- Made Ghost and .onion navigation fail closed until the Tor route is verified `READY`.
- Routed external HTTP(S) intents through the same navigation security gate.
- Moved automatic crash diagnostics to app-private no-backup storage.
- Removed release-signing fallback keys and made CI signing fail closed.
- Updated Android target/compile SDK to API 36 and aligned release CI to JDK 17.
- Updated jsoup to 1.23.2 and added public privacy/security/release documentation.
- Removed one-off source patch scripts from the public release tree.
