# android-device-discovery-mirroring — preparation

## Envelope

| field | value |
| --- | --- |
| stage | preparation |
| step | preparation |
| status | done |
| producedBy | sdd-build-preparation-compile |
| schemaVersion | 1 |
| createdAt | 2026-10-14T23:35:13Z |
| updatedAt | 2026-10-14T23:35:13Z |

## Gate

### type

human

### approvedBy

orchestrator-auto

### approved

true

### at

2026-10-14T23:35:13Z

## Provenance

### selfHash

da20ef73c135a07e33a076102fbfc1f6146e4118888446d817a29f03e77f9879

### inputs

| artifact | hash |
| --- | --- |
| preparation/input.json | 6fffeb9db47732501f8e0bc72b54f037ac4fa21f16ebf336839ef2e10c44b677 |
| preparation/clarify.json | 9eeeafe69beeb688a188b3cf85e6a810b133974f7ee447f419357971b5932c96 |
| preparation/brainstorm.json | 01478430ebba329cef102621421964ff893e86db3b9e9a6000c14bf5d5974406 |

## Data

### summary

F-2 (android-device-discovery-mirroring) makes every static stub in the F-1 shell (RectifyWindowShell/ShellToolbar/ShellStatusBar/StageSurface) live for Android: launch-time adb/idb tool detection, the Android/iOS platform switch (Android functional, iOS detection-only), a full-stage device picker (Physical + Emulator, inline Boot, arrow/Enter nav), a 'Start mirroring' action, a 2s-polling passive NO SIGNAL empty state, and a status bar reporting tool version/resolution/fps/latency. NFR-1 WAIVER (prominent, not buried): v1 ships a screenshot-polling MirrorTransport that KNOWINGLY DOES NOT MEET the <150ms glass-to-glass latency target; an H.264-decode transport is an explicit, interface-isolated v1.1+ follow-up. Twelve binding decisions (5 from clarify, 7 from brainstorm) are settled below with zero new dependencies. 8 FR, 4 NFR, 10 AC, 4 scenarios, 19 components, 11 risks, 11 implementation steps carried forward intact from the three approved Stage 1 artifacts.

### problemStatement

Rectify's canonical window exists (F-1) but shows nothing real: the platform switch, device indicator, and status bar are static previews, and StageSurface has no content source. Until a real Android device can be discovered and mirrored inside the single canonical window, there is no product to annotate — every downstream spec (annotation, measurement, payload, error recovery) depends on this one being live.

### goals

- GOAL-1: Android becomes a real, selectable live mirror source inside the single canonical Rectify window within this spec's scope (FR-1..FR-5) — the product's entry point stops being a stub.
- GOAL-5: Interaction over the mirror stays responsive enough for the annotation workflow (drag-to-freeze) that depends on it; this spec is explicit that v1's chosen transport (staged, see FR-24) does not yet meet the <150ms NFR-1 target and documents the follow-up path rather than silently falling short.

### confirmedRequirements

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
| NFR-1 | performance | Live mirror glass-to-glass latency should stay low enough (target under ~150ms) that drag-to-select and freeze feel responsive. WAIVED FOR v1 — see chosenApproach decision 1. |
| NFR-5 | security | adb/idb commands are only ever invoked against the user's explicitly selected, connected device — never an arbitrary or remote target. |
| NFR-8 | a11y | Device picker, annotation popovers, and error-card actions are fully keyboard-navigable (arrow keys/Enter for pickers, Enter/Escape for popovers). |
| NFR-10 | compatibility | v1 targets macOS only; no Windows/Linux builds. |

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

### scope

#### in

- FR-1 launch-time adb/idb tool detection with per-platform toolbar status dots (AC-1, AC-41, SC-6).
- FR-2 Android/iOS platform switch; Android functional in v1, iOS switch exists and shows detected idb only — not functional.
- FR-3 device picker: full-stage takeover panel with Physical/Emulator sections, inline Boot, keyboard nav (AC-2, AC-43, NFR-8).
- FR-4 'Start mirroring' action connecting to the selected device via the v1 screenshot-polling transport (AC-3).
- FR-5 2s-polling passive NO SIGNAL empty state with guidance and inline emulator Boot (AC-4, SC-2, SC-7, AC-42).
- FR-22/FR-24/FR-25 status bar live stats (version/resolution/fps/latency) behind a swappable MirrorTransport interface (AC-25, AC-45).
- NFR-5 device-scoped command executor enforcing selected-device-only execution by API shape (AC-44).
- Scenarios SC-1 (first-run happy path), SC-2 (empty state), SC-6 (adb not on PATH — typed state only), SC-7 (zero devices sustained — typed state only).

