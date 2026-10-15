package domain.usecase.api.source.usecase.device

import domain.core.source.model.device.MirrorStatsModel
import kotlinx.coroutines.flow.Flow

/**
 * Use case for observing live statistics of the active mirroring session.
 */
public interface ObserveMirrorStatsUseCase {
    /**
     * @return A [Flow] of [MirrorStatsModel]. `latencyMs` is `null` when the active
     *   transport cannot measure it — render a placeholder, never a fabricated number.
     */
    public operator fun invoke(): Flow<MirrorStatsModel>
}
