package dev.rectify.io.core.chooser

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.swing.Swing
import kotlinx.coroutines.withContext
import java.awt.FileDialog
import java.awt.Frame
import java.io.File

/**
 * Asks the user to point Rectify at an `adb` binary (AC-26).
 *
 * `java.awt.FileDialog` rather than Swing's `JFileChooser`: on macOS this is the real NSOpenPanel,
 * so it looks and behaves like every other open dialog on the system — `JFileChooser` renders
 * Java's own widget, which on this platform reads as a foreign application.
 *
 * Deliberately NOT validated here. The returned path goes to `pref_adb_path` and is checked
 * against the filesystem by `ToolResolver`'s existing `isExecutable` predicate at probe time —
 * the same one every other candidate passes through. A second definition of "usable" in this
 * file would be free to drift from the one that actually decides, which is why a path that turns
 * out to be a directory, missing or non-executable simply falls through to the ordered candidate
 * list instead of being rejected with a dialog of its own.
 *
 * Confined to [Dispatchers.Swing], i.e. the EDT, because `FileDialog` is a blocking native
 * modal and AWT requires it there. `coroutines-swing` is already a `desktopMain` dependency for
 * exactly this. Running it on whatever dispatcher Orbit happened to invoke the side-effect
 * handler on was the shipped behaviour until this was caught in review — it worked by luck,
 * since Compose Desktop usually lands there anyway, and would have broken the first time it
 * did not.
 *
 * @param parent Owner frame, so the sheet attaches to the Rectify window rather than floating free.
 * @return The absolute path chosen, or `null` when the user cancelled.
 */
internal suspend fun chooseAdbPath(parent: Frame?): String? = withContext(Dispatchers.Swing) {
    val dialog = FileDialog(parent, "Locate adb", FileDialog.LOAD).apply {
        // Opening in platform-tools is the single most likely answer, and costs nothing when
        // wrong. An unreadable or absent directory is ignored by AWT rather than throwing.
        directory = File(System.getProperty("user.home"), "Library/Android/sdk/platform-tools")
            .takeIf(File::isDirectory)
            ?.absolutePath
        isMultipleMode = false
        isVisible = true
    }
    val file = dialog.file ?: return@withContext null
    val directory = dialog.directory ?: return@withContext null
    File(directory, file).absolutePath
}
