package data.repository.impl

import data.device.api.source.datasource.AdbHostSource
import data.device.api.source.datasource.DeviceCommandExecutor
import data.device.api.source.datasource.EmulatorSource
import data.device.api.source.datasource.IdbHostSource
import data.device.api.source.datasource.MirrorTransport
import data.device.api.source.error.DeviceCommandException
import data.device.api.source.model.AvdName
import data.device.api.source.model.AvdResource
import data.device.api.source.model.DeviceCommand
import data.device.api.source.model.DeviceId
import data.device.api.source.model.DeviceResource
import data.device.api.source.model.DeviceStateResource
import data.device.api.source.model.MirrorFrame
import data.device.api.source.model.ToolKind
import data.device.api.source.model.ToolProbe
import data.device.api.source.model.TransportStats
import data.device.api.source.tuning.MirrorTuning
import data.repository.impl.source.DeviceRepositoryImpl
import domain.core.source.model.device.DiscoveryModel
import domain.core.source.model.device.PlatformModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

/**
 * The discovery state machine under virtual time, with no device, no process and no real
 * two-second wait anywhere.
 */
class DeviceDiscoveryPollingTest {

    @Test
    fun `poll emissions land two seconds apart`() = runTest {
        val adb = FakeAdbHostSource()
        val received = collectDiscovery(adb)

        advanceTimeBy(4 * MirrorTuning.DiscoveryPollMs + 1)

        // Detecting is immediate, then one poll per DiscoveryPollMs.
        assertEquals(listOf(0L, 0L, 2_000L, 4_000L, 6_000L, 8_000L), received.map { it.first })
    }

    @Test
    fun `an empty poll stays passive until the escalation threshold`() = runTest {
        val adb = FakeAdbHostSource()
        val received = collectDiscovery(adb)

        // One poll short of the threshold.
        advanceTimeBy((MirrorTuning.EmptyPollEscalation - 2) * MirrorTuning.DiscoveryPollMs + 1)

        assertEquals(MirrorTuning.EmptyPollEscalation - 1, received.count { it.second is DiscoveryModel.Found })
        assertTrue(received.none { it.second is DiscoveryModel.NoDeviceFound })
    }

    @Test
    fun `the escalation poll reports attempts and the last poll time`() = runTest {
        val adb = FakeAdbHostSource(version = "Android Debug Bridge version 1.0.41")
        adb.devices = listOf(device("R5CT10ABCDE", DeviceStateResource.Unauthorized))
        val received = collectDiscovery(adb)

        advanceTimeBy((MirrorTuning.EmptyPollEscalation - 1) * MirrorTuning.DiscoveryPollMs + 1)

        val escalated = assertIs<DiscoveryModel.NoDeviceFound>(received.last().second)
        assertEquals(MirrorTuning.EmptyPollEscalation, escalated.attempts)
        assertEquals("Android Debug Bridge version 1.0.41", escalated.toolVersion)
        // An unauthorized phone is attached but unusable, and the user must not be told
        // there is no device while holding one.
        assertEquals(1, escalated.attachedCount)
        assertEquals(received.last().first, escalated.lastPollEpochMs)
        assertEquals(MirrorTuning.EmptyPollEscalation - 1, received.count { it.second is DiscoveryModel.Found })
    }

    @Test
    fun `a device attached after escalation produces Found again`() = runTest {
        val adb = FakeAdbHostSource()
        val received = collectDiscovery(adb)

        advanceTimeBy(MirrorTuning.EmptyPollEscalation * MirrorTuning.DiscoveryPollMs + 1)
        assertIs<DiscoveryModel.NoDeviceFound>(received.last().second)

        adb.devices = listOf(device("R5CT10ABCDE", DeviceStateResource.Device))
        advanceTimeBy(MirrorTuning.DiscoveryPollMs)

        val found = assertIs<DiscoveryModel.Found>(received.last().second)
        assertEquals(listOf("R5CT10ABCDE"), found.physical.map { it.id })
        assertTrue(found.physical.single().isSelectable)
    }

