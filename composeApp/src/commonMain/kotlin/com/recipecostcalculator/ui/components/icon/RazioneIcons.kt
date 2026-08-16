package com.recipecostcalculator.ui.components.icon

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.unit.dp

private const val StrokeWidth = 1.75f
private val StrokeColor = SolidColor(Color.Black)

private fun razioneIcon(name: String, vararg pathData: String): ImageVector =
    ImageVector.Builder(
        name = name,
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        pathData.forEach { d ->
            addPath(
                pathData = PathParser().parsePathString(d).toNodes(),
                fill = null,
                stroke = StrokeColor,
                strokeLineWidth = StrokeWidth,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            )
        }
    }.build()

object RazioneIcons {
    val recipe: ImageVector
        get() = razioneIcon(
            name = "Receta",
            "M4 5.5h7a3 3 0 0 1 3 3V19a2.5 2.5 0 0 0-2.5-2.5H4z",
            "M20 5.5h-3.5a3 3 0 0 0-2.5 3V19a2.5 2.5 0 0 1 2.5-2.5H20z"
        )

    val ingredient: ImageVector
        get() = razioneIcon(
            name = "Ingrediente",
            "M11 20c-4 0-7-3-7-7 5-1 8 2 8 7",
            "M12 20c0-6 3-10 8-11 0 6-3 10-8 11"
        )

    val scale: ImageVector
        get() = razioneIcon(
            name = "Balanza",
            "M12 4v3",
            "M5 20h14",
            "M4 10h16l-2.5 10h-11z",
            "M9 14h6"
        )

    val cost: ImageVector
        get() = razioneIcon(
            name = "Costo",
            "M13.5 3.5H20V10l-9.5 9.5a2 2 0 0 1-2.8 0l-4.2-4.2a2 2 0 0 1 0-2.8z",
            "M16.5 7h.01"
        )

    val margin: ImageVector
        get() = razioneIcon(
            name = "Margen",
            "M4 17 9.5 11l3.5 3L20 7",
            "M15 7h5v5"
        )

    val inventory: ImageVector
        get() = razioneIcon(
            name = "Inventario",
            "M3.5 7.5 12 4l8.5 3.5V17L12 20.5 3.5 17z",
            "M3.5 7.5 12 11l8.5-3.5",
            "M12 11v9.5"
        )

    val supplier: ImageVector
        get() = razioneIcon(
            name = "Proveedor",
            "M2.5 6.5h9v9h-9z",
            "M11.5 10h4l2.5 2.5v3h-6.5z",
            "M4.5 18.0a2.0 2.0 0 1 0 4.0 0a2.0 2.0 0 1 0 -4.0 0",
            "M13.5 18.0a2.0 2.0 0 1 0 4.0 0a2.0 2.0 0 1 0 -4.0 0"
        )

    val servings: ImageVector
        get() = razioneIcon(
            name = "Porciones",
            "M12 8v8",
            "M9 10.5h6",
            "M4.0 12.0a8.0 8.0 0 1 0 16.0 0a8.0 8.0 0 1 0 -16.0 0"
        )

    val time: ImageVector
        get() = razioneIcon(
            name = "Tiempo",
            "M12 7.5V12l3 2",
            "M4.0 12.0a8.0 8.0 0 1 0 16.0 0a8.0 8.0 0 1 0 -16.0 0"
        )

    val calculator: ImageVector
        get() = razioneIcon(
            name = "Calculadora",
            "M5.5 3.5h13v17h-13z",
            "M8.5 7.5h7",
            "M9 12h.01",
            "M12 12h.01",
            "M15 12h.01",
            "M9 16h.01",
            "M12 16h.01",
            "M15 16h.01"
        )

    val currency: ImageVector
        get() = razioneIcon(
            name = "Moneda",
            "M14 9.5c-.6-.8-3.8-1.3-4 .8-.2 2 4 1.3 4 3.2 0 2-3.4 1.6-4 .8",
            "M12 6.5v11",
            "M4.0 12.0a8.0 8.0 0 1 0 16.0 0a8.0 8.0 0 1 0 -16.0 0"
        )

    val alert: ImageVector
        get() = razioneIcon(
            name = "Alerta",
            "M12 3.5 2.8 19.5h18.4z",
            "M12 9.5v4.5",
            "M12 17h.01"
        )

    val search: ImageVector
        get() = razioneIcon(
            name = "Buscar",
            "M15.8 15.8 20.5 20.5",
            "M4.5 11.0a6.5 6.5 0 1 0 13.0 0a6.5 6.5 0 1 0 -13.0 0"
        )

    val filter: ImageVector
        get() = razioneIcon(
            name = "Filtro",
            "M4 6.5h16",
            "M7 12h10",
            "M10 17.5h4"
        )

    val bowl: ImageVector
        get() = razioneIcon(
            name = "Bowl",
            "M3.5 12h17c0 4.5-3.8 7.5-8.5 7.5S3.5 16.5 3.5 12z",
            "M9 8.5c1-2 4-2 5 0"
        )

    val add: ImageVector
        get() = razioneIcon(
            name = "Agregar",
            "M12 5.5v13",
            "M5.5 12h13"
        )

}
