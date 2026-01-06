package com.example.coherence.domain.usecase

import com.example.coherence.domain.model.StressLevel
import kotlin.math.max
import kotlin.math.min

object BiofeedbackCalculator {

    /**
     * Calcula el puntaje de coherencia.
     *
     * @param hrv Variabilidad (ms).
     * @param gsrRaw Valor recibido del Arduino (GSR en uS multiplicado por 10).
     * @param temp Temperatura (°C).
     */
    fun calculateCoherenceScore(hrv: Float, gsrRaw: Int, temp: Float): Int {

        // --- 1. RECUPERAR EL VALOR REAL EN uS ---
        // El Arduino manda "35" para decir "3.5 uS". Lo dividimos por 10.
        // ESTE ES EL VALOR QUE MOSTRARÁS EN LA PANTALLA PRINCIPAL.
        val gsrMicroSiemens = gsrRaw / 10f

        // --- 2. NORMALIZAR HRV (Más es mejor) ---
        // 20ms = 0 pts, 100ms = 100 pts.
        val hrvScore = normalize(hrv.toFloat(), min = 20f, max = 100f)

        // --- 3. NORMALIZAR TEMPERATURA (Más es mejor) ---
        // 30°C = 0 pts, 34.5°C = 100 pts.
        val tempScore = normalize(temp, min = 30f, max = 34.5f)

        // --- 4. NORMALIZAR GSR (AQUÍ ESTÁ LA MAGIA: MENOS ES MEJOR) ---
        // Basado en tu explicación experta:
        // < 5.0 uS  = Relajación Profunda -> Queremos 100 Puntos (Verde)
        // > 25.0 uS = Estrés Elevado      -> Queremos 0 Puntos (Rojo)

        val gsrScore = normalizeInverse(gsrMicroSiemens, min = 5.0f, max = 25.0f)

        // --- 5. CÁLCULO FINAL ---
        // HRV (70%) + Temp (20%) + GSR (10%)
        val totalScore = (hrvScore * 0.7) + (tempScore * 0.2) + (gsrScore * 0.1)

        return totalScore.toInt()
    }

    fun determineStressLevel(coherenceScore: Int): StressLevel {
        return when {
            coherenceScore >= 75 -> StressLevel.LOW
            coherenceScore >= 45 -> StressLevel.MEDIUM
            else -> StressLevel.HIGH
        }
    }

    // Función normal: Valor alto = Buen puntaje (Para HRV y Temp)
    private fun normalize(value: Float, min: Float, max: Float): Float {
        val result = (value - min) / (max - min) * 100
        return max(0f, min(100f, result))
    }

    // Función INVERSA: Valor BAJO = Buen puntaje (Para GSR)
    // Si tengo 5.0 uS (min), obtengo 100 puntos.
    // Si tengo 25.0 uS (max), obtengo 0 puntos.
    private fun normalizeInverse(value: Float, min: Float, max: Float): Float {
        val result = 100 - ((value - min) / (max - min) * 100)
        return max(0f, min(100f, result))
    }
}