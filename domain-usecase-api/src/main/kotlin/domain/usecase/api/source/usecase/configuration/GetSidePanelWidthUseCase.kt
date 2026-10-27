package domain.usecase.api.source.usecase.configuration

/**
 * Use case for retrieving the persisted shell side panel width, in dp.
 */
public interface GetSidePanelWidthUseCase {
    /**
     * @return A [Result] containing the stored width in dp.
     */
    public suspend operator fun invoke(): Result<Int>
}