#### out

- Functional iOS mirroring (FR-2 defers it to v1.1+; the switch exists and shows detected idb only).
- The rich error-card UI and recovery actions for comps 04/05 (Locate adb, Copy, Retry, Restart adb, Boot-emulator-instead) — owned by error-handling-and-recovery (F-3), which consumes F-2's typed discovery states (AC-41, AC-42).
- Annotation, measurement, payload, history, and MCP specs (later in the roadmap).
- Recovery/reconnect UX for a mirror that disconnects mid-session after FR-4 has already started streaming — pre-connection discovery only is this spec's scope; mid-session recovery is F-3 territory.
- A production H.264-decode transport that actually meets the <150ms NFR-1 target in v1 — staged out per FR-24; tracked as an explicit follow-up, not silently dropped.
- Windows/Linux builds (NFR-10).

### constraints

| type | text |
| --- | --- |
| tech | No JVM H.264 decoder exists on the classpath today (verified against gradle/libs.versions.toml) — adding one is a new-dependency decision requiring explicit justification against version-catalog-hygiene.md, and is deferred past v1 (see FR-24). |
| tech | All adb/idb process execution must route through a single device-scoped command executor whose API requires a resolved device id (NFR-5) — no repository or use case may shell out directly (see AC-44). |
| tech | Kotlin daemon is capped at 3GB; building this spec's three affected KMP modules across all targets simultaneously OOMs the 18GB build host — build per module during implementation. |
| business | PRD estimates this spec as 'L'; the staged transport plan (FR-24) keeps the build inside that envelope by deferring the highest-cost item (in-JVM H.264 decode) rather than absorbing it into v1. |
| tech | Zero new runtime dependencies across all twelve binding decisions — everything is JDK, kotlinx-coroutines, Compose Multiplatform or Koin, all already wired. The only dependency this spec would otherwise have needed (an H.264 decoder) is precisely what FR-24 defers. |
| tech | DeviceCommand is a closed sealed set (Screencap/WmSize/WmDensity/GetProp), not a vararg string runner — no API in the graph can run an arbitrary command against an arbitrary target, removing argument-injection as a category and making NFR-5 true by construction rather than by call-site discipline. |

### assumptions

| id | text |
| --- | --- |
| AS-6 | scrcpy 3.3.1 and adb remain available via PATH/Homebrew-standard locations during implementation and demo; detection logic reads PATH dynamically rather than hardcoding the exact verified paths from this machine. |
| AS-7 | No physical Android device or running emulator is guaranteed available in the automated build/test environment; AC-2/AC-3/AC-44's 'test' verification is achieved by mocking the process-execution/device-list boundary, not by requiring real hardware in CI. |
| AS-8 | Shipping screenshot-polling as the v1 mirror transport (not meeting NFR-1) is an acceptable, explicitly-flagged v1 trade-off rather than a silent shortfall — this clarify stage surfaces it for the plan stage and any human review rather than deciding alone that the gap is acceptable forever. |
| AS-9 | presentation-core-ui's existing SourceListRow molecule is a reasonable starting point for the device-picker's Physical/Emulator rows (radio + trailing slot variants); exact fit is confirmed at plan/design stage per design-tokens.md's 'browse before authoring' rule rather than assumed finished here. |

### dependencies

| on | note |
| --- | --- |
| F-1 (app-shell-window-layout) | Merged to development; provides RectifyWindowShell/ShellToolbar/ShellStatusBar/StageSurface slot contracts this spec fills. |
| error-handling-and-recovery (F-3) | Downstream dependent; inherits the ToolMissing/NoDeviceFound discovery-state contract (AC-41, AC-42) to render comps 04 and 05, including their recovery actions. F-2 does not build that UI. |
| rectify-poc Devices.kt | Reference implementation only (not a build dependency) for adb devices -l parsing, screencap capture, and wm/getprop device-info logic. |

