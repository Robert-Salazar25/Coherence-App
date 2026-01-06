package com.example.coherence.ui.screens.history.components.monthly.components

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

@Composable
fun GradientLineChart(
    data: List<Float>,
    graphColor: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier,
    showAxes: Boolean = true
) {
    if (data.isEmpty()) return

    // Colores y Estilos
    val labelColor = MaterialTheme.colorScheme.onSurfaceVariant
    val gridColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
    val textMeasurer = androidx.compose.ui.text.rememberTextMeasurer()
    val labelStyle = MaterialTheme.typography.labelSmall

    // Estado para la interacción táctil
    var selectedIndex by remember { mutableStateOf<Int?>(null) }

    androidx.compose.foundation.Canvas(
        modifier = modifier
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = { offset ->
                        val paddingStart = if (showAxes) 40.dp.toPx() else 0f
                        val chartWidth = size.width - paddingStart
                        val xStep = chartWidth / (data.size - 1).coerceAtLeast(1)
                        val relativeX = (offset.x - paddingStart).coerceIn(0f, chartWidth)
                        val index = (relativeX / xStep).roundToInt().coerceIn(0, data.size - 1)
                        selectedIndex = index
                        tryAwaitRelease()
                        selectedIndex = null
                    }
                )
            }
    ) {
        // 1. Configuración de dimensiones y márgenes visuales
        val paddingStart = if (showAxes) 40.dp.toPx() else 0f
        val paddingBottom = if (showAxes) 24.dp.toPx() else 0f

        // Margen superior e inferior para que los puntos extremos no se corten (mitad del tamaño del punto)
        val verticalPadding = 8.dp.toPx()

        val chartWidth = size.width - paddingStart
        // La altura disponible para dibujar DATOS es la altura total menos los paddings
        val chartHeight = size.height - paddingBottom - (verticalPadding * 2)

        val minVal = data.minOrNull() ?: 0f
        val maxVal = data.maxOrNull() ?: 100f
        // Si todos los valores son iguales, evitamos división por cero
        val range = (maxVal - minVal).coerceAtLeast(1f)

        // Función auxiliar para calcular Y exacto
        fun getY(value: Float): Float {
            val normalized = (value - minVal) / range
            // Invertimos Y (0 es arriba) y sumamos el padding superior
            return (chartHeight - (normalized * chartHeight)) + verticalPadding
        }

        // 2. Dibujar Grid (Líneas Punteadas) EXACTAS
        if (showAxes) {
            val steps = 3
            val pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)

            for (i in 0..steps) {
                // Calculamos el valor exacto para esta línea del grid
                val ratio = i.toFloat() / steps
                val value = minVal + (ratio * (maxVal - minVal))

                // Obtenemos la posición Y exacta para ese valor
                val yPos = getY(value)

                // Línea Horizontal
                drawLine(
                    color = gridColor,
                    start = Offset(paddingStart, yPos),
                    end = Offset(size.width, yPos),
                    strokeWidth = 1.dp.toPx(),
                    pathEffect = pathEffect
                )

                // Etiqueta Y
                val textResult = textMeasurer.measure(
                    text = androidx.compose.ui.text.AnnotatedString(value.toInt().toString()),
                    style = labelStyle.copy(color = labelColor)
                )
                drawText(
                    textLayoutResult = textResult,
                    topLeft = Offset(
                        x = paddingStart - textResult.size.width - 8.dp.toPx(),
                        y = yPos - (textResult.size.height / 2)
                    )
                )
            }
        }

        // 3. Calcular Puntos
        val path = androidx.compose.ui.graphics.Path()
        val fillPath = androidx.compose.ui.graphics.Path()
        val xStep = chartWidth / (data.size - 1).coerceAtLeast(1)
        val points = mutableListOf<Offset>()

        data.forEachIndexed { i, value ->
            val x = paddingStart + (i * xStep)
            val y = getY(value) // Usamos la misma función exacta
            points.add(Offset(x, y))

            if (i == 0) {
                path.moveTo(x, y)
                // Para el relleno, bajamos hasta el borde inferior del área de gráfico
                fillPath.moveTo(x, chartHeight + verticalPadding)
                fillPath.lineTo(x, y)
            } else {
                val prevX = points[i-1].x
                val prevY = points[i-1].y
                val controlX1 = prevX + (x - prevX) / 2
                val controlX2 = prevX + (x - prevX) / 2

                path.cubicTo(controlX1, prevY, controlX2, y, x, y)
                fillPath.cubicTo(controlX1, prevY, controlX2, y, x, y)
            }
        }

        // Cerrar relleno
        fillPath.lineTo(paddingStart + chartWidth, chartHeight + verticalPadding)
        fillPath.lineTo(paddingStart, chartHeight + verticalPadding)
        fillPath.close()

        // 4. Dibujar Relleno
        drawPath(
            path = fillPath,
            brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                colors = listOf(
                    graphColor.copy(alpha = 0.4f),
                    graphColor.copy(alpha = 0.1f),
                    androidx.compose.ui.graphics.Color.Transparent
                ),
                startY = verticalPadding,
                endY = chartHeight + verticalPadding
            )
        )

        // 5. Dibujar Línea
        drawPath(
            path = path,
            color = graphColor,
            style = androidx.compose.ui.graphics.drawscope.Stroke(
                width = 3.dp.toPx(),
                cap = androidx.compose.ui.graphics.StrokeCap.Round,
                join = androidx.compose.ui.graphics.StrokeJoin.Round
            )
        )

        // 6. Puntos (Dots)
        if (data.size < 20) {
            points.forEach { point ->
                drawCircle(
                    color = androidx.compose.ui.graphics.Color.White,
                    radius = 4.dp.toPx(),
                    center = point
                )
                drawCircle(
                    color = graphColor,
                    radius = 4.dp.toPx(),
                    center = point,
                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.dp.toPx())
                )
            }
        }

        // 7. Tooltip Interactivo
        selectedIndex?.let { index ->
            val point = points[index]
            val value = data[index]

            drawLine(
                color = labelColor.copy(alpha = 0.5f),
                start = Offset(point.x, verticalPadding),
                end = Offset(point.x, chartHeight + verticalPadding),
                strokeWidth = 1.dp.toPx(),
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(5f, 5f), 0f)
            )

            drawCircle(color = graphColor, radius = 6.dp.toPx(), center = point)
            drawCircle(color = androidx.compose.ui.graphics.Color.White, radius = 3.dp.toPx(), center = point)

            val text = "${value.toInt()}"
            val textLayout = textMeasurer.measure(
                androidx.compose.ui.text.AnnotatedString(text),
                style = labelStyle.copy(color = androidx.compose.ui.graphics.Color.White, fontWeight = FontWeight.Bold)
            )

            val boxWidth = textLayout.size.width + 16.dp.toPx()
            val boxHeight = textLayout.size.height + 8.dp.toPx()
            val boxX = (point.x - boxWidth / 2).coerceIn(0f, size.width - boxWidth)
            val boxY = (point.y - boxHeight - 10.dp.toPx()).coerceAtLeast(0f)

            drawRoundRect(
                color = graphColor,
                topLeft = Offset(boxX, boxY),
                size = androidx.compose.ui.geometry.Size(boxWidth, boxHeight),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(4.dp.toPx())
            )

            drawText(
                textLayoutResult = textLayout,
                topLeft = Offset(boxX + 8.dp.toPx(), boxY + 4.dp.toPx())
            )
        }

        // 8. Etiquetas Eje X
        if (showAxes && data.isNotEmpty()) {
            val startText = textMeasurer.measure("Inicio", style = labelStyle.copy(color = labelColor))
            val endText = textMeasurer.measure("Fin", style = labelStyle.copy(color = labelColor))

            drawText(
                textLayoutResult = startText,
                topLeft = Offset(paddingStart, size.height - paddingBottom + 4.dp.toPx())
            )
            drawText(
                textLayoutResult = endText,
                topLeft = Offset(size.width - endText.size.width, size.height - paddingBottom + 4.dp.toPx())
            )
        }
    }
}