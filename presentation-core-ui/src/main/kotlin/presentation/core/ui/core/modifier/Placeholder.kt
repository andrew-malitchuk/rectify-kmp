package presentation.core.ui.core.modifier

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.ui.tooling.preview.Preview
import presentation.core.styling.core.Theme
import presentation.core.styling.source.theme.AppTheme

/**
 * Holds the current loading state for the composition tree.
 *
 * @param isLoading When `true`, composables carrying the [placeholder] modifier render a
 *   flat block instead of their content.
 */
public data class PlaceholderState(val isLoading: Boolean)

/**
 * CompositionLocal that provides [PlaceholderState] to descendant composables.
 *
 * Provided by [PlaceholderProvider]. Defaults to `isLoading = false` so composables are
 * safe to use outside a provider (previews, tests).
 */
public val LocalPlaceholderState: ProvidableCompositionLocal<PlaceholderState> =
    compositionLocalOf { PlaceholderState(isLoading = false) }

/**
 * Provides a [PlaceholderState] to the composition tree via [LocalPlaceholderState].
 *
 * ```kotlin
 * PlaceholderProvider(isLoading = state.isLoading) {
 *     Box(modifier = Modifier.placeholder())
 * }
 * ```
 *
 * @param isLoading Whether descendants with [placeholder] render as blocks.
 * @param content The composable subtree that consumes [LocalPlaceholderState].
 */
@Composable
public fun PlaceholderProvider(
    isLoading: Boolean = true,
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(
        LocalPlaceholderState provides PlaceholderState(isLoading = isLoading),
        content = content,
    )
}

/**
 * Fills the composable with a flat block while [LocalPlaceholderState] reports
 * `isLoading = true`; otherwise the modifier is a no-op.
 *
 * This replaces v1's animated shimmer sweep. The shimmer was Material's idiom and it lied
 * twice over: a moving highlight implies work is progressing when nothing may be happening,
 * and a perpetual animation is exactly what reduced motion asks to avoid. A loading tile in
 * this system is simply a tile without a value.
 *
 * @param shape Clip shape of the block. Defaults to the nested-block corner token.
 * @param color Fill color of the block.
 */
@Composable
public fun Modifier.placeholder(
    shape: Shape = Theme.cornerToken.nested,
    color: Color = Theme.color.stage,
): Modifier {
    if (!LocalPlaceholderState.current.isLoading) return this
    return this.background(color = color, shape = shape)
}

@Preview(showBackground = true)
@Composable
private fun PlaceholderProviderPreview() {
    AppTheme {
        PlaceholderProvider(isLoading = true) {
            Column(
                modifier = Modifier
                    .background(Theme.color.bg)
                    .fillMaxWidth()
                    .padding(Theme.spacing.spacingL),
                verticalArrangement = Arrangement.spacedBy(Theme.spacing.spacingS),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(Theme.spacing.spacingXL)
                        .placeholder(),
                )
                Box(
                    modifier = Modifier
                        .width(160.dp)
                        .height(Theme.spacing.spacingL)
                        .placeholder(),
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp)
                        .placeholder(),
                )
            }
        }
    }
}
