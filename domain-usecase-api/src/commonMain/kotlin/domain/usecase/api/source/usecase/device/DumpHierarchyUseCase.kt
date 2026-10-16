package domain.usecase.api.source.usecase.device

import domain.core.source.model.device.HierarchySnapshotModel

/**
 * Use case for capturing a device's view hierarchy.
 */
public interface DumpHierarchyUseCase {
    /**
     * @param deviceId Identifier of the device to capture.
     * @return A [Result] holding the snapshot. An unreadable dump is
     *   [HierarchySnapshotModel.Empty] rather than a failure — only a dead device, a missing
     *   `adb` or a timeout fails.
     */
    public suspend operator fun invoke(deviceId: String): Result<HierarchySnapshotModel>
}
