package presentation.feature.mirror

import domain.core.source.model.device.DeviceModel
import domain.core.source.model.device.DeviceStateModel
import domain.core.source.model.device.DiscoveryModel
import domain.core.source.model.device.EmulatorModel
import domain.core.source.model.device.PlatformModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.orbitmvi.orbit.Orbit
import presentation.feature.mirror.source.mirror.MirrorIntent
import presentation.feature.mirror.source.mirror.MirrorRowKind
import presentation.feature.mirror.source.mirror.MirrorSideEffect
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

/** A real udid from this host's captured `simctl list devices available` (see T-1's fixtures). */
private const val SIM_UDID = "77C17238-5B51-4206-9A58-CA601AF51E7E"

/** The SECOND `iPhone 15 Pro` under the very same iOS 17.5 runtime — the name is not a key. */
private const val SIM_UDID_TWIN = "4210645D-322E-43DB-B6FB-7E4F388D1DF5"

private const val SIM_NAME = "iPhone 15 Pro"

private const val AVD_NAME = "Pixel_7_API_34"

private const val RUNNING_EMULATOR_ID = "emulator-5554"

private fun handset(id: String, state: DeviceStateModel): DeviceModel = DeviceModel(
    id = id,
    displayName = id,
    serial = id,
    state = state,
    isEmulator = false,
)

/**
 * An Android bench whose cursor is NOT on row zero.
 *
 * The unauthorised handset comes first on purpose: it makes `highlightedIndex` land on 1, so the
 * `highlight = 0` the switch resets to is a value the test watched change rather than the
 * default it started at.
 */
private fun androidBench(): DiscoveryModel.Found = DiscoveryModel.Found(
    physical = listOf(
        handset("unauthorized-1", DeviceStateModel.Unauthorized),
        handset("PIXEL-9", DeviceStateModel.Device),
    ),
    emulators = listOf(EmulatorModel(avdName = AVD_NAME, isRunning = false)),
)

/** Two shut-down simulators sharing one name, udids verbatim from the capture. */
private fun simulatorNotBooted(): DiscoveryModel.SimulatorNotBooted = DiscoveryModel.SimulatorNotBooted(
    simulators = listOf(
        EmulatorModel(avdName = SIM_NAME, isRunning = false, runningDeviceId = SIM_UDID, runtime = "iOS 17.5"),
        EmulatorModel(avdName = SIM_NAME, isRunning = false, runningDeviceId = SIM_UDID_TWIN, runtime = "iOS 17.5"),
    ),
    toolVersion = "26.3",
    lastPollEpochMs = 0L,
)

