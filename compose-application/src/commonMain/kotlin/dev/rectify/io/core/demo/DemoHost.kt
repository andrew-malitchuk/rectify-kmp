package dev.rectify.io.core.demo

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import presentation.core.styling.core.Theme
import presentation.core.ui.source.showcase.StyleguideScreen

/**
 * Hosts the design-system showcase in demo mode.
 *
 * Used only when `DEMO_MODE` is enabled in [dev.rectify.io.source.app.App].
 *
 * This used to own two screens of its own — a token gallery and a component gallery — which
 * together restated what [StyleguideScreen] already shows. Multiple copies of one style guide
 * drift apart within a release, and the copy nobody opens drifts first. The demo host now
 * renders the canonical screen instead of maintaining its own.
 *
 * @see StyleguideScreen
 */
@Composable
internal fun DemoHost() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Theme.color.bg),
    ) {
        StyleguideScreen(onBack = {})
    }
}
