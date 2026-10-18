package data.repository.impl.core.mapper

import common.core.source.mapper.Mapper
import data.database.impl.source.entity.HistoryAnnotationEntity
import data.database.impl.source.entity.HistoryEntryEntity
import data.database.impl.source.entity.HistoryEntryWithAnnotations
import domain.core.source.model.history.BatchStatusModel
import domain.core.source.model.history.HistoryEntryModel
import domain.core.source.model.history.SentBatchModel

/**
 * Maps between the stored history rows and the domain's history models.
 *
 * Deliberately **not** a [data.repository.impl.core.mapper.base.ModelResourceMapper]
 * (PLAN-AS-9): that interface pairs a mapping with its inverse, and the inverse of the read
 * model is not the write path — writing starts from a [SentBatchModel] and additionally needs
 * the file store's directory. A `toResource: HistoryEntryModel -> entity` would be a fabricated
 * function nothing calls, which is exactly the dead code `.claude/rules/mappers.md`'s
 * named-`Mapper`-object clause exists to avoid.
 *
 * Neither direction touches the filesystem: a stored path is carried through verbatim, so a row
 * whose files the user deleted still maps to a complete model (the renderer treats a path as a
 * hint, never as a guarantee).
 */
internal object HistoryEntryMapper {

    /** Name of the full-frame PNG inside a batch directory. */
    const val FRAME_FILE: String = "frame.png"

    /** Name of the view-hierarchy dump inside a batch directory. */
    const val HIERARCHY_FILE: String = "hierarchy.xml"

    /** Name of annotation [index]'s crop PNG inside a batch directory. */
    fun cropFile(index: Int): String = "crop-$index.png"

    /**
     * Read direction. An unrecognised stored status falls back to [BatchStatusModel.Sent]
     * instead of throwing — a mapper never throws, and a row the user can see beats a crash.
     */
    val toModel: Mapper<HistoryEntryWithAnnotations, HistoryEntryModel> =
        Mapper { relation ->
            val entry = relation.entry
            HistoryEntryModel(
                id = entry.id,
                sentAtMs = entry.sentAtMs,
                status = BatchStatusModel.entries.firstOrNull { it.name == entry.status }
                    ?: BatchStatusModel.Sent,
                title = entry.title,
                annotationCount = entry.annotationCount,
                thumbPng = entry.thumbPng,
                framePath = entry.framePath,
            )
        }

    /**
     * Write direction: the row and its annotation children for a just-sent batch.
     *
     * [dirPath] is whatever [data.repository.impl.source.store.BatchFileStore.write] returned —
     * `null` when the disk write failed, in which case every path on both the row and its
     * children stays `null` and only the inline thumbnail survives (SC-18). A path is also left
     * `null` when the corresponding bytes were absent, so a stored path always refers to a file
     * that was genuinely written.
     */
    fun SentBatchModel.toEntities(
        dirPath: String?,
    ): Pair<HistoryEntryEntity, List<HistoryAnnotationEntity>> {
        val entry = HistoryEntryEntity(
            id = id,
            sentAtMs = sentAtMs,
            status = BatchStatusModel.Sent.name,
            // The model carries no title of its own; the list shows the first comment.
            title = annotations.firstOrNull()?.comment.orEmpty(),
            annotationCount = annotations.size,
            deviceLabel = deviceLabel,
            prompt = prompt,
            dirPath = dirPath,
            framePath = dirPath.pathTo(FRAME_FILE, present = frameBytes != null),
            hierarchyPath = dirPath.pathTo(HIERARCHY_FILE, present = hierarchyXml != null),
            thumbPng = thumbPng,
        )
        val children = annotations.mapIndexed { index, annotation ->
            HistoryAnnotationEntity(
                batchId = id,
                index = index,
                left = annotation.left,
                top = annotation.top,
                right = annotation.right,
                bottom = annotation.bottom,
                comment = annotation.comment,
                cropPath = dirPath.pathTo(cropFile(index), present = annotation.cropBytes != null),
            )
        }
        return entry to children
    }

    private fun String?.pathTo(fileName: String, present: Boolean): String? =
        if (this == null || !present) null else "$this/$fileName"
}