/**
 * T-21 (AC-46 / AC-47) — the toolbar's platform switch, end to end through the ViewModel.
 *
 * The projection tests next door prove what each state LOOKS like; this one proves the switch
 * itself: that selecting iOS cancels the adb poll structurally (wave 10 made
 * `combine(sessionDeviceId, discoveryPlatform)` the `flatMapLatest` key), resets the bench in
 * one reduction, starts the iOS poll, and survives a late emission from the loop it just
 * cancelled.
 *
 * Fakes only — `ProcessRunner` spawning inside `runTest`'s virtual clock always times out, which
 * is why the real-toolchain probes live in their own `runBlocking`, skip-guarded test.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class MirrorViewModelIosPlatformSwitchTest {

    /** One dispatcher for `Dispatchers.Main` and the test body — see `MirrorFakes.kt`. */
    private val mainDispatcher = StandardTestDispatcher()

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(mainDispatcher)
        useTestDispatcherForOrbit(mainDispatcher)
    }

    /**
     * Orbit's dispatcher defaults are PROCESS-WIDE, so leaving them set has broken sibling
     * classes in this repo before. Reset, every time, not only on the happy path.
     */
    @AfterTest
    fun tearDown() {
        Orbit.resetDefaults()
        Dispatchers.resetMain()
    }

    /**
     * Selecting iOS re-keys discovery and clears the Android bench in the same reduction.
     *
     * The subscription LOG is the load-bearing half: `[Android, Ios]` with zero live Android
     * collectors is "the adb loop was cancelled and the simctl one started", which a state
     * assertion alone cannot distinguish from "the adb loop is still running and we ignored it".
     *
     * MUTATION: drop `discoveryPlatform` from `observeDiscoveryWhileIdle`'s `combine` key — the
     * subscriptions, androidCollectors and iosCollectors clauses all fail.
     */
    @Test
    fun selectingIosCancelsTheAdbPollAndResetsTheBenchInOneStep() = runTest(mainDispatcher) {
        val discovery = FakePlatformDiscovery()
        val viewModel = mirrorViewModel(discovery = discovery)
        subscribe(viewModel)
        advanceUntilIdle()
        discovery.emit(PlatformModel.Android, androidBench())
        advanceUntilIdle()
        val android = viewModel.currentState

        viewModel.handleIntent(MirrorIntent.SelectPlatform(PlatformModel.Ios))
        advanceUntilIdle()

        val ios = viewModel.currentState
        assertEquals(
            expected = listOf(
                "rowsBefore=3",
                "avdsBefore=[$AVD_NAME]",
                "highlightBefore=1",
                "platform=Ios",
                "discovery=Detecting",
                "rows=0",
                "avds=[]",
                "selectedAvd=null",
                "highlight=0",
                "frozen=null",
                "subscriptions=[Android, Ios]",
                "androidCollectors=0",
                "iosCollectors=1",
            ),
            actual = listOf(
                "rowsBefore=${android.rows.size}",
                "avdsBefore=${android.avds}",
                "highlightBefore=${android.highlightedIndex}",
                "platform=${ios.platform}",
                "discovery=${ios.discovery}",
                "rows=${ios.rows.size}",
                "avds=${ios.avds}",
                "selectedAvd=${ios.selectedAvd}",
                "highlight=${ios.highlightedIndex}",
                "frozen=${ios.frozen}",
                "subscriptions=${discovery.subscriptions}",
                "androidCollectors=${discovery.collectorsOn(PlatformModel.Android)}",
                "iosCollectors=${discovery.collectorsOn(PlatformModel.Ios)}",
            ),
        )
    }

    /**
     * The device the user picked on the OLD platform does not survive the switch.
     *
     * Separate from the bench-reset case above because it is a different kind of staleness:
     * `rows`/`avds` are a poll result and were always cleared, but `selectedDeviceId` /
     * `selectedDeviceName` are the user's own choice and were not. The visible bug was the
     * toolbar chip still advertising a Pixel while the iOS tab was active — and, worse,
     * `isPickerDismissable` reading true for a device this platform cannot mirror.
     *
     * `chipBefore` is what stops this being vacuous: the selection is shown to exist before
     * the switch, so the nulls after it are the reset and not a value that was never set.
     *
     * MUTATION: drop either null from `selectPlatform`'s reduce — this fails, the test above
     * does not.
     */
    @Test
    fun switchingPlatformForgetsTheDevicePickedOnThePreviousOne() = runTest(mainDispatcher) {
        val discovery = FakePlatformDiscovery()
        val viewModel = mirrorViewModel(discovery = discovery)
        subscribe(viewModel)
        advanceUntilIdle()
        discovery.emit(PlatformModel.Android, androidBench())
        advanceUntilIdle()
        // Commit picks the highlighted row, which is the usable handset, not the unauthorised one.
        viewModel.handleIntent(MirrorIntent.Commit)
        advanceUntilIdle()
        val picked = viewModel.currentState

        viewModel.handleIntent(MirrorIntent.SelectPlatform(PlatformModel.Ios))
        advanceUntilIdle()

        val ios = viewModel.currentState
        assertEquals(
            expected = listOf(
                "idBefore=PIXEL-9",
                "nameBefore=PIXEL-9",
                "dismissableBefore=true",
                "id=null",
                "name=null",
                "dismissable=false",
            ),
            actual = listOf(
                "idBefore=${picked.selectedDeviceId}",
                "nameBefore=${picked.selectedDeviceName}",
                "dismissableBefore=${picked.isPickerDismissable}",
                "id=${ios.selectedDeviceId}",
                "name=${ios.selectedDeviceName}",
                "dismissable=${ios.isPickerDismissable}",
            ),
        )
    }

    /**
     * A poll that was already in flight when the user switched lands nowhere.
     *
     * `landedBefore` is what stops this being vacuous: the SAME emission on the SAME channel is
     * shown to reach the reducer before the switch, so the empty bench afterwards is the
     * cancellation and not a fake that never delivers anything.
     *
     * The effects clause covers the hypothesis T-21 was written to check — that a swallowed
     * `CancellationException` could surface as a spurious `Failure`. `ObserveDeviceDiscoveryUseCase`
     * returns a bare `Flow` with no `resultLauncher` in the path, so it does not; this is the
     * assertion that keeps it that way.
     */
    @Test
    fun aLateAndroidPollAfterTheSwitchCannotResurrectStaleStateOrRaiseAnEffect() = runTest(mainDispatcher) {
        val discovery = FakePlatformDiscovery()
        val effects = mutableListOf<MirrorSideEffect>()
        val viewModel = mirrorViewModel(discovery = discovery)
        subscribe(viewModel)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.container.sideEffectFlow.collect { effects += it }
        }
        advanceUntilIdle()
        discovery.emit(PlatformModel.Android, androidBench())
        advanceUntilIdle()
        val landedBefore = viewModel.currentState.rows.size

        viewModel.handleIntent(MirrorIntent.SelectPlatform(PlatformModel.Ios))
        advanceUntilIdle()
        discovery.emit(PlatformModel.Android, androidBench())
        advanceUntilIdle()

        val state = viewModel.currentState
        assertEquals(
            expected = listOf("landedBefore=3", "rows=0", "discovery=Detecting", "effects=[]"),
            actual = listOf(
                "landedBefore=$landedBefore",
                "rows=${state.rows.size}",
                "discovery=${state.discovery}",
                "effects=$effects",
            ),
        )
    }

    /**
     * Switching back is symmetric: the simctl poll is cancelled, adb is subscribed again, and a
     * fresh Android poll lands on a bench that was showing simulators a moment earlier.
     */
    @Test
    fun switchingBackToAndroidRestoresTheAdbPath() = runTest(mainDispatcher) {
        val discovery = FakePlatformDiscovery()
        val viewModel = mirrorViewModel(discovery = discovery)
        subscribe(viewModel)
        advanceUntilIdle()
        viewModel.handleIntent(MirrorIntent.SelectPlatform(PlatformModel.Ios))
        advanceUntilIdle()
        discovery.emit(PlatformModel.Ios, simulatorNotBooted())
        advanceUntilIdle()
        val simulatorRows = viewModel.currentState.rows.map { it.id }

        viewModel.handleIntent(MirrorIntent.SelectPlatform(PlatformModel.Android))
        advanceUntilIdle()
        discovery.emit(PlatformModel.Android, androidBench())
        advanceUntilIdle()

        val state = viewModel.currentState
        assertEquals(
            expected = listOf(
                "simulatorRows=[$SIM_UDID, $SIM_UDID_TWIN]",
                "platform=Android",
                "rows=3",
                "avds=[$AVD_NAME]",
                "subscriptions=[Android, Ios, Android]",
                "iosCollectors=0",
                "androidCollectors=1",
            ),
            actual = listOf(
                "simulatorRows=$simulatorRows",
                "platform=${state.platform}",
                "rows=${state.rows.size}",
                "avds=${state.avds}",
                "subscriptions=${discovery.subscriptions}",
                "iosCollectors=${discovery.collectorsOn(PlatformModel.Ios)}",
                "androidCollectors=${discovery.collectorsOn(PlatformModel.Android)}",
            ),
        )
    }

    /**
     * AC-47's identity split: `simctl boot` takes a udid, `emulator -avd` takes an AVD name.
     *
     * Driven through `BootSelectedAvd`, the one path where the two genuinely diverge — the AVD
     * shortcut list carries NAMES on both platforms, so on iOS the handler has to resolve the
     * name back to its row and boot `row.id`. Measured on this host, the name cannot do the job:
     * the capture has two `iPhone 15 Pro` under ONE runtime ([SIM_UDID] and [SIM_UDID_TWIN]), so
     * a name does not identify a simulator even within a runtime.
     *
     * MUTATION: make `bootTargetOf` return `row.avdName ?: row.id` for both platforms — the
     * booted clause becomes `[iPhone 15 Pro, Pixel_7_API_34]` and fails on the first element.
     */
    @Test
    fun theBootTargetIsTheUdidOnIosAndTheAvdNameOnAndroid() = runTest(mainDispatcher) {
        val discovery = FakePlatformDiscovery()
        val boot = FakeBootEmulator()
        val viewModel = mirrorViewModel(discovery = discovery, boot = boot)
        subscribe(viewModel)
        advanceUntilIdle()
        viewModel.handleIntent(MirrorIntent.SelectPlatform(PlatformModel.Ios))
        advanceUntilIdle()
        discovery.emit(PlatformModel.Ios, simulatorNotBooted())
        advanceUntilIdle()
        val iosShortcuts = viewModel.currentState.avds

        viewModel.handleIntent(MirrorIntent.BootSelectedAvd)
        advanceUntilIdle()
        viewModel.handleIntent(MirrorIntent.SelectPlatform(PlatformModel.Android))
        advanceUntilIdle()
        // A RUNNING AVD, so the row's id and its avdName differ on Android too — a cold AVD has
        // `id == avdName` and could not tell the two branches apart.
        discovery.emit(
            PlatformModel.Android,
            DiscoveryModel.Found(
                physical = emptyList(),
                emulators = listOf(
                    EmulatorModel(avdName = AVD_NAME, isRunning = true, runningDeviceId = RUNNING_EMULATOR_ID),
                ),
            ),
        )
        advanceUntilIdle()
        viewModel.handleIntent(MirrorIntent.BootSelectedAvd)
        advanceUntilIdle()

        assertEquals(
            expected = listOf(
                "iosShortcutsAreNames=[$SIM_NAME, $SIM_NAME]",
                "androidRowId=$RUNNING_EMULATOR_ID",
                "booted=[$SIM_UDID, $AVD_NAME]",
                "platforms=[Ios, Android]",
            ),
            actual = listOf(
                "iosShortcutsAreNames=$iosShortcuts",
                "androidRowId=${viewModel.currentState.rows.single().id}",
                "booted=${boot.booted}",
                "platforms=${boot.platforms}",
            ),
        )
        assertEquals(MirrorRowKind.Running, viewModel.currentState.rows.single().kind)
    }
}
