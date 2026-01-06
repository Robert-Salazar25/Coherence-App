package com.example.coherence.ui.screens.main.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.Stop // Usamos Stop en vez de Pause
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.coherence.ui.theme.CoherenceTheme

@Composable
fun ControlButtons(
    sessionActive: Boolean,
    isLoading: Boolean = false, // Nuevo parámetro crítico
    onToggleSession: () -> Unit,
    onStartExercise: () -> Unit,
    modifier: Modifier = Modifier
) {
    // 1. Animaciones de color
    val containerColor by animateColorAsState(
        targetValue = if (sessionActive) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.primary,
        animationSpec = tween(durationMillis = 400),
        label = "sessionButtonContainerColor"
    )
    val contentColor by animateColorAsState(
        targetValue = if (sessionActive) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onPrimary,
        animationSpec = tween(durationMillis = 400),
        label = "sessionButtonContentColor"
    )

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 2. Botón de Ejercicio
        FilledTonalButton(
            onClick = onStartExercise,
            enabled = !isLoading && !sessionActive, // Deshabilitar si estamos grabando o cargando
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(12.dp),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 3.dp)
        ) {
            Icon(
                imageVector = Icons.Default.SelfImprovement,
                contentDescription = "Ejercicio"
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("EJERCICIO")
        }

        // 3. Botón de Sesión (Iniciar / Detener)
        Button(
            onClick = onToggleSession,
            enabled = !isLoading, // Evitar doble click mientras guarda
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(12.dp),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = containerColor,
                contentColor = contentColor,
                disabledContainerColor = containerColor.copy(alpha = 0.6f)
            )
        ) {
            if (isLoading) {
                // Mostrar spinner si está guardando/conectando
                CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    strokeWidth = 2.dp,
                    color = contentColor
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("PROCESANDO")
            } else {
                Icon(
                    imageVector = if (sessionActive) Icons.Default.Stop else Icons.Default.PlayArrow,
                    contentDescription = if (sessionActive) "Detener" else "Iniciar"
                )
                Spacer(modifier = Modifier.width(8.dp))
                // Usamos "DETENER" porque la acción real es guardar y finalizar
                Text(if (sessionActive) "DETENER" else "INICIAR")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ControlButtonPreview() {
    CoherenceTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Inactiva:")
            ControlButtons(
                sessionActive = false,
                isLoading = false,
                onToggleSession = {},
                onStartExercise = {}
            )

            Text("Activa:")
            ControlButtons(
                sessionActive = true,
                isLoading = false,
                onToggleSession = {},
                onStartExercise = {}
            )

            Text("Cargando (Guardando):")
            ControlButtons(
                sessionActive = true,
                isLoading = true,
                onToggleSession = {},
                onStartExercise = {}
            )
        }
    }
}