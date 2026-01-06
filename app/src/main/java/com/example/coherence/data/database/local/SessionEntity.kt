package com.example.coherence.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.coherence.domain.model.Session

@Entity(tableName = "sessions")
data class SessionEntity(
    @PrimaryKey
    val id: String,
    val startTime: Long,
    val endTime: Long,
    val durationSeconds: Long,
    val avgCoherence: Int,

    // Guardamos las listas como Strings (JSON simple o CSV)
    val coherenceHistoryJson: String,
    val heartRateHistoryJson: String,
    val hrvHistoryJson: String,
    val temperatureHistoryJson: String,
    val conductanceHistoryJson: String
)

// --- FUNCIONES DE MAPEO ACTUALIZADAS ---

fun SessionEntity.toDomain(): Session {
    // Helpers para convertir String "1,2,3" -> List<Int/Float>
    fun stringToIntList(str: String): List<Int> {
        if (str.isEmpty()) return emptyList()
        return try {
            str.split(",").mapNotNull { it.toIntOrNull() }
        } catch (e: Exception) { emptyList() }
    }

    fun stringToFloatList(str: String): List<Float> {
        if (str.isEmpty()) return emptyList()
        return try {
            str.split(",").mapNotNull { it.toFloatOrNull() }
        } catch (e: Exception) { emptyList() }
    }

    return Session(
        id = id,
        startTime = startTime,
        endTime = endTime, // En la entidad guardamos Long, al pasar a dominio es Long? (pero aquí siempre tendrá valor)
        finalAvgCoherence = avgCoherence,
        finalDurationSeconds = durationSeconds,

        // Mapeamos las listas recuperadas
        coherenceHistory = stringToIntList(coherenceHistoryJson),
        heartRateHistory = stringToIntList(heartRateHistoryJson),
        hrvHistory = stringToIntList(hrvHistoryJson),
        temperatureHistory = stringToFloatList(temperatureHistoryJson),
        conductanceHistory = stringToFloatList(conductanceHistoryJson),

        // Dejamos dataPoints vacío por ahora, ya que la UI usa las listas individuales
        dataPoints = emptyList()
    )
}

fun Session.toEntity(): SessionEntity {
    return SessionEntity(
        id = id,
        startTime = startTime,
        // Si endTime es null (sesión activa), usamos el tiempo actual para guardar
        endTime = endTime ?: System.currentTimeMillis(),
        durationSeconds = finalDurationSeconds,
        avgCoherence = finalAvgCoherence,

        // Convertimos las listas a String separado por comas
        coherenceHistoryJson = coherenceHistory.joinToString(","),
        heartRateHistoryJson = heartRateHistory.joinToString(","),
        hrvHistoryJson = hrvHistory.joinToString(","),
        temperatureHistoryJson = temperatureHistory.joinToString(","),
        conductanceHistoryJson = conductanceHistory.joinToString(",")
    )
}