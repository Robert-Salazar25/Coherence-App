package com.example.coherence.ui.screens.history.components.weekly

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.coherence.domain.model.Session
import com.example.coherence.ui.screens.history.components.DateSelector
import com.example.coherence.ui.screens.history.components.weekly.components.CoherenceTrendChart
import com.example.coherence.ui.screens.history.components.SessionHistoryCard
import com.example.coherence.ui.screens.history.components.weekly.components.SummaryStatCard
import com.example.coherence.ui.screens.history.components.weekly.components.WeeklyBarChart
import com.example.coherence.ui.screens.history.components.weekly.helpers.getStartOfWeek
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun WeeklyHistoryView(
    sessions: List<Session>,
    onSessionClick: (String) -> Unit,
    onDeleteSession: (Session) -> Unit // <--- 1. Nuevo parámetro para manejar el borrado
) {
    if (sessions.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No hay historial disponible", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        return
    }

    // 1. Agrupar sesiones por Semana
    val sessionsByWeek = remember(sessions) {
        sessions.groupBy { session ->
            getStartOfWeek(session.startTime)
        }.toSortedMap(compareByDescending { it })
    }

    val availableWeeks = sessionsByWeek.keys.toList()
    if (availableWeeks.isEmpty()) return

    // Estado para la semana seleccionada
    var selectedWeekIndex by remember { mutableIntStateOf(0) }

    // Datos de la semana actual
    val currentWeekStart = availableWeeks[selectedWeekIndex]
    val currentSessions = sessionsByWeek[currentWeekStart] ?: emptyList()

    // Etiqueta de la semana
    val weekLabel = remember(currentWeekStart) {
        val start = Date(currentWeekStart)
        val end = Date(currentWeekStart + (6 * 24 * 60 * 60 * 1000)) // +6 días
        val fmt = SimpleDateFormat("dd MMM", Locale("es", "ES"))
        "${fmt.format(start)} - ${fmt.format(end)}"
    }

    // Cálculos para KPIs
    val avgCoherence = if (currentSessions.isNotEmpty())
        currentSessions.map { it.finalAvgCoherence }.average().toInt()
    else 0

    val totalMinutes = if (currentSessions.isNotEmpty())
        currentSessions.sumOf { it.finalDurationSeconds } / 60
    else 0

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        // 1. Selector de Semana
        item {
            DateSelector(
                label = weekLabel,
                onPrevClick = { if (selectedWeekIndex < availableWeeks.lastIndex) selectedWeekIndex++ },
                onNextClick = { if (selectedWeekIndex > 0) selectedWeekIndex-- },
                canGoPrev = selectedWeekIndex < availableWeeks.lastIndex,
                canGoNext = selectedWeekIndex > 0
            )
        }

        // 2. TARJETAS DE RESUMEN
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                SummaryStatCard(
                    title = "Coherencia Prom.",
                    value = "$avgCoherence%",
                    modifier = Modifier.weight(1f)
                )
                SummaryStatCard(
                    title = "Tiempo Total",
                    value = "${totalMinutes}m",
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // 3. GRÁFICA DE TENDENCIA
        item {
            CoherenceTrendChart(
                sessions = currentSessions,
                modifier = Modifier.fillMaxWidth(),
                title = "Tendencia Semanal"
            )
        }

        // 4. Gráfica de Barras
        item {
            WeeklyBarChart(sessions = currentSessions)
        }

        // Lista de Sessions
        item {
            Text(
                text = "Sesiones de la semana",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
            )
        }

        // Ordenamos por fecha descendente
        items(currentSessions.sortedByDescending { it.startTime }) { session ->
            SessionHistoryCard(
                session = session,
                onClick = { onSessionClick(session.id) },
                onDelete = { onDeleteSession(session) } // <--- 2. Pasamos la acción de borrar a la tarjeta
            )
        }

        item { Spacer(modifier = Modifier.height(30.dp)) }
    }
}