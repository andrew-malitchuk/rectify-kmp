package data.device.api

import data.device.api.source.parse.parseUiAutomatorDump
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Pins `parseUiAutomatorDump` against the three real T-1 fixtures plus synthetic noise,
 * and the T-2 gate rulings: cut the `"hierchary"` trailer, tolerate a single-quoted XML
 * declaration, normalise inverted rects, and resolve node identity through the four-step
 * `resource-id` -> `text` -> `content-desc` -> `class[n]` chain. [HierarchyNodeResource]
 * (not yet written -- this is TDD red) must carry `text` and `content-desc` alongside
 * `bounds`, `class`, `resource-id`, `clickable` and `depth`.
 */
class UiAutomatorDumpParserTest {

    // --- 1. The output is not XML: cut at </hierarchy>, drop the "hierchary" trailer ---

    @Test
    fun `parseUiAutomatorDump cuts at the closing hierarchy tag and drops the hierchary trailer`() {
        val settings = parseUiAutomatorDump(VIEW_BASED_SETTINGS_DUMP)
        val compose = parseUiAutomatorDump(COMPOSE_APP_DUMP)
        val midScroll = parseUiAutomatorDump(MID_SCROLL_LIST_DUMP)

        assertEquals(69, settings.size)
        assertEquals(17, compose.size)
        assertEquals(79, midScroll.size)
        assertTrue((settings + compose + midScroll).none { node ->
            node.text.contains("hierchary") || node.contentDesc.contains("hierchary")
        })
    }

    @Test
    fun `parseUiAutomatorDump returns an empty list for the trailer alone`() {
        assertEquals(emptyList(), parseUiAutomatorDump(UIAUTOMATOR_DUMP_TRAILER))
    }

    // --- 2. Single-quoted XML declaration ---

    @Test
    fun `parseUiAutomatorDump accepts a single-quoted xml declaration`() {
        val xml = """<?xml version='1.0' encoding='UTF-8' standalone='yes' ?><hierarchy rotation="0"><node index="0" text="" resource-id="" class="android.widget.FrameLayout" content-desc="" clickable="false" bounds="[0,0][10,20]" /></hierarchy>""" +
            UIAUTOMATOR_DUMP_TRAILER

        val result = parseUiAutomatorDump(xml)

        assertEquals(1, result.size)
        assertEquals("android.widget.FrameLayout", result.single().className)
        assertEquals(0, result.single().left)
        assertEquals(0, result.single().top)
        assertEquals(10, result.single().right)
        assertEquals(20, result.single().bottom)
    }

    // --- 3. Inverted rects normalised on parse ---

    @Test
    fun `parseUiAutomatorDump normalises the inverted Wallpaper and style rect`() {
        val node = parseUiAutomatorDump(VIEW_BASED_SETTINGS_DUMP)
            .single { it.text == "Wallpaper & style" }

        assertEquals("android:id/title", node.resourceId)
        assertEquals("android.widget.TextView", node.className)
        assertEquals(221, node.left)
        assertEquals(2069, node.top)
        assertEquals(568, node.right)
        assertEquals(2080, node.bottom)
        assertTrue(node.bottom >= node.top)
        assertTrue(node.right >= node.left)
    }

    // --- 4. Four-step node identity chain ---

    @Test
    fun `parseUiAutomatorDump resolves node id from resource-id when present`() {
        val node = parseUiAutomatorDump(VIEW_BASED_SETTINGS_DUMP)
            .single { it.resourceId == "com.android.settings:id/search_action_bar" }

        assertEquals("com.android.settings:id/search_action_bar", node.nodeId)
    }

    @Test
    fun `parseUiAutomatorDump falls through to text when resource-id is empty`() {
        val node = parseUiAutomatorDump(COMPOSE_APP_DUMP).single { it.text == "Grayscale" }

        assertEquals("", node.resourceId)
        assertEquals("Grayscale", node.nodeId)
    }

    @Test
    fun `parseUiAutomatorDump falls through to content-desc when resource-id and text are empty`() {
        val node = parseUiAutomatorDump(MID_SCROLL_LIST_DUMP)
            .single { it.contentDesc == "Navigate up" }

        assertEquals("", node.resourceId)
        assertEquals("", node.text)
        assertEquals("Navigate up", node.nodeId)
    }

    @Test
    fun `parseUiAutomatorDump assigns a collision index to anonymous Compose views sharing a class`() {
        val anonymousViews = parseUiAutomatorDump(COMPOSE_APP_DUMP)
            .filter { it.className == "android.view.View" }

        assertEquals(8, anonymousViews.size)
        assertEquals(
            (0..7).map { "android.view.View[$it]" },
            anonymousViews.map { it.nodeId },
        )
    }

    // --- 5. UTF-8 and entities ---

    @Test
    fun `parseUiAutomatorDump unescapes exactly the five xml entities`() {
        val xml = """<?xml version='1.0' encoding='UTF-8' standalone='yes' ?><hierarchy rotation="0"><node index="0" text="&amp;&lt;&gt;&quot;&apos;" resource-id="" class="android.widget.TextView" content-desc="" clickable="false" bounds="[0,0][10,10]" /></hierarchy>""" +
            UIAUTOMATOR_DUMP_TRAILER

        val node = parseUiAutomatorDump(xml).single()

        assertEquals("&<>\"'", node.text)
    }

