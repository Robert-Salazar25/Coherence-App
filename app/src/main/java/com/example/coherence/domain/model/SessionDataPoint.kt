package com.example.coherence.domain.model

data class SessionDataPoint(
    val timestamp: Long,
    val coherence: Float,
    val heartRate: Int,
    val hrv: Float,
    val temperature: Float,
    val conductance: Float
)