# app-shell-window-layout — audit

## Envelope

| field | value |
| --- | --- |
| stage | validation |
| step | audit |
| status | awaiting-review |
| producedBy | sdd-build-validation-audit |
| schemaVersion | 1 |
| createdAt | 2026-10-14T21:50:44Z |
| updatedAt | 2026-10-14T21:50:44Z |

## Gate

### type

human

## Provenance

### selfHash

1c66ba3114416e40b867bc5797b0933833b4f0f98a65c0186ac4491f0142571a

### inputs

| artifact | hash |
| --- | --- |
| plan/plan.json | e2130d43a2c30655ac9375c37bb1bc293a7c4595e1a8f0dbe8c510b2e5d9b344 |
| backlog/backlog.json | 0626dbba1debc8671ab3d626c7c2308eb38fe0d5ee43282ab82f0fbd86331a0e |

## Data

### iteration

1

### auditScope

#### spec

app-shell-window-layout

#### integrationBranch

sdd/app-shell-window-layout

#### base

development

#### headAtAudit

6f78fa5

#### cumulativeDiff

45 production/doc files, +2281/-20 (excluding .pipeline artifacts)

#### method

Verified against real files, the real cumulative diff, the four committed evidence PNGs (looked at, not just listed) and the real JUnit XML written to build/test-results by the orchestrator's in-flight full build. Executor reports were read as claims only; none was used as evidence.

### tasksChecked

| id | status | done | evidence | needsHumanReview | outcome |
| --- | --- | --- | --- | --- | --- |
| T-1 | done | true | LibraryConventionPlugin.kt:108-110 and FeatureConventionPlugin.kt:122-124 both add commonTest.dependencies { implementation(libs.findLibrary("kotlin-test").get()) }; gradle/libs.versions.toml:71 adds the kotlin-test alias. Proven non-vacuous: 10 TEST-*.xml files now exist under build/test-results with 49 executed tests. |  |  |
| T-2 | done | true | presentation-core-styling/src/commonTest/.../PaletteContrastTest.kt (296 lines, 54 pairings per palette) + WcagContrast.kt (31 lines). TEST-presentation.core.styling.contrast.PaletteContrastTest.xml tests="5" failures="0". Carries a documented DEVIATION from survey Q1-A — see AF-7. | true |  |
| T-3 | done | true | ShellState.kt, ShellTab.kt, ShellWindowDefaults.kt all present and public; ShellStateTest 11/11, ShellTabTest 3/3, ClampPanelWidthTest 4/4, ShortcutToTabTest 3/3, ShellWindowDefaultsTest 5/5 — all green in the real XML. |  |  |
| T-4 | done | true | StageSurface.kt:35-37 DotCellSize=16.dp / DotRadius=0.75.dp / DOT_ALPHA=0.45f as private file-level constants with an 'Open question — not design-specified' KDoc. Independently cross-checked against raw/design/02-empty-no-device.png: comp dot pitch scales to ~16dp at the 1240dp window width, so the invented value is a close match, not a guess at odds with the comp. | true |  |
| T-5 | done | true | ShellToolbar.kt (six named slots, fixed order, 52dp, 78dp leading inset) and ShellStatusBar.kt (28dp, leading/trailing). Both render correctly in all four evidence PNGs. |  |  |
| T-6 | done | true | SidePanelFrame.kt:32 PanelHeaderHeight=40.dp, KDoc'd as derived from SidebarTabs' 26dp tab, 'no comp exists for this panel'. Confirmed: neither cached design screenshot shows a side panel. The with-panel evidence PNGs are the only visual reference and they are renders of this same invented value, so they cannot validate it. | true |  |
| T-7 | done | true | RectifyWindowShell.kt:58-88 — Column[toolbar, Row[StageSurface, if(sidePanel!=null){handle,panel}], statusBar]; four @Preview pairs (light/dark x no-panel/with-panel) at :155-183. |  |  |
| T-8 | done | true | Full chain present and Koin-registered at the END of each layer group: SidePanelWidthPreference(.kt) -> Source -> SourceImpl(KEY="pref_side_panel_width") -> Mapper -> ConfigureRepository.get/setSidePanelWidth -> Get/SetSidePanelWidthUseCase(+Impl). DataPreferenceImplModule.kt:37 and DomainUseCaseImplModule.kt:47-48 both append at the end. SidePanelWidthPreferenceSourceTest 5/5 and SidePanelWidthPreferenceMapperTest 2/2 green. |  |  |
| T-9 | done | true | ThemeModel.kt: MaterialU renamed to System with mode="materialu" preserved; ThemePreferenceMapper fallback Light->System; App.kt:48/51/55 null-branch and collectAsState initial both ThemeMode.System. ThemePreferenceMapperTest 6/6 green incl. the legacy "materialu" round-trip and the "system" default. Repo-wide grep: zero remaining MaterialU references in .kt/.kts. BUT one doc reference survives — see AF-2. |  |  |
| T-10 | done | true | RectifyWindow.kt:108-115 rememberWindowState(InitialSize) + onPreviewKeyEvent; :213-233 ApplyWindowChrome sets window.minimumSize and the three apple.awt client properties behind an isMacOs() guard with runCatching. DesktopEntry.kt now calls RectifyWindow() instead of Window { App() }. |  |  |
| T-11 | done | true | RectifyShellContent (RectifyWindow.kt:158-211) fills all six toolbar slots and both status-bar slots from shipped kit components, plus an IconButton(Sun) theme-override control. Visually confirmed in all four evidence PNGs. |  |  |
| T-13 | done | true | Independently re-confirmed: grep -rn "152" .pipeline/prd/docs/ returns nothing. Correct no-op. The scope line it was meant to satisfy was based on a false premise — see AF-14. |  |  |
| T-12 | done | true | Four PNGs exist, are git-tracked, and I opened all of them. They are genuine 1240x820 in-process Compose renders with real content (toolbar, dot stage, status bar, and in the session-active pair the three-tab panel with Notes selected). They contain NO OS window chrome — no traffic lights, no title bar, no resize affordance — which independently corroborates the report's admission that the agent never saw a real window. Three inspection items remain genuinely open — see AF-4 and AF-11. | true | partial |

### findings