### chosenApproach

A single vertical slice through the existing layering, with process execution quarantined in one desktop-only module. Twelve binding decisions, settled and not to be revisited without a new gate: (1) TRANSPORT IS STAGED — v1 ships screenshot polling (adb exec-out screencap, reusing rectify-poc logic) behind a MirrorTransport interface; scrcpy-server + in-JVM H.264 decode is a v1.1+ follow-up. v1 KNOWINGLY DOES NOT MEET NFR-1's <150ms target — a deliberate, recorded waiver, not an oversight. (2) F-2 renders only the passive NO SIGNAL state (comp 02) plus the toolbar status dot, and exposes typed ToolMissing/NoDeviceFound discovery states; F-3 owns both rich error cards (comps 04/05) and their recovery actions. (3) The device picker is a single full-stage takeover panel in StageSurface's content slot, centred on Theme.color.bg (not the dotted stage), auto-shown before first selection and reopened via the toolbar DeviceChip — no separate anchored dropdown exists despite FR-3's wording. (4) A device-scoped command executor in the data layer, whose every method requires a resolved DeviceId as a non-optional parameter, is the sole place in the codebase allowed to shell out (NFR-5 enforced by API shape, not convention). (5) Status bar stats: version and resolution shown unconditionally; fps is the transport's real achieved rate; latency is an explicit placeholder, never a fabricated number, when unmeasurable. (6) New data-device-api + data-device-impl module pair mirroring data-preference-api/impl: ALL pure parsers (adb devices -l, wm size/density, avd list, PNG-signature trim) live in the api module's commonMain — the placement that makes AC-2/AC-3/AC-44 verifiable with plain kotlin.test and no phone attached; ProcessBuilder lives only in the impl module's desktopMain, internal, behind an expect/actual Koin provider copying data-preference-impl's provideSettings() pattern. (7) New Failure.Technical.Process(cause) in domain-core, with DeviceCommandException(argv, exitCode, stderrTail) as the cause; a missing binary at DETECTION time is NOT an error — it is a DiscoveryModel.ToolMissing success value (AC-41 needs it renderable). Use withTimeoutOrNull, never withTimeout. (8) New presentation-feature-mirror module using Orbit MVI; RectifyWindow injects the ViewModel and passes plain state + frame + onIntent into RectifyShellContent, which gains no imports and stays Koin/AWT/coroutine-free. Entry point is MirrorHost, not MirrorScreen — there is no Destination here, documented as a deliberate deviation. (9) Adaptive single-flight capture loop: re-issue as soon as the previous frame decodes, 100ms floor (10fps ceiling), 5s per-capture timeout, dropped frames counted in TransportStats; floor and timeout live in one MirrorTuning constant object (the calibration knob — the right floor differs per device). Expected ~3-7fps and ~250-500ms/frame on 1080p over USB; the real number MUST BE MEASURED during implementation, not estimated. (10) The picker panel owns arrow/Enter via a FocusRequester + Modifier.onPreviewKeyEvent on its root; RectifyWindow's handleShellShortcut is untouched (unmodified vs meta-modified keys cannot collide). Escape closes the panel only when a device is already selected; Enter on a shut-down emulator row Boots rather than starting. (11) Tool detection probes an ordered candidate list ($ANDROID_HOME/platform-tools/adb, $ANDROID_SDK_ROOT/platform-tools/adb, ~/Library/Android/sdk/platform-tools/adb, /opt/homebrew/bin/adb, /usr/local/bin/adb) then falls back to PATH, recording every path tried in ToolProbe for F-3's comp-04 footer — because a Finder-launched .app inherits a minimal PATH excluding /opt/homebrew/bin, so PATH-only detection passes every dev run and fails on first double-click. (12) Transport emits MirrorFrame(ByteArray, width, height, capturedAtMs); the ViewModel decodes on Dispatchers.Default and publishes to a StateFlow<ImageBitmap?> held OUTSIDE the Orbit state; stats update on a separate 1s ticker — pixels are a stream, UI semantics are a state machine, keeping the bitmap out of reduced state avoids a full state copy 10x/second that every later annotation feature would inherit. ZERO NEW DEPENDENCIES across all twelve decisions.

### rationale

