package dev.rectify.io.docs

import kotlinx.serialization.json.Json
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.Serializable
import java.io.File

/** One record in `search-index.json`, consumed by `assets/search.js`. */
@Serializable
private data class SearchEntry(
    val title: String,
    val section: String,
    val url: String,
    val text: String,
)

/** How much page prose the search index carries, in characters. */
private const val SEARCH_TEXT_BUDGET = 1600

private val json = Json { ignoreUnknownKeys = true; prettyPrint = false }

/**
 * Renders `docs/` into a static site.
 *
 * Usage: `generateDocs <docsDir> <outDir> [baseUrl]`.
 *
 * The optional third argument overrides `site.json`'s `baseUrl`, and it is the difference
 * between a site that works locally and one that works on Pages: a project site lives under
 * `/<repo>`, so deployed asset URLs need that prefix, while a local `file://` or
 * `python3 -m http.server` preview needs none. Passing it keeps one manifest honest for both
 * instead of making someone edit the file before every deploy.
 */
fun main(args: Array<String>) {
    require(args.size in 2..3) { "usage: generateDocs <docsDir> <outDir> [baseUrl]" }
    val docsDir = File(args[0])
    val outDir = File(args[1])
    val baseUrlOverride = args.getOrNull(2)

    val manifest = File(docsDir, "site.json")
    check(manifest.isFile) { "missing ${manifest.path}" }
    val site = json.decodeFromString(Site.serializer(), manifest.readText())
        .let { if (baseUrlOverride != null) it.copy(baseUrl = baseUrlOverride) else it }
    require(
        site.baseUrl.isEmpty() ||
            site.baseUrl == RELATIVE ||
            (site.baseUrl.startsWith("/") && !site.baseUrl.endsWith("/")),
    ) {
        "baseUrl must be empty, \".\" for relative links, or look like \"/repo-name\" " +
            "— got \"${site.baseUrl}\""
    }

    // A full wipe, not an overwrite: a page deleted from site.json has to disappear from the
    // output too, and an incremental copy would publish it forever.
    if (outDir.exists()) outDir.deleteRecursively()
    outDir.mkdirs()

    val contentDir = File(docsDir, "content")
    val index = mutableListOf<SearchEntry>()

    site.readingOrder().forEach { resolved ->
        val source = File(contentDir, resolved.page.file)
        check(source.isFile) { "page \"${resolved.page.title}\" points at a missing file: ${source.path}" }

        val rendered = renderMarkdown(source.readText())
        val html = renderPage(site = site, resolved = resolved, content = rendered)

        val target = if (resolved.page.slug == "index") {
            File(outDir, "index.html")
        } else {
            File(outDir, "${resolved.page.slug}/index.html")
        }
        target.parentFile.mkdirs()
        target.writeText(html)

        index += SearchEntry(
            title = resolved.page.title,
            section = resolved.section.title,
            // Indexed from the site root; search.js rewrites it for the page doing the
            // searching, which is the only page that knows its own depth.
            url = if (resolved.page.slug == "index") "" else "${resolved.page.slug}/",
            text = rendered.plainText.take(SEARCH_TEXT_BUDGET),
        )
    }

    File(outDir, "assets").mkdirs()
    File(docsDir, "assets").listFiles()?.forEach { it.copyRecursively(File(outDir, "assets/${it.name}"), overwrite = true) }
    File(outDir, "assets/search-index.json")
        .writeText(json.encodeToString(ListSerializer(SearchEntry.serializer()), index))

    // Jekyll is GitHub Pages' default and it drops any path starting with an underscore.
    // Nothing here starts with one today, but the failure is silent and remote-only, so the
    // opt-out ships with the site rather than waiting to be diagnosed from a 404.
    File(outDir, ".nojekyll").writeText("")

    println("docs: ${index.size} pages -> ${outDir.path}")
}
