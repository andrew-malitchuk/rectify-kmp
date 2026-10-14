# app-shell-window-layout — T-2/report

## Envelope

| field | value |
| --- | --- |
| stage | execution |
| step | T-2/report |
| status | done |
| producedBy | sdd-build-business-implement |
| schemaVersion | 1 |
| createdAt | 2026-10-14T20:53:18Z |
| updatedAt | 2026-10-14T20:53:35Z |

## Gate

### type

auto

### approvedBy

sdd-build-business-implement

### approved

true

### at

2026-10-14T20:53:35Z

## Provenance

### selfHash

26a3fe18c3aba9bb5cc7050df2add98617cd48afeac2abf6fc5f563e55b75681

### inputs

| artifact | hash |
| --- | --- |
| backlog/backlog.json | 0626dbba1debc8671ab3d626c7c2308eb38fe0d5ee43282ab82f0fbd86331a0e |

## Data

### taskId

T-2

### type

test

### outcome

success

### touchedPaths

- presentation-core-styling/src/commonTest/kotlin/presentation/core/styling/contrast/WcagContrast.kt
- presentation-core-styling/src/commonTest/kotlin/presentation/core/styling/contrast/PaletteContrastTest.kt

### testRun

#### command

./gradlew :presentation-core-styling:desktopTest

#### passed

true

#### output

PaletteContrastTest[desktop] tests=5 skipped=0 failures=0 errors=0. Proof the sweep bites: first run went RED on 'dark: seg over surface regressed to 1.18:1 from baseline 1.2:1', fixed by recording the real measured value, not by loosening the assertion. Post-merge re-run green.

### implementation

#### filesChanged

- WcagContrast.kt
- PaletteContrastTest.kt

#### summary

54 explicitly named pairings per palette (108 total). Tier 1 hard-fails ink/ink2 x4 surfaces and onPrimary at 4.5:1 and focus x4 at 3:1, plus ink3 and the signals at 3:1 wherever they clear it. Everything else is a frozen baseline with an inline // WAIVED: comment naming the ratio and the AC. Alpha tokens (line, line2, hover, seg, codeBg) composited over all four surfaces using androidx.compose.ui.graphics.compositeOver - the exact path the renderer uses - rather than a re-derived helper. pop measured against a Color.White literal per CounterBadge precedent; no onPop token added. WCAG 1.4.3 inactive-component and 1.4.11 decorative-graphic exemptions named in KDoc. ZERO production diff.

#### reusedFromRegistry

| registryRef | how |
| --- | --- |
| presentation-core-styling#attributeLight/DarkColorPalette | Read unmodified from commonTest via the KMP main/test friend relationship - the reason the sweep must live in this module |
| presentation-core-ui#atom/badge/CounterBadge | Followed its documented Color.White-on-pop precedent instead of introducing an onPop token |

### troubleshooting

| at | issue | resolution | severity |
| --- | --- | --- | --- |
| WcagContrast.kt | No custom compositeOver written: androidx.compose.ui.graphics.compositeOver is what the renderer actually uses, so re-deriving it would measure something never drawn. | See issue text; handled in-task or routed onward. | info |
| PaletteContrastTest.kt | Compose packs sRGB Color at 8 bits/channel, so composited results quantise. Float-exact maths and the Kotlin measurement diverge by up to 0.0082 on the 40 composited pairings (worst: dark seg-over-surface 1.1980 vs 1.1898). Baselines for those use the real Kotlin measurements; all 68 non-composited pairings matched the orchestrator's independent computation exactly. | See issue text; handled in-task or routed onward. | info |

### needsHumanReview

#### required

true

#### reason

Tier 1 as briefed is UNSATISFIABLE for composited line/line2 at 3:1. Independently re-verified by the orchestrator: an 8%/14% alpha hairline measures 1.19-1.57:1 over every surface in BOTH palettes (light line 1.19-1.20, line2 1.37-1.38; dark line 1.24-1.28, line2 1.51-1.57). Reaching 3:1 would require editing tokens AC-36 freezes. Recorded as frozen baselines with WAIVED comments and a DEVIATION note in the test KDoc rather than asserted into a permanently red suite. NEEDS A STAGE 5 GATE DECISION: accept the downgrade for hairlines (defensible - a subtle divider is arguably decorative under WCAG 1.4.11), or re-open AC-36 for the line tokens.

### linkedAC

- AC-30
- AC-36
- AC-40
- NFR-9
- NFR-11
