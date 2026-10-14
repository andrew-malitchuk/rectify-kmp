# app-shell-window-layout — findings

## Envelope

| field | value |
| --- | --- |
| stage | review |
| step | findings |
| status | done |
| producedBy | sdd-build-correctness-review |
| schemaVersion | 1 |
| createdAt | 2026-10-14T22:54:52Z |
| updatedAt | 2026-10-14T22:54:52Z |

## Gate

### type

auto

### approvedBy

sdd-build-correctness-review

### approved

true

### at

2026-10-14T22:54:52Z

## Provenance

### selfHash

ebdafab2c2737e159709d2be745d8d87acc050586e0929ab7ff9d86ebf9d3147

### inputs

| artifact | hash |
| --- | --- |
| plan/plan.json | e2130d43a2c30655ac9375c37bb1bc293a7c4595e1a8f0dbe8c510b2e5d9b344 |
| validation/verdict.json | b304963f333fed1a5bc8d7a5c171b6ff98e19791a2a68b805eae4cd4e0a9f112 |

## Data

### findings

| id | dimension | severity | file | line | ruleOrACRef | detail | verified |
| --- | --- | --- | --- | --- | --- | --- | --- |
| COR-1 | correctness | minor | presentation-core-ui/src/commonMain/kotlin/presentation/core/ui/source/kit/organism/shell/RectifyWindowShell.kt | 77 | AC-31 (stage/panel seam) + plan architecture: PanelResizeHandle 'owns no width, draws the divider' | The stage/panel seam renders TWO hairlines separated by an unpainted gutter. PanelResizeHandle draws a line2 hairline centred in an 8dp strip that paints no background of its own (PanelResizeHandle.kt:69-89), and SidePanelFrame independently draws a second hairline in Theme.color.line at its own leading edge (SidePanelFrame.kt:63-68). Verified by sampling the shipped evidence screenshot (execution/T-12/evidence/shell-light-session-active.png, row y=400): x<=911 stage #E7E7E4, x=912..919 bg #F6F6F5 (the unpainted handle strip, a third colour band), x=916 #D3D3D2 (line2 over bg), x=920 #EBEBEB (line over surface), x>=921 panel #FFFFFF. One divider is intended; the build ships a 9px seam with two rules and a lighter gutter between them. Pick one owner for the hairline and give the handle a background that matches the surface it sits between. | true |
| COR-2 | correctness | minor | presentation-core-ui/src/commonMain/kotlin/presentation/core/ui/source/kit/organism/shell/ShellState.kt | 52 | AC-38 / performanceBudget 'width follows the pointer within one frame' | resizePanelBy() applies each drag frame's delta to the ALREADY CLAMPED width, so overshoot past a bound is discarded and the panel edge detaches from the pointer. Drag 200dp past PanelMaxWidth and then back 10dp and the panel immediately narrows to 470dp while the pointer is still ~190dp outside the panel; every subsequent pixel of that drag is offset by the lost overshoot. The usual fix is to accumulate the raw drag distance from the drag-start width (dragStartWidth is already tracked) and clamp only on read, so the edge re-attaches when the pointer comes back. ShellStateTest:57-68 pins the current accumulate-then-clamp behaviour, so this is a deliberate shape, not an oversight — but it is the shape with the pointer-detach artefact. | true |
| COR-3 | correctness | minor | presentation-feature-settings/src/commonMain/kotlin/presentation/feature/settings/source/settings/SettingsViewModel.kt | 66 | AC-35 (ThemePreferenceMapper fallback Light -> System) | Flipping ThemePreferenceMapper.toModel's fallback to ThemeModel.System desynchronises the Android/iOS settings theme picker, which was not updated. THEME_MODELS = listOf(Dark, Light) contains no System entry, so loadCurrentTheme()'s THEME_MODELS.indexOf(theme).coerceAtLeast(0) maps System to index 0: a fresh install (which now resolves to System, previously to Light) shows 'Dark' highlighted while the app actually renders the system appearance, and tapping the already-highlighted option writes a real Dark override. Either add System to THEME_MODELS or make the -1 case render no selection instead of silently coercing to the first entry. Out of the desktop shell's surface, but inside this PR's blast radius. | true |
| ARC-1 | architecture | minor | compose-application/src/desktopMain/kotlin/dev/rectify/io/source/window/RectifyWindow.kt | 122 | .claude/rules/localisation.md (AppLocaleProvider is the only locale override mechanism) | Replacing App() with RectifyWindow() as the desktop root drops AppLocaleProvider from the desktop composition: App() wraps its content in AppLocaleProvider(languageCode) { AppTheme { ... } }, RectifyWindow wraps only AppTheme. pref_language therefore has no effect on desktop any more. Zero user-visible impact today (plan i18n deliberately ships no stringResource() in the shell), but the first downstream localised string on desktop will silently resolve against the host system locale instead of the stored preference, and that failure is invisible until someone switches language. Either wrap the shell in AppLocaleProvider now or record the omission next to the i18n decision. | true |
| ARC-2 | architecture | minor | presentation-core-ui/src/commonMain/kotlin/presentation/core/ui/source/kit/organism/shell/RectifyWindowShell.kt | 66 | AC-39 (panel width persists across relaunch); plan: RectifyWindowShell is the slot contract four downstream specs build against | onPanelWidthCommit defaults to {} while sidePanel is an ordinary optional slot, so the default configuration of a resizable panel silently discards every width the user drags — the one failure mode AC-39 exists to prevent, and the one a caller cannot see in a preview. The nullable-sidePanel trick is good (it genuinely makes 'panel with no content' unrepresentable, and the KDoc explains it in two sentences), but the commit hook should not be the part that is easy to forget: drop the default so a caller wiring a panel has to decide what persistence means, or document the no-op default in the KDoc paragraph that already covers the commit contract. Cheap now, expensive once four specs depend on the signature. | true |
| PRF-1 | performance | minor | presentation-core-ui/src/commonMain/kotlin/presentation/core/ui/source/kit/organism/shell/StageSurface.kt | 80 | plan performanceBudget: 'One Canvas pass over the visible stage area' | The dot lattice issues one drawCircle per dot rather than one draw call for the lattice. At the 1240x820 default with a ~920dp-wide stage on a 2x display that is ~58x51 = ~2.9k draw ops; a maximised window on a 5K display is ~12-16k ops. The budget's 'no per-dot allocation' clause does hold (Offset is a value class, the step is precomputed from LocalDensity) — the cost is call count, not garbage. It matters downstream rather than here: Compose Desktop redraws the whole window tree per frame, so once the mirrored stream animates in the stage slot the lattice is re-issued every frame underneath it, fully overdrawn. A single drawPoints(PointMode.Points, cap = StrokeCap.Round) over a remembered Offset list, or a tiled ShaderBrush, collapses it to one op. Not measured here, so the op counts are arithmetic rather than a profile. | false |
| CON-1 | conventions | minor | compose-application/build.gradle.kts | 9 | .claude/rules/platform-coverage.md (desktop-only wiring belongs on desktopMain) | compose.material3 and libs.kermit were added to commonMain, but grep finds zero references to either in compose-application's commonMain or iosMain — both are used only by desktopMain/RectifyWindow.kt (material3 Text, kermit Logger). They therefore enter the Android and iOS compile and link graphs (and the iOS static framework) for nothing. Move both into desktopMain.dependencies, which is where the only consumer lives. | true |
| MNT-1 | maintainability | minor | .claude/rules/testing.md | 6 | project rule accuracy (rule files are read as authoritative by later agents) | The rewritten rule states, in bold and without qualification, '**Never add a test dependency module-by-module.**' — and the same PR adds two of them: data-preference-impl/build.gradle.kts:14-17 wires multiplatform-settings-test and kotlinx-coroutines-test into that module's commonTest. The intended scope is clearly kotlin.test (which genuinely is wired once in the two convention plugins), but as written the rule forbids what the tree does, which is exactly the kind of contradiction this rule file exists to prevent. Scope the sentence to kotlin.test and name the per-module test-double exception. | true |
| MNT-2 | maintainability | minor | presentation-core-ui/src/commonMain/kotlin/presentation/core/ui/source/kit/atom/button/AppButton.kt | 281 | doc/code contradiction introduced by 29e6c84 | containerColor's KDoc still warns that 'hover and seg are translucent overlay tokens, not solid fills... Swapping a solid surface token for a translucent one (or for a different solid token two steps down the ladder) makes the fill jump instead of stepping, and inverts in dark mode' — and the first branch of the very function it documents (line 295) now returns Theme.color.seg for the disabled Primary/Secondary fill, i.e. exactly the swap the KDoc warns against. The change itself is sound (verified: seg over chrome composites to #DFDFDD in light, 1.14:1 against chrome where the old chrome-on-chrome was 1.00:1, and the evidence screenshot shows the pill; the dark palette is the symmetric 6% white at 1.20:1; no call site in the repo puts a disabled AppButton on a seg-filled or pop surface, so there is no collision) — but the doc now argues against the code and a future reader cannot tell which is current. | true |
| MNT-3 | maintainability | nit | data-preference-api/src/commonMain/kotlin/data/preference/api/source/model/SidePanelWidthPreference.kt | 18 | single source of truth for the 320dp default | SidePanelWidthPreference.DEFAULT_WIDTH_DP = 320 and ShellWindowDefaults.PanelDefaultWidth = 320.dp are two independent copies of the same number with no cross-reference, each pinned by its own test (SidePanelWidthPreferenceSourceTest:23, ShellWindowDefaultsTest:26). The data layer cannot depend on presentation, so the duplication is unavoidable — a KDoc cross-reference on both sides would at least make a future divergence visible to the person changing one of them. | true |
| MNT-4 | maintainability | nit | presentation-core-styling/src/commonTest/kotlin/presentation/core/styling/contrast/PaletteContrastTest.kt | 91 | test name vs. assertion strength | 'every pairing in both palettes is measured - none silently skipped' asserts a hardcoded size of 56. That catches a deletion, but not the case the name promises: a token added to ThemeColor and never swept leaves the count at 56 and the suite green. (The count itself is correct — 11 text + 4 focus + 4 ink3 + 16 signal + 20 overlay + 1 pop = 56 per palette, so the guard did survive primaryHover/primaryPress going 54 -> 56.) Either rename it to what it checks or derive the expected count from something that changes when ThemeColor does. | true |
| MNT-5 | maintainability | nit | presentation-core-ui/src/commonMain/kotlin/presentation/core/ui/source/kit/organism/shell/ShellState.kt | 90 | API-misuse hazard on the public slot contract | rememberShellState(initialPanelWidth) remembers with no key, so any later value of the parameter is ignored. The KDoc says so plainly and the reasoning (re-keying would drop the active tab) is right, but the parameter still invites the natural downstream call rememberShellState(initialPanelWidth = widthLoadedFromPreferences) where the width arrives asynchronously and is silently discarded — RectifyWindow dodges it only by calling the no-arg overload and then setPanelWidth() from a LaunchedEffect. Consider dropping the parameter (callers apply the stored width through setPanelWidth, which is the only path that works anyway). | true |
| CON-2 | conventions | nit | .gitignore | 49 | repo hygiene: ignore rule vs. tracked content | '.pipeline/**/raw/' is added while this very branch tracks .pipeline/sdd/docs/app-shell-window-layout/raw/design/01-*.png and 02-*.png (already-tracked files are unaffected by a new ignore rule). The effect is asymmetric: this spec's design sources are reviewable, the next spec's silently will not be. If raw inputs are meant to stay local, drop the two committed PNGs in the same change; if they are meant to be evidence, don't ignore the directory. | true |
