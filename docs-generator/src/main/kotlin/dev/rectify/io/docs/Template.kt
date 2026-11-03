package dev.rectify.io.docs

/** Escapes the five XML-significant characters for use in text nodes and attribute values. */
internal fun String.escapeHtml(): String = this
    .replace("&", "&amp;")
    .replace("<", "&lt;")
    .replace(">", "&gt;")
    .replace("\"", "&quot;")
    .replace("'", "&#39;")

/**
 * Wraps one rendered page in the site chrome.
 *
 * Everything is inlined or local except the two Google Fonts the app itself uses — matching
 * the desktop UI is the whole point of this theme, and Roboto Flex and JetBrains Mono are the
 * two things a static site cannot reproduce from tokens alone.
 */
internal fun renderPage(
    site: Site,
    resolved: ResolvedPage,
    content: RenderedMarkdown,
): String {
    val page = resolved.page
    val base = site.baseUrl
    val here = page.slug
    val isLanding = here == "index"
    val description = page.description.ifEmpty { site.tagline }
    val title = if (isLanding) site.title else "${page.title} · ${site.title}"

    return buildString {
        appendLine("<!doctype html>")
        appendLine("""<html lang="en" data-slug="${page.slug.escapeHtml()}">""")
        appendLine("<head>")
        appendLine("""<meta charset="utf-8">""")
        appendLine("""<meta name="viewport" content="width=device-width, initial-scale=1">""")
        appendLine("""<title>${title.escapeHtml()}</title>""")
        appendLine("""<meta name="description" content="${description.escapeHtml()}">""")
        appendLine("""<link rel="icon" type="image/png" href="${asset(base, "icon.png", here)}">""")
        appendLine("""<link rel="preconnect" href="https://fonts.googleapis.com">""")
        appendLine("""<link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>""")
        appendLine(
            """<link rel="stylesheet" href="https://fonts.googleapis.com/css2?""" +
                """family=Roboto+Flex:opsz,wght@8..144,400;8..144,500;8..144,600&""" +
                """family=JetBrains+Mono:wght@400;500&display=swap">""",
        )
        appendLine("""<link rel="stylesheet" href="${asset(base, "styles.css", here)}">""")
        // Inline and before the body so the stored theme applies on the first paint. Loading
        // it with the deferred bundle instead makes every dark-mode visit flash light first.
        appendLine("<script>")
        appendLine(
            """(function(){try{var t=localStorage.getItem('rectify-theme');""" +
                """if(t==='light'||t==='dark'){document.documentElement.dataset.theme=t;}}catch(e){}})();""",
        )
        appendLine("</script>")
        appendLine("</head>")
        appendLine("""<body class="${if (isLanding) "is-landing" else "is-doc"}">""")

        appendHeader(site, base, here)
        appendLine("""<div class="layout">""")
        appendSidebar(site, base, page)
        appendLine("""<main class="content" id="content">""")
        appendLine("""<article class="prose">""")
        if (!isLanding) {
            appendLine("""<p class="eyebrow">${resolved.section.title.escapeHtml()}</p>""")
        }
        append(content.html)
        appendLine("</article>")
        appendPrevNext(base, resolved, here)
        appendLine("</main>")
        appendToc(content.headings)
        appendLine("</div>")
        appendFooter(site)

        appendLine("""<script src="${asset(base, "search.js", here)}" defer></script>""")
        appendLine("</body>")
        appendLine("</html>")
    }
}

private fun StringBuilder.appendHeader(site: Site, base: String, here: String) {
    appendLine("""<header class="topbar">""")
    appendLine("""<button class="nav-toggle" aria-label="Toggle navigation" aria-expanded="false">""")
    appendLine("""<span></span><span></span><span></span>""")
    appendLine("</button>")
    appendLine("""<a class="brand" href="${href(base, "index", here)}">""")
    appendLine("""<img src="${asset(base, "icon.png", here)}" alt="" width="26" height="26">""")
    appendLine("""<span>${site.title.escapeHtml()}</span>""")
    appendLine("</a>")
    appendLine("""<div class="search">""")
    appendLine(
        """<input type="search" id="search-input" placeholder="Search the docs" """ +
            """autocomplete="off" aria-label="Search the docs">""",
    )
    appendLine("""<kbd>/</kbd>""")
    appendLine("""<div class="search-results" id="search-results" hidden></div>""")
    appendLine("</div>")
    appendLine("""<div class="topbar-actions">""")
    if (site.repository != null) {
        appendLine(
            """<a class="ghost" href="${site.repository.escapeHtml()}" """ +
                """rel="noopener" target="_blank">GitHub</a>""",
        )
    }
    appendLine("""<button class="ghost theme-toggle" aria-label="Switch colour theme">Theme</button>""")
    appendLine("</div>")
    appendLine("</header>")
}

