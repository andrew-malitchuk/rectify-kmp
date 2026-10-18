package dev.rectify.io.mcp.core.rpc

import data.handoff.api.source.envelope.AnnotationEnvelope
import data.handoff.api.source.envelope.EnvelopeAnnotation
import data.handoff.api.source.envelope.EnvelopeDevice
import data.handoff.api.source.envelope.HandoffImage
import data.handoff.api.source.handoff.AnnotationHandoff
import java.util.concurrent.CountDownLatch
import kotlin.coroutines.Continuation
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.coroutines.startCoroutine

/**
 * A stdlib-only bridge from a suspend block to a plain blocking call, for `kotlin.test` functions
 * (which JUnit requires to be non-`suspend`). Deliberately NOT `kotlinx.coroutines.runBlocking`:
 * this module's own compile classpath is pinned to `:data-handoff-api` + `kotlinx-serialization
 * -json` (see `Main.kt`'s KDoc) — `kotlinx-coroutines-core` is a transitive RUNTIME dependency via
 * `:data-handoff-api`'s `implementation`, never a compile-time one here, so `runBlocking` would
 * not resolve. [kotlin.coroutines.startCoroutine] is part of kotlin-stdlib.
 */
internal fun <T> runSuspendBlocking(block: suspend () -> T): T {
    val latch = CountDownLatch(1)
    var outcome: Result<T>? = null
    block.startCoroutine(
        Continuation(EmptyCoroutineContext) { result ->
            outcome = result
            latch.countDown()
        },
    )
    latch.await()
    return outcome!!.getOrThrow()
}

/**
 * A realistic, fully-populated envelope — every field non-default so an equality/content check
 * that only happened to pass on zeros/blanks cannot hide a wiring bug. Mirrors (but does not
 * reuse — that fixture is `internal` to `:data-handoff-api`'s own test source set)
 * `data-handoff-api`'s `sampleEnvelope`.
 */
internal fun sampleEnvelope(
    version: Int = SUPPORTED_ENVELOPE_VERSION,
    sentAtMs: Long = 1_700_000_000_000L,
): AnnotationEnvelope = AnnotationEnvelope(
    version = version,
    sessionId = "emulator-5554-f3-$sentAtMs",
    sentAtMs = sentAtMs,
    device = EnvelopeDevice(id = "emulator-5554", label = "Android 15", widthPx = 1080, heightPx = 2400),
    prompt = "Fix 1 UI issue.\nAndroid 15 · 1080x2400",
    hierarchyXml = "<hierarchy rotation=\"0\"><node /></hierarchy>",
    hierarchyAvailable = true,
    framePath = "frame.png",
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

/**
 * A controllable [AnnotationHandoff] test double. [envelopeProvider] stands in for an empty slot
 * (return `null`), a populated one (return an envelope), or a corrupt one (throw) — without ever
 * touching a real file. [resolve] composes a deterministic fake `file://` URI so assertions never
 * depend on `FileSlotAnnotationHandoff`'s real on-disk layout.
 */
internal class FakeAnnotationHandoff(
    private val envelopeProvider: () -> AnnotationEnvelope?,
) : AnnotationHandoff {

    var publishCount: Int = 0
        private set

    override suspend fun publish(envelope: AnnotationEnvelope, images: List<HandoffImage>) {
        publishCount++
    }

    override suspend fun latest(): AnnotationEnvelope? = envelopeProvider()

    override fun resolve(envelope: AnnotationEnvelope, name: String): String =
        "file:///fake-handoff/${envelope.sentAtMs}/$name"
}
