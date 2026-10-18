package data.handoff.api.core.slot

import data.handoff.api.source.envelope.AnnotationEnvelope
import data.handoff.api.source.envelope.HandoffImage
import data.handoff.api.source.handoff.AnnotationHandoff
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.nio.file.FileSystems
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import java.nio.file.attribute.FileAttribute
import java.nio.file.attribute.PosixFilePermissions

/** THE slot. One file, always complete, always the newest send. */
private const val SLOT_NAME = "latest.json"

/** The staging name `latest.json` is written under before it is moved into place. */
private const val SLOT_TMP_NAME = "latest.json.tmp"

/** `rwx------` — the handoff holds screenshots of whatever the user had on screen. */
private const val OWNER_ONLY = "rwx------"

private val json = Json

/**
 * The real, filesystem-backed [AnnotationHandoff]: one atomic `latest.json` under [root], plus
 * one `<root>/<sentAtMs>/` directory per publish holding that send's images.
 *
 * On-disk layout, per publish:
 * - Images are written to `<root>/<sentAtMs>/<name>` BEFORE `latest.json` moves into place, so a
 *   reader that observes `latest.json` always sees a complete image set.
 * - `latest.json` is written to a `latest.json.tmp` sibling first, then moved into place with
 *   `Files.move(ATOMIC_MOVE, REPLACE_EXISTING)` — the slot either holds the previous envelope or
 *   the new one, never a half-written file.
 * - Every sibling numeric directory `latest.json` does not name is pruned after each successful
 *   publish, so exactly one send's images are ever retained on disk.
 * - [root] is created with owner-only POSIX permissions where the filesystem supports them.
 */
internal class FileSlotAnnotationHandoff(private val root: Path) : AnnotationHandoff {

    override suspend fun publish(envelope: AnnotationEnvelope, images: List<HandoffImage>) {
        withContext(Dispatchers.IO) {
            val sendDirName = envelope.sentAtMs.toString()
            val sendDir = root.resolve(sendDirName)
            createOwnerOnlyDirectories(sendDir)
            // Images first: a reader races only on latest.json, so by the time the slot names
            // this directory every file it references is already fully written.
            images.forEach { image ->
                Files.write(sendDir.resolve(image.name.asBareName()), image.bytes)
            }
            val tmp = root.resolve(SLOT_TMP_NAME)
            Files.writeString(tmp, json.encodeToString(envelope))
            Files.move(
                tmp,
                root.resolve(SLOT_NAME),
                StandardCopyOption.ATOMIC_MOVE,
                StandardCopyOption.REPLACE_EXISTING,
            )
            pruneSendDirectoriesExcept(sendDirName)
        }
    }

    /**
     * Re-reads the slot every call. An ABSENT slot is `null` — the genuinely-empty case. A slot
     * that is present but unreadable or unparseable THROWS, carrying the parse message.
     *
     * The distinction is the whole point: collapsing both onto `null` told the agent "nothing
     * has been sent yet" right after a send the user watched succeed, which is a lie about the
     * one fact the reader is asking for. A corrupt slot is a fault; only an empty one is empty.
     */
    override suspend fun latest(): AnnotationEnvelope? = withContext(Dispatchers.IO) {
        val slot = root.resolve(SLOT_NAME)
        if (!Files.exists(slot)) return@withContext null
        json.decodeFromString<AnnotationEnvelope>(Files.readString(slot))
    }

    override fun resolve(envelope: AnnotationEnvelope, name: String): String =
        root.resolve(envelope.sentAtMs.toString())
            .resolve(name.asBareName())
            .toUri()
            .toString()

    private fun pruneSendDirectoriesExcept(keep: String) {
        Files.newDirectoryStream(root).use { entries ->
            entries.filter { entry ->
                val entryName = entry.fileName.toString()
                entryName != keep && entryName.isSendDirectoryName() && Files.isDirectory(entry)
            }.forEach { stale -> stale.toFile().deleteRecursively() }
        }
    }
}

/** A `<sentAtMs>` directory is all digits — never `latest.json`, never anything a user put there. */
private fun String.isSendDirectoryName(): Boolean = isNotEmpty() && all(Char::isDigit)

/**
 * The slot is untrusted input from the reader's point of view even though Rectify wrote it, and
 * writing is the same boundary in reverse: a name that escapes the send directory must never
 * reach a `Path`.
 */
private fun String.asBareName(): String {
    require(isNotBlank() && !contains('/') && !contains('\\') && !contains("..")) {
        "handoff image name must be a bare file name, got: $this"
    }
    return this
}

private val supportsPosix: Boolean =
    FileSystems.getDefault().supportedFileAttributeViews().contains("posix")

private fun createOwnerOnlyDirectories(dir: Path) {
    val attributes: Array<FileAttribute<*>> =
        if (supportsPosix) {
            arrayOf(PosixFilePermissions.asFileAttribute(PosixFilePermissions.fromString(OWNER_ONLY)))
        } else {
            emptyArray()
        }
    Files.createDirectories(dir, *attributes)
}
