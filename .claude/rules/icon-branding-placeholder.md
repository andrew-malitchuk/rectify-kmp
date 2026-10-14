---
paths:
  - "aos-application/src/main/res/**"
  - "ios-application/**/Assets.xcassets/**"
  - "presentation-core-ui/**/atom/icon/**"
---

# Icon / branding placeholder

This template ships a neutral placeholder mark (a plain rounded square, no wordmark, no logo)
in place of real branding — it is meant to be replaced, and every piece of it is wired to the
theme so it already adapts to light/dark without extra work. Replace all four pieces together;
leaving one stale produces a visible mismatch between launcher icon and in-app logo.

- **Android adaptive icon** — `aos-application/src/main/res/drawable/ic_launcher_foreground.xml`
  (light) and `drawable-night/ic_launcher_foreground.xml` (dark) hold the mark geometry;
  `ic_launcher_monochrome.xml` is the Android 13+ themed-icon variant (white-only, OS applies
  the tint). `ic_launcher_background.xml`/`drawable-night/ic_launcher_background.xml` are flat
  fills. No flat `mipmap-*dpi/ic_launcher.png` fallbacks exist — `minSdk` is 27 (≥ 26), so the
  adaptive icon (`mipmap-anydpi-v26`/`v33`) is the only resource Android ever resolves; don't
  regenerate the legacy per-density PNGs when you replace the mark.
- **iOS app icon** — `ios-application/ios-application/Assets.xcassets/AppIcon.appiconset/`
  needs three fully-opaque 1024×1024 PNGs (`app-icon-1024.png` default, `-dark`, `-tinted` —
  Apple's current three-appearance icon format; no alpha channel allowed). `Contents.json`
  already declares all three variants — only the PNG bytes need replacing.
- **In-app logo** — `presentation-core-ui/.../kit/atom/icon/RectifyLogo.kt` is an `ImageVector`
  rendered on the splash screen (`SplashSuccessContent`), filled with `Theme.color.inkMain` so
  it tracks the active theme automatically. Replace the path data; keep the `Theme.color.*` fill
  if you want the same light/dark behaviour for free.
- `presentation-core-ui/.../kit/atom/icon/AppIcon.kt` was deleted as unused dead code — don't
  recreate a second logo composable; `RectifyLogo` is the one both the splash screen and any
  future in-app brand reference should use.
