# rectify — drag-annotate-frozen-frame

## Envelope

| field | value |
| --- | --- |
| stage | handoff |
| step | drag-annotate-frozen-frame |
| status | done |
| producedBy | prd.py handoff |
| schemaVersion | 1 |
| createdAt | 2026-10-11T16:12:17Z |
| updatedAt | 2026-10-11T16:12:17Z |
| dryRun | false |

## Gate

### type

auto

### approvedBy

prd.py

### approved

true

### at

2026-10-11T16:12:17Z

## Provenance

### inputs

| artifact | hash |
| --- | --- |
| prd.json | a7b4b0d1c39a04a00aec0acd67900ad85595779106bea6f82ae1265133169110 |

### selfHash

f8eb3fd130ed8590a707f7449ca5b4d1fb95b4aceba941a135d2c25da61fc3d7

## Data

### sourcePrd

rectify

### id

F-4

### slug

drag-annotate-frozen-frame

### title

Drag-to-Annotate & Frozen Frame

### goalStatement

The core capture interaction: click-and-drag on the live mirror renders a dashed red outline with a live device-pixel size label and ruler/tick guides; release freezes the exact current frame and stages a numbered annotation anchored to the box. On the frozen frame everything outside annotation boxes dims and each annotation gets a popover (numbered badge, autofocused 'What's wrong here?' comment field, delete control, size/position meta line) with Enter to save and delete to remove and decrement the staged count. When popovers would crowd or overlap, they relocate to a gutter/sidebar panel linked to their boxes by leader lines. Why: this is the interaction that replaces 'screenshot plus prose description' and is the single most differentiating surface of the product.

### rawDescription

The core capture interaction: click-and-drag on the live mirror renders a dashed red outline with a live device-pixel size label and ruler/tick guides; release freezes the exact current frame and stages a numbered annotation anchored to the box. On the frozen frame everything outside annotation boxes dims and each annotation gets a popover (numbered badge, autofocused 'What's wrong here?' comment field, delete control, size/position meta line) with Enter to save and delete to remove and decrement the staged count. When popovers would crowd or overlap, they relocate to a gutter/sidebar panel linked to their boxes by leader lines. Why: this is the interaction that replaces 'screenshot plus prose description' and is the single most differentiating surface of the product.

### priority

must

### phase

PH-1

### estimate

L

### dependsOn

- F-1
- F-2

### linkedGoals

- GOAL-1
- GOAL-3

### functionalRequirements

| id | text | priority |
| --- | --- | --- |
| FR-6 | Support click-and-drag on the live mirror to draw a selection box, rendering a dashed red outline, a live device-pixel size label, and ruler/tick coordinate guides during the drag. | must |
| FR-7 | On drag release, freeze the exact current frame and create a new staged annotation anchored to the drawn box. | must |
| FR-8 | On a frozen frame, dim everything outside annotation boxes and show a per-annotation popover with a numbered badge, an autofocused free-text comment field (placeholder 'What's wrong here?'), a delete control, and a size/position meta line; Enter saves, clicking the delete control removes the annotation. | must |
| FR-9 | When annotation popovers would crowd/overlap, relocate them to a gutter/sidebar panel connected to their boxes via leader lines. | should |

### nonFunctionalRequirements

| id | category | text |
| --- | --- | --- |
| NFR-1 | performance | Live mirror glass-to-glass latency should stay low enough (target under ~150ms) that drag-to-select and freeze feel responsive. |
| NFR-8 | a11y | Device picker, annotation popovers, and error-card actions are fully keyboard-navigable (arrow keys/Enter for pickers, Enter/Escape for popovers). |
| NFR-9 | ux | All screens support full light and dark theme parity using the defined signal-color system (red = bug, green = fixed, orange = warning, blue = measurement) with sufficient contrast in both themes. |

### acceptanceCriteria

| id | text | priority | verificationMethod |
| --- | --- | --- | --- |
| AC-5 | Given live mirror is streaming, when the user drags on the mirror, then a dashed red outline, live pixel-size label, and ruler guides render in real time. | must | test |
| AC-6 | When the user releases the drag, then the exact frame is frozen and a new staged annotation is created anchored to the drawn box. | must | test |
| AC-7 | Given a frozen frame with one annotation, then everything outside the box dims and a popover with numbered badge, autofocused comment field, delete control, and meta line appears. | must | test |
| AC-8 | Given an annotation popover is focused, when the user presses Enter, then the comment saves; when the user clicks the delete control, then the annotation is removed and the staged count decrements. | must | test |
| AC-9 | Given three annotations are drawn close together, when their popovers would overlap, then they relocate to a sidebar/gutter panel connected via leader lines. | should | demo |

### scenarios

| id | title | type | steps |
| --- | --- | --- | --- |
| SC-3 | Draw and freeze a single annotation | happy | ["Drag on the live mirror to draw a box", "Release to freeze the exact frame", "Popover appears autofocused", "Type a comment and press Enter to save"] |
| SC-4 | Multiple crowded annotations relocate to gutter | edge | ["Draw three annotation boxes close together", "Popovers would overlap", "Popovers relocate to sidebar gutter with leader lines"] |

### userStories

| id | asA | iWant | soThat | linkedFR | acceptanceCriteria | scenarios | priority |
| --- | --- | --- | --- | --- | --- | --- | --- |
| US-3 | Mobile Developer (PER-1) | to drag a box directly on the live mirror and have the exact frame freeze on release | the bug is captured at the precise moment and region I saw it | ["FR-6", "FR-7"] | ["AC-5", "AC-6"] | ["SC-3"] | must |
| US-4 | QA Tester (PER-2) | an autofocused popover per box where I can type what's wrong, press Enter, or delete the box | I can flag several bugs in one pass without breaking flow | ["FR-8", "FR-9"] | ["AC-7", "AC-8", "AC-9"] | ["SC-3", "SC-4"] | must |

### sddReady

true
