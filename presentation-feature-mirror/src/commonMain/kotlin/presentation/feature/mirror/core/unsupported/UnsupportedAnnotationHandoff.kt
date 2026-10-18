package presentation.feature.mirror.core.unsupported

import data.handoff.api.source.envelope.AnnotationEnvelope
import data.handoff.api.source.envelope.HandoffImage
import data.handoff.api.source.handoff.AnnotationHandoff

/**
 * No-op [AnnotationHandoff] for targets that cannot write to a desktop file slot (AS-29).
 *
 * Lives here, not in `:data-handoff-api`, so that module stays Koin-free — a declared, gate
 * approved deviation (deviation 2). See `presentation-feature-mirror/CLAUDE.md` and
 * `data-handoff-api/CLAUDE.md`.
 *
 * Same split as `data-device-impl`'s `Unsupported*` sources: reads answer "nothing here"
 * ([latest] returns `null`) while writes fail loudly rather than silently dropping a payload.
 */
internal object UnsupportedAnnotationHandoff : AnnotationHandoff {

    override suspend fun publish(envelope: AnnotationEnvelope, images: List<HandoffImage>): Unit =
        throw UnsupportedOperationException(UNSUPPORTED)

    override suspend fun latest(): AnnotationEnvelope? = null

    override fun resolve(envelope: AnnotationEnvelope, name: String): String =
        throw UnsupportedOperationException(UNSUPPORTED)
}

/** Reason carried by every failure this handoff raises. */
private const val UNSUPPORTED: String = "The annotation handoff slot lives on the desktop host only"
