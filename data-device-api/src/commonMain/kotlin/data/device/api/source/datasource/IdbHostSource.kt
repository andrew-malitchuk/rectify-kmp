package data.device.api.source.datasource

import data.device.api.source.model.ToolProbe

/**
 * The deviceless half of `idb`.
 *
 * Probe only, and that is the whole iOS surface this release: FR-2 defers iOS MIRRORING to
 * v1.1+, but AC-1 still asks the toolbar to report whether the binary is installed — so the
 * answer has to come from a real probe, not a constant.
 *
 * REPLACED by [IosHostSource], whose `probeIdb()` subsumes this. Not deleted yet only because
 * its last caller is `DeviceRepositoryImpl`'s constructor and `detectTools()`, which T-16
 * re-points at `IosHostSource.probeSimctl()`; deleting it ahead of that would break the
 * repository and four of its test files. T-12 deletes `IdbHostSourceImpl`, T-13 the three
 * registrations, and this file goes with them.
 */
public interface IdbHostSource {

    /**
     * Locates `idb` and reads what it will say about itself.
     *
     * A missing binary yields `ToolProbe(found = false, …)`, never an exception. `idb` reports
     * no version through this path, so [ToolProbe.version] is `null` even when found.
     */
    public suspend fun probe(): ToolProbe
}
