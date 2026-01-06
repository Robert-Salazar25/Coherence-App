package com.example.coherence.ui.screens.history.components.monthly

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Analytics
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.coherence.domain.model.Session
import com.example.coherence.ui.screens.history.components.DateSelector
import com.example.coherence.ui.screens.history.components.SessionHistoryCard
import com.example.coherence.ui.screens.history.components.monthly.components.GradientLineChart
import com.example.coherence.ui.screens.history.components.monthly.components.InteractiveCalendarGrid
import com.example.coherence.ui.screens.history.components.monthly.components.MonthlyStatCard
import com.example.coherence.ui.screens.history.components.monthly.helpers.calculateRealWeeklyTrend
import com.example.coherence.ui.screens.history.components.monthly.helpers.getStartOfMonth
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@Composable
fun MonthlyHistoryView(
    sessions: List<Session>,
    onSessionClick: (String) -> Unit, // Nuevo: Para navegar al detalle
    onDeleteSession: (Session) -> Unit // Nuevo: Para borrar
) {
    if (sessions.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No hay historial disponible", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        return
    }

    // 1. Agrupar sesiones por Mes
    val sessionsByMonth = remember(sessions) {
        sessions.groupBy { session ->
            val cal = Calendar.getInstance().apply { timeInMillis = session.startTime }
            getStartOfMonth(cal)
        }.toSortedMap(compareByDescending { it })
    }

    val availableMonths = sessionsByMonth.keys.toList()
    if (availableMonths.isEmpty()) return

    // Estado del mes seleccionado
    var selectedMonthIndex by remember { mutableIntStateOf(0) }
    // Estado del día seleccionado dentro del mes (para ver detalles)
    var selectedDay by remember { mutableStateOf<Int?>(null) }

    // Validación de índice seguro
    if (selectedMonthIndex >= availableMonths.size) selectedMonthIndex = 0

    val currentMonthStart = availableMonths[selectedMonthIndex]
    // Esta lista se actualiza automáticamente si borras una sesión de la lista principal
    val currentSessions = sessionsByMonth[currentMonthStart] ?: emptyList()

    // Etiqueta del mes
    val monthLabel = remember(currentMonthStart) {
        val cal = Calendar.getInstance().apply { timeInMillis = currentMonthStart }
        SimpleDateFormat("MMMM yyyy", Locale("es", "ES")).format(cal.time).replaceFirstChar { it.uppercase() }
    }

    // --- CÁLCULOS REACTIVOS (Se actualizan al borrar) ---
    val avgCoherence = if (currentSessions.isNotEmpty())
        currentSessions.map { it.finalAvgCoherence }.average().toInt()
    else 0

    val totalHours = if (currentSessions.isNotEmpty())
        currentSessions.sumOf { it.finalDurationSeconds } / 3600f
    else 0f

    val weeklyTrend = remember(currentSessions) { calculateRealWeeklyTrend(currentSessions) }

    // Lógica de Puntos: Obtenemos los días únicos que tienen al menos una sesión.
    // Si borras una sesión pero queda otra ese día, el día sigue en este Set.
    val daysWithSessions = remember(currentSessions) {
        currentSessions.map { session ->
            val cal = Calendar.getInstance().apply { timeInMillis = session.startTime }
            cal.get(Calendar.DAY_OF_MONTH)
        }.toSet()
    }

    // Filtrar sesiones para el día seleccionado (para la lista de abajo)
    val sessionsForSelectedDay = remember(currentSessions, selectedDay) {
        if (selectedDay == null) emptyList()
        else currentSessions.filter { session ->
            val cal = Calendar.getInstance().apply { timeInMillis = session.startTime }
            cal.get(Calendar.DAY_OF_MONTH) == selectedDay
        }.sortedByDescending { it.startTime }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Selector de Mes
        DateSelector(
            label = monthLabel,
            onPrevClick = {
                if (selectedMonthIndex < availableMonths.lastIndex) {
                    selectedMonthIndex++
                    selectedDay = null // Reseteamos el día al cambiar de mes
                }
            },
            onNextClick = {
                if (selectedMonthIndex > 0) {
                    selectedMonthIndex--
                    selectedDay = null
                }
            },
            canGoPrev = selectedMonthIndex < availableMonths.lastIndex,
            canGoNext = selectedMonthIndex > 0
        )

        // 2. KPIs (Tarjetas de estadísticas)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            MonthlyStatCard(
                title = "Promedio",
                value = "$avgCoherence%",
                icon = Icons.Outlined.Analytics,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.weight(1f),
                delay = 0
            )
            MonthlyStatCard(
                title = "Horas Totales",
                value = "%.1f h".format(totalHours),
                icon = Icons.Outlined.Timer,
                color = MaterialTheme.colorScheme.tertiary,
                modifier = Modifier.weight(1f),
                delay = 100
            )
        }

        // 3. Gráfica de Tendencia
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Tendencia Semanal", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(24.dp))

                if (weeklyTrend.any { it > 0 }) {
                    GradientLineChart(
                        data = weeklyTrend,
                        graphColor = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp)
                    )
                    // Ejes X simples
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp, start = 35.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Sem 1", style = MaterialTheme.typography.labelSmall)
                        Text("Sem 2", style = MaterialTheme.typography.labelSmall)
                        Text("Sem 3", style = MaterialTheme.typography.labelSmall)
                        Text("Sem 4", style = MaterialTheme.typography.labelSmall)
                    }
                } else {
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .height(100.dp), contentAlignment = Alignment.Center) {
                        Text("Datos insuficientes", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }

        // 4. Calendario Interactivo
        // Usamos un calendario que permite seleccionar días
        InteractiveCalendarGrid(
            monthStartMillis = currentMonthStart,
            daysWithSessions = daysWithSessions,
            selectedDay = selectedDay,
            onDayClick = { day ->
                // Toggle: si tocas el mismo día, se deselecciona
                selectedDay = if (selectedDay == day) null else day
            }
        )

        // 5. Lista de Sesiones del Día Seleccionado
        if (selectedDay != null) {
            Text(
                text = "Sesiones del día $selectedDay",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 8.dp)
            )

            if (sessionsForSelectedDay.isEmpty()) {
                Text(
                    "No hay sesiones este día.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                sessionsForSelectedDay.forEach { session ->
                    SessionHistoryCard(
                        session = session,
                        onClick = { onSessionClick(session.id) },
                        onDelete = { onDeleteSession(session) } // Conectamos el borrado
                    )
                }
            }
        } else {
            // Mensaje guía
            Box(
                modifier = Modifier.fillMaxWidth().padding(vertical = 20.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "Selecciona un día marcado para ver o eliminar sesiones",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(30.dp))
    }
}