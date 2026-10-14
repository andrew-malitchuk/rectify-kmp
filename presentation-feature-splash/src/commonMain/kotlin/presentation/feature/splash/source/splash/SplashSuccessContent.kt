package presentation.feature.splash.source.splash

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import presentation.core.styling.core.Theme
import presentation.core.styling.source.theme.AppTheme
import presentation.core.ui.source.kit.atom.icon.RectifyLogo
import rectify_kmp.presentation_core_localisation.generated.resources.Res
import rectify_kmp.presentation_core_localisation.generated.resources.splash_title

private const val LOGO_ASPECT_RATIO = 86f / 51f
private const val LOGO_WIDTH_FRACTION = 0.65f

@Composable
internal fun SplashSuccessContent() {
    val visibleState = remember {
        MutableTransitionState(false).apply { targetState = true }
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Theme.color.bg),
        contentAlignment = Alignment.Center,
    ) {
        AnimatedVisibility(
            visibleState = visibleState,
            enter = fadeIn(animationSpec = tween(durationMillis = Theme.motion.durations.normalMs)),
        ) {
            Image(
                imageVector = RectifyLogo,
                contentDescription = stringResource(Res.string.splash_title),
                modifier = Modifier
                    .fillMaxWidth(LOGO_WIDTH_FRACTION)
                    .aspectRatio(LOGO_ASPECT_RATIO),
            )
        }
    }
}

@Preview
@Composable
private fun SplashSuccessContentPreview() {
    AppTheme {
        SplashSuccessContent()
    }
}
