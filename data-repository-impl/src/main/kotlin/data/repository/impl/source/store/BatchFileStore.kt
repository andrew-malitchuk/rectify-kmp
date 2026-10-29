package data.repository.impl.source.store

/**
 * Writes a sent batch's bulk bytes somewhere durable.
 *
 * Same seam shape as `presentation-feature-mirror`'s `CropEncoder`: an injectable `fun
 * interface` with exactly one real implementation (`FileSystemBatchFileStore`), so a test can
 * substitute a `null`-returning stand-in and exercise both the write-succeeded and the
 * write-failed branch without touching a real disk.
 *
 * ponytail: nothing ever deletes these directories, so history grows unbounded (~0.5-2 MB per
 * batch). Accepted ceiling. Upgrade path when it bites — prune by age or by count here, on
 * [write], before creating the new directory; no caller changes.
 */
public fun interface BatchFileStore {

    /**
     * Writes every `name to bytes` pair of [files] into one directory owned by [batchId].
     *
     * **Never throws.** Returns the directory's path on success, or `null` when anything at all
     * went wrong — a full disk must degrade the row to "no files on disk", never fail the send
     * that produced it.
     */
    public suspend fun write(batchId: String, files: List<Pair<String, ByteArray>>): String?
}
