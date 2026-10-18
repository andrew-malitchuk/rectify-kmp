# T-2 — Operator go/no-go gate on the T-1 spike

Recorded 2026-10-17 by the orchestrator at the Stage 4 wave-2 pause, from the
operator's answers at the gate. Evidence: `execution/T-1/spike-note.md` and
`execution/T-1/report.md` (task T-1, outcome PASS).

## Decision: **GO — continue as planned**

The operator chose **continue as planned**. The two descope exits the task was
required to present were NOT taken:

| candidate exit | taken? |
|---|---|
| Continue as planned (freeze-time `exec-out uiautomator dump /dev/tty`) | **YES** |
| Add a `/sdcard` dump + `cat`/`pull` fallback command | no |
| Descope FR-36..FR-40, ship the FR-41 fallback only | no |
| Stop the spec | no |

### Why the spike supports GO

- **The command works on real hardware.** Three dumps from a Pixel 8 (`shiba`,
  API 36) came back non-empty, complete and bounds-correct. No `FLAG_SECURE`
  screen was attempted, so that remains unmeasured.
- **The timing assumption is confirmed, not merely assumed.** 2317 / 2711 /
  2938 ms, mean ~2.66 s, against the plan's 2665 ms baseline. Cost is dominated
  by fixed `adb` + accessibility round-trip overhead, not serialisation (a 4.5x
  size range moved the number only 620 ms). NFR-2 therefore holds **only**
  because the dump happens at freeze time; dump-on-Option-key-down stays off
  the table.
- **Density is known** — `wm density` = 420, no override, 2.625 px/dp. The
  plan's "unknown density => label everything px" mitigation is not needed on
  this device, but stays as a code path for devices that report nothing.
- **Dump bounds pair with the OVERRIDE screen size, not the physical one.**
  All three roots are `[0,0][984,2132]`, matching `wm size`'s override line
  exactly and disagreeing with the physical 1080x2400 by 9.76%. The existing
  `parseWmSize` already prefers the override line -- **do not invert it**.
  Reporting layout dp is sound; claiming physical millimetres is not.

## Four findings the plan did not anticipate, and the rulings on them

### 1. The output is not XML — ruling: cut at `</hierarchy>`

Every dump ends with a non-XML trailer glued onto the closing tag, carrying
AOSP's own misspelling:

```
</hierarchy>UI hierchary dumped to: /dev/tty\n
```

`hierchary`, not `hierarchy` — matching the correct spelling will not find it.
Any XML parser fed raw stdout fails on trailing content. Pinned by
`UIAUTOMATOR_DUMP_TRAILER` and asserted in `UiAutomatorDumpFixturesTest`.

Two further parse hazards from the same note: the XML declaration uses **single
quotes** (`<?xml version='1.0'`), so matching `version="1.0"` fails; and the
Settings dump contains **U+2011 non-breaking hyphen**, so text handling must be
UTF-8 safe. Attribute values are entity-escaped (`&amp;`).

### 2. Inverted rectangles in a vanilla dump — ruling: **normalise on parse**

Three nodes on the partially-scrolled-off "Wallpaper & style" row have bottom
above top (e.g. `[42,2083][189,2069]`, height **-14**). This is a first,
ordinary dump of the default Settings screen, not an exotic edge case.

**Operator ruling: normalise on parse.** Coordinates are swapped so every rect
leaves the parser with non-negative extents, in one place, before any geometry
sees it. `snapTarget`, `neighboursOf`, `gapsOf` and `findingOf` therefore keep
their positive-extent assumption and need no defensive guard each. The rejected
alternatives were dropping inverted nodes (silently removes real views from
snap candidates and neighbour selection) and making each pure function
defensive (same guard in five places, inviting one to miss it).

### 3. `resource-id` is useless on Compose — ruling: **text -> content-desc -> class+index**

83% non-empty `resource-id` on the View app; **6%** on the Compose app, and
that single hit is `android:id/content`, the Android content frame, not a
Compose element. `class` is no better: Compose nodes are anonymous
`android.view.View`. What Compose does populate is `text` and `content-desc`.

Note also: **Settings' own all-apps list is Compose** (`SpaActivity`) on
Android 16. "View-based apps" is a shrinking target on a modern Pixel.

**Operator ruling: a four-step fallback chain for the node identifier** --
`resource-id`, then the node's own `text`, then `content-desc`, then
class + `[n]` collision index as the last resort.

