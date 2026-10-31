package presentation.core.ui.core.modifier

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import presentation.core.styling.core.Theme

/** Distance a tile rises into place. Small on purpose — this is a settle, not a slide. */
private val RISE = 12.dp

/**
 * Staggered entrance for a bento grid: each tile fades and rises in, [index] × `tileCascadeMs`
 * after the one before it.
 *
 * This is the only decorative motion the design system keeps. It needs no driving-mode branch —
 * both the stagger and the fade read their durations from [Theme.motion], which resolves to 0 ms
 * while driving, so the grid simply appears.
 *
 * @param index Position of this tile in its grid, counted in reading order.
 */
@Composable
public fun Modifier.cascadeIn(index: Int): Modifier {
    val durations = Theme.motion.durations
    var shown by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(index.toLong() * durations.tileCascadeMs)
        shown = true
    }

    val progress by animateFloatAsState(
        targetValue = if (shown) 1f else 0f,
        animationSpec = tween(
            durationMillis = durations.normalMs,
            easing = Theme.motion.easings.easeInOut,
        ),
        label = "cascadeIn",
    )

    return this.graphicsLayer {
        alpha = progress
        translationY = (1f - progress) * RISE.toPx()
    }
}
