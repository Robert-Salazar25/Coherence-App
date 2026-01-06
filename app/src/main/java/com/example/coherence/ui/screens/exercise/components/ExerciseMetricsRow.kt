package com.example.coherence.ui.screens.exercise.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Waves
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun ExerciseMetricsRow(heartRate: Int, spo2: Int, coherence: Int) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Row(
            Modifier.padding(16.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            MetricItem(Icons.Default.Favorite, "$heartRate", "BPM", Color(0xFFE57373))
            MetricItem(Icons.Default.Air, "$spo2", "SpO2", if (spo2 >= 95) Color(0xFF81C784) else Color(0xFFFFB74D))
            MetricItem(Icons.Default.Waves, "$coherence", "Coherencia", MaterialTheme.colorScheme.primary)
        }
    }
}