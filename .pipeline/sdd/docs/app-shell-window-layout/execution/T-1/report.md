# app-shell-window-layout — T-1/report

## Envelope

| field | value |
| --- | --- |
| stage | execution |
| step | T-1/report |
| status | done |
| producedBy | sdd-build-business-implement |
| schemaVersion | 1 |
| createdAt | 2026-10-14T20:38:18Z |
| updatedAt | 2026-10-14T20:38:18Z |

## Gate

### type

auto

### approvedBy

sdd-build-business-implement

### approved

true

### at

2026-10-14T20:38:18Z

## Provenance

### selfHash

df3ce91e72efe799324118df7dd83938cf9939ab15c29de3043880176e643a32

### inputs

| artifact | hash |
| --- | --- |
| backlog/backlog.json | 0626dbba1debc8671ab3d626c7c2308eb38fe0d5ee43282ab82f0fbd86331a0e |

## Data

### taskId

T-1

### type

test

### outcome

success

### touchedPaths

- gradle/libs.versions.toml
- build-logic/convention/src/main/kotlin/dev/rectify/io/convention/source/convention/library/LibraryConventionPlugin.kt
- build-logic/convention/src/main/kotlin/dev/rectify/io/convention/source/convention/feature/FeatureConventionPlugin.kt

### testRun

#### command

./gradlew :build-logic:convention:assemble ; :presentation-core-ui:desktopTest :presentation-core-styling:desktopTest :presentation-feature-home:desktopTest ; :data-preference-impl:desktopTest :data-repository-impl:desktopTest

#### passed

true

#### output

BUILD SUCCESSFUL (17s / 14s / 3s). JUnit XML for all 5 probe modules: tests=1 skipped=0 failures=0 errors=0. Orchestrator re-verified independently after the agent deleted its throwaway tests: fresh probe in presentation-core-styling commonTest, --rerun-tasks, BUILD SUCCESSFUL in 4s, TEST-OrchestratorProbe.xml reports tests=1 skipped=0 failures=0 errors=0 (guards against a vacuous NO-SOURCE green). Probe then removed; worktree clean at merge.

### implementation

#### filesChanged

- gradle/libs.versions.toml
- LibraryConventionPlugin.kt
- FeatureConventionPlugin.kt

#### summary

First test infrastructure in the repo. One catalog entry (kotlin-test, reusing the existing kotlin=2.4.21 version ref) wired as a commonTest implementation dependency inside the EXISTING configureCommonDependencies() of the library and feature convention plugins. No new BaseConventionPlugin hook. commonTest now compiles and runs repo-wide for every KMP library/feature module.

#### reusedFromRegistry

| registryRef | how |
| --- | --- |
| build-logic/convention#BaseConventionPlugin.configureCommonDependencies | Extended the existing hook rather than adding a ninth template-method hook, per .claude/rules/convention-plugins.md |
| gradle/libs.versions.toml#versions.kotlin | Reused the existing kotlin=2.4.21 version ref for the kotlin-test coordinate |

### troubleshooting


### needsHumanReview

#### required

false

#### reason



### linkedAC

- TAC-1
- TAC-2
- TAC-3
- TAC-4

### notes

- kotlin-test resolves its desktop/JVM variant automatically; no per-target kotlin-test-junit entry needed, so downstream tasks can write plain kotlin.test assertions in commonTest.
- junit = 4.13.2 remains declared-but-unused in the catalog. Out of scope here; now the only dead test-related pin and a candidate for a later cleanup per version-catalog-hygiene.md.
