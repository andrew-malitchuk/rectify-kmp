package presentation.feature.mirror.source.mirror

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.ImageBitmap
import domain.core.source.model.device.HierarchySnapshotModel
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
 * Two frame flows are collected and **one** is handed on: while a frame is pinned the content
 * layer must draw the pinned pixels, and resolving that here rather than widening [content]'s
 * signature keeps every existing call site compiling unchanged. The capture loop keeps running
 * behind the pinned frame either way (FR-30).
 *
 * The cached hierarchy travels the same way, through [content], for the reason
 * `MirrorViewModel.frozenHierarchy`'s own KDoc gives: a few-hundred-node snapshot reduced into
 * [MirrorUiState] would be copied on every unrelated reduction. It is handed down rather than
 * re-resolved at the panel, because a second `koinViewModel()` call would rely on two call sites
 * implicitly sharing a `ViewModelStoreOwner` — true today, silently wrong the day the shell
 * nests one. There is exactly **one** `MirrorHost` call site; keep it that way.
 *
 * @param content Renders the surface. Receives the state, the latest frame, the hierarchy
 *   captured for the pinned frame and the intent sink.
 * @param viewModel The state machine; resolved from Koin by default.
 * @param onSideEffect One-shot events. F-2 renders no error UI (that is F-3's card), so the
 *   default drops them rather than pretending to handle them.
 */
@Composable
public fun MirrorHost(
    viewModel: MirrorViewModel = koinViewModel(),
    // The sink is handed to the handler, not just the effect. A side effect exists to ask the
    // platform for something the ViewModel cannot do itself - open a file dialog - and the
    // answer has to get back in as an intent. A handler that receives only the effect can
    // observe but never reply, which is how MirrorSideEffect.LocateAdb came to open nothing.
    // `suspend`, because the whole point of a side effect is to ask the platform for
    // something — and a file dialog has to be confined to the EDT, which needs a
    // `withContext`. Orbit's `collectSideEffect` body is already a coroutine, so this is free.
    onSideEffect: suspend (MirrorSideEffect, (MirrorIntent) -> Unit) -> Unit = { _, _ -> },
    content: @Composable (MirrorUiState, ImageBitmap?, HierarchySnapshotModel, (MirrorIntent) -> Unit) -> Unit,
) {
    val state by viewModel.collectAsState()
    val frame by viewModel.frame.collectAsState()
    val frozenFrame by viewModel.frozenFrame.collectAsState()
    val frozenHierarchy by viewModel.frozenHierarchy.collectAsState()

    viewModel.collectSideEffect { effect -> onSideEffect(effect, viewModel::handleIntent) }

    content(state, frozenFrame ?: frame, frozenHierarchy, viewModel::handleIntent)
}
