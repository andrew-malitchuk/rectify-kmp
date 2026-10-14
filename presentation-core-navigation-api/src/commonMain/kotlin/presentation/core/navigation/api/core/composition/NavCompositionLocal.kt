package presentation.core.navigation.api.core.composition

import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.compositionLocalOf
import presentation.core.navigation.api.source.destination.AppNavigator

/**
 * Provides the [AppNavigator] instance to the composition tree for screen-level navigation.
 *
 * @see AppNavigator
 */
public val LocalAppNavigator: ProvidableCompositionLocal<AppNavigator?> =
    compositionLocalOf { error("No App Navigator Provided") }

/**
 * Provides a back-navigation callback, typically bound to [AppNavigator.popBackStack].
 */
public val LocalBackAction: ProvidableCompositionLocal<() -> Unit> =
    compositionLocalOf { error("No Back Action Provided") }
