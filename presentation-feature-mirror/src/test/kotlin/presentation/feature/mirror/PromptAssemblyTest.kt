package presentation.feature.mirror

import androidx.compose.ui.unit.IntSize
import presentation.feature.mirror.core.annotation.TIMES
import presentation.feature.mirror.core.measure.noteLine
import presentation.feature.mirror.core.payload.buildPrompt
import presentation.feature.mirror.source.mirror.DeviceRect
import presentation.feature.mirror.source.mirror.FrozenFrame
import presentation.feature.mirror.source.mirror.MeasureSide
import presentation.feature.mirror.source.mirror.MeasureValue
import presentation.feature.mirror.source.mirror.StagedAnnotation
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * T-3 / AC-13, AC-72, AC-73 -- `buildPrompt` ports the design's `pl[]` assembly (see
 * design-payload-preview.md) with three deliberate divergences, all pinned here rather than
 * restored: (1) the device line is real (`frozen.deviceLabel` + `frozen.deviceSize`), never the
 * design's hardcoded `"Android 15 · 1344×2992"` literal -- every fixture below therefore uses
 * device numbers that do NOT match that literal, so a hardcoded implementation cannot pass by
 * coincidence; (2) there is no separate `measured:` branch -- a comment already carrying a `⌥`
 * measurement line (from `core/measure`'s own `noteLine`) must survive byte-identical; (3) the
 * headline drops the design's `" on the Cart screen."` clause -- this codebase has no
 * screen-name source anywhere; and (4) **AF-4** -- the trailer is an inventory of what is
 * really attached, not the design's fixed `"Attached: frame.png, N crops, hierarchy.xml"`
 * manifest. The cases above call `buildPrompt` with its defaults (no hierarchy, every staged
 * annotation cropped), which is why they read `Attached: frame.png, N crop(s)`; the AF-4 cases
 * at the bottom drive the other combinations.
 */
class PromptAssemblyTest {

    // TAC-1 / TAC-2: one annotation -- documented block shape exactly (singular headline, the
    // real device line, the "#1 {node} · {w}×{h}" line, the comment line indented 3 spaces,
    // the blank line between blocks, the trailing "Attached:" line) AND an empty comment
    // renders "no comment". The trailer reads "1 crop": AF-4 fixed "1 crops" with it.
    @Test
    fun oneAnnotationAssemblesTheDocumentedBlockShapeWithNoCommentFallback() {
        val frozen = FrozenFrame(
            deviceSize = IntSize(1440, 3120),
            deviceLabel = "Android 14",
            annotations = listOf(
                StagedAnnotation(index = 1, rect = DeviceRect(0, 0, 64, 32), comment = ""),
            ),
        )

        val prompt = buildPrompt(frozen) { "Button#submit" }

        val expected = """
            Fix 1 UI issue.
            Android 14 · 1440×3120

            #1 Button#submit · 64×32
               "no comment"

            Attached: frame.png, 1 crop
        """.trimIndent()
        assertEquals(expected, prompt)
    }

    // TAC-1 / TAC-2: two annotations -- plural headline, a blank line between EACH block (not
    // just after the device line), and a comment that already carries a real `⌥` measurement
    // line (built via core/measure's own noteLine, the same function measure-mode uses) passes
    // through byte-identical, with no separate "measured:" segment appended anywhere.
    @Test
    fun twoAnnotationsAssembleTheDocumentedBlockShapeWithMeasurementCommentPassedThroughVerbatim() {
        val measurementComment = noteLine(
            nodeId = "TextView#price",
            values = listOf(
                MeasureValue(MeasureSide.Size, "48×17dp"),
                MeasureValue(MeasureSide.Top, "15dp"),
                MeasureValue(MeasureSide.Right, "20dp"),
                MeasureValue(MeasureSide.Bottom, "49dp"),
                MeasureValue(MeasureSide.Left, "62dp"),
            ),
            finding = null,
        )
        assertEquals(
            "⌥ TextView#price 48×17dp · top 15dp · right 20dp · bottom 49dp · left 62dp",
            measurementComment,
        )

        val frozen = FrozenFrame(
            deviceSize = IntSize(1080, 2400),
            deviceLabel = "iOS 17",
            annotations = listOf(
                StagedAnnotation(index = 1, rect = DeviceRect(0, 0, 48, 17), comment = "Looks cramped"),
                StagedAnnotation(index = 2, rect = DeviceRect(0, 0, 300, 120), comment = measurementComment),
            ),
        )
        val nodeIds = mapOf(1 to "TextView#price", 2 to "ImageView#icon")

        val prompt = buildPrompt(frozen) { annotation -> nodeIds[annotation.index] }

        val expected = """
            Fix 2 UI issues.
            iOS 17 · 1080×2400

            #1 TextView#price · 48×17
               "Looks cramped"

            #2 ImageView#icon · 300×120
               "⌥ TextView#price 48×17dp · top 15dp · right 20dp · bottom 49dp · left 62dp"

            Attached: frame.png, 2 crops
        """.trimIndent()
        assertEquals(expected, prompt)
        assertFalse(prompt.contains("measured:"))
    }

    // TAC-4: no node match (nodeIdFor returns null) omits the node id segment entirely --
    // "#1 64×32", never a stray " · " left behind, never the measure panel's own
    // "View (no match)" sentinel leaking into a prompt sent to Claude. See the final message
    // for the full reasoning: nodeIdFor is nullable precisely so the "no candidate" case can be
    // represented as "nothing to say" rather than invented text.
    @Test
    fun noNodeMatchOmitsTheNodeIdSegmentWithoutBreakingTheLine() {
        val frozen = FrozenFrame(
            deviceSize = IntSize(1200, 2600),
            deviceLabel = "Android 13",
            annotations = listOf(
                StagedAnnotation(index = 1, rect = DeviceRect(0, 0, 64, 32), comment = "Spacing off"),
            ),
        )

        val prompt = buildPrompt(frozen) { null }

        val expected = """
            Fix 1 UI issue.
            Android 13 · 1200×2600

            #1 64×32
               "Spacing off"

            Attached: frame.png, 1 crop
        """.trimIndent()
        assertEquals(expected, prompt)
        assertFalse(prompt.contains("View (no match)"))
        assertFalse(prompt.contains("· ·"))
        assertFalse(prompt.lines().any { it.startsWith("#1 ·") || it.startsWith("#1  ") })
    }

    // TAC-3: the dimension pair is U+00D7 unspaced, built by interpolating the shared TIMES
    // constant directly. AnnotationFormat.sizeLabel space-pads ("48 × 17"); this asserts both
    // the correct unspaced substring (constructed from TIMES itself, not a hand-typed glyph)
    // and the total absence of the spaced variant anywhere in the output.
    @Test
    fun dimensionPairIsUnspacedTimesNeverTheSpacePaddedSizeLabel() {
        val frozen = FrozenFrame(
            deviceSize = IntSize(900, 1600),
            deviceLabel = "Android 12",
            annotations = listOf(
                StagedAnnotation(index = 1, rect = DeviceRect(0, 0, 48, 17), comment = "x"),
            ),
        )

        val prompt = buildPrompt(frozen) { "TextView#price" }

        assertTrue(prompt.contains("48${TIMES}17"))
        assertFalse(prompt.contains(" $TIMES "))
    }

    // -- AF-4 (Stage 5): the trailer names what is actually attached -----------------------------

    // The hierarchy segment appears only when the payload really carries the XML, and when it
    // does it must NOT name a file: no `hierarchy.xml` has ever existed on any path -- the XML
    // rides inline in the envelope's `hierarchyXml` field, which the MCP server then renders
    // under the tool result's `hierarchy.xml` JSON key. The old literal sent the agent looking
    // for a file on disk.
    @Test
    fun anAttachedHierarchyIsNamedAsInlineXmlAndNeverAsAFile() {
        val frozen = oneAnnotationFrame()

        val prompt = buildPrompt(frozen, hierarchyAttached = true) { "Button#submit" }

        assertEquals("Attached: frame.png, 1 crop, inline view hierarchy XML (no file)", prompt.lines().last())
        assertFalse(prompt.contains("hierarchy.xml"))
    }

    // A hierarchy-less send -- iOS, or an Absent/Unavailable dump -- ships hierarchyAvailable =
    // false and hierarchyXml = "". The prompt stays silent rather than promising a hierarchy.
    @Test
    fun noHierarchyDropsTheHierarchySegmentEntirely() {
        val prompt = buildPrompt(oneAnnotationFrame(), hierarchyAttached = false) { "Button#submit" }

        assertEquals("Attached: frame.png, 1 crop", prompt.lines().last())
        assertFalse(prompt.contains("hierarchy"))
    }

    // The count is what EnvelopeAssembly actually attached, not what was staged: an annotation
    // whose crop failed to encode gets cropPath = null and no HandoffImage, so promising it
    // would send the agent after a crop that is not in the payload.
    @Test
    fun theCropCountIsTheAttachedCountNotTheStagedCount() {
        val frozen = FrozenFrame(
            deviceSize = IntSize(1440, 3120),
            deviceLabel = "Android 14",
            annotations = (1..3).map {
                StagedAnnotation(index = it, rect = DeviceRect(0, 0, 64, 32), comment = "c")
            },
        )

        // Three staged, one encode failed.
        val prompt = buildPrompt(frozen, cropCount = 2, hierarchyAttached = true) { null }

        assertEquals("Fix 3 UI issues.", prompt.lines().first())
        assertEquals("Attached: frame.png, 2 crops, inline view hierarchy XML (no file)", prompt.lines().last())
    }

    // Every encode failed: the crop segment is dropped rather than printed as "0 crops".
    @Test
    fun zeroAttachedCropsDropTheCropSegment() {
        val prompt = buildPrompt(oneAnnotationFrame(), cropCount = 0) { null }

        assertEquals("Attached: frame.png", prompt.lines().last())
        assertFalse(prompt.contains("crop"))
    }

    private fun oneAnnotationFrame() = FrozenFrame(
        deviceSize = IntSize(1440, 3120),
        deviceLabel = "Android 14",
        annotations = listOf(
            StagedAnnotation(index = 1, rect = DeviceRect(0, 0, 64, 32), comment = "Spacing off"),
        ),
    )

    // RCR-2 / F-2: an emulator row carries no detail at all -- EmulatorModel.toRow never sets
    // it, MirrorRow.detail defaults to "" and MirrorViewModel's lookup ends in .orEmpty() -- so
    // deviceLabel is blank on a first-class path (the app boots AVDs on request). The label
    // segment AND its separator are both dropped, the same way a null node id is dropped from an
    // annotation head line, so the device line degrades to the bare resolution rather than to a
    // leading-space dangling-dot " · 1080×2400". No placeholder and no invented device
    // name: the rule that drops the design's " on the Cart screen." clause forbids fabricating
    // data into a prompt sent to Claude, and a blank label is exactly nothing to say.
    @Test
    fun blankDeviceLabelDegradesTheDeviceLineToTheResolutionAlone() {
        val frozen = FrozenFrame(
            deviceSize = IntSize(1080, 2400),
            deviceLabel = "",
            annotations = listOf(
                StagedAnnotation(index = 1, rect = DeviceRect(0, 0, 320, 180), comment = "Padding looks off"),
            ),
        )

        val prompt = buildPrompt(frozen) { "ImageView#banner" }

        val expected = """
            Fix 1 UI issue.
            1080×2400

            #1 ImageView#banner · 320×180
               "Padding looks off"

            Attached: frame.png, 1 crop
        """.trimIndent()
        assertEquals(expected, prompt)
        val deviceLine = prompt.lines()[1]
        assertFalse(deviceLine.startsWith(" ") || deviceLine.contains("·"))
    }
}
