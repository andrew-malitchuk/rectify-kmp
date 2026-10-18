# resolve-and-send-mcp — plan

## Envelope

| field | value |
| --- | --- |
| stage | plan |
| step | plan |
| status | done |
| producedBy | sdd-build-plan-refine |
| schemaVersion | 1 |
| createdAt | 2026-10-18T11:50:13Z |
| updatedAt | 2026-10-18T11:50:31Z |

## Gate

### type

human

### approvedBy

human

### approved

true

### at

2026-10-18T11:50:13Z

## Provenance

### selfHash

290d3fc7fcdfa323bad3dd726b49401b5ae9a35bb9e81aae4bbd84904fb1de18

### inputs

| artifact | hash |
| --- | --- |
| preparation/preparation.json | 467dac29e61d0bd77248e0d08fb1f0f512e833b1b2477390b32a751c20e0a308 |

## Data

### meta

#### title

Resolve & Send via Local MCP (F-7)

#### version

2

#### status

final

### goal

Wire the already-mounted-but-inert toolbar 'Resolve & Send' button (RectifyWindow.kt:304) into a complete handoff pipeline: assemble F-6's payload bundle into a versioned @Serializable AnnotationEnvelope, publish it through a single-slot atomic file drop at ~/.rectify/handoff/latest.json, and serve it to a Claude Code agent through a new bounded in-repo stdio MCP server stub (:mcp-application) exposing exactly one tool, get_latest_annotation. The toolbar animates Streaming -> Frozen -> Dumping hierarchy -> Sent through the already-shipped StatusLine, confirms with a toast whose 'Open session' action copies a minted session identifier to the system clipboard, then resumes live mirroring and resets the staged counter to 0.

### problemStatement

F-6 (merged) assembles the full payload bundle but stops short of transmitting it, and declares PAYLOAD_FORMAT_VERSION = 1 as an internal const serialized nowhere. FR-17's premise — 'the existing local rectify MCP server' — is factually false against this codebase: a repo-wide grep found zero MCP, JSON-RPC or get_latest_annotation code, and gradle/libs.versions.toml has no MCP SDK, no JSON-RPC library and no ktor-server-* entry. Three further gaps the PRD could not see, all re-verified against source during this refine pass: HierarchySnapshotMapper.toModel holds HierarchyDumpStdout.dump and discards it (HierarchySnapshotMapper.kt:52-62); MirrorViewModel.observeFrames maps MirrorFrameModel.bytes to an ImageBitmap and drops the bytes (MirrorViewModel.kt:219-223); and nothing anywhere has a session identifier for FR-58's clipboard copy to act on.

### highLevelDescription

Three seams, two wiring onto shipped pieces and one genuinely new. (1) UI wiring: the stubbed AppButton at RectifyWindow.kt:304 gets enabled/busy/onClick, Clear is disabled while in flight, Cmd+Enter joins handleShellShortcut through a hoisted send-action holder (the window-level onPreviewKeyEvent cannot see into the Window content lambda — RectifyWindow.kt:139-141 says so in as many words), StatusLine renders a pure SendPhase -> List<StatusStep> projection as a top-aligned stage overlay, and a StackedSnackbarHost hosts the completion toast whose action does an AWT clipboard write at the window layer. No new design-system component. (2) Envelope and publication: a new library module :data-handoff-api holds a @Serializable AnnotationEnvelope with version as its first field plus one AnnotationHandoff interface, implemented once in desktopMain as a file-slot writer/reader. Image bytes land as sibling PNGs in a per-send directory named by sentAtMs; latest.json is written last via temp-write + Files.move(ATOMIC_MOVE, REPLACE_EXISTING). (3) The server: a new plain-JVM :mcp-application whose main() is a readLine() loop over System.in dispatching hand-rolled JSON-RPC (initialize, notifications/initialized, tools/list, tools/call) over kotlinx-serialization, writing protocol frames to System.out exclusively and every diagnostic to System.err. Two retained-but-currently-discarded data points feed the assembly: the frozen frame's original adb-captured PNG bytes (held as ONE value beside the bitmap, see architecture.interfaces) and HierarchySnapshotModel.rawDump.

### background

F-7 is the last of the PH-1 mirror specs and depends on F-6 (payload-preview, merged), which built every piece of the payload except its transmission. F-1 shipped RectifyWindowShell and its five named toolbar slots; F-2 the capture transport; F-3 the Reconnect/stream-drop precedent governing send-failure recovery (AS-31); F-4 the frozen frame and staged annotations; F-5 the hierarchy dump. Nine architecture, naming and transport decisions were settled at prior human gates in clarify.json and brainstorm.json and are carried here as closed. Two decisions preparation carried as open (session-identifier composition, stale-slot policy) are closed in this plan.

### sources

