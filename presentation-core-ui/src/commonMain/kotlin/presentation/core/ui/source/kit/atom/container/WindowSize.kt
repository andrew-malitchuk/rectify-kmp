package presentation.core.ui.source.kit.atom.container

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Simplified window size class for adaptive layout decisions.
 *
 * Breakpoints follow Material 3 / Google adaptive guidelines:
 * - [Compact]  < 600dp  — phones in portrait
 * - [Medium]   600–839dp — phones in landscape, small tablets
 * - [Expanded] ≥ 840dp  — tablets, desktops, foldables unfolded
 */
public enum class WindowSize { Compact, Medium, Expanded }

/** Converts a measured [Dp] width into the corresponding [WindowSize] bucket. */
public fun Dp.toWindowSize(): WindowSize = when {
    this < 600.dp -> WindowSize.Compact
    this < 840.dp -> WindowSize.Medium
    else -> WindowSize.Expanded
}

/**
 * CompositionLocal that provides the current [WindowSize] to the composition tree.
 *
 * Provided by [SafeContainer] via [BoxWithConstraints]. Defaults to [WindowSize.Compact]
 * so composables are safe to use outside of [SafeContainer] in previews and tests.
 */
public val LocalWindowSize: androidx.compose.runtime.ProvidableCompositionLocal<WindowSize> =
    compositionLocalOf { WindowSize.Compact }
