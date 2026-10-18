package data.handoff.api.source.envelope

import kotlinx.serialization.Serializable

/**
 * The single handoff payload Rectify publishes and an MCP tool call reads back.
 *
 * [version] is declared FIRST on purpose: `kotlinx.serialization.json` preserves declaration
 * order when encoding a plain (non-polymorphic) class, and a reader on the other side of the
 * slot — a separate OS process — must be able to sniff the format version before committing to
 * parsing the rest of the document (AC-32). Reordering these properties is a breaking change to
 * the wire format even though Kotlin itself would not notice.
 */
@Serializable
public data class AnnotationEnvelope(
    public val version: Int,
    public val sessionId: String,
    public val sentAtMs: Long,
    public val device: EnvelopeDevice,
    public val prompt: String,
    public val hierarchyXml: String,
    public val hierarchyAvailable: Boolean,
    public val framePath: String,
    public val annotations: List<EnvelopeAnnotation>,
)

/** The device an [AnnotationEnvelope] was captured from. */
@Serializable
public data class EnvelopeDevice(
    public val id: String,
    public val label: String,
    public val widthPx: Int,
    public val heightPx: Int,
)

/** One annotated rectangle within an [AnnotationEnvelope]. */
@Serializable
public data class EnvelopeAnnotation(
    public val index: Int,
    public val left: Int,
    public val top: Int,
    public val right: Int,
    public val bottom: Int,
    public val comment: String,
    public val cropPath: String?,
)

/**
 * One image belonging to a [AnnotationHandoff][data.handoff.api.source.handoff.AnnotationHandoff]
 * `publish` call.
 *
 * Never serialized directly — only [name] (a bare file name) is referenced from inside the
 * envelope; the bytes are written to `<root>/<sentAtMs>/<name>` alongside it. Deliberately not a
 * `data class`: `ByteArray` has reference equality, and giving [HandoffImage] a generated
 * `equals`/`hashCode` over it would silently do the wrong thing.
 */
public class HandoffImage(
    public val name: String,
    public val bytes: ByteArray,
)
