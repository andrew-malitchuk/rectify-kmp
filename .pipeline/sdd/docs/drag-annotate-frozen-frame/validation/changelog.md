# drag-annotate-frozen-frame — changelog

## Envelope

| field | value |
| --- | --- |
| stage | validation |
| step | changelog |
| status | done |
| producedBy | sdd-build-validation-verdict |
| schemaVersion | 1 |
| createdAt | 2026-10-15T05:40:52Z |
| updatedAt | 2026-10-15T05:40:52Z |

## Gate

### type

auto

### approvedBy

sdd-build-validation-verdict

### approved

true

### at

2026-10-15T05:40:52Z

## Provenance

### selfHash

07f9154783af3dbfaa8ee491401485edecfabe04acc9b9b89bb3f78bc262a362

### inputs

| artifact | hash |
| --- | --- |
| validation/verdict.json | b67aa2a7aa05de36010a41336c3343234dfeb0d5306f9d7cda26a585049e900e |

## Data

### spec

drag-annotate-frozen-frame

### entries

| type | summary |
| --- | --- |
| added | Drag-to-annotate on the live mirror: dashed outline, live device-pixel size label and ruler guides, with freeze-on-release pinning the exact frame held at that moment. |
| added | FrameGeometry, the single coordinate conversion across composable dp, host pixels and device pixels. Mutation-tested: dropping the density divide and smuggling density into the storage leg each produced real failures. |
| added | Frozen stage with a scrim that dims everything outside annotation boxes, editable CommentPopover on the selected mark, read-only popovers on the rest, and an in-frame right-edge gutter with leader lines when they crowd. |
| added | Notes tab filled with one AnnotationRow per staged annotation, plus the toolbar's stagedCounter and clearAction stubs wired and tabCounts forwarded - a parameter that existed on SidePanelFrame but was unreachable because the shell never passed it. |
| added | 37 new tests. Repo total 196 desktop, 0 failures. |
| fixed | AF-1: anyOverlap gated WHETHER read-only popovers existed rather than WHERE they go, so two uncrowded annotations hid the first comment once the second was selected while two crowded ones kept both visible - crowding made more content appear. Now a pure popoverLayout splits anchored from gutter and exactly one list is populated. |
| fixed | A text-selection drag inside the comment field could draw a stray mark. Fixed by moving detectDragGestures to a sibling and having the popover claim its plate for hit-testing - consumption could never have stopped awaitFirstDown(requireUnconsumed=false). |
| changed | SC-4's scenario text amended: stageAnnotation auto-selects each new annotation and the gutter excludes the selected one, so the steps as written could never show three gutter rows. The scenario was wrong, not the code. |

### regressionRunPassed

true