| id | severity | area | about | ref | issue | description | evidence | recommendedTarget | target |
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
| AF-1 | error | diff | diff | .claude/rules/testing.md | An auto-loading project rule file is now factually false because of this spec, and was not fixed. | .claude/rules/testing.md opens with 'No test exists anywhere in this repo today (zero files under any *Test* source set, and the convention plugins wire no commonTest/kotlin.test dependency) — there's no existing pattern to extend.' Every clause of that sentence is now wrong: this spec added 9 test files across 4 modules and T-1 wired kotlin-test into both LibraryConventionPlugin and FeatureConventionPlugin. The file then prescribes, as work still to do, exactly what T-1 already shipped ('Wire kotlin.test into commonTest once, in LibraryConventionPlugin/FeatureConventionPlugin') and names three 'currently unverified by anything' first targets. This file is loaded into the context of every future agent in this repo, so the damage compounds. The orchestrator itself logged the governing policy in events.jsonl (category: stale-doc, T-9): 'This staleness was introduced BY this spec and must be fixed before the spec closes' — that policy was applied to .claude/rules/mappers.md and to nothing else. | .claude/rules/testing.md lines 3-9 vs LibraryConventionPlugin.kt:108-110, FeatureConventionPlugin.kt:122-124, and 9 files matching git diff development..HEAD --name-only -- '*Test.kt'. | new doc task (terminal) | doc-fix |
| AF-2 | error | diff | diff | domain-core/README.md:21 | Module README documents an enum constant that this spec deleted. | domain-core/README.md:21 still reads '\| `ThemeModel` \| App theme enumeration (Light, Dark, MaterialU) \|'. T-9 renamed MaterialU to System. The orchestrator fixed CLAUDE.md:56 and .claude/rules/mappers.md:18 for the same rename but missed this third reference. This is the only remaining MaterialU string in the non-.pipeline tree. | grep -rn MaterialU excluding .pipeline returns exactly one hit: domain-core/README.md:21. domain-core/src/commonMain/kotlin/domain/core/source/model/ThemeModel.kt now declares Light, Dark, System("materialu"). | new doc task (terminal) | doc-fix |
| AF-3 | warn | diff | diff | six module READMEs + two rule files | Public-API inventory tables were not updated for the API this spec added. | Every touched module keeps a README table that is an inventory of its public surface. None was updated: data-preference-api/README.md (missing SidePanelWidthPreferenceSource row in 'Preference Sources' and SidePanelWidthPreference in 'Models'); data-preference-impl/README.md:27-31 (missing the SidePanelWidthPreferenceSourceImpl / pref_side_panel_width row); data-repository-impl/README.md:34-40 (missing the SidePanelWidthPreferenceMapper row); domain-usecase-api/README.md (missing Get/SetSidePanelWidthUseCase); domain-usecase-impl/README.md (missing both Impls); presentation-core-ui/README.md (missing the entire shell/ organism group and the PanelResizeHandle atom — note presentation-core-ui/CLAUDE.md WAS updated for exactly these, so the two sibling docs in one module now disagree). Also .claude/rules/preferences.md:17 enumerates the pref_<name> keys and omits pref_side_panel_width, and .claude/rules/version-catalog-hygiene.md:16 justifies junit as a dead entry with 'no test source set exists yet' — the entry is still dead but the stated reason is now false. | Read all six README.md inventory tables and both rule files directly; cross-checked against the 14 new public declarations in the cumulative diff. | new doc task (terminal) | doc-fix |
| AF-4 | warn | ac | ac | AC-33 / SC-3 | The Cmd+1/2/3 dispatch path is unreachable by construction, so the human-review item asking a human to verify it cannot be satisfied as written. | RectifyWindow.kt:103 declares `val sidePanel: (@Composable ColumnScope.(ShellTab) -> Unit)? = null` as a compile-time constant, and :111 passes `isPanelVisible = sidePanel != null` into handleShellShortcut. handleShellShortcut (:245-254) returns false unconditionally when isPanelVisible is false, so in the shipped binary Cmd+1/2/3 can never select a tab — there is no panel and no code path that makes one. This is CORRECT for AC-31/SC-4, which require exactly that. But it means T-12's open item (3) 'Cmd+1/2/3 dispatch' is not merely unverified in this environment — it is unverifiable in any environment on this build, and a human sent to check it will observe nothing and may wrongly log a defect. SC-3's precondition ('mirroring session is active and the side panel is visible') has no reachable path in this deliverable at all. The pure mapping IS covered: ShortcutToTabTest 3/3 green. The dispatch gate itself has zero coverage (see AF-5). | compose-application/src/desktopMain/kotlin/dev/rectify/io/source/window/RectifyWindow.kt lines 103, 108-115, 245-254; build/test-results/desktopTest/TEST-presentation.core.ui.kit.shell.ShortcutToTabTest.xml tests="3" failures="0". | human gate — re-word the T-12 review item, or accept AC-33 on the tested mapping alone and defer live dispatch to android-device-discovery-mirroring | T-12 |
| AF-5 | warn | task | task | T-1 / T-10 / AC-35 / SC-4 | compose-application has no test source set, so every desktop-only decision function is untested and also not manually inspected. | T-1 wired kotlin-test into LibraryConventionPlugin and FeatureConventionPlugin only. compose-application applies ApplicationConventionPlugin, which was not touched, so there is no commonTest/desktopTest there — confirmed by the absence of any compose-application entry under build/test-results. Four pieces of real logic therefore have zero automated coverage AND no completed manual inspection: handleShellShortcut (RectifyWindow.kt:245-254 — this is the function that implements SC-4's 'the event is not consumed' clause), ThemeModel.next() (:270-274 — the System->Light->Dark->System cycle that AC-35's toggle actually drives), toThemeMode() (:263-267), and isMacOs() (:238-239). AC-35's persistence half is well covered at the data layer (ThemePreferenceMapperTest 6/6), but its presentation half — the cycle order and the immediate re-render — rests on nothing. The plan anticipated this placement problem for ShellWindowDefaults and solved it by moving the constants into presentation-core-ui; the same move was not made for the dispatch/cycle logic. | No compose-application directory under any build/test-results path; ApplicationConventionPlugin.kt is absent from git diff development..HEAD --name-only. | follow-up — move shortcut-gate and theme-cycle logic into presentation-core-ui (testable) or wire kotlin-test into ApplicationConventionPlugin | T-10 |
| AF-6 | warn | ac | ac | AC-36 / AC-40 | The contrast sweep's own anti-omission guard overclaims: two of ThemeColor's 22 tokens are never measured in either palette. | PaletteContrastTest has a test literally named 'every pairing in both palettes is measured, none silently skipped' asserting 54 pairings per palette. I counted the builders: 9 text + 4 focus + 4 ink3 + 16 signal + 20 alpha + 1 pop = 54 in each — the arithmetic is honest. But ThemeColor.primaryHover (ThemeColor.kt:51) and ThemeColor.primaryPress (ThemeColor.kt:52) appear nowhere in either builder, in any role. AC-36 names both explicitly in its enumerated token list that must 'meet WCAG AA'. onPrimary-on-primaryHover and onPrimary-on-primaryPress are the pairings a real hovered/pressed primary button paints, and neither has even a frozen baseline. Only onPrimary-on-primary (the resting state) is asserted. The guard test's name makes the omission harder to notice, not easier. | presentation-core-styling/src/commonMain/kotlin/presentation/core/styling/core/ThemeColor.kt:50-52 declares primary, primaryHover, primaryPress; grep for primaryHover/primaryPress in PaletteContrastTest.kt returns zero matches; PaletteContrastTest.kt assertEquals(54, lightPairings().size). | T-2 — add four pairings (onPrimary over primaryHover/primaryPress, both palettes) and raise the count to 56 | T-2 |
| AF-7 | warn | ac | ac | AC-30 / AC-40 / NFR-11 (T-2 adjudication) | CORRECTION to the dispatch framing: the line/line2 3:1 hard-fail is in the sealed human-approved plan.json, not only in the orchestrator's brief. The deviation is technically right but overrides a human gate answer and needs explicit ratification. | The dispatch brief states the composited line/line2 3:1 requirement 'came from the ORCHESTRATOR'S dispatch brief, not from plan.json — the plan's own wording is looser.' That is not accurate. plan.json .data.survey[0].answer, source 'user-edited', reads: 'Hard-fail on the pairings this shell actually paints (ink/ink2 on bg/chrome/stage/surface at 4.5:1; focus and composited line/line2 at 3:1; onPrimary on primary at 4.5:1)'. The brief faithfully transmitted a sealed human decision. So this is an executor deviation from the human gate, not a brief defect. ADJUDICATION: the deviation is nonetheless the correct engineering call and is handled about as well as it could be. (a) It is arithmetically unsatisfiable — the test itself proves the mechanism in 'compositing an alpha token changes what is measured', and the 16 recorded hairline baselines run 1.19 to 1.58:1 across both palettes. (b) The only remedy is editing tokens AC-36 freezes and scope.out[6] forbids. (c) WCAG 2.2 SC 1.4.11 governs 'visual information required to identify user interface components and states'; a 1dp decorative divider between two panes is not that, so the gate's inclusion of line/line2 in the hard-fail tier was itself the error. (d) The implementation does not hide it: 16 per-line '// WAIVED: <ratio> < 3:1 — AC-40/NFR-11 waived, see DEVIATION above' comments plus a named DEVIATION paragraph in the class KDoc that ends 'Needs a human call.' VERDICT: accept as a satisfaction of AC-30/AC-40/NFR-11, but only by explicit gate ratification — an agent cannot unilaterally overrule a sealed user-edited survey answer. | plan.json .data.survey[0] (source: user-edited); PaletteContrastTest.kt class KDoc 'DEVIATION from the gate's Tier 1 list' paragraph; the 16 'line over X' / 'line2 over X' frozen() entries with baselines 1.19-1.58. | human gate — ratify the downgrade and amend survey Q1-A, or re-open AC-36 for the line tokens | T-2 |
| AF-8 | info | ac | ac | AC-40 | AC-40 is satisfied only under Q1-A's rendered-scope reading; the literal AC fails on four light-mode pairs and the screenshots prove nothing either way. | Independently confirmed by looking at all four evidence PNGs: not one signal colour appears anywhere in any of them. DeviceChip(None), CounterBadge(Empty) and StatusIndicator(Offline) all resolve to ink3/seg greys, so the executor's own 'AC-40 evidence is WEAK' admission is understated — the screenshots carry zero AC-40 signal. The real coverage is PaletteContrastTest's signal tier, and it is genuine: dark hard-asserts all 16 signal pairings at 3:1 and they pass with margin (3.51-7.71). Light passes annotate (3.15-3.90) and measure (3.25-4.03) but WAIVES sentFixed (2.70/2.49/2.36/2.92) and warnStillOff (2.19/2.02/1.91/2.36) — eight sub-threshold pairings, not four. So literal AC-40 is NOT met in the light palette. It is met under the Q1-A rendered-scope policy the human gate chose, because this deliverable paints none of them. Recommend recording AC-40 as 'satisfied with waiver' with the eight light pairs handed explicitly to android-device-discovery-mirroring, which is the first spec that will actually paint them. | Opened execution/T-12/evidence/*.png (all four); PaletteContrastTest.kt lightPairings() frozen() entries for sentFixed and warnStillOff vs darkPairings() nonText() entries for the same tokens. | human gate — record AC-40 as satisfied-with-waiver and carry the eight light pairs downstream | T-2 |
| AF-9 | info | ac | ac | AC-31 | AC-31's 'matching both cached design screenshots' clause is not met; the plan carved it out but the AC text was never amended. | I compared raw/design/02-empty-no-device.png against execution/T-12/evidence/shell-light-no-session.png directly. MATCHES: toolbar element order and content (leading inset, Android\|iOS with Android selected, '. No device v', right-aligned '0 staged', 'Clear', 'Resolve & Send' with the Cmd-Return glyph), the faint dot ground at a pitch that scales to the implemented 16dp, the status bar ('. Waiting for device...' mono left, 'Rectify 0.4' right), and the absence of any side panel. DOES NOT MATCH: comp 02's stage carries a dashed rounded phone outline with 'NO SIGNAL' inside it, a 'Waiting for an Android device' heading, a subhead, two numbered instruction steps, a Pixel_7_API_34 picker and a Boot button; the shipped stage renders a single centred 'NO SIGNAL'. Also the implementation adds a Sun IconButton to the status bar that the comp does not have (required by AC-35, so correct). plan.json errorStates.empty and scope.out[2] both explicitly place comp 02's richer stage content downstream, so this is an approved scope decision — but a reader holding only the AC text will score AC-31 as failed. Worth an explicit gate note. Secondary observation: comp 02 draws 'Resolve & Send' as a filled disabled pill; the implementation renders it as bare disabled text. | Direct visual comparison of the two PNGs; plan.json .data.errorStates[0].behavior and .data.scope.out[2]. | human gate — ratify the carve-out in writing | T-12 |
| AF-10 | warn | ac | ac | A11Y-8 / NFR-11 | The plan's accessibility requirement says the resize handle sets an E-resize cursor; the code sets a Hand cursor. | plan.json accessibility A11Y-8 states 'PanelResizeHandle has an 8dp pointer target around a 1dp hairline and sets an E-resize hover cursor.' The 8dp target is real (PanelResizeHandle.kt:31 HandleHitWidth = 8.dp). The cursor is not: PanelResizeHandle.kt:73 sets `.pointerHoverIcon(PointerIcon.Hand)` with an honest inline comment that commonMain has no E-resize icon. Compose Desktop does expose it as PointerIcon(Cursor(Cursor.E_RESIZE_CURSOR)); the smallest fix is a `cursor: PointerIcon = PointerIcon.Hand` parameter the desktop caller overrides. Either fix the code or amend A11Y-8 — today the approved plan and the shipped code state different things, and a hand cursor on a divider reads as 'drag this object' rather than 'resize this edge'. | presentation-core-ui/src/commonMain/kotlin/presentation/core/ui/source/kit/atom/handle/PanelResizeHandle.kt:70-73 vs plan.json .data.accessibility[7].requirement. | T-4 (PanelResizeHandle) or a plan amendment | T-4 |
| AF-11 | info | ac | ac | AC-34 | Two of AC-34's three clauses have no evidence of any kind; the four PNGs cannot supply it. | AC-34 has three clauses. (1) 'initial size is 1240x820' — satisfied twice over: ShellWindowDefaultsTest pins DpSize(1240.dp, 820.dp) (5/5 green) and all four evidence PNGs are exactly 1240x820 pixels of rendered content. (2) 'it is user-resizable' and (3) 'cannot be resized below 1024x700' — no evidence. The constant is pinned by test and the AWT application is three lines (RectifyWindow.kt:217-220, window.minimumSize = Dimension(1024, 700)), but nobody has dragged the window. Critically, the evidence PNGs cannot ever close this: they are in-process Compose renders with no OS window chrome at all — no traffic lights, no title bar, no resize grip — which is also why T-12's item (2), the traffic lights sitting inside the 78dp toolbar inset, stays open. Residual risk is low (the AWT path is short and the client properties were confirmed applied by an in-process probe) but non-zero: minimumSize on a non-macOS host measures the outer frame including decorations, so the content floor would be slightly under 700 there. Real human inspection on a 2x Retina macOS display is still required. | build/test-results/desktopTest/TEST-presentation.core.ui.kit.shell.ShellWindowDefaultsTest.xml tests="5" failures="0"; RectifyWindow.kt:215-221; visual inspection of all four PNGs confirming zero OS chrome. | human gate — manual inspection on macOS | T-12 |
| AF-12 | info | diff | diff | compose-application/src/desktopMain/.../DesktopEntry.kt | Desktop silently loses every existing feature screen. Declared out of scope, but it is a user-visible regression the human should knowingly accept. | runDesktopApp() previously composed Window { App() }, which mounted the Navigation3 host and with it splash, onboarding, home, settings and about. It now composes RectifyWindow(), which has no navigation host at all — so on desktop none of those screens is reachable any more. App() is untouched and remains the Android/iOS root. plan.json scope.out[10] names this precisely ('Deciding the fate of the existing template features ... now that the desktop root bypasses NavigationHost — carried forward as an OPEN question, deliberately NOT decided here and deliberately NOT put to this survey'), so it is in-scope-as-declared and not a defect. Flagging because it is the single largest behavioural change in the diff and it was deliberately kept off the survey the human answered. | git diff development..HEAD -- compose-application/src/desktopMain/kotlin/dev/rectify/io/source/window/DesktopEntry.kt; plan.json .data.scope.out[10]. | human gate — acknowledge; schedule the open question | T-10 |
| AF-13 | info | ac | ac | AC-36 / AS-6 | The sweep re-derived rather than copied the refiner's figures, and corrected one of them. The plan now carries a superseded number. | AS-6 required that the refiner-only chrome/stage figures 'MUST be re-derived by the step-2 sweep, never hardcoded from this document.' That instruction was followed and it caught an error: plan A11Y-4 and survey Q2-A both quote light 'ink2 on chrome ~5.87', while the sweep measures and records 5.75. Every other refiner figure I spot-checked reproduces exactly (light ink3 on bg 3.13 / chrome 2.89 / stage 2.73 / surface 3.39; dark ink 14.74, ink2 6.64, ink3 on bg 3.39 / chrome 3.07; light sentFixed on chrome 2.49). Both 5.87 and 5.75 clear the 4.5:1 text minimum so there is no AC impact — this is the process working. Noting it only so the gate knows plan.json's A11Y-4 table and survey Q2-A hold one stale figure. Separately, A11Y-4 lists dark 'ink on pop 12.19 PASS' but the sweep measures pop only as 'pop under Color.White' — that is survey Q3-A's explicit instruction, not an omission. | PaletteContrastTest.kt lightPairings() text("ink2 on chrome", baseline = 5.75) vs plan.json .data.accessibility[3] and .data.survey[1].answer. | none — informational | T-2 |
| AF-14 | info | task | task | T-13 / scope.in[16] | An in-scope deliverable had no target: the text it was meant to amend is not in the PRD, it is in this spec's own sealed artifacts. | plan scope.in[16] commits to 'Documentation amendment: the PRD's green-signal note changes from oklch hue 152 to the shipped 150; no code change.' Confirmed independently: zero occurrences of '152' anywhere under .pipeline/prd/docs/. T-13's no-op is therefore correct. But the stale figure does exist — 'oklch(0.66 0.15 152)' appears in .pipeline/sdd/docs/app-shell-window-layout/inputs/input.md and preparation/input.md, which are sealed read-only artifacts that must not be edited. The shipped palettes already carry the right value and comment it: LightColor.kt:45 and DarkColor.kt:46 both read sentFixed = Color(0xFF3EAB5E), // oklch(0.66 0.15 150). So the in-scope line was written against a premise that did not hold, and nothing in the tree is actually wrong. | grep -rn "152" .pipeline/prd/docs/ -> no matches; grep in inputs/input.md and preparation/input.md -> 'oklch(0.66 0.15 152)'; LightColor.kt:45, DarkColor.kt:46. | none — close the scope line as void | T-13 |
| AF-15 | info | diff | diff | RectifyWindowShell.kt + RectifyWindow.kt | Two independent copies of the same stub composition, and they have already drifted on a point the human gate decided. | RectifyWindowShell.kt's private ShellPreviewToolbar/ShellPreviewStatusBar/ShellPreviewStage and RectifyWindow.kt's RectifyShellContent build the same six toolbar slots and two status-bar slots from the same components with the same literal strings ('No device', 'staged', 'Clear', 'Resolve & Send', the Cmd-Return glyph, 'Waiting for device...', 'Rectify 0.4', 'NO SIGNAL'). ShellToolbar.kt and ShellStatusBar.kt each carry a third near-copy for their own previews. The drift is not hypothetical — it has already happened on a gate decision: survey Q2-A requires 'the shell's own new files use Theme.color.ink2 for any text the shell itself authors', the shipped stage correctly uses ink2 (RectifyWindow.kt:206), and the preview stage uses ink3 (RectifyWindowShell.kt:131). The preview that is supposed to be standing evidence for AC-30/AC-31 therefore paints a token the gate ruled against for shell-authored text. Low impact (preview only, and ink3 is swept at 3:1), but it is exactly the failure mode duplication produces. | RectifyWindowShell.kt:93-135 vs RectifyWindow.kt:158-211; RectifyWindowShell.kt:131 color = Theme.color.ink3 vs RectifyWindow.kt:206 color = Theme.color.ink2; plan.json .data.survey[1].answer. | follow-up — hoist one internal preview-stub composable, or at minimum switch RectifyWindowShell.kt:131 to ink2 | T-7 |
| AF-16 | info | diff | diff | StageSurface.kt:72-85 | The dot grid issues one draw op per cell; within the stated budget today, worth knowing before the video stream lands on top of it. | The Canvas loop calls drawCircle once per grid cell. At the 1240x820 default on a 2x display the stage is roughly 1232x740dp, which is about 77 x 46 = ~3,500 draw ops recorded per draw pass, scaling with window area. The plan's explicit budget ('One Canvas pass over the visible stage area with no per-dot allocation inside the draw loop (precompute the step from LocalDensity)') IS met: cellPx and dotRadiusPx are computed once at :65-67 and Offset is a value class, so the loop allocates nothing. Raising it only because this surface is where a 60fps mirrored device stream will composite downstream; if it ever shows in a profile, a tiled ImageShader brush replaces the whole loop with one drawRect. | presentation-core-ui/src/commonMain/kotlin/presentation/core/ui/source/kit/organism/shell/StageSurface.kt:64-85; plan.json .data.performanceBudget[2]. | none — informational, revisit in android-device-discovery-mirroring | T-4 |
| AF-17 | info | registry | registry | .pipeline/sdd/docs/registry | No registry exists; no consistency check is possible. | Confirmed by direct ls: .pipeline/sdd/docs/registry does not exist. plan scope.out[12] forbids any write to it. No phantom entries and no missing entries can be determined. Not a defect. | ls -d .pipeline/sdd/docs/registry -> No such file or directory. | none | none |
| AF-18 | info | diff | diff | whole cumulative diff | Scope discipline and token discipline are clean. Nothing from a downstream spec leaked in. | Grep over all 45 changed production/build files for adb\|idb\|scrcpy\|mcp\|screenRecord returns exactly two hits, both KDoc sentences asserting the opposite: RectifyWindow.kt:152 'nothing in this file knows what adb or idb is' and ShellStatusBar.kt:69 'Comp 01's green adb ... ready line is deliberately not modelled'. No device discovery, no annotation/measure/payload/history implementation, no MCP wiring, no iOS support work, no new network or filesystem access. Zero TODO/FIXME/HACK/XXX markers. DEMO_MODE is still `false` (App.kt:21). Token discipline: zero Color(0x and zero TextStyle( in any new production file; every .dp occurrence is either a private file-level val (HandleHitWidth, StatusBarHeight, ToolbarHeight, PanelHeaderHeight, DotCellSize, DotRadius, and six Preview* constants) or a ShellWindowDefaults public constant, which is the geometry object itself — the DoD's 'no raw .dp at any shell CALL site' holds. Package naming matches the repo's existing convention (bare presentation./data./domain. roots for library modules, dev.rectify.io for compose-application) and test packages mirror their production packages. explicitApi() compliance is proven by construction: the modules compiled and their tests ran. | git diff development..HEAD --name-only -- '*.kt' '*.kts' piped through grep for each pattern; per-file .dp inventory; build/test-results existence implies successful compilation under explicitApi(). | none | none |

