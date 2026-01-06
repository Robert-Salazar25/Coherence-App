package com.example.coherence.data.repository

import android.content.Context
import androidx.compose.ui.geometry.isEmpty
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.coherence.data.ble.BiofeedbackBleManager
import com.example.coherence.data.database.AppDatabase
import com.example.coherence.data.local.toDomain
import com.example.coherence.data.local.toEntity
import com.example.coherence.domain.model.BiofeedbackData
import com.example.coherence.domain.model.Session
import com.example.coherence.domain.repository.BiofeedbackRepository
import com.example.coherence.domain.repository.ConnectionState
import com.example.coherence.domain.usecase.CalculateCoherenceUseCase
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import java.util.UUID
import javax.inject.Inject
import kotlin.math.roundToInt

// Extensión para crear el DataStore (Singleton)
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class BiofeedbackRepositoryImpl @Inject constructor(
    private val bleManager: BiofeedbackBleManager,
    private val db: AppDatabase,
    @ApplicationContext private val context: Context
) : BiofeedbackRepository {

    private val dao = db.sessionDao()
    private val dataStore = context.dataStore

    // Instanciamos el caso de uso para calcular la coherencia real
    private val calculateCoherence = CalculateCoherenceUseCase()

    // Claves para DataStore
    private object PreferencesKeys {
        val DARK_MODE = booleanPreferencesKey("dark_mode")
        val SOUND_ENABLED = booleanPreferencesKey("sound_enabled")
        val REMINDER_ENABLED = booleanPreferencesKey("reminder_enabled")
        val REMINDER_HOUR = intPreferencesKey("reminder_hour")
        val REMINDER_MINUTE = intPreferencesKey("reminder_minute")
        val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
        val SESSION_DURATION = intPreferencesKey("session_duration")
    }

    override suspend fun startScanning() {
        bleManager.startScanning()
    }

    // --- ESTADOS EN TIEMPO REAL ---

    // Lista temporal para acumular los datos de la sesión actual
    private var currentSessionData = mutableListOf<BiofeedbackData>()
    private var sessionStartTime: Long = 0

    private val _isSessionRecording = MutableStateFlow(false)
    override val isSessionRecording = _isSessionRecording.asStateFlow()

    override val biofeedbackData: Flow<BiofeedbackData> = bleManager.biofeedbackData
        .map { rawData: BiofeedbackData ->
            // 1. Calcular Coherencia REAL usando el caso de uso
            val realCoherenceScore = calculateCoherence(rawData)

            // 2. Crear el objeto procesado con el valor calculado
            val processedData = rawData.copy(
                coherence = realCoherenceScore.toFloat()
            )

            // 3. Si estamos grabando, guardamos este dato procesado en la lista
            if (_isSessionRecording.value) {
                // Filtro opcional para evitar ceros absolutos si el sensor falla momentáneamente
                if (processedData.coherence > 0.1f) {
                    currentSessionData.add(processedData)
                }
            }

            // 4. Emitimos el dato procesado a la UI
            processedData
        }

    override val connectionState: Flow<ConnectionState> = bleManager.connectionState
    override val batteryLevel: Flow<Int> = bleManager.batteryLevel

    // --- HISTORIAL ---
    override val allSessions: Flow<List<Session>> = dao.getAllSessions().map { entities ->
        entities.map { it.toDomain() }
    }

    // --- FUNCIONES DE CONEXIÓN ---
    override suspend fun connectToDevice(deviceAddress: String?): Result<Unit> {
        bleManager.connect(deviceAddress ?: "")
        return Result.success(Unit)
    }

    override suspend fun disconnect() { bleManager.disconnect() }
    override suspend fun calibrateSensors(): Result<Unit> = Result.success(Unit)

    // --- GESTIÓN DE SESIÓN ---
    override suspend fun startSession() {
        currentSessionData.clear()
        sessionStartTime = System.currentTimeMillis()
        _isSessionRecording.value = true
    }

    override suspend fun stopSession(): Result<Session> {
        if (!_isSessionRecording.value) return Result.failure(IllegalStateException("No hay sesión activa"))

        _isSessionRecording.value = false
        val endTime = System.currentTimeMillis()

        // Si no hay datos suficientes, no guardamos nada
        if (currentSessionData.isEmpty()) return Result.failure(Exception("Sesión sin datos válidos"))

        // --- CÁLCULO DEL PROMEDIO REAL ---
        val avgCoherence = currentSessionData
            .map { it.coherence }
            .average()
            .roundToInt()

        val durationSeconds = (endTime - sessionStartTime) / 1000

        // Generar listas históricas para las gráficas
        val coherenceHistory = currentSessionData.map { it.coherence.roundToInt() }
        val heartRateHistory = currentSessionData.map { it.heartRate }
        val hrvHistory = currentSessionData.map { it.heartRateVariability.roundToInt() }
        val tempHistory = currentSessionData.map { it.temperature }
        val conductanceHistory = currentSessionData.map { it.touchValue / 10f }

        val session = Session(
            id = UUID.randomUUID().toString(),
            startTime = sessionStartTime,
            endTime = endTime,
            finalAvgCoherence = avgCoherence,
            finalDurationSeconds = durationSeconds,
            coherenceHistory = coherenceHistory,
            heartRateHistory = heartRateHistory,
            hrvHistory = hrvHistory,
            temperatureHistory = tempHistory,
            conductanceHistory = conductanceHistory,
            dataPoints = emptyList()
        )

        return try {
            saveSessionToDb(session)
            currentSessionData.clear()
            Result.success(session)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    // --- PREFERENCIAS (DataStore) ---
    override suspend fun saveDarkMode(enabled: Boolean) {
        dataStore.edit { it[PreferencesKeys.DARK_MODE] = enabled }
    }
    override fun getDarkMode(): Flow<Boolean> = dataStore.data.map { it[PreferencesKeys.DARK_MODE] ?: false }

    override suspend fun saveSoundPreference(enabled: Boolean) {
        dataStore.edit { it[PreferencesKeys.SOUND_ENABLED] = enabled }
    }
    override fun getSoundPreference(): Flow<Boolean> = dataStore.data.map { it[PreferencesKeys.SOUND_ENABLED] ?: true }

    override suspend fun saveReminderSettings(enabled: Boolean, hour: Int, minute: Int) {
        dataStore.edit {
            it[PreferencesKeys.REMINDER_ENABLED] = enabled
            it[PreferencesKeys.REMINDER_HOUR] = hour
            it[PreferencesKeys.REMINDER_MINUTE] = minute
        }
    }
    override fun getReminderSettings(): Flow<Triple<Boolean, Int, Int>> = dataStore.data.map {
        Triple(it[PreferencesKeys.REMINDER_ENABLED] ?: false, it[PreferencesKeys.REMINDER_HOUR] ?: 8, it[PreferencesKeys.REMINDER_MINUTE] ?: 0)
    }

    override suspend fun saveNotificationsEnabled(enabled: Boolean) {
        dataStore.edit { it[PreferencesKeys.NOTIFICATIONS_ENABLED] = enabled }
    }
    override fun getNotificationsEnabled(): Flow<Boolean> = dataStore.data.map { it[PreferencesKeys.NOTIFICATIONS_ENABLED] ?: true }

    override suspend fun saveSessionDuration(minutes: Int) {
        dataStore.edit { it[PreferencesKeys.SESSION_DURATION] = minutes }
    }
    override fun getSessionDuration(): Flow<Int> = dataStore.data.map { it[PreferencesKeys.SESSION_DURATION] ?: 5 }


    // --- OPERACIONES DE BASE DE DATOS ---
    override suspend fun saveSession(session: Session): Result<Unit> {
        return try { saveSessionToDb(session); Result.success(Unit) } catch (e: Exception) { Result.failure(e) }
    }

    override suspend fun getSessionById(id: String): Session? {
        val entity = dao.getSessionById(id) ?: return null
        return entity.toDomain()
    }

    override suspend fun deleteSession(id: String): Result<Unit> {
        return try {
            dao.getSessionById(id)?.let {
                dao.deleteSession(it)
                Result.success(Unit)
            } ?: Result.failure(Exception("No encontrada"))
        } catch (e: Exception) { Result.failure(e) }
    }

    override suspend fun clearAllSessions(): Result<Unit> {
        return try { dao.clearAllSessions(); Result.success(Unit) } catch (e: Exception) { Result.failure(e) }
    }

    // --- CORRECCIÓN AQUÍ ---
    override suspend fun deleteSession(session: Session) {
        // Convertimos el modelo de dominio (Session) a entidad de base de datos (SessionEntity)
        // antes de pasarlo al DAO.
        dao.deleteSession(session.toEntity())
    }

    private suspend fun saveSessionToDb(session: Session) {
        dao.insertSession(session.toEntity())
    }
}