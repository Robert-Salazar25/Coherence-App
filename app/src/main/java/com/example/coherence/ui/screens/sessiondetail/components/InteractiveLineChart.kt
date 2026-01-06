package com.example.coherence.ui.screens.sessiondetail.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.util.Locale
import kotlin.math.roundToInt

// --- GRÁFICA INTERACTIVA CON DECIMALES ---
@Composable
fun InteractiveLineChart(
    data: List<Number>,
    lineColor: Color,
    modifier: Modifier = Modifier,
    minY: Float? = null,
    maxY: Float? = null,
    labelSuffix: String = ""
) {
    if (data.isEmpty()) {
        Box(modifier = modifier, contentAlignment = Alignment.Center) {
            Text("Sin datos", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        return
    }

    var cursorX by remember { mutableStateOf<Float?>(null) }
    var selectedValue by remember { mutableStateOf<Number?>(null) }

    val floatData = remember(data) { data.map { it.toFloat() } }
    val actualMinY = minY ?: floatData.minOrNull() ?: 0f
    val actualMaxY = maxY ?: floatData.maxOrNull() ?: 100f
    val rangeY = (actualMaxY - actualMinY).coerceAtLeast(1f)

    val surfaceVariantColor = MaterialTheme.colorScheme.surfaceVariant

    Box(modifier = modifier) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTapGestures(
                        onPress = { offset ->
                            cursorX = offset.x
                            tryAwaitRelease()
                            cursorX = null
                            selectedValue = null
                        }
                    )
                }
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = { offset -> cursorX = offset.x },
                        onDrag = { change, _ ->
                            change.consume()
                            cursorX = change.position.x
                        },
                        onDragEnd = { cursorX = null; selectedValue = null },
                        onDragCancel = { cursorX = null; selectedValue = null }
                    )
                }
        ) {
            val width = size.width
            val height = size.height
            val stepX = width / (data.size - 1).coerceAtLeast(1)

            // 1. Grid (Sutil)
            val gridLines = 4
            for (i in 0..gridLines) {
                val y = height - (i * height / gridLines)
                drawLine(
                    color = surfaceVariantColor.copy(alpha = 0.2f),
                    start = Offset(0f, y),
                    end = Offset(width, y),
                    strokeWidth = 1.dp.toPx()
                )
            }

            // 2. Path de la gráfica
            val path = Path()
            floatData.forEachIndexed { index, value ->
                val x = index * stepX
                val normalizedY = (value - actualMinY) / rangeY
                val y = height - (normalizedY * height)
                if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
            }

            // 3. Relleno (Gradiente del color de la gráfica)
            val fillPath = Path().apply {
                addPath(path)
                lineTo(width, height)
                lineTo(0f, height)
                close()
            }
            drawPath(
                path = fillPath,
                brush = Brush.verticalGradient(
                    colors = listOf(lineColor.copy(alpha = 0.3f), Color.Transparent),
                    startY = 0f,
                    endY = height
                )
            )

            // 4. Línea Principal
            drawPath(
                path = path,
                color = lineColor,
                style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
            )

            // 5. CURSOR PERSONALIZADO
            cursorX?.let { cx ->
                val constrainedX = cx.coerceIn(0f, width)
                val index = (constrainedX / stepX).roundToInt().coerceIn(0, data.size - 1)
                selectedValue = data[index]

                val pointX = index * stepX
                val value = floatData[index]
                val normalizedY = (value - actualMinY) / rangeY
                val pointY = height - (normalizedY * height)

                // A. Línea Vertical (Del color de la gráfica)
                drawLine(
                    color = lineColor.copy(alpha = 0.8f),
                    start = Offset(pointX, 0f),
                    end = Offset(pointX, height),
                    strokeWidth = 2.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(15f, 15f))
                )

                // B. Punto "Donut"
                drawCircle(
                    color = Color.White,
                    radius = 7.dp.toPx(),
                    center = Offset(pointX, pointY)
                )
                drawCircle(
                    color = lineColor,
                    radius = 7.dp.toPx(),
                    center = Offset(pointX, pointY),
                    style = Stroke(width = 3.dp.toPx())
                )
            }
        }

        // 6. ETIQUETA FLOTANTE (Píldora del color de la gráfica)
        if (selectedValue != null) {
            Surface(
                color = lineColor,
                contentColor = Color.White,
                shape = RoundedCornerShape(50),
                shadowElevation = 6.dp,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 0.dp)
            ) {
                Box(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    // CAMBIO AQUÍ: Formateo a 2 decimales
                    Text(
                        text = String.format(Locale.US, "%.2f%s", selectedValue?.toFloat(), labelSuffix),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}