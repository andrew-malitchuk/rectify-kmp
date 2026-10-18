# measure-mode — T-1

## Envelope

| field | value |
| --- | --- |
| stage | execution |
| step | T-1 |
| status | done |
| producedBy | sdd-build-business-implement |
| schemaVersion | 1 |
| createdAt | 2026-10-16T21:32:31Z |
| updatedAt | 2026-10-16T21:32:31Z |

## Gate

### type

auto

### approvedBy

sdd-build-business-implement

### approved

true

### at

2026-10-16T21:32:31Z

## Provenance

### selfHash

de4b9988338d6ec2e50b9b7eaffebd2edfcec7dd63ef691b3e39bcc49f3d16a3

### inputs

| artifact | hash |
| --- | --- |
| backlog/backlog.json | 6f849f4416e8adcf94559bb05055afe9181ce0fe26bbcf6da2d9619f09dab0d8 |

## Data

### taskId

T-1

### type

business-logic

### outcome

success

### touchedPaths

- data-device-api/src/commonTest/kotlin/data/device/api/UiAutomatorDumpFixtures.kt

### tdd

#### cycle

red-green-refactor

#### testsFrozen

true

#### testsWritten

- data-device-api/src/commonTest/kotlin/data/device/api/UiAutomatorDumpFixtures.kt::UiAutomatorDumpFixturesTest::every fixture is non-empty and still the exact byte size the device produced
- data-device-api/src/commonTest/kotlin/data/device/api/UiAutomatorDumpFixtures.kt::UiAutomatorDumpFixturesTest::every fixture is one well-formed hierarchy document followed only by the adb trailer

### testRun

#### command

./gradlew :data-device-api:desktopTest && ./gradlew :data-device-api:compileTestKotlinIosArm64

#### passed

true

#### output

```
$ ./gradlew :data-device-api:desktopTest
> Task :data-device-api:compileTestKotlinDesktop
> Task :data-device-api:compileDesktopTestJava NO-SOURCE
> Task :data-device-api:desktopTestClasses
> Task :data-device-api:desktopTest
BUILD SUCCESSFUL in 11s
12 actionable tasks: 9 executed, 3 from cache

TEST-data.device.api.UiAutomatorDumpFixturesTest.xml: tests="2" skipped="0" failures="0" errors="0"
  every fixture is non-empty and still the exact byte size the device produced[desktop]
  every fixture is one well-formed hierarchy document followed only by the adb trailer[desktop]

$ ./gradlew :data-device-api:compileTestKotlinIosArm64
BUILD SUCCESSFUL in 4s
12 actionable tasks: 6 executed, 2 from cache, 4 up-to-date

```

### implementation

#### filesChanged

- data-device-api/src/commonTest/kotlin/data/device/api/UiAutomatorDumpFixtures.kt

#### summary

Hardware spike, no production code by design. Captured three real `adb exec-out uiautomator dump /dev/tty` payloads from the attached Pixel 8 (shiba, Android 16 / API 36) and committed them verbatim as Kotlin raw-string constants in one new commonTest fixtures file, matching this module's inline-literal convention (PngTrimTest / WmOutputParserTest); no src/commonTest/resources exists in this repo and none was introduced. Captures: (1) VIEW_BASED_SETTINGS_DUMP, Settings homepage, View-based, 2938 ms, 26645 B, 69 nodes, 57 with resource-id (83%); (2) COMPOSE_APP_DUMP, dev.duotone.kmp.demo Compose Multiplatform, 2317 ms, 6142 B, 17 nodes, 1 resource-id (6%); (3) MID_SCROLL_LIST_DUMP, Settings all-apps list captured mid-drag 1.0 s into a held 5 s input swipe, 2711 ms, 28070 B, 79 nodes. Mean 2.66 s versus the plan's 2665 ms screencap baseline, so the ~2.6 s assumption holds and dump-on-keypress stays off the table. wm density = 420 physical with no override (2.625 px/dp); wm size = physical 1080x2400, override 984x2132. All three root nodes report bounds [0,0][984,2132], matching the override size exactly and the physical size only to within 9.76%, so dump bounds must be paired with the override line -- which the existing parseWmSize already prefers. No dump was empty or truncated. Two tests pin each fixture's exact byte size and its single well-formed hierarchy document plus the trailing non-XML adb trailer.

#### reusedFromRegistry


### troubleshooting

