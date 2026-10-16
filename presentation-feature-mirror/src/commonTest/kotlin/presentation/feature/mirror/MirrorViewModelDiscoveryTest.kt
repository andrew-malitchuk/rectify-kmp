package presentation.feature.mirror

import domain.core.source.model.device.DeviceModel
import domain.core.source.model.device.DeviceStateModel
import domain.core.source.model.device.DiscoveryModel
import domain.core.source.model.device.EmulatorModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.orbitmvi.orbit.Orbit
import presentation.feature.mirror.source.mirror.MirrorRowGroup
import presentation.feature.mirror.source.mirror.MirrorRowKind
import presentation.feature.mirror.source.mirror.MirrorStage
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

private fun device(id: String, state: DeviceStateModel): DeviceModel = DeviceModel(
    id = id,
    displayName = id,
    serial = id,
    state = state,
    isEmulator = false,
)

@OptIn(ExperimentalCoroutinesApi::class)
class MirrorViewModelDiscoveryTest {

    /**
     * One dispatcher for both `Dispatchers.Main` and the test body — the ViewModel's
     * `viewModelScope` runs on Main, and two `StandardTestDispatcher`s would mean two
     * schedulers, so `advanceUntilIdle()` would never advance the ViewModel's work.
     */
    private val mainDispatcher = StandardTestDispatcher()

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(mainDispatcher)
        useTestDispatcherForOrbit(mainDispatcher)
    }

    @AfterTest
    fun tearDown() {
        Orbit.resetDefaults()
        Dispatchers.resetMain()
    }

    @Test
    fun aMissingToolIsASuccessValueThatEmptiesTheList() = runTest(mainDispatcher) {
        val discovery = FakeObserveDiscovery(
            listOf(
                DiscoveryModel.Found(physical = listOf(device("serial-1", DeviceStateModel.Device)), emulators = emptyList()),
                DiscoveryModel.ToolMissing(
                    tool = "adb",
                    probedPaths = listOf("/usr/local/bin/adb"),
                    installCommand = "brew install android-platform-tools",
                    envHint = "ANDROID_HOME",
                ),
            ),
        )
        val viewModel = mirrorViewModel(discovery = discovery)
        subscribe(viewModel)
        advanceUntilIdle()

        val state = viewModel.currentState
        assertIs<DiscoveryModel.ToolMissing>(state.discovery)
        assertTrue(state.rows.isEmpty())
        // Nothing is streaming and no tool answered, so the status indicator is offline.
        assertEquals(MirrorStage.Picker, state.stage)
    }

    @Test
    fun devicesFoundPopulateThePickerAndHighlightTheFirstSelectableRow() = runTest(mainDispatcher) {
        val discovery = FakeObserveDiscovery(
            listOf(
                DiscoveryModel.Found(
                    physical = listOf(
                        device("unauthorized-1", DeviceStateModel.Unauthorized),
                        device("serial-1", DeviceStateModel.Device),
                    ),
                    emulators = listOf(EmulatorModel(avdName = "Pixel_7_API_34", isRunning = false)),
                ),
            ),
        )
        val viewModel = mirrorViewModel(discovery = discovery)
        subscribe(viewModel)
        advanceUntilIdle()

        val state = viewModel.currentState
        assertEquals(3, state.rows.size)
        assertEquals(1, state.highlightedIndex)
        assertEquals("serial-1", state.highlightedRow?.id)
        assertEquals(MirrorStage.Picker, state.stage)
        assertEquals(MirrorRowGroup.Emulator, state.rows[2].group)
        assertEquals(MirrorRowKind.Boot, state.rows[2].kind)
    }

    @Test
    fun sustainedEmptyPollsSurfaceNoDeviceFoundWhileTheStageStaysPassive() = runTest(mainDispatcher) {
        val discovery = FakeObserveDiscovery(
            listOf(
                DiscoveryModel.Found(
                    physical = emptyList(),
                    emulators = listOf(EmulatorModel(avdName = "Pixel_7_API_34", isRunning = false)),
                ),
                DiscoveryModel.NoDeviceFound(
                    toolVersion = "35.0.1",
                    lastPollEpochMs = 1_700_000_000_000L,
                    attempts = 3,
                    attachedCount = 1,
                ),
            ),
        )
        val viewModel = mirrorViewModel(discovery = discovery)
        subscribe(viewModel)
        advanceUntilIdle()

        val state = viewModel.currentState
        val escalated = assertIs<DiscoveryModel.NoDeviceFound>(state.discovery)
        assertEquals(3, escalated.attempts)
        assertEquals(1, escalated.attachedCount)
        // F-2 shows only the passive waiting stage; the escalated card is F-3's.
        assertEquals(MirrorStage.NoSignal, state.stage)
    }

    @Test
    fun theLastKnownEmulatorsSurviveAnEscalatedPoll() = runTest(mainDispatcher) {
        val discovery = FakeObserveDiscovery(
            listOf(
                DiscoveryModel.Found(
                    physical = listOf(device("serial-1", DeviceStateModel.Device)),
                    emulators = listOf(EmulatorModel(avdName = "Pixel_7_API_34", isRunning = true, runningDeviceId = "emulator-5554")),
                ),
                DiscoveryModel.NoDeviceFound(
                    toolVersion = "35.0.1",
                    lastPollEpochMs = 1_700_000_000_000L,
                    attempts = 3,
                    attachedCount = 0,
                ),
            ),
        )
        val viewModel = mirrorViewModel(discovery = discovery)
        subscribe(viewModel)
        advanceUntilIdle()

        val state = viewModel.currentState
        // NoDeviceFound carries no emulator list, so the AVD shortcut would go empty exactly
        // when the user needs it unless the last known list is retained.
        assertEquals(listOf("Pixel_7_API_34"), state.avds)
        assertEquals("Pixel_7_API_34", state.selectedAvd)
        assertEquals(1, state.rows.size)
        // The retained row is demoted: nothing is running if nothing is attached.
        assertEquals(MirrorRowKind.Boot, state.rows.single().kind)
        assertEquals("Pixel_7_API_34", state.rows.single().id)
    }

    @Test
    fun aFailedPollKeepsTheRowsItCouldNotRefresh() = runTest(mainDispatcher) {
        val discovery = FakeObserveDiscovery(
            listOf(
                DiscoveryModel.Found(
                    physical = listOf(device("serial-1", DeviceStateModel.Device)),
                    emulators = emptyList(),
                ),
                // Deliberately NOT the watchdog's wording. Presentation never classifies a
                // reason, it renders it, and this module cannot see
                // DeviceCommandException.TIMEOUT_REASON_PREFIX without putting the data layer
                // on its test classpath. A hand-typed copy of that string would be a fixture
                // claiming to be a timeout with nothing binding it to the real one.
                DiscoveryModel.PollFailed(tool = "adb", reason = "error: protocol fault"),
            ),
        )
        val viewModel = mirrorViewModel(discovery = discovery)
        subscribe(viewModel)
        advanceUntilIdle()

        val state = viewModel.currentState
        assertIs<DiscoveryModel.PollFailed>(state.discovery)
        // A poll that never answered learned nothing, so clearing the list would claim the
        // phone had been unplugged. ToolMissing clears, this does not.
        assertEquals(listOf("serial-1"), state.rows.map { it.id })
        assertEquals(MirrorStage.Picker, state.stage)
    }
}
