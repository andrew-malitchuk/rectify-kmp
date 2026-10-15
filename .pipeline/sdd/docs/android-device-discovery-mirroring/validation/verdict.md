# android-device-discovery-mirroring — verdict

## Envelope

| field | value |
| --- | --- |
| stage | validation |
| step | verdict |
| status | done |
| producedBy | sdd-build-validation-verdict |
| schemaVersion | 1 |
| createdAt | 2026-10-15T02:25:55Z |
| updatedAt | 2026-10-15T02:25:55Z |

## Gate

### type

human

### approvedBy

orchestrator-auto

### approved

true

### at

2026-10-15T02:25:55Z

## Provenance

### selfHash

6f2f0e1caf29e1d1f5e3b0c8c611807f08a754bda8eccaf2fd35c12a139bbacb

### inputs

| artifact | hash |
| --- | --- |
| validation/audit.json | ce4c7dcc475934eb062f5e12933bd15a2831ee45737db758a340a4d39a79ec36 |
| plan/plan.json | 685e70a97c538c5bf3ac6b5b50a06601c803863ee89f04bc6dfccfc721dd1d3e |

## Data

### verdict

complete-with-waivers

### summary

All 16 tasks done; 152 desktop tests plus the iosSimulatorArm64 runs green on the merged tree. The adversarial audit raised 20 findings (3 error, 6 warn, 11 info) and all three errors were FIXED before close rather than carried: AF-1 (the transport reported its host-side capture round-trip as latencyMs, which would have printed a fabricated number under a label the NFR-1 waiver was explicitly granted against), AF-2 (the iOS tool dot was a hardcoded constant while ToolKind.Idb and ToolResolver already shipped and were tested, and a comment claimed iOS was probed when it was not), and AF-3 (Key.Escape existed nowhere in the repo, making the picker a one-way door and leaving the plan's only accessibility row unimplemented). The audit also verified NFR-5 holds under four separate attempts to bypass it, that the retained-emulator-list fix is correct and tested, and that scope discipline is clean across all 106 changed files.

### tasks

| id | status |
| --- | --- |
| T-1 | done |
| T-2 | done |
| T-3 | done |
| T-4 | done |
| T-5 | done |
| T-6 | done |
| T-7 | done |
| T-8 | done |
| T-9 | done |
| T-10 | done |
| T-11 | done |
| T-12 | done |
| T-13 | done |
| T-14 | done |
| T-15 | done |
| T-16 | done |

### waivers

| id | ac | reason | acceptedBy |
| --- | --- | --- | --- |
| W-1 | NFR-1 | v1 ships screenshot polling behind the MirrorTransport interface and knowingly does not meet the ~150ms glass-to-glass target. No H.264 decoder exists anywhere on the classpath; building one is disproportionate to this deliverable; the staged interface makes the real transport (scrcpy-server + in-JVM decode) a drop-in replacement. Accepted at Stage 1 with option B so it is flagged for re-review BEFORE ship, not merely recorded. | orchestrator-auto |
| W-2 | AC-25 / AC-45 | HARDWARE GATE, genuinely unresolved. The capture rate was never measured and no number was invented. The attached handset 38031FDJH00JVF reports unauthorized so screencap returns 0 bytes, and the only installed AVD never booted in 12 minutes. MirrorTuning.CaptureFloorMs stays at an unmeasured 100ms with a 5-step harness written into its KDoc. AF-1's fix is what makes this waiver honest: latency is now reported as an explicit placeholder rather than a fabricated figure. | orchestrator-auto |
| W-3 | AC-43 and the visual ACs | No window was ever observed: this environment denies screen capture and the composition did not render under :desktop-application:run. Substituted evidence is stronger than a claim but is not sight - a headless Koin probe resolving all ten device bindings on production jars, and a headless discovery probe against REAL hardware that emitted Detecting -> Found(real serial, real AVD) -> NoDeviceFound(adb 1.0.41, attempts=3) with the 3-poll escalation working. One human :desktop-application:run is still owed. | orchestrator-auto |
| W-4 | T7-TAC3 | Satisfied vacuously and accepted by the audit. It asks for a commented reason at each withTimeoutOrNull call site; there are zero such sites in the domain layer because every child process is already bounded inside ProcessRunner and a session has no deadline. The two impls carry KDoc explaining why no timeout exists rather than adding a decorative one. | orchestrator-auto |
| W-5 | A11Y / comp fidelity | Two measured comp deltas routed to a design-system ticket rather than re-cut inside F-2: NoSignalStage ships the phone outline at StageSurface's 180x380dp because the task mandated porting that geometry while comp 02 measures roughly 274x578dp, and the CTA is 32dp against the comp's roughly 39dp because PageCta is the largest AppButtonSize tier that exists. | orchestrator-auto |
| W-6 | ProcessRunner coverage | ProcessRunner's real timeout, orphan-freedom and drain behaviour remain code-review-only; they need a spawned process to exercise. The capture-loop gap it originally flagged WAS closed - 10 tests via a fake DeviceCommandExecutor. | orchestrator-auto |

### openChangeRequests

| id | note |
| --- | --- |
| AF-4..AF-15 | 12 warn/info findings untouched by the CR, carried as follow-ups. Notably mirror_status_ready and mirror_emulator_none are unused keys, and root CLAUDE.md's Module Structure still omits the three new modules. |
| CARRY-F1 | Inherited from F-1 and still open: resultLauncher catches CancellationException (repo-wide), six module README inventory tables, the E-resize cursor needing expect/actual, and the missing onPop token. |

### changelogRef

validation/changelog.json