The operator first chose class+index, then revised to this chain once the note's
detail surfaced that Compose classes are anonymous too, which would have made
the Measuring panel's headline a bare `View[3]` on exactly the apps this tool
targets. The `[n]` collision index from the plan's earlier ratification is
retained as the tail of the chain, not replaced.

**This supersedes the plan's node-id format** for Stage 5 reconciliation
purposes: any downstream task that assumed `resource-id` as the sole node key
is now wrong and must implement the chain. Affects the node-id derivation in
T-4 (`HierarchyNodeResource`) and its formatting in T-7 (`MeasureFormat`).

### 4. Clipped rows are the normal case in a list — no ruling needed

Capture #3's topmost label is a row clipped by the viewport, its app-name
sibling scrolled off above. Any "topmost element" logic sees a half-element.
No design change: the snap algorithm is containment-based and does not have
topmost-element logic. Recorded so Stage 5 does not treat it as a surprise.

## Downstream effect

Every task from T-3 onward is unblocked. Two task definitions are affected by
the rulings above and the dispatcher carries them as explicit context, rather
than amending the human-gated immutable backlog mid-execution:

- **T-4** (`parseUiAutomatorDump` + `HierarchyNodeResource`) -- cut the trailer;
  tolerate a single-quoted XML declaration; be UTF-8 safe; normalise inverted
  rects; derive the node id through the four-step chain.
- **T-7** (`MeasureFormat`) -- format the node id produced by that chain,
  including the `[n]` collision index.

