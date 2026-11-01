package presentation.feature.mirror

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import domain.core.source.model.device.DiscoveryModel
import kotlinx.coroutines.launch
import presentation.feature.mirror.source.mirror.MirrorSideEffect
import presentation.feature.mirror.source.mirror.MirrorIntent

/**
 * FR-37 — every recovery action does real work rather than waiting out the 2 s poll.
 *
 * This is the test the carry-forward note asked for. Each of these intents spent a wave as a
 * `-> Unit` branch in `handleIntent`, which compiles, keeps the whole suite green, and ships a
 * button that does nothing. A `-> Unit` is invisible at the call site: nothing fails, the user
 * presses Retry, and the app shrugs. So what is asserted here is not the handler's internals
 * but that pressing the thing MOVED something.
 */
/**
 * Scripted, not empty. With an empty script the first subscription had not landed by the time
 * the intent was dispatched, so `subscriptions > before` held whether or not the handler did
 * anything - the assertion was vacuous, and the mutation run is what exposed it. Emitting one
 * value forces the initial collection to be live first, which is what makes `before` a real
 * baseline to measure against.
 */
private fun scriptedDiscovery(): FakeObserveDiscovery =
    FakeObserveDiscovery(
        listOf(
            DiscoveryModel.NoDeviceFound(
                toolVersion = "35.0.1",
                lastPollEpochMs = 0L,
                attempts = 1,
                attachedCount = 0,
            ),
        ),
    )

@OptIn(ExperimentalCoroutinesApi::class)
class MirrorRecoveryActionsTest {