### gaps

#### unsatisfiedAC

- AC-33
- AC-34

#### unsatisfiedSC

- SC-3

#### incompleteTasks


### acceptanceCriteriaChecked

| id | satisfied | verificationMethod | evidence |
| --- | --- | --- | --- |
| AC-12 | true | inspection | ShellTab.kt declares exactly Notes/Payload/History; ShellTabTest pins size, order and labels (3/3 green). Visually confirmed in shell-dark-session-active.png and shell-light-session-active.png: three tabs over a bezel-less dotted stage. |
| AC-30 | true | inspection | Light and dark renders exist for both panel states and both read correctly; PaletteContrastTest 5/5 green under the Q1-A/Q2-A/Q3-A policy. Satisfied under the gate's rendered-scope reading — see AF-7 and AF-8 for what that reading waives. |
| AC-31 | true | inspection | Structural guarantee verified directly, not taken on faith: RectifyWindowShell.kt:71 wraps BOTH PanelResizeHandle and SidePanelFrame in a single `if (sidePanel != null)` block, and the slot is a nullable lambda with no separate visibility flag, so 'panel with no content' is inexpressible. RectifyWindow.kt:103 passes null. shell-light/dark-no-session.png show no panel and no handle. CAVEAT: the 'matching both cached design screenshots' clause is not met for stage content — see AF-9. |
| AC-32 | true | inspection | ShellState.activeTab initialises to ShellTab.Notes and onSessionStarted() resets to it while leaving panelWidth untouched; ShellStateTest covers both (11/11 green). shell-dark-session-active.png shows Notes selected. |
| AC-33 | false | test | PARTIAL. The pure mapping is satisfied: ShortcutToTabTest 3/3 green covers Cmd+1/2/3, the no-meta case and unmapped keys. The dispatch half is not: handleShellShortcut is untested (no test source set in compose-application) and unreachable at runtime because sidePanel is a constant null, so no human can demonstrate it either. See AF-4 and AF-5. |
| AC-34 | false | inspection | PARTIAL. Clause 1 (1240x820 initial) satisfied: ShellWindowDefaultsTest 5/5 green plus four 1240x820 renders. Clauses 2 and 3 (user-resizable, floor at 1024x700) have no evidence — the AWT wiring exists at RectifyWindow.kt:217-220 but no window was ever observed and the evidence PNGs contain no OS chrome. Needs human inspection on a 2x Retina macOS display. See AF-11. |
| AC-35 | true | test | The test-verified half is fully green: ThemePreferenceMapperTest 6/6 pins light->Light, dark->Dark, legacy "materialu"->System, DEFAULT_THEME "system"->System, garbage->System and toResource(System).theme == "materialu" (no stored value orphaned). App.kt null branch and collectAsState initial are both ThemeMode.System. The inspection half (immediate re-render on toggle, persistence across relaunch) was not performed and the cycle function ThemeModel.next() is untested — see AF-5. |
| AC-36 | true | inspection | Confirming the orchestrator's check rather than redoing it: git diff over presentation-core-styling/src/commonMain is empty and the only two files added to that module are commonTest. Zero Color(0x / TextStyle( in new production files; every .dp is a private file-level val or a ShellWindowDefaults constant. Contrast clause satisfied under Q1-A/Q2-A. Gap: primaryHover and primaryPress are named in AC-36's token list and are not swept at all — see AF-6. |
| AC-37 | true | inspection | RectifyShellContent fills all six toolbar slots and both status-bar slots from shipped kit components in fixed non-functional states (DeviceChipState.None, CounterBadgeState.Empty, both AppButtons enabled=false, StatusIndicatorState.Offline). ShellToolbar's slots are named parameters, not a RowScope lambda, so callers cannot reorder the canonical bar. Repo-wide grep confirms no adb/idb/device import anywhere in the shell or the window. Toolbar order matches comp 02 exactly. |
| AC-38 | true | test | clampPanelWidth coerces into [260.dp, 480.dp]; ClampPanelWidthTest 4/4 green covers below, above, inside and the inclusive exact bounds. ShellStateTest 11/11 green covers clamping through the constructor, setPanelWidth and resizePanelBy at both ends. The drag gesture itself is inspection-only and unexercised (the panel never appears) — but PanelResizeHandle reports raw deltas and owns no state, so everything behind the gesture is covered. |
| AC-39 | true | test | SidePanelWidthPreferenceSourceTest 5/5 green, including the relaunch proxy ('a fresh source over the same settings sees the stored width') and an explicit key assertion that the stored key is literally pref_side_panel_width. SidePanelWidthPreferenceMapperTest 2/2. ShellStateTest proves onDragStopped() returns null for a no-op drag, a netted-out drag, a drag that only pushed a clamped bound, and a width change made outside a drag — so the zero-writes-during-drag budget holds. RectifyWindow.kt:97-101 reads the width once in LaunchedEffect(Unit) and applies it via setPanelWidth (which clamps), not by re-keying remember. |
| AC-40 | true | inspection | SATISFIED WITH WAIVER under survey Q1-A only. Dark hard-asserts all 16 signal pairings at 3:1 and passes (3.51-7.71). Light waives eight pairings (sentFixed 2.36-2.92, warnStillOff 1.91-2.36), so literal AC-40 fails on the light palette. The screenshots contribute nothing — I confirmed no signal colour is painted in any of the four. See AF-8. |
| NFR-9 / NFR-11 | true | test | PaletteContrastTest 5/5 green over both palettes, 54 pairings each, with the A11Y-4/A11Y-5 measurement tables carried as checked-in baseline data and both WCAG exemptions (SC 1.4.3 inactive component, SC 1.4.11 decorative graphic) encoded by name in the KDoc. ShellStatusBar's shell-authored text uses ink2 per Q2-A. Waivers per AF-7/AF-8; sweep gap per AF-6; A11Y-8 cursor mismatch per AF-10. |
| NFR-10 | true | inspection | The three apple.awt client properties sit behind an isMacOs() guard inside runCatching with a logged fallback (RectifyWindow.kt:227-236), so a non-Mac host degrades to a standard title bar rather than failing. Only :desktop-application produces a runnable artifact and it builds for the current OS via compose.desktop.currentOs. |

