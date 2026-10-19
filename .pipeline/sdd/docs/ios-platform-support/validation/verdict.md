# ios-platform-support — verdict

## Envelope

| field | value |
| --- | --- |
| stage | validation |
| step | verdict |
| status | done |
| producedBy | sdd-build-validation-verdict |
| schemaVersion | 1 |
| createdAt | 2026-10-19T06:03:55Z |
| updatedAt | 2026-10-19T06:03:55Z |

## Gate

### type

human

### approvedBy

orchestrator-autonomous

### approved

true

### at

2026-10-19T06:03:55Z

## Provenance

### selfHash

6a6b2cb66225c7ce784d68d8a469d0f8cc1bc564f5b477de166fd4a3e13dc869

### inputs

| artifact | hash |
| --- | --- |
| validation/audit.json | ad6982ad7d985b9649e114117ff26cf8730b17fe3c0485b570d97e493c2605bf |
| plan/plan.json | 69edd9e6f6f5eca6b6ef71fc602ede32817013db2ad2ab98db859ce0f36f07d0 |

## Data

### verdict

complete-with-waivers

### tasks

| id | status | satisfiesAC |
| --- | --- | --- |
| T-1 | done | ["AC-46", "AC-31"] |
| T-2 | done | ["AC-49"] |
| T-3 | done | ["AC-49"] |
| T-4 | done | ["AC-49"] |
| T-5 | done | ["AC-48"] |
| T-6 | done | ["AC-48"] |
| T-7 | done | ["AC-48"] |
| T-8 | done | ["AC-46", "AC-31"] |
| T-9 | done | ["AC-46", "AC-31"] |
| T-10 | done | ["AC-46"] |
| T-11 | done | ["AC-46"] |
| T-12 | done | ["AC-47", "AC-48"] |
| T-13 | done | ["AC-48", "AC-49"] |
| T-14 | done | ["AC-48", "AC-49"] |
| T-15 | done | ["AC-46", "AC-31"] |
| T-16 | done | ["AC-46", "AC-49", "AC-50"] |
| T-17 | done | ["AC-46", "AC-50", "AC-31"] |
| T-18 | done | ["AC-31", "AC-46"] |
| T-19 | done | ["AC-31", "AC-46"] |
| T-20 | done | ["AC-31", "AC-46", "AC-47"] |
| T-21 | done | ["AC-31", "AC-46", "AC-47"] |
| T-22 | done | ["AC-31"] |
| T-23 | done | ["AC-46", "AC-31"] |
| T-24 | done | ["AC-49"] |
| T-25 | done | ["AC-49", "AC-50"] |
| T-26 | done | ["AC-48", "AC-31"] |

### unsatisfied

#### acceptanceCriteria

- AC-48

#### scenarios

- SC-25

#### tasks

- T-26

### waivers