    private val mainDispatcher = StandardTestDispatcher()

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(mainDispatcher)
        // Required, not optional. Orbit's defaults are PROCESS-WIDE, so omitting this made
        // these tests pass only because a sibling class had already configured them, and
        // fail whenever the class order changed. A test that passes because of another
        // test's side effect is not passing for its own reason.
        useTestDispatcherForOrbit(mainDispatcher)
    }

    @AfterTest
    fun tearDown() = Dispatchers.resetMain()

    /**
     * AC-27. "Restart adb" must restart adb, not re-poll.
     *
     * `start-server` alone cannot clear a daemon that is alive but not serving — measured: it
     * hangs against one — so a button that only re-polled would be unable to fix the state the
     * user pressed it in, while still claiming to.
     *
     * MUTATION: drop the `restartAdbServer()` call — the restarts assertion fails.
     */
    @Test
    fun restartAdbReallyRestartsTheDaemonAndThenRepolls() = runTest(mainDispatcher) {
        val discovery = scriptedDiscovery()
        val restart = FakeRestartAdbServer()
        val refresh = FakeRefreshDiscovery()
        val viewModel = mirrorViewModel(
            discovery = discovery,
            refreshDiscovery = refresh,
            restartAdbServer = restart,
        )
        subscribe(viewModel)
        advanceUntilIdle()

        viewModel.handleIntent(MirrorIntent.RestartAdb)
        advanceUntilIdle()

        assertEquals(
            expected = listOf("restarts=1", "nudges=1"),
            actual = listOf("restarts=${restart.restarts}", "nudges=${refresh.nudges}"),
        )
    }

    /**
     * AC-26. The chosen path is persisted and the probe runs again at once.
     *
     * Nothing is validated at this end — `ToolResolver`'s existing `isExecutable` predicate
     * decides, so an unusable path falls through to the ordered candidates instead of becoming
     * an error state the user has to clear.
     *
     * MUTATION: drop the `setAdbPath(path)` call — the stored assertion fails.
     */
    @Test
    fun aChosenAdbPathIsStoredAndTheProbeRunsAgain() = runTest(mainDispatcher) {
        val discovery = scriptedDiscovery()
        val stored = FakeSetAdbPath()
        val refresh = FakeRefreshDiscovery()
        val viewModel = mirrorViewModel(
            discovery = discovery,
            refreshDiscovery = refresh,
            setAdbPath = stored,
        )
        subscribe(viewModel)
        advanceUntilIdle()

        viewModel.handleIntent(MirrorIntent.AdbPathChosen("/opt/homebrew/bin/adb"))
        advanceUntilIdle()

        assertEquals(
            expected = listOf("stored=[/opt/homebrew/bin/adb]", "nudges=1"),
            actual = listOf("stored=${stored.paths}", "nudges=${refresh.nudges}"),
        )
    }

    /**
     * AF-1, the audit's hardest finding: `RetryDiscovery` was `= Unit`.
     *
     * The dead-button exit check — `grep '\-> Unit' MirrorViewModel.kt` — printed nothing,
     * because the no-op had moved out of the `when` branch and into a function body. The check
     * I wrote was defeated by the shape of the thing it was meant to catch, and nothing else
     * noticed: no test dispatched `RetryDiscovery` anywhere, so AC-26's secondary and AC-53's
     * primary were both wired to nothing.
     *
     * This test is the real check, and it is behavioural rather than textual: it asserts the
     * dispatch REACHES something. A grep can be sidestepped by moving code; a dispatch cannot.
     *
     * MUTATION: make `retryDiscovery()` a no-op again — the nudges assertion fails.
     */
    @Test
    fun retryDiscoveryNudgesThePollLoop() = runTest(mainDispatcher) {
        val refresh = FakeRefreshDiscovery()
        val viewModel = mirrorViewModel(discovery = scriptedDiscovery(), refreshDiscovery = refresh)
        subscribe(viewModel)
        advanceUntilIdle()

        viewModel.handleIntent(MirrorIntent.RetryDiscovery)
        advanceUntilIdle()

        assertEquals(expected = listOf("nudges=1"), actual = listOf("nudges=${refresh.nudges}"))
    }

    /**
     * COR-7 — a recovery action that FAILS says so.
     *
     * All three handlers discarded the `Optional` their use cases returned, so a real failure
     * — an SDK unmounted after the probe cached a success, say — was indistinguishable from the
     * dead button FR-37 forbids: the card simply re-rendered as if the user had cancelled.
     * `DeviceRepositoryImpl.restartAdbServer`'s KDoc justifies throwing precisely so this layer
     * can report it, and this layer was dropping it.
     *
     * The second clause matters as much as the first: a failed restart must NOT be followed by
     * a re-poll, because polling a daemon that was never restarted just reports the same
     * failure again and buries the real reason.
     *
     * MUTATION A: drop `.onFailure` — the effect clause fails.
     * MUTATION B: change `.onSuccess { refreshDiscovery() }` back to an unconditional call —
     * the nudges clause fails. Neither mutation covers the other.
     */
    @Test
    fun aFailedRestartIsReportedAndDoesNotRepoll() = runTest(mainDispatcher) {
        val restart = FakeRestartAdbServer().apply { failure = IllegalStateException("sdk gone") }
        val refresh = FakeRefreshDiscovery()
        val effects = mutableListOf<MirrorSideEffect>()
        val viewModel = mirrorViewModel(
            discovery = scriptedDiscovery(),
            refreshDiscovery = refresh,
            restartAdbServer = restart,
        )
        subscribe(viewModel)
        // Unconfined, so the collector is live BEFORE the intent posts. Orbit's sideEffectFlow
        // is hot; a lazily-started collector misses the effect and the test then reports the
        // fix as broken when it is the harness that is.
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.container.sideEffectFlow.collect { effects += it }
        }
        advanceUntilIdle()

        viewModel.handleIntent(MirrorIntent.RestartAdb)
        advanceUntilIdle()

        assertEquals(
            expected = listOf(
                "attempted=1",
                "effect=[RecoveryFailed(action=RestartAdb)]",
                "nudges=0",
            ),
            actual = listOf(
                "attempted=${restart.restarts}",
                "effect=${effects.filterIsInstance<MirrorSideEffect.RecoveryFailed>()}",
                "nudges=${refresh.nudges}",
            ),
        )
    }
}
