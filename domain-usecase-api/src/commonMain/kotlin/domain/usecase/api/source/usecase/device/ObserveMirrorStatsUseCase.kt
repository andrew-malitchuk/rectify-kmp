package domain.usecase.api.source.usecase.device

import domain.core.source.model.device.MirrorStatsModel
import domain.core.source.model.device.PlatformModel
import kotlinx.coroutines.flow.Flow

/**
 * Use case for observing live statistics of the active mirroring session.
 */
public interface ObserveMirrorStatsUseCase {
    /**
     * @param platform Which device family the session streams from.
     * @return A [Flow] of [MirrorStatsModel]. `latencyMs` is `null` when the active
     *   transport cannot measure it — render a placeholder, never a fabricated number.
     */
    public operator fun invoke(platform: PlatformModel): Flow<MirrorStatsModel>
}