    @Test
    fun `a missing adb arrives as a ToolMissing value`() = runTest {
        val adb = FakeAdbHostSource(found = false)
        val received = collectDiscovery(adb)

        advanceTimeBy(1)

        val missing = assertIs<DiscoveryModel.ToolMissing>(received.last().second)
        assertEquals("adb", missing.tool)
        assertEquals(listOf("/opt/homebrew/bin/adb"), missing.probedPaths)
        assertEquals("brew install", missing.installCommand)
    }

    @Test
    fun `a missing adb never escalates to NoDeviceFound`() = runTest {
        val adb = FakeAdbHostSource(found = false)
        val received = collectDiscovery(adb)

        advanceTimeBy(5 * MirrorTuning.DiscoveryPollMs + 1)

        assertTrue(received.drop(1).all { it.second is DiscoveryModel.ToolMissing })
    }

    @Test
    fun `an avd both listed and running appears once as running`() = runTest {
        val adb = FakeAdbHostSource()
        adb.devices = listOf(device("emulator-5554", DeviceStateResource.Device, isEmulator = true))
        val emulators = FakeEmulatorSource(listOf("Pixel_7_API_34", "Tablet_API_33"))
        val executor = FakeDeviceCommandExecutor(avdNameOf = mapOf("emulator-5554" to "Pixel_7_API_34"))
        val received = collectDiscovery(adb, emulators, executor)

        advanceTimeBy(1)

        val found = assertIs<DiscoveryModel.Found>(received.last().second)
        assertTrue(found.physical.isEmpty())
        assertEquals(listOf("Pixel_7_API_34", "Tablet_API_33"), found.emulators.map { it.avdName })
        val running = found.emulators.single { it.isRunning }
        assertEquals("Pixel_7_API_34", running.avdName)
        assertEquals("emulator-5554", running.runningDeviceId)
    }

    @Test
    fun `a running emulator that is not listed still appears once`() = runTest {
        val adb = FakeAdbHostSource()
        adb.devices = listOf(device("emulator-5554", DeviceStateResource.Device, isEmulator = true))
        val emulators = FakeEmulatorSource(emptyList())
        val executor = FakeDeviceCommandExecutor(avdNameOf = mapOf("emulator-5554" to "Ghost_API_34"))
        val received = collectDiscovery(adb, emulators, executor)

        advanceTimeBy(1)

        val found = assertIs<DiscoveryModel.Found>(received.last().second)
        assertEquals(1, found.emulators.size)
        assertEquals("Ghost_API_34", found.emulators.single().avdName)
    }

    @Test
    fun `an emulator that will not name itself is listed under its serial`() = runTest {
        val adb = FakeAdbHostSource()
        adb.devices = listOf(device("emulator-5554", DeviceStateResource.Device, isEmulator = true))
        val received = collectDiscovery(adb, FakeEmulatorSource(emptyList()), FakeDeviceCommandExecutor())

        advanceTimeBy(1)

        val found = assertIs<DiscoveryModel.Found>(received.last().second)
        assertEquals("emulator-5554", found.emulators.single().avdName)
        assertTrue(found.emulators.single().isRunning)
    }

    @Test
    fun `the avd name of an attached serial is asked for once`() = runTest {
        val adb = FakeAdbHostSource()
        adb.devices = listOf(device("emulator-5554", DeviceStateResource.Device, isEmulator = true))
        val executor = FakeDeviceCommandExecutor(avdNameOf = mapOf("emulator-5554" to "Pixel_7_API_34"))
        collectDiscovery(adb, FakeEmulatorSource(listOf("Pixel_7_API_34")), executor)

        advanceTimeBy(3 * MirrorTuning.DiscoveryPollMs + 1)

        assertEquals(1, executor.calls)
    }

