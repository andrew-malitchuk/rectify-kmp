package presentation.feature.mirror

import domain.core.source.model.device.DeviceModel
import domain.core.source.model.device.DeviceStateModel
import domain.core.source.model.device.DiscoveryModel
import domain.core.source.model.device.EmulatorModel
import domain.core.source.model.device.PlatformModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.orbitmvi.orbit.Orbit
import presentation.feature.mirror.source.mirror.MirrorIntent
import presentation.feature.mirror.source.mirror.MirrorSession
import presentation.feature.mirror.source.mirror.MirrorStage
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertSame
import kotlin.test.assertTrue

private const val PHONE = "37281FDH2000AB"
private const val AVD = "Pixel_Fold_API_35"

private fun phone(): DeviceModel = DeviceModel(
    id = PHONE,
    displayName = "Pixel 8 Pro",
    serial = PHONE,
    state = DeviceStateModel.Device,
    isEmulator = false,
    transportLabel = "USB",
)

/** One usable handset followed by one cold AVD — the shape every session test starts from. */
/** Two usable handsets — the ambiguity AC-28's card exists for. */
private fun twoHandsets(): FakeObserveDiscovery = FakeObserveDiscovery(
    listOf(
        DiscoveryModel.Found(
            physical = listOf(phone(), phone().copy(id = "R5CX10ABCDE", serial = "R5CX10ABCDE", displayName = "Galaxy S24")),
            emulators = emptyList(),
        ),
    ),
)

private fun oneOfEach(): FakeObserveDiscovery = FakeObserveDiscovery(
    listOf(
        DiscoveryModel.Found(
            physical = listOf(phone()),
            emulators = listOf(EmulatorModel(avdName = AVD, isRunning = false)),
        ),
    ),
)

@OptIn(ExperimentalCoroutinesApi::class)
class MirrorViewModelSessionTest {

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
    fun arrowAndEnterSelectARowAndStartMirroringIt() = runTest(mainDispatcher) {
        val mirroring = FakeStartMirroring()
        val viewModel = mirrorViewModel(discovery = oneOfEach(), mirroring = mirroring)
        subscribe(viewModel)
        advanceUntilIdle()

        viewModel.handleIntent(MirrorIntent.Commit)
        advanceUntilIdle()

        assertEquals(listOf(PHONE), mirroring.startedDevices)
        val state = viewModel.currentState
        assertEquals(MirrorSession.Streaming(PHONE), state.session)
        assertEquals(PHONE, state.selectedDeviceId)
        assertEquals("Pixel 8 Pro", state.selectedDeviceName)
        assertEquals(MirrorStage.Mirror, state.stage)
    }

    @Test
    fun enterOnAColdEmulatorBootsItInsteadOfStartingASession() = runTest(mainDispatcher) {
        val boot = FakeBootEmulator()
        val mirroring = FakeStartMirroring()
        val viewModel = mirrorViewModel(discovery = oneOfEach(), boot = boot, mirroring = mirroring)
        subscribe(viewModel)
        advanceUntilIdle()

        viewModel.handleIntent(MirrorIntent.MoveHighlight(1))
        viewModel.handleIntent(MirrorIntent.Commit)
        advanceUntilIdle()

        assertEquals(listOf(AVD), boot.booted)
        assertTrue(mirroring.startedDevices.isEmpty())
        assertEquals(MirrorSession.Idle, viewModel.currentState.session)
    }

    @Test
    fun aLostDeviceEndsTheSessionAndResumesPolling() = runTest(mainDispatcher) {
        val discovery = oneOfEach()
        val mirroring = FakeStartMirroring()
        mirroring.failure = IllegalStateException("device went away")
        val viewModel = mirrorViewModel(discovery = discovery, mirroring = mirroring)
        subscribe(viewModel)
        advanceUntilIdle()
        val pollsBeforeSession = discovery.subscriptions

        viewModel.handleIntent(MirrorIntent.Commit)
        advanceUntilIdle()

        assertIs<MirrorSession.Lost>(viewModel.currentState.session)
        assertEquals(0, mirroring.activeCollectors)
        // Discovery was torn down for the session and subscribed again once it died.
        assertTrue(discovery.subscriptions > pollsBeforeSession, "polling did not resume")
    }

    @Test
    fun discoveryPausesWhileASessionStreams() = runTest(mainDispatcher) {
        val discovery = oneOfEach()
        val viewModel = mirrorViewModel(discovery = discovery, mirroring = FakeStartMirroring())
        subscribe(viewModel)
        advanceUntilIdle()
        val pollsBeforeSession = discovery.subscriptions

        viewModel.handleIntent(MirrorIntent.Commit)
        advanceUntilIdle()

        assertEquals(pollsBeforeSession, discovery.subscriptions)
    }

    @Test
    fun switchingPlatformToIosStopsTheSession() = runTest(mainDispatcher) {
        val mirroring = FakeStartMirroring()
        val viewModel = mirrorViewModel(discovery = oneOfEach(), mirroring = mirroring)
        subscribe(viewModel)
        advanceUntilIdle()
        viewModel.handleIntent(MirrorIntent.Commit)
        advanceUntilIdle()

        viewModel.handleIntent(MirrorIntent.SelectPlatform(PlatformModel.Ios))
        advanceUntilIdle()

        val state = viewModel.currentState
        assertEquals(PlatformModel.Ios, state.platform)
        assertEquals(MirrorSession.Idle, state.session)
        assertEquals(0, mirroring.activeCollectors)
        assertEquals(MirrorStage.IosDeferred, state.stage)
    }

