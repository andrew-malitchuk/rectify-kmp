# history-batch-tracking — review

## Envelope

| field | value |
| --- | --- |
| stage | review |
| step | review |
| status | done |
| producedBy | sdd-build-correctness-review |
| schemaVersion | 1 |
| createdAt | 2026-10-18T21:09:16Z |
| updatedAt | 2026-10-18T21:09:22Z |

## Gate

### type

human

### approvedBy

orchestrator-autonomous

### approved

true

### at

2026-10-18T21:09:22Z

## Provenance

### selfHash

9d152d3b7bc173f4fff239c0afcdc774e95700ca877ea3e4ec603f49b2112919

### inputs

| artifact | hash |
| --- | --- |
| validation/verdict.json | 624beffa9f111205a32675aac80dfec9a663ff1075d73b6f47e76a5117a95f07 |

## Data

### verdict

approve-with-nits

### reviewers

| dimension | agent | findingCount |
| --- | --- | --- |
| merged (correctness+architecture+conventions+performance+security+maintainability) | sdd-build-correctness-review | 9 |

### summary

Single merged reviewer at fast tier over the 23 source files in development...HEAD. Layering above the repository boundary is correct, core/history is genuinely pure and tested, existing components reused rather than reinvented, and the AC-38 publish->persist->postSideEffect ordering is pinned by a test rather than a comment. Crop clamping, degenerate rects, the crop-name lookup and the rect/bitmap coordinate space were all attacked and hold. One real privacy regression plus eight smaller items.

### findings

