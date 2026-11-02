# Module map

Rectify is 22 Gradle modules. The split is strict and the dependency direction never reverses:
**presentation → domain → data**, with a few shared leaves at the bottom.

## A warning before you read the source

**"Android" and "iOS" appear everywhere in this codebase as *domain* concepts, not build
targets.**

Rectify is a desktop application. Every module is a plain `kotlin("jvm")` module; there is no
Kotlin Multiplatform plugin, no `commonMain`/`desktopMain` split, no `expect`/`actual`, and no
Android or iOS target anywhere. Sources are in the classic `src/main/kotlin` and
`src/test/kotlin`.

`AdbHostSource`, `IosHostSource`, `SimctlDevicesParser`, the `ANDROID_SOURCE` and `IOS_SOURCE`
Koin qualifiers — those are **the product**. They are how a desktop app talks to phones. They
are not evidence of a mobile build.

The repository is still *named* `rectify-kmp`, which is also why Compose resource accessors
generate under `rectify_kmp.*`. That is a name, not a target.

## Entry points

| Module | Role |
|---|---|
| `desktop-application` | The runnable one. `main()` calls `initKoin()` then `runDesktopApp()`. Nothing else. |
| `compose-application` | The app composition: the Rectify window, the Koin graph assembly. A library — it has no `main()`. |
| `data-mcp-impl` | The MCP endpoint the app serves over loopback HTTP. Same process as the window. |
| `docs-generator` | A build-time tool that renders this site out of `docs/`. |

The runnable module is separate from the UI module on purpose: `compose-application` stays a
library so a harness can compose the shell without a process, and `desktop-application` owns
nothing but the entry point and the packaging config.

## Layers

### Data

| Module | Contents |
|---|---|
| `data-core` | The `Resource` marker interface. One file. |
| `data-device-api` | Contracts and **every pure parser** — `adb devices`, `simctl list`, `idb list-targets`, the UiAutomator dump, PNG trimming. |
| `data-device-impl` | The process drivers. The only place in the repo that spawns a child process. |
| `data-preference-api` / `-impl` | Typed preference sources over `multiplatform-settings`. |
| `data-database-api` / `-impl` | Composable datasource base interfaces, and the Room implementation behind them. |
| `data-repository-impl` | Repository implementations and the mappers that convert resources to domain models. |
| `data-handoff-api` | The handoff contract the UI writes and `data-mcp-impl` reads. Koin-free by design. |

Parsers live in the **api** module, not the impl, deliberately. They are pure functions over
strings and bytes, so keeping them there means they are testable with no hardware attached —
which is most of what this feature is. Moving one into `data-device-impl` would drag the
process-spawning layer onto the test classpath.

### Domain

| Module | Contents |
|---|---|
| `domain-core` | Models and cross-cutting enums. No dependencies on anything above it. |
| `domain-repository-api` | Repository interfaces: `DeviceRepository`, `HistoryRepository`, `ConfigureRepository`. |
| `domain-usecase-api` | Use case interfaces — one per operation. |
| `domain-usecase-impl` | Thin implementations, mostly a single repository call each. |

### Presentation

| Module | Contents |
|---|---|
| `presentation-core-styling` | Design tokens: colour, typography, spacing, radii, motion. |
| `presentation-core-ui` | The UI kit, atoms through organisms, plus the window shell. |
| `presentation-core-localisation` | String resources. |
| `presentation-feature-mirror` | The whole product surface: discovery, mirroring, annotation, measure, send, verify, history. |

### Shared

`common-core` holds the `ModelResourceMapper` contract and the `resultLauncher` helper that
wraps a suspending call into a `Result`.

## Package layout inside a module

Identical everywhere, and worth internalising before reading any single module:

- **`core/`** holds `internal` and `private` declarations only — infrastructure, wiring,
  helpers not meant to leave the module.
- **`source/`** holds everything `public`, grouped one subfolder per concern.
- A module with no internal declarations has **no `core/` directory at all**. Do not create an
  empty one for symmetry.

`explicitApi()` is enforced repo-wide by the convention plugins, so every public declaration
needs an explicit visibility modifier and return type. That is what actually polices the
boundary at compile time rather than leaving it to convention.