### scenariosChecked

| id | satisfied | evidence |
| --- | --- | --- |
| SC-1 | true | Shell-scoped portion only, as the plan's own SHELL SCOPE NOTE requires. The window composes at 1240x820 with the stub toolbar ('. No device'), the bezel-less dotted stage and the stub status bar ('. Waiting for device...'). Detection, picker content and Start mirroring are explicitly downstream. Live window-open was not observed — see AF-11. |
| SC-2 | false | PARTIAL. Step 1 (default follows system appearance) is test-proven at the data layer: ThemePreferenceMapperTest shows DEFAULT_THEME "system" and every unknown value resolve to ThemeModel.System, and App.kt/RectifyWindow both initialise to System. Steps 2, 3 and 5 (toggle fires, both palettes re-render immediately, choice survives relaunch) were never observed and ThemeModel.next() — the cycle the toggle drives — has no test. Step 4's 'all four signal colors unchanged' is vacuous here since none is painted. |
| SC-3 | false | Not satisfiable in this deliverable. The scenario opens with 'Mirroring session is active and the side panel is visible', a state RectifyWindow cannot reach (sidePanel is a constant null). The underlying mapping is green (ShortcutToTabTest 3/3) but the scenario as written has no reachable path. See AF-4. |
| SC-4 | true | Structurally verified line by line. 'Side panel is absent (sidePanel slot is null - neither the panel nor the resize handle is composed)': RectifyWindowShell.kt:71 guards both behind one null check. 'Cmd+1/2/3 pressed in this state is a no-op and the event is not consumed': handleShellShortcut returns false before calling onTabSelect when isPanelVisible is false (RectifyWindow.kt:251). The final step ('a mirroring session starts, side panel appears') is downstream. Caveat: the non-consumption clause has no test — see AF-5. |
| SC-5 | true | Every step except the physical drag is covered by green tests. Default 320dp on first run (SidePanelWidthPreference.DEFAULT_WIDTH_DP, pinned). Stops at 260 and at 480 (ClampPanelWidthTest, ShellStateTest). Written once on release and only if changed (four distinct ShellStateTest no-change cases all return null). Reopens at the last-set width on relaunch (SidePanelWidthPreferenceSourceTest's fresh-source-over-same-store case). The drag gesture itself is unexercised because the panel never appears. |

### unresolvedFlags

| task | flag | reason |
| --- | --- | --- |
| T-2 | needsHumanReview | Contrast policy conflict. ADJUDICATED in AF-7: the composited line/line2 3:1 hard-fail is NOT a brief defect — it is in plan.json's sealed, user-edited survey Q1 answer. The deviation is nonetheless technically correct (unsatisfiable without editing AC-36-frozen tokens; a 1dp decorative divider is outside WCAG SC 1.4.11's scope) and is documented with 16 inline WAIVED comments plus a named DEVIATION paragraph. Recommend ACCEPT, but the gate must ratify explicitly because it overrides a human answer. |
| T-4 | needsHumanReview | Invented dot-grid values (16dp cell, 0.75dp radius, 0.45 alpha on ink3). ADJUDICATED: low risk and better grounded than the report claims — I measured comp 02's dot pitch and it scales to ~16dp at the 1240dp window width, so the invented cell size matches the comp. Correctly kept as private file-level constants with an 'Open question - not design-specified' KDoc and deliberately not promoted to Theme tokens. Recommend ACCEPT with a design-review note. |
| T-6 | needsHumanReview | PanelHeaderHeight = 40dp derived from SidebarTabs geometry, not a comp. CONFIRMED unverifiable: I checked both cached design screenshots and neither shows a side panel at all, and the with-panel evidence PNGs are renders of this same invented value so they cannot validate it. Lowest-stakes of the four (one private constant, trivially changed later). Recommend ACCEPT with a design-review note. |
| T-12 | needsHumanReview | Three inspection items open. CONFIRMED independently: all four evidence PNGs are in-process Compose renders containing zero OS window chrome, so the agent demonstrably never saw a real window. (1) resize floor at 1024x700 - still open, needs a human drag. (2) traffic lights inside the 78dp inset - still open; the three client properties are applied in code behind an isMacOs() guard but were never seen. (3) Cmd+1/2/3 dispatch - CANNOT be verified by a human on this build at all, because sidePanel is a constant null (AF-4); the review item needs re-wording before anyone is sent to check it. Theme persistence across relaunch - still open and untested in the presentation layer (AF-5). Outcome 'partial' is an accurate self-assessment. |

