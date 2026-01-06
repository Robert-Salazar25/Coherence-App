package com.example.coherence.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.coherence.domain.model.BiofeedbackData

@Entity(
    tableName = "biofeedback_data",
    foreignKeys = [
        ForeignKey(
            entity = SessionEntity::class,
            parentColumns = ["id"],
            childColumns = ["sessionId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["sessionId"])]
)
data class BiofeedbackDataEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val sessionId: String,
    val timestamp: Long,
    val heartRate: Int,
    val spo2: Int,
    val heartRateVariability: Float,
    val temperature: Float,
    val coherence: Float,
    val touchValue: Int // <--- NUEVA COLUMNA: Guardamos el valor del sensor Touch
)

// Mapper: Dominio -> Entidad BD
fun BiofeedbackData.toEntity(sessionId: String): BiofeedbackDataEntity {
    return BiofeedbackDataEntity(
        sessionId = sessionId,
        timestamp = timestamp,
        heartRate = heartRate,
        spo2 = spo2,
        heartRateVariability = heartRateVariability,
        temperature = temperature,
        coherence = coherence,
        touchValue = touchValue // <--- Mapeamos el valor del modelo a la base de datos
    )
}