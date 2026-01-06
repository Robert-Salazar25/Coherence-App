package com.example.coherence.ui.screens.main.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.coherence.ui.theme.CoherenceTheme
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun CoherenceCard(
    coherence: Int,
    sessionActive: Boolean,
    isStable: Boolean,
    remainingTime: String? = null,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    // 1. Animación suave del valor numérico
    val animatedCoherence by animateFloatAsState(
        targetValue = coherence.toFloat(),
        animationSpec = tween(1000, easing = FastOutSlowInEasing),
        label = "coherenceValue"
    )

    // 2. Animaciones infinitas (rotación y respiración)
    val infiniteTransition = rememberInfiniteTransition(label = "breathing")

    val particleRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(15000, easing = LinearEasing)
        ),
        label = "particleRotation"
    )

    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 0.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow"
    )

    // 3. Lógica de Color Dinámico
    // Mantenemos el color amarillo si no es estable para dar feedback visual sutil,
    // pero el texto dirá el tiempo.
    val targetColor = when {
        sessionActive && !isStable -> Color(0xFFFFC107) // Amarillo (Estabilizando)
        coherence >= 70 -> Color(0xFF00E676) // Verde (Alto)
        coherence >= 40 -> Color(0xFFFFC400) // Naranja/Ámbar (Medio)
        else -> Color(0xFFFF3D00)            // Rojo (Bajo)
    }

    val animatedColor by animateColorAsState(
        targetValue = targetColor,
        animationSpec = tween(1000),
        label = "colorChange"
    )

    // --- CONTENEDOR PRINCIPAL ---
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        // Fondo circular sutil + CLICKABLE
        Surface(
            modifier = Modifier
                .fillMaxSize(0.9f)
                .clip(CircleShape)
                .clickable { onClick() },
            color = MaterialTheme.colorScheme.surfaceContainerLow.copy(alpha = 0.5f),
            tonalElevation = 2.dp
        ) {}

        val colorCircle = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)

        // --- CANVAS DE DIBUJO ---
        Canvas(modifier = Modifier.fillMaxSize().padding(24.dp)) {
            val canvasSize = size.minDimension
            val strokeWidth = canvasSize * 0.07f
            val radius = (canvasSize - strokeWidth) / 2
            val centerOffset = Offset(size.width / 2, size.height / 2)

            // A. Fondo del anillo
            drawCircle(
                color = colorCircle,
                radius = radius,
                style = Stroke(width = strokeWidth)
            )

            // B. Efecto Glow
            if (sessionActive) {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(animatedColor.copy(alpha = glowAlpha), Color.Transparent),
                        center = centerOffset,
                        radius = radius * 1.1f
                    ),
                    radius = radius * 1.1f,
                )
            }

            // C. Arco de Progreso
            val sweepAngle = (animatedCoherence / 100f) * 360f

            rotate(degrees = -90f) {
                drawArc(
                    brush = Brush.sweepGradient(
                        colors = listOf(
                            animatedColor.copy(alpha = 0.0f),
                            animatedColor.copy(alpha = 0.5f),
                            animatedColor
                        ),
                        center = centerOffset
                    ),
                    startAngle = 0f,
                    sweepAngle = sweepAngle,
                    useCenter = false,
                    topLeft = Offset(centerOffset.x - radius, centerOffset.y - radius),
                    size = Size(radius * 2, radius * 2),
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
            }

            // D. Indicador
            val angleInRadians = (sweepAngle - 90) * (Math.PI / 180.0)
            val dotX = (centerOffset.x + radius * cos(angleInRadians)).toFloat()
            val dotY = (centerOffset.y + radius * sin(angleInRadians)).toFloat()

            drawCircle(
                color = animatedColor.copy(alpha = 0.5f),
                radius = strokeWidth * 0.6f,
                center = Offset(dotX, dotY)
            )
            drawCircle(
                color = Color.White,
                radius = strokeWidth * 0.3f,
                center = Offset(dotX, dotY)
            )

            // E. Partículas
            if (sessionActive) {
                rotate(degrees = particleRotation) {
                    drawCircle(
                        color = animatedColor.copy(alpha = 0.4f),
                        radius = 4.dp.toPx(),
                        center = Offset(centerOffset.x, centerOffset.y - radius * 1.25f)
                    )
                    drawCircle(
                        color = animatedColor.copy(alpha = 0.2f),
                        radius = 3.dp.toPx(),
                        center = Offset(centerOffset.x, centerOffset.y + radius * 1.25f)
                    )
                }
            }
        }

        // --- TEXTO CENTRAL ---
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "COHERENCIA",
                    style = MaterialTheme.typography.labelSmall.copy(
                        letterSpacing = 3.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = "Info",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                    modifier = Modifier.size(12.dp)
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // NÚMERO GIGANTE
            Text(
                text = "${animatedCoherence.toInt()}%",
                style = MaterialTheme.typography.displayLarge.copy(
                    fontWeight = FontWeight.Thin,
                    fontSize = 72.sp,
                    letterSpacing = (-2).sp
                ),
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(8.dp))

            // PÍLDORA DE ESTADO
            Surface(
                color = if (sessionActive) animatedColor.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surfaceVariant,
                shape = CircleShape
            ) {
                // --- CAMBIO AQUÍ ---
                // Prioridad absoluta al tiempo si la sesión está activa.
                // Eliminamos la condición de !isStable para el texto.
                val statusText = when {
                    !sessionActive -> "En espera"
                    remainingTime != null -> "Tiempo: $remainingTime" // <--- AHORA ESTO TIENE PRIORIDAD
                    else -> "Sincronizando"
                }

                Text(
                    text = statusText,
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = if (remainingTime != null) FontWeight.Bold else FontWeight.Normal,
                        fontFeatureSettings = "tnum" // Números monoespaciados para que no bailen
                    ),
                    color = if (sessionActive) animatedColor else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CoherenceCardPreview() {
    CoherenceTheme {
        Column(Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(24.dp)) {
            // Caso 1: Funcionando normal
            CoherenceCard(
                coherence = 88,
                sessionActive = true,
                isStable = true,
                remainingTime = "04:59",
                modifier = Modifier.size(300.dp)
            )

            // Caso 2: Estabilizando (Color Amarillo) PERO mostrando el tiempo
            CoherenceCard(
                coherence = 0,
                sessionActive = true,
                isStable = false, // Esto pondrá el color amarillo
                remainingTime = "05:00", // Esto pondrá el texto del tiempo
                modifier = Modifier.size(300.dp)
            )
        }
    }
}