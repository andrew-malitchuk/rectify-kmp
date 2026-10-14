# app-shell-window-layout — T-9/report

## Envelope

| field | value |
| --- | --- |
| stage | execution |
| step | T-9/report |
| status | done |
| producedBy | sdd-build-business-implement |
| schemaVersion | 1 |
| createdAt | 2026-10-14T20:53:19Z |
| updatedAt | 2026-10-14T20:53:36Z |

## Gate

### type

auto

### approvedBy

sdd-build-business-implement

### approved

true

### at

2026-10-14T20:53:36Z

## Provenance

### selfHash

12c5f13b168f16a62e7f9987353bc9ae6c580c20e8054d87df0c289286feb800

### inputs

| artifact | hash |
| --- | --- |
| backlog/backlog.json | 0626dbba1debc8671ab3d626c7c2308eb38fe0d5ee43282ab82f0fbd86331a0e |

## Data

### taskId

T-9

### type

business-logic

### outcome

success

### touchedPaths

- domain-core/src/commonMain/kotlin/domain/core/source/model/ThemeModel.kt
- data-repository-impl/src/commonMain/kotlin/data/repository/impl/core/mapper/ThemePreferenceMapper.kt
- data-repository-impl/src/commonTest/kotlin/data/repository/impl/ThemePreferenceMapperTest.kt
- compose-application/src/commonMain/kotlin/dev/rectify/io/source/app/App.kt

### testRun

#### command

./gradlew :data-repository-impl:desktopTest

#### passed

true

#### output

ThemePreferenceMapperTest tests=6 f=0 e=0 (light->Light, dark->Dark, materialu->System, system->System, garbage/empty->System, toResource(System).theme=="materialu")

### implementation

#### filesChanged

- ThemeModel.kt
- ThemePreferenceMapper.kt
- ThemePreferenceMapperTest.kt
- App.kt

#### summary

ThemeModel.MaterialU renamed to ThemeModel.System with its STORED value kept as "materialu" (no orphaned preference, per preferences.md). ThemePreferenceMapper.toModel fallback flipped Light -> System, which is what actually makes the already-shipped ThemePreference.DEFAULT_THEME ("system") resolve to system appearance - this one line is the real FR-18 delivery. App.kt when-branch renamed; null branch and collectAsState(initial) moved from ThemeMode.Light to ThemeMode.System. No new key; system-appearance detection NOT re-implemented (AppTheme already does it via isSystemInDarkTheme()).

#### reusedFromRegistry

| registryRef | how |
| --- | --- |
| presentation-core-styling#AppTheme/ThemeMode | ThemeMode.System already resolves via isSystemInDarkTheme(); nothing added |

### troubleshooting

| at | issue | resolution | severity |
| --- | --- | --- | --- |
| ThemePreferenceMapper.kt | ThemePreferenceMapper's KDoc was already wrong before this change (claimed 'Falls back to null' while the code fell back to ThemeModel.Light). Fixed in passing. | See issue text; handled in-task or routed onward. | info |
| .claude/rules/mappers.md | STALE DOC INTRODUCED BY THIS TASK: .claude/rules/mappers.md line 18 documents ThemePreferenceMapper.toModel as defaulting to ThemeModel.Light. Now wrong. Executor correctly did not edit a project rules file on its own authority; orchestrator routed it to the terminal wave. | See issue text; handled in-task or routed onward. | warn |
| App.kt | App.kt calls AppTheme(mode = themeMode) single-arg today, not the two-arg AppTheme(mode, reducedMotion) the brief described. Call site left alone. | See issue text; handled in-task or routed onward. | info |

### needsHumanReview

#### required

false

#### reason



### linkedAC

- AC-35