| item | reason | acceptedBy |
| --- | --- | --- |
| demo-click-through:AC-48/SC-25 | AC-48/SC-25 declare verificationMethod: demo and the literal GUI walkthrough never happened -- demo-measurements.md states 'No GUI was driven'; nobody clicked 'Boot simulator' in a rendered error card or watched live frames stream in the desktop app window. PROVEN: the zero-diff ScreencapMirrorTransport reuse (inspection-verified, no diff vs Android's transport), the xcrun simctl io screenshot primitive itself (exit=0, clean 1179x2556 PNG, status line correctly on stderr), the already-booted swallow path (byte-exact stderr token match, exit code 149 confirmed unguessable and unused), and -- as of commit 2160331 -- captureRoundTripMs is now a real measured number: median 300ms (n=10, min 277, max 608 cold) on a booted iPhone 16, establishing an iOS ceiling of ~3.3fps against the ~10fps MirrorTuning.CaptureFloorMs=100 assumption written for Android. REMAINING: that 300ms figure comes from direct primitive timing, not from observing frames render inside the running Compose desktop app; the rendered-error-card interaction (Boot simulator / Choose another / trust-retry buttons) is asserted only in commonTest (MirrorIosErrorCardTest), never clicked by a human. No UI-test harness exists anywhere in this repo (standing architectural limitation predating this spec, not a regression it introduced). Feature is could-priority PH-2. | human |
| physical-device-path:SC-26/AC-31(device-not-trusted)/SC-28 | This is a design-time waiver the plan itself declared (AS-3/NFR-18), reaffirmed here rather than newly introduced. PROVEN: parseIdbTargets and the Screencap-x-Idb argv shape are fixture- and unit-tested (IdbTargetsParserTest, DeviceArgvTest); the DeviceNotTrusted projection and its picker-yields guard are exercised against constructed fixtures (MirrorIosErrorCardTest, DiscoveryErrorProjectionTest); the implementation is conservative by construction -- an untrusted handset never hides an available simulator (untrusted.size == 1 && simulators.isEmpty() gate). REMAINING: zero hardware evidence exists for the physical-device capture path or for a real untrusted iPhone surfacing the trust card -- demo-measurements.md additionally records a new negative result, idb list-targets failing to see the booted simulator within a 20s window, which is honestly surfaced rather than hidden. No physical untrusted iPhone was available to this spec's author. | human |

### openChangeRequests

| id | about | target | source |
| --- | --- | --- | --- |
| CR-AF12 | registry/features.json has 19 entries and none for ios-platform-support -- the only shipped spec in this repo with no feature row. Add an entry (status: partial; simulator path measured, physical-device path code-review-only per SC-26/AS-3, AC-48 demo click-through still unwaived-pending) covering the 10 touched modules. | registry/features.json | audit |
| CR-AF4-tuning | The plan instructed adding an iOS-specific MirrorTuning constant once captureRoundTripMs was measured; it now is (median 300ms, ~3.3fps ceiling vs Android's ~10fps CaptureFloorMs=100 assumption) but no iOS tuning constant was added. The poll loop risks requesting frames faster than the simulator can produce them. | MirrorTuning.kt | audit |
| CR-AF7 | Traceability matrix names BootTargetTest and a dedicated 'transport loop against a fake iOS executor' that were never created under those names. Coverage exists under different names (MirrorViewModelIosPlatformSwitchTest.theBootTargetIsTheUdidOnIosAndTheAvdNameOnAndroid; ScreencapMirrorTransportTest's generic fake, which proves a weaker platform-agnostic claim). Update the matrix to point at what actually exists. | plan/plan.json#traceabilityMatrix | audit |
| CR-AF11 | pollIos() calls the measured-5.27s idb list-targets on every poll cycle, serially ahead of listSimulators() and the 2s DiscoveryPollMs interval, holding the actionable simulator list behind the physical-device call. Contained today (missing idb short-circuits before spawning; physical path is waived) but worth a concurrent-fetch or idb-presence short-circuit before the physical-device waiver is ever lifted. | DeviceRepositoryImpl.kt | audit |

### humanRemarks

- could-priority PH-2 feature: evaluated on the same objective bar as any other spec, priority used only to judge how much unproven GUI surface is tolerable, not to soften the reading.
- AC-49's stated evidence condition (commit 1 = 20 files, five token shapes only) is literally false -- commit 1 is 21 files, the extra being a 168-line, zero-@Test iOS fixture file (AF-6). The property AC-49 actually protects (no Android behaviour changed) holds on hand-inspection of all 19 production files plus the fixture's zero test-surface; the evidence wording itself should be corrected, not re-litigated as a new gap.
- Four of the twelve original audit findings (AF-2, AF-3, AF-4, AF-5) were closed by commit 2160331 and were independently re-verified here rather than trusted: AF-2's four new IosDeviceCommandExecutor tests are mutation-proven (the 149-exit-code fake cannot be told apart from an unrelated failure by exit code alone, forcing a stderr-token implementation); AF-5's ComposedDeviceGraphTest now fails with NoDefinitionFoundException if either qualified registration is deleted, closing the exact boundary that caused three prior green-then-crash waves (though the full 11-module initKoin() graph is still not composed in a single test -- residual risk judged low because producer/consumer share the same qualifier constants); AF-3's plan performanceBudget now matches the shipped 15000ms/5.27s reality; AF-4's captureRoundTripMs is now a real recorded number (median 300ms) but the iOS tuning constant the plan asked be derived from it was not added (CR-AF4-tuning).
- The two waivers both trace to the same root cause: no human has ever clicked through the running desktop app against a booted iOS simulator. Every primitive underneath that click (CLI commands, parsers, boot targeting, error projections, Koin wiring, the zero-diff transport) is independently measured or test-proven; the click itself is the only thing missing.

### changelogRef

changelog.json

### survey

| question | options | answer | source | rationale |
| --- | --- | --- | --- | --- |
| Accept waiver 1 (demo-click-through AC-48/SC-25) - ship without ever having clicked through the live desktop app against a booted iOS simulator? | ["Accept", "Reject"] | Accept | ai-proposed | Every CLI primitive the buttons dispatch was measured live, including captureRoundTripMs; no UI-test harness exists anywhere in this repo, so this is a pre-existing limitation rather than one this spec introduced. |
| Accept waiver 2 (physical-device path SC-26/AC-31 device-not-trusted/SC-28) - ship with the physical-iPhone idb path code-review-only? | ["Accept", "Reject"] | Accept | ai-proposed | A plan-declared waiver (AS-3/NFR-18) reaffirmed, not newly invented. No physical untrusted iPhone exists in this environment, and the simulator path does not depend on it. |
| Should the four open change requests block the verdict or ride along as follow-ups? | ["Follow-up after sign-off", "Block verdict"] | Follow-up after sign-off | ai-proposed | None affects shipped behaviour: a registry entry, a tuning constant the measurement showed is not needed, two stale traceability names, and a poll-interval nit. The two error-severity findings were closed before this verdict. |
