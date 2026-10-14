package presentation.core.ui.source.kit.atom.icon

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

public val RefreshCcw: ImageVector
    get() {
        if (_RefreshCcw != null) return _RefreshCcw!!
        _RefreshCcw = ImageVector.Builder(
            name = "RefreshCcw",
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
                moveTo(1f, 4f)
                lineTo(1f, 10f)
                lineTo(7f, 10f)
            }
            path(
                fill = null,
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round,
            ) {
                moveTo(23f, 20f)
                lineTo(23f, 14f)
                lineTo(17f, 14f)
            }
            path(
                fill = null,
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round,
            ) {
                moveTo(20.49f, 9f)
                arcToRelative(9f, 9f, 0f, false, false, -15.14f, -2.36f)
                lineTo(1f, 10f)
                moveTo(23f, 14f)
                lineToRelative(-4.36f, 4.36f)
                arcToRelative(9f, 9f, 0f, false, true, -15.14f, -2.36f)
            }
        }.build()
        return _RefreshCcw!!
    }

@Suppress("ObjectPropertyName")
private var _RefreshCcw: ImageVector? = null