    @Test
    fun `parseUiAutomatorDump preserves the non-breaking hyphen in Settings text`() {
        val node = parseUiAutomatorDump(VIEW_BASED_SETTINGS_DUMP)
            .single { it.text.contains('‑') }

        assertEquals("Mobile, Wi‑Fi, hotspot", node.text)
    }

    // --- 6. Never throws on noise ---

    @Test
    fun `parseUiAutomatorDump returns an empty list for an empty string`() {
        assertEquals(emptyList(), parseUiAutomatorDump(""))
    }

    @Test
    fun `parseUiAutomatorDump returns an empty list for truncated input`() {
        val truncated = """<?xml version='1.0' encoding='UTF-8' standalone='yes' ?><hierarchy rotation="0"><node index="0" text="" resource-id="" class="android.widget.FrameLayout" bounds="[0,0][984"""

        assertEquals(emptyList(), parseUiAutomatorDump(truncated))
    }

    @Test
    fun `parseUiAutomatorDump skips a node with malformed bounds instead of throwing`() {
        val xml = """<?xml version='1.0' encoding='UTF-8' standalone='yes' ?><hierarchy rotation="0"><node index="0" text="" resource-id="bad" class="android.widget.FrameLayout" content-desc="" clickable="false" bounds="[1,2][3]" /><node index="1" text="" resource-id="good" class="android.widget.FrameLayout" content-desc="" clickable="false" bounds="[0,0][10,10]" /></hierarchy>""" +
            UIAUTOMATOR_DUMP_TRAILER

        val result = parseUiAutomatorDump(xml)

        assertEquals(1, result.size)
        assertEquals("good", result.single().resourceId)
    }

    @Test
    fun `parseUiAutomatorDump skips a node whose bounds are not the bracket shape at all`() {
        val xml = """<?xml version='1.0' encoding='UTF-8' standalone='yes' ?><hierarchy rotation="0"><node index="0" text="" resource-id="lonely" class="android.widget.FrameLayout" content-desc="" clickable="false" bounds="not-a-rect" /></hierarchy>""" +
            UIAUTOMATOR_DUMP_TRAILER

        assertEquals(emptyList(), parseUiAutomatorDump(xml))
    }

    @Test
    fun `parseUiAutomatorDump tolerates a greater-than character inside an attribute value`() {
        val xml = """<?xml version='1.0' encoding='UTF-8' standalone='yes' ?><hierarchy rotation="0"><node index="0" text="" resource-id="" class="android.widget.TextView" content-desc="1 > 2" clickable="false" bounds="[0,0][10,10]" /></hierarchy>""" +
            UIAUTOMATOR_DUMP_TRAILER

        val node = parseUiAutomatorDump(xml).single()

        assertEquals("1 > 2", node.contentDesc)
        assertEquals(0, node.left)
        assertEquals(10, node.right)
    }

    // --- 7. Structural fidelity against the real fixtures ---

    @Test
    fun `parseUiAutomatorDump walks the Settings dump depth-first with exact bounds and depth`() {
        val nodes = parseUiAutomatorDump(VIEW_BASED_SETTINGS_DUMP)

        val root = nodes[0]
        assertEquals("android.widget.FrameLayout", root.className)
        assertEquals(0, root.depth)

        val searchActionBar = nodes[7]
        assertEquals("com.android.settings:id/search_action_bar", searchActionBar.resourceId)
        assertEquals(7, searchActionBar.depth)
        assertTrue(searchActionBar.clickable)
        assertEquals(42, searchActionBar.left)
        assertEquals(138, searchActionBar.top)
        assertEquals(942, searchActionBar.right)
        assertEquals(327, searchActionBar.bottom)

        val imageView = nodes[8]
        assertEquals("com.android.settings:id/imageView", imageView.resourceId)
        assertEquals(8, imageView.depth)
        assertEquals(105, imageView.left)
        assertEquals(201, imageView.top)
        assertEquals(168, imageView.right)
        assertEquals(264, imageView.bottom)

        val searchBarTitle = nodes[9]
        assertEquals("com.android.settings:id/search_bar_title", searchBarTitle.resourceId)
        assertEquals("Search Settings", searchBarTitle.text)
        assertEquals(8, searchBarTitle.depth)

        val mainContentScrollable = nodes[10]
        assertEquals(
            "com.android.settings:id/main_content_scrollable_container",
            mainContentScrollable.resourceId,
        )
        assertEquals(4, mainContentScrollable.depth)
        assertEquals(0, mainContentScrollable.left)
        assertEquals(348, mainContentScrollable.top)
        assertEquals(984, mainContentScrollable.right)
        assertEquals(2069, mainContentScrollable.bottom)
    }

    @Test
    fun `parseUiAutomatorDump does not confuse scrollable with clickable`() {
        val node = parseUiAutomatorDump(VIEW_BASED_SETTINGS_DUMP)
            .single { it.resourceId == "com.android.settings:id/main_content_scrollable_container" }

        assertTrue(!node.clickable)
    }

    @Test
    fun `parseUiAutomatorDump reports an empty resource-id and clickable true on the Compose fixture`() {
        val node = parseUiAutomatorDump(MID_SCROLL_LIST_DUMP)
            .single { it.left == 42 && it.top == 201 && it.right == 942 && it.bottom == 336 }

        assertEquals("", node.resourceId)
        assertEquals("android.view.View", node.className)
        assertTrue(node.clickable)
    }
}
