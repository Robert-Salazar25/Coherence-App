package com.example.coherence.domain.model

enum class StressLevel {
    LOW, MEDIUM, HIGH, UNKNOWN;

    companion object {
        fun fromCoherence(score: Float): StressLevel {
            return when {
                score >= 70f -> LOW
                score >= 40f -> MEDIUM
                score > 0f -> HIGH
                else -> UNKNOWN
            }
        }
    }
}