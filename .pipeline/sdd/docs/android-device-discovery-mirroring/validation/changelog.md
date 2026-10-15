# android-device-discovery-mirroring — changelog

## Envelope

| field | value |
| --- | --- |
| stage | validation |
| step | changelog |
| status | done |
| producedBy | sdd-build-validation-verdict |
| schemaVersion | 1 |
| createdAt | 2026-10-15T02:25:55Z |
| updatedAt | 2026-10-15T02:25:55Z |

## Gate

### type

auto

### approvedBy

sdd-build-validation-verdict

### approved

true

### at

2026-10-15T02:25:55Z

## Provenance

### selfHash

02a22780646c20209ae33a696f526fca4470508ec072c3eba5393bd755fef32f

### inputs

| artifact | hash |
| --- | --- |
| validation/verdict.json | 6f2f0e1caf29e1d1f5e3b0c8c611807f08a754bda8eccaf2fd35c12a139bbacb |

## Data

### spec

android-device-discovery-mirroring

### entries

| type | summary |
| --- | --- |
| added | Three new modules: data-device-api (contracts, resources and every pure parser in commonMain - the placement that makes the feature testable with no phone), data-device-impl (the repo's only ProcessBuilder, internal and desktopMain only), presentation-feature-mirror (Orbit MVI, entry point MirrorHost). |
| added | Real Android device discovery: ordered adb probe across $ANDROID_HOME, $ANDROID_SDK_ROOT, ~/Library/Android/sdk, Homebrew and PATH (PATH alone fails for a Finder-launched .app), 2s polling with empty-poll escalation, AVD de-duplication, and a device picker with keyboard navigation. |
| added | Screenshot-polling live mirror behind a MirrorTransport interface: adaptive single-flight loop, 100ms floor, 5s timeout, dropped-frame accounting. |
| added | Failure.Technical.Process with DeviceCommandException(argv, exitCode, stderrTail). A missing binary at detection time is a DiscoveryModel.ToolMissing SUCCESS value, not an error, so F-3 can render it. |
| added | 113 new tests. Repo total 152 desktop plus the iosSimulatorArm64 runs. |
| fixed | AF-1: the transport reported its host-side capture round-trip as latencyMs. Now a separate captureRoundTripMs field, latencyMs stays null, and StatsFormatter's placeholder branch is the production path. |
| fixed | AF-2: the iOS tool dot was a hardcoded constant. Now a real idb probe through IdbHostSource; the false comment claiming iOS was probed is now true. |
| fixed | AF-3: Key.Escape existed nowhere in the repo. Escape now closes the picker when a device is already selected, with a matching visible Close affordance. |

### regressionRunPassed

true