| type | ref | cached |
| --- | --- | --- |
| code | .pipeline/sdd/docs/resolve-and-send-mcp/preparation/preparation.json | .pipeline/sdd/docs/resolve-and-send-mcp/preparation/preparation.json |
| code | .pipeline/sdd/docs/resolve-and-send-mcp/preparation/clarify.json | .pipeline/sdd/docs/resolve-and-send-mcp/preparation/clarify.json |
| code | .pipeline/sdd/docs/resolve-and-send-mcp/preparation/brainstorm.json | .pipeline/sdd/docs/resolve-and-send-mcp/preparation/brainstorm.json |
| code | .pipeline/sdd/docs/resolve-and-send-mcp/preparation/input.json | .pipeline/sdd/docs/resolve-and-send-mcp/preparation/input.json |
| figma | design-rectify-ui-kit.html — Resolve & Send primary button, Clear ghost, send status steps (+failure variant), send-complete toast | .pipeline/sdd/docs/resolve-and-send-mcp/inputs/design-rectify-ui-kit.html |
| figma | design-rectify-window-screens.html — payload / success / err-stream screens, canSend logic, sendLabel hint | .pipeline/sdd/docs/resolve-and-send-mcp/inputs/design-rectify-window-screens.html |
| code | compose-application/src/desktopMain/kotlin/dev/rectify/io/source/window/RectifyWindow.kt:304 — the stubbed AppButton(text = "Resolve & Send", onClick = {}, shortcut = "⌘↵", enabled = false) this spec wires; and :134-141, the onPreviewKeyEvent scope constraint that shapes Cmd+Enter |  |
| code | presentation-core-ui/.../molecule/status/StatusLine.kt:163-193 — its own @Preview already renders both AC-19 variants verbatim |  |
| code | presentation-core-ui/.../atom/snackbar/StackedSnakbarHostState.kt:79-128 — the host state type (name typo'd in source) and showSnackbar(title, description, actionTitle, action, duration) |  |
| code | presentation-feature-mirror/.../core/payload/ — F-6's buildPrompt, cropWindow, PAYLOAD_FORMAT_VERSION |  |
| code | presentation-feature-mirror/.../MirrorViewModel.kt:210-225 (observeFrames), :353-356 (stageAnnotation freeze), :536-544 (clearStaged — PP-TD-2 verified) |  |
| code | data-repository-impl/.../core/mapper/HierarchySnapshotMapper.kt:52-62 — holds stdout.dump and discards it |  |
| code | desktop-application/build.gradle.kts + desktop-application/CLAUDE.md — the documented plain-JVM bespoke-Gradle precedent :mcp-application extends |  |
| code | data-device-impl/.../source/DeviceSourcesProvider.kt — the internal expect fun Module.provideX() + Unsupported* shape to copy |  |
| code | data-core/CLAUDE.md — 'No external dependencies allowed — this module is a leaf', the evidence that the envelope cannot live there |  |

### scope

#### in

- Wire the existing stubbed toolbar Resolve & Send button (RectifyWindow.kt:304) — enabled/busy/onClick — plus disable Clear while a send is in flight, and add Cmd+Enter to handleShellShortcut via a hoisted send-action holder (FR-16, AC-17, AC-20).
- Add a send state machine: public MirrorIntent.ResolveAndSend, public SendPhase in MirrorUiState, a derived canSend guard, and a reducer-level in-flight/zero-staged guard that Cmd+Enter cannot bypass (FR-16, FR-18, AC-17, AC-20).
- Retain the frozen frame's ORIGINAL adb-captured PNG bytes by holding bitmap and bytes as ONE value (HeldFrame) on a single private StateFlow, so the freeze reads the pair in one read. No re-encode of the full frame anywhere (FR-17, AC-18).
- Thread the raw uiautomator dump through: add rawDump: String = "" to HierarchySnapshotModel (defaulted so every existing construction site including Empty keeps compiling) and populate it in HierarchySnapshotMapper.toModel from HierarchyDumpStdout.dump (FR-17, AC-18).
- Add :data-handoff-api (library convention plugin + kotlin-serialization plugin, both already in the catalog): commonMain source/envelope/AnnotationEnvelope.kt with version as the first field, commonMain source/handoff/AnnotationHandoff.kt, desktopMain core/slot/FileSlotAnnotationHandoff.kt and the fileSlotAnnotationHandoff() factory. No Koin, no di convention plugin — the server's classpath stays envelope + serialization only (AC-32, NFR-12, NFR-15).
- Add :mcp-application (new plain-JVM module, kotlin("jvm") + the core Gradle application plugin + kotlin-serialization): a stdio JSON-RPC main() exposing exactly get_latest_annotation, reading the published slot on every call, returning metadata + prompt + hierarchy inline with images by file:// reference (FR-56, AC-18, AC-81).
- Encode per-annotation crop PNGs in a NEW presentation-feature-mirror/desktopMain source set behind an injected CropEncoder seam (ImageBitmap + DeviceRect -> ByteArray?), reusing F-6's cropWindow for the padded source geometry, mirroring the existing decodeFrame parameter (FR-17, AC-18).
- Add presentation-feature-mirror/core/send/: pure, internal, Compose-free sendSteps(phase, session, hierarchy), canSend(state), toastLabel(count, elapsedMs), mintSessionId(deviceId, frameId, sentAtMs) and EnvelopeAssembly. These are where AC-17/AC-19/AC-20/AC-32 are actually proved (no Compose UI-test harness exists).
- Render the status sequence through the already-shipped StatusLine as a top-aligned overlay inside MirrorStageContent, visible regardless of which side-panel tab is active (FR-18, AC-19).
- Host the completion toast on StackedSnackbarHost newly mounted at the RectifyWindow layer, with an EXPLICIT non-indefinite duration; its 'Open session' action copies the session identifier via java.awt.Toolkit systemClipboard in compose-application/desktopMain, following CommandBlock's documented host-owns-clipboard rule (FR-18, FR-58, AC-19).
- On success: capture the toast's count BEFORE resetting, then resume live mirroring and reset the staged counter to 0 (FR-19, AC-21).
- On failure: preserve the frozen frame, its bytes and the staged batch untouched; re-enable Resolve & Send for retry with the same batch; surface through F-3's existing error affordances rather than a parallel path (AC-20, AS-31, SC-25).
- Fix bugs.json#PP-TD-2 in the one-line reducer change it already prescribes: ClearStaged must also clear promptOverride. IN SCOPE because F-7 is the first spec where that latent bug sends a WRONG PROMPT TO THE AGENT rather than mis-rendering a preview — verified against MirrorViewModel.kt:536-544, which copies frozen with annotations = emptyList() and leaves promptOverride intact, and against EnvelopeAssembly's promptOverride ?: buildPrompt(...). Gets its own implementation step and its own traceability row.
- Lock the handoff directory down to owner-only POSIX permissions where the filesystem supports it, and prune superseded per-send directories on each publish (NFR-4, SEC).
- Document the Claude Code MCP wiring: a checked-in .mcp.json.example naming the :mcp-application installDist launcher path, plus :mcp-application/CLAUDE.md, :data-handoff-api/CLAUDE.md and a root CLAUDE.md module-list update.

#### out

- File-drop or webhook transport exposed to the AGENT. The agent only ever calls an MCP tool and never learns a path; the slot is private plumbing between two local Rectify-owned processes. This is the reading of the PRD's 'no file fallback transport' that decision 5's human gate approved.
- Full production-grade MCP server hardening: multi-client support, auth/pairing handshake, tool discovery beyond the single retrieval tool, transport reconnection, wait_for_annotations / push semantics. Explicitly deferred (AS-32).
- Encrypting payload bytes at rest. The slot is owner-readable only and local, which is what NFR-4 asks for; encryption is a policy nobody specified.
- Generating or mutating the user's real Claude Code MCP configuration. F-7 documents the entry and ships an example; writing into a user's config from an app is a trust-boundary action nobody asked for, and installDist must have run first anyway.
- The deeper PP-TD-7 contract change (PromptOverride(text, seededAgainst)). F-7 takes the PP-TD-2 one-liner only; the root-cause contract change needs its own task and gate.
- Any work belonging to F-8 (verify-and-rediff-loop) or F-9 (history-batch-tracking). The single-slot shape is deliberately not a ring buffer; F-9 can widen it behind the same interface.
- iOS hierarchy dump / iOS payload send — inherits F-5/F-6's Android-only boundary (AS-19, AS-23, AS-29).
- Replacing the no-measured-NFR-1 capture transport, or anything else in the already-waived mirror backlog.
- Localising the toolbar, status-step or toast strings. They follow footerLabel's machine-text precedent and the existing hardcoded English in RectifyWindow; the inconsistency is recorded as debt, not fixed here.

### assumptions

- AS-28 — F-6's PAYLOAD_FORMAT_VERSION = 1 is today an internal const in presentation-feature-mirror/core/payload/PayloadEstimate.kt, serialized into nothing because nothing is transmitted yet. F-7 reads it and embeds it into the actual envelope for AC-32. Verified by reading the file directly.
- AS-29 — The hierarchy-dump phase stays Android-only, matching measure-mode's AS-19 and payload-preview's AS-23. F-7 ships whatever F-6 already assembled: an empty hierarchy field when HierarchyStatus is Absent or Unavailable, and unconditionally on iOS.
- AS-30 — The send runs asynchronously off the UI thread. FR-18's four-step animated sequence and AC-20's 'disabled while in flight, re-enables after toast or failure' only mean anything if the UI keeps rendering while the send is in progress.
- AS-31 — On a failed send, staged annotations and the frozen frame are NOT cleared, following F-3's shipped Reconnect precedent (FR-33/FR-35). Resolve & Send simply re-enables (AC-20); only explicit Clear or Resume Live discards staged annotations.
- AS-32 — The MCP server this spec builds is a bounded stub sufficient to prove AC-18/AC-81, not a fully spec-compliant multi-session implementation. Hardening is explicit future work, not silently assumed-away scope.
- The adb-captured MirrorFrameModel.bytes are already valid PNG (ScreencapMirrorTransport trims to the PNG signature before publishing — data-device-impl/CLAUDE.md:63), so frame.png is a byte copy and never a re-encode.
- A plain-JVM module can consume a KMP module's jvm("desktop") variant. Proven today: :desktop-application (kotlin("jvm")) depends on :compose-application (KMP) and resolves.
- Two sends cannot land in the same millisecond in practice (a send takes ~1s), so sentAtMs is a usable per-send directory name; the writer still uses REPLACE_EXISTING so a collision overwrites rather than throws.
- DECLARED DEVIATION FROM PREPARATION WORDING 1 — preparation's scope line names 'AnnotationHandoff/AnnotationHandoffReader interfaces'. This plan ships ONE interface with three members. There is exactly one implementation and the server simply never calls publish; a second interface would buy a type-level guarantee nobody is at risk of violating. Recorded here rather than taken silently, because preparation is the authoritative artifact.
- DECLARED DEVIATION FROM PREPARATION WORDING 2 — preparation places the Android/iOS Unsupported* actuals in :data-handoff-api. This plan puts UnsupportedAnnotationHandoff and the provideHandoffSources() expect/actual set in presentation-feature-mirror instead, so :data-handoff-api stays Koin-free and the server's classpath stays envelope + serialization only (the whole point of the 1500 ms spawn budget). Same shape, different module.

### constraints

| type | text |
| --- | --- |
| tech | Android, iOS and Desktop-JVM only (platform-coverage.md). There is no macOS/Linux native target and no macosMain anywhere. Every new desktop-only source file lands in a desktopMain source set; androidMain/iosMain get Unsupported* actuals so all targets keep linking. |
| tech | explicitApi() is enforced repo-wide: every public declaration in :data-handoff-api, in MirrorContract's new SendPhase/intent/side-effect cases, and in the new CropEncoder needs an explicit visibility modifier AND an explicit return type. :mcp-application is the one module with no convention plugin and therefore no explicitApi() — its declarations should still be internal by default. |
| tech | package-layout.md: core/ holds internal declarations only, source/ holds public API grouped per concern, and a module with no internal declarations gets no core/ at all. :data-handoff-api therefore gets core/slot/ only because FileSlotAnnotationHandoff is internal; its envelope and interface are source/. |
| tech | No convention plugin models a plain-JVM module — all four are built around KotlinMultiplatformExtension (convention-plugins.md). :mcp-application needs its own small bespoke build.gradle.kts. This extends the deviation :desktop-application already set and documented by exactly one, and it is declared here rather than discovered in review. It knowingly contradicts NFR-15's 'introducing no bespoke Gradle' clause; decision 6's human gate supersedes that clause and the deviation is recorded in requirements.nonFunctional (NFR-15), in the NFR-15 traceability row and in definitionOfReady so Stage 5 validation meets it rather than files it as a defect. |
| tech | Zero new version-catalog entries. kotlinx-serialization-json (libs.kotlinx.serialization.json), the serialization compiler plugin (libs.plugins.kotlin.serialization) and the JVM plugin (libs.plugins.kotlinJvm) all already exist in gradle/libs.versions.toml and are already used. No MCP SDK, no JSON-RPC library, no ktor-server-*. |
| tech | testing.md: kotlin.test is wired ONCE in the Library/Feature convention plugins, so :data-handoff-api and presentation-feature-mirror must never declare it. :mcp-application has no convention plugin and therefore MUST declare testImplementation(kotlin("test")) itself — a sanctioned exception, declared here. |
| tech | testing.md: a commonTest backtick name containing a comma or a colon compiles on desktop and fails compileTestKotlinIosArm64. presentation-feature-mirror/CLAUDE.md goes further and requires plain-word camelCase names in this module. Apostrophes are fine. |
| tech | No Compose UI-test harness exists (no runComposeUiTest, no ui-test-junit4), so AC-19's animated ordering is provable only through the pure sendSteps projection plus a @Preview pair. Introducing a harness is out of scope. |
| tech | presentation-feature-mirror/CLAUDE.md deviation 2: pixels never enter the reduced state. rawDump and frame bytes must stay out of MirrorUiState, on StateFlows beside frame/frozenFrame/frozenHierarchy. |
| tech | RectifyWindow.kt:134-141: onPreviewKeyEvent is a Window PARAMETER and cannot see into the Window's content lambda — this is why isPanelVisible is passed as a literal today. Cmd+Enter therefore cannot read mirror state or dispatch a MirrorIntent directly from handleShellShortcut; it must go through a holder hoisted beside shellState. See architecture.interfaces 'Cmd+Enter wiring'. |
| tech | Koin load order in initKoin() is data -> domain -> presentation and is load-order significant. :data-handoff-api binds nothing itself; the Koin binding lives in presentation-feature-mirror's existing module via internal expect fun Module.provideHandoffSources(). |
| tech | MirrorViewModel is already bound with viewModel { MirrorViewModel(...) } and explicit get<T>() (PresentationFeatureMirrorModule.kt:20-25), NOT viewModelOf — which is exactly what the new defaulted seams require. Keep it that way. |
| business | Effort tier is 'fast'. The server is a bounded stub (AS-32); anything that looks like protocol completeness is out. |

### dependencies

| on | note |
| --- | --- |
| payload-preview (F-6, merged) | supplies buildPrompt, cropWindow, PAYLOAD_FORMAT_VERSION, FrozenFrame.frameId/deviceLabel/promptOverride and MirrorPayloadTab. F-7 consumes all of them and rebuilds none. |
| error-handling-and-recovery (F-3, merged) | its Reconnect/stream-drop semantics are the basis for AS-31, and MirrorErrorCard/MirrorDropBanner/FeedbackBar is the error surface a failed send plugs into. |
| measure-mode (F-5, merged) | supplies DumpHierarchyUseCase, HierarchySnapshotModel and HierarchyStatus (Absent/Dumping/Ready/Unavailable), and establishes the Android-only hierarchy boundary F-7 inherits (AS-29). |
| drag-annotate-frozen-frame (F-4, merged) | supplies FrozenFrame, StagedAnnotation, DeviceRect and the freeze path F-7 hooks frame-byte retention into. |
| app-shell-window-layout (F-1, merged) | supplies RectifyWindowShell, ShellToolbar's five named slots and handleShellShortcut, all of which F-7 extends rather than replaces. |
| HierarchySnapshotModel.rawDump retention (step 2 of this plan) | FR-17/AC-18's hierarchy.xml blocks on it. |
| Frozen-frame raw PNG byte retention (step 1 of this plan) | FR-17/AC-18's frame.png blocks on it. |
| Claude Code's own MCP client configuration | outside this repo's control. The user must add the documented entry and run :mcp-application:installDist before AC-18 can be demonstrated end to end. |
| JDK 21 java.nio.file (Files.move ATOMIC_MOVE, PosixFilePermissions) and javax.imageio | both already on the desktop toolchain, neither a new dependency. |

### risks

| text | likelihood | impact | mitigation |
| --- | --- | --- | --- |
| The file-slot handoff is judged in review to violate the PRD's 'no file fallback transport', forcing a late rewrite to a socket. | med | high | Settled at the brainstorm human gate with the reading stated explicitly: the prohibition governs the AGENT-facing transport, and the agent only ever calls an MCP tool and never learns a path. AnnotationHandoff is an interface precisely so the implementation can become an AF_UNIX socket later without touching the ViewModel, the envelope, or the server's tool surface. |
| A multi-megabyte tool result on one JSON-RPC stdout line exceeds what the agent's stdio reader handles, failing exactly the large payloads users care most about. | med | high | Decision 8: images are returned by file:// reference, never inlined or base64-encoded. The inline portion is metadata + prompt + hierarchy XML only, with a 512 KB budget; above it the XML truncates with an explicit marker rather than failing the call. A measured three-annotation 1080x2400 send is a required implementation check. |
| The second process entry point drifts out of sync with the envelope format — a stale installDist launcher runs against a newly published slot. | med | med | This is what AC-32 is for. The server reads version FIRST and refuses an unknown version with a message naming both numbers and the rebuild command. A unit test asserts the refusal message contains both. |
| AC-18 and AC-81 ship 'verified' on unit tests alone and the first real Claude Code session finds the integration broken — wrong initialize reply, wrong tool-result shape, or a stray stdout println silently corrupting the protocol. | high | high | One manual end-to-end run against a real Claude Code session is a hard exit criterion in definitionOfDone. Plus a module rule for :mcp-application (no println outside the single frame writer) enforced by a test asserting the dispatcher's output is exactly one line with no embedded newline, and a grep-level check in the task's DoD. |
| SILENT CORRECTNESS BUG: the frame bitmap and the raw bytes of the 'same' frame drift apart, so frame.png would not match the pixels the user annotated and every annotation rectangle would address the wrong image. | med | high | Structural, and stronger than the candidate's co-assignment: bitmap and bytes are ONE value (HeldFrame) on ONE private StateFlow. Co-assigning two separate flows in one collect body makes the WRITE consistent but not the READ — stageAnnotation would still do two .value reads that a newly arrived frame can straddle. One value means one read. Named as a task-level invariant with its own test (a fake transport emitting two distinguishable frames asserts the frozen pair is one frame, not a mix). |
| Cmd+Enter is wired into handleShellShortcut and cannot actually reach canSend or the intent sink, so it either does nothing or bypasses the guard. | high | med | Caught in this refine pass against RectifyWindow.kt:134-141, which states the constraint outright. The fix is one hoisted nullable holder — see architecture.interfaces 'Cmd+Enter wiring'. A11Y-2 holds because the holder is derived from the same canSend(state) the button reads. |
| Retaining the live frame's bytes plus the frozen frame's bytes plus crop bitmaps grows the GUI process heap over long sessions. | low | med | Bounded by construction: exactly one live HeldFrame and at most one frozen HeldFrame are referenced at a time (~3 MB each at 1080x2400, ~6 MB total), both cleared with the frame on ResumeLive/StopMirroring/SelectPlatform. The live reference is to an array the transport already allocated per frame, so it adds no allocation — only one frame's worth of delayed GC. Crops are produced during the send and released after publish. |
| Adding rawDump to HierarchySnapshotModel makes the model carry tens of kilobytes of text that ends up compared by value in a hot path. | low | med | The snapshot already lives OUTSIDE reduced state on MirrorViewModel.frozenHierarchy for exactly this reason. Keep rawDump out of MirrorUiState; a review check in the task's DoD asserts no MirrorUiState field references it. |
| The obvious Compose clipboard API (ClipboardManager) is deprecated in this version (Compose Multiplatform 1.12.1) and its replacement is suspend-based, so a naive implementation bakes a moving API into the UI layer. | low | low | Named and closed with a concrete non-deprecated path: java.awt.Toolkit.getDefaultToolkit().systemClipboard.setContents(java.awt.datatransfer.StringSelection(sessionId), null) in a new internal compose-application/desktopMain/core/clipboard/SystemClipboard.kt, mirroring the existing core/chooser/AdbPathChooser.kt placement. This is exactly what CommandBlock's KDoc (CommandBlock.kt:36-39) prescribes. LocalClipboard.current.setClipEntry(...) was rejected because it is suspend and needs platform-specific ClipEntry construction. If the write misbehaves off the EDT on a non-macOS host, wrap the call in withContext(Dispatchers.Swing) as chooseAdbPath already does — same file-level precedent, no new idea. |
| The completion toast never dismisses. | med | low | Caught in this refine pass: StackedSnakbarHostState.showSnackbar defaults to StackedSnackbarDuration.Indefinite. The call site MUST pass an explicit finite duration; AnimationConfiguration.SNACKBAR is a token, not an automatic default. |
| A crash between writing the per-send image directory and moving latest.json into place leaves an orphan directory on disk. | low | low | Accepted and bounded: each successful publish prunes every sibling numeric directory that is not the one latest.json names, so at most one orphan survives until the next send. No lock file, no recovery path, no cleanup daemon. |
| MCP's protocolVersion string in the initialize reply may not match what the installed Claude Code expects. | med | med | Declared as a single named constant (MCP_PROTOCOL_VERSION) in the server, echoed in the initialize response, and called out in :mcp-application/CLAUDE.md as the first thing to check if the handshake fails. The manual demo is where this gets found. |
| Kotlin/Native release linker OOMs under parallel linkReleaseFramework tasks (bugs.json#PP-TD-8), which will bite this spec's cross-target gate. | high | low | Pre-existing and already characterised: use --max-workers=1 for the cross-target gate, as payload-preview did. |

### requirements

#### functional

| id | text |
| --- | --- |
| FR-16 | In the toolbar, show a staged-annotation counter, a 'Clear' action that removes all staged annotations, and a primary 'Resolve & Send' button that is disabled while zero annotations are staged or while a send is in progress. |
| FR-17 | On 'Resolve & Send', package the full frame screenshot, per-annotation crops, view-hierarchy XML dump, and generated prompt into a versioned envelope, and publish it so a locally running Claude Code agent can retrieve it by calling a single MCP tool (name/transport per survey Q2/Q3) against an in-repo stdio MCP server stub that this spec builds and hosts from the desktop app (survey Q1) — not a pre-existing external server, which this codebase does not contain. |
| FR-18 | During send, show a visible status sequence (Streaming -> Frozen -> Dumping hierarchy -> Sent) with colored dots, running asynchronously off the UI thread (AS-30), then confirm with a toast ('Sent N annotations to Claude Code · frame + hierarchy · <time>s') including an 'Open session' link. |
| FR-19 | After a successful send, resume live mirroring automatically and reset the staged-annotation counter to 0. |
| FR-56 | Rectify desktop hosts/spawns a local, stdio-transport MCP server process exposing exactly one retrieval tool (name per survey Q2) that returns the most recently sent payload envelope; this is new infrastructure this spec builds, not integration with a pre-existing server. |
| FR-57 | On 'Resolve & Send', serialize the payload envelope — frame screenshot, per-annotation crops, hierarchy XML, generated prompt, and the PAYLOAD_FORMAT_VERSION field (AS-28) — and publish it to the in-process store the MCP server's retrieval tool reads from. |
| FR-58 | The toast's 'Open session' action copies the current session identifier to the system clipboard (survey Q4 default), giving the user something to paste into their own Claude Code terminal/session picker without this repo needing to focus or launch an external application window. |

#### nonFunctional

| id | category | text |
| --- | --- | --- |
| NFR-3 | performance | Resolve & Send payload transmission to the local MCP server should typically complete in under ~2s on localhost for a typical batch. |
| NFR-4 | security | All mirror stream data, screenshots, hierarchy dumps, and payloads stay local to the machine; no network egress beyond localhost MCP communication — trivially satisfied by a stdio transport, which never opens a network socket at all. |
| NFR-12 | maintainability | The payload format (prompt + crops + hierarchy XML) is versioned so the receiving MCP server/Claude Code agent can evolve its parsing without breaking older sends. |
| NFR-15 | maintainability | The MCP server component ships as a convention-plugin-shaped module (library or application plugin, per build-logic/convention) hosted/spawned by :desktop-application's main(), introducing no bespoke Gradle and no new third-party dependency beyond the already-cataloged kotlinx-serialization unless the survey's transport answer requires one. PARTIALLY SUPERSEDED by decision 6's human gate: the no-new-dependency clause holds exactly (zero new catalog entries); the no-bespoke-Gradle clause is knowingly deviated from for :mcp-application, because no convention plugin models a plain-JVM module. DECLARED DEVIATION — recorded in constraints, in definitionOfReady and in this requirement text so Stage 5 validation meets it rather than files it as a defect. |

### acceptanceCriteria

| id | text | priority | verificationMethod |
| --- | --- | --- | --- |
| AC-17 | Given zero annotations are staged, then 'Resolve & Send' is disabled; given one or more are staged, it is enabled; 'Clear' removes all staged annotations and resets the counter to 0. | must | test |
| AC-18 | When the user clicks 'Resolve & Send', then the frame screenshot, per-annotation crops, hierarchy XML, and generated prompt are transmitted via the local MCP server (per survey Q1/Q3: an in-repo stdio MCP server stub spawned/hosted by the desktop app) such that a Claude Code session calling the retrieval tool (per survey Q2) receives the complete payload. | must | test |
| AC-19 | When sending, then the status sequence Streaming -> Frozen -> Dumping hierarchy -> Sent displays in order with colored dots, and on completion a toast shows the sent-count/time summary with an 'Open session' link. | must | demo |
| AC-20 | While a send is in progress, 'Resolve & Send' is disabled and only re-enables after the toast confirms or the send fails; on failure, staged annotations and the frozen frame are NOT cleared (AS-31), so the user can retry with the same batch. | must | test |
| AC-21 | After a successful send, then live mirroring resumes automatically and the staged-annotation counter resets to 0. | must | test |
| AC-32 | Given a staged annotation batch is sent, then the persisted payload envelope carries an explicit format/schema version field (serialized from F-6's existing but currently unserialized PAYLOAD_FORMAT_VERSION=1 const, per AS-28) that the receiving MCP server can read to select its parsing path. | should | inspection |
| AC-81 | Given the in-repo MCP server stub is running and a payload has been sent, when a client calls the retrieval tool a second time with no new send in between, then it returns the same last-sent payload (idempotent 'latest' semantics) rather than an error or an empty result. | must | test |

### scenarios

| id | title | type | steps |
| --- | --- | --- | --- |
| SC-7 | Resolve & Send happy path | happy | ["Stage three annotations on a frozen frame and click Resolve & Send (or press Cmd+Enter)", "The button goes busy and Clear is disabled; the status sequence animates Streaming -> Frozen -> Dumping hierarchy -> Sent", "The envelope and its PNGs are published to ~/.rectify/handoff/ with latest.json moved into place last", "A toast confirms 'Sent 3 annotations to Claude Code · frame + hierarchy · 1.4s' with an Open session action", "Live mirroring resumes and the staged counter reads 0"] |
| SC-25 | Send fails mid-flight and the user retries | error | ["Stage one or more annotations and click Resolve & Send", "The publish fails (slot directory unwritable, disk full, permission denied)", "The status sequence shows the failure variant; the frozen frame, its retained bytes and every staged annotation remain exactly as before (AS-31)", "Resolve & Send re-enables (AC-20) and Clear re-enables", "The user clicks Resolve & Send again and the retry succeeds with the same batch"] |
| SC-26 | Agent retrieves the same payload twice | happy | ["A payload has been published and the MCP server is running", "The agent calls get_latest_annotation and receives the envelope metadata, prompt, hierarchy and image references", "With no new send in between, the agent calls get_latest_annotation again", "It receives a byte-identical result, because the server re-reads the same slot rather than consuming it (AC-81)"] |
| SC-27 | Send while the stream has dropped | edge | ["A frozen frame with annotations exists and the device goes away mid-session (MirrorSession.Lost)", "The user clicks Resolve & Send", "The send is ALLOWED and succeeds: the payload is assembled entirely from already-captured data and needs no device", "The status line renders the failure variant's first step ('Stream dropped', Error) while the remaining three still complete", "On success, ResumeLive is NOT performed because there is no live stream to return to; the frame stays pinned and the counter stays as-is, and the toast still confirms"] |
| SC-28 | Agent calls the tool before any send | edge | ["The MCP server is spawned but ~/.rectify/handoff/latest.json does not exist", "The agent calls get_latest_annotation", "It receives an explicit 'No annotation has been sent yet.' text result with isError false — not a JSON-RPC error and not a fabricated stale payload", "The read loop keeps running"] |
| SC-29 | Malformed stdin line and unknown envelope version | error | ["A client writes a line that is not valid JSON", "The server replies with a JSON-RPC -32700 parse error and keeps reading — one bad frame never ends the session", "Separately, the slot is replaced with an envelope whose version exceeds the server's supported version", "get_latest_annotation returns a tool error naming BOTH version numbers rather than mis-parsing (AC-32's purpose)", "The server process stays alive in both cases; stdin EOF is the only thing that exits it, with code 0"] |
| SC-30 | Clear the batch, re-stage, then send (PP-TD-2) | edge | ["Stage annotations on a frozen frame and edit the prompt in the Payload tab, so promptOverride is set", "Click Clear — the annotation list empties but the frame stays pinned", "Stage new annotations on the SAME frame", "Click Resolve & Send", "The published envelope's prompt describes the NEW annotations, because ClearStaged now clears promptOverride too; before this fix it would have shipped the stale override to the agent"] |

### definitionOfReady

- preparation.json is human-approved with readyForPlan: true (done).
- The nine gate-settled decisions are reproduced in this plan unchanged and not reopened.
- The two preparation open questions (session-id composition, stale-slot policy) are closed in this plan.
- The two declared deviations from preparation wording (one handoff interface instead of two; Unsupported* actuals in presentation-feature-mirror instead of :data-handoff-api) have been seen and accepted at the Stage 2 gate.
- The PP-TD-2 scope addition has been accepted at the Stage 2 gate (survey Q2).
- F-6 and F-3 are merged on the branch this work starts from.
- A reviewer has acknowledged the declared NFR-15 bespoke-Gradle deviation for :mcp-application.
- An Android device or AVD is available for the manual end-to-end demo, since the hierarchy dump is Android-only.

### definitionOfDone

- ./gradlew :presentation-feature-mirror:build :data-handoff-api:build :mcp-application:build is green (cross-target; use --max-workers=1 per bugs.json#PP-TD-8). A green desktop suite alone does not count — commonTest compiles for every target and Kotlin/Native is stricter.
- ./gradlew :presentation-feature-mirror:desktopTest :data-handoff-api:desktopTest :mcp-application:test is green.
- Every new commonTest backtick name is plain-word camelCase with no comma and no colon (presentation-feature-mirror/CLAUDE.md).
- explicitApi() passes: every new public declaration has an explicit visibility modifier and an explicit return type.
- No new entry was added to gradle/libs.versions.toml.
- grep confirms :mcp-application has exactly one println/stdout write site, inside the frame writer; every diagnostic goes to System.err.
- grep confirms no MirrorUiState field references rawDump or any ByteArray/ImageBitmap.
- grep confirms stageAnnotation performs exactly ONE .value read to obtain the frozen bitmap+bytes pair (the HeldFrame invariant).
- The toast call site passes an explicit finite StackedSnackbarDuration — the host state's default is Indefinite.
- MANDATORY MANUAL DEMO (AC-18, AC-19): ./gradlew :mcp-application:installDist, add the documented entry to a real Claude Code MCP config, run the desktop app against a real Android device, stage annotations, send, and have a real Claude Code session call get_latest_annotation and receive the payload — then call it a second time and receive the same payload (AC-81). Record the transcript and the measured elapsed time in the execution report. This cannot be automated and is not optional.
- MANUAL MEASUREMENT (NFR-3, risk): a real three-annotation 1080x2400 send is timed end to end and the inline tool-result size is recorded against the 512 KB budget.
- MANUAL CHECK (AC-19 animation, live ⌘↩): both are demo-only; record them in the execution report rather than claiming test coverage.
- A @Preview pair (light and dark) exists for the send-status overlay and the toast, per compose-preview-convention.md.
- :mcp-application/CLAUDE.md, :data-handoff-api/CLAUDE.md, .mcp.json.example and the root CLAUDE.md module list are written/updated.
- The registry delta (new modules, new components, new feature entry — plus the MISSING measure-mode and payload-preview entries this refine pass found absent from registry/features.json) is reported to the orchestrator; this plan writes no registry file itself.

### successMetrics

- A real Claude Code session calling get_latest_annotation against the in-repo stdio server receives the complete, version-tagged payload for the most recent send (AC-18), and receives the identical result on a second call (AC-81).
- A typical three-annotation send completes in under ~2s measured end to end on the developer machine (NFR-3).
- Zero network sockets are opened by either process during a send — verifiable by inspection, since no socket API appears anywhere in the diff (NFR-4).
- A failed send never loses staged annotations or the frozen frame, proven by a ViewModel test and reproduced once by hand (AS-31, AC-20, SC-25).
- frame.png is byte-identical to the adb capture the user annotated, proven by the HeldFrame pairing test.
- Zero new version-catalog entries and zero new third-party dependencies (NFR-15's dependency clause).

### architecture

#### approach

Three seams, one genuinely new. (A) UI wiring onto already-shipped kit components with pure projections carrying every assertable rule. (B) A single-slot atomic file drop under ~/.rectify/handoff/ behind one AnnotationHandoff interface, owned by a new library module :data-handoff-api that holds the wire types both processes compile against. (C) A new plain-JVM :mcp-application whose main() speaks line-delimited JSON-RPC over stdin/stdout and exposes exactly one tool. Nothing in F-6 moves, nothing in F-6 is re-exported, and no new design-system component is created.

#### rationale

The crux is that Claude Code spawns the MCP server as its own OS process while Rectify is a separately launched process, so an in-memory Kotlin interface cannot bridge them. A single-slot atomic file drop makes AC-81's idempotent 'latest' a property of storage rather than code, makes the payload retrievable whether or not Rectify is running at tool-call time, and is the only option provable in the existing desktop-only test target. stdio makes NFR-4 literally true rather than argued, because no socket is ever opened. Threading the raw uiautomator dump through was chosen over re-serializing XML from parsed nodes because parseUiAutomatorDump keeps only the attributes the measuring code needs, so a re-synthesised document labelled hierarchy.xml would not be what the device emitted. Retaining the original adb PNG bytes was chosen over re-encoding the decoded bitmap because screencap already hands over PNG and the ViewModel merely throws it away.

#### alternativesConsidered

| title | whyRejected |
| --- | --- |
| AF_UNIX domain socket (JVM 21 UnixDomainSocketAddress), Rectify listens and the server connects per tool call | Couples liveness hard: if Rectify is closed the tool call fails instead of returning the last payload, which directly weakens AC-81. Also materially more code (listener coroutine in the GUI process, accept/close handling, a length-prefixed frame format because a socket has no message boundaries, socket-file cleanup on crash) and a second concurrency surface inside the Compose process, which today has exactly one. AnnotationHandoff is an interface precisely so this remains a drop-in later. |
| MCP server hosted in-process inside the Rectify JVM with a thin spawned stdio relay | Strictly dominated. It does not remove the cross-process problem, it adds a hop: Claude Code still spawns a process, so the relay must still reach the GUI process by socket or file — a superset of either alternative, with two protocol translations instead of one. Listed because 'just keep it in memory' is the first instinct and needs an explicit answer. |
| A second main class inside :desktop-application instead of a new module | Drags all of Compose Desktop and Skiko onto the classpath of a process whose entire job is reading stdin — a slow, heavy JVM start for every agent tool spawn. compose.desktop.application { mainClass } names exactly one main, so a second launcher needs its own startScripts/jar task anyway. It also contradicts that module's own CLAUDE.md ('the single required entry point and nothing else'). |
| Put the envelope in an existing module (data-core, domain-core) instead of creating :data-handoff-api | Re-checked in this refine pass against the ladder, because a whole new module deserves the same parsimony package-layout.md applies to a core/ directory. data-core/CLAUDE.md states 'No external dependencies allowed — this module is a leaf'; the envelope needs kotlinx-serialization. domain-core has zero dependencies by rule (domain-layer.md) and must not learn about handoff infrastructure. presentation-feature-mirror cannot host it because :mcp-application would then drag Compose, Koin and Orbit onto a stdin reader's classpath (the 1500 ms spawn budget is the whole reason the server is separate). And the reverse — hosting the types in :mcp-application — is impossible: a plain-JVM module cannot be consumed from a KMP commonMain source set, which is where EnvelopeAssembly lives. The module is load-bearing, not ceremonial. Its -api suffix carrying a desktopMain implementation is a naming mismatch, recorded as a reviewer note rather than paid for with a second module; the name itself comes from preparation and is not reopened. |
| Split :data-handoff-api into -api and -impl to match the repo's api/impl convention | Two Gradle modules for ~250 lines. The split exists elsewhere to keep client-agnostic contracts free of an engine (Ktor, Room); here there is one implementation, it is java.nio only, and it must be on the server's classpath anyway because the server IS the reader. |
| Re-serialize hierarchy XML from the parsed HierarchySnapshotModel.nodes at send time | Lossy by construction — parseUiAutomatorDump keeps only the attributes the measuring code needs, so resource-id, text and content-desc are gone from the payload the agent receives. It would ship a synthesised document labelled hierarchy.xml that is not what the device said, and grow a second XML serializer in a repo whose documented position is that it deliberately does not parse XML. |
| Ship the full frame plus crop rectangles and let the agent crop | Orchestrator-settled: AC-18 and FR-17 both name 'per-annotation crops' literally as a deliverable, not as something derivable by the agent. Reinterpreting them would be a scope change dressed as a simplification. |
| Inline base64 image bytes in the tool result | A 1080x2400 PNG is 1-3 MB and base64 inflates it ~33%, so a single JSON-RPC line could exceed 4 MB — the most likely integration failure, and it fails worst for exactly the payloads users care most about. |
| Apply the di convention plugin to :data-handoff-api and bind the handoff there | DiConventionPlugin exposes koin-core as api(), which would put Koin on :mcp-application's classpath for no benefit. The Koin binding instead lives in presentation-feature-mirror's own module — which already needs a new desktopMain source set for the crop encoder — so one source set carries both desktop-only providers and :data-handoff-api stays Koin-free. This is declared deviation 2 from preparation's wording. |
| Two interfaces (AnnotationHandoff + AnnotationHandoffReader) as preparation's scope line words it | One implementation, and the server simply never calls publish. A second interface buys a type-level guarantee nobody is at risk of violating, in exchange for a second file both processes compile against. Declared deviation 1; surfaced at the gate rather than taken silently. |
| skiko Image.makeFromBitmap(...).encodeToData() for crop PNGs | Kept as the fallback, not the first choice. ImageBitmap.toAwtImage() + BufferedImage.getSubimage() + ImageIO.write() is two JDK calls and gives cropping for free, whereas the skiko path needs an explicit sub-pixmap step. Both are already on the desktop classpath; neither is a new dependency. Revisit if ImageIO measures slow against the 120 ms per-crop budget. |
| Co-assign two separate StateFlows (currentFrame + currentFrameBytes) in one collect body for the frame pairing | Rejected in this refine pass as a near-miss. Co-assignment makes the WRITE consistent, but stageAnnotation then performs two independent .value reads (MirrorViewModel.kt:354 reads currentFrame.value today), and a frame arriving between them pairs an old bitmap with new bytes — frame.png would silently not be the image the rectangles address. One value on one flow makes the read atomic too. |
| A MirrorIntent.OpenSession + MirrorSideEffect.CopySessionId round trip for the toast action | Unnecessary indirection. MirrorSideEffect.SendComplete already carries the sessionId, so the host's snackbar action closure captures it and writes the clipboard directly. One side effect, zero new intents, and the ViewModel needs no 'last session' field. |

#### components

| name | kind | status | registryRef | responsibility |
| --- | --- | --- | --- | --- |
| :data-handoff-api | data | new |  | New Gradle module (library convention plugin + libs.plugins.kotlin.serialization, no di plugin). The wire contract both processes compile against. commonMain source/envelope/AnnotationEnvelope.kt and source/handoff/AnnotationHandoff.kt; desktopMain core/slot/FileSlotAnnotationHandoff.kt (internal) and source/handoff/FileSlotFactory.kt. Registered in settings.gradle.kts under the data group. |
| AnnotationEnvelope (+ EnvelopeDevice, EnvelopeAnnotation, HandoffImage) | data | new |  | commonMain, :data-handoff-api/src/commonMain/kotlin/data/handoff/api/source/envelope/AnnotationEnvelope.kt. @Serializable, public, with version: Int as the FIRST field (AC-32/NFR-12 in one line). Carries only primitives and nested @Serializable types — no Compose, no presentation types — so DeviceRect becomes four Ints. |
| AnnotationHandoff | data | new |  | commonMain, :data-handoff-api/.../source/handoff/AnnotationHandoff.kt. One public interface with three members: suspend publish(envelope, images), suspend latest(): AnnotationEnvelope?, and resolve(envelope, name): String returning an absolute file:// URI. Declared deviation 1 from preparation's two-interface wording. |
| FileSlotAnnotationHandoff | data | new |  | desktopMain, :data-handoff-api/src/desktopMain/kotlin/data/handoff/api/core/slot/FileSlotAnnotationHandoff.kt, internal. The only implementation. Writes images to <root>/<sentAtMs>/<name>, then latest.json.tmp + Files.move(ATOMIC_MOVE, REPLACE_EXISTING) to <root>/latest.json, then prunes every sibling numeric directory that latest.json does not name. Reads the slot whole on every latest() call — AC-81's idempotency is a property of the storage, not of code. resolve() rejects any name containing '/' or '..' before composing the URI, because the slot is a file on disk and the server must treat it as untrusted input. Creates <root> with owner-only POSIX permissions where the filesystem supports it. // ponytail: single slot, add a ring buffer only when F-9's history needs one. |
| fileSlotAnnotationHandoff() factory + defaultHandoffRoot() | data | new |  | desktopMain, :data-handoff-api/.../source/handoff/FileSlotFactory.kt. public fun fileSlotAnnotationHandoff(root: java.nio.file.Path = defaultHandoffRoot()): AnnotationHandoff. The root parameter is the test seam — desktopTest passes a temp dir and needs no home directory. defaultHandoffRoot() resolves System.getProperty("user.home")/.rectify/handoff. |
| UnsupportedAnnotationHandoff | data | new |  | commonMain of presentation-feature-mirror, core/unsupported/UnsupportedAnnotationHandoff.kt, internal object. Throws on publish, returns null from latest. Mirrors UnsupportedDeviceSources exactly so every target still links while the feature stays desktop-only. Declared deviation 2: it lives here, not in :data-handoff-api. |
| :mcp-application | logic | new |  | New plain-JVM Gradle module, the repo's SECOND process entry point. Depends on :data-handoff-api and kotlinx-serialization-json only — no Compose, no Koin, no Skiko. Registered in settings.gradle.kts. See architecture.interfaces for the exact build file. |
| McpDispatcher | logic | new |  | :mcp-application/src/main/kotlin/dev/rectify/io/mcp/core/rpc/McpDispatcher.kt, internal. A pure suspend fun handle(line: String): String? over an injected AnnotationHandoff — null means 'this was a notification, write nothing'. This purity is the whole testing story: no spawned process, no stdin, no stdout. |
| Main.kt (server entry) | logic | new |  | :mcp-application/src/main/kotlin/dev/rectify/io/mcp/source/entry/Main.kt. runBlocking { generateSequence(::readLine).forEach { dispatcher.handle(it)?.let(::writeFrame) } }, exit 0 on EOF. writeFrame is the ONLY function in the module that touches System.out; every log goes through a stderr helper. A stray println silently corrupts the protocol and the failure mode looks like anything but its cause. |
| presentation-feature-mirror/core/send/SendProjection.kt | logic | new |  | commonMain, internal, Compose-free. sendSteps(phase: SendPhase, session: MirrorSession, hierarchy: HierarchyStatus): List<StatusStep> and sendTrailing(phase, session): String?. HierarchyStatus is the shipped four-case enum (Absent, Dumping, Ready, Unavailable). This is where AC-19's ordering and the 'Stream dropped' failure variant are proved, because no Compose UI-test harness exists. |
| presentation-feature-mirror/core/send/SendGate.kt | logic | new |  | commonMain, internal. canSend(state: MirrorUiState): Boolean = state.frozen != null && state.stagedCount > 0 && !state.send.isInFlight. Deliberately does NOT test the session (SC-27). Read by the composable (to disable the button), by the reducer (to drop a second ResolveAndSend) and by the hoisted Cmd+Enter holder — one definition, three consumers, which is what stops the keyboard path doing what the pointer path cannot. It is pure over MirrorUiState and therefore cannot see frame bytes; the bytes-absent case is a handler-level guard, not a canSend clause. |
| presentation-feature-mirror/core/send/SendLabels.kt | logic | new |  | commonMain, internal. toastLabel(count: Int, elapsedMs: Long): String producing 'Sent 3 annotations to Claude Code · frame + hierarchy · 1.4s'. Machine text, unlocalised, matching footerLabel's precedent and reusing its ' · ' separator rule. Elapsed is floored at 0.1s and printed to one decimal — a toast claiming '0s' reads as a bug even when it is true. Singular/plural handled the way buildPrompt already does it. |
| presentation-feature-mirror/core/send/SessionId.kt | logic | new |  | commonMain, internal. mintSessionId(deviceId: String?, frameId: Int, sentAtMs: Long): String producing '<deviceId>-f<frameId>-<sentAtMs>'. deviceId null or blank degrades to 'unknown'. See architecture.interfaces for the full rule. |
| presentation-feature-mirror/core/send/EnvelopeAssembly.kt | logic | new |  | commonMain, internal. suspend assembleEnvelope(frozen, frameBytes, hierarchy, encodeCrop, sentAtMs, deviceId) -> Pair<AnnotationEnvelope, List<HandoffImage>>. Reuses F-6's buildPrompt (promptOverride ?: buildPrompt(...)) and F-6's cropWindow for the padded source geometry — specifically cropWindow(...).srcOffset and .srcSize, which are already in device-pixel space; dstSize/scale are preview-only and must not be used (bugs.json#PP-TD-1). Passes PAYLOAD_FORMAT_VERSION in as the version argument; the const stays internal to core/payload and is never moved or re-exported. |
| CropEncoder | logic | new |  | commonMain, presentation-feature-mirror/source/mirror/CropEncoder.kt. public fun interface CropEncoder { public suspend fun encode(frame: ImageBitmap, rect: DeviceRect): ByteArray? }. Injected into MirrorViewModel with a null-returning default so commonTest needs no real image — the decodeFrame precedent exactly. |
| AwtCropEncoder | logic | new |  | NEW desktopMain source set of presentation-feature-mirror: core/crop/AwtCropEncoder.kt, internal. frame.toAwtImage().getSubimage(clamped rect) -> ImageIO.write(png) on Dispatchers.IO, returning null rather than throwing. The rect is clamped to the frame bounds first; a DeviceRect can legitimately touch an edge. |
| provideHandoffSources() | logic | new |  | internal expect fun Module.provideHandoffSources() in presentation-feature-mirror/source/mirror/, with three actuals: desktopMain binds single<AnnotationHandoff> { fileSlotAnnotationHandoff() } and single<CropEncoder> { AwtCropEncoder() }; androidMain and iosMain bind UnsupportedAnnotationHandoff and a null-returning CropEncoder. A verbatim copy of data-device-impl's provideDeviceSources() shape. Note the repo's trap: a seam with a constructor default must be bound with single<T> { ... }, never singleOf. |
| MirrorViewModel send handler | logic | reuse |  | EXTENDED, not replaced. Handles MirrorIntent.ResolveAndSend: guards on canSend(state); guards separately on the frozen HeldFrame being present and reduces SendPhase.Failed with a Kermit warning if it is not (canSend cannot see bytes, and a silent no-op on an enabled button is worse than a visible failure); reduces SendPhase.Preparing, assembles the envelope off the main thread, reduces SendPhase.Publishing, calls handoff.publish, measures elapsed with the injected monotonic seam, then on success captures stagedCount BEFORE resetting, posts SendComplete, resumes live (skipped when the session is Lost, SC-27) and clears the batch (FR-19/AC-21); on failure reduces SendPhase.Failed and leaves frozen, its bytes and the staged list untouched (AS-31/AC-20/SC-25). |
| MirrorViewModel HeldFrame retention | logic | new |  | A private internal data class HeldFrame(val bitmap: ImageBitmap, val bytes: ByteArray) plus a private MutableStateFlow<HeldFrame?> for the live frame and one for the frozen frame. observeFrames' map stage must carry the captured MirrorFrameModel through (it currently discards it at MirrorViewModel.kt:219) so the collect body can build one HeldFrame. stageAnnotation reads the live holder ONCE and copies it to the frozen holder at the same moment it sets currentFrozenFrame. resumeLive/stopSession/selectPlatform clear it with the frame. The existing public frame: StateFlow<ImageBitmap?> keeps its type and is assigned from the same collect body; nothing pairs off it any more. Never enters MirrorUiState. |
| MirrorContract additions | logic | reuse |  | EXTENDED. public enum class SendPhase { Idle, Preparing, Publishing, Sent, Failed } with a public val isInFlight: Boolean getter; MirrorUiState.send: SendPhase = SendPhase.Idle; public data object MirrorIntent.ResolveAndSend; public data class MirrorSideEffect.SendComplete(count: Int, summary: String, sessionId: String). The side effect exists because only the host can show a toast and touch the clipboard — the same reasoning as the shipped MirrorSideEffect.LocateAdb. ResumeLive, Reconnect, ClearStaged, EditPrompt, canResumeLive and stagedCount are reused unchanged (all verified present in MirrorContract.kt). |
| MirrorViewModel.clearStaged (PP-TD-2 fix) | logic | reuse |  | EXTENDED by one line. MirrorViewModel.kt:536-544 currently copies frozen with annotations = emptyList() and selectedIndex = null, leaving promptOverride intact. Add promptOverride = null to that copy. Root-cause fix in the one reducer every Clear routes through, not a guard at the send site. |
| HierarchySnapshotModel.rawDump | data | reuse |  | EXTENDED. New field rawDump: String = "" on the existing domain-core model, defaulted so every construction site including Empty keeps compiling, populated by HierarchySnapshotMapper.toModel from the HierarchyDumpStdout.dump it already holds (HierarchySnapshotMapper.kt:52-62). Must not reach MirrorUiState. |
| StatusLine / StatusStep / StatusStepState | ui | reuse | components.json#status-line | Renders FR-18/AC-19 whole. VERIFIED: its own @Preview (StatusLine.kt:163-193) already draws 'Streaming › Frozen › Dumping hierarchy › Sent' with trailing "live" AND the 'Stream dropped' (Error) failure variant with trailing "failed". F-7 writes only the pure projection feeding it and mounts it as a top-aligned overlay inside MirrorStageContent. |
| AppButton | ui | reuse | components.json#app-button | VERIFIED: the stub sits at RectifyWindow.kt:304 with shortcut = "⌘↵", and AppButton.kt:168-181 already exposes enabled, busy and busyText, with busy implying non-interactive (isInteractive = enabled && !busy). AC-17's disabled-at-zero is one flag and AC-20's disabled-while-in-flight is the other. No second button is added. |
| CounterBadge | ui | reuse | components.json#counter-badge | Already mounted in the toolbar's stagedCounter slot (RectifyWindow.kt:282-292). CounterBadgeState.Sent already exists for the post-send moment, so FR-16's counter needs no new component and no new state. |
| StackedSnackbarHost / rememberStackedSnackbarHostState | ui | reuse | components.json#stacked-snackbar-host | Hosts the completion toast with its 'Open session' action via showSnackbar(title, description, actionTitle, action, duration). NEWLY MOUNTED at the RectifyWindow layer — the desktop root does not go through SafeContainer today. EXECUTOR TRAP, verified: the state type is spelled StackedSnakbarHostState (missing 'c') in source, and showSnackbar's duration defaults to StackedSnackbarDuration.Indefinite — pass an explicit finite duration or the toast never leaves. |
| ShellToolbar / RectifyWindowShell / handleShellShortcut | ui | reuse | components.json#shell-toolbar | Slots filled, not changed. handleShellShortcut gains a Cmd+Enter branch beside the existing Cmd+1/2/3 dispatch, taking a nullable send action — see architecture.interfaces 'Cmd+Enter wiring' for why it cannot read state directly. |
| MirrorStageContent | ui | reuse | components.json#mirror-stage-content | EXTENDED with a top-aligned StatusLine overlay shown while state.send != SendPhase.Idle. It already dispatches on MirrorStage and already hosts F-3's error surfaces, so this is the established overlay location rather than a parallel one. |
| MirrorErrorCard / MirrorDropBanner / FeedbackBar / Reconnect | ui | reuse | components.json#feedback-bar | F-3's shipped error surface. A failed send plugs in here — the status line's failure variant plus the existing banner — rather than growing a parallel set of affordances. |
| SystemClipboard.kt | ui | new |  | compose-application/src/desktopMain/kotlin/dev/rectify/io/core/clipboard/SystemClipboard.kt, internal. copyToClipboard(text: String) via java.awt.Toolkit.getDefaultToolkit().systemClipboard.setContents(StringSelection(text), null), wrapped in runCatching + a Kermit warning on a headless or denied clipboard. Mirrors the existing core/chooser/AdbPathChooser.kt placement exactly. This is the non-deprecated path CommandBlock.kt:36-39 prescribes. |

#### dataModel

| entity | fields |
| --- | --- |
| AnnotationEnvelope (@Serializable, :data-handoff-api/commonMain) | ["version: Int — FIRST field, from F-6's PAYLOAD_FORMAT_VERSION (AC-32)", "sessionId: String — '<deviceId>-f<frameId>-<sentAtMs>'", "sentAtMs: Long — wall-clock stamp, the stale-slot signal and the per-send directory name", "device: EnvelopeDevice", "prompt: String — promptOverride ?: buildPrompt(...)", "hierarchyXml: String — HierarchySnapshotModel.rawDump, empty when Absent/Unavailable/iOS", "hierarchyAvailable: Boolean — distinguishes 'no dump' from 'an empty dump'", "framePath: String — bare file name, 'frame.png'", "annotations: List<EnvelopeAnnotation>"] |
| EnvelopeDevice (@Serializable) | ["id: String — the adb serial, or 'unknown'", "label: String — FrozenFrame.deviceLabel, may be blank (bugs.json#PP-TD-9)", "widthPx: Int", "heightPx: Int"] |
| EnvelopeAnnotation (@Serializable) | ["index: Int — 1-based badge number", "left: Int", "top: Int", "right: Int", "bottom: Int", "comment: String — verbatim, including any ⌥ measurement line core/measure folded in", "cropPath: String? — bare file name 'crop-<index>.png', null when encoding failed for this annotation"] |
| HandoffImage (:data-handoff-api/commonMain, not serialized) | ["name: String — the bare file name the envelope references", "bytes: ByteArray — PNG bytes; transient, released after publish"] |
| HeldFrame (presentation-feature-mirror, private, NOT serialized and NOT in MirrorUiState) | ["bitmap: ImageBitmap — the decoded frame the user sees and annotates", "bytes: ByteArray — the original adb-captured PNG for that exact frame", "Invariant: these two are always constructed together from one MirrorFrameModel and read together in one .value read"] |
| On-disk slot layout | ["~/.rectify/handoff/ — root, created rwx------ where POSIX permissions are supported", "~/.rectify/handoff/latest.json — THE slot; written as latest.json.tmp then Files.move(ATOMIC_MOVE, REPLACE_EXISTING)", "~/.rectify/handoff/<sentAtMs>/frame.png", "~/.rectify/handoff/<sentAtMs>/crop-1.png … crop-N.png", "Images are written BEFORE latest.json moves into place, so a reader that sees latest.json sees a complete set", "Every sibling numeric directory that latest.json does not name is pruned after each successful publish"] |
| SendPhase (public enum, MirrorContract) | ["Idle — status overlay hidden", "Preparing — encoding crops, assembling the envelope", "Publishing — writing the slot", "Sent — terminal success, reset to Idle after the toast data is captured", "Failed — terminal failure; frame and batch preserved", "isInFlight: Boolean — Preparing or Publishing"] |

#### interfaces

| name | contract |
| --- | --- |
| AnnotationHandoff (Kotlin, :data-handoff-api/commonMain/source/handoff/) | public interface AnnotationHandoff {     /** Publishes [envelope] and its [images] as the new single slot, atomically. */     public suspend fun publish(envelope: AnnotationEnvelope, images: List<HandoffImage>)      /** The last published envelope, or null when nothing has been published. Re-reads on every call — AC-81. */     public suspend fun latest(): AnnotationEnvelope?      /** Absolute file:// URI for [name] within [envelope]'s send directory. Rejects '/' and '..'. */     public fun resolve(envelope: AnnotationEnvelope, name: String): String } |
| Cmd+Enter wiring (CORRECTED in refine — the candidate's version could not compile against the real scope) | PROBLEM, verbatim from RectifyWindow.kt:137-141: `onPreviewKeyEvent` is a Window *parameter* — it cannot see into the Window's content lambda. That is why `isPanelVisible = true` is passed as a literal today. Mirror state and the MirrorIntent sink both live INSIDE the content lambda (MirrorHost provides them), so `handleShellShortcut` cannot read canSend and cannot dispatch ResolveAndSend.  FIX — one hoisted nullable holder, declared beside `shellState` in the same composable that calls Window:      var sendAction by remember { mutableStateOf<(() -> Unit)?>(null) }      Window(         onPreviewKeyEvent = { event ->             handleShellShortcut(event, isPanelVisible = true, onTabSelect = shellState::selectTab,                                 onSend = sendAction)         },         ...     ) {         MirrorHost { state, frame, hierarchy, onIntent ->             val canSend = canSend(state)             // Keyed on canSend so the holder is assigned only when the predicate flips.             LaunchedEffect(canSend) {                 sendAction = if (canSend) { { onIntent(MirrorIntent.ResolveAndSend) } } else null             }             ...         }     }  handleShellShortcut consumes Cmd+Enter (returns true) ONLY when onSend != null, and invokes it; otherwise the event passes through, exactly the isPanelVisible precedent.  WHY ONE NULLABLE HOLDER AND NOT TWO (a boolean + a lambda): nullability already encodes the guard, so there is no way for the two to disagree. A11Y-2 holds because the holder is derived from the same canSend(state) the button's `enabled` reads, and the reducer guard behind it is the real backstop. The live key dispatch itself stays demo-only evidence (F-1 waived W-2 on the same grounds for Cmd+1/2/3); what IS tested is canSend and the reducer guard. |
| Frame/bytes pairing (CORRECTED in refine — write-consistency was not read-consistency) | TODAY (MirrorViewModel.kt:210-225):     .map { captured -> decodeFrame(captured.bytes) }      // the bytes die here     .collect { bitmap -> currentFrame.value = bitmap }  TODAY (MirrorViewModel.kt:353-356):     val held = currentFrame.value ?: return@intent        // one read, one value     if (freezing) currentFrozenFrame.value = held  FIX:     private data class HeldFrame(val bitmap: ImageBitmap, val bytes: ByteArray)     private val currentHeld = MutableStateFlow<HeldFrame?>(null)      .map { captured -> decodeFrame(captured.bytes)?.let { HeldFrame(it, captured.bytes) } }     .collect { held ->         currentHeld.value = held         currentFrame.value = held?.bitmap        // publication only; nothing pairs off this flow     }      // stageAnnotation — still exactly ONE .value read, so no frame can land mid-pair:     val held = currentHeld.value ?: return@intent     if (freezing) { currentFrozenFrame.value = held.bitmap; currentFrozenHeld.value = held }  WHY NOT two co-assigned flows (currentFrame + currentFrameBytes): co-assignment in one collect body makes the WRITE atomic but the READ is still two `.value` loads from two flows, and observeFrames runs on viewModelScope while stageAnnotation runs in an Orbit intent on the container's dispatcher. A frame landing between the two loads pairs an old bitmap with new bytes: frame.png would silently not be the image the annotation rectangles address. That is a correctness bug a perf review would never catch.  MEMORY, OWNED: at most two HeldFrames are referenced at a time — one live, one frozen — roughly 3 MB each at 1080x2400, so ~6 MB. The live reference is to an array ScreencapMirrorTransport already allocated per frame, so it adds no allocation, only one frame's worth of delayed GC. Crops are produced during the send and released after publish. frame.png is a BYTE COPY, never a re-encode (the decode/encode round trip is not bit-identical, is slower, and is desktopMain-only for the one artefact that must be pixel-faithful). Decision 9's injected CropEncoder seam covers per-annotation crops ONLY. |
| MirrorViewModel constructor seams (all four, all defaulted) | The existing constructor ends with `decodeFrame: suspend (ByteArray) -> ImageBitmap? = ::decodePng`. F-7 appends four parameters in the same style:      private val handoff: AnnotationHandoff,                       // REQUIRED — no default     private val encodeCrop: CropEncoder = CropEncoder { _, _ -> null },     private val nowMs: () -> Long = { Clock.System.now().toEpochMilliseconds() },     private val elapsed: TimeSource = TimeSource.Monotonic,  handoff is deliberately required: defaulting it to UnsupportedAnnotationHandoff would turn a missing Koin binding into a silent no-op at runtime. The other three default, so the only two call sites that must change are PresentationFeatureMirrorModule.kt:24-25 (already `viewModel { MirrorViewModel(...) }` with explicit get<T>(), which is exactly what defaults require — never singleOf/viewModelOf) and MirrorFakes.kt:271.  TWO CLOCKS, TWO JOBS, both injected: `elapsed` (Monotonic) drives the toast's <time>s because a wall clock can step backwards under NTP or sleep and print nonsense; `nowMs` (wall clock) stamps sentAtMs because that value must be comparable across two processes and across restarts. The `nowMs: () -> Long` shape copies ScreencapMirrorTransport's documented seam rather than inventing one. |
| mcp-application/build.gradle.kts (the exact bespoke Gradle, modelled on :desktop-application) | // No convention plugin models a plain-JVM module — all four are built around // KotlinMultiplatformExtension. :desktop-application set and documented this precedent; // this extends it by one. Declared, not discovered (NFR-15 deviation). plugins {     alias(libs.plugins.kotlinJvm)     alias(libs.plugins.kotlin.serialization)     application }  kotlin {     jvmToolchain(21) }  dependencies {     implementation(projects.dataHandoffApi)   // resolves the jvm("desktop") variant, as :desktop-application already proves     implementation(libs.kotlinx.serialization.json)     // kotlin.test is wired once in the Library/Feature convention plugins; this module has     // neither, so it must declare its own. Sanctioned exception, see testing.md.     testImplementation(kotlin("test")) }  application {     mainClass.set("dev.rectify.io.mcp.source.entry.MainKt")     applicationName = "rectify-mcp"   // installDist -> build/install/rectify-mcp/bin/rectify-mcp }  // settings.gradle.kts gains include(":mcp-application") beside :desktop-application, and // include(":data-handoff-api") in the // data group. // Zero new version-catalog entries: kotlinJvm, kotlin-serialization and // kotlinx-serialization-json all already exist and are already used. // explicitApi() is NOT applied here (no convention plugin) — keep declarations internal by default. |
| data-handoff-api/build.gradle.kts | plugins {     id("dev.rectify.io.convention.library")     alias(libs.plugins.kotlin.serialization) }  kotlin {     sourceSets {         commonMain.dependencies {             implementation(libs.kotlinx.serialization.json)         }     } }  // Deliberately NOT applying the di convention plugin: it exposes koin-core as api(), which // would land Koin on :mcp-application's classpath for no benefit. The Koin binding lives in // presentation-feature-mirror, which needs a desktopMain source set anyway. |
| MCP wire — tools/list entry | {   "name": "get_latest_annotation",   "description": "Returns the most recent annotation batch sent from Rectify: the generated prompt, the device's view-hierarchy XML, and file:// references to the full frame screenshot and each per-annotation crop.",   "inputSchema": { "type": "object", "properties": {}, "additionalProperties": false } } |
| MCP wire — get_latest_annotation success result (decision 8: metadata + prompt + hierarchy inline, images by reference) | result = {   "content": [ { "type": "text", "text": "<the JSON document below, pretty-printed>" } ],   "isError": false }  The JSON document: {   "version": 1,   "sessionId": "emulator-5554-f3-1760784512345",   "sentAtMs": 1760784512345,   "ageSeconds": 42,   "device": { "id": "emulator-5554", "label": "Android 15", "widthPx": 1080, "heightPx": 2400 },   "annotationCount": 3,   "prompt": "Fix 3 UI issues.\nAndroid 15 · 1080×2400\n…",   "hierarchy": { "available": true, "xml": "<hierarchy rotation=\"0\">…" },   "frame": { "ref": "file:///Users/me/.rectify/handoff/1760784512345/frame.png", "widthPx": 1080, "heightPx": 2400 },   "annotations": [     { "index": 1, "rect": { "left": 120, "top": 880, "right": 960, "bottom": 1040 },       "comment": "Button is 8dp too low",       "cropRef": "file:///Users/me/.rectify/handoff/1760784512345/crop-1.png" }   ] }  REFERENCE FORMAT: an absolute file:// URI, composed by the SERVER from its own handoff root plus envelope.sentAtMs plus the bare file name the envelope carries. The envelope stores bare names only, so a moved home directory does not invalidate a slot and the machine-specific part never enters the serialized bytes. A null cropPath is omitted from the annotation entry rather than emitted as null — a missing crop is a degraded payload, not a field the agent must branch on.  ageSeconds is derived at read time from sentAtMs. It is advisory: see the stale-slot decision. |
| MCP wire — the other three responses | initialize -> { "protocolVersion": MCP_PROTOCOL_VERSION, "capabilities": { "tools": {} },                 "serverInfo": { "name": "rectify", "version": "0.4" } } notifications/initialized -> handle() returns null; nothing is written (it is a notification). unknown method -> JSON-RPC error -32601 'Method not found: <name>'. unparseable line -> JSON-RPC error -32700 with id null; the loop continues. no slot yet -> content [{type:text, text:"No annotation has been sent yet."}], isError false. unknown envelope version -> isError true, text "Envelope format version <n> is newer than this   server supports (<m>). Rebuild :mcp-application (./gradlew :mcp-application:installDist)." corrupt/truncated slot -> isError true with the parse message; never throws out of the loop. stdin EOF -> exit 0 quietly. |
| Session identifier (CLOSED — preparation open question 3) | FORMAT: "<deviceId>-f<frameId>-<sentAtMs>"  e.g. "emulator-5554-f3-1760784512345"  MINTED IN: presentation-feature-mirror/core/send/SessionId.kt, mintSessionId(deviceId, frameId, sentAtMs), called exactly once per send from EnvelopeAssembly — at envelope-build time in the ViewModel's send handler, not at freeze and not in the UI.  INPUTS: MirrorUiState.selectedDeviceId (null or blank degrades to the literal "unknown"), FrozenFrame.frameId (already stamped once per freeze by the existing freezeCounter), and the send's wall-clock sentAtMs from the injected nowMs seam.  WHY: every part already exists, so nothing new has to be remembered or persisted; it is human-readable when pasted into a terminal; it is unique per send by sentAtMs and self-describing about which device and which freeze it came from; and it is the natural primary key for F-9's history. A random UUID was rejected for being opaque at exactly the moment the user pastes it; the slot filename was rejected because the slot name is implementation detail the agent must never learn.  It travels three ways from one mint: into AnnotationEnvelope.sessionId, into MirrorSideEffect.SendComplete (so the toast's Open session action copies it with no round trip), and into the Kermit log line for the send. |
| Stale-slot policy (CLOSED — preparation open question 4) | NO EXPIRY. A slot published days ago is still a valid get_latest_annotation answer, because AC-81's wording is unconditional and an expiry is a policy nobody asked for — it would turn a working retrieval into an error for a user who simply left the app open over a weekend.  INSTEAD: the envelope carries sentAtMs and the tool result reports both sentAtMs and a derived ageSeconds, so the agent can judge. The server never refuses on age, never deletes on age, and runs no timer. The only thing that removes a payload is the next successful publish replacing it.  This is also why sentAtMs uses a wall clock and not the monotonic source: it must be comparable across two processes and across restarts. |
| Claude Code MCP discovery (CLOSED, with one precise residual open question) | DECISION: F-7 DOCUMENTS, it does not generate. Shipped: a checked-in .mcp.json.example at the repo root plus a 'Wiring this into Claude Code' section in :mcp-application/CLAUDE.md.  .mcp.json.example contents: {   "mcpServers": {     "rectify": {       "command": "./mcp-application/build/install/rectify-mcp/bin/rectify-mcp",       "args": []     }   } } with a one-line note that ./gradlew :mcp-application:installDist must run first, and that the command may be made absolute if Claude Code is launched from elsewhere.  WHY NOT GENERATE: writing into a user's Claude Code configuration from inside an app is a trust-boundary action nobody asked for, and installDist must have run first anyway — a generated entry would frequently point at a binary that does not exist yet, which is a worse failure than no entry at all. An .example file is also diffable and reviewable, which a runtime write is not.  RESIDUAL OPEN QUESTION (precise): if Rectify ever ships a packaged distribution — compose.desktop { nativeDistributions { } } is NOT configured today (desktop-application/CLAUDE.md) — the launcher path becomes stable and absolute, and a first-run generator or a bundled server binary becomes defensible. That decision belongs to whichever spec first configures nativeDistributions. |

#### stateManagement

Orbit MVI, unchanged in shape. MirrorUiState gains exactly one field (send: SendPhase) — a value-equal enum, so a state copy stays cheap. Every byte-bearing thing stays OUT of reduced state on private StateFlows beside frame/frozenFrame/frozenHierarchy: the live and frozen HeldFrames are those, and rawDump rides on the existing frozenHierarchy snapshot. The in-flight guard is a derived canSend(state) read by the composable, the reducer and the hoisted Cmd+Enter holder; Orbit serialises intents through its container, so a second ResolveAndSend observes the in-flight phase and no-ops without a lock. Elapsed time and the wall-clock stamp are two separate injected seams. The ShellState side-panel holder stays a plain Compose state holder, deliberately not Orbit, as F-1 established.

#### navigation

- No navigation change. presentation-feature-mirror has no Destination and will not get one (its CLAUDE.md deviation 1). The five-step checklist in .claude/rules/navigation.md is deliberately not applied — stated here so a reviewer does not flag the omission.
- Cmd+Enter is added to handleShellShortcut in RectifyWindow beside the existing Cmd+1/2/3 dispatch, consumed only when the hoisted send action is non-null (see architecture.interfaces 'Cmd+Enter wiring').

#### affectedModules

- :data-handoff-api (NEW — library convention plugin + serialization)
- :mcp-application (NEW — plain JVM, bespoke build.gradle.kts, second process entry point)
- domain-core (HierarchySnapshotModel gains rawDump: String = "")
- data-repository-impl (HierarchySnapshotMapper.toModel populates rawDump)
- presentation-feature-mirror (core/send/ new; core/unsupported/ new; NEW desktopMain/androidMain/iosMain source sets; MirrorContract, MirrorViewModel incl. the clearStaged one-liner, MirrorContent, DI module extended; build.gradle.kts gains the :data-handoff-api dependency)
- compose-application (RectifyWindow wiring + the hoisted send-action holder, new desktopMain core/clipboard/SystemClipboard.kt)
- settings.gradle.kts (two includes)
- CLAUDE.md at repo root (module list), plus new :data-handoff-api/CLAUDE.md and :mcp-application/CLAUDE.md, plus .mcp.json.example

### projectContext

#### reuseComponents

| id | name | registryRef |
| --- | --- | --- |
| status-line | StatusLine + StatusStep + StatusStepState | components.json#status-line |
| app-button | AppButton (enabled / busy / busyText / shortcut) | components.json#app-button |
| counter-badge | CounterBadge (Empty/Staged/Sent) | components.json#counter-badge |
| stacked-snackbar-host | StackedSnackbarHost + rememberStackedSnackbarHostState (state type spelled StackedSnakbarHostState) | components.json#stacked-snackbar-host |
| feedback-bar | FeedbackBar (the send-failure surface) | components.json#feedback-bar |
| shell-toolbar | ShellToolbar (five fixed named slots) | components.json#shell-toolbar |
| rectify-window-shell | RectifyWindowShell | components.json#rectify-window-shell |
| mirror-stage-content | MirrorStageContent (gains the status overlay) | components.json#mirror-stage-content |
| mirror-host | MirrorHost (Koin/Orbit boundary, 4-arg content lambda — also where the Cmd+Enter holder is assigned) | components.json#mirror-host |
| mirror-payload-tab | MirrorPayloadTab (unchanged; the preview of what F-7 now actually sends) | components.json#mirror-payload-tab |
| command-block | CommandBlock — not rendered by F-7, but its KDoc is the normative host-owns-clipboard rule FR-58 follows | components.json#command-block |

#### reuseAssets

| id | name | registryRef |
| --- | --- | --- |
| font-jetbrains-mono | JetBrains Mono — the mono face StatusLine's step labels and the toast's machine segment already render in | assets.json#font-jetbrains-mono |
| strings-en | values/strings.xml — NOT extended by F-7. Status steps, the toast and the toolbar labels are machine text following footerLabel's precedent. | assets.json#strings-en |

#### reuseStyles

| id | name | registryRef |
| --- | --- | --- |
| theme-accessor | Theme.* — every colour, spacing, corner and motion value; no Color/dp/TextStyle literal at any new call site | styles.json#theme-accessor |
| theme-color | ThemeColor — sentFixed (Done), warnStillOff (Busy), annotate (Error/Live), ink3 (Pending); already wired inside StatusLine | styles.json#theme-color |
| animation-configuration | AnimationConfiguration.SNACKBAR = 5s — a token, NOT an automatic default; pass it explicitly to showSnackbar | styles.json#animation-configuration |
| theme-motion | Theme.motion.reducedMotion — resolved centrally by AppTheme; the status overlay must read it, never take a boolean parameter | styles.json#theme-motion |

#### existingFeatures

| id | name | registryRef |
| --- | --- | --- |
| payload-preview | payload-preview (F-6) — NO registry entry exists; registry/features.json stops at error-handling-and-recovery. Source of truth is the spec doc. | .pipeline/sdd/docs/payload-preview/plan/plan.data.json |
| measure-mode | measure-mode (F-5) — also absent from registry/features.json. Reported as a registry delta. | .pipeline/sdd/docs/measure-mode/plan/plan.data.json |
| error-handling-and-recovery | error-handling-and-recovery (F-3) | features.json#error-handling-and-recovery |
| drag-annotate-frozen-frame | drag-annotate-frozen-frame (F-4) | features.json#drag-annotate-frozen-frame |
| android-device-discovery-mirroring | android-device-discovery-mirroring (F-2) | features.json#android-device-discovery-mirroring |
| app-shell-window-layout | app-shell-window-layout (F-1) | features.json#app-shell-window-layout |
| error-propagation | Failure / Result propagation — publish failures surface as Failure.Technical.Platform | features.json#error-propagation |

#### newToBuild

- :data-handoff-api Gradle module (envelope + AnnotationHandoff + FileSlotAnnotationHandoff + factory)
- :mcp-application Gradle module (Main.kt + McpDispatcher + tool-result rendering)
- presentation-feature-mirror/core/send/ (SendProjection, SendGate, SendLabels, SessionId, EnvelopeAssembly)
- presentation-feature-mirror desktopMain/androidMain/iosMain source sets (AwtCropEncoder + provideHandoffSources actuals)
- CropEncoder fun interface + UnsupportedAnnotationHandoff
- MirrorContract additions: SendPhase, MirrorUiState.send, MirrorIntent.ResolveAndSend, MirrorSideEffect.SendComplete
- MirrorViewModel: HeldFrame retention + the send handler + four constructor seams + the clearStaged one-liner (PP-TD-2)
- HierarchySnapshotModel.rawDump + the mapper line that fills it
- compose-application/desktopMain core/clipboard/SystemClipboard.kt + the hoisted Cmd+Enter send-action holder
- .mcp.json.example, :mcp-application/CLAUDE.md, :data-handoff-api/CLAUDE.md

### resources

#### rules

| ref | why |
| --- | --- |
| CLAUDE.md#Module Structure | The root module list must gain :data-handoff-api and :mcp-application; it is already stale from three prior specs and this one must not add to that. |
| .claude/rules/platform-coverage.md | Android + iOS + Desktop-JVM only, no macosMain. Every desktop-only file goes in a desktopMain source set and every commonMain expect needs an androidMain and an iosMain actual, or targets stop linking. |
| .claude/rules/package-layout.md | core/ = internal only, source/ = public grouped per concern, and no empty core/ 'for consistency'. Decides exactly where the envelope, the interface and the slot writer land. |
| .claude/rules/convention-plugins.md | Names the four plugins and their KotlinMultiplatformExtension basis — the direct reason :mcp-application needs bespoke Gradle, and the reason :data-handoff-api must use the library plugin. |
| .claude/rules/testing.md | kotlin.test is wired once in the convention plugins and must not be added per-module (except :mcp-application, which has none); desktopTest is the runner; a commonTest backtick name with a comma or colon fails compileTestKotlinIosArm64; a green desktop suite does not prove cross-target compilation. |
| .claude/rules/koin-di.md | One Koin module per Gradle module, load-order data -> domain -> presentation, and the binding-style rule: a constructor with defaults must be bound with viewModel { } / single<T> { }, never viewModelOf/singleOf. |
| .claude/rules/orbit-mvi.md | The intent/reduce/postSideEffect shape the send handler must match, and why the toast and the clipboard write are side effects rather than reduced flags. |
| .claude/rules/error-handling.md | A publish failure must arrive as Failure.Technical.Platform rather than a swallowed exception; also documents the known ShowError gap that must not be extended. |
| .claude/rules/design-tokens.md | No Color/dp/TextStyle literal at any new call site; reducedMotion is read from Theme.motion, never passed as a parameter; atom -> molecule -> organism layering holds. |
| .claude/rules/compose-preview-convention.md | The send-status overlay and the toast each need a private, AppTheme-wrapped @Preview — which, with no UI-test harness, is part of AC-19's evidence. |
| .claude/rules/mappers.md | HierarchySnapshotMapper is a deliberate one-direction Mapper, not a ModelResourceMapper; adding rawDump must not be taken as an invitation to 'fix' its shape. |
| .claude/rules/domain-layer.md | domain-core has zero dependencies, so rawDump must be a plain String field and nothing in domain-core may learn about the handoff module. |
| .claude/rules/version-catalog-hygiene.md | A catalog entry existing does not mean anything consumes it — the reason the ktor-client-* entries are not a network layer and F-7 adds no entry of its own. |
| .claude/rules/navigation.md | Read and deliberately NOT applied: presentation-feature-mirror has no Destination by design. Recorded so the omission is not flagged as an oversight. |
| .claude/rules/localisation.md | Read; F-7 adds no string keys. The status/toast/toolbar strings are machine text following footerLabel's precedent, recorded as debt rather than fixed here. |
| presentation-feature-mirror/CLAUDE.md | Deviation 1 (MirrorHost, no Destination), deviation 2 (pixels never enter reduced state), plain-word camelCase test names, and the two Orbit test-environment facts in MirrorFakes.kt (configureDefaults for the event-loop dispatcher; runCurrent() before advanceUntilIdle()). |
| desktop-application/CLAUDE.md | The written precedent for a plain-JVM, non-convention-plugin module, and the note that nativeDistributions is not configured — which keeps the installDist launcher path the only stable one and shapes the .mcp.json decision. |
| data-core/CLAUDE.md | 'No external dependencies allowed — this module is a leaf.' The evidence that the serialized envelope cannot be folded into an existing data module, which is what justifies :data-handoff-api existing at all. |
| data-device-impl/CLAUDE.md | The provideDeviceSources()/Unsupported* shape to copy verbatim, the PNG-signature trim that makes frame.png a byte copy, and ProcessRunner's two transferable hazards for the server's read loop. |
| compose-application/CLAUDE.md | core/ vs source/ in the window module, the Koin registration-order rule, and the existing core/chooser placement that core/clipboard mirrors. |
| presentation-core-ui/CLAUDE.md | Kit components take copy as parameters and never read stringResource; machine output is rendered mono and carries no resource key. Governs how the status steps and toast text are passed in. |

#### skills


#### agents


#### docs

| ref | why |
| --- | --- |
| .pipeline/sdd/docs/payload-preview/plan/plan.data.json | F-6's own plan — the authority for AS-28 and for what the payload bundle already contains. |
| .pipeline/sdd/docs/registry/bugs.json | PP-TD-1, PP-TD-2, PP-TD-4, PP-TD-7, PP-TD-8 and PP-TD-9 all touch code this spec edits; see knownRisksFromQA for the per-entry verdict. |
| .pipeline/sdd/docs/resolve-and-send-mcp/inputs/design-rectify-ui-kit.html | The verbatim toast string, the status step labels and the failure variant, and the ⌘↩ affix already rendering on the stub button. |
| gradle/libs.versions.toml | Confirms kotlinJvm, kotlin-serialization and kotlinx-serialization-json already exist (so F-7 adds zero catalog entries), that composeMultiplatform is 1.12.1 (the version whose ClipboardManager is deprecated), and that there is no MCP SDK, no JSON-RPC library and no ktor-server-* entry. |

### errorStates

| state | behavior |
| --- | --- |
| empty | No payload has ever been published: get_latest_annotation returns content [{type:text, text:"No annotation has been sent yet."}] with isError false. Not a JSON-RPC error (nothing went wrong) and never a fabricated stale payload. |
| empty | Zero annotations staged: Resolve & Send is disabled, the Cmd+Enter holder is null so the key event passes through, and the reducer drops a ResolveAndSend anyway — three layers, one predicate. |
| loading | SendPhase.Preparing / Publishing: AppButton(busy = true, busyText = "Sending…") — busy already implies non-interactive, so one flag covers both affordance and guard. Clear is disabled for the same window, or the envelope and the counter would disagree about what was just sent. The StatusLine overlay shows Busy on the active step. |
| error | Publish failed (slot unwritable, disk full, permission denied): surfaces as Failure.Technical.Platform, reduces SendPhase.Failed, leaves the frozen frame, its HeldFrame bytes and the staged batch intact (AS-31), re-enables Resolve & Send and Clear (AC-20), and renders the StatusLine failure variant plus F-3's existing banner. No parallel error surface is introduced. |
| error | Crop encoding returns null for one annotation: the envelope ships the remaining crops plus the full frame, with that annotation's cropPath null. A missing crop degrades the payload; a failed send loses the batch's moment. |
| error | Frozen HeldFrame absent (possible only for a freeze that predates this retention within a running process, or a decode-without-bytes test path): the handler reduces SendPhase.Failed with a Kermit warning rather than returning silently. canSend is pure over MirrorUiState and cannot see bytes, so the button is enabled in this case — a visible failure beats a dead click. |
| empty | HierarchyStatus is Absent or Unavailable, or the platform is iOS (AS-29): the envelope carries hierarchyXml = "" and hierarchyAvailable = false, and the send proceeds. The 'Dumping hierarchy' step still renders because the design shows it unconditionally; it resolves to Pending rather than Done. |
| error | Server reads a slot written by a NEWER envelope version: reads version first and returns a tool error naming BOTH numbers plus the rebuild command, rather than mis-parsing into a plausible-looking wrong answer. This is precisely what AC-32 exists to enable. |
| error | Server reads a corrupt or truncated slot (should be impossible given ATOMIC_MOVE, but a disk can lie): returns a tool error with the parse message and never throws out of the read loop. |
| error | Malformed JSON-RPC line on stdin: respond with -32700 and keep reading. One bad frame must not end the session. Unknown method: -32601. |
| empty | stdin reaches EOF (the agent went away): the server exits 0 quietly, writing nothing further. |
| error | Clipboard write fails (headless session, OS denial): runCatching + a Kermit warning. The toast has already confirmed the send; a failed convenience copy must never read as a failed send. |
| error | Send while the stream has dropped (MirrorSession.Lost): ALLOWED and expected to succeed — the payload is assembled entirely from already-captured data and needs no device. The status line renders the 'Stream dropped' Error first step while the remaining three complete. On success, ResumeLive is skipped because there is nothing live to return to; the frame stays pinned and the toast still confirms (SC-27). This is also what closes F-3's deferred AC-29 clause. |

### observability

#### logs

- Kermit Logger.withTag("MirrorSend"): i at send start with annotation count and sessionId; i at success with elapsed ms and published byte total; w on publish failure with the Failure and the slot path attempted.
- Kermit Logger.withTag("MirrorSend"): w when crop encoding returns null for an annotation, naming the index — a silently missing crop is the kind of degradation that otherwise only shows up in the agent's answer.
- Kermit Logger.withTag("MirrorSend"): w when a send is attempted with no HeldFrame bytes (the SendPhase.Failed guard above).
- Kermit Logger.withTag("RectifyWindow"): w when the clipboard write fails.
- :mcp-application writes every diagnostic to System.err, tagged [rectify-mcp], with the method name and outcome per request. stdout carries protocol frames EXCLUSIVELY — a single stray println silently corrupts the protocol and the failure mode looks like anything but its cause.
- :mcp-application logs the resolved handoff root once at startup to stderr, which is the first thing to check when the agent gets 'No annotation has been sent yet.' after a successful send.

#### metrics

- Elapsed send duration, measured with the injected monotonic seam and rendered in the toast — the only user-visible timing, and the NFR-3 evidence.
- Published byte total (frame + crops + latest.json), logged per send, which is how the 512 KB inline budget and the heap claim get checked against reality rather than against the estimate heuristics in PayloadEstimate.kt.

#### analyticsEvents

- None. This repo has no analytics dependency, no event bus and no telemetry of any kind, and F-7 does not introduce one — stated explicitly rather than inventing event names a later spec would have to delete.

### accessibility

| id | requirement |
| --- | --- |
| A11Y-1 | Resolve & Send must be genuinely DISABLED, not merely dimmed, at zero staged annotations and while in flight — AppButton's isInteractive gate already drops the click handler, matching the SourceListRow precedent. A keyboard or screen-reader user must not be able to activate it. |
| A11Y-2 | Cmd+Enter must be refused on the same canSend predicate as the button, not on a separate check. The hoisted send-action holder is derived from canSend(state) and is null whenever the button is disabled, so the keyboard path cannot do what the pointer path cannot. |
| A11Y-3 | The StatusLine overlay is dots plus mono text; it needs a single contentDescription summarising the current phase (e.g. 'Sending: dumping hierarchy, step 3 of 4'), because colour alone must not carry the state. |
| A11Y-4 | StatusStepState.Error is conveyed by the label text ('Stream dropped') as well as by colour, which the shipped component already does — do not introduce a state distinguishable only by dot colour. |
| A11Y-5 | The toast's 'Open session' action must be reachable and activatable by keyboard for the full toast duration, and the toast must not steal focus from the stage. |
| A11Y-6 | Any animation in the status overlay respects Theme.motion.reducedMotion, read from the Theme and never taken as a component parameter. |
| A11Y-7 | The toast text must be announced on appearance — it is the only confirmation that the send succeeded, and a user who cannot see it has no other signal. |

### performanceBudget

| metric | target |
| --- | --- |
| End-to-end send, intent to SendComplete, 3 annotations at 1080x2400 (NFR-3) | <= 2000 ms |
| Crop PNG encode, per annotation (AwtCropEncoder, off the main thread) | <= 120 ms |
| Slot publish: write images + latest.json.tmp + ATOMIC_MOVE + prune | <= 300 ms |
| Inline portion of the get_latest_annotation result (metadata + prompt + hierarchy XML), the single-stdio-line risk | <= 512 KB; above it the XML is truncated with an explicit marker rather than failing the call |
| :mcp-application JVM start to first response (agent spawn latency) | <= 1500 ms — the whole reason the server's classpath is envelope + serialization and carries no Compose, Koin or Skiko |
| get_latest_annotation round trip on a warm process | <= 250 ms |
| Retained PNG bytes in the GUI heap | <= 2 HeldFrames (~6 MB at 1080x2400); zero accumulation across sends |
| Reduced-state cost of the feature | exactly one new MirrorUiState field, a value-equal enum; no ByteArray, ImageBitmap or rawDump may enter reduced state |
| UI responsiveness during a send (AS-30) | no frame skipped on the main thread — assembly, encoding and the file write all run off it |

### i18n

#### locales

- en

#### strings

- No new keys are added to presentation-core-localisation/src/commonMain/composeResources/values/strings.xml.
- Status step labels ('Streaming', 'Stream dropped', 'Frozen', 'Dumping hierarchy', 'Sent') and the trailing word ('live', 'failed') are machine text, rendered mono, unlocalised — StatusLine's own @Preview already ships them in this form.
- The toast string 'Sent N annotations to Claude Code · frame + hierarchy · <time>s' follows footerLabel's precedent: machine text with a literal ' · ' separator, pasted and screenshotted into bug reports, so it must read identically on every machine.
- 'Resolve & Send', 'Clear' and 'Sending…' remain hardcoded English in RectifyWindow, consistent with the lines already there at RectifyWindow.kt:294-305.

#### notes

Recorded as debt rather than fixed here: the toolbar labels are arguably prose that deserves a key, while the existing mirror_* keys show the project does localise some mirror copy. F-7 keeps the file it edits internally consistent instead of half-migrating it; a dedicated copy pass is the right owner, and it should also delete the two known-unused keys (mirror_status_ready, mirror_emulator_none).

### security

#### dataHandling

- NFR-4 holds literally, not by argument: the stdio transport never opens a socket, and no socket API appears anywhere in this diff. Verifiable by inspection.
- NEW at-rest data, named rather than glossed: device screenshots, per-annotation crops and the full view-hierarchy dump now land on disk under ~/.rectify/handoff/ unencrypted, where previously they existed only in memory. This is a real consequence of decision 5 and the single most important thing a reviewer should check.
- Mitigation: the handoff root is created with owner-only POSIX permissions (rwx------, files rw-------) on filesystems that support them, so another local user cannot read a colleague's device screenshots on a shared machine.
- Retention is bounded to exactly one send: every successful publish prunes every sibling per-send directory that latest.json does not name. There is no history, no ring buffer and no growth over time.
- Encryption at rest is explicitly out of scope and recorded as such, not silently omitted.
- No payload data is ever logged. Log lines carry counts, byte totals, elapsed times, the sessionId and paths — never prompt text, comments or hierarchy XML.

#### permissions

- No new OS permission is requested. File access is to the user's own home directory; clipboard access is a user-initiated action on a user-visible affordance.
- :mcp-application is spawned by Claude Code under the user's own account and inherits exactly those privileges; it opens no socket, spawns no child process and reads only ~/.rectify/handoff/.
- The existing NFR-5 guarantee is untouched: F-7 adds no adb/idb command and no new ProcessBuilder site. The repo-wide 'exactly one ProcessBuilder' audit must still print exactly one path after this spec.

#### privacy

- Everything stays on the machine. No network client, no telemetry, no analytics, no crash reporter is added.
- The session identifier is derived from a local device serial and a local timestamp and never leaves the machine; copying it to the clipboard is an explicit user action on a labelled affordance.
- TRUST BOUNDARY, enforced in code: the server treats latest.json as untrusted input even though Rectify writes it, because it is a file on disk any local process could replace. AnnotationHandoff.resolve rejects any name containing '/' or '..' before composing a file:// URI, and the server never follows a path outside its own handoff root. Validation lives in the one resolve() both sides route through, not duplicated at call sites.
- The envelope stores bare file names and no absolute path, so the serialized bytes contain no username or home-directory path; the machine-specific part is composed at read time by the server.

### rollout

#### featureFlags

- None. The repo has no feature-flag mechanism and this spec does not introduce one. The natural gate is already structural: the Resolve & Send button is disabled unless a frame is frozen and at least one annotation is staged, and the server is inert until a user adds the MCP entry and runs installDist.

#### migrations

- No database migration — data-database-* is still an unused scaffold and F-7 does not consume it.
- No preference migration. ~/.rectify/handoff/ is created on first publish; its absence is the normal first-run state and is handled, not an error.
- HierarchySnapshotModel.rawDump is a DEFAULTED field (= ""), which is what keeps every existing construction site — including HierarchySnapshotModel.Empty and every test fixture — compiling untouched.

#### backwardCompatibility

AnnotationEnvelope.version is the forward-compatibility mechanism and its first consumer is this spec's own server: it reads version before anything else and refuses an unknown one with a message naming both numbers and the rebuild command. The slot is single-writer, single-format, with no older format to read, so there is nothing to migrate in v1 — the machinery exists for v2. Every public API change is additive: a new defaulted field on HierarchySnapshotModel, a new defaulted MirrorUiState field, a new intent case, a new side-effect case. The one source-incompatible change is MirrorViewModel's constructor gaining a required AnnotationHandoff (the other three new seams — encodeCrop, nowMs, elapsed — all default), which has exactly two call sites: PresentationFeatureMirrorModule.kt:24-25 and MirrorFakes.kt:271. Both are in this repo and both are updated by this spec. One BEHAVIOUR change is deliberate and user-visible: ClearStaged now also clears promptOverride (PP-TD-2) — a user who cleared a batch and expected their typed prompt to survive will see it reset. That is the fix, not a regression.

### knownRisksFromQA

| bugRef | description | watchFor | mitigation |
| --- | --- | --- | --- |
| bugs.json#PP-TD-2 | ClearStaged keeps promptOverride, so a prompt authored against now-deleted annotations can resurface. VERIFIED IN THIS REFINE PASS against MirrorViewModel.kt:536-544, which copies frozen with annotations = emptyList() and selectedIndex = null and leaves promptOverride untouched. F-7's EnvelopeAssembly reads promptOverride ?: buildPrompt(...), so a stale override would be SENT TO THE AGENT — the first time this latent bug has a consequence outside the preview panel. | Clear all annotations, stage new ones on the same frame, send, and read the prompt in the published envelope. If it describes the old annotations, this bug has escalated from cosmetic to wrong-payload. | FIXED IN THIS SPEC: the one-line reducer change PP-TD-2 already prescribes (ClearStaged also clears promptOverride). Carried as its own implementation step (step 8), its own scenario (SC-30), its own regression test ('clearStagedAlsoClearsPromptOverride') and its own traceability row, so Stage 3 slices a task for it rather than burying it in the assembly step. |
| bugs.json#PP-TD-7 | ROOT CAUSE behind PP-TD-2: promptOverride carries no record of the payload it was authored against, so nothing can distinguish a stale override from a deliberate one. | Any temptation in EnvelopeAssembly to add a third staleness heuristic beside promptSeedIsStale. | Do NOT add one. The proper fix is a PromptOverride(text, seededAgainst) contract change, which needs its own task and gate. F-7 takes the PP-TD-2 one-liner and leaves the contract alone; EnvelopeAssembly sends exactly what the Payload tab previews, which is the invariant that matters. |
| bugs.json#PP-TD-4 | PayloadEstimateTest contains assertEquals(1, PAYLOAD_FORMAT_VERSION) — a tautology that asserts a literal against itself and would pass under any edit. The const was 'planned scope, rendered nowhere'; F-7 is the spec that renders it. | Shipping AC-32 with only that tautology as evidence. | Replace it with the behavioural assertion F-7 can now make: publish an envelope, read it back, assert the deserialized version field equals PAYLOAD_FORMAT_VERSION, and assert the raw JSON's first key is "version". That is AC-32's real evidence. |
| bugs.json#PP-TD-5 | A test name and comment claiming a structural property the body does not assert. Not a defect F-7 inherits, but the documented failure mode of this repo's test suite — two of payload-preview's three defects came from it. | F-7's new test names (sendSteps ordering, the HeldFrame pairing invariant, AC-81 idempotency) claiming more than their bodies assert. 'latestTwiceReturnsSameEnvelope' must compare the DESERIALIZED CONTENT, not two nulls. | Every new test name is checked against its body during review; the pairing test must emit two distinguishable frames or it asserts nothing. |
| bugs.json#PP-TD-6 | core/payload file names do not partition their contents (one label function in PayloadLabels.kt while two live in PayloadEstimate.kt; PayloadGate.kt holds three unrelated predicates). | The new core/send/ repeating it — e.g. dropping canSend into SendProjection.kt because it is 'send-ish'. | core/send/ is partitioned up front and by name: SendProjection (steps + trailing), SendGate (canSend), SendLabels (toastLabel), SessionId (mintSessionId), EnvelopeAssembly (assembleEnvelope). One concern per file, stated in the component list. |
| bugs.json#PP-TD-1 | Crop cells are fitted to a constant 84dp but drawn in weight(1f), clipping the outline at narrow panel widths, and cropWindow's KDoc about the pad is inaccurate. | Assuming cropWindow's dstSize/scale are export-ready. They are preview geometry and are wrong for export. | EnvelopeAssembly uses ONLY cropWindow(...).srcOffset and .srcSize, which are full-resolution device-pixel values and unaffected by the cell-width defect. Stated explicitly in the component responsibility so an executor does not reach for dstSize. The MirrorPayloadTab defect itself is NOT fixed here — it is a preview-layout bug with its own owner. |
| bugs.json#PP-TD-3 | HierarchyRow.matched is exactly tag != null at every call site and defaults false. | Nothing — F-7 renders no HierarchyRow and changes no hierarchy UI. | Out of scope, recorded so the registry walk is complete rather than selective. It cannot recur through this change. |
| bugs.json#PP-TD-9 | EmulatorModel.toRow sets no detail, unlike DeviceModel.toRow, so FrozenFrame.deviceLabel is blank on a first-class emulator path. | An empty label field in the published envelope and in the tool result, read as a bug in F-7. | Expected, not a defect of this spec: buildPrompt already drops a blank label together with its separator rather than inventing a device name, and EnvelopeDevice.label is allowed to be empty for the same reason. Cross-spec; belongs to android-device-discovery-mirroring and its own gate. |
| bugs.json#PP-TD-8 | Kotlin/Native release linker OOMs under roughly four or more parallel linkReleaseFramework tasks against a 4 GiB daemon heap. Pre-existing and repo-wide. | The cross-target build gate in step 14 failing with 'Java heap space' rather than a real compilation error. | Use --max-workers=1 for the cross-target gate, as payload-preview did. Do not chase it as a defect introduced by this spec. |
| features.json#error-handling-and-recovery | F-3 recorded that AC-29's 'Resolve & Send remains clickable' clause was unsatisfiable because the button is hard-disabled at all three call sites, and explicitly deferred it to F-6/F-7 territory. | F-7 wiring the button but leaving the dropped-stream case still disabled, which would leave AC-29 unsatisfied a second time. | SC-27 closes it: a send during MirrorSession.Lost is explicitly ALLOWED, because the payload is assembled from already-captured data and needs no device. canSend deliberately does not test the session, and a dedicated unit case asserts that. |
| features.json#error-propagation | Repo-wide: resultLauncher catches CancellationException, and every ShowError handler ignores its messageId and shows a hardcoded unlocalised 'Error'. | Modelling the send failure on that pattern, or wrapping the send in anything that swallows cancellation. | The send handler reports through SendPhase.Failed and the existing F-3 surfaces, not through a new ShowError. Do not extend the documented gap. ProcessRunner's withTimeoutOrNull reasoning applies to any timeout added to the publish path. |
| features.json#app-shell-window-layout | F-1 waived W-2: Cmd+1/2/3 live dispatch is unverifiable because only the pure shortcutToTab mapping is tested. | Claiming Cmd+Enter is 'tested' on the same basis — and, worse, assuming handleShellShortcut can read mirror state, which RectifyWindow.kt:137-141 says it cannot. | Test canSend and the reducer guard, which is what actually decides. State plainly that the live key dispatch is covered by the manual demo only. The scope problem is solved structurally by the hoisted send-action holder, not by a claim. |
| registry gap (no bug id) | registry/features.json has no entry for measure-mode (F-5) or payload-preview (F-6) even though both are merged, so any registryRef to them resolves to nothing. | A reviewer treating 'features.json#payload-preview' in a plan as a valid reference; this plan points at the spec docs instead. | Reported to the orchestrator as a registry delta for Stage 7, not patched by this plan (plans write no registry file). |

### implementationPlan

| step | title | dependsOn | linkedAC | estimate |
| --- | --- | --- | --- | --- |
| 1 | Retain the frozen frame's raw PNG bytes as a HeldFrame (one value, one read) in MirrorViewModel | [] | ["AC-18"] | S |
| 2 | Thread the raw uiautomator dump through: HierarchySnapshotModel.rawDump + HierarchySnapshotMapper | [] | ["AC-18"] | S |
| 3 | Create :data-handoff-api — envelope, AnnotationHandoff, FileSlotAnnotationHandoff, factory, settings.gradle.kts, CLAUDE.md | [] | ["AC-18", "AC-32", "AC-81"] | M |
| 4 | desktopTest the slot: publish/read/read-again identity, replacement + prune, absent slot, version round trip and first-key check, path-traversal rejection, no surviving .tmp | [3] | ["AC-32", "AC-81"] | S |
| 5 | Add presentation-feature-mirror/core/send/ pure projections: sendSteps/sendTrailing, canSend, toastLabel, mintSessionId — with their tests | [] | ["AC-17", "AC-19", "AC-20"] | M |
| 6 | Add CropEncoder + the new presentation-feature-mirror desktopMain/androidMain/iosMain source sets (AwtCropEncoder, Unsupported actuals, provideHandoffSources) | [3] | ["AC-18"] | M |
| 7 | Add core/send/EnvelopeAssembly consuming F-6's buildPrompt and cropWindow srcOffset/srcSize only | [1, 2, 3, 5, 6] | ["AC-18", "AC-32"] | M |
| 8 | Fix bugs.json#PP-TD-2: ClearStaged also clears promptOverride, with its regression test (SC-30) | [] | ["AC-18"] | S |
| 9 | Extend MirrorContract: SendPhase, MirrorUiState.send, MirrorIntent.ResolveAndSend, MirrorSideEffect.SendComplete | [5] | ["AC-17", "AC-19", "AC-20", "AC-21"] | S |
| 10 | Implement the MirrorViewModel send handler with the four constructor seams; wire Koin via viewModel { }; update MirrorFakes | [7, 9] | ["AC-17", "AC-18", "AC-20", "AC-21"] | L |
| 11 | Wire RectifyWindow and MirrorStageContent: live button, Clear guard, the hoisted Cmd+Enter send-action holder, StatusLine overlay, snackbar host with an explicit finite duration, AWT clipboard, @Preview pairs | [9, 10] | ["AC-17", "AC-19", "AC-20"] | M |
| 12 | Create :mcp-application — bespoke build.gradle.kts, Main.kt read loop, McpDispatcher, tool-result rendering, and its pure String->String tests | [3] | ["AC-18", "AC-81", "AC-32"] | L |
| 13 | Documentation: .mcp.json.example, :mcp-application/CLAUDE.md, :data-handoff-api/CLAUDE.md, root CLAUDE.md module list | [12] | ["AC-18"] | S |
| 14 | Cross-target build gate (--max-workers=1) and the mandatory manual end-to-end demo against a real Claude Code session | [4, 8, 10, 11, 12, 13] | ["AC-18", "AC-19", "AC-81"] | M |

### testStrategy

#### unit

- presentation-feature-mirror/commonTest — sendSteps: the four labels in order for each SendPhase; 'Streaming' Live before the freeze and Done after; 'Stream dropped' with StatusStepState.Error when session is Lost; 'Dumping hierarchy' Busy while HierarchyStatus.Dumping and Pending when Absent; 'Sent' Done only in SendPhase.Sent and Error in Failed. This is the ONLY automated evidence for AC-19's ordering, because no Compose UI-test harness exists.
- presentation-feature-mirror/commonTest — sendTrailing: 'live', 'failed', 'sent', null.
- presentation-feature-mirror/commonTest — canSend: false at zero staged, false with no frozen frame, false in Preparing and in Publishing, true with one or more staged and Idle, and true while the session is Lost (SC-27, which is also what closes F-3's deferred AC-29).
- presentation-feature-mirror/commonTest — toastLabel: singular/plural, the ' · ' separators verbatim, one decimal place, and the 0.1s floor (a sub-100ms send must not print '0s').
- presentation-feature-mirror/commonTest — mintSessionId: the exact format, the 'unknown' degradation for a null or blank deviceId, and distinctness across two sentAtMs values.
- presentation-feature-mirror/commonTest — EnvelopeAssembly: version equals PAYLOAD_FORMAT_VERSION; promptOverride wins over buildPrompt and buildPrompt is used when it is null; annotation count and rects survive; a null-returning CropEncoder yields cropPath null for that annotation and does NOT abort the send; hierarchyXml is empty with hierarchyAvailable false when the snapshot is Empty.
- presentation-feature-mirror/commonTest — the reducer guards: a second ResolveAndSend while in flight is dropped; a ResolveAndSend at zero staged is dropped (the Cmd+Enter path's backstop).
- presentation-feature-mirror/commonTest — clearStagedAlsoClearsPromptOverride (bugs.json#PP-TD-2, SC-30): set promptOverride, dispatch ClearStaged, assert frozen.promptOverride is null while the frame itself survives.
- presentation-feature-mirror/commonTest — the send handler against a fake AnnotationHandoff: success resumes live and resets the counter to 0 (AC-21) and reads stagedCount BEFORE the reset so the toast says '3' and not '0'; success with session Lost does NOT resume live (SC-27); failure leaves frozen, the frozen HeldFrame and the annotation list unchanged and re-enables the button (AS-31/AC-20/SC-25); a send with no HeldFrame reduces SendPhase.Failed rather than returning silently.
- presentation-feature-mirror/commonTest — the HeldFrame pairing invariant: a fake transport emitting two DISTINGUISHABLE frames (different bytes and different bitmaps), asserting the frozen bitmap and the frozen bytes come from ONE frame rather than a mix.
- data-repository-impl commonTest — HierarchySnapshotMapper.toModel populates rawDump with HierarchyDumpStdout.dump verbatim, including the adb trailer, and HierarchySnapshotModel.Empty still has rawDump == "".
- :mcp-application/src/test — McpDispatcher as a pure String -> String? function with a fake AnnotationHandoff: initialize shape and protocolVersion; notifications/initialized returns null; tools/list contains exactly one tool named get_latest_annotation with an empty object inputSchema; unknown method yields -32601; unparseable input yields -32700 with id null; every response is exactly one line with no embedded newline (the stdout-corruption guard).
- :mcp-application/src/test — get_latest_annotation: empty slot yields the explicit 'No annotation has been sent yet.' text with isError false; a populated slot yields the documented JSON with prompt, hierarchy and file:// refs; a second call with no publish in between yields a byte-identical result (AC-81); an envelope with a newer version yields isError true with BOTH numbers in the message (AC-32); a corrupt slot yields a tool error and the function still returns.

#### integration

- :data-handoff-api/desktopTest against a temp root (the factory's root parameter is the seam, so no home directory is touched) — publish then latest() returns an equal envelope; latest() twice returns equal envelopes with equal CONTENT, not two nulls (AC-81 at the storage layer); a second publish replaces it and the old per-send directory is pruned; an absent slot returns null rather than throwing; the version field survives the round trip and is the FIRST key in the serialized JSON (AC-32); image bytes land at <root>/<sentAtMs>/<name> and round-trip byte-identically; resolve() rejects names containing '/' and '..'; a latest.json containing garbage yields null-or-error rather than an exception escaping.
- :data-handoff-api/desktopTest — atomicity as far as a test can reach it: assert latest.json.tmp does not survive a successful publish, and that a latest.json present on disk always parses (the ATOMIC_MOVE contract). A true torn-write test would need process kill injection and is not attempted; stated rather than faked.
- :mcp-application/src/test — the dispatcher run IN-PROCESS against a real FileSlotAnnotationHandoff over a temp root that a test publish just wrote. This is the closest automatable approximation of AC-18: it proves everything except the actual process spawn and the actual agent.
- Cross-target compile gate: ./gradlew :presentation-feature-mirror:build :data-handoff-api:build :mcp-application:build (with --max-workers=1 per PP-TD-8). commonTest compiles for every target and Kotlin/Native is stricter than the JVM, so a green desktopTest proves nothing about iosArm64.

#### ui

- NONE AUTOMATED, and this is a pre-existing repo constraint rather than a choice: there is no runComposeUiTest and no ui-test-junit4 anywhere, and this host cannot load Skiko in-process (F-4 recorded the same three waivers). Nothing visual is asserted by any test in this repo.
- Substitute 1 — @Preview pairs (light and dark) for the send-status overlay and for the completion toast, per compose-preview-convention.md. StatusLine's own existing @Preview already renders both AC-19 variants verbatim, so the new previews only have to prove the projection drives it correctly.
- Substitute 2 — the pure sendSteps/canSend/toastLabel tests above carry every assertable rule out of the composables, the same move core/payload's hasPayload and hierarchyOutlineDisplay made.
- Substitute 3 — the manual demo below.

#### linkedScenarios

- SC-7
- SC-25
- SC-26
- SC-27
- SC-28
- SC-29
- SC-30

#### coverageGoal

Every pure function in core/send/ and every branch of McpDispatcher is covered by a direct unit test, and the file-slot round trip is covered end to end on the desktop target. WHAT CI CANNOT PROVE, stated plainly rather than papered over — five gaps, re-audited in this refine pass and confirmed to be genuinely unautomatable rather than convenient: (1) AC-18's literal claim — 'a Claude Code session calling the tool receives the complete payload' — needs a real external agent process this repo neither starts nor controls, so it is covered by an in-process dispatcher test against a real published slot PLUS a mandatory manual demo. (2) AC-81 is proved at the storage layer and at the dispatcher layer automatically; only its end-to-end form shares AC-18's limit. (3) AC-19's animated ORDERING-IN-TIME and the LIVE Cmd+Enter key dispatch have no automated coverage — the same honest position F-1 took for Cmd+1/2/3. (4) No real torn-write is injected; ATOMIC_MOVE is relied on as a platform guarantee. (5) NFR-3's ~2s is measured by hand once, not asserted in CI, because the only timing that matters includes real disk and real image sizes. WHAT THE MANUAL DEMO MUST NOT BE USED TO EXCUSE, tightened here: the step ORDER of sendSteps (pure, tested), the Cmd+Enter GUARD (the holder is null exactly when canSend is false — tested through canSend plus the reducer guard; only the key event itself is demo-only), the toast STRING (toastLabel, tested), AC-81 IDEMPOTENCY at both the storage and dispatcher layers (tested), AC-32's version refusal (tested), and the PP-TD-2 regression (tested). These five gaps are the verdict stage's input, not something to be discovered there.

### traceabilityMatrix

| ac | functional | components | tests |
| --- | --- | --- | --- |
| AC-17 | ["FR-16"] | ["AppButton (reuse)", "CounterBadge (reuse)", "ShellToolbar (reuse)", "core/send/SendGate.kt", "MirrorContract additions", "RectifyWindow wiring"] | ["canSend unit tests", "reducer zero-staged guard test", "SC-7"] |
| AC-18 | ["FR-17", "FR-56", "FR-57"] | ["MirrorViewModel HeldFrame retention", "HierarchySnapshotModel.rawDump", "core/send/EnvelopeAssembly.kt", "AwtCropEncoder", "AnnotationEnvelope", "FileSlotAnnotationHandoff", ":mcp-application", "McpDispatcher"] | ["EnvelopeAssembly unit tests", "HeldFrame pairing test", "slot round-trip desktopTest", "dispatcher-against-real-slot integration test", "SC-7", "SC-26", "MANUAL DEMO (required — not provable in CI)"] |
| AC-19 | ["FR-18"] | ["StatusLine (reuse)", "core/send/SendProjection.kt", "core/send/SendLabels.kt", "StackedSnackbarHost (reuse)", "MirrorStageContent overlay"] | ["sendSteps ordering tests", "sendTrailing tests", "toastLabel tests", "@Preview pairs", "SC-7", "MANUAL DEMO for the animation itself (no UI-test harness exists)"] |
| AC-20 | ["FR-16", "FR-18"] | ["AppButton busy (reuse)", "core/send/SendGate.kt", "SendPhase", "MirrorViewModel send handler", "FeedbackBar / MirrorDropBanner (reuse)"] | ["in-flight guard test", "failure-preserves-state test", "SC-25", "SC-27"] |
| AC-21 | ["FR-19"] | ["MirrorViewModel send handler (resumeLive reuse)", "CounterBadge (reuse)", "MirrorSideEffect.SendComplete"] | ["success-resumes-live-and-resets test", "count-captured-before-reset test", "lost-session-skips-resume test", "SC-7"] |
| AC-32 | ["FR-57"] | ["AnnotationEnvelope.version (first field)", "core/send/EnvelopeAssembly.kt", "McpDispatcher version check", "PAYLOAD_FORMAT_VERSION (reuse, stays internal to core/payload)"] | ["version round-trip desktopTest", "first-key-is-version JSON inspection test", "newer-version refusal test naming both numbers", "replaces the tautological bugs.json#PP-TD-4 assertion"] |
| AC-81 | ["FR-56", "FR-17"] | ["FileSlotAnnotationHandoff (re-reads per call)", "McpDispatcher", ":mcp-application"] | ["latest() twice returns equal envelopes by content", "dispatcher twice returns byte-identical results", "SC-26", "SC-28", "MANUAL DEMO for the end-to-end form"] |
| FR-16 | ["FR-16"] | ["ShellToolbar stagedCounter/clearAction/primaryAction slots (all reuse)", "MirrorIntent.ClearStaged (reuse)", "MirrorUiState.stagedCount (reuse)"] | ["canSend tests", "AC-17 tests", "SC-7"] |
| FR-17 | ["FR-17"] | ["EnvelopeAssembly", "AnnotationEnvelope", "AnnotationHandoff", ":mcp-application"] | ["AC-18 test set"] |
| FR-18 | ["FR-18"] | ["SendProjection", "SendLabels", "StatusLine (reuse)", "StackedSnackbarHost (reuse, explicit finite duration)"] | ["AC-19 test set", "AS-30 off-main-thread review check"] |
| FR-19 | ["FR-19"] | ["MirrorViewModel send handler", "resumeLive (reuse)"] | ["AC-21 test set"] |
| FR-56 | ["FR-56"] | [":mcp-application", "Main.kt stdio loop", "McpDispatcher", "tools/list with exactly one tool"] | ["dispatcher branch tests", "single-line-output test", "SC-28", "SC-29", "MANUAL DEMO"] |
| FR-57 | ["FR-57"] | ["EnvelopeAssembly", "AnnotationEnvelope.version", "FileSlotAnnotationHandoff.publish"] | ["AC-32 test set", "slot round-trip desktopTest"] |
| FR-58 | ["FR-58"] | ["core/send/SessionId.kt", "MirrorSideEffect.SendComplete(sessionId)", "compose-application/desktopMain core/clipboard/SystemClipboard.kt"] | ["mintSessionId format and degradation tests", "clipboard write is demo-only (an AWT system clipboard is not assertable in this harness) — stated, not claimed"] |
| NFR-3 | ["FR-17", "FR-18"] | ["AwtCropEncoder (off the main thread)", "FileSlotAnnotationHandoff", "frame bytes reused rather than re-encoded"] | ["performanceBudget row 1", "MANUAL MEASUREMENT in the demo — not asserted in CI"] |
| NFR-4 | ["FR-56", "FR-17"] | ["stdio transport (no socket API anywhere)", "FileSlotAnnotationHandoff owner-only permissions", "AnnotationHandoff.resolve path guard"] | ["inspection: grep the diff for socket/http/ktor — zero hits", "resolve() path-traversal rejection test", "security.dataHandling review"] |
| NFR-12 | ["FR-57"] | ["AnnotationEnvelope.version as the first field", "McpDispatcher version gate"] | ["AC-32 test set"] |
| NFR-15 | ["FR-56"] | [":data-handoff-api (library convention plugin)", ":mcp-application (DECLARED bespoke-Gradle deviation, per decision 6's human gate)"] | ["inspection: zero new gradle/libs.versions.toml entries (dependency clause satisfied exactly)", "inspection: the no-bespoke-Gradle clause is knowingly deviated from and is recorded in the NFR-15 requirement text, in constraints and in definitionOfReady for the validation stage"] |
| AS-28 | ["FR-57"] | ["PAYLOAD_FORMAT_VERSION (reuse, unmoved, still internal)", "EnvelopeAssembly passes it in"] | ["AC-32 test set; replaces bugs.json#PP-TD-4's tautology"] |
| AS-29 | ["FR-17", "FR-18"] | ["EnvelopeAssembly hierarchy branch", "HierarchyStatus (reuse)", "UnsupportedAnnotationHandoff on iOS"] | ["empty-hierarchy assembly test", "SC-7 (Android)", "errorStates row for Absent/Unavailable/iOS"] |
| AS-30 | ["FR-18"] | ["MirrorViewModel send handler (viewModelScope, off the main dispatcher)", "AwtCropEncoder on Dispatchers.IO"] | ["review check that no assembly, encode or file write runs on the main dispatcher", "performanceBudget UI-responsiveness row", "MANUAL DEMO"] |
| AS-31 | ["FR-16", "FR-19"] | ["SendPhase.Failed", "MirrorViewModel failure branch", "FeedbackBar / Reconnect (reuse)"] | ["failure-preserves-frame-and-batch test", "SC-25"] |
| AS-32 | ["FR-56"] | [":mcp-application as a bounded stub", "scope.out hardening list"] | ["inspection: the server exposes exactly one tool and no auth, discovery or multi-client machinery", "SC-29"] |
| SC-25 | ["FR-16", "FR-19"] | ["SendPhase.Failed", "MirrorViewModel failure branch", "AppButton re-enable", "FeedbackBar (reuse)"] | ["failure-then-retry ViewModel test", "errorStates publish-failure row"] |
| SC-27 / features.json#error-handling-and-recovery AC-29 clause | ["FR-16", "FR-17"] | ["core/send/SendGate.kt (canSend does not test the session)", "SendProjection 'Stream dropped' branch", "MirrorViewModel send handler (skips resumeLive when Lost)"] | ["canSend-true-while-lost test", "lost-session-skips-resume test", "sendSteps Lost branch test"] |
| bugs.json#PP-TD-2 | ["FR-17", "FR-57"] | ["MirrorViewModel.clearStaged (one-line reducer fix)", "core/send/EnvelopeAssembly.kt (the consumer that made it consequential)"] | ["clearStagedAlsoClearsPromptOverride regression test", "SC-30", "implementationPlan step 8"] |

### openQuestions

- RESIDUAL (precise, non-blocking): if Rectify ever configures compose.desktop { nativeDistributions { } } — it does not today — the :mcp-application launcher gets a stable absolute path and a first-run .mcp.json generator or a bundled server binary becomes defensible. That decision belongs to whichever spec first configures nativeDistributions, not to F-7.
- RESIDUAL (non-blocking): the long-term MCP hardening path — multi-client support, an auth/pairing handshake, full protocol compliance, tool discovery beyond the single retrieval tool, and a push-style wait_for_annotations — is explicitly out of v1 (AS-32) and should be filed as a follow-up once F-7 ships and the manual demo has shown which parts of the handshake the real client exercises. The AF_UNIX socket alternative is the natural first move the moment a push tool is specified, and AnnotationHandoff is an interface precisely so it is a drop-in.
- The exact MCP protocolVersion string the installed Claude Code negotiates is unknown until the manual demo runs. It ships as one named constant with a note in :mcp-application/CLAUDE.md; the demo is where it gets confirmed or corrected.
- Whether the toolbar/toast/status strings should eventually move into strings.xml is left open as recorded debt. A dedicated copy pass (which should also delete the two known-unused mirror_* keys) is the right owner.
- REGISTRY DELTA for Stage 7, not a decision: registry/features.json is missing entries for measure-mode (F-5) and payload-preview (F-6), both merged. F-7's own entry plus those two should be added when the registry is next written.
- CLOSED IN THIS PLAN, listed so nothing looks dodged: session-identifier composition; stale-slot policy; .mcp.json discovery; the frozen-frame raw-PNG-bytes gap (now a HeldFrame, with the read-atomicity hole the candidate left closed); the Cmd+Enter scope problem (hoisted send-action holder); the exact :mcp-application build file; and the PP-TD-2 scope call (in, with its own step, scenario, test and traceability row).

### proposedSurvey

| question | options | recommended | rationale |
| --- | --- | --- | --- |
| Sign-off — Approve this Plan for resolve-and-send-mcp (F-7) and unlock Stage 3 (Backlog)? The refine pass kept all nine gate-settled decisions intact and fixed two defects the draft would have carried into execution: Cmd+Enter could not actually reach canSend or the intent sink from the window-level onPreviewKeyEvent, and the frame bytes/bitmap pairing was write-consistent but not read-consistent. | ["Approve — proceed to Backlog (Recommended)", "Comment / revise — something in the plan needs correction first"] | Approve — proceed to Backlog (Recommended) | All nine settled decisions verified intact (stdio stub, get_latest_annotation, stdio transport, clipboard copy, single-slot ATOMIC_MOVE drop, :mcp-application plain-JVM, rawDump defaulted field, inline metadata + images by reference, injected crop-encoder seam). All 11 reuse claims were checked against source and hold: AppButton already exposes enabled/busy/busyText/shortcut with busy implying non-interactive; StatusLine's own @Preview already renders both AC-19 variants verbatim; CounterBadgeState.Sent, StackedSnackbarHost, MirrorContract's ClearStaged/ResumeLive/stagedCount and FrozenFrame.frameId/deviceLabel/promptOverride all exist as described. SC-26..29 collide with nothing (the highest id used elsewhere is SC-24). The clipboard path is concrete and non-deprecated against Compose Multiplatform 1.12.1, exactly as CommandBlock's KDoc prescribes. Traceability covers every required id. Three executor traps were added that the draft would have hit: the host-state type is spelled StackedSnakbarHostState, showSnackbar defaults to Indefinite, and two registry refs (features.json#payload-preview, #measure-mode) do not exist. |
| Scope call — fix bugs.json#PP-TD-2 (ClearStaged also clears promptOverride) inside F-7, as its own task? | ["Yes — fix it here; it is one reducer line and F-7 is where it becomes a wrong payload (Recommended)", "No — leave it to its own spec; keep F-7 strictly inside the approved preparation scope"] | Yes — fix it here; it is one reducer line and F-7 is where it becomes a wrong payload (Recommended) | Verified against source, not taken on the drafter's word: MirrorViewModel.kt:536-544 copies the frozen frame with annotations = emptyList() and selectedIndex = null and leaves promptOverride intact, and F-7's EnvelopeAssembly reads promptOverride ?: buildPrompt(...). So after Clear + re-stage + Send, the envelope ships a prompt describing deleted annotations — to the agent, not just to the preview panel. The fix is the one line PP-TD-2 itself prescribes, in the single reducer every Clear routes through, and it is cheaper than shipping a known-wrong prompt. It is nonetheless scope beyond the approved preparation, which is why it is a question and not an assumption. If you decline, drop implementation step 8, scenario SC-30, the clearStagedAlsoClearsPromptOverride test and the PP-TD-2 traceability row; nothing else in the plan depends on them. Note the deeper root cause (PP-TD-7, a PromptOverride(text, seededAgainst) contract) stays out either way. |
| Two declared deviations from preparation's wording — accept as planned, or hold to the letter of preparation? | ["Accept both — one AnnotationHandoff interface, and the Unsupported*/Koin actuals in presentation-feature-mirror (Recommended)", "Hold to preparation — split into AnnotationHandoff + AnnotationHandoffReader and put the Unsupported* actuals in :data-handoff-api"] | Accept both — one AnnotationHandoff interface, and the Unsupported*/Koin actuals in presentation-feature-mirror (Recommended) | Preparation is the authoritative artifact and wins on conflict, so these are surfaced rather than taken silently. (1) Preparation's scope line names 'AnnotationHandoff/AnnotationHandoffReader'; the plan ships one interface with three members, because there is exactly one implementation and the server simply never calls publish — a second interface buys a type-level guarantee nobody is at risk of violating, at the cost of a second file both processes compile against. (2) Preparation places the Android/iOS Unsupported* actuals in :data-handoff-api; the plan puts them in presentation-feature-mirror so :data-handoff-api stays Koin-free and the server's classpath stays envelope + serialization only — which is the whole reason the 1500 ms spawn budget is achievable. Same shape as data-device-impl's provideDeviceSources(), different module. Both are cosmetic-to-structural rather than behavioural; holding to the letter costs one extra interface file and puts Koin on the server's classpath. |

### readyForBacklog

true
