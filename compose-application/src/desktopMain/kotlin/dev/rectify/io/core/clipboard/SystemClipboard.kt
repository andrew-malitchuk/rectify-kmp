package dev.rectify.io.core.clipboard

import co.touchlab.kermit.Logger
import java.awt.Toolkit
import java.awt.datatransfer.StringSelection

private val log = Logger.withTag("SystemClipboard")

/**
 * Puts [text] on the system clipboard — the toast's `Open session` action (FR-58).
 *
 * Lives beside `core/chooser/AdbPathChooser.kt` for the same reason it does: AWT is a desktop
 * API, the host window is the only layer allowed to touch the platform, and the ViewModel is
 * handed the result (here, nothing) rather than the toolkit.
 *
 * **Never throws.** A headless toolkit, a clipboard owned by another process or a denied X11
 * selection are all real and all irrelevant to whether the send succeeded — the payload is
 * already published by the time this runs, so a failed copy is a logged warning and nothing
 * more. Reporting it as a failed send would be a lie (TAC-5).
 *
 * @param text Value to copy, in practice the minted session identifier.
 */
internal fun copyToClipboard(text: String) {
    runCatching {
        Toolkit.getDefaultToolkit().systemClipboard.setContents(StringSelection(text), null)
    }.onFailure {
        log.w(it) { "Clipboard unavailable — the session id was not copied." }
    }
}
