package presentation.feature.onboarding.core.component

import androidx.compose.foundation.pager.PagerState
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.TextStyle

internal fun Modifier.parallax(
    pagerState: PagerState,
    page: Int,
    factor: Float,
): Modifier = this.graphicsLayer {
    val pageOffset = (pagerState.currentPage - page) + pagerState.currentPageOffsetFraction
    translationX = pageOffset * size.width * factor
}

@Composable
internal fun ParallaxText(
    text: String,
    style: TextStyle,
    color: Color,
    pagerState: PagerState,
    page: Int,
    factor: Float,
    modifier: Modifier = Modifier,
) {
    Text(
        text = text,
        style = style,
        color = color,
        modifier = modifier.parallax(pagerState, page, factor),
    )
}
