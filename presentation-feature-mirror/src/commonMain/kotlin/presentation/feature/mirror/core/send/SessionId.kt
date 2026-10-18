package presentation.feature.mirror.core.send

/** What [mintSessionId] prints in place of a null or blank device id. */
internal const val UNKNOWN_DEVICE_ID: String = "unknown"

/**
 * Mints the identifier the completion toast's "Open session" action copies to the clipboard
 * (FR-58), so the user can paste it back to Claude Code to point at a specific send.
 *
 * Format is `<deviceId>-f<frameId>-<sentAtMs>`, e.g. `RF8M90ABCDE-f3-1760000000000` — stable
 * per-send (two sends of the same frame get different ids because [sentAtMs] differs) and
 * reconstructible from the three facts the session already carries, so nothing new needs
 * generating or persisting.
 *
 * @param deviceId Device the session ran on; degrades to [UNKNOWN_DEVICE_ID] when `null` or
 *   blank — a send can complete after the session is [presentation.feature.mirror.source.mirror.MirrorSession.Lost]
 *   (SC-27), and `MirrorSession.Lost.deviceId` is itself always a real id, so the degradation
 *   exists for defensiveness rather than a known-reachable caller.
 * @param frameId [presentation.feature.mirror.source.mirror.FrozenFrame.frameId] of the frame
 *   that was sent.
 * @param sentAtMs Wall-clock stamp of the send, from the injected clock seam.
 * @return The minted identifier.
 */
internal fun mintSessionId(deviceId: String?, frameId: Int, sentAtMs: Long): String {
    val device = deviceId?.takeIf { it.isNotBlank() } ?: UNKNOWN_DEVICE_ID
    return "$device-f$frameId-$sentAtMs"
}
