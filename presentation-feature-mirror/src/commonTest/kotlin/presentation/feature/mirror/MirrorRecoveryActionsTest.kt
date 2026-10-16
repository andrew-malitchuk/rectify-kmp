package presentation.feature.mirror

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import domain.core.source.model.device.DiscoveryModel
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
    fun setUp() = Dispatchers.setMain(mainDispatcher)

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
        val viewModel = mirrorViewModel(discovery = discovery, restartAdbServer = restart)
        subscribe(viewModel)
        advanceUntilIdle()

        viewModel.handleIntent(MirrorIntent.RestartAdb)
        advanceUntilIdle()

        assertEquals(
            expected = listOf("restarts=1"),
            actual = listOf("restarts=${restart.restarts}"),
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
        val viewModel = mirrorViewModel(discovery = discovery, setAdbPath = stored)
        subscribe(viewModel)
        advanceUntilIdle()

        viewModel.handleIntent(MirrorIntent.AdbPathChosen("/opt/homebrew/bin/adb"))
        advanceUntilIdle()

        assertEquals(
            expected = listOf("stored=[/opt/homebrew/bin/adb]"),
            actual = listOf("stored=${stored.paths}"),
        )
    }
}