    @Test
    fun `a failed poll is reported and the next tick polls again`() = runTest {
        val adb = FakeAdbHostSource()
        adb.devices = listOf(device("R5CT10ABCDE", DeviceStateResource.Device))
        // Two: one for the poll, one for the ladder's single retry after it heals the daemon.
        adb.failuresLeft = 2
        val received = collectDiscovery(adb)

        advanceTimeBy(1)
        val failed = assertIs<DiscoveryModel.PollFailed>(received.last().second)
        assertEquals("adb", failed.tool)
        assertEquals(timeoutReason(MirrorTuning.HostCommandTimeoutMs), failed.reason)

        // The stream is still alive - a terminal operator would have ended it here.
        advanceTimeBy(MirrorTuning.DiscoveryPollMs)
        val found = assertIs<DiscoveryModel.Found>(received.last().second)
        assertEquals(listOf("R5CT10ABCDE"), found.physical.map { it.id })
    }

    @Test
    fun `a stream of failed polls keeps polling forever`() = runTest {
        val adb = FakeAdbHostSource()
        adb.failuresLeft = Int.MAX_VALUE
        val received = collectDiscovery(adb)

        advanceTimeBy(5 * MirrorTuning.DiscoveryPollMs + 1)

        assertEquals(6, received.count { it.second is DiscoveryModel.PollFailed })
    }

    @Test
    fun `a failed poll does not advance the empty poll escalation`() = runTest {
        val adb = FakeAdbHostSource()
        val received = collectDiscovery(adb)

        // Two empty polls, then one that never answered, then the third empty one. If the
        // failure counted as empty the escalation would already have fired by now.
        advanceTimeBy(MirrorTuning.DiscoveryPollMs + 1)
        adb.failuresLeft = 2
        advanceTimeBy(MirrorTuning.DiscoveryPollMs)
        assertIs<DiscoveryModel.PollFailed>(received.last().second)
        assertTrue(received.none { it.second is DiscoveryModel.NoDeviceFound })

        advanceTimeBy(MirrorTuning.DiscoveryPollMs)
        val escalated = assertIs<DiscoveryModel.NoDeviceFound>(received.last().second)
        assertEquals(MirrorTuning.EmptyPollEscalation, escalated.attempts)
    }

    // ---- FR-36: the escalation ladder ----

    @Test
    fun `a timed out poll heals the daemon once and recovers without waiting for the next tick`() = runTest {
        val adb = FakeAdbHostSource()
        adb.devices = listOf(device("R5CT10ABCDE", DeviceStateResource.Device))
        adb.failuresLeft = 1
        val received = collectDiscovery(adb)

        advanceTimeBy(1)

        // start-server comes AFTER the first devices call, never before it: a pre-emptive
        // call hangs against a wedged daemon and would burn the 15 s watchdog every poll.
        assertEquals(
            listOf(
                "calls=devices, start-server, devices",
                "emissions=Detecting, Found(R5CT10ABCDE)",
            ),
            listOf(
                "calls=${adb.calls.joinToString(", ")}",
                "emissions=${received.joinToString(", ") { it.second.summary() }}",
            ),
        )
    }

    @Test
    fun `a start-server that times out escalates to kill-server then start-server`() = runTest {
        val adb = FakeAdbHostSource()
        adb.devices = listOf(device("R5CT10ABCDE", DeviceStateResource.Device))
        adb.failuresLeft = 1
        // A wedged daemon - alive but not serving, so start-server hangs instead of helping.
        adb.startServerFailures = 1
        val received = collectDiscovery(adb)

        advanceTimeBy(1)

        assertEquals(
            listOf(
                "calls=devices, start-server, kill-server, start-server, devices",
                "emissions=Detecting, Found(R5CT10ABCDE)",
            ),
            listOf(
                "calls=${adb.calls.joinToString(", ")}",
                "emissions=${received.joinToString(", ") { it.second.summary() }}",
            ),
        )
    }

    @Test
    fun `a non-timeout failure spawns nothing and is reported as it is`() = runTest {
        val adb = FakeAdbHostSource()
        adb.failuresLeft = 1
        adb.failureExitCode = 1
        adb.failureReason = "error: protocol fault (couldn't read status)"
        val received = collectDiscovery(adb)

        advanceTimeBy(1)

        // Firing a 15 s watchdog at output that merely failed to parse is its own bug.
        assertEquals(
            listOf(
                "calls=devices",
                "emissions=Detecting, PollFailed(adb error: protocol fault (couldn't read status))",
            ),
            listOf(
                "calls=${adb.calls.joinToString(", ")}",
                "emissions=${received.joinToString(", ") { it.second.summary() }}",
            ),
        )
    }

