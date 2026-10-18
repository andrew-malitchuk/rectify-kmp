# payload-preview — change-requests

## Envelope

| field | value |
| --- | --- |
| stage | validation |
| step | change-requests |
| status | done |
| producedBy | sdd-build-validation-request |
| schemaVersion | 1 |
| createdAt | 2026-10-17T20:46:49Z |
| updatedAt | 2026-10-17T20:46:49Z |

## Gate

### type

human

### approvedBy

user

### approved

true

### at

2026-10-17T20:46:49Z

## Provenance

### selfHash

bf5c6ed50302042a924c0d12a2ef11e78b41d44ddd69930cb98e525599ec40c2

### inputs

| artifact | hash |
| --- | --- |
| validation/audit.json | 1211aed62b561b3e47ca27f5e6d1b4b82ae7467b0ab83b0301994a324cab78ad |

## Data

### changeRequests

| id | target | source | status | iteration | linkedFindings | about | context |
| --- | --- | --- | --- | --- | --- | --- | --- |
| CR-1 | T-5 | human | open | 1 | ["AF-1"] | HierarchyOutline's `last` predicate marks a node followed by a sibling as last, breaking the tree guides (AC-15). | FILE: presentation-feature-mirror/src/commonMain/kotlin/presentation/feature/mirror/core/payload/HierarchyOutline.kt, line 75. CHANGE: `last = next == null \|\| next.depth <= node.depth` -> `last = next == null \|\| next.depth < node.depth`.  WHY (verified by the orchestrator against HierarchyRow's draw code, not just the audit's claim): a row at depth d draws ancestor columns 0..d-1 full height via `repeat(depth)`, plus its OWN branch in column d, full height when !last and only to midY when last. So column d must continue below this row if EITHER a child follows (next.depth > d, which draws column d as its ancestor guide) OR a following sibling follows (next.depth == d, which draws its own branch in column d). Only next.depth < d ends the column. The shipped `<=` therefore marks a node followed by a SIBLING as last: its guide stops at the elbow while the sibling's line starts at the next row's top, leaving a half-row gap before every non-final sibling. On a ~400-node dump the tree renders as disconnected dashes.  THE CONTRACT IS ALREADY DEMONSTRATED TWICE IN SHIPPED CODE: HierarchyRow's own preview (HierarchyRow.kt:158-161) and StyleguideScreen.kt:743-745 both give `last = true` to ONLY the final sibling; non-final siblings take the default false. HierarchyRow's @param last KDoc says the same in prose.  YOU MUST ALSO FIX THE TEST — it currently asserts the bug. HierarchyOutlineTest.kt:65-67 asserts item_a (depth 1, followed by sibling item_b at depth 1) has last = true, and its comment rationalises it as 'regardless of sibling order'. That comment is wrong per HierarchyRow's KDoc and per both shipped reference trees. The test restated the implementation instead of the component's contract, which is exactly why 251 green tests missed this. Correct the assertion to last = false for item_a, keep last = true for item_b, fix the misleading comment, and make sure the suite still covers the genuinely distinct case: a node that is the last of its siblings but HAS CHILDREN is NOT last (next.depth > d).  SCOPE: HierarchyOutline.kt and HierarchyOutlineTest.kt only. Do not touch HierarchyRow.kt - the component is correct. GATES: ./gradlew :presentation-feature-mirror:desktopTest :presentation-core-ui:desktopTest --rerun  (expect 251 still, 0 failures) then :presentation-feature-mirror:build :presentation-core-ui:build --max-workers=1 (serialized: the parallel build OOMs on the Kotlin/Native release linker, a known pre-existing infra issue - do not chase it). |
| CR-2 | T-9 | human | open | 1 | ["AF-2"] | A reachable state renders hundreds of unbadged hierarchy rows under a 'no hierarchy' readout, violating AC-80. | FILES: presentation-feature-mirror/src/commonMain/kotlin/presentation/feature/mirror/source/mirror/MirrorPayloadTab.kt (PayloadHierarchySection's `when`) and/or .../MirrorViewModel.kt (dumpHierarchyFor).  THE REACHABLE FAILING STATE (chain verified by the orchestrator against source): 1. dumpHierarchyFor publishes the FULL snapshot unconditionally: `currentFrozenHierarchy.value = snapshot ?: Empty`. 2. It THEN reduces hierarchyStatus = Unavailable whenever `projectMeasureNodes(snapshot, frozenSize).isEmpty()`. 3. projectMeasureNodes returns emptyList() on a FRAME-SIZE MISMATCH - the measured uiautomator display-override gap (9.76%, 984x2132 dump vs 1080x2400 panel) named in HierarchySnapshotModel's own KDoc. So this is a documented real device condition, not a hypothetical. 4. payloadHierarchyRows maps snapshot.nodes to rows regardless of status (it only early-returns when nodes.isEmpty()), and badges come from projectMeasureNodes which IS empty - so rows exist but are all unbadged/unmatched. 5. PayloadHierarchySection's `when` tests `rows.isNotEmpty()` FIRST, before the Absent/Unavailable branch. RESULT: a several-hundred-node unbadged ink3 tree renders underneath a readout reading 'no hierarchy'. AC-80 requires ZERO rows plus the explicit no-hierarchy line.  NOTE the AC-80 preview pair cannot catch this: it pins hierarchy = Empty, steering away from the only combination that fails. If you change the previews, do NOT remove that pair - it is AC-80's declared primary evidence - but consider ADDING a preview for the mismatch state (non-empty snapshot + Unavailable status) so the regression is visible.  EITHER FIX IS ACCEPTABLE, pick one and justify it: (a) Reorder PayloadHierarchySection's `when` so the Absent/Unavailable branch is tested BEFORE rows.isNotEmpty(). Smallest diff, keeps the snapshot available to anything else that wants it. (b) Stop publishing an unusable snapshot in dumpHierarchyFor - only set currentFrozenHierarchy when the projection is non-empty. Fixes it at the source so no consumer can make this mistake, but changes ViewModel behaviour, so check nothing else (measure mode) depends on the snapshot being present while Unavailable.  ALSO ADD A TEST. This is the second finding in a row where the Compose layer carried fake coverage; a unit test over the pure gating decision would have caught it. If (a), the gating predicate is extractable to core/payload/ the same way hasPayload was for AC-77 - that precedent is already in the tree and is the preferred shape.  GATES: ./gradlew :presentation-feature-mirror:desktopTest :presentation-core-ui:desktopTest --rerun then :presentation-feature-mirror:build :presentation-core-ui:build --max-workers=1 (serialized - the parallel build OOMs on the Kotlin/Native release linker, a known pre-existing infra issue, do not chase it). |

### humanRemarks

- Stage 5 gate decision 1: FIX both AF-1 and AF-2 via targeted CRs rather than waiving - both are genuine shipped-behaviour defects and AF-2 is an outright AC-80 violation.
- Stage 5 gate decision 2: WAIVE HV-1 (the real-device manual demo over SC-6/SC-20..24) and the preview-only UI halves of AC-13/14/15/72/78/79 plus AC-80, acceptedBy human. The QA pipeline (sdd-flow-launch-qa) covers these scenarios properly later. Verdict is therefore complete-with-waivers, not complete.
- Stage 5 gate decision 3: ACCEPT all five design divergences as shipped - T-12's two (prompt body monoData 11sp vs the figure's 10.5px; Crops 'from frame #N' tag ink3 vs the implied ink2) and AF-5's three plan.errorStates colour divergences. No 10.5sp tabular-mono token exists and monoMeta is owned by the footer, so matching the figure would mean adding a token for one composable.
- AF-3 (HierarchyRow's matched=false default flipped ink at 3 un-updated StyleguideScreen call sites), AF-4 (crop cells fitted to a constant 84dp but drawn in weight(1f), so the real cell is ~76dp at PanelMinWidth and ~149dp at PanelMaxWidth, and cropWindow's 'changes only on a different display' KDoc is false) and AF-6 (PAYLOAD_FORMAT_VERSION is dead with a tautological test) were NOT converted to CRs this iteration. They are recorded as tech debt for Stage 6 review.
