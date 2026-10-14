package presentation.feature.onboarding.core.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import presentation.core.styling.core.Theme

@Composable
internal fun OnboardingPagerIndicator(
    current: Int,
    total: Int,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.End,
    ) {
        Text(
            text = "// ${(current + 1).toString().padStart(2, '0')} / ${total.toString().padStart(2, '0')}",
            style = Theme.typography.overline,
            color = Theme.color.ink2,
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(Theme.spacing.spacingS),
            modifier = Modifier.fillMaxWidth(),
        ) {
            repeat(total) { index ->
                val isActive = index <= current
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(Theme.stroke.regular)
                        .background(if (isActive) Theme.color.ink else Theme.color.line),
                )
            }
        }
    }
}
