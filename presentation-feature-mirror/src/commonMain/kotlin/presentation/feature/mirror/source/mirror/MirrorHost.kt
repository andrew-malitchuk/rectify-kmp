package presentation.feature.mirror.source.mirror

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.ImageBitmap
import org.koin.compose.viewmodel.koinViewModel
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect

/**
 * Entry point of the mirror surface.
 *
 * A **host, not a screen**: it owns no layout. There is no `Destination` for this surface —
 * it is a set of shell slots the desktop window fills — so none of `navigation.md`'s checklist
 * applies and there is nothing for a `*Screen` to navigate. It resolves the ViewModel, hands
 * state, the decoded frame and the intent sink to [content], and gets out of the way.
 *
 * The frame is collected separately from the state on purpose: it is a `StateFlow` outside the
 * reducer, which is what keeps ten frames a second from becoming ten state copies a second.
 *
 * @param content Renders the surface. Receives the state, the latest frame and the intent sink.
 * @param viewModel The state machine; resolved from Koin by default.
 * @param onSideEffect One-shot events. F-2 renders no error UI (that is F-3's card), so the
 *   default drops them rather than pretending to handle them.
 */
@Composable
public fun MirrorHost(
    viewModel: MirrorViewModel = koinViewModel(),
    onSideEffect: (MirrorSideEffect) -> Unit = {},
    content: @Composable (MirrorUiState, ImageBitmap?, (MirrorIntent) -> Unit) -> Unit,
) {
    val state by viewModel.collectAsState()
    val frame by viewModel.frame.collectAsState()

    viewModel.collectSideEffect { effect -> onSideEffect(effect) }

    content(state, frame, viewModel::handleIntent)
}
