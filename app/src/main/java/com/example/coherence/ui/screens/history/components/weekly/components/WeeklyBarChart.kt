package com.example.coherence.ui.screens.history.components.weekly.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.coherence.domain.model.Session
import java.util.Calendar

@Composable
fun WeeklyBarChart(sessions: List<Session>) {
    val dailyAverages = remember(sessions) {
        val averages = FloatArray(7) { 0f }
        val counts = IntArray(7) { 0 }

        sessions.forEach { session ->
            val cal = Calendar.getInstance().apply { timeInMillis = session.startTime }
            val dayOfWeek = cal.get(Calendar.DAY_OF_WEEK) // Dom=1, Lun=2, ..., Sab=7

            // Convertimos a índice 0 (Lunes) - 6 (Domingo)
            val index = when (dayOfWeek) {
                Calendar.MONDAY -> 0
                Calendar.TUESDAY -> 1
                Calendar.WEDNESDAY -> 2
                Calendar.THURSDAY -> 3
                Calendar.FRIDAY -> 4
                Calendar.SATURDAY -> 5
                Calendar.SUNDAY -> 6
                else -> 0 // Fallback
            }

            averages[index] += session.finalAvgCoherence.toFloat()
            counts[index]++
        }

        val result = mutableListOf<Float>()
        for (i in 0..6) {
            if (counts[i] > 0) result.add(averages[i] / counts[i]) else result.add(0f)
        }
        result
    }

    val days = listOf("L", "M", "M", "J", "V", "S", "D")

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Rendimiento Diario", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.Bottom
            ) {
                dailyAverages.forEachIndexed { index, value ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Bottom,
                        modifier = Modifier
                            .fillMaxHeight()
                            .weight(1f)
                    ) {
                        if (value > 0) {
                            Box(
                                modifier = Modifier
                                    .width(16.dp)
                                    .weight(value / 100f)
                                    .background(
                                        color = if (value >= 80) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary,
                                        shape = RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp)
                                    )
                            )
                        } else {
                            Spacer(modifier = Modifier
                                .height(1.dp)
                                .weight(0.01f))
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(days[index], style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}