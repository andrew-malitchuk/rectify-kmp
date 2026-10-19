package data.repository.impl.source.store

/**
 * Reads back bytes a [BatchFileStore] wrote.
 *
 * A sibling of [BatchFileStore] rather than a second method on it: `BatchFileStore` is a `fun
 * interface`, and a second abstract method would destroy its SAM conversion and every
 * `BatchFileStore { _, _ -> null }` stand-in with it. Same seam shape either way — one real
 * desktop implementation plus `null`-returning Android/iOS stand-ins (AS-29).
 *
 * Not a general file-read API: the only paths ever passed are the absolute ones this app wrote
 * itself and stored in its own `history_entry` row.
 */
public fun interface BatchFileReader {

    /**
     * The file's bytes, or `null` when it cannot be read.
     *
     * **Never throws.** A missing file (the user deleted the directory), a path that names a
     * directory, a permission failure and an I/O error all read as `null` — a stored path is a
     * hint, never a guarantee, and a lost frame must degrade the verify run, not crash it.
     */
    public suspend fun read(path: String): ByteArray?
}