Every decision was chosen against the same two filters: (a) does it make a claim verifiable by construction (type system, module boundary, API shape) rather than by convention or call-site discipline, and (b) does it avoid a disproportionate build against this spec's PRD 'L' estimate and actual scope (discovery + first connection, not a media pipeline). The NFR-1 waiver (decision 1) is the clearest example: no H.264 decoder exists anywhere in this repo today, building the only transport that could plausibly meet <150ms would be a multi-week effort and a new, unjustified dependency, and launching scrcpy as a child process breaks the canonical single-window architecture F-1 just established. Staging the decision behind MirrorTransport (FR-25) means the eventual fix is a new class and a DI line, not a rewrite. The data-device-api/impl split (decision 6) and the closed DeviceCommand set exist so NFR-5 and the 'no phone in CI' requirement (AS-7) are structural guarantees, not promises. The pixel/state split (decision 12) and the F-2/F-3 error-rendering boundary (decision 2) both exist to stop a short-term shortcut (reducing every frame, or building error-card chrome twice) from becoming a cost every future feature inherits. Reuse was preferred throughout: ErrorCard and CommandBlock already exist (so F-3 composes shipped components against F-2's typed states), SourceListRow/SegmentedButtonGroup/DeviceChip/StatusIndicator/AppButton/KeyCap/SectionHeader all exist and are reused as-is or with one additive parameter; only 2 molecules (PlatformToolCard, EmulatorSelect), 2 organisms (DevicePickerPanel, NoSignalStage), and 1 extracted atom (RadioMark) plus one dumb molecule (MirrorFrameView) must be authored.

### architectureOverview

DATA: new data-device-api (library+di) holds DeviceId (value class, of() factory rejecting invalid input), DeviceCommand (closed sealed set), DeviceCommandExecutor/AdbHostSource/EmulatorSource/MirrorTransport interfaces, MirrorFrame/TransportStats/ToolProbe resource types, and ALL pure parsers in commonMain (parseAdbDevices, parseWmSize, parseWmDensity, parseAvdList, trimToPngSignature) — unit-testable with no phone attached. data-device-impl holds, desktopMain-only and internal: ProcessRunner (the single ProcessBuilder in the repo; runInterruptible/Dispatchers.IO, concurrent stdout/stderr drain, withTimeoutOrNull, destroyForcibly in finally, live-child registry + one shutdown hook), ToolResolver (ordered-candidate-list probing), and the source/transport impls; DI via an internal expect fun Module.provideDeviceSources() with desktop/android/iOS actuals (android/iOS register UnsupportedDeviceSources so common code compiles on all four targets). data-repository-impl gains DeviceRepositoryImpl + mappers, owning the 2s polling flow and the empty-poll escalation into NoDeviceFound. DOMAIN: domain-core gains source/model/device/ (DeviceModel, EmulatorModel, ToolModel, DiscoveryModel sealed [Detecting|ToolMissing|NoDeviceFound|Found], MirrorStatsModel) and Failure.Technical.Process(cause). domain-repository-api gains DeviceRepository; five use cases (Detect/ObserveDiscovery/BootEmulator/StartMirroring/ObserveMirrorStats) through resultLauncher. PRESENTATION: presentation-core-ui gains RadioMark (extracted atom), PlatformToolCard + EmulatorSelect (molecules), DevicePickerPanel + NoSignalStage (organisms), MirrorFrameView (dumb molecule). presentation-feature-mirror (new, Orbit MVI) holds MirrorContract/MirrorViewModel/MirrorContent + MirrorHost entry point, the pollingEnabled gate, the 1s stats ticker, the separate frame StateFlow, and the picker's key handling. SHELL WIRING: RectifyWindow injects MirrorViewModel and passes state/frame/onIntent into RectifyShellContent, which stays Koin/AWT/coroutine-free (zero new imports) so the render harness keeps working from literal MirrorUiState values. PIXEL PATH: MirrorTransport emits PNG bytes as MirrorFrame; decode happens once per frame on Dispatchers.Default in the ViewModel via ByteArray.decodeToImageBitmap() (Compose Multiplatform 1.12 common, skiko desktopMain fallback, classpath reachability to be verified at plan stage — no new dependency either way) and is published on a StateFlow<ImageBitmap?> outside Orbit state.

### techStack

