# resolve-and-send-mcp — T-6

## Envelope

| field | value |
| --- | --- |
| stage | execution |
| step | T-6 |
| status | done |
| producedBy | sdd-build-business-implement |
| schemaVersion | 1 |
| createdAt | 2026-10-18T14:43:40Z |
| updatedAt | 2026-10-18T14:43:40Z |

## Gate

### type

auto

### approvedBy

sdd-build-business-implement

### approved

true

### at

2026-10-18T14:43:40Z

## Provenance

### selfHash

3f311d2c26010cebf042ad33dd0d2829b2fcdd92f44cee681bd10c58ca875015

### inputs

| artifact | hash |
| --- | --- |
| backlog/backlog.json | 13736f4c3b002eb7483a12681dfff64471a26926e7be014daa6853d369d3d83b |

## Data

### taskId

T-6

### type

business-logic

### outcome

success

### touchedPaths

- presentation-feature-mirror/src/commonMain/kotlin/presentation/feature/mirror/core/send/EnvelopeAssembly.kt
- presentation-feature-mirror/src/commonTest/kotlin/presentation/feature/mirror/EnvelopeAssemblyTest.kt

### tdd

#### testsFrozen

true

#### cycle

green

#### testsWrittenBy

sdd-build-business-test

#### implementedBy

sdd-build-business-implement (wave-3)

### testRun

#### command

./gradlew :presentation-feature-mirror:desktopTest :data-handoff-api:desktopTest

#### passed

true

#### output

```
BUILD SUCCESSFUL in 3s
presentation-feature-mirror 340 tests 0 failures (328 pre-existing + 12 frozen)
data-handoff-api 14 tests 0 failures
EnvelopeAssemblyTest tests=12 failures=0
```

### implementation

#### summary

assembleEnvelope projects F-6's seams into the wire envelope with no recomputation: frozen.promptOverride ?: buildPrompt(frozen, nodeIdResolver(...)), the resolver inverting payloadHierarchyRows badges exactly as MirrorPayloadTab's preview does so the prompt sent equals the prompt previewed. Crop geometry from cropWindow(...).srcOffset/.srcSize only, rounded once per span. Hierarchy collapses to if (platform == Android && status == Ready) hierarchy.rawDump else "", with hierarchyAvailable = hierarchyXml.isNotEmpty() - the Empty snapshot's rawDump == "" degrades a stale Ready verdict for free. A null encodeCrop sets cropPath = null for that annotation and contributes no HandoffImage without aborting. mintSessionId called exactly once; version echoed from the argument.

#### filesChanged

- presentation-feature-mirror/src/commonMain/kotlin/presentation/feature/mirror/core/send/EnvelopeAssembly.kt

### validations

| stage | passed | iterations | issues | note |
| --- | --- | --- | --- | --- |
| tests | true | 1 | [] |  |
| cross-target-compile | true | 1 | [] | ./gradlew :presentation-feature-mirror:compileTestKotlinIosArm64 -> BUILD SUCCESSFUL |
| AS-28-greps | true | 1 | [] | (1) .dstSize/.scale in core/send/: two hits, BOTH KDoc prose warning against them, zero code references. (2) PAYLOAD_FORMAT_VERSION defined once at core/payload/PayloadEstimate.kt:24; the only other hit is a KDoc @param link - not an import, not a re-export. |

### troubleshooting

| at | issue | resolution | severity |
| --- | --- | --- | --- |
| T-6 | PP-TD-1 regression pinned by a dedicated frozen test: it captures the DeviceRect actually passed to encodeCrop and compares it against an expectation derived purely from cropWindow(...).srcOffset/.srcSize, using a fixture where srcSize is ~702px and dstSize ~72px so a .dstSize/.scale regression cannot pass by coincidence. | Implemented correctly; test green | info |
| T-6 | INHERITED F-6 INCONSISTENCY, out of scope here but real: buildPrompt is reused unchanged, so its trailer always reads 'Attached: frame.png, N crops, hierarchy.xml' even when hierarchyAvailable == false. The envelope can therefore contradict its own prompt text on a hierarchy-less send (iOS, or Absent/Unavailable status). | Flagged for Stage 5 / Stage 6 - not fixed in T-6 | warn |
| T-6 | Not pinned by any test: EnvelopeDevice.id degrades a null/blank deviceId to T-4's existing UNKNOWN_DEVICE_ID rather than shipping an empty string on the wire. The constant is reused, not duplicated, and mintSessionId still owns its own degradation (still exactly one call). T-9 should confirm. | Flagged for T-9 | info |
| T-6 | The node-id resolver runs off the snapshot regardless of platform, so a hypothetical iOS send with a snapshot could name nodes in the prompt while hierarchyXml is empty. Matches the preview's behaviour; AS-29 governs the XML field only. Unreachable today (iOS has no dumper). | Documented; T-9 should not assume otherwise | info |
| T-6 | DECLARED SIGNATURE DEVIATION from the backlog's abbreviated parameter list: assembleEnvelope additionally takes hierarchyStatus, platform and version (needed to satisfy TAC-1/TAC-5), and encodeCrop is curried to suspend (DeviceRect) -> ByteArray? rather than a raw CropEncoder, since assembly never touches ImageBitmap. T-9 binds it as { rect -> cropEncoder.encode(bitmap, rect) } and supplies PAYLOAD_FORMAT_VERSION from core/payload at the call site - the constant is still not importable from core/send. | Documented inline in KDoc and here | info |

### linkedAC

- AC-18
- AC-32
- AS-28
- AS-29
- FR-17
- FR-57

### backlogUpdate

#### taskId

T-6

#### status

done

### needsHumanReview

#### required

true

#### reason

buildPrompt's 'Attached: ... hierarchy.xml' trailer can contradict hierarchyAvailable=false; inherited F-6 behaviour left unfixed.
