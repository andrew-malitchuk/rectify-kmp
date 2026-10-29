package data.handoff.api.source.handoff

import data.handoff.api.core.slot.FileSlotAnnotationHandoff
import java.nio.file.Path
import java.nio.file.Paths

/** `~/.rectify/handoff` — created lazily by [FileSlotAnnotationHandoff] on first publish. */
private fun defaultHandoffRoot(): Path =
    Paths.get(System.getProperty("user.home"), ".rectify", "handoff")

/**
 * The real [AnnotationHandoff], backed by a single atomic file drop under [root].
 *
 * [root] is the test seam: production callers always use the default, so every real send lands
 * under the user's home directory; tests pass a temp directory so the suite never touches
 * `~/.rectify`.
 */
public fun fileSlotAnnotationHandoff(root: Path = defaultHandoffRoot()): AnnotationHandoff =
    FileSlotAnnotationHandoff(root)