private fun StringBuilder.appendSidebar(site: Site, base: String, current: Page) {
    appendLine("""<nav class="sidebar" aria-label="Documentation">""")
    site.sections.forEach { section ->
        appendLine("""<p class="nav-section">${section.title.escapeHtml()}</p>""")
        appendLine("<ul>")
        section.pages.forEach { page ->
            val active = if (page.slug == current.slug) """ class="active"""" else ""
            appendLine("""<li><a href="${href(base, page.slug, current.slug)}"$active>${page.title.escapeHtml()}</a></li>""")
        }
        appendLine("</ul>")
    }
    appendLine("</nav>")
}

private fun StringBuilder.appendToc(headings: List<Heading>) {
    // One heading is not a table of contents, it is a restatement of the page title.
    if (headings.size < 2) {
        appendLine("""<aside class="toc" aria-hidden="true"></aside>""")
        return
    }
    appendLine("""<aside class="toc" aria-label="On this page">""")
    appendLine("""<p class="nav-section">On this page</p>""")
    appendLine("<ul>")
    headings.forEach { h ->
        appendLine("""<li class="lvl-${h.level}"><a href="#${h.id}">${h.text.escapeHtml()}</a></li>""")
    }
    appendLine("</ul>")
    appendLine("</aside>")
}

private fun StringBuilder.appendPrevNext(base: String, resolved: ResolvedPage, here: String) {
    if (resolved.previous == null && resolved.next == null) return
    appendLine("""<nav class="pager" aria-label="Previous and next page">""")
    resolved.previous?.let {
        appendLine("""<a class="prev" href="${href(base, it.slug, here)}">""")
        appendLine("""<span class="pager-dir">Previous</span><span class="pager-title">${it.title.escapeHtml()}</span>""")
        appendLine("</a>")
    }
    resolved.next?.let {
        appendLine("""<a class="next" href="${href(base, it.slug, here)}">""")
        appendLine("""<span class="pager-dir">Next</span><span class="pager-title">${it.title.escapeHtml()}</span>""")
        appendLine("</a>")
    }
    appendLine("</nav>")
}

private fun StringBuilder.appendFooter(site: Site) {
    appendLine("""<footer class="sitefoot">""")
    appendLine("""<span>${site.title.escapeHtml()} documentation</span>""")
    appendLine("""<span class="mono">generated from docs/ · Apache-2.0</span>""")
    appendLine("</footer>")
}

/**
 * The literal [Site.baseUrl] that switches every link from root-relative to page-relative.
 *
 * Root-relative URLs are what GitHub Pages wants, and they are useless anywhere the site is
 * not mounted at a known prefix — a `file://` browse, or a preview host that serves it from an
 * arbitrary path. Relative mode makes the same output portable to both.
 */
internal const val RELATIVE = "."

/**
 * URL for a slug, as seen from the page at [fromSlug].
 *
 * Pages are written as `<slug>/index.html` so the published URL has no `.html` suffix; the
 * landing page is the site root.
 */
internal fun href(base: String, slug: String, fromSlug: String = "index"): String {
    if (base != RELATIVE) return if (slug == "index") "$base/" else "$base/$slug/"
    val up = "../".repeat(depthOf(fromSlug))
    return if (slug == "index") "${up}index.html" else "$up$slug/index.html"
}

/** Asset URL, as seen from the page at [fromSlug]. */
internal fun asset(base: String, name: String, fromSlug: String = "index"): String =
    if (base == RELATIVE) "${"../".repeat(depthOf(fromSlug))}assets/$name" else "$base/assets/$name"

/** How many directories deep a slug's `index.html` sits below the site root. */
private fun depthOf(slug: String): Int = if (slug == "index") 0 else slug.count { it == '/' } + 1
