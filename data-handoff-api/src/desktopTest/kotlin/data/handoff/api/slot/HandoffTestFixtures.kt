package data.handoff.api.slot

import data.handoff.api.source.envelope.AnnotationEnvelope
import data.handoff.api.source.envelope.EnvelopeAnnotation
import data.handoff.api.source.envelope.EnvelopeDevice
import data.handoff.api.source.envelope.HandoffImage

/** A realistic, fully-populated envelope for [sentAtMs] — every field non-default so an equality
 * check that only happened to pass on zeros/blanks cannot hide a wiring bug. */
internal fun sampleEnvelope(
    sentAtMs: Long = 1_700_000_000_000L,
    framePath: String = "frame.png",
): AnnotationEnvelope = AnnotationEnvelope(
    version = 1,
    sessionId = "emulator-5554-f3-$sentAtMs",
    sentAtMs = sentAtMs,
    device = EnvelopeDevice(id = "emulator-5554", label = "Android 15", widthPx = 1080, heightPx = 2400),
    prompt = "Fix 1 UI issue.\nAndroid 15 · 1080x2400",
    hierarchyXml = "<hierarchy rotation=\"0\"><node /></hierarchy>",
    hierarchyAvailable = true,
    framePath = framePath,
    annotations = listOf(
        EnvelopeAnnotation(
            index = 1,
            left = 120,
            top = 880,
            right = 960,
            bottom = 1040,
            comment = "Button is 8dp too low",
            cropPath = "crop-1.png",
        ),
    ),
)

/** A tiny, deterministic "PNG" payload — the bytes never need to be a real image, only to
 * round-trip byte-identically. */
internal fun sampleImage(name: String, seed: Int = 1): HandoffImage =
    HandoffImage(name = name, bytes = ByteArray(32) { (it + seed).toByte() })
