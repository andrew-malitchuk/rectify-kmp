package data.mcp.impl.core.rpc

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
 * (which JUnit requires to be non-`suspend`).
 *
 * `kotlinx.coroutines.runBlocking` would resolve here now — the HTTP handler needs coroutines on
 * the compile classpath, so the module declares them. This stays as it is anyway: it is
 * dependency-free, it already works, and churning a green fixture to use a dependency it does
 * not need buys nothing. [kotlin.coroutines.startCoroutine] is part of kotlin-stdlib.
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

    /**
     * Mirrors `FileSlotAnnotationHandoff.resolve`'s real contract: a name that is not bare is
     * REJECTED by throwing (NFR-4). This double used to accept anything, which is exactly why a
     * traversal-shaped `framePath` escaped the dispatcher as a crash instead of a tool error.
     */
    override fun resolve(envelope: AnnotationEnvelope, name: String): String {
        require(name.isNotBlank() && !name.contains('/') && !name.contains('\\') && !name.contains("..")) {
            "handoff image name must be a bare file name, got: $name"
        }
        return "file:///fake-handoff/${envelope.sentAtMs}/$name"
    }
}
