package presentation.core.ui.source.kit.atom.icon

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

public val Wizard: ImageVector
    get() {
        if (_Wizard != null) return _Wizard!!
        _Wizard = ImageVector.Builder(
            name = "Wizard",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).apply {
            path(
                fill = null,
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round,
            ) {
                moveTo(13f, 2f)
                lineTo(3f, 14f)
                lineTo(12f, 14f)
                lineTo(11f, 22f)
                lineTo(21f, 10f)
                lineTo(12f, 10f)
                lineTo(13f, 2f)
                close()
            }
        }.build()
        return _Wizard!!
    }

@Suppress("ObjectPropertyName")
private var _Wizard: ImageVector? = null