    @Test
    fun `a daemon that will not come back reports the real reason and keeps polling`() = runTest {
        val adb = FakeAdbHostSource()
        adb.failuresLeft = Int.MAX_VALUE
        adb.startServerFailures = Int.MAX_VALUE
        val received = collectDiscovery(adb)

        advanceTimeBy(MirrorTuning.DiscoveryPollMs + 1)

        // The reported reason is start-server's 15 s watchdog, not the 3 s poll it began as:
        // the last thing that failed is what the user is told about. And the ladder runs once
        // per poll - it never loops inside one.
        val startServerTimedOut = "PollFailed(adb ${timeoutReason(MirrorTuning.DaemonStartTimeoutMs)})"
        assertEquals(
            listOf(
                "calls=devices, start-server, kill-server, start-server, devices, start-server, kill-server, start-server",
                "emissions=Detecting, $startServerTimedOut, $startServerTimedOut",
            ),
            listOf(
                "calls=${adb.calls.joinToString(", ")}",
                "emissions=${received.joinToString(", ") { it.second.summary() }}",
            ),
        )
    }

    @Test
    fun `a start-server that fails for another reason is not treated as a wedge`() = runTest {
        val adb = FakeAdbHostSource()
        adb.failuresLeft = 1
        adb.startServerFailures = 1
        adb.startServerExitCode = 1
        adb.startServerReason = "cannot bind to 127.0.0.1:5037"
        val received = collectDiscovery(adb)

        advanceTimeBy(1)

        // Only a HANG says the daemon is alive but not serving. A daemon that refused to
        // start for a stated reason has nothing to kill, so killing is not the answer.
        assertEquals(
            listOf(
                "calls=devices, start-server",
                "emissions=Detecting, PollFailed(adb cannot bind to 127.0.0.1:5037)",
            ),
            listOf(
                "calls=${adb.calls.joinToString(", ")}",
                "emissions=${received.joinToString(", ") { it.second.summary() }}",
            ),
        )
    }

    @Test
    fun `restartAdbServer kills the daemon before starting it`() = runTest {
        val adb = FakeAdbHostSource()

        repository(adb).restartAdbServer()

        assertEquals(listOf("kill-server", "start-server"), adb.calls)
    }

    /**
     * Starts collecting the discovery flow on the background scope and returns the list it
     * fills, each emission stamped with the virtual time it arrived at.
     */

    /**
     * FR-37 — `refreshDiscovery()` cuts the poll interval short so a Retry acts now.
     *
     * This is the test that justifies the trigger living in the data layer at all. Doing it in
     * the presentation layer was tried three ways and withdrawn: it had to be threaded through
     * the discovery gate F-2 shipped and confirmed, and it proved timing-dependent against
     * Orbit's dispatchers — unprovable, so unshippable. Here the clock is virtual, the loop is
     * the one that owns the delay, and the claim is exact rather than probabilistic.
     *
     * MUTATION: put `delay(DiscoveryPollMs)` back in place of the withTimeoutOrNull race — the
     * early-poll assertion fails, because the poll lands at 2000 ms instead of 500 ms.
     */
    @Test
    fun `a refresh request cuts the poll interval short`() = runTest {
        val adb = FakeAdbHostSource()
        val repository = repository(adb)
        val received = mutableListOf<Pair<Long, DiscoveryModel>>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            repository.observeDiscovery(PlatformModel.Android).collect { received += testScheduler.currentTime to it }
        }

        // Detecting plus the first poll land at 0; the next would normally be at 2000.
        advanceTimeBy(500)
        repository.refreshDiscovery()
        advanceTimeBy(1)