### regressionRun

#### command

./gradlew build -x lint

#### owner

orchestrator (running at audit time; audit instructed not to start a competing build)

#### passed

true

#### scope

partial-at-audit-time

#### output

```
Read first-hand from the real JUnit XML the in-flight build wrote to build/test-results, NOT from any executor report. 10 suites, 49 tests, 0 failures, 0 errors, 0 skipped:

./data-repository-impl/build/test-results/desktopTest/TEST-data.repository.impl.ThemePreferenceMapperTest.xml                               tests="6" skipped="0" failures="0" errors="0"
./data-repository-impl/build/test-results/desktopTest/TEST-data.repository.impl.SidePanelWidthPreferenceMapperTest.xml                     tests="2" skipped="0" failures="0" errors="0"
./data-preference-impl/build/test-results/desktopTest/TEST-data.preference.impl.SidePanelWidthPreferenceSourceTest.xml                     tests="5" skipped="0" failures="0" errors="0"
./data-preference-impl/build/test-results/iosSimulatorArm64Test/TEST-iosSimulatorArm64Test.data.preference.impl.SidePanelWidthPreferenceSourceTest.xml  tests="5" skipped="0" failures="0" errors="0"
./presentation-core-styling/build/test-results/desktopTest/TEST-presentation.core.styling.contrast.PaletteContrastTest.xml                 tests="5" skipped="0" failures="0" errors="0"
./presentation-core-ui/build/test-results/desktopTest/TEST-presentation.core.ui.kit.shell.ShellStateTest.xml                               tests="11" skipped="0" failures="0" errors="0"
./presentation-core-ui/build/test-results/desktopTest/TEST-presentation.core.ui.kit.shell.ShellWindowDefaultsTest.xml                      tests="5" skipped="0" failures="0" errors="0"
./presentation-core-ui/build/test-results/desktopTest/TEST-presentation.core.ui.kit.shell.ClampPanelWidthTest.xml                          tests="4" skipped="0" failures="0" errors="0"
./presentation-core-ui/build/test-results/desktopTest/TEST-presentation.core.ui.kit.shell.ShellTabTest.xml                                 tests="3" skipped="0" failures="0" errors="0"
./presentation-core-ui/build/test-results/desktopTest/TEST-presentation.core.ui.kit.shell.ShortcutToTabTest.xml                            tests="3" skipped="0" failures="0" errors="0"

Notes:
- The DoD's four required tasks (:presentation-core-styling:desktopTest, :presentation-core-ui:desktopTest, :data-preference-impl:desktopTest, :data-repository-impl:desktopTest) are ALL represented and ALL green.
- Tests ran at 00:41:57-00:42:02; the build process was still alive at 00:43:44, so compile/link tasks past the test phase (iOS framework link, remaining allTests variants) had not finished. Only one iosSimulatorArm64Test result had landed. The orchestrator's final BUILD SUCCESSFUL/FAILED line is authoritative for those.
- Successful execution of these tests is itself proof that every touched module compiled under explicitApi(), which is the DoD's explicit-API gate.
- The Android variant cannot build in this environment (no local.properties/ANDROID_HOME) - pre-existing, unrelated to this spec.
- Pre-execution baseline logged in events.jsonl: compileKotlinDesktop for both presentation modules was BUILD SUCCESSFUL before any task ran, so no failure here would be pre-existing.
```

