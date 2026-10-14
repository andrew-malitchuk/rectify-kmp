# app-shell-window-layout — T-6/report

## Envelope

| field | value |
| --- | --- |
| stage | execution |
| step | T-6/report |
| status | done |
| producedBy | sdd-build-frontend-implement |
| schemaVersion | 1 |
| createdAt | 2026-10-14T21:07:40Z |
| updatedAt | 2026-10-14T21:07:40Z |

## Gate

### type

auto

### approvedBy

sdd-build-frontend-implement

### approved

true

### at

2026-10-14T21:07:40Z

## Provenance

### selfHash

98da5f1814d0c5ba461b8ecf170b661f522ee5d3b14053f445f681e83e24d9ac

### inputs

| artifact | hash |
| --- | --- |
| backlog/backlog.json | 0626dbba1debc8671ab3d626c7c2308eb38fe0d5ee43282ab82f0fbd86331a0e |

## Data

### taskId

T-6

### type

ui

### outcome

success

### touchedPaths

- presentation-core-ui/src/commonMain/kotlin/presentation/core/ui/source/kit/organism/shell/SidePanelFrame.kt

### testRun

#### command

./gradlew :presentation-core-ui:desktopTest

#### passed

true

#### output

26 tests, 0 failures; preview renders Notes / Payload / History with Notes selected.

### implementation

#### filesChanged

- SidePanelFrame.kt

#### summary

Row[leading 1dp line hairline, Column[40dp header hosting SidebarTabs, hairline divider, content(activeTab)]] on Theme.color.surface. Tabs are built from ShellTab.entries so the rendered set CANNOT drift from the enum that shortcutToTab and ShellState already share - a single source of truth for AC-12's 'exactly three'. selectedIndex = activeTab.ordinal; onSelect(index) maps back through ShellTab.entries. Optional tabCounts feeds SidebarTabItem.count.

#### reusedFromRegistry

| registryRef | how |
| --- | --- |
| presentation-core-ui#molecule/bar/tab/SidebarTabs | The entire three-tab header; no tab-rendering code authored |
| presentation-core-ui#organism/shell/ShellTab (T-3) | Labels Notes/Payload/History, order and default |

#### authoredNew

- SidePanelFrame - the frame wrapper only

### troubleshooting

| at | issue | resolution | severity |
| --- | --- | --- | --- |
| SidePanelFrame.kt | SidebarTabs' KDoc example says 'Annotations / Payload / History'; the shell renders Notes / Payload / History per FR-11 and AC-12. The KDoc wording was not propagated and the shipped molecule was NOT edited. | Handled in-task; see issue text. | info |

### needsHumanReview

#### required

true

#### reason

No comp exists for the side panel - neither cached screenshot shows it (both are pre-mirror states). PanelHeaderHeight=40dp is derived from SidebarTabs' 26dp tab height plus padding, not copied from a design. Flagged in the file KDoc; this is Stage 1's carried-forward open question about panel geometry.

### linkedAC

- AC-12
- AC-32
