package presentation.feature.mirror

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
import presentation.feature.mirror.core.discovery.errorCard
import presentation.feature.mirror.source.mirror.MirrorErrorCard
import presentation.feature.mirror.source.mirror.MirrorErrorKind
import presentation.feature.mirror.source.mirror.MirrorIntent
import presentation.feature.mirror.source.mirror.MirrorRowKind
import presentation.feature.mirror.source.mirror.MirrorStage
import presentation.feature.mirror.source.mirror.MirrorUiState
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

/** Fixed wall clock for the card's poll-age line — the projection takes it as a parameter. */
private const val NOW = 1_700_000_000_000L

/** A real udid from this host's captured `simctl list devices available` (T-1's fixtures). */
private const val BOOT_UDID = "77C17238-5B51-4206-9A58-CA601AF51E7E"

/** The second `iPhone 15 Pro` under the SAME iOS 17.5 runtime — a name is not a key. */
private const val TWIN_UDID = "4210645D-322E-43DB-B6FB-7E4F388D1DF5"

private const val SIMULATOR_NAME = "iPhone 15 Pro"

/** Shape of a physical iOS udid, for the fixture-driven trust case. */
private const val TRUST_HANDSET = "00008130-001A2B3C4D5E6F00"

private fun simulatorNotBooted(): DiscoveryModel.SimulatorNotBooted = DiscoveryModel.SimulatorNotBooted(
    simulators = listOf(
        EmulatorModel(avdName = SIMULATOR_NAME, isRunning = false, runningDeviceId = BOOT_UDID, runtime = "iOS 17.5"),
        EmulatorModel(avdName = SIMULATOR_NAME, isRunning = false, runningDeviceId = TWIN_UDID, runtime = "iOS 17.5"),
    ),
    toolVersion = "26.3",
    lastPollEpochMs = NOW - 800,
)

/** The poll after a successful boot: the same two simulators, the first one now up. */
private fun firstSimulatorBooted(): DiscoveryModel.Found = DiscoveryModel.Found(
    physical = emptyList(),
    emulators = listOf(
        EmulatorModel(avdName = SIMULATOR_NAME, isRunning = true, runningDeviceId = BOOT_UDID, runtime = "iOS 17.5"),
        EmulatorModel(avdName = SIMULATOR_NAME, isRunning = false, runningDeviceId = TWIN_UDID, runtime = "iOS 17.5"),
    ),
)