| at | issue | resolution | severity |
| --- | --- | --- | --- |
| capture / parser contract | uiautomator stdout is not XML: every dump ends '</hierarchy>UI hierchary dumped to: /dev/tty\n' -- a non-XML trailer glued to the closing tag, carrying AOSP's own misspelling 'hierchary'. Feeding raw stdout to an XML parser fails on trailing content, and matching the correctly spelled word will not find it. | Captured verbatim rather than cleaned, pinned as UIAUTOMATOR_DUMP_TRAILER and asserted in UiAutomatorDumpFixturesTest. T-3's parser must cut at the end of </hierarchy>. | warn |
| capture / bounds arithmetic | A vanilla Settings dump contains three inverted rectangles on the partially scrolled-off 'Wallpaper & style' row -- [42,2083][189,2069] h=-14, [84,2083][189,2069] h=-14, [221,2080][568,2069] h=-11. bottom-top yields negative sizes on the very first screen dumped, not on an exotic edge case. | Preserved in VIEW_BASED_SETTINGS_DUMP so the measure arithmetic in E-3 has a real negative-height case to clamp against. Recorded in the spike note for the T-2 call. | warn |
| capture / node identification | resource-id is effectively absent on Compose: 83% coverage on the View-based app versus 6% on the Compose app, where the single hit is only android:id/content. Compose nodes are anonymous android.view.View, so class is no help either. Settings' own all-apps list is itself Compose (SpaActivity) on Android 16, so 'View-based app' is a shrinking target. | Both shapes committed side by side as the contrast evidence. Flagged for T-2: any downstream task keyed on resource-id as the node identity must move to text / content-desc. | warn |
| capture / display metrics | This device has an active display-size override (physical 1080x2400, override 984x2132) while wm density reports no override. uiautomator reports the override resolution, so pairing dump bounds with the physical size is 9.76% wrong on every number. Dump px / 2.625 does give the dp the app laid out at, but a dp here is not a physical dp -- the 984 px surface is upscaled onto a 1080 px panel. | Verified the existing data-device-api parseWmSize already prefers the override line, so no change needed. Recorded in the spike note with an explicit 'do not invert this' note. | info |
| fixtures encoding | Byte fidelity risk in raw strings. Checked before committing: no payload contains '$', a triple quote, a backslash or a CR, so zero escaping was needed. The Settings dump does contain U+2011 non-breaking hyphen, and the XML declaration uses single quotes (<?xml version='1.0'), so a matcher expecting double quotes would fail. | Fixtures generated programmatically from the captured bytes, never hand-typed, and each dump's exact byte size (26645 / 6142 / 28070) is pinned via encodeToByteArray().size so any re-encoding or stray review edit fails the build. | info |
| mid-scroll capture method | A fast swipe plus an immediate dump cannot be trusted to land mid-scroll, because exec-out takes ~2.7 s and the fling settles first. | Held a 5 s input swipe and dumped 1.0 s in, with the finger still down. Confirmed genuinely mid-drag against a settled capture of the same screen: the mid-drag dump's topmost label is a viewport-clipped row ('133 MB' sitting exactly on the scroll container top edge at y=264), while the settled capture started a full row lower. The settled control capture (2591 ms, 27682 B) was deliberately not committed -- it adds no shape the three fixtures lack. | info |
| expectedOutput path | The backlog's expectedOutput named .../data/device/api/fixtures/UiAutomatorDumpFixtures.kt, but every existing test in this module lives flat in data/device/api/ with no fixtures/ subpackage. | Written flat at data-device-api/src/commonTest/kotlin/data/device/api/UiAutomatorDumpFixtures.kt, matching the module's actual layout and the task brief. | info |

### needsHumanReview

#### required

true

#### reason

By design -- T-2 is the operator's go/no-go gate on this spike's findings, and nothing downstream should start until it is made. The spike itself PASSED: dumps work, cost 2.3-2.9 s as assumed, bounds agree with the override size. Three findings the plan did not anticipate need the operator's call: the non-XML 'hierchary' trailer every dump carries, inverted rectangles with negative height in a vanilla Settings dump, and resource-id being 6% present on Compose versus 83% on View -- the last with real design consequences for any downstream task that assumed resource-id as the node key. See .pipeline/sdd/docs/measure-mode/execution/T-1/spike-note.md.

### linkedAC

- AC-57
- AC-58
- AC-65
- AC-69

### blockedDependents


### registryUpdates


### validations

| stage | passed | iterations | issues |
| --- | --- | --- | --- |
| desktopTest | true | 1 | [] |
| compileTestKotlinIosArm64 | true | 1 | [] |
