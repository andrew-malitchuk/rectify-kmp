package domain.usecase.impl.source.usecase.device

import domain.core.source.model.device.DiscoveryModel
import domain.core.source.model.device.HierarchySnapshotModel
import domain.core.source.model.device.MirrorFrameModel
import domain.core.source.model.device.MirrorStatsModel
import domain.core.source.model.device.ToolModel
import domain.core.source.monad.Failure
import domain.repository.api.source.repository.DeviceRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertIs

/**
 * [DumpHierarchyUseCaseImpl] never lets an infrastructure exception cross its boundary - a
 * dead device, a missing `adb` or a timeout must arrive as a [Failure], like every other
 * `resultLauncher`-wrapped use case (`.claude/rules/testing.md`'s open item on this pattern).
 *
 * The thrown type is a plain [RuntimeException], not `data-device-api`'s
 * `DeviceCommandException`: `domain-usecase-impl` does not depend on the data layer, and
 * `resultLauncher`'s `errorMapper` branch has to handle ANY unexpected [Throwable], not only
 * the one real adb failures happen to raise.
 */
class DumpHierarchyUseCaseImplTest {

    @Test
    fun `invoke maps a thrown infrastructure exception into a Technical Platform Failure`() {
        runBlocking {
            val repository = ThrowingDeviceRepository(RuntimeException("device offline"))
            val useCase = DumpHierarchyUseCaseImpl(repository)

            val result = useCase("R5CT10ABCDE")

            assertIs<Failure.Technical.Platform>(result.exceptionOrNull())
        }
    }
}

/** A [DeviceRepository] whose [dumpHierarchy] always throws [toThrow]. */
private class ThrowingDeviceRepository(private val toThrow: Throwable) : DeviceRepository {

    override suspend fun dumpHierarchy(deviceId: String): HierarchySnapshotModel = throw toThrow

    override suspend fun refreshDiscovery(): Unit = throw NotImplementedError()
    override suspend fun detectTools(): List<ToolModel> = throw NotImplementedError()
    override fun observeDiscovery(): Flow<DiscoveryModel> = emptyFlow()
    override suspend fun bootEmulator(avdName: String): Unit = throw NotImplementedError()
    override fun mirror(deviceId: String): Flow<MirrorFrameModel> = emptyFlow()
    override fun observeMirrorStats(): Flow<MirrorStatsModel> = emptyFlow()
    override suspend fun restartAdbServer(): Unit = throw NotImplementedError()
    override suspend fun setAdbPath(path: String): Unit = throw NotImplementedError()
}