| id | severity | dimension | file | line | rule | summary | failureScenario | status | fixedIn | note |
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
| F-1 | major | security | data-repository-impl/src/desktopMain/kotlin/data/repository/impl/core/store/FileSystemBatchFileStore.kt | 29 | NFR-4 | History archive written with the default umask while the sibling handoff slot in the same ~/.rectify root is deliberately owner-only (0700). | On a shared Linux workstation (~ defaults to 0755) another user reads ~alice/.rectify/history/*/frame.png and strings ~alice/.rectify/rectify.db, obtaining every screenshot, hierarchy dump, annotation comment and prompt Alice ever sent - permanently, since there is no retention policy. The same data inside ~/.rectify/handoff is 0700 and refuses them. | fixed | 2bb5cf4 |  |
| F-2 | minor | correctness | presentation-feature-mirror/src/commonMain/kotlin/presentation/feature/mirror/source/mirror/MirrorViewModel.kt | 964 | AC-38 | started.elapsedNow() read after the thumbnail encode and history write, inflating the reported send duration. | The first send in a process pays the one-off Skiko native-library load plus a ~1MB frame.png write, the hierarchy XML write and the DB insert before the clock is read; a 0.3s publish is reported to the user as 'Sent 1 note in 0.9 s'. | fixed | 2bb5cf4 |  |
| F-3 | minor | correctness | presentation-feature-mirror/src/commonMain/kotlin/presentation/feature/mirror/source/mirror/MirrorHistoryTab.kt | 94 | FR-28 / AC-22 | groupHistory's remember is keyed on state.history only, so the clock is re-read only when the list instance changes. | A tester sends three batches at 23:40 and leaves the panel open overnight (normal for a desktop QA tool); at 09:00 the header over all three still reads TODAY, correcting itself only when the next batch arrives or the app restarts. | deferred |  | Fix by also keying on the current local date from a one-per-minute ticker, preserving the single-clock-read purity. |
| F-4 | minor | performance | data-database-impl/src/commonMain/kotlin/data/database/impl/core/dao/HistoryEntryDao.kt | 31 | NFR-13 | observeAll has no LIMIT and @Relation pulls every child row; the reduced-state justification argues emission frequency, not volume. | At 500 batches x 3 annotations, every send and every app start materialises 500 entities (each carrying the full prompt string the read model discards) + 1500 child rows + 500 thumbnail BLOBs, and ~2-4MB stays parked in MirrorUiState.history for the process lifetime. | deferred |  | LIMIT 200 on the observe query, or drop prompt/thumbPng from the list projection and load the thumbnail per row. |
| F-5 | minor | architecture | data-repository-impl/build.gradle.kts | 13 | .claude/rules/database.md | First -impl -> -impl module edge in the repo; Room-annotated entities are now on the repository module's compile classpath and in its test fixtures. | The Room backend is no longer swappable behind data-database-api. The leak stops at the repository - nothing Room-shaped reaches domain-usecase-impl or presentation - which is why this is minor. | accepted |  | Plan-approved at T-6; recorded, not re-litigated. |
| F-6 | minor | security | data-repository-impl/src/desktopMain/kotlin/data/repository/impl/core/store/FileSystemBatchFileStore.kt | 12 | PLAN-AS-6 | Sanitiser regex keeps '.', so a batch id of '..' resolves to File(root, '..'); the comment and the test name both over-claim relative to the code. | write('..', files) writes frame.png into ~/.rectify itself. Unreachable today because mintSessionId always emits <deviceId>-f<n>-<millis>, but the guard will be trusted later by a caller with a different id source. | fixed | 2bb5cf4 |  |
| F-7 | nit | correctness | presentation-feature-mirror/src/commonMain/kotlin/presentation/feature/mirror/core/history/HistoryFormatting.kt | 131 | AC-35 (cosmetic half) | When the union clamps at the left or top edge, the requested width is preserved rather than the pad. | Union (0,0,100,100) on a 1080-wide frame gives pad 65.7 and left 0, but width stays 231, so the crop extends 131px right of the union instead of 65.7 - the thumbnail of any top-left-corner annotation is visibly off-centre. Appearance only; the rect never leaves the frame. | deferred |  |  |
| F-8 | nit | maintainability | data-repository-impl/src/commonMain/kotlin/data/repository/impl/core/mapper/HistoryEntryMapper.kt | 33 |  | cropFile(index) is 0-based while the envelope's cropFileName(index) is 1-based; the same image is crop-1.png in the handoff slot and crop-0.png under ~/.rectify/history/<id>/. | Self-consistent within each side, but F-9's diff loop must know both conventions and will mismatch crops if it assumes one. | deferred |  |  |
| F-9 | nit | test-quality | presentation-feature-mirror/src/commonTest/kotlin/presentation/feature/mirror/MirrorViewModelResolveAndSendTest.kt | 215 |  | settleUntil can burn 5s of wall clock waiting out a one-off Skiko load; MirrorUiStateSendFieldTest keeps a now-stale class and file name. | Slow, non-deterministic test time; the next person re-baselining the field test greps for the wrong name. | deferred |  |  |

### changeRequests

| id | about | status | resolvedIn | target |
| --- | --- | --- | --- | --- |
| RCR-1 | F-1 owner-only permissions on the history archive and ~/.rectify | resolved | 2bb5cf4 | FileSystemBatchFileStore.kt + DatabaseBuilder.desktop.kt |
| RCR-2 | F-2 hoist the elapsed read above the persist block | resolved | 2bb5cf4 | MirrorViewModel.kt |
| RCR-3 | F-6 reject blank/./.. batch ids in the path guard | resolved | 2bb5cf4 | FileSystemBatchFileStore.kt |

### techDebt

- F-3 stale Today header when the panel is left open across midnight
- F-4 unbounded observe query and unbounded history in reduced state
- F-5 data-repository-impl -> data-database-impl module edge
- F-7 thumbnail off-centre when the union clamps at the left/top edge
- F-8 crop file index 0-based in history vs 1-based in the handoff envelope
- F-9 settleUntil wall-clock cost and the stale SendField test name
- CR-4 no per-task report.json trail
- CR-5 no Koin DI-graph test for the five new bindings

### survey

| question | options | answer | source | rationale |
| --- | --- | --- | --- | --- |
| F-1: the history archive is written with the default umask while the ephemeral handoff slot in the same root is deliberately owner-only. How should this land? | ["Fix F-1 on this branch before merge; merge F-2..F-9 as follow-ups", "Merge as-is and track F-1 as a follow-up ticket", "Request changes: hold until F-1, F-2 and F-3 are all fixed"] | Fix F-1 on this branch before merge; merge F-2..F-9 as follow-ups | ai-proposed | F-1 turns pruned, owner-only data into a permanent world-readable copy of the tester's screen, and the fix reuses a helper this repo already has for exactly this reason. F-2 and F-6 were also one-liners and were fixed in the same pass. |

### approved

true