/**
 * T-21 (AC-31) — the two iOS error cards driven through real intents, not read off a projection.
 *
 * ## AC-31 is split, and this class is honest about which half is which
 *
 * - **simulator-not-booted is MEASURED on this host.** Xcode 26.3 with `simctl` and `idb`
 *   installed and **zero simulators booted** is the state this machine is actually in, and the
 *   udids below are copied verbatim from T-1's captured `simctl list devices available`
 *   (`data-device-api/src/test/.../IosDiscoveryFixtures.kt`; the constant there is
 *   `internal` to that module's test source set, which is why the values are restated rather
 *   than imported). The duplicate `iPhone 15 Pro` pair under ONE runtime is real, not invented.
 * - **device-not-trusted is FIXTURE-DRIVEN AND WAIVED (NFR-18 / AS-3).** There is no physical
 *   untrusted iPhone in this environment, so [theTrustCardRepollsAndItsSimulatorFallbackOpensThePicker]
 *   feeds a hand-built `DeviceNotTrusted` value. It proves the ViewModel's reaction to that
 *   state and NOTHING about what `idb` reports for a real untrusted handset. That half of
 *   AC-31 is verified by the manual demo, not here.
 *
 * ## Why ViewModel level when the projection is already tested
 *
 * `errorCard()` is computed BEFORE the stage `when`, so "Boot simulator" / "Choose another" /
 * "Use simulator" are only live buttons if the card yields the stage to the picker its own
 * secondary just opened. The projection tests prove the guard; these prove the round trip the
 * user actually performs — dispatch the intent, read the next state, ask the projection again.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class MirrorIosErrorCardTest {

    private val mainDispatcher = StandardTestDispatcher()

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(mainDispatcher)
        useTestDispatcherForOrbit(mainDispatcher)
    }

    /** Orbit's defaults are process-wide — leaving them set breaks sibling classes. */
    @AfterTest
    fun tearDown() {
        Orbit.resetDefaults()
        Dispatchers.resetMain()
    }

    /** The card the current state projects, with the test's fixed clock. */
    private val MirrorUiState.card: MirrorErrorCard? get() = errorCard(NOW)

    /**
     * MEASURED half of AC-31 plus AC-47 — "Boot simulator" boots the udid the card names, and
     * the next poll of the SAME discovery loop surfaces it as a running, selectable row.
     *
     * No restart anywhere in that sequence: the subscription log is unchanged across the boot,
     * which is what "the running poll picks it up" means and what a re-subscribe would betray.
     *
     * MUTATION: project `simulators.first().avdName` into `primaryDeviceId` — booted becomes
     * `[iPhone 15 Pro]` and the clause fails; a name would also have been ambiguous between
     * [BOOT_UDID] and [TWIN_UDID], which is the defect the udid addresses.
     */
    @Test
    fun bootSimulatorBootsTheUdidTheCardNamesAndTheNextPollShowsItRunning() = runTest(mainDispatcher) {
        val discovery = FakePlatformDiscovery()
        val boot = FakeBootEmulator()
        val viewModel = mirrorViewModel(discovery = discovery, boot = boot)
        subscribe(viewModel)
        advanceUntilIdle()
        viewModel.handleIntent(MirrorIntent.SelectPlatform(PlatformModel.Ios))
        advanceUntilIdle()
        discovery.emit(PlatformModel.Ios, simulatorNotBooted())
        advanceUntilIdle()
        // Dismissing the picker is what reveals the card — the projection yields to an open
        // picker for this state, and the picker IS open after a poll that found cold rows.
        viewModel.handleIntent(MirrorIntent.ClosePicker)
        advanceUntilIdle()
        val card = assertNotNull(viewModel.currentState.card, "no card behind the closed picker")

        viewModel.handleIntent(MirrorIntent.BootRow(assertNotNull(card.primaryDeviceId)))
        advanceUntilIdle()
        val pollsAfterBoot = discovery.subscriptions.toList()
        discovery.emit(PlatformModel.Ios, firstSimulatorBooted())
        advanceUntilIdle()

        val booted = viewModel.currentState.rows.first()
        assertEquals(
            expected = listOf(
                "kind=${MirrorErrorKind.SimulatorNotBooted}",
                "cardPlatform=Ios",
                "primaryDeviceId=$BOOT_UDID",
                "booted=[$BOOT_UDID]",
                "bootedOn=[Ios]",
                "rowId=$BOOT_UDID",
                "rowKind=${MirrorRowKind.Running}",
                "rowSelectable=true",
                "cardAfterBoot=null",
                "pollsUnchanged=true",
            ),
            actual = listOf(
                "kind=${card.kind}",
                "cardPlatform=${card.platform}",
                "primaryDeviceId=${card.primaryDeviceId}",
                "booted=${boot.booted}",
                "bootedOn=${boot.platforms}",
                "rowId=${booted.id}",
                "rowKind=${booted.kind}",
                "rowSelectable=${booted.selectable}",
                "cardAfterBoot=${viewModel.currentState.card}",
                "pollsUnchanged=${discovery.subscriptions == pollsAfterBoot}",
            ),
        )
    }

    /**
     * MEASURED half of AC-31 — "Choose another" is not a dead button.
     *
     * `OpenPicker` sets `isPickerOpen`, and because the card yields to the picker for this
     * state the stage actually CHANGES. Without the yield the card would re-cover the picker it
     * just opened and nothing visible would happen; the state would still say `isPickerOpen`,
     * which is why the stage and the re-asked projection are asserted and not just the flag.
     *
     * MUTATION: drop `SimulatorNotBooted` from `yieldsToPicker()` — cardWhileOpen comes back as
     * the card and stage stays on the card's surface.
     */
    @Test
    fun chooseAnotherOpensThePickerAndTheCardStepsAsideForIt() = runTest(mainDispatcher) {
        val discovery = FakePlatformDiscovery()
        val viewModel = mirrorViewModel(discovery = discovery)
        subscribe(viewModel)
        advanceUntilIdle()
        viewModel.handleIntent(MirrorIntent.SelectPlatform(PlatformModel.Ios))
        advanceUntilIdle()
        discovery.emit(PlatformModel.Ios, simulatorNotBooted())
        advanceUntilIdle()
        viewModel.handleIntent(MirrorIntent.ClosePicker)
        advanceUntilIdle()
        val cardKind = assertNotNull(viewModel.currentState.card).kind

        viewModel.handleIntent(MirrorIntent.OpenPicker)
        advanceUntilIdle()

        val state = viewModel.currentState
        assertEquals(
            expected = listOf(
                "cardBefore=${MirrorErrorKind.SimulatorNotBooted}",
                "isPickerOpen=true",
                "stage=${MirrorStage.Picker}",
                "cardWhileOpen=null",
                "rowsToChooseFrom=[$BOOT_UDID, $TWIN_UDID]",
            ),
            actual = listOf(
                "cardBefore=$cardKind",
                "isPickerOpen=${state.isPickerOpen}",
                "stage=${state.stage}",
                "cardWhileOpen=${state.card}",
                "rowsToChooseFrom=${state.rows.map { it.id }}",
            ),
        )
    }

    /**
     * WAIVED HALF OF AC-31 — FIXTURE-DRIVEN, NOT HARDWARE-VERIFIED (NFR-18 / AS-3).
     *
     * No physical untrusted iPhone exists in this environment, so the `DeviceNotTrusted` value
     * below is hand-built rather than captured, and nothing here proves what `idb` reports for a
     * handset whose Trust dialog has not been tapped. What it DOES prove is the ViewModel's
     * half: the card's primary re-polls discovery rather than restarting a daemon that does not
     * exist on iOS, and its "Use simulator" secondary reaches a picker that the card steps
     * aside for. Real-handset confirmation is T-26's manual demo.
     *
     * MUTATION: route the trust card's primary to `RestartAdb` — nudges stays 0 and the clause
     * fails.
     */
    @Test
    fun theTrustCardRepollsAndItsSimulatorFallbackOpensThePicker() = runTest(mainDispatcher) {
        val discovery = FakePlatformDiscovery()
        val refresh = FakeRefreshDiscovery()
        val restart = FakeRestartAdbServer()
        val viewModel = mirrorViewModel(
            discovery = discovery,
            refreshDiscovery = refresh,
            restartAdbServer = restart,
        )
        subscribe(viewModel)
        advanceUntilIdle()
        viewModel.handleIntent(MirrorIntent.SelectPlatform(PlatformModel.Ios))
        advanceUntilIdle()
        // A simulator IS available, so "Use simulator" is offered — the branch where the card
        // keeps the stage is the projection test's, not this one's.
        discovery.emit(
            PlatformModel.Ios,
            DiscoveryModel.DeviceNotTrusted(
                deviceId = TRUST_HANDSET,
                deviceName = "Andrew iPhone",
                simulatorsAvailable = true,
            ),
        )
        advanceUntilIdle()
        viewModel.handleIntent(MirrorIntent.ClosePicker)
        advanceUntilIdle()
        val card = assertNotNull(viewModel.currentState.card, "no trust card behind the closed picker")

        viewModel.handleIntent(MirrorIntent.RetryDiscovery)
        advanceUntilIdle()
        viewModel.handleIntent(MirrorIntent.OpenPicker)
        advanceUntilIdle()

        val state = viewModel.currentState
        assertEquals(
            expected = listOf(
                "kind=${MirrorErrorKind.DeviceNotTrusted}",
                "cardPlatform=Ios",
                "primaryDeviceId=$TRUST_HANDSET",
                "canBootEmulator=true",
                "nudges=1",
                "adbRestarts=0",
                "stage=${MirrorStage.Picker}",
                "cardWhileOpen=null",
            ),
            actual = listOf(
                "kind=${card.kind}",
                "cardPlatform=${card.platform}",
                "primaryDeviceId=${card.primaryDeviceId}",
                "canBootEmulator=${card.canBootEmulator}",
                "nudges=${refresh.nudges}",
                "adbRestarts=${restart.restarts}",
                "stage=${state.stage}",
                "cardWhileOpen=${state.card}",
            ),
        )
    }

    /**
     * AC-46 — the SHARED error states keep reporting after a switch to iOS, in iOS words.
     *
     * The projection test next door proves the per-state wording; what this adds is that the
     * platform the switch wrote into state is the one the card reads, through the real
     * reduction. The failure it guards is the one a blanket "suppress shared cards on iOS"
     * would have produced: a blank stage, or an `adb kill-server` block on a bench with no adb.
     */
    @Test
    fun theSharedErrorStatesKeepWorkingOnTheIosBenchAndNeverSayAdb() = runTest(mainDispatcher) {
        val discovery = FakePlatformDiscovery()
        val viewModel = mirrorViewModel(discovery = discovery)
        subscribe(viewModel)
        advanceUntilIdle()
        viewModel.handleIntent(MirrorIntent.SelectPlatform(PlatformModel.Ios))
        advanceUntilIdle()

        discovery.emit(
            PlatformModel.Ios,
            DiscoveryModel.ToolMissing(
                tool = "xcrun",
                probedPaths = listOf("/usr/bin/xcrun"),
                installCommand = "xcode-select --install",
                envHint = "\$DEVELOPER_DIR unset",
            ),
        )
        advanceUntilIdle()
        val toolMissing = assertNotNull(viewModel.currentState.card, "iOS tool-missing left a blank stage")

        discovery.emit(
            PlatformModel.Ios,
            DiscoveryModel.NoDeviceFound(
                toolVersion = "26.3",
                lastPollEpochMs = NOW - 500,
                attempts = 3,
                attachedCount = 0,
            ),
        )
        advanceUntilIdle()
        val noDevice = assertNotNull(viewModel.currentState.card, "iOS no-device left a blank stage")

        assertEquals(
            expected = listOf(
                "toolMissingKind=${MirrorErrorKind.ToolMissing}",
                "toolMissingPlatform=Ios",
                "toolMissingCommand=xcode-select --install",
                "noDeviceKind=${MirrorErrorKind.NoDevice}",
                "noDevicePlatform=Ios",
                "noDeviceCommand=null",
                "adbText=[]",
            ),
            actual = listOf(
                "toolMissingKind=${toolMissing.kind}",
                "toolMissingPlatform=${toolMissing.platform}",
                "toolMissingCommand=${toolMissing.command}",
                "noDeviceKind=${noDevice.kind}",
                "noDevicePlatform=${noDevice.platform}",
                "noDeviceCommand=${noDevice.command}",
                "adbText=${
                    listOf(toolMissing, noDevice)
                        .flatMap { listOfNotNull(it.command, it.diagnostic) }
                        .filter { "adb" in it }
                }",
            ),
        )
    }
}