| area | choice | reason |
| --- | --- | --- |
| Process execution | java.lang.ProcessBuilder inside one internal desktopMain class, wrapped in runInterruptible(Dispatchers.IO) with withTimeoutOrNull and destroyForcibly cleanup | Zero new dependencies; the JDK already does everything needed. The value is not the API, it is that exactly one file in the repository is allowed to call it. |
| Mirror transport (v1) | adb exec-out screencap -p, adaptive single-flight loop, 100ms floor, 5s per-capture timeout, PNG-signature trim | Proven in the poc, no dependency, demoable today. Known to miss NFR-1 — a recorded waiver, not an oversight (FR-24). |
| Mirror transport (v1.1+) | scrcpy-server socket + in-JVM H.264 decode behind the same MirrorTransport interface | Deferred. Requires a new decoder dependency justified against version-catalog-hygiene.md and a latency spike first; the interface is the only part of it this spec builds (FR-25). |
| Frame decode | ByteArray.decodeToImageBitmap() (Compose Multiplatform 1.12 common) on Dispatchers.Default, with skiko Image.makeFromEncoded(...).toComposeImageBitmap() as the desktopMain fallback | Both are already on the classpath via Compose Desktop; no catalog entry needed either way. |
| Concurrency / polling | kotlinx.coroutines Flow — flow{}+delay(2s) for discovery, flatMapLatest for session switching, viewModelScope for lifetime | Cancellation is structural, so the 2s poller cannot leak: window close → VM cleared → scope cancelled → delay cancels → finally destroys children. No Timer, no thread, nothing to remember to stop. |
| Emulator enumeration | `emulator -list-avds` for names; running AVDs matched by the emulator-NNNN serial from `adb devices -l` plus `adb -s <serial> emu avd name` | `emulator` ships with the SDK and prints exactly the names comp 01 shows. avdmanager needs cmdline-tools, a separate optional install, and returns far more than is needed. |
| Emulator boot | `emulator -avd <name>` spawned detached with output discarded, untracked and untimed | The emulator is meant to outlive Rectify (Android Studio behaves the same way). Discovery picks it up on a later 2s poll — no bespoke progress protocol. |
| Error typing | New Failure.Technical.Process(cause); DeviceCommandException(argv, exitCode, stderrTail) as the cause | No existing subtype means 'an external CLI failed'. One new sealed case now is cheaper than string-matching forever. |
| Presentation state | Orbit MVI in presentation-feature-mirror for semantics; a separate StateFlow<ImageBitmap?> for pixels | orbit-mvi.md is mandatory for feature modules; routing 10 frames/s through a reducer is not what a reducer is for. |
| Testing | kotlin.test in commonTest against pure parsers and fake sources; kotlinx-coroutines-test virtual time for the 2s poller (wiring to be verified) | AS-7 requires passing tests with no phone attached. Putting every parser in commonMain is what makes that achievable rather than aspirational. |
| New dependencies | None | Everything above is JDK, kotlinx-coroutines, Compose Multiplatform or Koin — all already wired. The only dependency this spec would have needed (an H.264 decoder) is precisely the thing FR-24 defers. |

### openRisks

