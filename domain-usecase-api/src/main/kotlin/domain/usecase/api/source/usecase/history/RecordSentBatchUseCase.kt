package domain.usecase.api.source.usecase.history

import domain.core.source.model.history.SentBatchModel
import domain.usecase.api.source.monad.Optional

/**
 * Use case for making a just-sent batch of annotations durable.
 */
public interface RecordSentBatchUseCase {
    /**
     * @param batch The batch that was just published to the coding agent.
     * @return An [Optional] result; a storage failure arrives as
     *   `Failure.Technical.Database` instead of escaping as an exception, so recording
     *   history can never fail a send.
     */
    public suspend operator fun invoke(batch: SentBatchModel): Optional
}
