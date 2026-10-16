# Backlog test id → actual test name

The backlog names its tests `T1`, `T1b`, `T5`, `T11` and so on. The suite names
them in prose. A reader grepping for the backlog's ids finds nothing and may
conclude the tests were never written, so this is the map.

Every entry below was run and, unless noted, mutation-proven.

## T-7 — the escalation ladder (`data-repository-impl`, `data-device-api`)

| backlog | actual test |
|---|---|
| T1 | `` `a timed out poll heals the daemon once and recovers without waiting for the next tick` `` |
| T1b | `timeoutFailureIsDistinguishedFromAParseFailure` (`data-device-api`) |
| T1c | `` `a failed poll does not advance the empty poll escalation` `` + `` `a non-timeout failure spawns nothing and is reported as it is` `` |
| T14 | `` `buildHostArgv renders each host variant` `` + `` `buildHostArgv appends nothing beyond the daemon subcommand` `` |
| T15 | `` `a start-server that times out escalates to kill-server then start-server` `` |
| T16 | `` `a non-timeout failure spawns nothing and is reported as it is` `` |
| T17 | `timeoutPrefixIsSharedBetweenProducerAndConsumer` (`data-device-impl`) |

Three further ladder tests were written beyond the backlog's list:
`` `a daemon that will not come back reports the real reason and keeps polling` ``,
`` `a start-server that fails for another reason is not treated as a wedge` `` and
`` `restartAdbServer kills the daemon before starting it` ``.

## T-15 — the error projection (`presentation-feature-mirror`)

| backlog | actual test |
|---|---|
| T5 | `toolMissingProjectsTheInstallCommandCard` |
| T6 | `pollFailedProjectsItsOwnCardDistinctFromNoDeviceFound` |
| T7 | `threeDevicesProjectOneRowPerDeviceAndATopDevicePrimary` |
| T9 | `noDeviceFoundProjectsVersionLastPollAndAttachedCount` |
| T10 | `errorCardIsNullWhileThePickerIsOpenOrASessionOwnsTheStage` — **revised in T-14**, see below |
| T-retry | **NOT WRITTEN.** See `GAP-fr37-immediate-repoll.md` |

T10's revision is load-bearing, not cosmetic. It originally asserted the card is
null for *any* open picker, which is what made three of the four card kinds
unreachable. It now asserts a `ToolMissing` card **is** produced with the picker
open, since the picker has nothing to list in that state.

T-retry could not be written because the mechanism it tests was removed: the
discovery nonce did not restart the collection reliably, and the gate it had to be
threaded through is one F-2 shipped and confirmed. An intermediate version of that
test was also found **vacuous** by its own mutation run. Both are recorded.

## T-17 — the banner and post-loss editing

| backlog | actual test |
|---|---|
| T8 | `streamDropBannerReportsTheStagedCount` |
| T8b | `dropBannerIsNullWhileTheSessionIsStreaming` |
| T11 | `annotationsStayEditableWhileTheSessionIsLost` |

## T-11 — reconnect and the AC-51 guard

| backlog | actual test |
|---|---|
| T2 | `reconnectAfterInvoluntaryLossKeepsAnnotationsAndFrame` — pre-fix RED **not captured**, see `GAPS-for-later-waves.md` |
| T3 | `resumeLiveClearsTheFrozenFrameAndAnnotations` — F-4's existing test, annotated to record its new duty |
| T4 | `resumeLiveIsNotOfferedWhileTheSessionIsLost` |

T3 was deliberately **not** written as a new test. Drafting it revealed F-4
already had one covering the same behaviour more strongly — it also asserts
`stage`, `session`, `startedDevices` and `activeCollectors`. A duplicate under two
names would make the mutation record ambiguous, so the existing test gained a
KDoc recording that it now also serves as F-3's AC-51 regression guard, plus a
note not to weaken its assertions.

## Beyond the backlog

| test | why |
|---|---|
| `ScrimGateTest` (2 tests) | the zero-annotation scrim gate's input (T-16) |
| `MirrorRecoveryActionsTest` (2 tests) | AC-26/AC-27 actions do real work (T-13/T-23) |
| `theStatusDotMatchesTheCardItSitsBeside` | the status line agrees with the card (T-32) |
| `every declared colour token is either swept or explicitly exempt` | fixes a test whose name over-promised |
