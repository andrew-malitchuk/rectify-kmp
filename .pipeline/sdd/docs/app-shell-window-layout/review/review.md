# app-shell-window-layout — review

## Envelope

| field | value |
| --- | --- |
| stage | review |
| step | review |
| status | done |
| producedBy | sdd-build-review-consolidate |
| schemaVersion | 1 |
| createdAt | 2026-10-14T23:08:22Z |
| updatedAt | 2026-10-14T23:08:32Z |

## Gate

### type

human

### approvedBy

orchestrator-auto

### approved

true

### at

2026-10-14T23:08:32Z

## Provenance

### selfHash

6514b1ae3c77059936157a39c8094ec09451a7edd2a28ed7d62c025d79fbe0c2

### inputs

| artifact | hash |
| --- | --- |
| review/findings.json | ebdafab2c2737e159709d2be745d8d87acc050586e0929ab7ff9d86ebf9d3147 |
| validation/verdict.json | b304963f333fed1a5bc8d7a5c171b6ff98e19791a2a68b805eae4cd4e0a9f112 |

## Data

### verdict

approve-with-nits

### summary

Single merged reviewer over the aggregate diff (23 commits, 48 files, +2357/-38) at fast tier. No blocking and no major findings. The slot contract is minimal and documented, the preference chain follows the existing data->domain->Koin pattern with load order untouched, and the tests are real rather than tautological. The reviewer independently recomputed all 112 WCAG pairings including Compose's 8-bit Color quantisation: every Tier-1 assertion passes and every frozen baseline matches the measured ratio to 2dp. The AppButton chrome->seg change was checked for collisions across all ~16 disabled call sites and is clean - no disabled AppButton sits on a seg- or pop-filled surface anywhere in the repo. 13 findings, all minor or nit; 5 were fixed before merge, 8 carry forward as tech debt.

### blocking


### major


### minor

- COR-1 [RectifyWindowShell.kt:77] Stage/panel seam rendered three greys: stage, an unpainted 8dp gutter showing shell bg, then line2 from the handle and line from the panel. — fixed in ef15df2
- COR-2 [ShellState.kt:52] resizePanelBy clamps per frame, so overshoot is lost and the panel edge detaches from the pointer during an over-drag. — tech-debt
- COR-3 [SettingsViewModel.kt:121] REGRESSION: THEME_MODELS lacked System, so indexOf returned -1 and the selector showed Dark while the app rendered system appearance. — fixed in ef15df2
- ARC-1 [RectifyWindow.kt:122] REGRESSION: the desktop root stopped going through App() and lost AppLocaleProvider, making pref_language inert on desktop. — fixed in ef15df2
- ARC-2 [RectifyWindowShell.kt:66] onPanelWidthCommit had a = {} default that silently discarded every committed drag - AC-39's exact failure mode, invisible at the call site. — fixed in ef15df2; de-defaulting immediately caught two preview call sites doing exactly that
- PRF-1 [StageSurface.kt:80] One drawCircle per dot: ~2.9k ops at the default window, ~15k maximised at 5K, re-issued per frame once the stage animates. — tech-debt
- CON-1 [build.gradle.kts:9] compose.material3 and kermit declared in commonMain but used only from desktopMain. — tech-debt
- MNT-1 [testing.md:6] 'Never add a test dependency module-by-module' was contradicted by data-preference-impl in the same PR. — fixed in ef15df2
- MNT-2 [AppButton.kt:281] containerColor's KDoc still warns against the translucent swap its own body now performs. — tech-debt

### nits

- MNT-3 [SidePanelWidthPreference.kt:18] Default 320 duplicated against ShellWindowDefaults.PanelDefaultWidth.
- MNT-4 [PaletteContrastTest.kt:91] The 'none silently skipped' guard is a hardcoded 56 - it cannot see a newly added token.
- MNT-5 [ShellState.kt:90] rememberShellState(initialPanelWidth) ignores later values of the parameter.
- CON-2 [.gitignore:49] .pipeline/**/raw/ is ignored while this branch force-adds the raw design PNGs.

### changeRequests


### techDebt

- COR-2: Accumulate the drag delta and clamp on commit so the panel edge tracks the pointer through an over-drag.
- PRF-1: Replace the per-dot drawCircle loop with a tiled ImageBitmap shader or a cached Path before the stage gains animation.
- CON-1: Move compose.material3 and kermit from commonMain to desktopMain in compose-application.
- MNT-2: Rewrite AppButton.containerColor's KDoc to describe the seg behaviour it now has.
- MNT-3: Source the 320 default from a single owner rather than duplicating it across the preference and the shell.
- MNT-4: Derive the guard count from ThemeColor's field count so a new token fails the test instead of slipping past.
- MNT-5: Document that rememberShellState reads initialPanelWidth once, or key the remember on it.
- CON-2: Decide whether the design PNGs belong in git; they are currently force-added against the ignore rule.

### followUps

- The 4 change requests carried from Stage 5 remain open: AF-3 remaining doc tables, AF-5 untested compose-application helpers, AF-10 E-resize cursor needing expect/actual, and the missing onPop token.
- Kotlin daemon is capped at 3GB in gradle.properties; building three KMP modules x all targets at once OOMs on an 18GB host. Pre-existing, not caused by this spec, but it makes a full ./gradlew build unreliable.

### survey

| question | options | answer | source | rationale |
| --- | --- | --- | --- | --- |
| Approve this PR for merge into development? | ["approve-with-nits (recommended)", "request-changes", "approve"] | approve-with-nits - no blocking or major findings. The two regressions this spec caused (COR-3 theme selector, ARC-1 desktop locale) plus the seam, the footgun default and the self-contradicting rule were fixed before merge; the remaining 8 are genuine nits recorded as tech debt. | orchestrator-auto | Operator granted full autonomy for the chain and asked not to stop. The reviewer recommended approve-with-nits, nothing is blocking, and every finding that represented a real regression introduced by this spec was fixed rather than filed. |
