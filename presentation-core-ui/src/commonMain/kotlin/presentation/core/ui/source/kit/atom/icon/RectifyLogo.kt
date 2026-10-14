package presentation.core.ui.source.kit.atom.icon

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.addPathNodes
import androidx.compose.ui.unit.dp
import presentation.core.styling.core.Theme

public val RectifyLogo: ImageVector
    @Composable
    get() {
        if (_RectifyLogo != null) return _RectifyLogo!!
        _RectifyLogo = ImageVector.Builder(
            name = "RectifyLogo",
            defaultWidth = 86.dp,
            defaultHeight = 51.dp,
            viewportWidth = 86f,
            viewportHeight = 51f,
        ).apply {
            // Placeholder mark: a rounded square centred in the 86x51 viewport.
            // Swap this out for your own brand mark.
            addPath(
                pathData = addPathNodes(
                    "M31,5.5 h24 a7.5,7.5 0 0,1 7.5,7.5 v20 a7.5,7.5 0 0,1 -7.5,7.5 h-24 " +
                        "a7.5,7.5 0 0,1 -7.5,-7.5 v-20 a7.5,7.5 0 0,1 7.5,-7.5 z",
                ),
                fill = SolidColor(Theme.color.ink),
            )
        }.build()
        return _RectifyLogo!!
    }

@Suppress("ObjectPropertyName")
private var _RectifyLogo: ImageVector? = null
