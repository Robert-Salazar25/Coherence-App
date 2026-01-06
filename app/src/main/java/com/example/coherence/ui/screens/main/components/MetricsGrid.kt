package com.example.coherence.ui.screens.main.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.coherence.ui.theme.*

// Clase de datos para manejar la info del diálogo
data class MetricInfo(
    val title: String,
    val description: String,
    val range: String,
    val icon: ImageVector,
    val color: Color
)

@Composable
fun MetricsGrid(
    heartRate: Int,
    spo2: Int,
    touchValue: Float,
    temperature: Float,
    heartRateVariability: Float,
    modifier: Modifier = Modifier
){
    // Estado para controlar qué diálogo se muestra
    var selectedMetric by remember { mutableStateOf<MetricInfo?>(null) }

    if (selectedMetric != null) {
        MetricInfoDialog(
            title = selectedMetric!!.title,
            description = selectedMetric!!.description,
            optimalRange = selectedMetric!!.range,
            icon = selectedMetric!!.icon,
            iconColor = selectedMetric!!.color,
            onDismiss = { selectedMetric = null }
        )
    }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // --- FILA SUPERIOR ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                // 1. RITMO CARDÍACO
                Metricard(
                    icon = Icons.Default.Favorite,
                    iconColor = HEART_RATE_COLOR,
                    label = "Frecuencia Cardiaca",
                    value = "$heartRate",
                    unit = "lpm",
                    onClick = {
                        selectedMetric = MetricInfo(
                            "Frecuencia Cardíaca",
                            "Es la cantidad de veces que tu corazón late por minuto. Refleja tu estado de activación física y emocional.",
                            "Reposo: 60 - 100 lpm\nAtletas: 40 - 60 lpm",
                            Icons.Default.Favorite,
                            HEART_RATE_COLOR
                        )
                    }
                )

                // 2. TEMPERATURA
                Metricard(
                    icon = Icons.Default.Thermostat,
                    iconColor = TEMPERATURE_COLOR,
                    label = "Temperatura",
                    value = String.format("%.1f", temperature),
                    unit = "°C",
                    onClick = {
                        selectedMetric = MetricInfo(
                            "Temperatura Periférica",
                            "La temperatura de la piel en los dedos. Cuando te relajas, los vasos sanguíneos se dilatan y la temperatura sube (manos calientes).",
                            "Relajado: > 30°C\nEstresado: < 26°C (Manos frías)",
                            Icons.Default.Thermostat,
                            TEMPERATURE_COLOR
                        )
                    }
                )
            }

            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                // 3. SPO2
                Metricard(
                    icon = Icons.Default.Air,
                    iconColor = Color(0xFF03A9F4),
                    label = "Oxígeno (SpO2)",
                    value = "$spo2",
                    unit = "%",
                    onClick = {
                        selectedMetric = MetricInfo(
                            "Saturación de Oxígeno",
                            "Indica qué porcentaje de tu sangre está transportando oxígeno. Es vital para la salud celular.",
                            "Normal: 95% - 100%\nBajo: < 94% (Consultar médico)",
                            Icons.Default.Air,
                            Color(0xFF03A9F4)
                        )
                    }
                )

                // 4. HRV
                Metricard(
                    icon = Icons.Default.Timeline,
                    iconColor = HRV_COLOR,
                    label = "Variabilidad (HRV)",
                    value = "$heartRateVariability",
                    unit = "ms",
                    onClick = {
                        selectedMetric = MetricInfo(
                            "Variabilidad Cardíaca (HRV)",
                            "Mide la variación de tiempo entre latidos. Una HRV alta indica un sistema nervioso flexible y saludable (buena adaptación al estrés).",
                            "Alta (Bueno): > 50ms\nBaja (Estrés): < 30ms",
                            Icons.Default.Timeline,
                            HRV_COLOR
                        )
                    }
                )
            }
        }

        // --- FILA INFERIOR ---
        // 5. CONDUCTANCIA
        Metricard(
            icon = Icons.Default.TouchApp,
            iconColor = GSR_COLOR,
            label = "Conductancia Piel",
            value = "$touchValue",
            unit = "Cap",
            modifier = Modifier.fillMaxWidth(),
            onClick = {
                selectedMetric = MetricInfo(
                    "Conductancia de la Piel",
                    "Mide la actividad eléctrica de la piel, que cambia con la humedad (sudor). Refleja la activación del sistema nervioso simpático (estrés).",
                    "Relajado: Valores bajos y estables\nEstresado: Picos repentinos",
                    Icons.Default.TouchApp,
                    GSR_COLOR
                )
            }
        )
    }
}

@Composable
fun Metricard(
    icon: ImageVector,
    iconColor: Color,
    label: String,
    value: String,
    unit: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit // Nuevo parámetro
){
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp)) // Necesario para el efecto ripple
            .clickable { onClick() }, // Hacemos clickeable la tarjeta
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(16.dp),
        shadowElevation = 8.dp,

    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = iconColor,
                modifier = Modifier.size(28.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = label,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.labelMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(4.dp))

            Row (
                verticalAlignment = Alignment.Bottom
            ){
                Text(
                    text = value,
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.headlineSmall
                )
                Text(
                    text = unit,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
                )
            }
        }
    }
}