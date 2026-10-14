package presentation.feature.onboarding.core.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.PagerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import rectify_kmp.presentation_core_localisation.generated.resources.Res
import rectify_kmp.presentation_core_localisation.generated.resources.onboarding_ready_body
import rectify_kmp.presentation_core_localisation.generated.resources.onboarding_ready_title
import rectify_kmp.presentation_core_localisation.generated.resources.onboarding_welcome_body
import rectify_kmp.presentation_core_localisation.generated.resources.onboarding_welcome_title
import org.jetbrains.compose.resources.stringResource
import presentation.core.styling.core.Theme
import presentation.feature.onboarding.core.page.OnboardingPage

private const val TITLE_PARALLAX = 0.30f
private const val BODY_PARALLAX = 0.65f

@Composable
internal fun OnboardingPageContent(
    page: OnboardingPage,
    pageIndex: Int,
    pagerState: PagerState,
    modifier: Modifier = Modifier,
) {
    val title = stringResource(
        when (page) {
            OnboardingPage.Welcome -> Res.string.onboarding_welcome_title
            OnboardingPage.Ready -> Res.string.onboarding_ready_title
        },
    )
    val body = stringResource(
        when (page) {
            OnboardingPage.Welcome -> Res.string.onboarding_welcome_body
            OnboardingPage.Ready -> Res.string.onboarding_ready_body
        },
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .clipToBounds()
            .padding(
                horizontal = Theme.spacing.spacingL,
                vertical = Theme.spacing.spacingXL,
            ),
    ) {
        Spacer(modifier = Modifier.height(Theme.spacing.spacing2XL))

        ParallaxText(
            text = title,
            style = Theme.typography.titleL,
            color = Theme.color.ink,
            pagerState = pagerState,
            page = pageIndex,
            factor = TITLE_PARALLAX,
        )

        Spacer(modifier = Modifier.height(Theme.spacing.spacingL))

        Box(
            modifier = Modifier
                .width(Theme.spacing.spacing2XL)
                .height(Theme.stroke.regular)
                .background(Theme.color.line2)
                .graphicsLayer {
                    val offset = (pagerState.currentPage - pageIndex) + pagerState.currentPageOffsetFraction
                    translationX = offset * size.width * TITLE_PARALLAX
                },
        )

        Spacer(modifier = Modifier.height(Theme.spacing.spacingL))

        ParallaxText(
            text = body,
            style = Theme.typography.body,
            color = Theme.color.ink2,
            pagerState = pagerState,
            page = pageIndex,
            factor = BODY_PARALLAX,
        )

        Spacer(modifier = Modifier.weight(1f))
    }
}
