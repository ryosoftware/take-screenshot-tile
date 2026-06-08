<img src="assets/icon.png" width="192" alt="APKs Exporter logo"/>

# Take Screenshot Tile

Android app that adds a **Quick Settings tile** to take screenshots with a single tap, without using hardware button combinations.

## Features

- **Quick Settings tile** — Add it to the notification shade and take a screenshot in one tap
- **Configurable delay** — Adjustable delay (0–5s) before capture, useful to let the notification shade close first
- **Vibrate on capture** — Optional short vibration (10ms) as feedback when a screenshot is taken
- **Accessibility service** — Automatically opens accessibility settings if the service is not yet granted
- **Dark theme support** — Follows the system dark/light theme

## Requirements

- Android 14 (API 33) or higher

## ✅ Certificate Signature Verification

The SHA-256 digest of the certificate used to sign the app remains constant across all versions:

```
2390c310a7fbe8a5bb239de2747d7fab65de8bb37686648c933cbfd03e4b262f
```

Verify with:

```shell
apksigner verify --verbose --print-certs app-release.apk | grep "Signer #1 certificate SHA-256 digest"
```

## Technologies

- **Kotlin** — all source code in Kotlin
- **Jetpack Compose** + **Material3** — UI built entirely with Compose
- **Coroutines** — async delay before screenshot capture
- **SharedPreferences** — persist user preferences

## Icon

Foreground icon: [Material Design Icons](https://materialdesignicons.com) — `cellphone-screenshot` (Apache 2.0 license)
