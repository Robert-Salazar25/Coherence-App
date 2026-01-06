package com.example.coherence.ui.screens.history.components.monthly.helpers

import com.example.coherence.domain.model.Session
import java.util.Calendar

// 2. Agrupa por semanas reales (basado en el día del mes del timestamp)
fun calculateRealWeeklyTrend(sessions: List<Session>): List<Float> {
    if (sessions.isEmpty()) return emptyList()

    // Creamos 4 listas vacías (una por semana)
    val weeks = MutableList(4) { mutableListOf<Int>() }

    sessions.forEach { session ->
        val cal = Calendar.getInstance().apply { timeInMillis = session.startTime }
        val day = cal.get(Calendar.DAY_OF_MONTH)

        // Determinamos a qué semana pertenece el día (0=Sem1, 1=Sem2, etc.)
        // Días 1-7 -> Sem 0, Días 8-14 -> Sem 1...
        val weekIndex = ((day - 1) / 7).coerceIn(0, 3)

        // Usamos el valor real de coherencia
        weeks[weekIndex].add(session.finalAvgCoherence)
    }

    // Calculamos el promedio de cada semana. Si no hubo sesiones, ponemos 0.
    return weeks.map { weekScores ->
        if (weekScores.isNotEmpty()) weekScores.average().toFloat() else 0f
    }
}