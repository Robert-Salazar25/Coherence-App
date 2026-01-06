package com.example.coherence.domain.model

import kotlin.math.roundToInt

/**
 * Representa una sesión completa de ejercicio.
 */
data class Session(
    val id: String,
    val startTime: Long,
    val endTime: Long,
    val finalAvgCoherence: Int,
    val finalDurationSeconds: Long,

    // Historiales de datos (Listas de valores minuto a minuto o segundo a segundo)
    val coherenceHistory: List<Int>,
    val heartRateHistory: List<Int> = emptyList(),      // <--- NUEVO
    val hrvHistory: List<Int> = emptyList(),            // <--- NUEVO
    val temperatureHistory: List<Float> = emptyList(),  // <--- NUEVO
    val conductanceHistory: List<Float> = emptyList(),  // <--- NUEVO

    val dataPoints: List<SessionDataPoint> = emptyList()// Si usas esto, ignora las listas individuales
) {
    // --- Propiedades Calculadas ---

    val isActive: Boolean
        get() = endTime == null

    val duration: Long
        get() {
            val end = endTime ?: System.currentTimeMillis()
            return (end - startTime) / 1000
        }

    val currentAverageCoherence: Int
        get() = if (dataPoints.isNotEmpty()) {
            dataPoints.map { it.coherence }.average().roundToInt()
        } else 0

    val maxHeartRate: Int
        get() = dataPoints.maxOfOrNull { it.heartRate } ?: 0

    val minHeartRate: Int
        get() = dataPoints.minOfOrNull { it.heartRate } ?: 0
}