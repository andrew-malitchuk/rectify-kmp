# aos-application — Claude Instructions

## Module Purpose

Android-only application module. Contains the two required Android framework components and nothing else. No business logic, no ViewModels, no repositories — all of that lives in KMP-shared modules.

## Package Layout Convention

```
dev.rectify.io/
├── core/     internal components — invisible outside this module
└── source/   public Android framework components, grouped by type
    └── activity/
```

**Rule:** anything with `internal` visibility goes in `core/`. Anything that must be declared in `AndroidManifest.xml` (Activity, Service, Application) must be `public` and lives in `source/<type>/`.

## What Belongs in This Module

| Belongs here | Does NOT belong here |
|---|---|
| `Application` subclass | ViewModels |
| `Activity` entry points | Use case implementations |
| `Service` subclasses, manifest-registered receivers | Repository / DataSource logic |
| Android signing / build config | Compose UI screens, shared KMP logic |

If you are tempted to add a ViewModel or a repository, put it in the appropriate `domain-*` or `presentation-feature-*` module instead.

## Key Files

| File | Role |
|---|---|
| `core/RectifyApplication.kt` | Koin bootstrap — called once on process start |
| `source/activity/MainActivity.kt` | Single-activity host — no logic, just `setContent { App() }` |
| `src/main/AndroidManifest.xml` | Registers both components with full package paths |
| `build.gradle.kts` | Android app config: signing, build types, SDK versions |

## AndroidManifest Class Names

Always use fully-qualified names — do not rely on relative shortcuts:

- Application: `dev.rectify.io.core.RectifyApplication`
- Activity: `dev.rectify.io.source.activity.MainActivity`

## Build Commands

```bash
./gradlew :aos-application:assembleDebug     # debug APK
./gradlew :aos-application:assembleRelease   # release APK (needs signing.properties)
./gradlew :aos-application:check             # lint + unit tests
```

## Signing

Signing keys are read from `configure/secrets/signing.properties` (placeholder values — replace with your own keystore alias/password before enabling release signing). The release `signingConfig` block in `build.gradle.kts` is commented out until keys are provisioned — do not uncomment without the corresponding keystore file.

## Adding a New Android Component

1. Decide visibility: `internal` → `core/`, `public` (manifest-registered) → `source/<type>/`.
2. Create the file with the correct package (`dev.rectify.io.core.*` or `dev.rectify.io.source.<type>.*`).
3. Register in `AndroidManifest.xml` using the fully-qualified class name.
4. Inject dependencies via Koin (`by inject()`) — do not instantiate use cases directly.
5. Update `README.md` (Public API table + Manifest Registration section).
