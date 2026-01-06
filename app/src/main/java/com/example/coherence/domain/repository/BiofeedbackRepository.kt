package com.example.coherence.domain.repository

import com.example.coherence.domain.model.BiofeedbackData
import com.example.coherence.domain.model.Session
import kotlinx.coroutines.flow.Flow

interface BiofeedbackRepository {
    val biofeedbackData: Flow<BiofeedbackData>
    val connectionState: Flow<ConnectionState>
    val batteryLevel: Flow<Int>
    val isSessionRecording: Flow<Boolean>
    val allSessions: Flow<List<Session>> // Asegúrate de tener esto también

    suspend fun connectToDevice(deviceAddress: String?): Result<Unit>
    suspend fun disconnect()
    suspend fun calibrateSensors(): Result<Unit>

    suspend fun startSession()
    suspend fun stopSession(): Result<Session>

    suspend fun saveSession(session: Session): Result<Unit>
    suspend fun getSessionById(id: String): Session?
    suspend fun deleteSession(id: String): Result<Unit>

    // --- AGREGAR ESTA LÍNEA ---
    suspend fun clearAllSessions(): Result<Unit>
    // Para el Modo Oscuro
    suspend fun saveDarkMode(enabled: Boolean)
    fun getDarkMode(): Flow<Boolean>

    // Para los Sonidos
    suspend fun saveSoundPreference(enabled: Boolean)
    fun getSoundPreference(): Flow<Boolean>

    // Para el Recordatorio
    suspend fun saveReminderSettings(enabled: Boolean, hour: Int, minute: Int)
    fun getReminderSettings(): Flow<Triple<Boolean, Int, Int>> // Devuelve (Activado, Hora, Minuto)

    // Para Notificaciones Generales
    suspend fun saveNotificationsEnabled(enabled: Boolean)
    fun getNotificationsEnabled(): Flow<Boolean>

    // Para Duración de Sesión
    suspend fun saveSessionDuration(minutes: Int)
    fun getSessionDuration(): Flow<Int>

    suspend fun deleteSession(session: Session)

    suspend fun startScanning()

}