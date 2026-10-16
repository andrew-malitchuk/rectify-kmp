package data.device.api.source.parse

import data.device.api.source.model.HierarchyNodeResource

private const val HIERARCHY_CLOSE = "</hierarchy>"
private const val NODE_TAG = "node"

/** Strict `[l,t][r,b]`, anchored: anything else is not a rectangle and the node is skipped. */
private val BOUNDS = Regex("""^\[(-?\d+),(-?\d+)]\[(-?\d+),(-?\d+)]$""")

/**
 * Flattens `adb exec-out uiautomator dump /dev/tty` stdout depth-first.
 *
 * Hand-rolled on purpose — no XML library, no new dependency, compiles for Android, iOS and
 * desktop from `commonMain`. **It never throws.** Empty, truncated or otherwise unreadable
 * input yields an empty list; a single node with unusable `bounds` is skipped while its
 * siblings and children survive. Everything it has to tolerate was measured on a Pixel 8 and
 * is written up in `.pipeline/sdd/docs/measure-mode/execution/T-1/spike-note.md`:
 *
 * - **The payload is not XML.** It ends `</hierarchy>UI hierchary dumped to: /dev/tty\n` —
 *   AOSP's own misspelling. Everything from the closing tag on is cut, so a real XML parser
 *   could not be used here even if one were free.
 * - **The declaration is single-quoted** (`<?xml version='1.0' ...`), so attribute scanning
 *   accepts either quote character.
 * - **Attribute values may contain `>`**, so the end of a tag is found by scanning with quote
 *   state, not by `indexOf('>')`.
 * - **Rects can be inverted** — normalised here, see [HierarchyNodeResource].
 *
 * @param stdout Raw process stdout, trailer and all.
 * @return Nodes in depth-first document order; empty when nothing could be read.
 */
public fun parseUiAutomatorDump(stdout: String): List<HierarchyNodeResource> {
    val close = stdout.indexOf(HIERARCHY_CLOSE)
    if (close < 0) return emptyList()
    return stdout.substring(0, close).scanNodes().withCollisionIndices()
}

/** Walks every tag once, tracking depth across skipped nodes so children keep their level. */
private fun String.scanNodes(): List<HierarchyNodeResource> {
    val nodes = mutableListOf<HierarchyNodeResource>()
    var depth = 0
    var cursor = 0
    while (true) {
        val open = indexOf('<', cursor)
        if (open < 0) break
        val close = tagEnd(open)
        if (close < 0) break
        val tag = substring(open + 1, close)
        cursor = close + 1
        when {
            tag.startsWith("/$NODE_TAG") -> depth = (depth - 1).coerceAtLeast(0)
            tag.isNodeTag() -> {
                tag.toNode(depth)?.let(nodes::add)
                if (!tag.endsWith("/")) depth++
            }
        }
    }
    return nodes
}

/** `node`/`node ...`/`node/` but not `nodeinfo` — and never `hierarchy` or `?xml`. */
private fun String.isNodeTag(): Boolean =
    startsWith(NODE_TAG) && (length == NODE_TAG.length || !this[NODE_TAG.length].isLetterOrDigit())

/** Index of the `>` closing the tag opened at [open], ignoring `>` inside a quoted value. */
private fun String.tagEnd(open: Int): Int {
    var quote: Char? = null
    for (index in open + 1 until length) {
        val char = this[index]
        when {
            quote != null -> if (char == quote) quote = null
            char == '"' || char == '\'' -> quote = char
            char == '>' -> return index
        }
    }
    return -1
}

private fun String.toNode(depth: Int): HierarchyNodeResource? {
    val attributes = attributes()
    val bounds = BOUNDS.find(attributes["bounds"].orEmpty())?.groupValues ?: return null
    val (x1, y1, x2, y2) = bounds.drop(1).map { it.toIntOrNull() ?: return null }
    val text = attributes["text"].orEmpty().unescapeXml()
    val contentDesc = attributes["content-desc"].orEmpty().unescapeXml()
    val resourceId = attributes["resource-id"].orEmpty()
    val className = attributes["class"].orEmpty()
    return HierarchyNodeResource(
        left = minOf(x1, x2),
        top = minOf(y1, y2),
        right = maxOf(x1, x2),
        bottom = maxOf(y1, y2),
        className = className,
        resourceId = resourceId,
        text = text,
        contentDesc = contentDesc,
        clickable = attributes["clickable"] == "true",
        depth = depth,
        nodeId = resourceId.ifEmpty { text }.ifEmpty { contentDesc }.ifEmpty { className },
    )
}

/**
 * Splits `name="value"` pairs sequentially.
 *
 * Sequential rather than per-attribute matching because the names overlap: searching for
 * `clickable="` finds `long-clickable="` first, and a value may contain `=`, `>` or the other
 * quote character.
 */
private fun String.attributes(): Map<String, String> {
    val attributes = mutableMapOf<String, String>()
    var index = indexOfFirst(Char::isWhitespace).takeIf { it >= 0 } ?: return attributes
    while (index < length) {
        while (index < length && (this[index].isWhitespace() || this[index] == '/')) index++
        val nameStart = index
        while (index < length && this[index] != '=' && !this[index].isWhitespace()) index++
        if (index >= length || this[index] != '=' || index == nameStart) break
        val name = substring(nameStart, index)
        val quote = getOrNull(index + 1)?.takeIf { it == '"' || it == '\'' } ?: break
        val valueStart = index + 2
        val valueEnd = indexOf(quote, valueStart)
        if (valueEnd < 0) break
        attributes[name] = substring(valueStart, valueEnd)
        index = valueEnd + 1
    }
    return attributes
}

/** The five XML entities, `&amp;` last so `&amp;lt;` stays the literal text `&lt;`. */
private fun String.unescapeXml(): String =
    if ('&' !in this) {
        this
    } else {
        replace("&lt;", "<")
            .replace("&gt;", ">")
            .replace("&quot;", "\"")
            .replace("&apos;", "'")
            .replace("&amp;", "&")
    }

/**
 * Suffixes `[n]` onto every label that is ambiguous, whatever step of the chain produced it.
 *
 * A `resource-id` is NOT unique even on a View app — `android:id/title` appears nine times in
 * the real Settings dump — so the index cannot be reserved for the class fall-through. A label
 * that is unique in the document is left alone: a lone `ComposeView` stays `ComposeView`.
 */
private fun List<HierarchyNodeResource>.withCollisionIndices(): List<HierarchyNodeResource> {
    val occurrences = groupingBy { it.nodeId }.eachCount()
    val assigned = mutableMapOf<String, Int>()
    return map { node ->
        if (occurrences[node.nodeId] == 1) {
            node
        } else {
            val index = assigned[node.nodeId] ?: 0
            assigned[node.nodeId] = index + 1
            node.copy(nodeId = "${node.nodeId}[$index]")
        }
    }
}
