package presentation.feature.splash.source.splash

import androidx.compose.runtime.Composable
import org.jetbrains.compose.ui.tooling.preview.Preview
import presentation.core.styling.source.theme.AppTheme

@Composable
internal fun SplashContent() {
    SplashSuccessContent()
}

@Preview
@Composable
private fun SplashContentPreview() {
    AppTheme {
        SplashContent()
    }
}
