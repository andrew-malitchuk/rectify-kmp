package dev.rectify.io.docs

import org.intellij.markdown.flavours.gfm.GFMFlavourDescriptor
import org.intellij.markdown.html.HtmlGenerator
import org.intellij.markdown.parser.MarkdownParser

/** A heading lifted out of the rendered HTML, for the on-page table of contents. */
internal data class Heading(val id: String, val text: String, val level: Int)

/** Rendered Markdown plus everything the page template needs to know about it. */
internal data class RenderedMarkdown(
    val html: String,
    val headings: List<Heading>,
    /** Prose with tags stripped — the search index stores this, not the markup. */
    val plainText: String,
)

private val flavour = GFMFlavourDescriptor()

/** `<h2 id=...>`/`<h3>` only: h1 is the page title and h4+ is too fine for a sidebar. */
private val HEADING = Regex("""<h([23])>(.*?)</h\1>""", RegexOption.DOT_MATCHES_ALL)
private val TAG = Regex("<[^>]+>")
private val BODY = Regex("""^<body>(.*)</body>$""", RegexOption.DOT_MATCHES_ALL)

/**
 * Renders GitHub-flavoured Markdown and threads anchors through every h2/h3.
 *
 * Anchors are added by post-processing the HTML rather than by walking the parse tree: the
 * tree walk means reimplementing `HtmlGenerator`'s inline rendering to get the heading text
 * back out, and the text is right there in the output already.
 */
internal fun renderMarkdown(source: String): RenderedMarkdown {
    val tree = MarkdownParser(flavour).buildMarkdownTreeFromString(source)
    val generated = HtmlGenerator(source, tree, flavour).generateHtml()
    // HtmlGenerator always wraps its output in <body>; the template supplies its own.
    val body = BODY.find(generated)?.groupValues?.get(1) ?: generated

    val headings = mutableListOf<Heading>()
    val used = mutableSetOf<String>()
    val withAnchors = HEADING.replace(body) { match ->
        val level = match.groupValues[1].toInt()
        val inner = match.groupValues[2]
        val text = TAG.replace(inner, "").trim()
        val id = uniqueSlug(text, used)
        headings += Heading(id = id, text = text, level = level)
        // The anchor is a real link so a reader can copy a deep link to any section.
        """<h$level id="$id">$inner<a class="anchor" href="#$id" aria-label="Link to this section">#</a></h$level>"""
    }

    return RenderedMarkdown(
        html = withAnchors,
        headings = headings,
        plainText = TAG.replace(body, " ").replace(Regex("\\s+"), " ").trim(),
    )
}

/**
 * GitHub-style heading slug, de-duplicated within a page.
 *
 * Two sections legitimately share a name ("Notes" under two parents), and a duplicate `id`
 * silently sends every link to the first one — hence the `-2` suffix rather than first-wins.
 */
private fun uniqueSlug(text: String, used: MutableSet<String>): String {
    val base = text.lowercase()
        .replace(Regex("[^a-z0-9\\s-]"), "")
        .trim()
        .replace(Regex("\\s+"), "-")
        .ifEmpty { "section" }
    if (used.add(base)) return base
    var n = 2
    while (!used.add("$base-$n")) n++
    return "$base-$n"
}