The spike was **PASS**, so the DoD clause about a BLOCKED spike ("the spec
cannot close with that unaddressed") does not apply.

---

# Addendum — T-5 one-call AC reworded (wave 4)

Recorded 2026-10-17 at the wave-4 TDD-red phase, by operator decision.

## The conflict

T-5's acceptance criterion as approved read:

> `dumpHierarchy` issues the dump and `wm density` in exactly one
> `DeviceCommandExecutor` call, so geometry and density cannot drift apart.

T-3 (merged, green, frozen) shipped them as **two separate sealed cases**:

```
UiAutomatorDump -> ["exec-out", "uiautomator", "dump", "/dev/tty"]
WmDensity       -> ["shell", "wm", "density"]
```

and T-3's own AC pins that 7-element argv in `DeviceArgvTest`'s **exhaustive**
list. One executor call therefore cannot carry both without either a new
composite sealed case or rewriting a merged, frozen test.

## Ruling: two executor calls inside one repository method; the AC is reworded

**Operator decision: reword the AC.** `DeviceRepositoryImpl.dumpHierarchy`
issues both commands back-to-back and maps them together atomically. The AC now
reads:

> `dumpHierarchy` pairs the dump with `wm density` inside a single repository
> call, so a snapshot is never built from one without the other.

### Why the original wording was wrong, not merely inconvenient

The AC's stated rationale -- "so geometry and density cannot drift apart" --
guards against a mechanism that does not exist:

- **Density is a static device property.** It does not change between two adb
  calls a tenth of a second apart, and it does not vary with orientation.
- **The frame dimensions come from the dump's own root bounds**, so geometry is
  internally consistent by construction regardless of when density is read.
- T-1 measured density at 420 with no override, read by the already-tested
  `parseWmDensity` path.

The real invariant worth protecting is that a snapshot is never assembled from a
dump without its density (or vice versa) -- which is a property of the mapping,
not of the process count. The reworded AC states that invariant directly.

### What was rejected

- **A composite `UiAutomatorDumpWithDensity`** (`exec-out sh -c "uiautomator
  dump /dev/tty; wm density"`): literally one call, but costs a 7th sealed case,
  an edit to T-3's frozen exhaustive argv test, fragile shell quoting through
  `exec-out`, and a combined stdout that must then be split apart again.
- **Changing `UiAutomatorDump`'s argv to the composite**: breaks T-3's merged
  acceptance criterion and makes one command silently do two unrelated things.

## For Stage 5

Reconcile T-5 against **this addendum**, not the backlog's original AC wording.
The two-call implementation is correct and expected; an audit flagging "AC says
one call, code makes two" should resolve to this record. T-5's DoD clause
asserting the one-call pairing is likewise reworded: the test asserts that both
commands are issued within the single `dumpHierarchy` invocation and that the
snapshot is built from both, rather than asserting an executor call count of 1.

Note also that T-5 is what finally gives `DeviceCommand.WmDensity` and
`parseWmDensity` their first production caller -- the plan recorded them as dead
scaffolding (risk 2), tested but never called.

---

# Addendum — operator delegation (wave 4 onward)

Recorded 2026-10-17. The operator handed decision authority to the orchestrator
for the remainder of the spec ("decide yourself from here, finish measure-mode
completely") and left the session.

**What this changes:** every gate from wave 4 onward -- including the mandatory
Stage 5 validation gate and the Stage 6 PR-approval gate -- is closed by the
orchestrator under this delegation, recorded as
`gate: {type: human, approvedBy: orchestrator-under-operator-delegation}`
rather than silently as an auto gate. Each substantive decision is appended to
this file with its reasoning, so the operator can audit or reverse any of them.

**What this does NOT change:**

- **Nothing is waived to make the run look clean.** The repo's HARDWARE-BLOCKED
  precedent stands: an acceptance criterion that cannot be verified on this
  host is recorded **blocked, not waived**. This matters here because this
  machine cannot load Skiko and there is no Compose UI-test harness, so every
  visual criterion (AC-67, AC-71 and the visible half of AC-72) is
  unverifiable by machine. Those will close as BLOCKED awaiting a human looking
  at a rendered screen, not as passed.
- **The Stage 5 full-suite regression still runs.** It is an invariant of every
  effort tier and the delegation does not touch it.
- **Frozen tests are still not edited to go green.** Wave 3 established the
  pattern: a red frozen test goes back to its author, and the implementation is
  proved correct out-of-band rather than by loosening the assertion.

---

# Addendum — two T-5 mapping decisions ruled (wave 4, under delegation)

Recorded 2026-10-17 by the orchestrator under the operator's delegation. The
T-5 implement agent reported both as "documented in KDoc at the decision site,
asserted nowhere" and asked for them to be accepted or reversed. An unasserted
behavioural decision is the inert-code failure mode this repo has already
shipped once, so both now carry tests.

## 1. A thrown `wm density` failure degrades to `densityDpi = 0` — ACCEPTED

A *thrown* `DeviceCommandException` from the density command is caught and
mapped to the `densityDpi = 0` sentinel; a failing *dump* still throws.

The earlier ruling covered an absent or unparseable density, not a failed
command, so this was genuinely open. Accepted because the dump costs ~2.7 s and
is the valuable payload — discarding it over a failed trivial probe is the wrong
trade — and because `densityDpi = 0` makes the UI label everything `px`, which
is honest rather than wrong. It follows the plan's standing rule that an unknown
density must never become a confidently wrong `dp`.

**Now asserted on both halves**, because the asymmetry is the decision: a test
covering only one half would let a future change collapse them silently.

## 2. A zero-node dump no longer discards a good density — CHANGED

Previously a dump parsing to zero nodes returned `HierarchySnapshotModel.Empty`
even when the density had parsed fine. `Empty` carries `densityDpi = 0`, so a
known density was being thrown away.

**Reversed.** With no usable nodes, measure mode falls back to
`"View (no match)"` and measures the annotation rect against the frame edges --
and that fallback still needs density to label its numbers in `dp`. Discarding
it would downgrade the fallback to `px` in precisely the case the design says
must still produce full measurements.

A zero-node dump now preserves whatever density and frame dimensions were
actually obtained. `Empty` remains the sentinel for "nothing at all" --
unreadable or empty stdout.

## 3. `HierarchySnapshotMapper` is one-direction by design — ACCEPTED

It does not implement `ModelResourceMapper`: the node model drops
`resource-id`, `text` and `content-desc`, so no honest `toResource` exists. Its
input is the named `internal data class HierarchyDumpStdout(dump, density)`
rather than two `String` parameters, which is what makes "never a snapshot from
one half without the other" a **type-level** property instead of a convention --
the invariant the reworded AC asks for. `.claude/rules/mappers.md` describes a
bidirectional shape, so a reviewer will flag this; it resolves here.

## 4. A frozen test was fixed by the orchestrator, not an agent

`DumpHierarchyUseCaseImplTest`'s `@Test` method was expression-bodied, and
`kotlin.test.assertIs<T>` returns `T`, so the method was non-`Unit`: JUnit
rejected the whole class on desktop (`should be void`) and Kotlin/Native failed
to compile it (`Test function must return Unit`). The body never ran on either
target.

Converted to a block body. **The assertion is byte-identical** -- this changed
only the method's return type, which no production change could affect. It is
recorded here because the standing rule is that the implementer never edits a
frozen test; a mechanical return-type fix that makes a test run at all does not
weaken anything, which is what that rule protects.

---

# Addendum — the design's real `measure()` recovered; a shipped @Preview value is wrong (batch A, under delegation)

Recorded 2026-10-17 by the orchestrator under delegation.

## The oracle exists after all

`design-measure-mode.md` (149 lines) is a **prose extraction**; it contains the
algorithm's snippets but not the literal `ELS` array, so the batch-A test author
correctly reported there was "nothing to port". The fixture does exist: it was
recovered from the cached bundle (`raw/rectify-screens.html`) by gzip-decoding
its base64 `__bundler/manifest` blobs, together with the design's own
`measure(a, u)` in full. Both are written up verbatim at
**`inputs/design-measure-source.md`**, which now outranks the prose md and every
paraphrase of it in this spec.

Nine details the prose did not settle are listed there. The ones that changed
test code: the design's frame is **296 × 640** (an earlier draft invented
400 × 800); finding messages use the neighbour's **`id`**, not its `node` label;
the touch-target branch fires at `h < 32` but advises `min touch 48`, two
deliberately different numbers; and the design has **no density conversion at
all** (`u` is only a label suffix), so this project's rule that guide
suppression is evaluated after px→dp conversion is an **extension** for real
devices rather than something the design states.

## `MeasureCard`'s shipped @Preview has one value the algorithm cannot produce

Running the recovered `measure()` over the real 19-node `ELS` for the
`item_price` annotation — verified twice, once by the test author and once
independently by the orchestrator:

| source | Size | Top | Right | Bottom | Left | finding |
|---|---|---|---|---|---|---|
| `MeasureCard.kt` @Preview | 48×17 | **9dp** | 20dp | 49dp | 62dp | `top Δ6dp vs item_title · expected 0` |
| the design's own algorithm | 48×17 | **15dp** | 20dp | 49dp | 62dp | identical |

Four of five values and the finding string match exactly, so this is
unambiguously the same node and the same fixture. `Top` does not. The nearest
horizontally-overlapping node above `item_price` is `divider_1`, bottom edge
183, giving `198 - 183 = 15`. **No node in the fixture has a bottom edge at
189**, so `9dp` cannot be derived from this algorithm by any neighbour pick — it
is an illustrative value that was never run through it.

The two numbers are also independent by construction: the finding's `Δ6` comes
from the **left** neighbour (`item_title`), while the Top gap comes from the
**above** neighbour (`divider_1`). Nothing couples them, which is how the
preview could be internally plausible and still wrong.

### Rulings

1. **The oracle test pins the computed `15dp`.** The implementation follows the
   design's algorithm, not the preview. Same discipline as the 32-vs-48
   mismatch: do not "fix" a value to make two sources agree when one of them is
   the authority.
2. **T-12 corrects the @Preview's `Top` to `15dp`** with a comment pointing at
   this record, so the repo's own example agrees with the algorithm it
   illustrates. This is a one-line change inside T-12's existing scope
   (`MeasureCard.kt`), not new scope.
3. **The preview is no longer admissible as ground truth** for any value other
   than the four that reproduce. Stage 5 and Stage 6 should treat
   `inputs/design-measure-source.md` as the authority and this preview as
   corroboration only.

## One rule the real fixture cannot exercise

The design renders neighbour rects with `Math.max(e.h, 1)`, a 1px floor so its
1px dividers stay visible. No `ELS` node has `h: 0`, so the real fixture cannot
test the floor. Recorded as unexercised in a test comment rather than silently
skipped.

## A correctness requirement found by tracing real data, in neither plan nor backlog

The hierarchy's **root node always wins `snapTarget`**: it fully contains every
other node and always has the largest area, so against a real dump every measure
would snap to the root and the feature could not work at all. T-1's spike note
confirms all three dumps' roots are `[0,0][984,2132]`, i.e. the whole frame.
`projectMeasureNodes` therefore excludes the full-frame root from candidates,
and a test pins it. This was found by the batch-A test author tracing the real
data path, and appears nowhere in the plan or the backlog.
