# android-device-discovery-mirroring — plan

## Envelope

| field | value |
| --- | --- |
| stage | plan |
| step | plan |
| status | done |
| producedBy | sdd-build-plan-refine |
| schemaVersion | 1 |
| createdAt | 2026-10-15T00:19:33Z |
| updatedAt | 2026-10-15T00:19:55Z |

## Gate

### type

human

### approvedBy

orchestrator-auto

### approved

true

### at

2026-10-15T00:19:33Z

## Provenance

### selfHash

685e70a97c538c5bf3ac6b5b50a06601c803863ee89f04bc6dfccfc721dd1d3e

### inputs

| artifact | hash |
| --- | --- |
| preparation/preparation.json | da20ef73c135a07e33a076102fbfc1f6146e4118888446d817a29f03e77f9879 |
| plan/candidates/candidate-1.json | b1c05af93595003f78151e0bbfebbc2ade87f9c1f3c42540fa000ca617d7f464 |
| plan/evaluation.json | 5c5527873f606cf8f40a499938d6e7d20962e8dd5a13729f9d197b3beac3224d |

## Data

### meta

#### title

Android Device Discovery & Live Mirroring (F-2) — Implementation Plan

#### version

2

#### status

final

### goal

Make every static stub in the merged F-1 shell live for Android: detect adb/idb at launch and show per-platform status, offer a full-stage device picker over Physical + Emulator sources with inline Boot and arrow/Enter navigation, start a live mirror on the selected device through a swappable MirrorTransport, show the passive NO SIGNAL state while adb reports nothing, and report honest tool/resolution/fps/latency stats in the status bar — with zero new catalog dependencies, all adb/idb execution confined to one internal desktopMain file behind a device-scoped API, and v1's NFR-1 latency shortfall carried as an explicit, measured waiver rather than a silent miss.

### problemStatement

Rectify's canonical window exists (F-1) but shows nothing real: the platform switch, device indicator, and status bar are static previews, and StageSurface has no content source. Until a real Android device can be discovered and mirrored inside the single canonical window, there is no product to annotate — every downstream spec (annotation, measurement, payload, error recovery) depends on this one being live.

### highLevelDescription

One vertical slice through the existing data → domain → presentation layering, with process execution quarantined in a single desktop-only module. Three new Gradle modules (data-device-api, data-device-impl, presentation-feature-mirror) join the existing 24 (settings.gradle.kts 24 → 27 includes), and EIGHT existing modules are amended (domain-core, domain-repository-api, domain-usecase-api, domain-usecase-impl, data-repository-impl, presentation-core-ui, presentation-core-localisation, compose-application). Every pure parser — adb devices -l, wm size, wm density, emulator -list-avds, the foldable PNG-signature trim, the adb argv builder, the tool-probe candidate order — lives in data-device-api's commonMain, the single decision that makes AC-2/AC-3/AC-41/AC-42/AC-44 verifiable with plain kotlin.test and no phone attached (AS-7). java.lang.ProcessBuilder appears in exactly one internal file in data-device-impl/desktopMain. The mirror is a two-member MirrorTransport interface whose v1 implementation is an adaptive single-flight `adb exec-out screencap -p` loop; the picker, the NO SIGNAL stage and the stats line are Orbit MVI state in presentation-feature-mirror, injected by RectifyWindow and passed as plain defaulted data into RectifyShellContent, whose body stays free of Koin, AWT and coroutines.

### background

F-1 (app-shell-window-layout) shipped and merged to development (merge 62808cc, verdict complete-with-waivers). It delivered RectifyWindowShell, ShellToolbar (six fixed named slots), ShellStatusBar, StageSurface (bezel-less stage + dot grid, required content slot), SidePanelFrame, ShellState/ShellTab/ShellWindowDefaults, and RectifyShellContent with every slot a static stub. It also shipped the kit this spec reuses — all verified present at refine by listing the 110 files under presentation-core-ui kit/: SourceListRow/SourceListHeader, SegmentedButtonGroup, DeviceChip, StatusIndicator, AppButton, KeyCap, SectionHeader, StatusPill, CommandBlock, ErrorCard, ErrorGlyph, GlyphBadge, CounterBadge. rectify-poc's Devices.kt (249 lines) is a reference implementation only: its logic (adb devices -l parsing, screencap with the foldable trim, wm size/density/getprop) is proven and portable; its flat structure, untyped error() calls and blocking threads are not. Stage 1 closed with readyForPlan: true and twelve binding decisions, including a recorded NFR-1 waiver approved under survey option B — flagged for re-review before ship.

### sources

| type | ref | cached |
| --- | --- | --- |
| code | preparation/preparation.json (8 FR, 4 NFR, 10 AC, 4 SC, 12 binding decisions, 11 risks, 11 open questions) | .pipeline/sdd/docs/android-device-discovery-mirroring/preparation/preparation.json |
| code | preparation/brainstorm.json (19 components, 18 edge cases, 11 implementation steps, 15 ready-made reuses) | .pipeline/sdd/docs/android-device-discovery-mirroring/preparation/brainstorm.json |
| code | preparation/clarify.json (AC/FR/NFR/SC ids, 5 clarify decisions, AS-6..AS-9) | .pipeline/sdd/docs/android-device-discovery-mirroring/preparation/clarify.json |
| figma | 01-first-run-platform-source — READ AT REFINE: centred 'Choose what to mirror' panel on a plain UNDOTTED stage; PLATFORM cards 'adb 35.0.1 · /opt/homebrew/bin' and 'idb 1.1.8 · Xcode 16.0' with radios; SOURCE list headed '3 found · adb devices -l'; rows 'Pixel 8 Pro / 37281FDH2000AB / Android 15 · USB', 'Pixel_7_API_34 / emulator-5554 / Running', 'Pixel_Fold_API_35 / shut down / Boot'; hint '↑↓ select · ↵ start'; filled 'Start mirroring' CTA; status bar 'adb 35.0.1 · idb 1.1.8 · ready'. | raw/design/01-first-run-platform-source.png |
| figma | 02-empty-no-device — READ AT REFINE: NO SIGNAL dashed phone outline on the DOTTED stage; 'Waiting for an Android device'; subtitle 'Mirroring starts on its own as soon as adb sees one.' which CONTRADICTS comp 01's CTA (see proposedSurvey Q1); two numbered steps; inline AVD select 'Pixel_7_API_34' + Boot; footer 'adb devices · polling every 2 s'; status bar 'Waiting for device…'. | raw/design/02-empty-no-device.png |
| figma | 03-device-picker-first-run — same picker with Pixel 8 Pro selected (filled radio), Running dot on the warm emulator, Boot on the cold one. | raw/design/03-device-picker-first-run.png |
| figma | 04-error-adb-not-on-path — F-3 renders this from F-2's ToolMissing state (out of scope here). | raw/design/04-error-adb-not-on-path.png |
| figma | 05-error-no-device-detected — F-3 renders this from F-2's NoDeviceFound state (out of scope here). | raw/design/05-error-no-device-detected.png |
| code | F-1 plan (shell contract, Stage 5 four-desktopTest command, normalise-and-compare-landmarks method) | .pipeline/sdd/docs/app-shell-window-layout/plan/plan.json |
| code | rectify-poc AndroidBridge — listDevices / captureScreenshot (PNG-signature trim) / deviceInfo; logic reused, structure discarded | /Users/andrew-malitchuk/StudioProjects/rectify-poc/app/src/main/kotlin/rectify/Devices.kt |

### scope

#### in

- FR-1 launch-time adb/idb tool detection with ordered-candidate probing and per-platform status (AC-1, AC-41, SC-6).
- FR-2 Android/iOS platform switch wired to real state; Android functional, iOS shows the detected idb probe only and renders a deferred-platform stage.
- FR-3 device picker: full-stage takeover panel with Physical/Emulator sections, inline Boot, arrow/Enter/Escape navigation (AC-2, AC-43, NFR-8).
- FR-4 'Start mirroring' connecting to the selected device through the v1 screenshot-polling transport (AC-3, SC-1).
- FR-5 2s-polling passive NO SIGNAL empty state with guidance and inline emulator Boot (AC-4, SC-2, SC-7, AC-42).
- FR-22/FR-24/FR-25 status-bar live stats behind a swappable MirrorTransport interface (AC-25, AC-45).
- NFR-5 device-scoped command executor enforcing selected-device-only execution by API shape (AC-44).
- Typed DiscoveryModel contract (Detecting | ToolMissing | NoDeviceFound | Found) published for F-3 to render comps 04/05.
- Three new Gradle modules, one new Failure subtype, 27 mirror_* localisation keys, CLAUDE.md for each new module.
- Three new inter-module project dependencies (data-repository-impl → data-device-api; compose-application → data-device-impl and presentation-feature-mirror) and one new commonTest block on data-repository-impl. These are project/plugin wiring, not catalog entries — the zero-new-dependency rule is about gradle/libs.versions.toml.
- A measured v1 capture-rate/latency number recorded in the delivery notes (decision 9 — measured, never estimated).

#### out

- Functional iOS mirroring — the switch exists and shows detected idb only (FR-2 defers to v1.1+).
- The rich error-card UI and recovery actions for comps 04/05 — F-3 owns both, composing shipped ErrorCard/CommandBlock against F-2's typed states.
- Mid-session reconnect/recovery UX after FR-4 has started streaming — F-3 territory; F-2 ends the session cleanly and returns to discovery.
- A production H.264-decode transport meeting NFR-1's <150ms target — staged out per FR-24, tracked as a v1.1+ follow-up behind the same interface.
- A clipboard abstraction for F-3's Copy action — F-2 only carries the exact command string in ToolProbe.
- Fixing resultLauncher's CancellationException hazard — a repo-wide follow-up touching every existing use case.
- Annotation, measurement, payload, history and MCP specs.
- Windows/Linux builds (NFR-10); a headless CI harness for the desktop app (none exists, so demo ACs stay human-observed).
- Any new version-catalog entry — including orbit-test and turbine, both absent from the catalog.

### assumptions

- AS-6: scrcpy 3.3.1 and adb remain available via PATH/Homebrew-standard locations during implementation and demo; detection reads the environment dynamically rather than hardcoding this machine's verified paths.
- AS-7: no physical device or running emulator is guaranteed in the build/test environment; AC-2/AC-3/AC-41/AC-42/AC-44 'test' verification is achieved by exercising pure parsers and fake sources, never real hardware.
- AS-8: shipping screenshot polling as the v1 transport (not meeting NFR-1) is an accepted, explicitly-flagged trade-off, approved at the Stage 1 gate under option B — carried forward here, not resolved.
- AS-9 (VERIFIED at refine by reading SourceListRow.kt): the component already has the Canvas radio (private RadioMark, lines 101-113), the bold name, the mono serial and `trailing: @Composable RowScope.() -> Unit = {}`, and its own preview renders all three comp states (USB label / Running dot / Boot button). One additive optional parameter is the whole change.
- VERIFIED, not assumed: decodeToImageBitmap is reachable from a FEATURE module's commonMain — FeatureConventionPlugin.kt:115 declares implementation(composeDependencies.components.resources) in commonMain, where org.jetbrains.compose.resources.decodeToImageBitmap lives. No new dependency; the skiko fallback is dropped. Corollary enforced by this plan: LibraryConventionPlugin does NOT add compose resources, so the two data modules cannot decode — decoding stays in presentation-feature-mirror.
- VERIFIED, not assumed: kotlinx-coroutines-test is genuinely wired, not merely cataloged (data-preference-impl/build.gradle.kts:16). CORRECTION FOUND AT REFINE: data-repository-impl has commonTest sources (two mapper tests) but NO commonTest dependency block at all, so step 5 must add one or the virtual-time polling test cannot compile.
- VERIFIED, not assumed: kotlin.test reaches commonTest from BOTH LibraryConventionPlugin (line 109) and FeatureConventionPlugin (line 123); no new module declares it.
- VERIFIED, not assumed: nothing in the repo branches exhaustively on Failure — grepping `is Failure.Technical` and `when (failure)` returns zero files, and the four Technical subtypes are uniform `data class X(override val cause: Throwable)`. Adding Process is source-compatible.
- VERIFIED, not assumed: initKoin()'s current list is dataPreferenceImplModule, dataDatabaseImplModule, dataRepositoryImplModule, domainUseCaseImplModule, then the five presentation feature modules — the insertion points named in steps 8 and 14 are real line positions, not guesses.
- Koin single definitions resolve lazily at injection time, so intra-group ordering is defensive rather than load-bearing — but koin-di.md and compose-application/CLAUDE.md both treat order as significant, so dataDeviceImplModule sits before its consumer dataRepositoryImplModule and the reason is written as a comment at the call site.
- A frame's ByteArray is wrapped, never copied, when crossing the resource→model boundary. Corollary to respect: a data class holding a ByteArray has identity-based equals, so MirrorFrame/MirrorFrameModel must never be a reduced-state field or a distinctUntilChanged key.

### constraints

| type | text |
| --- | --- |
| tech | Zero new version-catalog entries. Everything used is JDK, kotlinx-coroutines, Compose Multiplatform, Koin, Kermit or kotlin.test — all already declared AND wired. The only dependency this spec would otherwise need (a JVM H.264 decoder) is precisely what FR-24 defers. `git diff gradle/libs.versions.toml` must be empty. |
| tech | Convention-plugin selection follows what the repo actually does, not Preparation's wording: NO library module in this repo applies the `di` plugin. data-device-api uses `library` alone with no Koin at all (mirroring data-preference-api). data-device-impl uses `library` alone plus api(projects.dataDeviceApi), implementation(libs.koin.core) and implementation(libs.kermit) (mirroring data-preference-impl and data-repository-impl). Only presentation-feature-mirror uses `feature` + `di` (mirroring presentation-feature-settings). Applying `di` to a data module would pull in koin-compose and koin-viewmodel, neither usable there. |
| tech | All adb/idb process execution routes through one device-scoped command executor whose API requires a resolved DeviceId; no repository, use case or composable may shell out directly (NFR-5, AC-44). |
| tech | Kotlin daemon is capped at 3GB in gradle.properties; building three new KMP modules across all targets at once OOMs the 18GB host. Build one module per implementation step; never ./gradlew build at the root. |
| tech | explicitApi() is enforced by both convention plugins — every public declaration needs an explicit visibility modifier AND an explicit return type. core/ is internal-only, source/ is public, and a module with no internal declarations gets no core/ directory (so data-device-api has none). |
| tech | Design tokens only: no literal Color(0x…), no raw .dp at a call site, no hardcoded TextStyle. Strict atom → molecule → organism direction in presentation-core-ui. |
| tech | A commonTest backtick function name containing a comma or an apostrophe compiles on the JVM and fails compileTestKotlinIosArm64 — F-1 shipped that repo-wide and fixed it at a71d232. Plain-word backtick names only, no punctuation of any kind: `override size wins over physical size`, never `parses the override size, not the physical one`. The repo's existing mapper tests already follow this; copy them. |
| tech | DeviceCommand is a closed sealed set, not a vararg string runner — argument injection and remote targets are unrepresentable rather than merely discouraged. |
| tech | RectifyShellContent's BODY must stay free of Koin, AWT and coroutines, and every new parameter is plain data with a default so the F-1 render harness keeps driving it from literals. This is a body-level invariant, NOT a file-level grep: RectifyWindow.kt already imports org.koin.compose.koinInject, java.awt.Dimension and kotlinx.coroutines.launch for RectifyWindow itself, so grepping the file cannot verify it. Verify by reading the function and its callee set. |
| business | PRD estimates this spec as 'L'; the staged transport (FR-24) keeps it inside that envelope by deferring the highest-cost item rather than absorbing it. |
| time | The measurement step (step 12) is a gate, not a nicety: the v1 capture rate and per-frame latency must be real numbers written into the delivery notes before the slice is called done. |

### dependencies

| on | note |
| --- | --- |
| F-1 (app-shell-window-layout) | Merged to development. Provides RectifyWindowShell / ShellToolbar / ShellStatusBar / StageSurface slot contracts and the kit components this spec fills and reuses. |
| error-handling-and-recovery (F-3) | Downstream dependent. Inherits the DiscoveryModel.ToolMissing / NoDeviceFound contract (AC-41, AC-42) including ToolProbe.probedPaths and installCommand, to render comps 04/05. F-2 builds none of that UI. |
| rectify-poc Devices.kt | Reference implementation only, not a build dependency. Source of the adb devices -l parse, the screencap PNG-signature trim, and the wm size/density/getprop reads. |
| Local toolchain for the demo and the measurement step | adb (Homebrew or Android SDK), the emulator binary, and either a USB-debug phone or one installed AVD. Automated tests must stay green without any of them. |

### risks

