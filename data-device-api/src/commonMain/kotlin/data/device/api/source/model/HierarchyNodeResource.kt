package data.device.api.source.model

import data.core.source.resource.Resource

/**
 * One node of an `uiautomator dump` hierarchy, flattened depth-first.
 *
 * Bounds are **normalised on parse**: real dumps contain inverted rectangles (a vanilla
 * Settings screen has three, e.g. `[42,2083][189,2069]`, height −14), so [right] is always
 * `>= `[left] and [bottom] always `>= `[top] here — downstream size arithmetic never sees a
 * negative. Coordinates are device pixels in the *override* resolution `uiautomator` reports,
 * which is what `parseWmSize` prefers too.
 *
 * @property left Left edge in px.
 * @property top Top edge in px.
 * @property right Right edge in px, never left of [left].
 * @property bottom Bottom edge in px, never above [top].
 * @property className `class`, e.g. `android.widget.TextView`.
 * @property resourceId `resource-id`, empty on most Compose nodes.
 * @property text `text`, XML-unescaped.
 * @property contentDesc `content-desc`, XML-unescaped.
 * @property clickable `clickable="true"` only — NOT `scrollable`, NOT `long-clickable`.
 * @property depth Nesting depth, root node = 0.
 * @property nodeId Display label: `resource-id` -> `text` -> `content-desc` -> [className],
 *   suffixed `[n]` (0-based, document order) only when that label is not unique in the dump.
 */
public data class HierarchyNodeResource(
    val left: Int,
    val top: Int,
    val right: Int,
    val bottom: Int,
    val className: String,
    val resourceId: String,
    val text: String,
    val contentDesc: String,
    val clickable: Boolean,
    val depth: Int,
    val nodeId: String,
) : Resource
