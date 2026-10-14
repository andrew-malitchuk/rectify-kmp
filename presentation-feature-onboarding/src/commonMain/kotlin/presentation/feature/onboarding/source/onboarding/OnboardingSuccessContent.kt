package presentation.feature.onboarding.source.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import rectify_kmp.presentation_core_localisation.generated.resources.Res
import rectify_kmp.presentation_core_localisation.generated.resources.onboarding_action_initialize
import rectify_kmp.presentation_core_localisation.generated.resources.onboarding_action_next
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import presentation.core.styling.core.Theme
import presentation.core.styling.source.theme.AppTheme
import presentation.core.ui.source.kit.atom.button.AppButton
import presentation.core.ui.source.kit.atom.icon.ChevronRight
import presentation.feature.onboarding.core.component.OnboardingPageContent
import presentation.feature.onboarding.core.component.OnboardingPagerIndicator
import presentation.feature.onboarding.core.page.ONBOARDING_PAGE_COUNT
import presentation.feature.onboarding.core.page.OnboardingPage

private val PAGES = OnboardingPage.entries.toList()

@Composable
internal fun OnboardingSuccessContent(
    state: OnboardingState,
    onIntent: (OnboardingIntent) -> Unit,
) {
    val pagerState = rememberPagerState(pageCount = { ONBOARDING_PAGE_COUNT })
    val scope = rememberCoroutineScope()

    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.currentPage }.collect { page ->
            onIntent(OnboardingIntent.OnPageChanged(page))
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Theme.color.bg)
            .statusBarsPadding(),
    ) {
        OnboardingPagerIndicator(
            current = state.currentPage,
            total = ONBOARDING_PAGE_COUNT,
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = Theme.spacing.spacingL,
                    vertical = Theme.spacing.spacingL,
                ),
        )

        HorizontalPager(
            state = pagerState,
            modifier = Modifier.weight(1f),
            beyondViewportPageCount = 1,
        ) { pageIndex ->
            val page = PAGES[pageIndex]
            OnboardingPageContent(
                page = page,
                pageIndex = pageIndex,
                pagerState = pagerState,
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(
                    horizontal = Theme.spacing.spacingL,
                    vertical = Theme.spacing.spacingL,
                ),
        ) {
            val currentPage = PAGES.getOrNull(state.currentPage) ?: OnboardingPage.Welcome

            when (currentPage) {
                OnboardingPage.Welcome -> {
                    AppButton(
                        text = stringResource(Res.string.onboarding_action_next),
                        icon = ChevronRight,
                        onClick = {
                            scope.launch {
                                pagerState.animateScrollToPage(state.currentPage + 1)
                            }
                        },
                        fillMaxWidth = true,
                    )
                }

                OnboardingPage.Ready -> {
                    AppButton(
                        text = stringResource(Res.string.onboarding_action_initialize),
                        icon = ChevronRight,
                        onClick = { onIntent(OnboardingIntent.OnGetStartedClick) },
                        enabled = !state.isLoading,
                        fillMaxWidth = true,
                    )
                }
            }
        }
    }
}

@Preview
@Composable
private fun OnboardingSuccessContentPreview() {
    AppTheme {
        OnboardingSuccessContent(
            state = OnboardingState(),
            onIntent = {},
        )
    }
}
