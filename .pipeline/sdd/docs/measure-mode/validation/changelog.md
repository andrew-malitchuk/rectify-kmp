# measure-mode — changelog

Option-Key Measure Mode (F-5). Holding Option over a staged annotation on a
frozen frame snaps to the underlying view-hierarchy node, draws neighbour guides
with dp/px gap numerals, highlights one misalignment or undersized hit target,
and on release attaches the numbers to that annotation's note so the sent
payload carries measurements instead of prose estimates.

## What shipped

**A never-throwing uiautomator parser** (`data-device-api`) over three real
Pixel 8 dumps committed as fixtures. It cuts the non-XML trailer adb appends —
which carries AOSP's own misspelling, `UI hierchary dumped to: /dev/tty` —
tolerates a single-quoted XML declaration, normalises the inverted rects that a
vanilla Settings dump genuinely contains, stays UTF-8 safe, and resolves each
node's id through a four-step chain (`resource-id` → `text` → `content-desc` →
`class[n]`) because `resource-id` is 83 % populated on a View app and **6 % on
Compose**, where classes are anonymous too.

**A freeze-time hierarchy dump** (`DeviceCommand.UiAutomatorDump` with its own
10 s budget, `DeviceRepository.dumpHierarchy`, `DumpHierarchyUseCase`). Measured
at ~2.7 s on real hardware, which is why it runs once at freeze rather than on
key-down: NFR-2's ~100 ms budget is satisfiable only with the hierarchy already
in hand. It gave `DeviceCommand.WmDensity` and `parseWmDensity` their first
production callers after shipping as tested-but-uncalled scaffolding.

**The measure algorithm** (`presentation-feature-mirror/core/measure/`, internal
and Compose-free) — snap, neighbours, gaps, guide suppression, finding selection
and formatting, mirroring the design's own `measure(a, u)` and asserted against
its real 19-node `ELS` fixture as a true oracle.

**The UI** — a floating 228 dp `MeasureCard` at `TopStart` with an additive
`footer` parameter, target and neighbour `MeasureMark`s, the dashed mid-axis
guides, the alignment rail for the top-misalignment branch, `MeasureTag`
numerals, and a hybrid Alt detection path (pointer sampler + focus requester +
three independent end routes) because a missed key-up would otherwise strand the
panel on screen.

## Bugs found and fixed during the work

- **The hierarchy root always won `snapTarget`** — it contains every node and has
  the largest area, so every measure against a real dump would have snapped to
  the whole screen. Found by tracing real data; in neither plan nor backlog.
- **The misalignment thresholds were read in raw device pixels** while the
  touch-target bound was converted, and every test ran at 160 dpi where px and
  dp are identical, so nothing distinguished them. On the Pixel 8's 420 dpi a
  10 px bound is 3.8 dp — 2.6× stricter than designed — so the finding would
  essentially never have fired.
- **A measurement readout could outlive the annotation index it named**, because
  `DeleteAnnotation` renumbers survivors. Fixed in the shared path:
  `deleteAnnotation` and `clearStaged` now drop the readout in the same reduce.
- **The status line contradicted its own indicator** — the dot had a `Dumping`
  branch while the label beside it still read `FROZEN` for the whole ~2.7 s dump.
- **`MeasureCard`'s shipped `@Preview` carried a value its own algorithm cannot
  produce** (`Top 9dp` against the oracle's 15). Corrected, with the discrepancy
  recorded rather than reconciled away.

## Verification

732 test executions, 0 failures, across desktop **and** `iosSimulatorArm64` — so
the measure logic is proven to run, not merely compile, on Kotlin/Native. 107 of
those tests are the measure suite itself.

Four criteria remain **blocked, not waived**: everything whose verification is a
human looking at a rendered window. See `validation/verdict.md` W-1..W-3 and the
runnable instructions in `execution/MANUAL-CHECKS.md`.

## Known limits

Android only — iOS measure support is explicitly out of scope. A `FLAG_SECURE`
screen was never tested, and it is the input most likely to return an empty dump
in the field. The Measuring card can occlude an annotation in the frame's
top-left corner; fixed placement was chosen deliberately and the flip to TopEnd
is not authorised.