        assertEquals(
            // 500, not 501: the collector is unconfined, so the emission is observed at the
            // instant the refresh releases the wait rather than on the next tick.
            expected = listOf("pollAt=500", "waitedFullInterval=false"),
            actual = listOf(
                "pollAt=${received.last().first}",
                "waitedFullInterval=${received.last().first >= MirrorTuning.DiscoveryPollMs}",
            ),
        )
    }

    /**
     * A refresh that arrives while a poll is already in flight is KEPT, not dropped.
     *
     * The channel is CONFLATED for this: with no buffer the nudge would vanish and the user's
     * Retry would silently do nothing, which is the whole defect FR-37 exists to forbid. Two
     * rapid requests collapse into one, because "poll now" is not a queue.
     *
     * MUTATION: change the channel to `Channel<Unit>()` (rendezvous) — `trySend` then fails
     * with no receiver parked and this test's early poll never happens.
     */
    @Test
    fun `a refresh requested between polls is not lost`() = runTest {
        val adb = FakeAdbHostSource()
        val repository = repository(adb)
        // Nudge BEFORE anything collects, so nothing is waiting on the channel yet.
        repository.refreshDiscovery()

        val received = mutableListOf<Pair<Long, DiscoveryModel>>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            repository.observeDiscovery(PlatformModel.Android).collect { received += testScheduler.currentTime to it }
        }
        advanceTimeBy(1)

        assertEquals(
            expected = "polledEarly=true",
            actual = "polledEarly=${received.count { it.second is DiscoveryModel.Found } >= 2}",
        )
    }

    private fun TestScope.collectDiscovery(
        adb: AdbHostSource,
        emulators: EmulatorSource = FakeEmulatorSource(emptyList()),
        executor: DeviceCommandExecutor = FakeDeviceCommandExecutor(),
    ): List<Pair<Long, DiscoveryModel>> {
        val repository = repository(adb, emulators, executor)
        val received = mutableListOf<Pair<Long, DiscoveryModel>>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            repository.observeDiscovery(PlatformModel.Android).collect { received += testScheduler.currentTime to it }
        }
        return received
    }

    private fun TestScope.repository(
        adb: AdbHostSource,
        emulators: EmulatorSource = FakeEmulatorSource(emptyList()),
        executor: DeviceCommandExecutor = FakeDeviceCommandExecutor(),
    ): DeviceRepositoryImpl = DeviceRepositoryImpl(
        adbHostSource = adb,
        idbHostSource = FakeIdbHostSource(),
        emulatorSource = emulators,
        deviceCommandExecutor = executor,
        mirrorTransport = FakeMirrorTransport,
        adbPathPreferenceSource = FakeAdbPathPreferenceSource(),
        nowMs = { testScheduler.currentTime },
    )

    private fun device(
        serial: String,
        state: DeviceStateResource,
        isEmulator: Boolean = false,
    ): DeviceResource = DeviceResource(
        id = requireNotNull(DeviceId.of(serial)),
        state = state,
        isEmulator = isEmulator,
    )
}

