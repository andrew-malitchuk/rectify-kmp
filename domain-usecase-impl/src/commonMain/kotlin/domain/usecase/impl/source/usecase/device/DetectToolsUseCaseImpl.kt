package domain.usecase.impl.source.usecase.device

import domain.core.source.model.device.ToolModel
import domain.core.source.monad.Failure
import domain.repository.api.source.repository.DeviceRepository
import domain.usecase.api.source.usecase.device.DetectToolsUseCase
import domain.usecase.impl.core.resultLauncher

/**
 * Default implementation of [DetectToolsUseCase].
 *
 * No timeout is imposed here: the probe's own child process is already bounded by
 * `MirrorTuning.HostCommandTimeoutMs` inside the data layer, and a second bound at this level
 * would only be able to express itself as `withTimeoutOrNull` returning `null` — a missing
 * answer that the UI cannot tell apart from a missing tool.
 *
 * @property deviceRepository Repository used for probing the platform tools.
 */
internal class DetectToolsUseCaseImpl(
    private val deviceRepository: DeviceRepository,
) : DetectToolsUseCase {

    /**
     * @return A [Result] holding one [ToolModel] per platform.
     * @see DetectToolsUseCase.invoke
     */
    override suspend fun invoke(): Result<List<ToolModel>> = resultLauncher(
        errorMapper = Failure.Technical::Process,
    ) {
        deviceRepository.detectTools()
    }
}
