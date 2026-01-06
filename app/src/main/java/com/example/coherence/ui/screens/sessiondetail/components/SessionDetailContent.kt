package com.example.coherence.ui.screens.sessiondetail.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Analytics
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material.icons.outlined.DeviceThermostat
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.MonitorHeart
import androidx.compose.material.icons.outlined.Psychology
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.coherence.domain.model.Session
import com.example.coherence.ui.screens.sessiondetail.InteractiveLineChart
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun SessionDetailContent(session: Session) {
    val stressLevel = (100 - session.finalAvgCoherence).coerceIn(0, 100)

    // --- ALGORITMO DE SUAVIZADO (Adaptado para devolver List<Float>) ---
    fun smoothData(data: List<Number>, windowSize: Int = 5): List<Float> {
        if (data.isEmpty()) return emptyList()
        if (data.size < windowSize) return data.map { it.toFloat() }

        val smoothedList = mutableListOf<Float>()
        for (i in data.indices) {
            val start = (i - windowSize / 2).coerceAtLeast(0)
            val end = (i + windowSize / 2).coerceAtMost(data.lastIndex)
            val subList = data.subList(start, end + 1)
            val average = subList.map { it.toFloat() }.average().toFloat()
            smoothedList.add(average)
        }
        return smoothedList
    }

    // Preparamos los datos suavizados
    val coherenceData = remember(session.coherenceHistory) { smoothData(session.coherenceHistory, 10) }
    val heartRateData = remember(session.heartRateHistory) { smoothData(session.heartRateHistory, 10) }
    val hrvData = remember(session.hrvHistory) { smoothData(session.hrvHistory, 8) }
    val tempData = remember(session.temperatureHistory) { smoothData(session.temperatureHistory, 20) }
    val gsrData = remember(session.conductanceHistory) { smoothData(session.conductanceHistory, 10) }

    // Promedios
    val avgHeartRate = if (session.heartRateHistory.isNotEmpty()) session.heartRateHistory.average().toFloat() else 0f
    val avgHrv = if (session.hrvHistory.isNotEmpty()) session.hrvHistory.average().toFloat() else 0f
    val avgTemp = if (session.temperatureHistory.isNotEmpty()) session.temperatureHistory.average().toFloat() else 0f
    val avgGsr = if (session.conductanceHistory.isNotEmpty()) session.conductanceHistory.average().toFloat() else 0f

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        item { SessionHeaderSummary(session) }
        item { StressLevelCard(stressLevel = stressLevel) }

        // --- GRÁFICAS INTERACTIVAS ---

        item {
            MetricChartCard(
                title = "Coherencia Cardíaca",
                data = coherenceData,
                unit = "%",
                avgValue = session.finalAvgCoherence.toFloat(),
                icon = Icons.Outlined.Analytics,
                chartColor = Color(0xFF2196F3),
                minY = 0f, maxY = 100f // Rango fijo para coherencia
            )
        }

        item {
            MetricChartCard(
                title = "Frecuencia Cardíaca",
                data = heartRateData,
                unit = " lpm",
                avgValue = avgHeartRate,
                icon = Icons.Outlined.Favorite,
                chartColor = Color(0xFFE91E63),
                minY = 40f, maxY = 120f // Rango humano típico
            )
        }

        item {
            MetricChartCard(
                title = "Variabilidad (VFC)",
                data = hrvData,
                unit = " ms",
                avgValue = avgHrv,
                icon = Icons.Outlined.MonitorHeart,
                chartColor = Color(0xFF9C27B0)
            )
        }

        item {
            MetricChartCard(
                title = "Temperatura Piel",
                data = tempData,
                unit = " °C",
                avgValue = avgTemp,
                icon = Icons.Outlined.DeviceThermostat,
                chartColor = Color(0xFFFF9800),
                minY = 30f, maxY = 40f
            )
        }

        item {
            MetricChartCard(
                title = "Conductancia (GSR)",
                data = gsrData,
                unit = " μS",
                avgValue = avgGsr,
                icon = Icons.Outlined.Bolt,
                chartColor = Color(0xFF00BCD4)
            )
        }
    }
}


// --- COMPONENTES VISUALES ---

@Composable
private fun SessionHeaderSummary(session: Session) {
    // Formateo de fecha real (Long -> String)
    val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale("es", "ES"))
    val timeFormat = SimpleDateFormat("HH:mm", Locale("es", "ES"))
    val dateStr = dateFormat.format(Date(session.startTime))
    val timeStr = timeFormat.format(Date(session.startTime))

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = dateStr,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = timeStr,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Chip de duración
        Surface(
            color = MaterialTheme.colorScheme.primaryContainer,
            shape = RoundedCornerShape(50),
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Outlined.Timer,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Spacer(modifier = Modifier.width(4.dp))

                // Formateo de duración
                val minutes = session.finalDurationSeconds / 60
                val seconds = session.finalDurationSeconds % 60
                Text(
                    text = String.format("%02d:%02d", minutes, seconds),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun StressLevelCard(stressLevel: Int) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.Psychology, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Nivel de Estrés Estimado",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = if(stressLevel < 30) "Bajo" else if(stressLevel < 70) "Moderado" else "Alto",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = if(stressLevel < 30) Color(0xFF4CAF50) else if(stressLevel < 70) Color(0xFFFFC107) else MaterialTheme.colorScheme.error
                    )
                    Text(
                        text = "$stressLevel/100",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { stressLevel / 100f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(12.dp)
                        .clip(RoundedCornerShape(50)),
                    color = if(stressLevel < 30) Color(0xFF4CAF50) else if(stressLevel < 70) Color(0xFFFFC107) else MaterialTheme.colorScheme.error,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Basado en tu coherencia cardíaca inversa.",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun MetricChartCard(
    title: String,
    data: List<Number>, // <--- CAMBIO IMPORTANTE: Recibe lista de números directa
    unit: String,
    avgValue: Float,
    icon: ImageVector,
    chartColor: Color,
    minY: Float? = null,
    maxY: Float? = null
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Encabezado
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(chartColor.copy(alpha = 0.1f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(icon, contentDescription = null, tint = chartColor, modifier = Modifier.size(20.dp))
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "${avgValue.toInt()}$unit",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = chartColor
                    )
                    Text("Promedio", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Gráfico Interactivo
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp) // Altura fija
            ) {
                InteractiveLineChart(
                    data = data,
                    lineColor = chartColor,
                    modifier = Modifier.fillMaxSize(),
                    minY = minY,
                    maxY = maxY,
                    labelSuffix = unit
                )
            }
        }
    }
}