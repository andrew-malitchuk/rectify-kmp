package domain.core.source.model.device

import domain.core.source.model.base.Model

/**
 * One node of a captured view hierarchy, in device pixels.
 *
 * Rectangles arrive already normalised by the data layer ([right] >= [left], [bottom] >=
 * [top]), so size arithmetic here never sees a negative — real dumps DO contain inverted
 * rects.
 *
 * @property id Display label for the node, unique within its snapshot.
 * @property className Platform class name, e.g. `android.widget.Button`.
 * @property left Left edge in px.
 * @property top Top edge in px.
 * @property right Right edge in px, never left of [left].
 * @property bottom Bottom edge in px, never above [top].
 * @property isTappable Whether the node reports itself clickable.
 * @property depth Nesting depth, root node = 0.
 */
public data class HierarchyNodeModel(
    val id: String,
    val className: String,
    val left: Int,
    val top: Int,
    val right: Int,
    val bottom: Int,
    val isTappable: Boolean,
    val depth: Int,
) : Model
