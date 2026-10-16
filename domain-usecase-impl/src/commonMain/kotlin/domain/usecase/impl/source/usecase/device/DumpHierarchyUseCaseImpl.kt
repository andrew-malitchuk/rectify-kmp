package domain.usecase.impl.source.usecase.device

import domain.core.source.model.device.HierarchySnapshotModel
import domain.core.source.monad.Failure
import domain.repository.api.source.repository.DeviceRepository
import domain.usecase.api.source.usecase.device.DumpHierarchyUseCase
import domain.usecase.impl.core.resultLauncher

/**
 * Default implementation of [DumpHierarchyUseCase].
 *
 * No timeout here: the dump's child process already carries its own, longer budget
 * (`MirrorTuning.DumpTimeoutMs`, 10 s for a ~2.7 s measured capture) inside the data layer.
 *
 * @property deviceRepository Repository used for capturing the hierarchy.
 */
internal class DumpHierarchyUseCaseImpl(
    private val deviceRepository: DeviceRepository,
) : DumpHierarchyUseCase {

    /**
     * @param deviceId Identifier of the device to capture.
     * @return A [Result] holding the snapshot.
     * @see DumpHierarchyUseCase.invoke
     */
    override suspend fun invoke(deviceId: String): Result<HierarchySnapshotModel> = resultLauncher(
        errorMapper = Failure.Technical::Platform,
    ) {
        deviceRepository.dumpHierarchy(deviceId)
    }
}
