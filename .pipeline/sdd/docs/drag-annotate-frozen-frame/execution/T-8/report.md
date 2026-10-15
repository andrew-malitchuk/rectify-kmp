# drag-annotate-frozen-frame — T-8/report

## Envelope

| field | value |
| --- | --- |
| stage | execution |
| step | T-8/report |
| status | done |
| producedBy | sdd-build-business-implement |
| schemaVersion | 1 |
| createdAt | 2026-10-15T05:15:01Z |
| updatedAt | 2026-10-15T05:15:01Z |

## Gate

### type

auto

### approvedBy

sdd-build-business-implement

### approved

true

### at

2026-10-15T05:15:01Z

## Provenance

### selfHash

c6afe63b097f9f6927b5909fed9131b1d227968d99669f37e49e5b10b897612a

### inputs

| artifact | hash |
| --- | --- |
| backlog/backlog.json | ad34003d23112d2823248241d2b777a5132e8b7f4e93f70c07ddb7d25a6422f1 |

## Data

### taskId

T-8

### type

ui

### outcome

success

### touchedPaths

- presentation-core-ui/.../kit/organism/annotation/CommentPopover.kt
- presentation-feature-mirror/.../MirrorAnnotationStage.kt
- +core/annotation/PopoverKeys.kt
- +PopoverKeysTest.kt

### testRun

#### command

./gradlew :presentation-feature-mirror:desktopTest --rerun-tasks ; :compileTestKotlinIosArm64

#### passed

true

#### output

PopoverKeysTest 3 new tests; module 62, 0 failures.

### implementation

#### filesChanged

- presentation-core-ui/.../kit/organism/annotation/CommentPopover.kt
- presentation-feature-mirror/.../MirrorAnnotationStage.kt
- +core/annotation/PopoverKeys.kt
- +PopoverKeysTest.kt

#### summary

CommentPopover gains focusRequester and closeLabel, both defaulted. Pure tested closesPopover(isMeta, key) refuses every meta chord, so Cmd+1/2/3 and the reserved Cmd+Enter still reach the window. KEYBOARD DISJOINTNESS RE-VERIFIED with a third consumer present: handleShellShortcut only matches meta, and pickerKeyAction lives on DevicePickerPanel which is composed only in MirrorStage.Picker - branches of one when, never co-composed. B2'S WART FIXED: detectDragGestures moved onto a SIBLING below the popover in z-order, and the popover claims its whole plate for hit-testing. Compose stops hit-testing siblings beneath the first hit, which is the ONLY mechanism that can stop awaitFirstDown(requireUnconsumed=false) - consumption cannot, so the layer B2 considered would not have worked.

#### reusedFromRegistry


### troubleshooting


### needsHumanReview

#### required

true

#### reason

Needs one rendered pass. This host cannot load Skiko in-process (ImageBitmap(2,3) throws LibraryLoadException) and the repo has no Compose UI-test harness, so nothing visual is asserted by a test.

### linkedAC

- AC-8
- AC-50
- FR-8
- NFR-8
- SC-9
