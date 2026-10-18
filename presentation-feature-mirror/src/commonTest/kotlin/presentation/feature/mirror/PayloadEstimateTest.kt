package presentation.feature.mirror

import presentation.feature.mirror.core.payload.PAYLOAD_FORMAT_VERSION
import presentation.feature.mirror.core.payload.footerLabel
import presentation.feature.mirror.core.payload.payloadSizeLabel
import presentation.feature.mirror.core.payload.tokenEstimate
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * PayloadEstimate (T-2 / AC-16, AC-78): the payload preview's size/token/footer estimates.
 * `tokenEstimate` and `payloadSizeLabel` are a deliberate heuristic, not a real tokenizer or
 * byte count -- the UI labels this "≈ tokens" so pinning exact numbers here is honest, not a
 * false precision claim. `footerLabel` is machine text pinned character-exactly, including the
 * middle dot (U+00B7) -- never the ASCII period or a bullet lookalike.
 *
 * The size label's rounding is the sharp edge: `1.6 + count * 0.12` does not land on clean
 * decimals in binary floating point (count 2 computes to 1.8400000000000002, count 12 to
 * 3.0400000000000005). Every count below is pinned to the single correctly-rounded decimal
 * digit the UI must show -- never the raw multi-decimal double. Count 2 is the chosen
 * discriminator: its hundredths digit (4) is the largest among counts 0/1/2 and, unlike count
 * 0's already-clean 1.6, its raw unrounded value has two decimal digits (1.84) that a naive
 * (non-rounding) implementation -- e.g. one that just appends the raw double's own toString --
 * would leak into the label as "1.84 MB" or worse "1.8400000000000002 MB" instead of the
 * correctly-rounded "1.8 MB". Pinning the exact rounded string forces real one-decimal rounding
 * rather than cosmetic truncation of the raw double.
 */
class PayloadEstimateTest {

    // TAC-1: token estimate is exact for count 0, the base offset with no crops added.
    @Test
    fun tokenEstimateForZeroCropsIsTheBaseOffset() {
        assertEquals(360, tokenEstimate(0))
    }

    // TAC-1: token estimate is exact for count 1.
    @Test
    fun tokenEstimateForOneCropAddsOneHundredForty() {
        assertEquals(500, tokenEstimate(1))
    }

    // TAC-1: token estimate is exact for count 2.
    @Test
    fun tokenEstimateForTwoCropsAddsTwoHundredEighty() {
        assertEquals(640, tokenEstimate(2))
    }

    // TAC-1: token estimate is exact for count 12.
    @Test
    fun tokenEstimateForTwelveCropsIsExact() {
        assertEquals(2040, tokenEstimate(12))
    }

    // TAC-1: token estimate is exact for count 3 -- see payloadSizeLabelForThreeCrops... below
    // for why count 3 is in this suite even though the task text names only 0/1/2/12.
    @Test
    fun tokenEstimateForThreeCropsIsExact() {
        assertEquals(780, tokenEstimate(3))
    }

    // TAC-1: count 0's raw double (1.6) already has a single decimal digit -- this pins the
    // non-rounding base case, not the rounding boundary itself.
    @Test
    fun payloadSizeLabelForZeroCropsIsOnePointSixMb() {
        assertEquals("1.6 MB", payloadSizeLabel(0))
    }

    // TAC-1: count 1's raw value is 1.72000000000000019540 (two decimal digits in the clean
    // decimal reading) -- must round down to the nearest tenth, not leak "1.72 MB".
    @Test
    fun payloadSizeLabelForOneCropRoundsDownToOnePointSeven() {
        assertEquals("1.7 MB", payloadSizeLabel(1))
    }

    // TAC-1 (rounding-boundary discriminator -- see class KDoc): count 2's raw value is
    // 1.8400000000000000799..., hundredths digit 4, the largest among counts 0/1/2. A naive
    // implementation that skips rounding (e.g. appending the raw double's toString) would
    // produce "1.84 MB" or a longer floating-point tail, not the correctly-rounded "1.8 MB"
    // this pins.
    @Test
    fun payloadSizeLabelForTwoCropsRoundsDownToOnePointEightNotTheRawDouble() {
        assertEquals("1.8 MB", payloadSizeLabel(2))
    }