| text | likelihood | impact | mitigation |
| --- | --- | --- | --- |
| Screenshot polling delivers roughly 3–7 fps and ~250–500 ms per frame, so the mirror feels like a slideshow under drag and NFR-1 is visibly unmet in the demo. | high | med | An accepted, recorded waiver (FR-24/AS-8), re-flagged at the Stage 1 gate under option B. Keep fps honest, keep latency an explicit placeholder (AC-45), add the one-word 'polling' hint next to fps, and keep MirrorTransport to two members so the decoder transport is a new class plus one DI line. |
| PATH in a packaged macOS .app omits /opt/homebrew/bin, so adb detection works all through development and fails the first time anyone double-clicks the app. | high | high | probeCandidates() is a PURE function in data-device-api/commonMain returning the ordered candidate list from an injected env map and home dir; ToolResolver only applies an executable predicate to it. Unit-tested with a fake env (empty PATH, ANDROID_HOME set, Homebrew-only) and every path tried is recorded in ToolProbe.probedPaths for F-3's comp-04 footer. |
| Orphaned adb children after an abrupt window close hold the USB connection and make the next launch see no device. | med | med | Live-child registry (Collections.newSetFromMap(ConcurrentHashMap())) + one Runtime shutdown hook registered once + destroyForcibly() in finally + a per-call withTimeoutOrNull budget. The adb server daemon itself is never killed. |
| Three new modules plus edits to eight existing ones blow the 3GB Kotlin daemon when built together. | med | med | The implementation sequence is bottom-up and per-module; each step names its own single :module:build or :module:desktopTest command. No root build at any point. |
| A commonTest backtick name containing a comma or apostrophe compiles on JVM and breaks compileTestKotlinIosArm64 — this spec adds more test files than any before it. | med | high | Plain-word backtick names, no punctuation, matching the style already in data-repository-impl's two mapper tests. Steps 3, 13 and 15 run the three :compileTestKotlinIosArm64 tasks before the slice is declared done. |
| Frames pushed through the Orbit reducer at up to 10/s cause allocation churn and sluggish UI misdiagnosed as transport latency; a ByteArray-bearing data class also compares by identity, so a reduced state holding one would never deduplicate. | med | med | Pixels live in a dedicated StateFlow<ImageBitmap?> outside Orbit state; only semantic state goes through the reducer, and stats update on a separate 1s ticker. No MirrorFrame/MirrorFrameModel field appears in MirrorUiState or any distinctUntilChanged key. |
| The live mirror repaints over StageSurface's dot grid, whose Canvas draws every dot across the full stage on each pass (F-1 finding PRF-1, severity minor, StageSurface.kt:80, unverified) — at 10fps that cost is paid ten times a second behind an opaque image. | med | med | MirrorFrameView is aspect-fit and opaque where it covers; measure frame time in step 12 with the grid visible vs covered, and if the grid is material raise a scoped PRF-1 follow-up against presentation-core-ui rather than editing StageSurface inside this slice. |
| Adding Failure.Technical.Process to a sealed hierarchy in the zero-dependency core breaks exhaustive when expressions elsewhere. | low | low | Already checked at refine: the grep returns zero files. Re-run it in step 1, then build :domain-core and :domain-usecase-impl before anything else depends on the change. |
| Orbit's internal intent dispatching makes plain runTest assertions over container.stateFlow flaky, tempting an orbit-test dependency that is not in the catalog. | med | med | Dispatchers.setMain(StandardTestDispatcher) + advanceUntilIdle() before each assertion; if unreliable, fall back to covering the extracted pure helpers (nextSelectableIndex, discovery→state derivation, stats formatting) and keep VM coverage at smoke level. Adding orbit-test is a gate decision, never a quiet fix. |
| Unauthorized/offline devices are filtered out (the poc's behaviour), so a user with a plugged-in phone sees NO SIGNAL and follows guidance that cannot help them. | med | med | parseAdbDevices returns a typed DeviceStateResource for every entry; non-device states survive into DeviceModel and render as non-selectable rows with their state as the trailing label (SourceListRow gains `enabled`). Arrow keys skip them. |
| scrcpy 3.3.1 is installed on the dev machine and will tempt a 'just shell out to scrcpy' shortcut, opening a second OS window and breaking F-1's canonical-window contract. | low | high | The closed DeviceCommand set contains no scrcpy command and the host sources enumerate only adb/emulator subcommands; adding one is a visible, reviewable API change in data-device-api, not a one-line call site. |
| Pre-existing repo hazard made live by this spec: resultLauncher catches Throwable, which includes CancellationException, so a withTimeout inside any use case would swallow genuine parent-scope cancellation. | med | high | This spec uses withTimeoutOrNull everywhere, with the reason in a comment at each site, and never lets a TimeoutCancellationException reach resultLauncher. Fixing the helper touches every existing use case and is an explicit follow-up, not done here. |
| Picker key handling and the window-level Cmd+1/2/3 handler fight over focus once the side panel exists in a later spec. | low | med | Ownership splits by modifier: unmodified keys belong to the focused panel, meta-modified to the window. handleShellShortcut (RectifyWindow.kt:287, already gated on isMetaPressed via shortcutToTab) is untouched, and a VM test asserts arrow/Enter intents carry no meta semantics. |
| FOUND AT REFINE: the Koin wiring step cannot compile as the draft specified it. InitKoin.kt lives in compose-application/commonMain and imports each Koin module by package, so :data-device-impl and :presentation-feature-mirror must be added to compose-application's commonMain dependency block — the draft specified the initKoin edit but not the build-file edit. | high | med | Steps 8 and 14 now name the exact build.gradle.kts lines. commonMain (not desktopMain) is the correct source set: both are full KMP modules that compile for every target, which is the entire reason UnsupportedDeviceSources exists. The fork is stated explicitly as proposedSurvey Q2. |

### requirements

#### functional

| id | text |
| --- | --- |
| FR-1 | On launch, detect installed adb and idb tool versions and show a status dot per platform in the toolbar. |
| FR-2 | Show a platform switch (Android/iOS) in the toolbar on every screen; Android is functional in v1, iOS is deferred to v1.1+. |
| FR-3 | Provide a device picker dropdown with Physical-device and Emulator sections (running, or shut-down with an inline Boot button), navigable via arrow keys and Enter. |
| FR-4 | Provide a primary 'Start mirroring' action that connects to the selected device and begins the live mirror stream. |
| FR-5 | When no device is connected, poll adb every 2s and show a 'NO SIGNAL' empty state with guidance to plug in a USB-debug device or boot an emulator inline. |
| FR-22 | Show a status bar with live stream stats: tool version, resolution, fps, and latency. |
| FR-24 | v1 ships a screenshot-polling mirror transport (adb exec-out screencap, reusing rectify-poc's proven capture/foldable-trim logic) as the FR-4 stream source; it is explicitly documented as not meeting NFR-1's <150ms target. An H.264-decode transport (scrcpy-server socket consumed in-JVM) is a deliberate v1.1+ follow-up, gated on choosing and justifying a new JVM decoder dependency per version-catalog-hygiene.md. |
| FR-25 | The live mirror transport is abstracted behind a single interface consumed by StageSurface's content slot and by FR-22's stats contract, so swapping the v1 polling transport for a future decoder-based transport (FR-24) requires no change to the shell integration or the stats shape. |

#### nonFunctional

| id | category | text |
| --- | --- | --- |
| NFR-1 | performance | Live mirror glass-to-glass latency should stay low enough (target under ~150ms) that drag-to-select and freeze feel responsive. WAIVED FOR v1 — screenshot polling knowingly misses this; the real achieved number must be measured (step 12) and recorded, and the waiver is flagged for re-review before ship. |
| NFR-5 | security | adb/idb commands are only ever invoked against the user's explicitly selected, connected device — never an arbitrary or remote target. Enforced by API shape (see security section), not by call-site discipline. |
| NFR-8 | a11y | Device picker, annotation popovers, and error-card actions are fully keyboard-navigable (arrow keys/Enter for pickers, Enter/Escape for popovers). In F-2's scope: the picker and the NO SIGNAL emulator control. |
| NFR-10 | compatibility | v1 targets macOS only; no Windows/Linux builds. The desktop actual is the only functional one; android/iOS actuals register unsupported sources so common code still compiles on all four targets. |

### acceptanceCriteria

| id | text | priority | verificationMethod |
| --- | --- | --- | --- |
| AC-1 | Given adb and idb are installed, when the app launches, then the toolbar shows a status dot reflecting detected tool versions for both platforms. | must | demo |
| AC-2 | Given a physical device is connected and an emulator is running, when the device picker is opened, then both appear in distinct Physical/Emulator sections and arrow keys plus Enter select an entry. | must | test |
| AC-3 | Given a device is selected in the picker, when the user clicks 'Start mirroring', then the live mirror begins streaming. | must | test |
| AC-4 | Given no device is connected, when adb polling runs, then the 'NO SIGNAL' state shows with guidance text and an inline emulator picker plus Boot button. | must | demo |
| AC-25 | Given live mirror is active, then the status bar displays current tool version, resolution, fps, and latency, updating at least once per second. | should | demo |
| AC-41 | Given adb is not found on PATH at launch, when tool detection runs, then F-2 exposes a typed ToolMissing discovery state (and reflects it as an offline/warning toolbar status dot) for error-handling-and-recovery (F-3) to render as the actionable error card (comp 04) — F-2 itself does not build the Locate-adb/Retry/Copy UI. | must | test |
| AC-42 | Given adb is installed but reports zero attached devices across repeated polls, when polling runs, then F-2 exposes a typed NoDeviceFound discovery state carrying poll metadata (last-poll time, attempt count) for F-3 to render the escalated error card (comp 05, Restart adb / Boot emulator instead) — F-2's own stage still shows only the passive NO SIGNAL state from AC-4. | must | test |
| AC-43 | Given no device has ever been selected, when the app has focus, then the device picker renders as a centered panel filling the stage's content slot on Theme.color.bg (not the dotted stage background), matching comps 01/03; given a device is already selected, clicking the toolbar DeviceChip reopens the same panel. There is no separate small anchored dropdown menu implementation. | must | demo |
| AC-44 | Given any adb/idb command needs to run, then it is issued only through the single device-scoped command executor, whose API requires an already-resolved device id as a non-optional parameter — no code path in the codebase can construct or issue a device command without one (NFR-5 enforced by API shape). | must | test |
| AC-45 | Given live mirror is active, then the status bar shows tool version and resolution unconditionally, fps reflecting the active transport's real achieved rate, and latency either as a real measurement or an explicit placeholder (never a fabricated number) when the active transport cannot measure it end-to-end — all updating at least once per second. | should | demo |

### scenarios

| id | title | type | steps |
| --- | --- | --- | --- |
| SC-1 | First-run setup and tool detection | happy | ["Launch app", "Detect adb/idb and show status dots", "Open device picker showing Physical/Emulator sections", "Select a device via keyboard or click", "Click Start mirroring"] |
| SC-2 | No device connected (empty state) | edge | ["No device is connected", "App polls adb every 2s", "Show NO SIGNAL placeholder with guidance", "Offer inline emulator picker + Boot button"] |
| SC-6 | adb not on PATH at launch | error | ["Launch app with adb not on PATH", "Tool detection fails for Android", "Toolbar status dot shows offline/warning", "F-2 emits ToolMissing discovery state", "F-3 renders the actionable error card (comp 04) from that state"] |
| SC-7 | adb present, zero devices sustained across polls | error | ["adb is installed and running", "Repeated 2s polls each report zero attached devices", "F-2's stage continues showing the passive NO SIGNAL state (AC-4)", "F-2 emits NoDeviceFound with poll metadata", "F-3 renders the escalated error card (comp 05) from that state"] |

### definitionOfReady

- Preparation is gate-approved with readyForPlan: true (approved under survey option B, NFR-1 waiver flagged for pre-ship re-review).
- Branch sdd/android-device-discovery-mirroring is cut from development, which contains the merged F-1 shell.
- All five comps are cached under raw/design/ and have been read; 01/03 are the picker, 02 is NO SIGNAL, 04/05 belong to F-3.
- The classpath verifications are done, not assumed: decodeToImageBitmap via components.resources at FeatureConventionPlugin.kt:115; kotlinx-coroutines-test wired at data-preference-impl/build.gradle.kts:16 but ABSENT from data-repository-impl; kotlin.test from both plugins; zero exhaustive `when` over Failure; no library module applies the `di` plugin.
- Both survey decisions are answered: Q1 (explicit CTA vs auto-start) before step 9/10/11, Q2 (commonMain vs desktopMain wiring) before step 8.
- A local adb and either a USB-debug device or one installed AVD are available for the demo and measurement steps (not for automated tests).
- The twelve binding decisions are treated as settled; reopening any of them requires a new gate.

### definitionOfDone

- All 10 AC are met: the five 'test' ones (AC-2, AC-3, AC-41, AC-42, AC-44) by green automated checks plus the AC-44 grep inspection, and the five demo-only ones (AC-1, AC-4, AC-25, AC-43, AC-45) by a recorded manual walkthrough of ./gradlew :desktop-application:run. 5 + 5 = 10, with no AC counted twice.
- ./gradlew :presentation-core-styling:desktopTest :presentation-core-ui:desktopTest :data-preference-impl:desktopTest :data-repository-impl:desktopTest :data-device-api:desktopTest :data-device-impl:desktopTest :presentation-feature-mirror:desktopTest is green — F-1's four tasks plus this spec's three. Stated honestly: :data-device-impl:desktopTest is RESERVED, not populated. That module plans no tests because everything testable in it was hoisted into data-device-api; the task is listed so a future test there cannot be silently skipped.
- ./gradlew :data-device-api:compileTestKotlinIosArm64 :data-device-impl:compileTestKotlinIosArm64 :presentation-feature-mirror:compileTestKotlinIosArm64 is green — the F-1 backtick-name trap (fixed at a71d232) does not recur.
- grep confirms java.lang.ProcessBuilder appears in exactly one file in the repository, and that file is internal and under data-device-impl/src/desktopMain.
- RectifyShellContent is READ, not grepped, and confirmed to resolve nothing from Koin, touch no AWT type and launch no coroutine in its body or callee set, and to compile with every new parameter defaulted. A file-level import grep is explicitly NOT the check: RectifyWindow.kt already imports koinInject, java.awt.Dimension and kotlinx.coroutines.launch for RectifyWindow itself.
- git diff on gradle/libs.versions.toml is empty. The expected build-file diff is exactly: three new build.gradle.kts files; one commonTest block plus one projects.dataDeviceApi line in data-repository-impl; two projects.* lines in compose-application; three includes in settings.gradle.kts.
- The v1 capture rate (fps) and per-frame latency are MEASURED on a real device and written into the delivery notes with the device model, resolution and link type; the chosen MirrorTuning.CaptureFloorMs is justified against that number.
- The picker and NO SIGNAL stage are compared against comps 01/02/03 with F-1's normalise-and-compare-landmarks method, and any deviation is recorded rather than eyeballed away.
- The NO SIGNAL subtitle copy matches the shipped start behaviour — no screen promises auto-start next to a button that requires a click (survey Q1).
- New CLAUDE.md files exist for the three new modules, each stating the plain-backtick-names rule, and presentation-feature-mirror's also recording the MirrorHost-not-MirrorScreen deviation. presentation-core-ui/CLAUDE.md component tables are updated with this spec's six additions AND the F-1 additions they still miss (DeviceChip, KeyCap, CounterBadge, StatusPill, CommandBlock, ErrorCard, ErrorGlyph, GlyphBadge, SourceListRow/SourceListHeader, StatusLine, FeedbackBar, SidebarTabs, MeasureTag, AnnotationPopover and the rest).
- The NFR-1 waiver, with its measured number, is restated in the delivery notes as flagged-for-re-review-before-ship.

### successMetrics

- A human can launch the desktop app, see real tool versions, pick a real device with the keyboard alone, and watch their own phone screen inside the Rectify window — the first end-to-end demo of the product.
- Zero new entries in gradle/libs.versions.toml.
- Exactly one file in the repository names ProcessBuilder.
- Every parser and argv builder this spec adds is covered by a test that runs with no phone attached.
- The reported fps equals the achieved fps (no invented constant), and latency reads as an explicit placeholder rather than a number nobody measured.
- F-3 can be started against F-2's typed DiscoveryModel without F-2 changing shape.

### architecture

#### approach

One vertical slice through the existing data → domain → presentation layering, with process execution quarantined in one desktop-only module and every pure function hoisted into commonMain so it is testable without hardware. Three new modules: data-device-api (`library` alone — contracts plus ALL parsers, no Koin, no core/ directory), data-device-impl (`library` alone plus api(projects.dataDeviceApi), koin-core and kermit — the only ProcessBuilder in the repo, desktopMain-only and internal, behind an internal expect fun Module.provideDeviceSources() copying data-preference-impl's provideSettings() pattern exactly), and presentation-feature-mirror (`feature` + `di`, mirroring presentation-feature-settings minus navigation-api — Orbit MVI with MirrorHost rather than *Screen because this surface is a shell slot set, not a navigation destination). data-repository-impl gains DeviceRepositoryImpl, four mappers, the 2s polling flow and its first commonTest dependency block. domain-core gains the device model vocabulary and Failure.Technical.Process; five use cases follow the existing one-interface-one-invoke shape. compose-application gains two project dependencies and two initKoin entries.

#### rationale

Every decision was taken against two filters: does it make a claim true by construction (type system, module boundary, API shape) rather than by convention, and does it stay proportional to a PRD 'L' estimate for discovery plus first connection. Putting the parsers in data-device-api/commonMain converts AS-7 ('no phone in CI') from an aspiration into a structural fact. Putting ProcessBuilder behind internal + a module boundary + explicitApi() makes NFR-5 unbreakable rather than documented. Hoisting probeCandidates() out of ToolResolver into a pure commonMain function makes the highest-likelihood, highest-impact risk in the spec (the packaged-.app PATH failure) unit-testable with a fake env map. Keeping pixels in a StateFlow outside Orbit state stops every future annotation feature inheriting a reducer pass per frame. Plugin selection follows what the repo does rather than what reads well. Reuse was preferred everywhere the kit allowed: eight shipped components are used as-is, one gains a single additive parameter, and only six new UI pieces are authored — each justified against a verified absence rather than an assumed one (a full listing of the 110 kit files plus a DropdownMenu grep confirms there is no select control and nothing that draws a bitmap).

#### alternativesConsidered

| title | whyRejected |
| --- | --- |
| Apply the `di` convention plugin to data-device-api and data-device-impl, as Preparation decision 6 worded it | Rejected at refine against the actual repo: zero library modules apply `di`. data-preference-impl and data-repository-impl declare implementation(libs.koin.core) directly; data-preference-api declares no Koin at all. DiConventionPlugin also adds koin-compose and koin-viewmodel, neither usable in a data module. Mirroring the real pattern costs one line and avoids two dead dependencies per module. |
| One combined data-device module holding both interfaces and implementation | data-repository-impl would have to depend on an impl module (no other slice does), tests would pull desktop process code onto the classpath, and the api/impl symmetry preferences and database already establish would break. Saves one build file, costs the structural guarantee that is the point of NFR-5. |
| Put the mirror ViewModel in compose-application/desktopMain next to its only consumer | compose-application/CLAUDE.md explicitly forbids feature logic and ViewModels there, Orbit and koin-viewmodel are not wired into that module, and desktopMain placement would make the state machine untestable from commonTest. |
| Wire the two new modules into compose-application's desktopMain instead of commonMain | Recommended against but genuinely arguable, so it is survey Q2. initKoin() lives in commonMain and imports each Koin module by package, so desktopMain wiring would force initKoin to split or take modules as a parameter — a change to the file every platform entry point calls, to save linking two KMP modules that already compile for every target. UnsupportedDeviceSources exists precisely so the graph stays uniform across platforms. |
| Reuse Failure.Technical.Platform for process failures | Platform is documented as Android/iOS OS-level errors — the mobile runtime, not a host CLI. F-3 must distinguish 'adb missing' from 'adb failed' to choose comp 04 vs comp 05, and would be reduced to string-matching messages. |
| Fixed-interval capture timer firing every 100ms regardless of completion | Stacks overlapping adb processes the moment a capture exceeds the interval — the exact failure that hangs USB and spawns orphans — and would make the status bar report the requested rate rather than the achieved one, violating AC-45. |
| Launch scrcpy as a child process | It opens its own OS window and cannot composite into StageSurface at all, breaking F-1's canonical-window contract. Structurally prevented: the closed DeviceCommand set contains no scrcpy command. |
| Route picker keys through RectifyWindow's existing onPreviewKeyEvent | The window would have to know whether the picker is open and which row is highlighted, pushing feature state into the shell; preview dispatch runs root-first, so the window would start intercepting arrow keys from every future focused control. |
| Push decoded frames through the Orbit reducer as part of MirrorUiState | Up to 10 reducer passes and 10 full state copies per second, inherited by every downstream annotation feature — and a ByteArray-bearing data class compares by identity, so the copies would never deduplicate anyway. Pixels are a stream; UI semantics are a state machine. |
| Add orbit-test and/or turbine to make ViewModel tests ergonomic | Two new catalog entries for test ergonomics against a hard zero-new-dependency constraint. runTest + Dispatchers.setMain + advanceUntilIdle covers it; if it proves flaky the fallback is extracting more pure helpers. |
| A separate small anchored dropdown for the toolbar DeviceChip in addition to the full-stage panel | AC-43 settles it and comp 01 confirms it: the whole stage area is a plain undotted surface with a centred column. Two picker implementations would be two things to keep in sync for one user intention. |
| Auto-start mirroring on the first discovered device, as comp 02's subtitle promises | Recommended against but real, so it is survey Q1. FR-4 mandates a primary 'Start mirroring' action, AC-3 verifies it, and comp 01 draws it. Auto-start would stream a phone screen into the window with no user act of consent, and F-2 ships no stop/reconnect UX to recover from a surprise stream. The cheaper honest fix is one string: the NO SIGNAL subtitle stops promising auto-start. |

#### components

| name | kind | status | registryRef | responsibility |
| --- | --- | --- | --- | --- |
| DeviceId | data | new |  | data-device-api/src/commonMain/kotlin/data/device/api/source/model/DeviceId.kt — @JvmInline value class with a PRIVATE constructor and a `public fun of(raw: String): DeviceId?` factory rejecting blank, whitespace-bearing, leading-dash and non-[A-Za-z0-9._:-] input. The only other mint point is parseAdbDevices, which routes through of(). First half of NFR-5-by-construction. |
| DeviceCommand | data | new |  | data-device-api/.../source/model/DeviceCommand.kt — closed sealed interface: Screencap, WmSize, WmDensity, GetProp(name), EmuAvdName. No vararg runner, no free text, no scrcpy, no connect/-H/-P. Adding a capability is a visible API change. |
| DeviceCommandExecutor | data | new |  | data-device-api/.../source/datasource/DeviceCommandExecutor.kt — `public suspend fun run(device: DeviceId, command: DeviceCommand): ByteArray`. One method, one overload, device non-optional and undefaultable (AC-44). |
| AdbHostSource / EmulatorSource | data | new |  | data-device-api/.../source/datasource/ — the deviceless half, enumerated explicitly: AdbHostSource.probe()/listDevices(); EmulatorSource.probe()/listAvds()/boot(AvdName). Separate interfaces so 'deviceless' is an auditable exception with five named members rather than a nullable parameter on the device executor. |
| MirrorTransport | data | new |  | data-device-api/.../source/datasource/MirrorTransport.kt — exactly two members: `fun frames(device: DeviceId): Flow<MirrorFrame>` and `val stats: StateFlow<TransportStats>`. FR-25's swap seam. Flow needs no new dependency: LibraryConventionPlugin already puts kotlinx-coroutines-core on commonMain (line 104). |
| Pure parsers (parseAdbDevices / parseWmSize / parseWmDensity / parseAvdList / trimToPngSignature) | logic | new |  | data-device-api/.../source/parse/ — side-effect-free commonMain functions ported from rectify-poc. parseAdbDevices types the state column (Device/Unauthorized/Offline/NoPermissions/Recovery/Sideload/Unknown) and KEEPS non-device entries (the poc dropped them). parseWmSize deliberately prefers Override size over Physical size. trimToPngSignature drops the foldable warning prefix and never throws on a buffer with no signature. |
| buildDeviceArgv / buildHostArgv | logic | new |  | data-device-api/.../source/parse/DeviceArgv.kt — the single argv construction point, pure and in commonMain. buildDeviceArgv always emits [adbPath, "-s", device.value, …]; buildHostArgv never emits -s. The AC-44 test asserts this for every DeviceCommand variant. |
| probeCandidates | logic | new |  | data-device-api/.../source/tool/ToolProbeCandidates.kt — pure `fun probeCandidates(tool: ToolKind, env: Map<String, String>, homeDir: String): List<String>` returning $ANDROID_HOME/platform-tools/x, $ANDROID_SDK_ROOT/platform-tools/x, ~/Library/Android/sdk/platform-tools/x, /opt/homebrew/bin/x, /usr/local/bin/x, then PATH entries. Hoisted out of ToolResolver so the packaged-.app PATH risk is testable with a fake env. |
| MirrorTuning | logic | new |  | data-device-api/.../source/tuning/MirrorTuning.kt — the one calibration knob object: CaptureFloorMs=100, CaptureTimeoutMs=5000, HostCommandTimeoutMs=3000, DiscoveryPollMs=2000, StatsTickMs=1000, StderrTailBytes=2048, EmptyPollEscalation=3. The right floor differs per device, so it is a named constant, never a literal inside the loop. |
| ProcessRunner | data | new |  | data-device-impl/src/desktopMain/kotlin/data/device/impl/core/process/ProcessRunner.kt — internal, the ONLY file in the repository naming ProcessBuilder. runInterruptible(Dispatchers.IO), two concurrent async drains for stdout and stderr (the poc's deadlock note preserved), withTimeoutOrNull never withTimeout, destroyForcibly() in finally, live-child registry plus one shutdown hook, stderr tail capped at MirrorTuning.StderrTailBytes. Every JVM-only import stays in desktopMain (F-1's CON-1). |
| ToolResolver | data | new |  | data-device-impl/src/desktopMain/.../core/tool/ToolResolver.kt — internal. Applies an executable-file predicate to probeCandidates()'s ordered list, runs `adb --version` / `emulator -version` / `idb --version` through ProcessRunner, returns ToolProbe(found, path, version, probedPaths, installCommand, envHint). Contains no ordering logic of its own. |
| ScreencapMirrorTransport | data | new |  | data-device-impl/src/desktopMain/.../source/transport/ScreencapMirrorTransport.kt — internal. Adaptive single-flight loop: issue `adb -s <id> exec-out screencap -p`, trim to the PNG signature, emit MirrorFrame, then delay only the remainder of the 100ms floor. One capture in flight; a capture still running when the next issue is due increments droppedFrames rather than spawning a second child. Width/height read from the PNG IHDR so rotation is followed, not cached. |
| UnsupportedDeviceSources | data | new |  | data-device-impl/src/commonMain/.../core/unsupported/UnsupportedDeviceSources.kt — internal no-op implementations registered by the android and iOS actuals of provideDeviceSources(), so common code compiles and the Koin graph resolves on all four targets (NFR-10). Every call returns an empty result or a Failure.Technical.Process. |
| provideDeviceSources | data | new |  | data-device-impl/src/commonMain/.../source/DeviceSourcesProvider.kt declares `internal expect fun Module.provideDeviceSources()`; actuals in DeviceSourcesProvider.desktop.kt/.android.kt/.ios.kt. Byte-for-byte the provideSettings() pattern: same file naming, same internal expect on Module, called from the module's single public Koin module di/DataDeviceImplModule.kt exposing `public val dataDeviceImplModule: Module`. |
| DeviceRepositoryImpl + device mappers | data | new |  | data-repository-impl/src/commonMain/.../source/DeviceRepositoryImpl.kt plus core/mapper/{DeviceResourceMapper,EmulatorResourceMapper,ToolProbeMapper,MirrorFrameMapper}.kt — internal objects in the existing ModelResourceMapper toModel/toResource shape, never throwing. Owns the 2s polling flow, the de-duplication of an AVD both listed and running, and the empty-poll counter that escalates an empty Found into NoDeviceFound with lastPollEpochMs/attempts/attachedCount after MirrorTuning.EmptyPollEscalation polls (AC-42). |
| DiscoveryModel | logic | new |  | domain-core/src/commonMain/.../source/model/device/DiscoveryModel.kt — sealed: Detecting \| ToolMissing(tool, probedPaths, installCommand, envHint) \| NoDeviceFound(toolVersion, lastPollEpochMs, attempts, attachedCount) \| Found(physical, emulators). THE F-2→F-3 contract. A missing binary at detection time is a SUCCESS value here, never an error. |
| Failure.Technical.Process | logic | new |  | domain-core/src/commonMain/.../source/monad/Failure.kt — one new data class identical in shape to its four verified siblings: `public data class Process(override val cause: Throwable) : Technical()`. The cause is DeviceCommandException(argv, exitCode, stderrTail). Verified safe: no exhaustive `when` over Failure exists in the repo. |
| Device use cases (DetectTools / ObserveDeviceDiscovery / BootEmulator / StartMirroring / ObserveMirrorStats) | logic | new |  | domain-usecase-api + domain-usecase-impl, source/usecase/device/. Suspend one-shots (DetectTools, BootEmulator) return Result<T> / Optional through resultLauncher(errorMapper = Failure.Technical::Process); reactive ones return a bare Flow<T>, per domain-layer.md. withTimeoutOrNull only, with the reason commented at each site. |
| RadioMark | ui | new |  | presentation-core-ui/.../kit/atom/indicator/RadioMark.kt — SourceListRow's existing `private fun RadioMark(selected: Boolean)` Canvas (SourceListRow.kt:101-113, ring Theme.color.line2 + fill Theme.color.ink) promoted to a public atom with no visual change, so PlatformToolCard reuses it instead of a second copy. SourceListRow switches to the atom in the same commit. |
| SourceListRow (amended) | ui | reuse | presentation-core-ui/src/commonMain/kotlin/presentation/core/ui/source/kit/molecule/row/SourceListRow.kt | Reused as the picker's device row exactly as shipped. ONE additive change: `enabled: Boolean = true`, which dims the row and drops the .clickable for unauthorized/offline devices, and is announced as disabled rather than merely dimmed. The default preserves every existing call site and its own preview. |
| PlatformToolCard | ui | new |  | presentation-core-ui/.../kit/molecule/card/PlatformToolCard.kt — comp 01's PLATFORM cards: title, trailing RadioMark, status-dot + mono line ('adb 35.0.1 · /opt/homebrew/bin'). Authored rather than reusing SelectableCard, which has no radio, no status dot, renders its subtitle as a plain String, and has three live call sites in Settings and Onboarding that bending it would change. |
| EmulatorSelect | ui | new |  | presentation-core-ui/.../kit/molecule/select/EmulatorSelect.kt — comp 02's inline AVD dropdown. Justification VERIFIED at refine: listing all 110 kit files and grepping the module for DropdownMenu/ExposedDropdown returns nothing — the kit has no select control at all. Material3 DropdownMenu styled entirely with Theme tokens; renders disabled with an explanatory label when zero AVDs exist rather than opening an empty menu. Reused unchanged by F-3's 'Boot emulator instead'. |
| MirrorFrameView | ui | new |  | presentation-core-ui/.../kit/molecule/mirror/MirrorFrameView.kt — dumb and stateless: draws an ImageBitmap aspect-fit with FilterQuality.Medium, opaque where it covers, with a contentDescription naming the mirrored device. Nothing in the kit draws a bitmap. The transport swap never touches it. |
| DevicePickerPanel | ui | new |  | presentation-core-ui/.../kit/organism/picker/DevicePickerPanel.kt — comps 01/03. An opaque Theme.color.bg surface filling StageSurface's content slot with a centred content column (comp 01 confirms no dots behind the panel), composing SectionHeader, PlatformToolCard, SourceListHeader, SourceListRow, KeyCap and AppButton. Owns the FocusRequester + Modifier.onPreviewKeyEvent on its root and translates key events into callbacks; holds no selection state of its own. |
| NoSignalStage | ui | new |  | presentation-core-ui/.../kit/organism/stage/NoSignalStage.kt — comp 02 / AC-4: the dashed phone outline (F-1 shipped only a private preview copy at StageSurface.kt:97-130 using PathEffect.dashPathEffect — port that geometry, do not re-derive it), the title, the subtitle whose copy must agree with the shipped start behaviour (survey Q1), two numbered steps, EmulatorSelect + Boot, and the 'adb devices · polling every 2 s' footer. Renders ON the dotted stage, unlike the picker. |
| MirrorViewModel | logic | new |  | presentation-feature-mirror/src/commonMain/.../source/mirror/MirrorViewModel.kt — all semantic state: platform selection, discovery, picker open/highlight index, session, stats. Owns the polling gate (discovery pauses while a session streams), the 1s stats ticker, flatMapLatest session switching, PNG decode on Dispatchers.Default via decodeToImageBitmap(), and `public val frame: StateFlow<ImageBitmap?>` held OUTSIDE the Orbit state. |
| MirrorHost | ui | new |  | presentation-feature-mirror/.../source/mirror/MirrorHost.kt — the entry point, named Host not Screen and documented as such in the module CLAUDE.md: there is no Destination for this surface, so none of navigation.md's five-step checklist applies. Resolves the ViewModel via koinViewModel(), collects state, frame and side effects, and hands them to a content lambda. |
| PickerNavigation / StatsFormatter | logic | new |  | presentation-feature-mirror/src/commonMain/.../core/picker/PickerNavigation.kt and core/stats/StatsFormatter.kt — internal pure helpers: nextSelectableIndex(rows, from, direction) skipping non-selectable rows and clamping at both ends, and formatStatusLine(tool, resolution, fps, latencyMs) producing 'adb 35.0.1 · 1080x2400 · 4.2 fps · polling · latency —'. Pure, so they are the cheapest and most durable tests in the slice. |
| RectifyShellContent (amended) | ui | reuse | compose-application/src/desktopMain/kotlin/dev/rectify/io/source/window/RectifyWindow.kt:175 | Today `internal fun RectifyShellContent(state: ShellState, onPanelWidthCommit, onThemeCycle, modifier = Modifier, sidePanel = null)` with every slot a static stub. Gains exactly three DEFAULTED parameters — `mirror: MirrorUiState = MirrorUiState()`, `frame: ImageBitmap? = null`, `onIntent: (MirrorIntent) -> Unit = {}` — and maps them onto existing slots: platformSwitch → SegmentedButtonGroup with the real index (replacing the hardcoded 0 at line 186), deviceIndicator → DeviceChip with real name/state (replacing the 'No device' stub at line 189), statusBar leading → StatusIndicator + an inline mono stat Row, stage → DevicePickerPanel \| NoSignalStage \| MirrorFrameView \| iOS-deferred notice. Body stays Koin/AWT/coroutine-free; defaults keep the render harness working from literals. |
| SegmentedButtonGroup | ui | reuse | presentation-core-ui/.../kit/molecule/button/SegmentedButtonGroup.kt | FR-2 platform switch — already in the toolbar slot with PLATFORM_SOURCES; only selectedIndex and onSelect become real. |
| DeviceChip + DeviceChipState | ui | reuse | presentation-core-ui/.../kit/molecule/chip/DeviceChip.kt | Toolbar deviceIndicator. States map 1:1: Ready = connected and streaming, Warn = selected but degraded/unauthorized, Lost = device vanished mid-session, None = nothing selected. onClick reopens the picker (AC-43). |
| StatusIndicator + StatusIndicatorState | ui | reuse | presentation-core-ui/.../kit/atom/indicator/StatusIndicator.kt | Status-bar leading slot: Offline while detecting or tool-missing, Standby while polling with no device, Active while streaming, Connected when tools are ready and a device is selected but not yet streaming. |
| AppButton (Secondary / ListAction / primary) | ui | reuse | presentation-core-ui/.../kit/atom/button/AppButton.kt | 'Start mirroring' CTA and the inline Boot buttons — AppButtonSize.ListAction exists for a button inside a row, as SourceListRow's own preview already demonstrates. |
| KeyCap | ui | reuse | presentation-core-ui/.../kit/atom/keycap/KeyCap.kt | Comp 01's '↑↓ select · ↵ start' hint line. |
| SectionHeader / SourceListHeader | ui | reuse | presentation-core-ui/.../kit/atom/text/SectionHeader.kt, .../kit/molecule/row/SourceListRow.kt:122 | The uppercase PLATFORM / SOURCE labels and the 'Physical device' / 'Emulator' group headers in comps 01/03 — both exist and need no change. |
| StageSurface / ShellToolbar / ShellStatusBar / RectifyWindowShell | ui | reuse | presentation-core-ui/.../kit/organism/shell/ | Every surface this spec fills. No shell component changes; only their slots get real content. |
| ErrorCard / CommandBlock / ErrorGlyph / GlyphBadge | ui | reuse | presentation-core-ui/.../kit/organism/error/ErrorCard.kt, .../kit/molecule/command/CommandBlock.kt | NOT used by F-2 — listed to make the boundary explicit. Comps 04/05's chrome already exists (verified present), so F-3 composes shipped components against F-2's typed states and authors almost no new UI. F-2 must therefore keep ToolProbe.installCommand and DiscoveryModel's poll metadata in a shape those components consume directly. |
| resultLauncher + Failure hierarchy | logic | reuse | domain-usecase-impl/core/ResultLauncher.kt, domain-core/source/monad/Failure.kt | The mandatory error path for every use case body. One new Technical subtype, no structural change to the helper — its CancellationException hazard is a separate flagged follow-up. |
| ModelResourceMapper | logic | reuse | data-repository-impl/core/mapper/base/ModelResourceMapper.kt | The mapping shape for all four device mappers, with SidePanelWidthPreferenceMapperTest and ThemePreferenceMapperTest (verified present in data-repository-impl/src/commonTest) as the live precedent to copy — including their punctuation-free backtick naming. |
| provideSettings() expect/actual DI pattern | data | reuse | data-preference-impl/src/commonMain/.../source/SettingsProvider.kt + 3 actuals | The exact mechanism copied for provideDeviceSources(): internal expect fun Module.x() in commonMain/source/, one actual per platform in <platform>Main/source/ named X.<platform>.kt, called from the module's single public Koin module. |
| CounterBadge / stagedCounter slot | ui | reuse | presentation-core-ui/.../kit/atom/badge/CounterBadge.kt | Left at the F-1 stub (count 0, CounterBadgeState.Empty) — the annotation spec owns it. Recorded so a reviewer does not read the untouched slot as an F-2 omission. |

#### dataModel

| entity | fields |
| --- | --- |
| DeviceId (data-device-api, value class, private constructor) | ["value: String"] |
| DeviceCommand (data-device-api, closed sealed interface) | ["Screencap", "WmSize", "WmDensity", "GetProp(name: String)", "EmuAvdName"] |
| DeviceResource (data-device-api) | ["id: DeviceId", "state: DeviceStateResource", "model: String?", "product: String?", "transport: String?", "isEmulator: Boolean"] |
| DeviceStateResource (data-device-api, enum) | ["Device", "Unauthorized", "Offline", "NoPermissions", "Recovery", "Sideload", "Unknown"] |
| AvdResource (data-device-api) | ["name: AvdName", "runningAs: DeviceId?"] |
| ToolProbe (data-device-api) | ["kind: ToolKind", "found: Boolean", "path: String?", "version: String?", "probedPaths: List<String>", "installCommand: String", "envHint: String"] |
| DeviceInfoResource (data-device-api) | ["widthPx: Int", "heightPx: Int", "densityDpi: Int", "model: String", "apiLevel: Int"] |
| MirrorFrame (data-device-api) — NOT a value-equality type: the ByteArray makes equals identity-based, so it must never be a reduced-state field or a distinctUntilChanged key | ["bytes: ByteArray", "widthPx: Int", "heightPx: Int", "capturedAtMs: Long"] |
| TransportStats (data-device-api) | ["fps: Float", "latencyMs: Long?", "droppedFrames: Int", "widthPx: Int", "heightPx: Int"] |
| DeviceCommandException (data-device-api, public — the cause carried in Failure.Technical.Process) | ["argv: List<String>", "exitCode: Int?", "stderrTail: String"] |
| DeviceModel (domain-core) | ["id: String", "displayName: String", "serial: String", "state: DeviceStateModel", "isEmulator: Boolean", "androidVersion: String?", "transportLabel: String?", "isSelectable: Boolean"] |
| EmulatorModel (domain-core) | ["avdName: String", "isRunning: Boolean", "runningDeviceId: String?"] |
| ToolModel (domain-core) | ["platform: PlatformModel", "isInstalled: Boolean", "version: String?", "path: String?", "probedPaths: List<String>", "installCommand: String"] |
| DiscoveryModel (domain-core, sealed) | ["Detecting", "ToolMissing(tool, probedPaths, installCommand, envHint)", "NoDeviceFound(toolVersion, lastPollEpochMs, attempts, attachedCount)", "Found(physical: List<DeviceModel>, emulators: List<EmulatorModel>)"] |
| MirrorFrameModel (domain-core) — same ByteArray identity-equality caveat as MirrorFrame | ["bytes: ByteArray", "widthPx: Int", "heightPx: Int", "capturedAtMs: Long"] |
| MirrorStatsModel (domain-core) | ["toolVersion: String", "widthPx: Int", "heightPx: Int", "fps: Float", "latencyMs: Long?", "droppedFrames: Int"] |
| PlatformModel (domain-core, enum) | ["Android", "Ios"] |
| MirrorUiState (presentation-feature-mirror) — carries no frame bytes by design | ["platform: PlatformModel = Android", "tools: List<ToolModel> = emptyList()", "discovery: DiscoveryModel = Detecting", "isPickerOpen: Boolean = true", "highlightedIndex: Int = 0", "selectedDeviceId: String? = null", "session: SessionUiState = Idle", "stats: MirrorStatsModel? = null", "avds: List<EmulatorModel> = emptyList()", "selectedAvd: String? = null"] |

#### interfaces

| name | contract |
| --- | --- |
| DeviceCommandExecutor | public suspend fun run(device: DeviceId, command: DeviceCommand): ByteArray — the sole device-scoped execution path; throws DeviceCommandException on non-zero exit, timeout or missing binary. |
| AdbHostSource | public suspend fun probe(): ToolProbe; public suspend fun listDevices(): List<DeviceResource> — deviceless adb calls only; a missing binary yields ToolProbe(found = false, …), never an exception. |
| EmulatorSource | public suspend fun probe(): ToolProbe; public suspend fun listAvds(): List<AvdResource>; public suspend fun boot(avd: AvdName) — boot spawns detached and untimed; a lock-file non-zero exit for an already-running AVD is a no-op success. |
| MirrorTransport | public fun frames(device: DeviceId): Flow<MirrorFrame>; public val stats: StateFlow<TransportStats> — FR-25's swap seam. Cancelling the collector stops the loop and destroys any in-flight child. |
| DeviceRepository (domain-repository-api) | public suspend fun detectTools(): List<ToolModel>; public fun observeDiscovery(): Flow<DiscoveryModel>; public suspend fun bootEmulator(avdName: String); public fun mirror(deviceId: String): Flow<MirrorFrameModel>; public fun observeMirrorStats(): Flow<MirrorStatsModel> — domain models only, never data-layer resource types. |
| DetectToolsUseCase | public suspend operator fun invoke(): Result<List<ToolModel>> |
| ObserveDeviceDiscoveryUseCase | public operator fun invoke(): Flow<DiscoveryModel> — bare Flow, never Flow<Result<T>> (domain-layer.md). |
| BootEmulatorUseCase | public suspend operator fun invoke(avdName: String): Optional — Optional = Result<Unit> from domain.usecase.api.source.monad. |
| StartMirroringUseCase | public operator fun invoke(deviceId: String): Flow<MirrorFrameModel> |
| ObserveMirrorStatsUseCase | public operator fun invoke(): Flow<MirrorStatsModel> |
| internal expect fun Module.provideDeviceSources() | Registers AdbHostSource, EmulatorSource, DeviceCommandExecutor and MirrorTransport singletons per platform; desktop registers the real implementations, android and iOS register UnsupportedDeviceSources. |
| MirrorHost | @Composable public fun MirrorHost(viewModel: MirrorViewModel = koinViewModel(), content: @Composable (MirrorUiState, ImageBitmap?, (MirrorIntent) -> Unit) -> Unit) — a host, not a screen: it owns no layout, it hands state down to whatever fills the shell slots. |

#### stateManagement

Orbit MVI in presentation-feature-mirror for UI semantics (MirrorContract/MirrorViewModel/MirrorContent + MirrorHost in place of the usual *Screen — a documented deviation from orbit-mvi.md's four-file shape, recorded in the module's CLAUDE.md, because this surface is a slot set rather than a navigation destination). Pixels deliberately bypass Orbit: MirrorViewModel decodes each PNG on Dispatchers.Default via decodeToImageBitmap() and publishes to a public StateFlow<ImageBitmap?> outside the reduced state, so up to 10 frames per second never become 10 reducer passes and 10 state copies — and no ByteArray-bearing type, which would compare by identity anyway, ever enters reduced state. Stats are reduced on a separate 1s ticker, not per frame. The 2s discovery cadence lives in DeviceRepositoryImpl's flow, not a ViewModel timer; session switching is flatMapLatest over the selected device, so rapid Start/Stop can never leave two capture loops alive. Lifetime is structural: window close → ViewModel cleared → viewModelScope cancelled → delay cancels → finally destroys children. Shell state (active tab, panel width) stays in F-1's ShellState, untouched.

#### navigation

- No Destination is added and NavigationHost is not touched — the desktop root is RectifyWindow, not the navigation graph, and this surface is a set of shell slots (the documented reason the entry point is MirrorHost). navigation.md's five-step checklist is deliberately not applied; this line exists so a reviewer does not flag the omission.
- Stage content is a state branch, not a route: DevicePickerPanel when isPickerOpen, NoSignalStage when discovery is ToolMissing/NoDeviceFound/empty Found, MirrorFrameView while a session streams, and a deferred-platform notice when the switch is on iOS.
- The toolbar DeviceChip reopens the same picker panel (AC-43); Escape closes it only when a device is already selected.
- F-1's window-level Cmd+1/2/3 handler (handleShellShortcut, RectifyWindow.kt:287, already gated on isMetaPressed via shortcutToTab) is unmodified; the picker owns unmodified keys only.

#### affectedModules

- NEW :data-device-api — `library` plugin ALONE (no `di`, no Koin). commonMain: implementation(projects.dataCore). No commonTest dependency block is needed: kotlin.test comes from the plugin and every test there is pure. Contracts, resource types, all parsers. No core/ directory.
- NEW :data-device-impl — `library` plugin ALONE. commonMain: api(projects.dataDeviceApi), implementation(projects.dataCore), implementation(libs.koin.core), implementation(libs.kermit). desktopMain holds the process execution, tool resolution and transport; androidMain/iosMain hold the unsupported actuals.
- NEW :presentation-feature-mirror — `feature` + `di`. Dependency block mirrors presentation-feature-settings minus projects.presentationCoreNavigationApi, and minus compose.material3 unless MirrorContent uses a Material3 composable directly. commonTest adds libs.kotlinx.coroutines.test.
- :domain-core — source/model/device/ package + Failure.Technical.Process.
- :domain-repository-api — DeviceRepository.
- :domain-usecase-api — five use case interfaces under source/usecase/device/.
- :domain-usecase-impl — five impls + DomainUseCaseImplModule registration.
- :data-repository-impl — DeviceRepositoryImpl, four mappers, DI registration, +implementation(projects.dataDeviceApi), AND a NEW commonTest.dependencies block adding libs.kotlinx.coroutines.test (the module has commonTest sources today but no commonTest dependency block at all).
- :presentation-core-ui — RadioMark extraction, SourceListRow `enabled`, PlatformToolCard, EmulatorSelect, MirrorFrameView, DevicePickerPanel, NoSignalStage, CLAUDE.md tables.
- :presentation-core-localisation — 27 mirror_* keys in src/commonMain/composeResources/values/strings.xml.
- :compose-application — RectifyShellContent parameters and live slots, RectifyWindow injection, TWO new commonMain project dependencies (projects.dataDeviceImpl, projects.presentationFeatureMirror) and two initKoin entries.
- settings.gradle.kts — three includes under the data and presentation-feature groups, 24 → 27 modules.

### projectContext

#### reuseComponents

| id | name | registryRef | note |
| --- | --- | --- | --- |
| SourceListRow | SourceListRow + SourceListHeader | presentation-core-ui/src/commonMain/kotlin/presentation/core/ui/source/kit/molecule/row/SourceListRow.kt | Picker rows and group headers. One additive parameter (`enabled: Boolean = true`); everything else reused as shipped. |
| SegmentedButtonGroup | SegmentedButtonGroup | presentation-core-ui/.../kit/molecule/button/SegmentedButtonGroup.kt | FR-2 platform switch; already in the toolbar slot with the right items. |
| DeviceChip | DeviceChip + DeviceChipState | presentation-core-ui/.../kit/molecule/chip/DeviceChip.kt | Toolbar deviceIndicator; Ready/Warn/Lost/None already match the four device situations. |
| StatusIndicator | StatusIndicator + StatusIndicatorState | presentation-core-ui/.../kit/atom/indicator/StatusIndicator.kt | Status-bar leading slot; Connected/Offline/Standby/Active cover every F-2 state. |
| AppButton | AppButton + AppButtonVariant + AppButtonSize | presentation-core-ui/.../kit/atom/button/AppButton.kt | Start mirroring CTA and inline Boot buttons; ListAction exists for in-row buttons. |
| KeyCap | KeyCap | presentation-core-ui/.../kit/atom/keycap/KeyCap.kt | The '↑↓ select · ↵ start' hint. |
| SectionHeader | SectionHeader | presentation-core-ui/.../kit/atom/text/SectionHeader.kt | Uppercase PLATFORM / SOURCE labels. |
| ShellKit | RectifyWindowShell / ShellToolbar / ShellStatusBar / StageSurface / ShellState | presentation-core-ui/.../kit/organism/shell/ | Every surface filled; no shell component changes. |
| ErrorCard | ErrorCard + CommandBlock + ErrorGlyph + GlyphBadge | presentation-core-ui/.../kit/organism/error/ErrorCard.kt, .../kit/molecule/command/CommandBlock.kt | Not used by F-2 — recorded because their existence is why comps 04/05 belong to F-3 and why F-2 must shape ToolProbe/DiscoveryModel to feed them. |
| CounterBadge | CounterBadge | presentation-core-ui/.../kit/atom/badge/CounterBadge.kt | Toolbar stagedCounter stays at the F-1 stub (0 / Empty) — annotation owns it. |

#### reuseAssets

| id | name | registryRef | note |
| --- | --- | --- | --- |
| design-comps | Five cached comps | raw/design/01…05*.png | 01/03 picker, 02 NO SIGNAL are F-2's; 04/05 are F-3's and were read only to fix the data contract. All five re-read at refine. |
| poc-logic | rectify-poc Devices.kt | /Users/andrew-malitchuk/StudioProjects/rectify-poc/app/src/main/kotlin/rectify/Devices.kt | Parsing, capture and foldable-trim logic ported as pure functions; its flat structure and untyped errors discarded. |
| phone-outline | Dashed phone outline geometry | presentation-core-ui/.../kit/organism/shell/StageSurface.kt:97-130 (private StageSurfaceEmptyPreviewContent, PathEffect.dashPathEffect) | F-1 shipped it preview-only and left the real one to F-2 — port the geometry rather than re-deriving it. |

#### reuseStyles

| id | name | registryRef | note |
| --- | --- | --- | --- |
| Theme | Theme.color / spacing / typography / stroke / cornerToken / motion | presentation-core-styling (presentation.core.styling.core.Theme) | Every colour, dimension and text style comes from a token. Theme.color.bg for the picker panel (AC-43), Theme.color.stage for NO SIGNAL, Theme.typography.monoMeta for serials and the stat line. |
| AppTheme | AppTheme(mode, reducedMotion) | presentation-core-styling/source/theme/AppTheme.kt | The single provider; every new private @Preview wraps in it. |

#### existingFeatures

| id | name | registryRef | note |
| --- | --- | --- | --- |
| F-1 | app-shell-window-layout | .pipeline/sdd/docs/app-shell-window-layout/plan/plan.json | Merged to development; this spec fills its slots and must not alter its contracts. |
| preferences | data-preference-api/impl + ConfigureRepository | data-preference-impl/ | The module shape (library plugin alone + koin-core), the expect/actual DI provider pattern, and the commonTest precedent being copied. |
| orbit-features | presentation-feature-{about,home,onboarding,settings,splash} | presentation-feature-settings/ | The four-file Orbit shape and the exact feature+di build.gradle.kts dependency block being mirrored, minus navigation-api which MirrorHost does not need. |

#### newToBuild

| id | name | note |
| --- | --- | --- |
| M-1 | :data-device-api | New Gradle module (library alone) — contracts, resources, all pure parsers, probeCandidates, MirrorTuning, DeviceCommandException. |
| M-2 | :data-device-impl | New Gradle module (library alone + koin-core + kermit) — the repository's only ProcessBuilder, ToolResolver, four source impls, expect/actual DI. |
| M-3 | :presentation-feature-mirror | New Gradle module (feature + di) — Orbit MVI, MirrorHost, pure picker/stats helpers. |
| U-1 | RadioMark (atom) | Extracted from SourceListRow's private copy; no visual change. |
| U-2 | PlatformToolCard (molecule) | SelectableCard lacks a radio and a status-dot mono line, and has three live call sites. |
| U-3 | EmulatorSelect (molecule) | Verified: the kit has no select/dropdown control at all. |
| U-4 | MirrorFrameView (molecule) | Verified: nothing in the kit draws a bitmap. |
| U-5 | DevicePickerPanel (organism) | Comps 01/03's full-stage takeover; composes six existing components. |
| U-6 | NoSignalStage (organism) | Comp 02; F-1 deliberately left it to F-2 and shipped only the preview geometry. |
| D-1 | Failure.Technical.Process + device model package | domain-core additions; verified source-compatible. |
| L-1 | 27 mirror_* localisation keys | presentation-core-localisation/src/commonMain/composeResources/values/strings.xml. |

### resources

#### rules

| ref | why |
| --- | --- |
| CLAUDE.md#build-commands | Per-module build commands; the Kotlin daemon is capped at 3GB so each implementation step names one module. |
| .claude/rules/package-layout.md | core/ internal-only, source/ public, no empty core/ directory; explicitApi() polices the boundary at compile time. |
| .claude/rules/domain-layer.md | Suspend one-shots return Result<T>/Optional, reactive use cases return a bare Flow<T>; repositories return domain models only; presentation never imports a data-*-impl type. |
| .claude/rules/error-handling.md | Every use case body goes through resultLauncher(errorMapper = Failure.Technical::Process); the three-layer error path must not be short-circuited. |
| .claude/rules/koin-di.md | One public Koin module per Gradle module in di/<Name>Module.kt; initKoin order is data → domain → presentation and new entries append to their layer group. |
| .claude/rules/orbit-mvi.md | The four-file feature shape; MirrorHost replacing *Screen is a deviation that must be documented in the module's CLAUDE.md. |
| .claude/rules/design-tokens.md | No literal Color(0x…), no raw .dp at a call site, no hardcoded TextStyle; strict atom → molecule → organism direction for the six new components. |
| .claude/rules/compose-preview-convention.md | Every new content composable ends with a private @Preview wrapped in AppTheme and fed default state plus no-op callbacks. |
| .claude/rules/testing.md | kotlin.test is wired by the convention plugins; parsers and pure helpers are the highest-value targets; don't write a test per thin use case wrapper. |
| .claude/rules/convention-plugins.md | Read at refine and applied literally: `di` must be applied alongside library or feature, adds koin-core as api() plus koin-compose/koin-viewmodel — which is why the two data modules use `library` alone with a direct koin-core dependency instead. SDK versions never hardcoded in a module build file. |
| .claude/rules/platform-coverage.md | Every commonMain expect needs desktopMain, androidMain and iosMain actuals; desktop-only dependencies belong on desktopMain (F-1 shipped CON-1 by ignoring this). |
| .claude/rules/version-catalog-hygiene.md | Zero new catalog entries; a cataloged dependency is not a wired one — which is why coroutines-test was checked module-by-module rather than assumed, finding data-repository-impl's gap. |
| .claude/rules/mappers.md | ModelResourceMapper pairs in data-repository-impl/core/mapper/, one internal object per resource type, and a mapper never throws. |
| .claude/rules/preferences.md | Read for the api/impl module shape and the expect/actual provider pattern being copied, not for preferences themselves. |
| .claude/rules/localisation.md | mirror_* keys go in presentation-core-localisation/composeResources/values/strings.xml; key convention is <feature>_<element>; only values/ (English) exists today. |
| .claude/rules/compose-resources-namespace.md | Generated accessors are rectify_kmp.<module>.generated.resources.* and appear on the next build — never hand-edited. |
| .claude/rules/navigation.md | Read to confirm the opposite: no Destination is added, so none of its five-step checklist applies — recorded so a reviewer doesn't flag the omission. |
| .claude/rules/database.md | Scanned for the api/impl + KSP-per-target precedent; no database work in this spec and no KSP configuration is added. |
| .claude/rules/network.md | Scanned; confirms Failure.Technical.Network belongs to a future HTTP layer and is the wrong subtype for a host CLI failure. |
| .claude/rules/icon-branding-placeholder.md | Scanned; no new icon assets are introduced by this spec. |
| compose-application/CLAUDE.md | No feature logic or ViewModels in that module; DEMO_MODE stays false; initKoin order is load-order significant. |
| presentation-core-ui/CLAUDE.md | The kit inventory to browse before authoring; its component tables are stale and must be updated with the F-1 additions they miss plus this spec's six. |
| data-preference-impl/CLAUDE.md | The reference module layout for an impl module with an expect/actual platform provider. |

#### skills


#### agents


#### docs

| ref | why |
| --- | --- |
| build-logic/convention/src/main/kotlin/dev/rectify/io/convention/source/convention/feature/FeatureConventionPlugin.kt:115 | Proves composeDependencies.components.resources is on every feature module's commonMain — the decodeToImageBitmap answer, verified rather than assumed. Line 123 of the same file proves kotlin-test reaches commonTest. |
| build-logic/convention/src/main/kotlin/dev/rectify/io/convention/source/convention/library/LibraryConventionPlugin.kt:99-113 | Proves the library plugin supplies coroutines-core to commonMain and kotlin-test to commonTest, and calls explicitApi() — and does NOT supply compose resources, which is why decode stays in the feature module. |
| data-preference-impl/build.gradle.kts:14-17 | Proves kotlinx-coroutines-test is genuinely wired in a commonTest block today; the new feature module and data-repository-impl copy that line. |
| compose-application/src/commonMain/kotlin/dev/rectify/io/source/di/InitKoin.kt:31-41 | The exact current module list and the insertion points for the two new entries; also proves the file is commonMain, which is why compose-application needs the two new project dependencies. |
| compose-application/src/desktopMain/kotlin/dev/rectify/io/source/window/RectifyWindow.kt:175-242 | RectifyShellContent's real signature and every stub slot this spec replaces; also the evidence that the file already imports Koin/AWT/coroutines, so the 'zero imports' check must be body-level. |
| .pipeline/sdd/docs/app-shell-window-layout/review/findings.json | F-1's shipped defects. CON-1 (desktop-only deps in commonMain), PRF-1 (full-area dot-grid Canvas) and COR-3 (ViewModel/mapper contract drift) all verified present and all actively guarded against here. |
| .pipeline/sdd/docs/app-shell-window-layout/validation/changelog.json (entry at a71d232, task T-3) | The authoritative record of the backtick-name regression and the three files it broke — the correct reference for that hazard (the draft cited verdict.json, which does not mention it). |
| .pipeline/sdd/docs/app-shell-window-layout/plan/plan.json | The Stage 5 four-desktopTest command this spec extends, and the normalise-and-compare-landmarks method for measuring against comps. |

### errorStates

| state | behavior |
| --- | --- |
| loading | DiscoveryModel.Detecting on launch: StatusIndicator(Offline, 'Detecting tools…'), DeviceChip(None), stage shows the picker panel with the PLATFORM cards in a probing state and an empty SOURCE list. No spinner over the stage — detection is sub-second and a flash of chrome is worse than nothing. |
| empty | DiscoveryModel.NoDeviceFound, or Found with no selectable device: NoSignalStage on the dotted stage (dashed outline, title, two numbered steps, EmulatorSelect + Boot, 'adb devices · polling every 2 s'), StatusIndicator(Standby, 'Waiting for device…'), DeviceChip(None). Polling continues every 2s; a device appearing switches the stage to the picker without user action (AC-4, SC-2). |
| empty | Zero AVDs installed: EmulatorSelect renders disabled with an explanatory label rather than opening an empty menu, and the Boot button is disabled alongside it. |
| error | DiscoveryModel.ToolMissing: StatusIndicator(Offline, 'adb not found'), the Android PlatformToolCard shows an offline dot and 'not found', and the typed state (with probedPaths, installCommand and envHint) is published for F-3. F-2 renders no Locate/Retry/Copy UI (AC-41, SC-6). |
| error | Sustained empty polls: the stage keeps the passive NO SIGNAL state while NoDeviceFound(toolVersion, lastPollEpochMs, attempts, attachedCount) is published for F-3's escalated card. F-2 never escalates its own stage (AC-42, SC-7). |
| error | Unauthorized or offline device attached: the row appears in the picker as non-selectable (SourceListRow enabled = false) with its state as the trailing label, arrow keys skip it, and the user is never told 'no device' while holding one. |
| error | Failure.Technical.Process during a session (device vanished, non-zero exit, timeout, hung child): the session ends cleanly, the child is destroyForcibly()'d, DeviceChip flips to Lost, the stage returns to NO SIGNAL, and 2s discovery resumes. Reconnect UX is F-3's. |
| error | Failure.Technical.Process during a one-shot (boot, detect, device info): a MirrorSideEffect.ShowError is posted and the previous state is kept; detection re-derives on the next poll. An already-running AVD's lock-file exit is treated as a no-op success, not an error. |
| error | Platform switch moved to iOS: any Android session stops immediately, the stage shows a deferred-platform notice built from existing text styles, and the iOS PlatformToolCard still shows the detected idb probe (FR-2). |

### observability

#### logs

- Kermit Logger.withTag("ToolResolver") at info once per launch: which tool resolved, at which path, after how many candidates — the single most useful line when a packaged .app misbehaves.
- Logger.withTag("DeviceDiscovery") at debug on state TRANSITION only (Detecting→Found, Found→NoDeviceFound, attach/detach), never once per 2s poll — a poll that changes nothing logs nothing.
- Logger.withTag("MirrorSession") at info on session start and stop with the device id and frame dimensions; at warn on Failure.Technical.Process with argv, exit code and the capped stderr tail.
- Logger.withTag("ScreencapTransport") at debug once per 1s stats tick with achieved fps and cumulative dropped frames — never per frame.
- No log line ever contains frame bytes, and the stderr tail is capped at MirrorTuning.StderrTailBytes (2KB) so a chatty tool cannot balloon a log.
- Kermit is already in the catalog and already used by data-repository-impl and presentation-core-ui; data-device-impl adds implementation(libs.kermit) to use it — a project wiring line, not a catalog change.

#### metrics

- TransportStats.fps — achieved frames per second over the trailing 1s window (the number AC-45 requires to be real).
- TransportStats.droppedFrames — cumulative captures skipped by the single-flight guard; the honest signal that the device is slower than the floor.
- TransportStats.latencyMs — null for the polling transport; rendered as an explicit placeholder, never a fabricated number.
- DiscoveryModel.NoDeviceFound.attempts and lastPollEpochMs — poll metadata, also F-3's comp-05 footer data.
- ToolProbe.probedPaths.size — how many candidates were tried before resolution; feeds F-3's comp-04 footer.
- Measured once in step 12 and recorded in the delivery notes, not emitted at runtime: per-frame capture round-trip (ms), decode time (ms), end-to-end observed latency, and frame time with the dot grid visible vs covered (the PRF-1 question).

#### analyticsEvents


### accessibility

| id | requirement |
| --- | --- |
| A11Y-1 | NFR-8 / AC-2: the device picker is fully operable from the keyboard — ↑/↓ move the highlight, Enter activates it, and nothing in the picker requires a pointer. |
| A11Y-2 | Arrow keys skip non-selectable rows (unauthorized, offline, no-permissions) rather than landing on a row Enter cannot act on; the highlight clamps at the first and last selectable rows rather than wrapping silently. |
| A11Y-3 | Enter on a shut-down emulator row Boots it rather than starting a mirror — there is nothing to mirror yet — and the keyboard hint reflects the highlighted row's actual action. |
| A11Y-4 | Escape closes the picker only when a device is already selected; on first run there is nothing behind the panel, so Escape is an explicit no-op rather than an empty stage. |
| A11Y-5 | The panel requests focus when it appears (FocusRequester in a LaunchedEffect) and returns focus to the stage when it closes, so keyboard control is never stranded. |
| A11Y-6 | Device rows carry Role.RadioButton semantics (already true of SourceListRow's clickable, line 70) and a non-selectable row is announced as disabled, not merely dimmed — which is why `enabled` drops the clickable rather than only changing the alpha. |
| A11Y-7 | Status is never conveyed by colour alone: every status dot is paired with its text (adb 35.0.1, not found, Running, Waiting for device…), as the comps already show. |
| A11Y-8 | MirrorFrameView carries a contentDescription naming the mirrored device, so the stage is not an unlabelled image. |

### performanceBudget

| metric | target |
| --- | --- |
| Discovery poll interval (FR-5) | 2000 ms exactly, from MirrorTuning.DiscoveryPollMs; the interval is between poll completions, so a slow adb cannot stack polls. |
| Host command timeout (adb --version, adb devices -l, device info) | 3000 ms via withTimeoutOrNull; a timeout yields Failure.Technical.Process, never a swallowed cancellation. Covers adb's 1–3s cold-start daemon case, which must not read as a failure. |
| Capture floor (fps ceiling) | 100 ms minimum between capture issues (MirrorTuning.CaptureFloorMs) — a 10 fps ceiling. The loop is adaptive: it re-issues as soon as the previous frame is decoded, so the floor only bites on a fast device. |
| Per-capture timeout | 5000 ms (MirrorTuning.CaptureTimeoutMs); the child is destroyForcibly()'d in finally and the frame is counted as dropped. |
| Captures in flight | Exactly 1. A capture still running when the next issue is due increments TransportStats.droppedFrames instead of spawning a second adb child. |
| Expected achieved capture rate (v1 polling transport) | 3–7 fps and ~250–500 ms per frame on a 1080p device over USB — AN ESTIMATE CARRIED FROM BRAINSTORM, NOT A MEASUREMENT. Step 12 replaces it with a measured number plus the device model, resolution and link type; the MirrorTuning floor is then justified against that measurement. |
| PNG decode | < 40 ms per 1080p frame on Dispatchers.Default; measured in step 12 alongside the capture rate. Decode never runs on the Compose/AWT thread, so a slow decode drops the frame rate but never freezes the window. |
| Glass-to-glass latency (NFR-1) | <150 ms is the requirement and v1 KNOWINGLY DOES NOT MEET IT. The polling transport cannot even measure it end-to-end, so TransportStats.latencyMs is null and the status bar shows an explicit placeholder (AC-45). The waiver is recorded and FLAGGED FOR RE-REVIEW BEFORE SHIP, per the Stage 1 gate answer (option B). |
| Status-bar update cadence (AC-25, AC-45) | Exactly 1000 ms (MirrorTuning.StatsTickMs) — 'at least once per second' satisfied by a dedicated ticker, decoupled from the frame rate so a 3 fps stream still updates its stats once a second. |
| Status-bar content | Tool version and resolution unconditionally; resolution read from the CURRENT frame so rotation is followed rather than cached at connect time; fps = achieved rate; a one-word 'polling' hint next to fps; latency as '—'. Dropped frames are carried in the model but not shown in v1's status bar. |
| Orbit reducer passes per second while streaming | ≤ 1 for stats plus whatever semantic events occur — never one per frame. Frames reach the UI through a StateFlow<ImageBitmap?> outside the reduced state. |
| Process hygiene | Zero orphaned adb children after an abrupt window close (live-child registry + one shutdown hook + destroyForcibly in finally). The adb server daemon is never killed. |
| Test suite runtime | The seven :desktopTest tasks complete in well under a minute on a warm daemon — all pure JVM, no Compose UI harness, no device, no real process spawned. |

### i18n

#### locales

- en

#### strings

- mirror_picker_title — Choose what to mirror
- mirror_picker_subtitle — Switch platform or device any time from the toolbar.
- mirror_section_platform — PLATFORM
- mirror_section_source — SOURCE
- mirror_source_summary — %1$d found · adb devices -l
- mirror_group_physical — Physical device
- mirror_group_emulator — Emulator
- mirror_row_boot — Boot
- mirror_row_running — Running
- mirror_row_shut_down — shut down
- mirror_hint_keys — ↑↓ select · ↵ start
- mirror_action_start — Start mirroring
- mirror_no_signal_label — NO SIGNAL
- mirror_waiting_title — Waiting for an Android device
- mirror_waiting_subtitle — CHANGED AT REFINE (survey Q1): 'Plug one in or boot an emulator, then press Start mirroring.' The comp-02 string 'Mirroring starts on its own as soon as adb sees one.' is NOT shipped under the recommended option, because it promises an auto-start that FR-4/AC-3/comp-01 do not implement. If survey Q1 selects auto-start instead, revert to the comp-02 wording and change the behaviour with it — the two must never disagree.
- mirror_waiting_step_usb — Plug in a phone with USB debugging on and accept the prompt.
- mirror_waiting_step_emulator — Or boot an emulator
- mirror_waiting_footer — adb devices · polling every 2 s
- mirror_status_detecting — Detecting tools…
- mirror_status_waiting — Waiting for device…
- mirror_status_ready — ready
- mirror_status_tool_missing — adb not found
- mirror_status_latency_placeholder — latency —
- mirror_status_polling_hint — polling
- mirror_tool_not_found — not found
- mirror_ios_deferred — iOS mirroring arrives in a later release. idb is detected so you can see it is installed.
- mirror_emulator_none — No AVDs installed

#### notes

27 keys, all in presentation-core-localisation/src/commonMain/composeResources/values/strings.xml with the mirror_ prefix, per localisation.md's <feature>_<element> convention; accessors appear as Res.string.mirror_* on the next build with no registration step. Only values/ (English) exists today and this spec adds no second locale; the comps give English only. Machine output is deliberately NOT localised and carries no key: tool names and versions, serials, the resolution, the fps number, the device's Android version and transport (comp 01's 'Android 15 · USB' trailing label is composed from getprop and adb output, with only the separator as a literal). The mono type style is what marks those fragments as machine output. Two keys contain glyphs that must survive copy-paste intact: mirror_hint_keys uses ↑↓ and ↵, and several strings use the · separator — both come straight from the comps.

### security

#### dataHandling

- NFR-5 is enforced by API shape at five independent points, any one of which would have to be deliberately dismantled to break it: (1) DeviceId is a value class with a PRIVATE constructor whose only factory, DeviceId.of(raw), rejects blank, whitespace-bearing, leading-dash and out-of-charset input — an arbitrary string cannot become a device id; (2) DeviceCommand is a CLOSED sealed set of five cases with no vararg, no free text and no scrcpy member, so there is no 'run this string' API anywhere in the dependency graph and argument injection is not a category that exists; (3) DeviceCommandExecutor has exactly one method and one overload, run(device: DeviceId, command: DeviceCommand) — the device parameter is non-optional and cannot be defaulted; (4) buildDeviceArgv is the single argv construction point in the repository and always emits [adbPath, "-s", device.value, …], asserted by a test over every DeviceCommand variant (AC-44); (5) ProcessRunner is internal, lives in data-device-impl/src/desktopMain, and is unreachable from any other module by the combination of module boundary, internal, and explicitApi().
- Remote targets are unrepresentable rather than merely forbidden: adb connect, -H and -P have no DeviceCommand case and no AdbHostSource method, so no code path can address a device over the network. Adding one would be a visible, reviewable change to a public sealed hierarchy in data-device-api.
- The deviceless surface (adb --version, adb devices -l, emulator -list-avds, emulator -avd) is enumerated on two separate interfaces precisely so that 'this command has no device' is an auditable exception with five named members, rather than a nullable parameter on the device executor that every future call site could pass null to.
- Frame bytes live only in memory, are never written to disk or a temp file in v1, and are released as soon as the next frame replaces them. Nothing in this spec persists a screenshot.
- Process stderr is captured for diagnostics but capped at 2KB (MirrorTuning.StderrTailBytes) before it enters DeviceCommandException, so a chatty or hostile tool cannot flood a log or a Failure message.
- Stage 5 inspection, not just a test: grep the repository for ProcessBuilder and for `-s` argv construction, and assert exactly one file matches each.

#### permissions

- No new OS permissions. The app reads environment variables (ANDROID_HOME, ANDROID_SDK_ROOT, HOME, PATH), checks file executability on the probed candidate paths, and spawns the user's own adb/emulator binaries — all ordinary user-level operations on macOS.
- USB debugging authorisation stays entirely with the device and adb; Rectify never bypasses or suppresses the on-device prompt, and an unauthorized device is surfaced as a non-selectable row with its real state.

#### privacy

- A mirrored screen can contain anything the user has on their phone. v1 keeps frames in memory only, ships no telemetry, and emits no analytics event (there is no analytics layer in this repo at all).
- This is the strongest argument against auto-start (survey Q1): streaming a phone screen into the window with no user act of consent is a privacy decision, not just a UX one, and F-2 ships no stop/reconnect UX to undo it.
- Log lines never contain frame data, and device serials appear only in local debug logs — never transmitted anywhere, because nothing in this spec transmits.
- ToolProbe.probedPaths contains local filesystem paths and is exposed to F-3 for the comp-04 diagnostics footer; that is on-screen local diagnostics for the user's own machine, not data leaving it.

### rollout

#### featureFlags


#### migrations


#### backwardCompatibility

No persisted data changes: no new preference key, no database change, no stored format. RectifyShellContent's three new parameters are all defaulted (MirrorUiState(), null, {}), so the F-1 render harness and every existing call site keep compiling unchanged. Adding Failure.Technical.Process to a sealed hierarchy is source-compatible for every current consumer — verified at refine, not assumed: grepping `is Failure.Technical` and `when (failure)` across the repo returns zero files, so nothing branches exhaustively today. Re-run that grep in step 1 before the edit. RadioMark's extraction from SourceListRow is a pure refactor with no visual or API change to SourceListRow's existing parameters, and SourceListRow's new `enabled` parameter is defaulted true, preserving its three preview call sites. The android and iOS actuals of provideDeviceSources() register unsupported no-op sources so the Koin graph still resolves and the iOS framework still links (NFR-10) — :compose-application:linkDebugFrameworkIosArm64 is part of step 15's sweep. No feature flag is introduced because the repo has no flag mechanism and one surface cannot be half-shipped: the shell either has a live stage or it has F-1's stub.

### knownRisksFromQA

| bugRef | description | watchFor | mitigation |
| --- | --- | --- | --- |
| NOTE ON PROVENANCE — .pipeline/sdd/docs/registry/ does not exist in this repository, so there is no bugs.json and no cross-spec bug registry to check against. Rather than leave this section empty, the entries below are drawn from F-1's own review and validation artifacts, each verified present at refine at the id and line given. No history is invented; if a central registry is created later, these five should seed it. | Provenance note, not a defect. | A reviewer assuming this list came from a registry that does not exist. | Each bugRef below points at a real file and a real id; all four F-1 references were opened and confirmed during refinement. |
| .pipeline/sdd/docs/app-shell-window-layout/review/findings.json#CON-1 (compose-application/build.gradle.kts:9, severity minor) | F-1 added compose.material3 and kermit to compose-application's commonMain although only desktopMain consumes them, pulling them into the Android and iOS compile/link graphs for nothing. | The identical mistake in the three new modules: anything the desktop actual alone needs (ProcessBuilder, java.util.concurrent, skiko) must be declared and imported on desktopMain, never commonMain. data-device-impl is the highest-risk module. Note the distinction from this spec's own two compose-application additions: :data-device-impl and :presentation-feature-mirror are full KMP modules that compile for every target, so commonMain is correct for them — CON-1 was about desktop-only LIBRARIES, not KMP project deps. | Each new build.gradle.kts mirrors data-preference-impl's shape: commonMain holds only shared contracts and Koin; platform-only dependencies go in the matching source-set block. Step 15 greps each new module's commonMain for JVM-only imports. |
| .pipeline/sdd/docs/app-shell-window-layout/review/findings.json#PRF-1 (StageSurface.kt:80, severity minor, unverified) | StageSurface's dot-grid Canvas draws the full stage area on every pass. | At up to 10 fps the grid is now repainted ten times a second behind an opaque image. If frame time in step 12 looks worse than decode plus capture explains, the grid is the suspect — and it will be misdiagnosed as transport latency. | Measure frame time in step 12 explicitly, with the grid visible vs covered. MirrorFrameView draws opaque over the area it covers. If the grid is material, raise a scoped follow-up against presentation-core-ui rather than editing StageSurface inside this slice. |
| .pipeline/sdd/docs/app-shell-window-layout/validation/changelog.json — entry at a71d232, task T-3, changeRequest regression (the draft cited validation/verdict.json, which contains no such record; corrected at refine) | F-1 shipped three commonTest function names containing a comma or apostrophe in backticks. They compiled on the JVM, so every per-task gate passed honestly, while compileTestKotlinIosArm64/IosSimulatorArm64 failed and broke ./gradlew build repo-wide. Caught only by Stage 5's full-build regression check. | This spec adds more test files than any before it (ten unit plus three integration), and descriptive names are exactly where punctuation creeps in. | Plain-word backtick names with no punctuation at all, stated in each new module's CLAUDE.md and modelled on the repo's existing clean names. Steps 3, 13 and 15 run the iOS test-compiles explicitly rather than relying on a desktop-only gate. |
| .pipeline/sdd/docs/app-shell-window-layout/review/findings.json#COR-3 (SettingsViewModel.kt:66, severity minor) | F-1 shipped a ViewModel whose state did not match a mapper's documented fallback (ThemePreferenceMapper Light→System) — a contract drift between layers that no test caught. | The same class of drift between DiscoveryModel's documented semantics and DeviceRepositoryImpl's escalation logic — especially 'an empty Found escalates to NoDeviceFound after EmptyPollEscalation polls' and 'ToolMissing is a success value, not an error'. | DeviceDiscoveryPollingTest asserts the escalation boundary directly with virtual time, and asserts ToolMissing arrives as a success value through the use case rather than as a Result.failure. |
| preparation/preparation.json#openRisks[10] (carried forward unresolved, verified present) | Pre-existing repo hazard, QA-visible but unfixed: resultLauncher catches Throwable, which includes CancellationException, so any withTimeout inside a use case would convert genuine parent-scope cancellation into a Failure. | Any withTimeout (not withTimeoutOrNull) introduced in this spec's use cases, repository or transport, and any new catch (Throwable) around cancellable I/O. This is the first spec in the repo with long-running cancellable work, so it is the first one where the latent bug can actually fire. | withTimeoutOrNull everywhere, with the reason stated in a comment at each site so nobody 'simplifies' it back. Fixing resultLauncher itself touches every existing use case and is raised as an explicit follow-up, not folded into this slice. |

### implementationPlan

| step | title | dependsOn | linkedAC | estimate | changes | verification |
| --- | --- | --- | --- | --- | --- | --- |
| 1 | domain-core: device model vocabulary + Failure.Technical.Process | [] | ["AC-41", "AC-42", "AC-45"] | S | Add source/model/device/{DeviceModel,EmulatorModel,ToolModel,DiscoveryModel,MirrorFrameModel,MirrorStatsModel,DeviceStateModel,PlatformModel}.kt and one data class to source/monad/Failure.kt matching its four siblings exactly. Re-run the grep for `is Failure.Technical` and `when (failure)` first (expected: zero hits, as verified at refine). | ./gradlew :domain-core:build then ./gradlew :domain-usecase-impl:build — both compile, no exhaustiveness break. |
| 2 | data-device-api module: contracts, resources, and EVERY pure parser | [1] | ["AC-2", "AC-41", "AC-44"] | L | New module applying `dev.rectify.io.convention.library` ALONE — no `di` plugin, no Koin dependency (mirroring data-preference-api). commonMain: implementation(projects.dataCore). NO commonTest dependencies block: kotlin.test arrives from the plugin and nothing here needs coroutines-test. Add include(":data-device-api") to settings.gradle.kts under the data group. Files: source/model/{DeviceId,DeviceCommand,DeviceResource,DeviceStateResource,AvdResource,AvdName,ToolProbe,ToolKind,DeviceInfoResource,MirrorFrame,TransportStats}.kt, source/datasource/{DeviceCommandExecutor,AdbHostSource,EmulatorSource,MirrorTransport}.kt, source/parse/{AdbDevicesParser,WmOutputParser,AvdListParser,PngTrim,DeviceArgv}.kt, source/tool/ToolProbeCandidates.kt, source/tuning/MirrorTuning.kt, source/error/DeviceCommandException.kt. No core/ directory — nothing here is internal. | ./gradlew :data-device-api:build; the module has zero platform-specific source sets. |
| 3 | data-device-api commonTest: the no-phone test suite | [2] | ["AC-2", "AC-41", "AC-44"] | M | AdbDevicesParserTest (physical + emulator + unauthorized + offline + no-permissions fixtures, header line, blank trailing lines), WmOutputParserTest (physical only vs physical+override — assert the override wins, with the reason in a punctuation-free name plus a code comment so nobody 'fixes' it), AvdListParserTest, PngTrimTest (clean PNG, foldable warning prefix, garbage with no signature must not throw), DeviceArgvTest (every DeviceCommand variant yields argv[1]=="-s" && argv[2]==id and buildHostArgv never emits -s — this IS AC-44), DeviceIdTest (of() rejects blank, whitespace, leading dash, out-of-charset; accepts a real serial and emulator-5554), ToolProbeCandidatesTest (empty env, ANDROID_HOME set, ANDROID_SDK_ROOT set, Homebrew-only, PATH-only — asserts exact order). All backtick names plain words only. | ./gradlew :data-device-api:desktopTest green; ./gradlew :data-device-api:compileTestKotlinIosArm64 green (backtick-name trap). |
| 4 | data-device-impl module: the repository's only ProcessBuilder | [2] | ["AC-1", "AC-3", "AC-44"] | L | New module applying `library` ALONE (no `di`). commonMain: api(projects.dataDeviceApi), implementation(projects.dataCore), implementation(libs.koin.core), implementation(libs.kermit). Add include(":data-device-impl"). commonMain files: source/DeviceSourcesProvider.kt (internal expect fun Module.provideDeviceSources()), di/DataDeviceImplModule.kt (public val dataDeviceImplModule), core/unsupported/UnsupportedDeviceSources.kt. desktopMain: core/process/ProcessRunner.kt (internal — runInterruptible(Dispatchers.IO), two concurrent drains, withTimeoutOrNull, destroyForcibly in finally, live-child registry + one shutdown hook), core/tool/ToolResolver.kt (internal — executable predicate over probeCandidates()), source/datasource/{AdbHostSourceImpl,EmulatorSourceImpl,DeviceCommandExecutorImpl}.kt, source/transport/ScreencapMirrorTransport.kt, source/DeviceSourcesProvider.desktop.kt. androidMain + iosMain: DeviceSourcesProvider.{android,ios}.kt registering UnsupportedDeviceSources. Nothing JVM-only in commonMain (F-1's CON-1). | ./gradlew :data-device-impl:build; grep confirms ProcessBuilder appears in exactly one file repo-wide, internal and under desktopMain. |
| 5 | data-repository-impl: DeviceRepositoryImpl, mappers, the 2s polling flow, and its first commonTest block | [1, 2] | ["AC-2", "AC-4", "AC-41", "AC-42"] | M | build.gradle.kts: add implementation(projects.dataDeviceApi) to commonMain AND add a commonTest.dependencies block with implementation(libs.kotlinx.coroutines.test) — the module has commonTest sources today but no commonTest dependency block at all, so step 6 cannot compile without this. core/mapper/{DeviceResourceMapper,EmulatorResourceMapper,ToolProbeMapper,MirrorFrameMapper}.kt as internal ModelResourceMapper objects that never throw. source/DeviceRepositoryImpl.kt: the flow { emit(poll()); delay(DiscoveryPollMs) } discovery loop, AVD de-duplication (an AVD both listed and running appears once, as Running), the empty-poll counter escalating into NoDeviceFound with lastPollEpochMs/attempts/attachedCount, and ToolMissing produced as a SUCCESS value. Register in DataRepositoryImplModule. | ./gradlew :data-repository-impl:build. |
| 6 | data-repository-impl commonTest: mappers + virtual-time polling | [5] | ["AC-41", "AC-42"] | M | DeviceResourceMapperTest and EmulatorResourceMapperTest alongside the existing SidePanelWidthPreferenceMapperTest/ThemePreferenceMapperTest, in the same shape and the same punctuation-free naming style. DeviceDiscoveryPollingTest with runTest virtual time over a fake AdbHostSource: emissions land at 2s intervals; EmptyPollEscalation empty polls escalate to NoDeviceFound with the right attempts; an attach between polls produces Found without resetting anything; a missing adb produces ToolMissing as a success value, not a failure. | ./gradlew :data-repository-impl:desktopTest green. |
| 7 | domain layer: DeviceRepository + five use cases | [1, 5] | ["AC-1", "AC-3", "AC-4", "AC-25"] | M | domain-repository-api/source/repository/DeviceRepository.kt. domain-usecase-api/source/usecase/device/{DetectToolsUseCase,ObserveDeviceDiscoveryUseCase,BootEmulatorUseCase,StartMirroringUseCase,ObserveMirrorStatsUseCase}.kt — one operator invoke each, Result<T>/Optional for suspend, bare Flow<T> for reactive. domain-usecase-impl: five impls, every suspend body through resultLauncher(errorMapper = Failure.Technical::Process), withTimeoutOrNull only, with the reason in a comment at each site. Register all five in DomainUseCaseImplModule. | ./gradlew :domain-repository-api:build :domain-usecase-api:build then ./gradlew :domain-usecase-impl:build. |
| 8 | Koin wiring: project dependency + registration in the correct position | [4, 5, 7] | ["AC-1"] | S | TWO edits, not one. (a) compose-application/build.gradle.kts: add implementation(projects.dataDeviceImpl) to the commonMain block — InitKoin.kt is commonMain and imports the Koin module by package, so without this the next edit does not compile. (b) compose-application/src/commonMain/.../di/InitKoin.kt: add dataDeviceImplModule to the data group, AFTER dataDatabaseImplModule and BEFORE dataRepositoryImplModule, with a comment noting that Koin resolves lazily so this is defensive ordering that follows the documented data→domain→presentation dependency direction. The existing list and its order are left otherwise untouched. | ./gradlew :compose-application:build, then ./gradlew :desktop-application:run launches and the Koin graph resolves with no 'No definition found'. |
| 9 | presentation-core-ui: one extraction, one amendment, five new components | [] | ["AC-2", "AC-4", "AC-43"] | L | Extract RadioMark to kit/atom/indicator/RadioMark.kt and point SourceListRow at it (no visual change). Add `enabled: Boolean = true` to SourceListRow — it dims the row AND drops the .clickable, so a disabled row is announced as disabled rather than merely dimmed (A11Y-6). New: kit/molecule/card/PlatformToolCard.kt, kit/molecule/select/EmulatorSelect.kt, kit/molecule/mirror/MirrorFrameView.kt, kit/organism/picker/DevicePickerPanel.kt (opaque Theme.color.bg fill with a centred content column, FocusRequester + onPreviewKeyEvent on the root, stateless — it reports key intents and holds no selection), kit/organism/stage/NoSignalStage.kt (port the dashed-outline geometry from StageSurface's private preview; its subtitle string is whichever survey Q1 selects). Every file ends with a private @Preview in AppTheme. Tokens only. Update presentation-core-ui/CLAUDE.md tables with these six and the F-1 additions they still miss. | ./gradlew :presentation-core-ui:build; then compare the picker and NO SIGNAL previews against comps 01/02/03 with F-1's normalise-and-compare-landmarks method and record deviations. |
| 10 | presentation-core-localisation: 27 mirror_* keys | [] | ["AC-4", "AC-43"] | S | Add the 27 mirror_* keys from the i18n section to src/commonMain/composeResources/values/strings.xml, using the survey Q1 answer for mirror_waiting_subtitle. Machine output (commands, serials, resolutions, fps, Android version, transport) stays unlocalised by design. Check the glyphs in mirror_hint_keys survive the paste intact. | ./gradlew :presentation-core-localisation:build; Res.string.mirror_* accessors generate. |
| 11 | presentation-feature-mirror: the Orbit state machine | [7, 9, 10] | ["AC-2", "AC-3", "AC-4", "AC-41", "AC-42", "AC-45"] | L | New module applying `feature` + `di`; add include(":presentation-feature-mirror") under the presentation-feature group. Dependencies mirror presentation-feature-settings minus projects.presentationCoreNavigationApi (and minus compose.material3 unless MirrorContent uses a Material3 composable directly); commonTest adds libs.kotlinx.coroutines.test. Files: source/mirror/{MirrorContract,MirrorViewModel,MirrorHost,MirrorContent}.kt — MirrorHost replaces the usual *Screen because no Destination exists, documented in the module CLAUDE.md. core/picker/PickerNavigation.kt and core/stats/StatsFormatter.kt as internal pure helpers. di/PresentationFeatureMirrorModule.kt. The ViewModel owns the polling gate (discovery PAUSES while a session streams — OQ-6), flatMapLatest session switching, decode on Dispatchers.Default via decodeToImageBitmap() (add the @OptIn only if the function still carries @ExperimentalResourceApi at CMP 1.12.1 — an annotation, not a dependency), the public StateFlow<ImageBitmap?> outside Orbit state, and the 1s stats ticker. No ByteArray-bearing type enters MirrorUiState. | ./gradlew :presentation-feature-mirror:build. |
| 12 | MEASURE the real capture rate and latency — the gate decision 9 demands | [4, 11] | ["AC-25", "AC-45"] | S | With a real device attached, run the desktop app and record over a 60-second session: achieved fps (from TransportStats, not a stopwatch), mean and p95 per-frame capture round-trip, mean decode time, dropped-frame count, and observed end-to-end latency by the wave test (watch a running stopwatch on the device against the mirror). Record the device model, screen resolution and link type (USB 2/3, wifi). Also time one frame end-to-end with the dot grid visible vs covered, to settle the PRF-1 suspicion. Then set MirrorTuning.CaptureFloorMs from the measurement rather than keeping 100 ms by default, and write the numbers plus the re-flagged NFR-1 waiver into the delivery notes. | The delivery notes contain real numbers with their conditions. An estimate in this slot fails the step — decision 9 is explicit that the number must be measured. |
| 13 | presentation-feature-mirror commonTest: state machine + pure helpers | [11] | ["AC-2", "AC-3", "AC-41", "AC-42"] | M | PickerNavigationTest (skips non-selectable rows, clamps at both ends, empty list is a no-op), StatsFormatterTest (latency placeholder when null, real value when present, resolution follows the current frame), MirrorViewModelDiscoveryTest (ToolMissing → tool-missing state + offline indicator; sustained empty polls → NoDeviceFound with metadata while the stage stays on NO SIGNAL; devices found → picker populated and the first selectable row highlighted), MirrorViewModelSessionTest (arrow+Enter selects and starts; Enter on a cold emulator Boots instead; device lost → session ends, chip Lost, polling resumes; platform switch to iOS stops the session; rapid start/stop leaves exactly one collector; arrow/Enter intents carry no meta semantics; the DeviceChip reopens the panel and Escape no-ops before first selection). Dispatchers.setMain(StandardTestDispatcher) + advanceUntilIdle; NO orbit-test, NO turbine. Plain-word backtick names only. | ./gradlew :presentation-feature-mirror:desktopTest and :presentation-feature-mirror:compileTestKotlinIosArm64 both green. |
| 14 | compose-application: make every shell slot live | [8, 9, 11] | ["AC-1", "AC-3", "AC-4", "AC-25", "AC-43", "AC-45"] | M | build.gradle.kts: add implementation(projects.presentationFeatureMirror) to commonMain. InitKoin.kt: append presentationFeatureMirrorModule to the END of the presentation group (after presentationFeatureSplashModule), per koin-di.md. RectifyWindow.kt: resolve MirrorViewModel through MirrorHost, collect state and frame, pass them plus onIntent into RectifyShellContent. RectifyShellContent gains exactly three DEFAULTED parameters and fills: platformSwitch (real index + callback, replacing selectedIndex = 0), deviceIndicator (real name/state, onClick reopens the picker — AC-43, replacing the 'No device' stub), statusBar leading (StatusIndicator + the inline mono stat Row formatted by StatsFormatter — OQ-5: inline Row for v1, no new molecule), stage (DevicePickerPanel \| NoSignalStage \| MirrorFrameView \| iOS notice, replacing the 'NO SIGNAL' Text stub). | Read RectifyShellContent and its callee set and confirm no Koin resolution, no AWT type and no coroutine launch in the body (a file-level import grep is NOT the check — RectifyWindow itself legitimately imports all three); ./gradlew :compose-application:build; ./gradlew :desktop-application:run. |
| 15 | Full verification sweep and docs | [3, 6, 13, 14] | ["AC-1", "AC-2", "AC-3", "AC-4", "AC-25", "AC-41", "AC-42", "AC-43", "AC-44", "AC-45"] | M | Run the seven desktopTest tasks and the three iOS test-compiles, plus :compose-application:linkDebugFrameworkIosArm64 to prove the unsupported actuals keep the framework linking (NFR-10). Walk the demo path manually (launch → tool dots → picker → keyboard select → Start mirroring → live stage → stats ticking at least once a second → unplug → NO SIGNAL → Boot an emulator). Confirm zero diff in gradle/libs.versions.toml and exactly one ProcessBuilder. Grep each new module's commonMain for JVM-only imports (CON-1). Add CLAUDE.md for the three new modules (each stating the plain-backtick-names rule; presentation-feature-mirror's also recording the MirrorHost-not-MirrorScreen deviation) and refresh presentation-core-ui/CLAUDE.md. Record the measured numbers and the re-flagged NFR-1 waiver in the delivery notes. | ./gradlew :presentation-core-styling:desktopTest :presentation-core-ui:desktopTest :data-preference-impl:desktopTest :data-repository-impl:desktopTest :data-device-api:desktopTest :data-device-impl:desktopTest :presentation-feature-mirror:desktopTest — all green; then ./gradlew :data-device-api:compileTestKotlinIosArm64 :data-device-impl:compileTestKotlinIosArm64 :presentation-feature-mirror:compileTestKotlinIosArm64. Run module-group by module-group if the 3GB daemon complains. |

### testStrategy

#### unit

- data-device-api/src/commonTest/kotlin/data/device/api/AdbDevicesParserTest.kt — physical + emulator + unauthorized + offline + no-permissions fixtures captured from real `adb devices -l` output; asserts the typed state column survives and that non-device entries are KEPT, not dropped (the poc's bug). Covers AC-2.
- data-device-api/src/commonTest/kotlin/data/device/api/WmOutputParserTest.kt — `Physical size:` alone, and `Physical size:` + `Override size:` together; asserts the override wins. Also wm density. Feeds AC-25/AC-45's resolution field.
- data-device-api/src/commonTest/kotlin/data/device/api/AvdListParserTest.kt — names with underscores and digits, a blank list, and the stray INFO lines the emulator binary sometimes prints.
- data-device-api/src/commonTest/kotlin/data/device/api/PngTrimTest.kt — a clean PNG, a PNG behind a foldable warning line, and a buffer with no signature at all (must not throw).
- data-device-api/src/commonTest/kotlin/data/device/api/DeviceArgvTest.kt — THE AC-44 test: for every DeviceCommand variant, buildDeviceArgv yields argv[1] == "-s" and argv[2] == the supplied DeviceId, and buildHostArgv never contains "-s".
- data-device-api/src/commonTest/kotlin/data/device/api/DeviceIdTest.kt — of() rejects blank, whitespace-bearing, leading-dash and out-of-charset input and accepts a real serial and emulator-5554; the private constructor means these are the only entry points. Supports AC-44.
- data-device-api/src/commonTest/kotlin/data/device/api/ToolProbeCandidatesTest.kt — exact ordering for an empty env, ANDROID_HOME set, ANDROID_SDK_ROOT set, Homebrew-only, and PATH-only; the unit test that covers the packaged-.app PATH risk without touching a filesystem.
- data-repository-impl/src/commonTest/kotlin/data/repository/impl/DeviceResourceMapperTest.kt and EmulatorResourceMapperTest.kt — resource→model in the shape of the existing SidePanelWidthPreferenceMapperTest, including the AVD-listed-and-running de-duplication.
- presentation-feature-mirror/src/commonTest/kotlin/presentation/feature/mirror/PickerNavigationTest.kt — nextSelectableIndex skips non-selectable rows, clamps at both ends, and no-ops on an empty list. Supports AC-2 and A11Y-2.
- presentation-feature-mirror/src/commonTest/kotlin/presentation/feature/mirror/StatsFormatterTest.kt — latency renders as the placeholder when null and as a number when present, the polling hint appears for the v1 transport, and resolution comes from the current frame. Supports AC-45.
- Every backtick test name is plain words with no comma, apostrophe, parenthesis or other punctuation — the F-1 a71d232 regression. The repo's existing mapper tests are the style model.

#### integration

- data-repository-impl/src/commonTest/kotlin/data/repository/impl/DeviceDiscoveryPollingTest.kt — runTest virtual time over a fake AdbHostSource: emissions land at 2s intervals (FR-5); EmptyPollEscalation consecutive empty polls escalate into NoDeviceFound carrying attempts and lastPollEpochMs (AC-42); a device attaching between polls yields Found on the next tick; a missing adb yields ToolMissing as a SUCCESS value (AC-41), not a Result.failure. Requires the new commonTest coroutines-test dependency added in step 5.
- presentation-feature-mirror/src/commonTest/kotlin/presentation/feature/mirror/MirrorViewModelDiscoveryTest.kt — against fake use cases: ToolMissing → tool-missing state and an offline indicator (AC-41); sustained empty polls → NoDeviceFound surfaced with metadata while the stage stays on NO SIGNAL (AC-42, SC-7); Found → picker populated with Physical and Emulator groups and the first selectable row highlighted (AC-2).
- presentation-feature-mirror/src/commonTest/kotlin/presentation/feature/mirror/MirrorViewModelSessionTest.kt — arrow keys then Enter select a device and start a session with frames flowing (AC-3, SC-1); Enter on a shut-down emulator Boots instead of starting; a transport failure ends the session, flips the chip to Lost and resumes polling; switching to iOS stops the session; rapid start/stop leaves exactly one collector (flatMapLatest); the DeviceChip reopens the panel and Escape no-ops before first selection (AC-43); arrow/Enter intents never carry meta semantics.
- No test spawns a real process, touches a real filesystem path, or requires a device — AS-7 is structural, achieved by the api/impl split rather than by mocking frameworks (there are none in this repo).

#### ui

- No automated Compose UI tests: the repo has no UI test harness and adding one is out of scope and out of the zero-new-dependency budget. Visual verification is (a) a private @Preview per new component in AppTheme, and (b) F-1's normalise-and-compare-landmarks method against comps 01/02/03, which found two real pixel defects in F-1 and is the recorded method here too.
- Demo-only by honest necessity (no headless CI harness exists for the desktop app): AC-1 (tool dots at launch), AC-4 (NO SIGNAL with inline Boot), AC-25 and AC-45 (status bar updating at least once a second with real fps and placeholder latency), AC-43 (picker on Theme.color.bg filling the stage, reopened from the DeviceChip). Each is walked manually in step 15 against ./gradlew :desktop-application:run and recorded.
- Test-verifiable despite their UI surface, because the logic was deliberately put where tests can reach it: AC-2 (sections + keyboard selection — parser plus PickerNavigation plus VM state), AC-3 (start mirroring — VM session test), AC-41/AC-42 (typed states — repository and VM tests), AC-44 (argv shape — pure parser test plus a grep inspection).
- Inspection-verified (grep or read, not a test): exactly one ProcessBuilder in the repository; RectifyShellContent's body free of Koin/AWT/coroutines (read, not grepped); zero diff in gradle/libs.versions.toml; no JVM-only import in any new commonMain; MirrorTransport has exactly two members and no presentation or domain file imports ScreencapMirrorTransport.

#### linkedScenarios

- SC-1
- SC-2
- SC-6
- SC-7

#### coverageGoal

Every pure function this spec adds (7 parser/argv/probe functions, 2 presentation helpers) has a direct test; every typed DiscoveryModel transition has a test; every AC marked 'test' is covered by a named test file. No coverage percentage target — the repo has no coverage tooling and adding one is a new dependency. The honest ceiling is stated rather than hidden: ProcessRunner itself, ToolResolver's filesystem predicate, the real screencap loop and all five demo ACs are NOT automated, because they need a process, a filesystem or a human eye; they are covered by step 12's measurement and step 15's recorded manual walkthrough, and :data-device-impl:desktopTest is therefore a reserved-but-empty task rather than a claim of coverage. Stage 5 command (F-1's four tasks plus this spec's three): ./gradlew :presentation-core-styling:desktopTest :presentation-core-ui:desktopTest :data-preference-impl:desktopTest :data-repository-impl:desktopTest :data-device-api:desktopTest :data-device-impl:desktopTest :presentation-feature-mirror:desktopTest — plus ./gradlew :data-device-api:compileTestKotlinIosArm64 :data-device-impl:compileTestKotlinIosArm64 :presentation-feature-mirror:compileTestKotlinIosArm64 for the backtick-name trap. Run in two halves if the 3GB daemon complains.

### traceabilityMatrix

| ac | functional | components | tests | steps |
| --- | --- | --- | --- | --- |
| AC-1 | ["FR-1", "FR-2"] | ["ToolResolver", "probeCandidates", "AdbHostSource", "EmulatorSource", "ToolModel", "DetectToolsUseCase", "PlatformToolCard", "StatusIndicator", "RectifyShellContent (amended)"] | ["ToolProbeCandidatesTest", "step 15 manual demo walkthrough (demo-only: no headless harness)"] | [4, 7, 9, 14, 15] |
| AC-2 | ["FR-3"] | ["parseAdbDevices", "DeviceResourceMapper", "DiscoveryModel.Found", "DevicePickerPanel", "SourceListRow (amended)", "SourceListHeader", "PickerNavigation", "MirrorViewModel"] | ["AdbDevicesParserTest", "DeviceResourceMapperTest", "PickerNavigationTest", "MirrorViewModelDiscoveryTest", "MirrorViewModelSessionTest", "SC-1"] | [3, 5, 9, 11, 13] |
| AC-3 | ["FR-4", "FR-24", "FR-25"] | ["MirrorTransport", "ScreencapMirrorTransport", "StartMirroringUseCase", "MirrorViewModel", "MirrorFrameView"] | ["MirrorViewModelSessionTest", "SC-1", "step 12 measured session"] | [4, 7, 11, 12, 13, 14] |
| AC-4 | ["FR-5"] | ["DeviceRepositoryImpl polling flow", "ObserveDeviceDiscoveryUseCase", "NoSignalStage", "EmulatorSelect", "BootEmulatorUseCase"] | ["DeviceDiscoveryPollingTest", "SC-2", "step 15 manual demo walkthrough (demo-only)"] | [5, 6, 7, 9, 14, 15] |
| AC-25 | ["FR-22"] | ["TransportStats", "MirrorStatsModel", "ObserveMirrorStatsUseCase", "StatsFormatter", "RectifyShellContent status bar Row", "MirrorTuning.StatsTickMs"] | ["StatsFormatterTest", "step 12 measurement", "step 15 manual demo walkthrough (demo-only)"] | [7, 11, 12, 14, 15] |
| AC-41 | ["FR-1"] | ["ToolProbe", "DiscoveryModel.ToolMissing", "DeviceRepositoryImpl", "DetectToolsUseCase", "MirrorViewModel", "StatusIndicator"] | ["ToolProbeCandidatesTest", "DeviceDiscoveryPollingTest (ToolMissing arrives as a success value)", "MirrorViewModelDiscoveryTest", "SC-6"] | [1, 4, 5, 6, 11, 13] |
| AC-42 | ["FR-5"] | ["DiscoveryModel.NoDeviceFound", "DeviceRepositoryImpl empty-poll counter", "MirrorTuning.EmptyPollEscalation", "MirrorViewModel"] | ["DeviceDiscoveryPollingTest (escalation boundary, attempts, lastPollEpochMs)", "MirrorViewModelDiscoveryTest", "SC-7"] | [1, 5, 6, 11, 13] |
| AC-43 | ["FR-3"] | ["DevicePickerPanel (opaque Theme.color.bg fill of the stage content slot)", "DeviceChip onClick", "MirrorViewModel isPickerOpen"] | ["MirrorViewModelSessionTest (chip reopens the panel; Escape no-ops before first selection)", "comp 01/03 landmark comparison in step 9", "step 15 manual demo walkthrough (demo-only)"] | [9, 11, 14, 15] |
| AC-44 | ["FR-4", "FR-25"] | ["DeviceId (private constructor)", "DeviceCommand (closed sealed set)", "DeviceCommandExecutor", "buildDeviceArgv", "ProcessRunner (internal, desktopMain)"] | ["DeviceArgvTest", "DeviceIdTest", "step 15 grep inspection: exactly one ProcessBuilder in the repository, internal, under data-device-impl/desktopMain"] | [2, 3, 4, 15] |
| AC-45 | ["FR-22", "FR-24", "FR-25"] | ["TransportStats.latencyMs (nullable by design)", "StatsFormatter", "ScreencapMirrorTransport achieved-rate counter", "MirrorTuning"] | ["StatsFormatterTest (placeholder when null, never fabricated)", "step 12 measured fps vs reported fps", "step 15 manual demo walkthrough (demo-only)"] | [4, 11, 12, 14, 15] |
| FR-1 | ["FR-1"] | ["ToolResolver", "probeCandidates", "ToolProbe", "PlatformToolCard"] | ["ToolProbeCandidatesTest", "AC-1 demo", "SC-6"] | [4, 9, 14] |
| FR-2 | ["FR-2"] | ["SegmentedButtonGroup (reuse)", "MirrorUiState.platform", "iOS deferred-platform stage notice", "EmulatorSource/idb probe"] | ["MirrorViewModelSessionTest (switching to iOS stops the session)", "inspection: the iOS card shows the detected idb probe and nothing streams"] | [11, 14] |
| FR-3 | ["FR-3"] | ["DevicePickerPanel", "SourceListRow", "PickerNavigation", "EmulatorSelect"] | ["AdbDevicesParserTest", "PickerNavigationTest", "MirrorViewModelDiscoveryTest", "AC-2", "AC-43"] | [3, 9, 11, 13] |
| FR-4 | ["FR-4"] | ["StartMirroringUseCase", "ScreencapMirrorTransport", "MirrorFrameView", "AppButton 'Start mirroring' CTA"] | ["MirrorViewModelSessionTest", "AC-3", "SC-1", "survey Q1 keeps the CTA and the NO SIGNAL copy consistent"] | [4, 7, 9, 10, 11, 14] |
| FR-5 | ["FR-5"] | ["DeviceRepositoryImpl polling flow", "NoSignalStage", "MirrorTuning.DiscoveryPollMs"] | ["DeviceDiscoveryPollingTest", "AC-4", "SC-2", "SC-7"] | [5, 6, 9] |
| FR-22 | ["FR-22"] | ["MirrorStatsModel", "StatsFormatter", "status bar Row"] | ["StatsFormatterTest", "AC-25", "AC-45"] | [7, 11, 14] |
| FR-24 | ["FR-24"] | ["ScreencapMirrorTransport", "trimToPngSignature", "MirrorTuning"] | ["PngTrimTest", "step 12 measurement (the waiver's evidence)", "inspection: the delivery notes record the measured shortfall"] | [4, 12, 15] |
| FR-25 | ["FR-25"] | ["MirrorTransport (two members only)", "TransportStats"] | ["inspection: MirrorTransport has exactly two members, lives in data-device-api, and no presentation or domain file imports ScreencapMirrorTransport; swapping the implementation touches one DI line"] | [2, 4, 8] |
| NFR-1 | ["FR-24", "FR-25"] | ["ScreencapMirrorTransport", "MirrorTuning", "TransportStats.latencyMs"] | ["step 12 measurement — WAIVED FOR v1: the measured number is recorded, the shortfall is explicit, and the waiver is re-flagged for pre-ship review per the Stage 1 option-B answer (AC-45 keeps the UI honest about it)"] | [12, 15] |
| NFR-5 | ["FR-4"] | ["DeviceId", "DeviceCommand", "DeviceCommandExecutor", "buildDeviceArgv", "ProcessRunner"] | ["DeviceArgvTest", "DeviceIdTest", "AC-44", "grep inspection (one ProcessBuilder; no connect/-H/-P anywhere)"] | [2, 3, 4, 15] |
| NFR-8 | ["FR-3"] | ["DevicePickerPanel key handling", "PickerNavigation", "FocusRequester", "SourceListRow enabled"] | ["PickerNavigationTest", "MirrorViewModelSessionTest", "A11Y-1..A11Y-6", "AC-2"] | [9, 11, 13] |
| NFR-10 | ["FR-1", "FR-4"] | ["provideDeviceSources expect/actual", "UnsupportedDeviceSources"] | ["inspection: :data-device-impl:build succeeds for android/iOS targets and :compose-application:linkDebugFrameworkIosArm64 still links; no macOS/Linux native target is added"] | [4, 15] |
| SC-1 | ["FR-1", "FR-2", "FR-3", "FR-4"] | ["ToolResolver", "DevicePickerPanel", "MirrorViewModel", "ScreencapMirrorTransport"] | ["MirrorViewModelDiscoveryTest + MirrorViewModelSessionTest cover the state path; step 15 walks the full path manually"] | [4, 9, 11, 13, 15] |
| SC-2 | ["FR-5"] | ["DeviceRepositoryImpl polling", "NoSignalStage", "EmulatorSelect", "BootEmulatorUseCase"] | ["DeviceDiscoveryPollingTest", "step 15 manual walkthrough (unplug → NO SIGNAL → Boot)"] | [5, 6, 9, 15] |
| SC-6 | ["FR-1"] | ["ToolResolver", "DiscoveryModel.ToolMissing", "StatusIndicator"] | ["ToolProbeCandidatesTest", "DeviceDiscoveryPollingTest", "MirrorViewModelDiscoveryTest"] | [4, 6, 13] |
| SC-7 | ["FR-5"] | ["DeviceRepositoryImpl empty-poll counter", "DiscoveryModel.NoDeviceFound", "NoSignalStage"] | ["DeviceDiscoveryPollingTest", "MirrorViewModelDiscoveryTest"] | [5, 6, 13] |

### openQuestions

- CARRIED (OQ-1, unresolved): measured or cited glass-to-glass latency for scrcpy-server + in-JVM H.264 decode is still unknown. A spike must produce a real number before that transport is committed as the v1.1+ follow-up. The v1 polling number is NOT left open — step 12 makes measuring it a gate.
- CARRIED (OQ-2, resolved with a recommendation — flag for UX override): v1 shows a one-word mono 'polling' hint next to the fps stat, so the user is told the stream is poll-based rather than left to infer it from sluggishness. Cost is one localised word; reverse it with a single string change if UX writing disagrees.
- CLOSED BY VERIFICATION (OQ-3): SourceListRow was read at refine. It already has the radio, the bold name, the mono serial and a trailing RowScope slot, and its preview renders all three comp states. It needs exactly one additive change, `enabled: Boolean = true`. No new row molecule.
- CLOSED BY VERIFICATION (OQ-4): comp 01 was re-read at refine and shows the entire stage area as a plain, undotted surface behind the centred panel. The picker therefore fills StageSurface's content slot with an OPAQUE Theme.color.bg surface, hiding the dot grid rather than skipping it, and the shell-level composition stays untouched.
- CARRIED (OQ-5, resolved): the status-bar stat line stays an inline Row in RectifyShellContent for v1, formatted by the pure StatsFormatter so the logic is tested even though the layout is not. Promote to a molecule only when a second surface needs it.
- CARRIED (OQ-6, resolved): discovery polling PAUSES while a mirror session is active and resumes when it ends — the capture loop's own failure is the disconnect signal, and a 2s adb poll competing with a capture loop over the same USB link is the fastest way to make both worse.
- CARRIED (OQ-7, partially resolved — needs F-3 agreement): ToolProbe carries installCommand (the exact brew/sdkmanager line comp 04 shows) plus probedPaths and envHint for the footer. F-3 owns the clipboard abstraction; F-2 only guarantees the string is present and exact. OPEN: whether F-3 wants the whole footer pre-rendered as one string or the three fields separately.
- CARRIED (OQ-8, resolved): 27 mirror_* keys are specified in the i18n section and land in presentation-core-localisation/values/strings.xml in step 10. Machine output stays unlocalised by design. One key's text depends on survey Q1.
- CLOSED BY VERIFICATION (OQ-9): decodeToImageBitmap is reachable from presentation-feature-mirror's commonMain — FeatureConventionPlugin.kt:115 wires composeDependencies.components.resources into commonMain, and that artifact is where org.jetbrains.compose.resources.decodeToImageBitmap lives. No new dependency; the skiko fallback is dropped. Residual, an annotation not a dependency: if the function still carries @ExperimentalResourceApi at CMP 1.12.1, add the opt-in at the call site. Also closed: the two data modules use the LIBRARY plugin, which does NOT supply compose resources, so they cannot decode — which is why decode lives in the feature module.
- CLOSED BY VERIFICATION, WITH A CORRECTION (OQ-10): kotlinx-coroutines-test is genuinely wired, not merely cataloged — data-preference-impl/build.gradle.kts:16. But data-repository-impl, which this plan adds a virtual-time test to, has commonTest SOURCES and no commonTest DEPENDENCIES block at all, so step 5 must add one. No catalog change either way.
- CARRIED (OQ-11, unresolved and accepted): no CI harness exists to run the desktop app headlessly, so AC-1, AC-4, AC-25, AC-43 and AC-45 stay manual, human-observed demo steps. Building one is out of scope and out of the dependency budget; step 15 records the walkthrough instead.
- CARRIED (NEW at draft): Orbit ViewModel tests without orbit-test or turbine rely on Dispatchers.setMain + advanceUntilIdle against container.stateFlow. If that proves flaky, the fallback is extracting more pure helpers rather than adding a dependency — but whether the project would rather spend one test-only dependency for reliable MVI tests is a gate decision, not an implementer's call. Not surveyed here because it is conditional on a flakiness that may never appear.
- CARRIED (NEW at draft): DeviceCommandException is public in data-device-api so the diagnostics ride in Failure.Technical.Process's cause, but presentation must NOT import a data module to read it. F-2's position: F-3 branches on the Failure TYPE and reads typed diagnostics from DiscoveryModel, never by casting the cause. If F-3 needs per-command diagnostics in the UI, a domain-level error model is required — a contract change to agree before F-3 starts.
- CARRIED (NEW at draft): what exactly should the stage show when the platform switch is on iOS? This plan renders a deferred-platform notice built from existing text styles, and mirror_ios_deferred is invented copy — no comp covers that state. It needs a UX glance, but it changes no build decision, so it is an open question rather than a survey item.
- SURVEYED (NEW at draft, now proposedSurvey Q1): comp 02's subtitle promises auto-start while comp 01 and FR-4/AC-3 require an explicit CTA. This plan implements the CTA and changes the subtitle copy to match. Whichever option is chosen, the behaviour and the NO SIGNAL copy must ship together.
- SURVEYED (NEW at refine, now proposedSurvey Q2): whether :data-device-impl and :presentation-feature-mirror are wired into compose-application's commonMain (uniform Koin graph, both modules link into the iOS framework) or desktopMain (smaller framework, but initKoin must be split or parameterised).
- CARRIED (NEW at draft): MirrorTuning.CaptureFloorMs ships at 100 ms but step 12 may show the device cannot approach 10 fps anyway, making the floor inert. Whether to keep an inert floor (cheap insurance against a future fast transport) or tune it to the measured rate is a decision for whoever runs the measurement, recorded with the number.
- NEW at refine, carried: presentation-core-ui/CLAUDE.md's component tables are materially stale — they omit roughly a dozen components F-1 shipped (DeviceChip, KeyCap, CounterBadge, StatusPill, CommandBlock, ErrorCard, ErrorGlyph, GlyphBadge, SourceListRow/SourceListHeader, StatusLine, FeedbackBar, SidebarTabs, MeasureTag, AnnotationPopover, Ruler, MeasureCard, BentoTile). This plan refreshes them in step 15, which is slightly beyond F-2's strict scope but is the cheapest moment to do it, since the next person browsing the kit before authoring will be misled otherwise.

### readyForBacklog

true

### survey

| question | options | answer | source | rationale |
| --- | --- | --- | --- | --- |
| Comp 01 draws an explicit 'Start mirroring' CTA (and FR-4/AC-3 mandate it), while comp 02's subtitle says 'Mirroring starts on its own as soon as adb sees one.' Which behaviour ships in v1? | ["A explicit CTA only, NO SIGNAL subtitle rewritten", "B auto-start first discovered device", "C auto-SELECT but still require Enter/click"] | A - Explicit CTA only. FR-4 mandates the action and AC-3 verifies it, so B would contradict a signed requirement and need a new AC plus a stop control. There is also a privacy argument: a mirrored screen can show anything on the user's phone, and auto-start would put it on screen with no act of consent, while F-2 deliberately ships no stop/reconnect UX (that is F-3's). The NO SIGNAL subtitle is rewritten to match in the SAME commit - now a DoD line, so the app never promises auto-start next to a button that requires a click. | ai-proposed | Autonomous run; recommended option taken. Cost comparison: A is one localised string, B is a requirement change plus a new AC plus a stop control plus a privacy posture. |
| compose-application must gain project dependencies on :data-device-impl and :presentation-feature-mirror. Which source set? | ["A commonMain alongside the existing eight", "B desktopMain, splitting initKoin per platform"] | A - commonMain, matching every existing module in initKoin()'s list. initKoin() lives in commonMain and imports each Koin module by package, so B would force a change to the single file every platform entry point calls, purely to avoid linking two modules that already compile for all four targets. Explicitly NOT F-1's CON-1 mistake: that was desktop-only LIBRARIES (compose.material3, kermit) in commonMain; these are KMP project dependencies. | ai-proposed | Autonomous run; recommended option taken. If iOS framework size later matters, B is a contained follow-up rather than a rewrite. |