| text | likelihood | impact | mitigation |
| --- | --- | --- | --- |
| Screenshot polling delivers roughly 3–7 fps and ~250–500 ms per frame, so the mirror feels like a slideshow under drag and NFR-1 is visibly unmet in the demo. | high | med | Already an accepted, recorded waiver (FR-24/AS-8). Keep the fps stat honest, keep latency a placeholder (AC-45), and keep MirrorTransport narrow enough that the decoder transport is a new class and a DI line, not a refactor. |
| PATH in a packaged macOS .app omits /opt/homebrew/bin, so adb detection works all through development and fails the first time anyone double-clicks the app. | high | high | ToolResolver probes explicit SDK and Homebrew paths before PATH, and ToolProbe records every path tried so the comp-04 footer tells the truth. Add a unit test over the probe-order function with a fake filesystem predicate. |
| Orphaned adb children after an abrupt window close hold the USB connection and make the next launch see no device. | med | med | Live-child registry + single shutdown hook + destroyForcibly in finally + per-call timeouts. Never kill the adb server daemon itself. |
| Three new Gradle modules plus edits to five existing ones blows the 3GB Kotlin daemon when built together. | med | med | The implementation sequence is already per-module and ordered bottom-up; build one module per step, never `./gradlew build` at the root. |
| A commonTest backtick name containing a comma or apostrophe compiles on JVM and breaks compileTestKotlinIosArm64 — the exact bug F-1 shipped, and this spec adds the most test files yet. | med | high | Plain-word backtick names only (no punctuation at all) and run one iOS test-compile before declaring the slice done. |
| Frames pushed through the Orbit reducer at 10/s cause allocation churn and sluggish UI that gets misdiagnosed as transport latency. | med | med | Pixels live in a dedicated StateFlow outside Orbit state; only semantic state goes through the reducer. Stats update on a 1s ticker, not per frame. |
| Adding Failure.Technical.Process to a sealed hierarchy in the zero-dependency core breaks exhaustive `when` expressions in unrelated modules. | low | low | Grep for `is Failure.Technical` before the change; today nothing branches exhaustively on it. Compile domain-core and domain-usecase-impl first. |
| The picker's key handling and F-1's window-level Cmd+1/2/3 handler fight over focus once the side panel exists in a later spec. | low | med | Scope split by modifier: unmodified keys belong to the focused panel, meta-modified keys to the window. Add a VM test asserting arrow/Enter never carry isMetaPressed semantics. |
| Unauthorized devices are filtered out (the poc's behaviour), so a user with a plugged-in phone sees NO SIGNAL and follows guidance that cannot help them. | med | med | Parse the state column into a typed enum, keep non-`device` entries in the model, and render them as non-selectable rows with their state as the trailing label. |
| scrcpy 3.3.1 is installed on this machine and will tempt a 'just shell out to scrcpy' shortcut during implementation, which would open a second OS window and break F-1's canonical-window contract. | low | high | The closed DeviceCommand set contains no scrcpy command; adding one would be a visible, reviewable API change rather than a one-line call site. |
| Pre-existing repo-wide hazard, not unique to this spec: resultLauncher catches Throwable, which includes CancellationException, so a withTimeout (not withTimeoutOrNull) inside any use case would swallow real parent-scope cancellation. This spec's own use cases use withTimeoutOrNull to sidestep it locally, but the helper itself is unfixed and every other use case in the repo remains exposed. | med | high | Not fixed by this spec (would touch every existing use case). Flagged for a dedicated follow-up: add a `catch (c: CancellationException) { throw c }` clause to resultLauncher itself. |

### openQuestions

- Measured or cited glass-to-glass latency for scrcpy-server + in-JVM H.264 decode is still unknown — the plan/a spike should get a real number before committing to it as the v1.1+ follow-up implied by FR-24/FR-25. The v1 screenshot-polling number is also not yet measured and must be captured during implementation (per implementationSteps item 3), not estimated.
- Whether v1's UI should visibly indicate to the user that the stream is polling-based and not meeting the 150ms target (vs. staying silent) is a UX-writing decision outside clarify/brainstorm's scope — recommendation on record is a one-word mono hint next to the fps stat, but this is flagged for plan stage / human override.
- Whether SourceListRow needs new props/states for the picker's radio + trailing-slot variants (Running dot / Boot button / Android-version+USB) and a disabled/non-selectable state for unauthorized/offline rows, or a new molecule is warranted — plan stage confirms via full kit/ browsing per design-tokens.md.
- Does the device-picker panel render over StageSurface's dot grid or replace/skip it entirely while open? Recommendation on record: fill the content slot with an opaque Theme.color.bg surface (comps 01/03 show no dots behind the panel) — not yet confirmed at plan stage.
- Does the status-bar stat line (version/resolution/fps/latency) get its own molecule or stay an inline Row in RectifyShellContent? Recommendation on record: inline Row for v1, promote only when a second surface needs it — not yet confirmed.
- Should discovery polling (the 2s adb poll) continue while a mirror session is active, or pause in favour of the capture loop's own failure as the disconnect signal? Recommendation on record: pause while mirroring — not yet confirmed.
- No clipboard abstraction exists yet for F-3's Copy action on comps 04/05, but F-2's ToolProbe must carry the exact command string F-3 will copy — the data shape needs to be agreed between the two specs, not assumed.
- No localisation keys exist for any of this spec's copy ('Choose what to mirror', 'Waiting for an Android device', the numbered steps, 'NO SIGNAL', the polling footer) — per localisation.md they belong in presentation-core-localisation with `mirror_*` keys; comps give English only.
- Whether `ByteArray.decodeToImageBitmap()` is reachable from presentation-feature-mirror's commonMain under Compose Multiplatform 1.12 is unverified; the skiko desktopMain fallback is the backup. No new dependency either way, but needs a classpath check at plan stage.
- Whether kotlinx-coroutines-test is actually wired into a module's test source set (it is in the version catalog, which is not the same as wired) is unverified — needed for virtual-time testing of the 2s poller.
- No CI harness exists to run the desktop app headlessly, so AC-1/AC-4/AC-25/AC-45's 'demo' verification stays a manual, human-observed step for this spec.

### readyForPlan

true

### proposedSurvey

| question | options | recommended | rationale |
| --- | --- | --- | --- |
| Final Stage 1 sign-off for android-device-discovery-mirroring (F-2): this Preparation deliverable consolidates input.json, clarify.json, and brainstorm.json — 8 FR (FR-1..FR-5, FR-22, FR-24, FR-25), 4 NFR, 10 AC, 4 scenarios, 12 binding decisions (including the recorded NFR-1 waiver: v1 ships screenshot polling, knowingly not meeting the <150ms target, behind a swappable MirrorTransport interface), zero new dependencies, and 11 carried-forward open risks / 11 open questions for the plan stage to pick up. Approve this consolidated Preparation and open the gate to Stage 2 (Plan)? | ["Approve — Preparation is correct and complete; proceed to Plan.", "Approve with the NFR-1 waiver specifically flagged for re-review before ship (plan must carry it forward explicitly, not resolve it silently).", "Request changes before Stage 2 can start."] | Approve — Preparation is correct and complete; proceed to Plan. | All three upstream artifacts are already gate-approved (orchestrator-auto, human-type gate) and every trace id (FR/NFR/AC/SC/AS/GOAL) is carried through unrenumbered and unreworded. The twelve binding decisions are consistent with each other (e.g. the closed DeviceCommand set in decision 6 structurally enforces NFR-5 from decision 4; the MirrorTransport interface in decision 1 is what decision 9's measured-fps requirement and FR-25's swap-seam both depend on). No new contradictions were introduced during consolidation. The NFR-1 shortfall is the one item with real product-visibility risk, which is why it is stated prominently in the summary and chosenApproach rather than left to be discovered in the plan — the recommended option proceeds on the basis that the waiver is already explicit, recorded, and interface-isolated (a v1.1+ swap, not a rewrite), which is the condition AS-8 set for accepting it. |

### artifacts

#### input

input.json

#### clarify

clarify.json

#### brainstorm

brainstorm.json

### survey

| question | options | answer | source | rationale |
| --- | --- | --- | --- | --- |
| Final Stage 1 sign-off for android-device-discovery-mirroring (F-2): this Preparation deliverable consolidates input.json, clarify.json, and brainstorm.json — 8 FR (FR-1..FR-5, FR-22, FR-24, FR-25), 4 NFR, 10 AC, 4 scenarios, 12 binding decisions (including the recorded NFR-1 waiver: v1 ships screenshot polling, knowingly not meeting the <150ms target, behind a swappable MirrorTransport interface), zero new dependencies, and 11 carried-forward open risks / 11 open questions for the plan stage to pick up. Approve this consolidated Preparation and open the gate to Stage 2 (Plan)? | ["Approve — Preparation is correct and complete; proceed to Plan.", "Approve with the NFR-1 waiver specifically flagged for re-review before ship (plan must carry it forward explicitly, not resolve it silently).", "Request changes before Stage 2 can start."] | B. Approve with the NFR-1 waiver specifically flagged for re-review before ship. | ai-proposed | Deliberately NOT the recommended option A. B is strictly more honest at zero cost: v1 knowingly misses NFR-1's <150ms target, and flagging that for re-review before ship is exactly what the waiver deserves. The orchestrator had already committed to surfacing it in the final chain report, so B simply makes that commitment part of the artifact rather than a promise held outside it. Autonomy was granted to exercise judgement, not only to take the first option. |
