package dev.rectify.io.docs

import kotlinx.serialization.Serializable

/**
 * `docs/site.json` — the whole nav, authored by hand.
 *
 * Explicit rather than derived from the directory tree: order and titles are editorial
 * decisions, and a generator that guesses them from filenames forces `01-`, `02-` prefixes
 * into every path and therefore into every published URL.
 *
 * @property title Site name, shown in the header and used in each page's `<title>`.
 * @property tagline One line under the header title on the landing page.
 * @property repository Repository URL, linked from the header; `null` hides the link.
 * @property baseUrl Path the site is served from, e.g. `/rectify-kmp`. Must start with `/` and
 *   must not end with one. GitHub Pages serves a project site from a subdirectory, so every
 *   absolute asset URL has to carry this prefix or the CSS 404s on everything but a user site.
 * @property sections Ordered nav sections.
 */
@Serializable
internal data class Site(
    val title: String,
    val tagline: String,
    val repository: String? = null,
    val baseUrl: String = "",
    val sections: List<Section>,
)

/**
 * One group in the sidebar.
 *
 * @property title Group heading.
 * @property pages Ordered pages in the group.
 */
@Serializable
internal data class Section(
    val title: String,
    val pages: List<Page>,
)

/**
 * One rendered page.
 *
 * @property title Nav label and `<h1>` fallback.
 * @property file Path of the Markdown source, relative to `docs/content/`.
 * @property slug Output path without extension, relative to the site root. `index` is the
 *   landing page.
 * @property description One line for the page header and the `<meta name="description">`.
 */
@Serializable
internal data class Page(
    val title: String,
    val file: String,
    val slug: String,
    val description: String = "",
)

/** A page flattened out of [Site.sections], with the neighbours its footer links to. */
internal data class ResolvedPage(
    val page: Page,
    val section: Section,
    val previous: Page?,
    val next: Page?,
)

/** Flattens [Site.sections] into reading order, which is what prev/next navigation needs. */
internal fun Site.readingOrder(): List<ResolvedPage> {
    val flat = sections.flatMap { section -> section.pages.map { section to it } }
    return flat.mapIndexed { index, (section, page) ->
        ResolvedPage(
            page = page,
            section = section,
            previous = flat.getOrNull(index - 1)?.second,
            next = flat.getOrNull(index + 1)?.second,
        )
    }
}
