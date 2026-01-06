package com.example.coherence.domain.model

import java.util.UUID
import kotlin.math.roundToInt

/**
 * Representa un punto de medición en el tiempo (un "frame" del sensor).
 */
data class BiofeedbackData(
    val timestamp: Long = System.currentTimeMillis(),
    val heartRate: Int = 0,
    val spo2: Int = 0,
    val temperature: Float = 0f,
    val heartRateVariability: Float = 0f, // HRV en ms
    val breathingRate: Int = 0,
    val coherence: Float = 0f,         // 0-100%
    val isStable: Boolean = false,
    val touchValue: Int = 0,
    val stressLevel: StressLevel = StressLevel.fromCoherence(coherence)
) {
    fun isValid(): Boolean {
        return isStable &&
                touchValue < 70 &&
                heartRate in Ranges.MIN_HR..Ranges.MAX_HR &&
                temperature in Ranges.MIN_TEMP..Ranges.MAX_TEMP
    }

    object Ranges {
        const val MIN_HR = 30
        const val MAX_HR = 220
        const val MIN_TEMP = 15f
        const val MAX_TEMP = 45f
    }
}
