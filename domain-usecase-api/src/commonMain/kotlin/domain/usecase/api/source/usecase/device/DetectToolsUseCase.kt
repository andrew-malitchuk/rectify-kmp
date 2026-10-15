package domain.usecase.api.source.usecase.device

import domain.core.source.model.device.ToolModel

/**
 * Use case for probing the external platform tools.
 */
public interface DetectToolsUseCase {
    /**
     * @return A [Result] holding one [ToolModel] per platform. A tool that is not installed
     *   is reported as `isInstalled = false`, not as a failure.
     */
    public suspend operator fun invoke(): Result<List<ToolModel>>
}