/** Scriptable [AdbHostSource]; every call returns instantly, so only the poll delay moves time. */
private class FakeAdbHostSource(
    private val found: Boolean = true,
    private val version: String = "1.0.41",
) : AdbHostSource {

    var devices: List<DeviceResource> = emptyList()

    /** Set to make the next [listDevices] calls fail, the way a cold adb server does. */
    var failuresLeft: Int = 0

    /**
     * How the scripted [listDevices] failures present themselves. A timeout by default -
     * assign a non-null exit code to get the kind of failure the ladder must NOT escalate on.
     */
    var failureExitCode: Int? = null
    var failureReason: String = timeoutReason(MirrorTuning.HostCommandTimeoutMs)

    /**
     * Leading [startServer] calls that fail, and how. A timeout by default - the way a WEDGED
     * daemon makes them - because that is the only shape the ladder treats as repairable.
     */
    var startServerFailures: Int = 0
    var startServerExitCode: Int? = null
    var startServerReason: String = timeoutReason(MirrorTuning.DaemonStartTimeoutMs)

    /**
     * Every deviceless call this fake received, in order.
     *
     * The ladder is an ORDER, so asserting a count per method cannot tell kill-then-start
     * from start-then-kill - only the sequence can.
     */
    val calls: MutableList<String> = mutableListOf()

    override suspend fun probe(): ToolProbe = ToolProbe(
        kind = ToolKind.Adb,
        found = found,
        path = "/opt/homebrew/bin/adb".takeIf { found },
        version = version.takeIf { found },
        probedPaths = listOf("/opt/homebrew/bin/adb"),
        installCommand = "brew install",
        envHint = "set ANDROID_HOME",
    )

    override suspend fun listDevices(): List<DeviceResource> {
        calls += "devices"
        if (failuresLeft > 0) {
            failuresLeft--
            throw DeviceCommandException(listOf("adb", "devices", "-l"), failureExitCode, failureReason)
        }
        return devices
    }

    override suspend fun startServer() {
        calls += "start-server"
        if (startServerFailures > 0) {
            startServerFailures--
            throw DeviceCommandException(listOf("adb", "start-server"), startServerExitCode, startServerReason)
        }
    }

    override suspend fun killServer() {
        calls += "kill-server"
    }
}

/** The watchdog's own words, built from the shared constant rather than hand-typed. */
private fun timeoutReason(budgetMs: Long): String =
    "${DeviceCommandException.TIMEOUT_REASON_PREFIX}${budgetMs}ms"

/**
 * One line per emission, so a whole sequence is one [assertEquals] instead of a chain of
 * [assertTrue]s - a chain short-circuits, and a mutation that only breaks the later links
 * then looks fully killed by the first one.
 */
private fun DiscoveryModel.summary(): String = when (this) {
    is DiscoveryModel.Detecting -> "Detecting"
    is DiscoveryModel.ToolMissing -> "ToolMissing(${tool})"
    is DiscoveryModel.NoDeviceFound -> "NoDeviceFound(attempts=${attempts})"
    is DiscoveryModel.PollFailed -> "PollFailed(${tool} ${reason})"
    is DiscoveryModel.Found -> "Found(${physical.joinToString("|") { it.id }})"
    is DiscoveryModel.SimulatorNotBooted -> "SimulatorNotBooted(${simulators.size})"
    is DiscoveryModel.DeviceNotTrusted -> "DeviceNotTrusted(${deviceId})"
}

/** An `idb` probe that answers without touching a process. */
internal class FakeIdbHostSource(
    private val found: Boolean = false,
) : IdbHostSource {

    override suspend fun probe(): ToolProbe = ToolProbe(
        kind = ToolKind.Idb,
        found = found,
        path = "/opt/homebrew/bin/idb".takeIf { found },
        probedPaths = listOf("/opt/homebrew/bin/idb"),
        installCommand = "brew tap facebook/fb && brew install idb-companion",
    )
}

private class FakeEmulatorSource(avds: List<String>) : EmulatorSource {

    private val avds: List<AvdResource> = avds.map { AvdResource(requireNotNull(AvdName.of(it))) }

    override suspend fun probe(): ToolProbe = ToolProbe(kind = ToolKind.Emulator, found = true)

    override suspend fun listAvds(): List<AvdResource> = avds

    override suspend fun boot(avd: AvdName) = Unit
}

/** Answers `emu avd name`; an unmapped serial answers with an error line, as a cold one does. */
private class FakeDeviceCommandExecutor(
    private val avdNameOf: Map<String, String> = emptyMap(),
) : DeviceCommandExecutor {

    var calls: Int = 0
        private set

    override suspend fun run(device: DeviceId, command: DeviceCommand): ByteArray {
        calls++
        val name = avdNameOf[device.value] ?: return "KO: unknown command\n".encodeToByteArray()
        return "$name\nOK\n".encodeToByteArray()
    }
}

private object FakeMirrorTransport : MirrorTransport {
    override fun frames(device: DeviceId): Flow<MirrorFrame> = emptyFlow()
    override val stats: StateFlow<TransportStats> = MutableStateFlow(TransportStats())
}
