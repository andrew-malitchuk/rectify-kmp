package data.handoff.api.source.handoff

import data.handoff.api.source.envelope.AnnotationEnvelope
import data.handoff.api.source.envelope.HandoffImage

/**
 * A single-slot publish/read contract for the most recent annotation batch Rectify sent.
 *
 * Deliberately ONE interface, not two — a declared deviation from preparation's two-interface
 * wording. The writer (Rectify's desktop process) and the reader (the MCP server, a separate OS
 * process spawned by Claude Code) never share a Kotlin object graph, so splitting a publish-only
 * and a read-only facade would buy no additional enforcement; the KDoc on each member already
 * documents which side calls it.
 */
public interface AnnotationHandoff {

    /** Publishes [envelope] and its [images] as the new single slot, atomically. */
    public suspend fun publish(envelope: AnnotationEnvelope, images: List<HandoffImage>)

    /**
     * The last published envelope, or `null` when nothing has been published.
     *
     * Re-reads from storage on every call — AC-81 — rather than caching, so a server process
     * started before Rectify ever published anything still answers correctly once a publish
     * happens, with no restart.
     *
     * `null` means EMPTY, nothing else. A slot that exists but cannot be read or parsed throws
     * instead, so a reader can report the fault rather than passing a corrupt slot off as a
     * slot that was never written.
     */
    public suspend fun latest(): AnnotationEnvelope?

    /**
     * Absolute `file://` URI for [name] within [envelope]'s send directory.
     *
     * Rejects any [name] containing `/` or `..` — the slot is untrusted input from the reader's
     * point of view even though Rectify is the one that wrote it.
     */
    public fun resolve(envelope: AnnotationEnvelope, name: String): String
}