    // TAC-1: count 12's raw value is 3.04000000000000003552... -- must render as a clean one
    // tenth, not "3.04 MB" nor a floating-point tail.
    @Test
    fun payloadSizeLabelForTwelveCropsRoundsDownToThreePointZero() {
        assertEquals("3.0 MB", payloadSizeLabel(12))
    }

    // TAC-1 (the actual rounding boundary): count 3's raw value is 1.96 -- its hundredths digit
    // (6) is >= 5, so correct rounding and truncation genuinely diverge here: round gives 2.0,
    // truncate gives 1.9. The task text names only 0/1/2/12, but none of those four has a
    // hundredths digit >= 5 (0, 2, 4, 4) -- truncation and rounding agree on all of them, so
    // they cannot pin TAC-1's "including the rounding boundary" clause by themselves. Count 3
    // is added because it is the first count where the two behaviours actually disagree; a
    // truncating (non-rounding) implementation passes every other assertion in this class and
    // fails only this one.
    @Test
    fun payloadSizeLabelForThreeCropsRoundsUpAcrossTheTenthsBoundary() {
        assertEquals("2.0 MB", payloadSizeLabel(3))
    }

    // Leading space before the unit is load-bearing: "1.6MB" silently drops it.
    @Test
    fun payloadSizeLabelHasALeadingSpaceBeforeTheUnit() {
        assertEquals(true, payloadSizeLabel(0).contains(" MB"))
    }

    // TAC-2 / TAC-1: footer label for zero crops -- exact string, middle dot included.
    @Test
    fun footerLabelForZeroCropsIsExact() {
        assertEquals("frame.png · 0 crops · hierarchy.xml", footerLabel(0))
    }

    // TAC-2 / TAC-1: footer label for one crop -- exact string, middle dot included.
    @Test
    fun footerLabelForOneCropIsExact() {
        assertEquals("frame.png · 1 crops · hierarchy.xml", footerLabel(1))
    }

    // TAC-2 / TAC-1: footer label for two crops -- exact string, middle dot included.
    @Test
    fun footerLabelForTwoCropsIsExact() {
        assertEquals("frame.png · 2 crops · hierarchy.xml", footerLabel(2))
    }

    // TAC-2 / TAC-1: footer label for twelve crops -- exact string, middle dot included.
    @Test
    fun footerLabelForTwelveCropsIsExact() {
        assertEquals("frame.png · 12 crops · hierarchy.xml", footerLabel(12))
    }

    // TAC-2 / TAC-1: footer label for three crops -- exact string, pinned alongside
    // tokenEstimate(3) and payloadSizeLabel(3) so count 3 (the rounding-boundary count) is
    // covered consistently across all three functions, not only the size label.
    @Test
    fun footerLabelForThreeCropsIsExact() {
        assertEquals("frame.png · 3 crops · hierarchy.xml", footerLabel(3))
    }

    // TAC-2: the separator is the literal middle dot U+00B7, never an ASCII period or a bullet
    // lookalike (U+2022). The only two ASCII periods allowed are the ones inside the fixed
    // file extensions "frame.png" and "hierarchy.xml" -- stripping those names out must leave
    // no stray period behind.
    @Test
    fun footerLabelUsesTheLiteralMiddleDotNotAPeriodOrABullet() {
        val label = footerLabel(3)
        assertEquals(true, '·' in label)
        assertEquals(false, '•' in label)
        val withoutFileNames = label.replace("frame.png", "").replace("hierarchy.xml", "")
        assertEquals(false, '.' in withoutFileNames)
    }

    // TAC-3: the payload format version is pinned at 1 -- bumping it is a deliberate,
    // reviewed decision, not an accident.
    @Test
    fun payloadFormatVersionIsPinnedAtOne() {
        assertEquals(1, PAYLOAD_FORMAT_VERSION)
    }
}
