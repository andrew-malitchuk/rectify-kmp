package presentation.core.ui.core.ext

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import kotlinx.coroutines.delay
import presentation.core.styling.core.Theme

/**
 * Samples [value] at the rate live numerals are allowed to change (`numeralRefreshMs`, ≈ 4 Hz).
 *
 * Telemetry arrives far faster than a person can read it. Rendering every packet makes the digits
 * smear into noise, so values step rather than stream. Unlike the other motion tokens this one is
 * *not* suppressed while driving — it is a rate limit, and at speed it matters more, not less.
 */
@Composable
public fun <T> rememberThrottled(value: T): T {
    val periodMs = Theme.motion.durations.numeralRefreshMs.toLong().coerceAtLeast(1L)
    val latest by rememberUpdatedState(value)
    var shown by remember { mutableStateOf(value) }

    LaunchedEffect(periodMs) {
        while (true) {
            shown = latest
            delay(periodMs)
        }
    }
    return shown
}
