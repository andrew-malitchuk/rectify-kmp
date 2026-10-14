package presentation.core.ui.source.kit.atom.icon

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

public val MoreVertical: ImageVector
    get() {
        if (_MoreVertical != null) return _MoreVertical!!
        _MoreVertical = ImageVector.Builder(
            name = "MoreVertical",
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
                moveTo(11f, 12f)
                arcToRelative(1f, 1f, 0f, false, false, 1f, 1f)
                arcToRelative(1f, 1f, 0f, false, false, 1f, -1f)
                arcToRelative(1f, 1f, 0f, false, false, -1f, -1f)
                arcToRelative(1f, 1f, 0f, false, false, -1f, 1f)
                close()
            }
            path(
                fill = null,
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round,
            ) {
                moveTo(11f, 5f)
                arcToRelative(1f, 1f, 0f, false, false, 1f, 1f)
                arcToRelative(1f, 1f, 0f, false, false, 1f, -1f)
                arcToRelative(1f, 1f, 0f, false, false, -1f, -1f)
                arcToRelative(1f, 1f, 0f, false, false, -1f, 1f)
                close()
            }
            path(
                fill = null,
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round,
            ) {
                moveTo(11f, 19f)
                arcToRelative(1f, 1f, 0f, false, false, 1f, 1f)
                arcToRelative(1f, 1f, 0f, false, false, 1f, -1f)
                arcToRelative(1f, 1f, 0f, false, false, -1f, -1f)
                arcToRelative(1f, 1f, 0f, false, false, -1f, 1f)
                close()
            }
        }.build()
        return _MoreVertical!!
    }

@Suppress("ObjectPropertyName")
private var _MoreVertical: ImageVector? = null
