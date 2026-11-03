package dev.rectify.io.docs

import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * The generator's three pieces of real logic. Everything else in this module is string
 * concatenation whose output the link check in CI already exercises.
 */
class GeneratorTest {

    /**
     * Two sections can legitimately share a heading, and a duplicate `id` silently sends every
     * link to the first one. The suffix is what stops a table of contents quietly lying.
     *
     * MUTATION: make `uniqueSlug` return `base` unconditionally — this fails.
     */
    @Test
    fun repeatedHeadingsGetDistinctAnchors() {
        val rendered = renderMarkdown(
            """
            ## Notes
            ## Notes
            ## Notes
            """.trimIndent(),
        )

        assertEquals(
            expected = listOf("notes", "notes-2", "notes-3"),
            actual = rendered.headings.map { it.id },
        )
    }

    /**
     * Headings carry inline markup and punctuation; the slug has to survive both, and the
     * table of contents shows the text without the tags.
     *
     * Note this is NOT byte-identical to GitHub's slugger: GitHub turns each space into its
     * own dash, so dropped punctuation leaves `it--really`. Collapsing whitespace runs first
     * gives `it-really`, which is the nicer URL and the one this site publishes.
     */
    @Test
    fun headingTextIsStrippedOfMarkupAndPunctuation() {
        val rendered = renderMarkdown("## Why `main()` starts it — really")

        assertEquals(
            expected = listOf("text=Why main() starts it — really", "id=why-main-starts-it-really"),
            actual = listOf("text=${rendered.headings[0].text}", "id=${rendered.headings[0].id}"),
        )
    }

    /** Only h2/h3 reach the sidebar: h1 is the page title and h4 is too fine to navigate by. */
    @Test
    fun onlySecondAndThirdLevelHeadingsAreCollected() {
        val rendered = renderMarkdown(
            """
            # Page
            ## Two
            ### Three
            #### Four
            """.trimIndent(),
        )

        assertEquals(
            expected = listOf("two" to 2, "three" to 3),
            actual = rendered.headings.map { it.id to it.level },
        )
    }

    /**
     * The landing page is the site root and every other page is a directory, so published URLs
     * carry no `.html`. The base path prefixes both or neither.
     */
    @Test
    fun urlsAreExtensionlessAndCarryTheBasePath() {
        assertEquals(
            expected = listOf("/", "/guide/installation/", "/rectify-kmp/", "/rectify-kmp/guide/installation/"),
            actual = listOf(
                href("", "index"),
                href("", "guide/installation"),
                href("/rectify-kmp", "index"),
                href("/rectify-kmp", "guide/installation"),
            ),
        )
    }

    /** Prev/next walks the flattened sections, and the ends of the run have no neighbour. */
    @Test
    fun readingOrderLinksNeighboursAcrossSectionBoundaries() {
        val site = Site(
            title = "t",
            tagline = "t",
            sections = listOf(
                Section("One", listOf(page("a"), page("b"))),
                Section("Two", listOf(page("c"))),
            ),
        )

        assertEquals(
            expected = listOf("a: null>b", "b: a>c", "c: b>null"),
            actual = site.readingOrder().map {
                "${it.page.slug}: ${it.previous?.slug}>${it.next?.slug}"
            },
        )
    }

    private fun page(slug: String) = Page(title = slug, file = "$slug.md", slug = slug)
}