#### newFailures


### aggregateDiffReview

- Documentation debt is the dominant cross-cutting defect and no single task owns it. The orchestrator wrote the correct policy into events.jsonl for T-9 ('staleness introduced BY this spec must be fixed before the spec closes') and then applied it exactly once, to .claude/rules/mappers.md. Four further doc surfaces this spec invalidated were missed: .claude/rules/testing.md (now false in its opening sentence and still prescribing T-1's completed work), domain-core/README.md:21 (names the deleted MaterialU constant), six module README inventory tables (14 new public declarations unlisted), and .claude/rules/version-catalog-hygiene.md:16 (junit justified by 'no test source set exists yet'). AF-1 / AF-2 / AF-3.
- Testability stops at the compose-application boundary. T-1 wired kotlin-test into the library and feature convention plugins only, so handleShellShortcut, ThemeModel.next(), toThemeMode() and isMacOs() are untested — and three of those sit directly under ACs the gate is being asked to sign (AC-33, AC-35, SC-4). The plan solved this exact placement problem once, by putting ShellWindowDefaults in presentation-core-ui so AC-34's constants could be asserted; the same move was not repeated for the desktop decision functions. AF-5.
- Duplication: four near-identical copies of the stub toolbar/status-bar composition (RectifyWindowShell.kt previews, RectifyWindow.kt RectifyShellContent, ShellToolbar.kt preview, ShellStatusBar.kt preview) with the same hardcoded strings. They have already drifted on a gate decision — the preview stage paints ink3 while the shipped stage paints ink2, and survey Q2-A requires ink2 for shell-authored text. AF-15.
- Security: nothing to flag. The diff adds no network call, no filesystem access beyond java.util.prefs, no permission request, no entitlement, no analytics and no logging of user data. The one new persisted value is an integer panel width. The three apple.awt client-property writes are wrapped in runCatching behind an os.name guard. Koin registrations are appended at the end of each layer group as the plan required. Error handling follows the repo pattern: both new use cases wrap in resultLauncher with Failure.Technical::Preference, and both failure paths in RectifyWindow log and continue so a dead preference store cannot stop the window opening.
- Performance: the plan's five budgets all hold. Panel resize is pure Dp arithmetic with only the panel's width modifier invalidated; writes during a drag are provably zero (four distinct ShellStateTest no-change cases); the stage texture precomputes its step and allocates nothing per dot; the width read is in LaunchedEffect(Unit) after first composition so the window paints without waiting on I/O; the test suite ran in ~5 seconds of wall clock. One forward-looking note only: ~3,500 drawCircle ops per draw pass under what will become a 60fps video surface. AF-16.
- Dead code: none in the conventional sense — no unused imports, no orphaned functions, no commented-out blocks, no TODO/FIXME/HACK markers anywhere in 45 files. The closest thing is the dispatch path in handleShellShortcut, which is live code that cannot currently execute; that is deliberate forward wiring for the downstream spec, not dead code, but it should be understood as untested forward wiring rather than working behaviour. AF-4.
- Pattern consistency: good. Package layout follows the core/ vs source/ rule with no empty core/ directories created; test packages mirror production packages; the new preference follows the full api -> impl -> mapper -> repository -> use case chain in the documented order; KDoc is present and genuinely informative on every public declaration (several comments explain a non-obvious decision rather than restating the signature — the rememberUpdatedState note in PanelResizeHandle and the private-backing-field note in ShellState are both real).
- Scope discipline: clean. No adb/idb/device discovery, no annotation/measure/payload/history implementation, no MCP, no iOS work, no new dependencies beyond the three test-only catalog entries. DEMO_MODE remains false. The two logged dispatch deviations (T-1/T-2 not deferred to a terminal wave; T-4..T-7 batched into one worktree) are both reasoned and both produced correct results — T-1 genuinely had to run first, and the T-4..T-7 batch produced a coherent organism with no visible seams between the atom, the regions and the assembling shell. AF-18.
- One behavioural change deserves a conscious human decision even though it is formally in scope: desktop no longer reaches splash, onboarding, home, settings or about, because runDesktopApp() swapped Window { App() } for RectifyWindow(). plan scope.out[10] names this and deliberately kept it off the survey the human answered. AF-12.

### registryConsistency

#### consistent

true

#### checked

false

#### reason

.pipeline/sdd/docs/registry/ does not exist (verified by direct ls). plan scope.out[12] forbids any write to it. No consistency check is possible and none is required.

#### phantomEntries


#### missingEntries


#### issues


### traceability

| ref | verificationMethod | satisfied | evidence |
| --- | --- | --- | --- |
| AC-12 | test + inspection | true | ShellTabTest 3/3 green; three tabs visible over the bezel-less stage in both session-active evidence renders. |
| AC-30 | inspection | true | Light+dark renders for both panel states; PaletteContrastTest 5/5 green under Q1-A/Q2-A. Waivers documented (AF-7, AF-8). |
| AC-31 | inspection | true | Nullable-slot guard verified at RectifyWindowShell.kt:71; no-session renders show neither panel nor handle. Design-match clause carved out by the plan (AF-9). |
| AC-32 | test + inspection | true | ShellStateTest 11/11 green (default Notes, onSessionStarted resets tab and preserves width); Notes active in the session-active render. |
| AC-33 | test | false | Mapping green (ShortcutToTabTest 3/3); dispatch untested and unreachable (AF-4, AF-5). |
| AC-34 | test + inspection | false | Constants green (ShellWindowDefaultsTest 5/5) and 1240x820 confirmed in the renders; resize floor and traffic-light inset never observed (AF-11). |
| AC-35 | test | true | ThemePreferenceMapperTest 6/6 green covering all five mapping cases plus the materialu round-trip; App.kt defaults flipped to System. Inspection half open, cycle function untested (AF-5). |
| AC-36 | inspection | true | Empty commonMain diff over presentation-core-styling confirmed; zero Color(0x / TextStyle( / raw call-site .dp; sweep green. primaryHover/primaryPress unswept (AF-6). |
| AC-37 | inspection | true | All eight slots stub-filled from shipped components in inert states; named-parameter slots prevent reordering; zero adb/idb references; toolbar order matches comp 02. |
| AC-38 | test | true | ClampPanelWidthTest 4/4 and ShellStateTest 11/11 green; bounds inclusive at 260/480. |
| AC-39 | test | true | SidePanelWidthPreferenceSourceTest 5/5 green including the fresh-source relaunch proxy and an explicit pref_side_panel_width key assertion; commit-only-on-change proven by four ShellStateTest cases. |
| AC-40 | inspection | true | Satisfied-with-waiver under Q1-A: dark 16/16 hard-asserted and passing; light waives eight pairings. Screenshots paint no signal token (independently confirmed) so PaletteContrastTest is the only real coverage (AF-8). |
| NFR-9 / NFR-11 | test | true | PaletteContrastTest both palettes, 54 pairings each, 5/5 green; A11Y-4/A11Y-5 tables carried as checked-in baselines; both WCAG exemptions encoded by name. See AF-6, AF-7, AF-10. |
| NFR-10 | inspection | true | macOS client properties behind isMacOs() + runCatching with a logged fallback; only :desktop-application produces a runnable current-OS artifact. |
| FR-11 | test | true | ShellTab enum + ShellTabTest + SidePanelFrame rendering ShellTab.entries through SidebarTabs, so the tab set cannot drift from the enum the shortcuts and state holder share. |
| FR-12 | inspection | true | ShellToolbar.kt six named slots in canonical order with a leadingInset parameter. |
| FR-13 | inspection | true | ShellStatusBar.kt leading/trailing slots, 28dp, stub-filled to comp 02's neutral waiting state. |
| FR-14 | inspection | true | StageSurface.kt — no border, no elevation, no corner radius, Theme.color.stage ground plus a Canvas dot grid and a BoxScope content slot. |
| FR-15 | inspection | true | Nullable sidePanel slot in RectifyWindowShell; SidePanelFrame holds nothing and takes activeTab in / onTabSelect out. |
| FR-16 | test | false | shortcutToTab is pure and fully tested; the onPreviewKeyEvent dispatch it feeds is untested and unreachable (AF-4). |
| FR-17 | test + inspection | false | Constants test-pinned; live AWT enforcement never observed (AF-11). |
| FR-18 | test | true | The one-line ThemePreferenceMapper fallback flip is what actually makes DEFAULT_THEME "system" resolve to System; ThemePreferenceMapperTest pins it. |
| FR-19 | test + inspection | true | Tokens read only through the Theme singleton; both palettes swept; AppTheme is the single provider in every preview and in RectifyWindow. |
| FR-20 | test | true | clampPanelWidth + ShellState.resizePanelBy, both fully covered. |
| FR-21 | test | true | Full pref_side_panel_width chain present, Koin-registered and round-trip tested including the relaunch proxy. |
| FR-23 | inspection | true | Zero Color(0x, zero TextStyle(, zero raw .dp at any call site across all new production files. |

### verdict

#### summary

The engineering is sound. All 13 tasks are genuinely done against real files, all 49 tests are genuinely green in XML I read myself, scope discipline is clean, token discipline is clean, and the one deliberate technical deviation (T-2's hairline contrast downgrade) is correct and honestly documented. The real defects are documentation debt the spec created and did not clean up, plus two ACs whose live-inspection half was never performed because the environment denied screen access.

#### blocking

- AF-1
- AF-2

#### needsGateDecision

- AF-7
- AF-8
- AF-9
- AF-4
- AF-11
- AF-12

#### recommendation

Do not close yet. AF-1 and AF-2 are cheap, provable doc fixes that should land before the spec closes under the orchestrator's own stale-doc policy. AF-4, AF-11 and AF-12 need a human decision, not more code. AF-6 and AF-10 are small, well-scoped follow-ups.
