package com.example.coherence.domain.usecase

import com.example.coherence.domain.model.BiofeedbackData

class CalculateCoherenceUseCase {

    /**
     * Calcula un puntaje de coherencia (0-100) basado en los datos fisiológicos.
     * Se llama desde el Repositorio cada vez que llega un dato nuevo.
     */
    operator fun invoke(data: BiofeedbackData): Float {
        // 1. Validación de seguridad:
        // Si el ritmo cardíaco es 0 o el sensor Touch está en 0 (o muy alto, indicando desconexión),
        // retornamos 0 para no afectar falsamente el promedio.
        if (data.heartRate <= 0) return 0f

        // En ESP32, si touchValue es muy alto (>80), suele significar que no se está tocando.
        // Si no hay contacto, la coherencia no debería calcularse como alta.
        if (data.touchValue > 90) return 0f

        // 2. Calculamos puntajes individuales
        val hrScore = calculateHRScore(data.heartRate)
        val tempScore = calculateTempScore(data.temperature)
        val hrvScore = calculateHRVScore(data.heartRateVariability)
        val touchScore = calculateTouchScore(data.touchValue)

        // 3. Promedio ponderado
        val totalScore = (hrScore * Weights.HR) +
                (hrvScore * Weights.HRV) +
                (touchScore * Weights.TOUCH) +
                (tempScore * Weights.TEMP)

        // Aseguramos que el resultado esté siempre entre 0 y 100
        return totalScore.coerceIn(0f, 100f)
    }

    // --- Lógica de Puntuación ---

    private fun calculateHRScore(hr: Int): Float {
        return when (hr) {
            in Thresholds.HR_OPTIMAL -> 100f
            in Thresholds.HR_NORMAL -> 80f
            in Thresholds.HR_ACCEPTABLE -> 60f
            else -> 40f
        }
    }

    /**
     * Calcula la calidad del contacto/relajación basado en el pin Touch del ESP32.
     * NOTA: En ESP32, el valor BAJA cuando tocas el pin.
     */
    private fun calculateTouchScore(touch: Int): Float {
        return when {
            // Rango ideal: Contacto firme (20-45)
            touch in Thresholds.TOUCH_OPTIMAL -> 100f

            // Rango aceptable: Contacto detectado (15-60)
            touch in Thresholds.TOUCH_NORMAL -> 80f

            // Valor muy bajo (<15): Mucha presión o humedad
            touch < Thresholds.TOUCH_NORMAL.first -> 60f

            // Valor alto (>60): Probablemente mal contacto o aire
            else -> 20f
        }
    }

    private fun calculateTempScore(temp: Float): Float {
        return when {
            temp >= Thresholds.TEMP_OPTIMAL_MIN -> 100f
            temp >= Thresholds.TEMP_NORMAL_MIN -> 80f
            else -> 50f
        }
    }

    private fun calculateHRVScore(hrv: Float): Float {
        return when {
            hrv >= Thresholds.HRV_OPTIMAL_MIN -> 100f
            hrv >= Thresholds.HRV_NORMAL_MIN -> 80f
            hrv >= Thresholds.HRV_ACCEPTABLE_MIN -> 60f
            else -> 30f
        }
    }

    // --- Configuración de Pesos y Umbrales ---
    companion object {
        object Weights {
            const val HRV = 0.5f   // 50% - La variabilidad es el indicador clave de coherencia
            const val HR = 0.2f    // 20%
            const val TOUCH = 0.2f // 20% - Indica relajación muscular/sudoración (GSR)
            const val TEMP = 0.1f  // 10% - Temperatura periférica
        }

        object Thresholds {
            // Heart Rate (Latidos por minuto)
            val HR_OPTIMAL = 55..75
            val HR_NORMAL = 50..85
            val HR_ACCEPTABLE = 45..100

            // ESP32 TOUCH (Valor crudo del sensor capacitivo)
            val TOUCH_OPTIMAL = 20..45
            val TOUCH_NORMAL = 15..60

            // Temperatura (Grados Celsius)
            const val TEMP_OPTIMAL_MIN = 34f
            const val TEMP_NORMAL_MIN = 30f

            // HRV (Milisegundos o unidad relativa)
            const val HRV_OPTIMAL_MIN = 60
            const val HRV_NORMAL_MIN = 40
            const val HRV_ACCEPTABLE_MIN = 20
        }
    }
}