# payload-preview — changelog

## Envelope

| field | value |
| --- | --- |
| stage | validation |
| step | changelog |
| status | done |
| producedBy | sdd-build-validation-verdict |
| schemaVersion | 1 |
| createdAt | 2026-10-18T09:01:13Z |
| updatedAt | 2026-10-18T09:01:37Z |

## Gate

### type

auto

### approvedBy

orchestrator

### approved

true

### at

2026-10-18T09:01:37Z

## Provenance

### selfHash

ee0758c42e2301fd1c8d5f1b801162433172019c985d2391c155ddf6da243dc2

### inputs

| artifact | hash |
| --- | --- |
| validation/verdict.json | 0babe0104948b1878b9f39467524aa8cacab7587fb3570be08222feb19c48211 |

## Data

### entries

| id | kind | summary | task | at | changeRequest |
| --- | --- | --- | --- | --- | --- |
| CL-1 | added | [presentation-feature-mirror] New Payload tab on the mirror side panel (Cmd+2): the generated prompt with its token estimate and an Edit toggle, a 3-column crop grid with baked annotation outlines, the hierarchy outline with match badges, and a pinned attachment/size footer. Degrades to an explicit no-hierarchy line when no usable dump exists, and shows only its empty-state copy until something is staged. | T-7 | stage-4 |  |
| CL-2 | added | [presentation-feature-mirror/core/payload] New internal package holding every pure decision the tab makes, so each is unit-testable outside a @Composable: PromptAssembly (buildPrompt), PayloadEstimate (token heuristic, MB and footer labels, PAYLOAD_FORMAT_VERSION), CropWindow (design-space crop transform), HierarchyOutline (node-row projection with badges), PayloadLabels (hierarchy source line), PayloadGate (hasPayload, hierarchyOutlineDisplay). | T-2 | stage-4 |  |
| CL-3 | changed | [presentation-feature-mirror] FrozenFrame gains three freeze-scoped fields (frameId, deviceLabel, promptOverride) and MirrorIntent gains EditPrompt. MirrorUiState is unchanged. The freeze counter increments in stageAnnotation's intent body, never inside reduce, whose lambda is CAS-retried and would otherwise burn two ids on a lost race. | T-1 | stage-4 |  |
| CL-4 | changed | [presentation-feature-mirror + compose-application] MirrorHost's content lambda widens to 4-arg so the cached hierarchy reaches the panel without entering reduced state — not reduced into MirrorUiState (a few-hundred-node snapshot would be copied on every unrelated reduction) and not via a second ViewModel resolution. RectifyWindow gains one Payload branch and one tabCounts entry; ShellTab, shortcutToTab, SidePanelFrame, SidebarTabs and MirrorNotesTab are byte-unchanged. | T-11 | stage-4 |  |
| CL-5 | changed | [presentation-core-ui] HierarchyRow gains an additive, defaulted matched: Boolean = false — matched nodes read at full ink, the rest recede to ink3. No existing call site needed editing to compile; StyleguideScreen's two tagged demo rows were updated to matched = true so the project's own component reference stays consistent with the contract. | T-6 | stage-4 |  |
| CL-6 | fixed | [presentation-feature-mirror/core/payload] Stage 5 CR-1: HierarchyOutline's `last` predicate was <= rather than <, marking a node followed by a sibling as last and leaving a half-row gap in the tree guides before every non-final sibling. Its test had asserted the bug with a comment rationalising it; both the predicate and the test now follow HierarchyRow's documented column-continuity contract. | T-5 | stage-5 | CR-1 |
| CL-7 | fixed | [presentation-feature-mirror] Stage 5 CR-2: a frame-size mismatch (the measured 9.76% uiautomator display-override gap) left a held snapshot with Unavailable status, and the Hierarchy block's rows-first branch order rendered hundreds of unbadged nodes under a 'no hierarchy' readout, violating AC-80. The decision now lives in hierarchyOutlineDisplay() with status tested before row count, unit-tested, plus a mismatch-state preview the original AC-80 pair could not reach. | T-9 | stage-5 | CR-2 |
| CL-8 | changed | [presentation-core-localisation] Three localised chrome strings for the Payload tab (empty state, Edit label, Edit content description). PROMPT/CROPS/HIERARCHY labels, the token readout and the prompt body stay machine text — they are pasted into Claude and into bug reports and must read identically on every machine. | T-7 | stage-4 |  |
| CL-9 | added | [tests] 56 new tests: PayloadEstimateTest 18, CropWindowTest 8, MirrorViewModelPayloadTest 8, HierarchyOutlineTest 6, PayloadLabelsTest 6, PayloadHierarchyDisplayTest 6, PromptAssemblyTest 4, PayloadGateTest 3. Full suite 383 -> 439, all green on desktop and on real iosSimulatorArm64. | T-13 | stage-4 |  |
