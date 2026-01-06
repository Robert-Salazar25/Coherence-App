package com.example.coherence.ui.screens.exercise.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun BreathingAnimation(
    isRunning: Boolean,
    currentPhase: BreathingPhase,
    phaseDuration: Long, // Necesitamos la duración total de la fase
    phaseTimeLeft: Int,
    modifier: Modifier = Modifier
) {
    // 1. Usamos Animatable para un control manual y preciso de la animación.
    val scale = remember { androidx.compose.animation.core.Animatable(1f) }
    var textOffsetY by remember { mutableFloatStateOf(50f) }

    // 2. Este LaunchedEffect se ejecuta cada vez que 'currentPhase' cambia.
    LaunchedEffect(currentPhase, isRunning) {
        if(!isRunning){
            val initialScale = if(currentPhase == BreathingPhase.INHALE) 1f else 1.5f
            scale.snapTo(initialScale)
            return@LaunchedEffect
        }
        when (currentPhase) {
            BreathingPhase.INHALE -> {
                // Anima la escala desde su valor actual hasta 1.5f
                // durante la duración completa de la fase de inhalación.
                scale.animateTo(
                    targetValue = 1.5f,
                    animationSpec = tween(durationMillis = phaseDuration.toInt(), easing = LinearEasing)
                )
                textOffsetY = -50f
            }
            BreathingPhase.HOLD -> {
                // Para 'MANTÉN', simplemente nos aseguramos de que la escala se quede en 1.5f.
                // Podríamos añadir un pulso aquí si quisiéramos.
                scale.snapTo(1.5f)
                textOffsetY = -50f
            }
            BreathingPhase.EXHALE -> {
                // Anima la escala desde su valor actual de vuelta a 1f
                // durante la duración completa de la fase de exhalación.
                scale.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(durationMillis = phaseDuration.toInt(), easing = LinearEasing)
                )
                textOffsetY = 50f
            }
        }
    }

    val circleColor = MaterialTheme.colorScheme.primary
    val alpha = if (currentPhase == BreathingPhase.EXHALE) scale.value / 1.5f else 1f

    Box(
        modifier = modifier.size(300.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            // Usamos scale.value que es el valor actual del Animatable
            val radius = (size.minDimension / 3f) * scale.value
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(circleColor.copy(alpha = 0.5f * alpha), Color.Transparent),
                    center = center,
                    radius = radius
                ),
                radius = radius,
            )

            for (i in 1..3) {
                val waveRadius = radius + (i * 30.dp.toPx())
                val waveAlpha = alpha * (0.3f - i * 0.08f).coerceAtLeast(0f)
                drawCircle(
                    color = circleColor.copy(alpha = waveAlpha),
                    radius = waveRadius,
                    style = Stroke(width = 4.dp.toPx())
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .offset(y = textOffsetY.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = when (currentPhase) {
                    BreathingPhase.INHALE -> "INHALA"
                    BreathingPhase.HOLD -> "MANTÉN"
                    BreathingPhase.EXHALE -> "EXHALA"
                },
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Light,
                color = MaterialTheme.colorScheme.primary
            )
        }

        Text(
            text = "${phaseTimeLeft}s",
            style = MaterialTheme.typography.bodyLarge.copy(fontSize = 20.sp),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

enum class BreathingPhase{
    INHALE,
    EXHALE,
    HOLD
}