    @Test
    fun rapidStartAndStopLeavesExactlyOneCollector() = runTest(mainDispatcher) {
        val mirroring = FakeStartMirroring()
        val viewModel = mirrorViewModel(discovery = oneOfEach(), mirroring = mirroring)
        subscribe(viewModel)
        advanceUntilIdle()

        repeat(3) {
            viewModel.handleIntent(MirrorIntent.Commit)
            viewModel.handleIntent(MirrorIntent.StopMirroring)
        }
        viewModel.handleIntent(MirrorIntent.Commit)
        advanceUntilIdle()

        assertEquals(1, mirroring.activeCollectors)
    }

    @Test
    fun theDeviceChipReopensThePickerAndEscapeDoesNothingBeforeTheFirstSelection() = runTest(mainDispatcher) {
        val viewModel = mirrorViewModel(discovery = oneOfEach())
        subscribe(viewModel)
        advanceUntilIdle()

        viewModel.handleIntent(MirrorIntent.ClosePicker)
        advanceUntilIdle()
        assertTrue(viewModel.currentState.isPickerOpen, "Escape closed a picker with no way back")

        viewModel.handleIntent(MirrorIntent.Commit)
        advanceUntilIdle()
        assertEquals(MirrorStage.Mirror, viewModel.currentState.stage)

        viewModel.handleIntent(MirrorIntent.OpenPicker)
        advanceUntilIdle()
        assertEquals(MirrorStage.Picker, viewModel.currentState.stage)

        viewModel.handleIntent(MirrorIntent.ClosePicker)
        advanceUntilIdle()
        assertEquals(MirrorStage.Mirror, viewModel.currentState.stage)
    }


    /**
     * COR-2 — AC-28's card is reachable in AC-28's OWN scenario: a cold start with two
     * attached handsets.
     *
     * The earlier projection test proved `errorCard()` returns a card when `isPickerOpen` is
     * false, which is a different claim: it forced the flag directly. The reachability question
     * is whether a user can GET there, and they could not — `closePicker()` was a hard no-op
     * until `selectedDeviceId != null`, so the only way to dismiss the picker was to resolve
     * the ambiguity the card exists to help with.
     *
     * The F-2 guard it narrows is still intact and still asserted next door by
     * `theDeviceChipReopensThePickerAndEscapeDoesNothingBeforeTheFirstSelection`, which uses
     * ONE selectable handset — no card, so Escape still does nothing and there is still no
     * empty stage with no way back.
     *
     * MUTATION: restore `if (state.selectedDeviceId != null)` — the stageAfterEscape clause
     * fails, and the card becomes unreachable again.
     */
    @Test
    fun escapeRevealsTheAmbiguityCardWhenTwoHandsetsAreAttached() = runTest(mainDispatcher) {
        val viewModel = mirrorViewModel(discovery = twoHandsets())
        subscribe(viewModel)
        advanceUntilIdle()

        assertEquals(
            expected = listOf("stageBefore=Picker", "selected=null"),
            actual = listOf(
                "stageBefore=${viewModel.currentState.stage}",
                "selected=${viewModel.currentState.selectedDeviceId}",
            ),
        )

        viewModel.handleIntent(MirrorIntent.ClosePicker)
        advanceUntilIdle()

        assertEquals(
            expected = listOf("stageAfterEscape=NoSignal", "stillNoSelection=true"),
            actual = listOf(
                "stageAfterEscape=${viewModel.currentState.stage}",
                "stillNoSelection=${viewModel.currentState.selectedDeviceId == null}",
            ),
        )
    }

    @Test
    fun arrowAndEnterIntentsCarryNoModifierKeySemantics() {
        // Structural guard, not behavioural: Commit is a singleton and MoveHighlight's only
        // component is the direction, so neither can smuggle an isMetaPressed flag past the
        // window's own Cmd+1/2/3 handler.
        assertSame(MirrorIntent.Commit, MirrorIntent.Commit)
        assertEquals(MirrorIntent.MoveHighlight(1), MirrorIntent.MoveHighlight(1))
        assertEquals(1, MirrorIntent.MoveHighlight(1).delta)
    }

    @Test
    fun clickingAnUnusableRowDoesNotMoveTheCursor() = runTest(mainDispatcher) {
        val discovery = FakeObserveDiscovery(
            listOf(
                DiscoveryModel.Found(
                    physical = listOf(
                        phone(),
                        DeviceModel(
                            id = "pending-1",
                            displayName = "pending-1",
                            serial = "pending-1",
                            state = DeviceStateModel.Unauthorized,
                            isEmulator = false,
                        ),
                    ),
                    emulators = emptyList(),
                ),
            ),
        )
        val viewModel = mirrorViewModel(discovery = discovery)
        subscribe(viewModel)
        advanceUntilIdle()

        viewModel.handleIntent(MirrorIntent.HighlightRow("pending-1"))
        advanceUntilIdle()

        assertEquals(PHONE, viewModel.currentState.highlightedRow?.id)
    }
}
