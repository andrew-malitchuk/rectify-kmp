# app-shell-window-layout — T-8/report

## Envelope

| field | value |
| --- | --- |
| stage | execution |
| step | T-8/report |
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

3f1972004b187fb1019dd48deb9642a5a84d4fac5df483ecbd23fb841e2c173f

### inputs

| artifact | hash |
| --- | --- |
| backlog/backlog.json | 0626dbba1debc8671ab3d626c7c2308eb38fe0d5ee43282ab82f0fbd86331a0e |

## Data

### taskId

T-8

### type

business-logic

### outcome

success

### touchedPaths

- data-preference-api/src/commonMain/kotlin/data/preference/api/source/model/SidePanelWidthPreference.kt
- data-preference-api/src/commonMain/kotlin/data/preference/api/source/datasource/SidePanelWidthPreferenceSource.kt
- data-preference-impl/src/commonMain/kotlin/data/preference/impl/source/datasource/SidePanelWidthPreferenceSourceImpl.kt
- data-preference-impl/src/commonMain/kotlin/data/preference/impl/di/DataPreferenceImplModule.kt
- data-preference-impl/build.gradle.kts
- data-preference-impl/src/commonTest/kotlin/data/preference/impl/SidePanelWidthPreferenceSourceTest.kt
- data-repository-impl/src/commonMain/kotlin/data/repository/impl/core/mapper/SidePanelWidthPreferenceMapper.kt
- data-repository-impl/src/commonMain/kotlin/data/repository/impl/source/ConfigureRepositoryImpl.kt
- data-repository-impl/src/commonTest/kotlin/data/repository/impl/SidePanelWidthPreferenceMapperTest.kt
- domain-repository-api/src/commonMain/kotlin/domain/repository/api/source/repository/ConfigureRepository.kt
- domain-usecase-api/src/commonMain/kotlin/domain/usecase/api/source/usecase/configuration/GetSidePanelWidthUseCase.kt
- domain-usecase-api/src/commonMain/kotlin/domain/usecase/api/source/usecase/configuration/SetSidePanelWidthUseCase.kt
- domain-usecase-impl/src/commonMain/kotlin/domain/usecase/impl/source/usecase/configuration/GetSidePanelWidthUseCaseImpl.kt
- domain-usecase-impl/src/commonMain/kotlin/domain/usecase/impl/source/usecase/configuration/SetSidePanelWidthUseCaseImpl.kt
- domain-usecase-impl/src/commonMain/kotlin/domain/usecase/impl/di/DomainUseCaseImplModule.kt
- gradle/libs.versions.toml

### testRun

#### command

./gradlew :data-preference-impl:desktopTest :data-repository-impl:desktopTest

#### passed

true

#### output

SidePanelWidthPreferenceSourceTest tests=5 f=0 e=0; SidePanelWidthPreferenceMapperTest tests=2 f=0 e=0. Also green: :domain-usecase-impl:compileKotlinDesktop, :compose-application:compileKotlinDesktop, :desktop-application:compileKotlin.

### implementation

#### filesChanged

- see touchedPaths

#### summary

New pref_side_panel_width (Int dp, default 320) mirroring the pref_theme chain file-for-file: Resource + typed PreferenceSource, SourceImpl with MutableStateFlow seeded from settings.getIntOrNull, Koin binding appended to dataPreferenceImplModule, ModelResourceMapper<Int,SidePanelWidthPreference>, getSidePanelWidth/setSidePanelWidth appended to ConfigureRepository(+Impl), Get/SetSidePanelWidthUseCase(+Impl) via resultLauncher(Failure.Technical::Preference), bindings appended to domainUseCaseImplModule. Plain Int model so no new domain model class. No observe* variant. No clamping - T-3 owns it. pref_theme's key untouched.

#### reusedFromRegistry

| registryRef | how |
| --- | --- |
| data-preference-impl#ThemePreferenceSourceImpl | Copied verbatim as the SourceImpl shape (StateFlow seeding, getData re-read, const val KEY_*) |
| data-repository-impl#LanguagePreferenceMapper | Precedent for a plain (non-Model) domain type in a mapper |
| domain-usecase-impl#resultLauncher | Error wrapping for both use cases; no new error plumbing |

### troubleshooting

| at | issue | resolution | severity |
| --- | --- | --- | --- |
| data-preference-impl/build.gradle.kts | commonTest needed kotlinx-coroutines-test for runTest (suspend getData/setData); added alongside multiplatform-settings-test, both wired into data-preference-impl commonTest in the same change per version-catalog-hygiene.md. | See issue text; handled in-task or routed onward. | info |

### needsHumanReview

#### required

false

#### reason



### linkedAC

- AC-39
