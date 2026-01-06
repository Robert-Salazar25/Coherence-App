package com.example.coherence.ui.screens.main.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.coherence.domain.model.StressLevel
import com.example.coherence.ui.theme.CoherenceTheme
import com.example.coherence.ui.theme.ESTRES_ALTO
import com.example.coherence.ui.theme.ESTRES_BAJO
import com.example.coherence.ui.theme.ESTRES_MEDIO

@Composable
fun StressLevelBar(
    stressLevel: StressLevel,
    coherence: Int,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {} // Nuevo parámetro para el click
) {
    // 1. CÁLCULO INVERTIDO
    val stressPercentage = (100 - coherence).coerceIn(0, 100)

    // 2. Determinamos el color y texto
    val (targetColor, statusText) = when (stressLevel) {
        StressLevel.HIGH -> ESTRES_ALTO to "ALTO"
        StressLevel.MEDIUM -> ESTRES_MEDIO to "MEDIO"
        StressLevel.LOW -> ESTRES_BAJO to "BAJO"
        StressLevel.UNKNOWN -> MaterialTheme.colorScheme.outline to "--"
    }

    // 3. Animaciones
    val animatedColor by animateColorAsState(
        targetValue = targetColor,
        animationSpec = tween(durationMillis = 500),
        label = "stressColorAnimation"
    )

    val animatedProgress by animateFloatAsState(
        targetValue = stressPercentage / 100f,
        animationSpec = tween(durationMillis = 1000),
        label = "progressAnimation"
    )

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp)) // Necesario para el efecto ripple
            .clickable { onClick() },        // Hacemos clickeable la tarjeta
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(16.dp),
        shadowElevation = 8.dp, // Elevación aumentada para estilo moderno
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Encabezado
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Título con icono de Info
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Nivel de Estrés",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Info",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                        modifier = Modifier.size(14.dp)
                    )
                }

                Text(
                    text = statusText,
                    color = animatedColor,
                    style = MaterialTheme.typography.labelLarge
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Barra de progreso
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(20.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(animatedProgress)
                        .height(20.dp)
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(
                                    animatedColor.copy(alpha = 0.6f),
                                    animatedColor
                                )
                            )
                        )
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun StressLevelBarPreview() {
    CoherenceTheme {
        Column(modifier = Modifier.padding(16.dp)) {
            StressLevelBar(stressLevel = StressLevel.MEDIUM, coherence = 50)
        }
    }
}