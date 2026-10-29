package data.repository.impl

import data.repository.impl.core.mapper.HierarchyDumpStdout
import data.repository.impl.core.mapper.HierarchySnapshotMapper
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * TAC-1: [HierarchySnapshotMapper.toModel] threads [HierarchyDumpStdout.dump] into
 * [domain.core.source.model.device.HierarchySnapshotModel.rawDump] VERBATIM — trailer and all.
 *
 * `parseUiAutomatorDump` only keeps the attributes measure-mode needs, so re-synthesizing XML
 * from the parsed nodes at send time would ship a document the device never emitted; the raw
 * stdout this mapper already holds is the only non-lossy source, which is why this asserts
 * exact equality rather than a substring or a normalized form.
 */
class HierarchySnapshotMapperTest {

    @Test
    fun `toModel carries the raw dump stdout verbatim including the adb trailer`() {
        val snapshot = HierarchySnapshotMapper.toModel.map(
            HierarchyDumpStdout(dump = PARSEABLE_DUMP, density = DENSITY_LINE)
        )

        assertEquals(PARSEABLE_DUMP, snapshot.rawDump)
    }

    @Test
    fun `toModel carries the raw dump stdout verbatim even when nothing parses`() {
        val snapshot = HierarchySnapshotMapper.toModel.map(
            HierarchyDumpStdout(dump = "not xml at all, just garbage", density = "")
        )

        assertEquals("not xml at all, just garbage", snapshot.rawDump)
    }
}

/**
 * Root `android.widget.FrameLayout` with one tappable child, followed by the AOSP "hierchary"
 * trailer — same shape as the frozen `DeviceHierarchyDumpTest` fixture, which is exactly the
 * detail this test depends on: a trailing, non-XML line that a re-synthesized document would
 * never reproduce.
 */
private const val PARSEABLE_DUMP = """<?xml version='1.0' encoding='UTF-8' standalone='yes' ?><hierarchy rotation="0"><node index="0" text="" resource-id="" class="android.widget.FrameLayout" package="dev.rectify.io" content-desc="" clickable="false" bounds="[0,0][1080,2400]"><node index="0" text="OK" resource-id="android:id/button1" class="android.widget.Button" package="dev.rectify.io" content-desc="" clickable="true" bounds="[100,200][300,260]" /></node></hierarchy>UI hierchary dumped to: /dev/tty
"""

private const val DENSITY_LINE = "Physical density: 420\n"
