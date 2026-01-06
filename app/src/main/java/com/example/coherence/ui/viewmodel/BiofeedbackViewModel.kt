package com.example.coherence.ui.viewmodel

import android.os.CountDownTimer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.coherence.domain.model.BiofeedbackData
import com.example.coherence.domain.model.Session
import com.example.coherence.domain.model.SessionDataPoint
import com.example.coherence.domain.model.StressLevel
import com.example.coherence.domain.repository.BiofeedbackRepository
import com.example.coherence.domain.repository.ConnectionState
import com.example.coherence.domain.usecase.BiofeedbackCalculator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject
import kotlin.math.roundToInt

@HiltViewModel
class BiofeedbackViewModel @Inject constructor(
    private val repository: BiofeedbackRepository
) : ViewModel() {

    // Estado de la UI
    private val _uiState = MutableStateFlow(BiofeedbackUiState())
    val uiState: StateFlow<BiofeedbackUiState> = _uiState.asStateFlow()

    // Temporizador para la sesión
    private var sessionTimer: CountDownTimer? = null

    init {
        // Observamos datos del dispositivo
        observeBiofeedbackData()
        observeConnectionStatus()
        observeSessionStatus()
        observeBatteryLevel()
        allSessions()

        // Observamos las preferencias guardadas en DataStore
        observePreferences()
    }

    // Evitar fugas de memoria cancelando el timer al destruir el ViewModel
    override fun onCleared() {
        super.onCleared()
        sessionTimer?.cancel()
    }

    // --- 1. OBSERVACIÓN DE PREFERENCIAS ---
    private fun observePreferences() {
        viewModelScope.launch {
            launch {
                repository.getDarkMode()
                    .collect { isEnabled -> _uiState.update { it.copy(isDarkMode = isEnabled) } }
            }
            launch {
                repository.getSoundPreference()
                    .collect { isEnabled -> _uiState.update { it.copy(isSoundEnabled = isEnabled) } }
            }
            launch {
                repository.getReminderSettings().collect { (isEnabled, hour, minute) ->
                    _uiState.update {
                        it.copy(
                            isReminderEnabled = isEnabled,
                            reminderHour = hour,
                            reminderMinute = minute
                        )
                    }
                }
            }
            launch {
                repository.getNotificationsEnabled()
                    .collect { isEnabled -> _uiState.update { it.copy(areNotificationsEnabled = isEnabled) } }
            }
            launch {
                repository.getSessionDuration()
                    .collect { minutes -> _uiState.update { it.copy(targetSessionDurationMinutes = minutes) } }
            }
        }
    }

    // --- 2. GUARDADO DE PREFERENCIAS ---
    fun setDarkMode(enabled: Boolean) {
        _uiState.update { it.copy(isDarkMode = enabled) }
        viewModelScope.launch { repository.saveDarkMode(enabled) }
    }

    fun setSoundEnabled(enabled: Boolean) {
        _uiState.update { it.copy(isSoundEnabled = enabled) }
        viewModelScope.launch { repository.saveSoundPreference(enabled) }
    }

    fun setReminder(enabled: Boolean, hour: Int, minute: Int) {
        _uiState.update {
            it.copy(
                isReminderEnabled = enabled,
                reminderHour = hour,
                reminderMinute = minute
            )
        }
        viewModelScope.launch { repository.saveReminderSettings(enabled, hour, minute) }
    }

    fun setNotificationsEnabled(enabled: Boolean) {
        _uiState.update { it.copy(areNotificationsEnabled = enabled) }
        viewModelScope.launch { repository.saveNotificationsEnabled(enabled) }
    }

    fun setSessionDuration(minutes: Int) {
        _uiState.update { it.copy(targetSessionDurationMinutes = minutes) }
        viewModelScope.launch { repository.saveSessionDuration(minutes) }
    }

    // --- 3. LÓGICA DE NEGOCIO ---

    private fun observeBiofeedbackData() {
        viewModelScope.launch {
            repository.biofeedbackData.collect { data ->

                // 1. CALCULAR EL VALOR REAL PARA MOSTRAR (Dividir por 10)
                val realGsrValue = data.touchValue / 10f

                // 2. YA NO CALCULAMOS LA COHERENCIA AQUÍ.
                // Confiamos en que el Repositorio ya nos envía el dato procesado en 'data.coherence'.

                // 3. Calculamos el nivel de estrés basado en la coherencia que nos llega
                // CORRECCIÓN: Usamos .toInt() o .roundToInt() porque la función espera un Int
                val newStressLevel = BiofeedbackCalculator.determineStressLevel(data.coherence.roundToInt())

                // ACTUALIZACIÓN DE LA UI
                _uiState.update { currentState ->
                    if (currentState.connectionState is ConnectionState.Connected && currentState.isSessionActive) {
                        currentState.copy(
                            // Pasamos el objeto data tal cual (ya trae la coherencia correcta)
                            currentData = data,
                            gsrValueDisplay = realGsrValue,
                            stressLevel = newStressLevel,
                            isLoading = false
                        )
                    } else {
                        currentState
                    }
                }
            }
        }
    }

    private fun observeConnectionStatus() {
        viewModelScope.launch {
            repository.connectionState.collect { state ->
                _uiState.update { it.copy(connectionState = state) }

                if (state is ConnectionState.Disconnected || state is ConnectionState.Error) {
                    stopSessionTimer()
                    _uiState.update {
                        it.copy(
                            currentData = BiofeedbackData(),
                            stressLevel = StressLevel.UNKNOWN,
                            isSessionActive = false,
                            remainingSeconds = 0,
                            error = if (state is ConnectionState.Error) state.message else null
                        )
                    }
                }
            }
        }
    }

    private fun observeSessionStatus() {
        viewModelScope.launch {
            repository.isSessionRecording.collect { isRecording ->
                _uiState.update { it.copy(isSessionActive = isRecording) }
            }
        }
    }

    private fun observeBatteryLevel() {
        viewModelScope.launch {
            repository.batteryLevel.collect { level ->
                _uiState.update { it.copy(batteryLevel = level) }
            }
        }
    }

    fun connectToDevice() {
        viewModelScope.launch {
            repository.startScanning()
        }
    }

    fun disconnectDevice() {
        viewModelScope.launch {
            repository.disconnect()
        }
    }

    fun allSessions() {
        viewModelScope.launch {
            repository.allSessions.collect { dbSessions ->
                // Combinamos datos reales + datos falsos
                val dummyList = getDummySessionsList()
                val combinedSessions = (dbSessions + dummyList).sortedByDescending { it.startTime }
                _uiState.update { it.copy(allSessions = combinedSessions) }
            }
        }
    }

    fun deleteSession(session: Session) {
        viewModelScope.launch {
            // 1. Llamamos al repositorio para borrar de la base de datos
            repository.deleteSession(session)

            // 2. Actualizamos la lista local inmediatamente para que la UI reaccione
            // Filtramos la lista actual quitando la sesión que acabamos de borrar
            _uiState.update { currentState ->
                currentState.copy(
                    allSessions = currentState.allSessions.filter { it.id != session.id }
                )
            }
        }
    }

    // --- 4. GESTIÓN DE SESIÓN Y TEMPORIZADOR ---

    fun toggleSession() {
        if (_uiState.value.isSessionActive) {
            stopSession()
        } else {
            startSession()
        }
    }

    private fun startSession() {
        viewModelScope.launch {
            _uiState.update { it.copy(error = null) }
            repository.startSession()
            startSessionTimer() // <--- ESTO ES LO QUE ACTIVA EL CONTADOR
        }
    }

    private fun startSessionTimer() {
        val durationMinutes = _uiState.value.targetSessionDurationMinutes
        val totalSeconds = durationMinutes * 60L // Esto es Long

        _uiState.update {
            it.copy(
                remainingSeconds = totalSeconds, // CORREGIDO: Se pasa Long directamente
                sessionProgress = 1f
            )
        }

        sessionTimer?.cancel()
        sessionTimer = object : CountDownTimer(totalSeconds * 1000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                // CORREGIDO: Mantenemos secondsLeft como Long
                val secondsLeft = millisUntilFinished / 1000
                val progress = secondsLeft.toFloat() / totalSeconds.toFloat()

                _uiState.update {
                    it.copy(remainingSeconds = secondsLeft, sessionProgress = progress)
                }
            }

            override fun onFinish() {
                stopSession()
            }
        }.start()
    }

    private fun stopSessionTimer() {
        sessionTimer?.cancel()
        sessionTimer = null
        _uiState.update {
            it.copy(remainingSeconds = 0, sessionProgress = 0f)
        }
    }

    private fun stopSession() {
        stopSessionTimer()
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val result = repository.stopSession()

            result.onSuccess { session ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        lastSessionDuration = session.finalDurationSeconds,
                        lastSessionScore = session.finalAvgCoherence,
                        currentData = BiofeedbackData(),
                        stressLevel = StressLevel.UNKNOWN,
                        isSessionActive = false
                    )
                }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "Error al guardar sesión: ${error.message}",
                        isSessionActive = false
                    )
                }
            }
        }
    }

    fun clearHistory() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val result = repository.clearAllSessions()
            result.onSuccess {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "Historial borrado correctamente"
                    )
                }
            }.onFailure { e ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "Error al borrar: ${e.message}"
                    )
                }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    // --- GENERACIÓN DE DATOS FALSOS ---

    private fun getDummySessionsList(): List<Session> {
        val calendar = Calendar.getInstance()
        val dummySessions = mutableListOf<Session>()

        // 1. LUNES 25 NOV
        calendar.set(2025, Calendar.NOVEMBER, 25, 18, 30)
        dummySessions.add(
            createDummySession(
                id = "dummy_nov_25",
                startTime = calendar.timeInMillis,
                durationSecs = 600,
                avgCoherence = 35.0,
                historyPattern = { i -> 30.0 + Math.sin(i * 0.5) * 15 + (Math.random() * 10) }
            )
        )

        // 2. MARTES 26 NOV
        calendar.set(2025, Calendar.NOVEMBER, 26, 9, 0)
        dummySessions.add(
            createDummySession(
                id = "dummy_nov_26",
                startTime = calendar.timeInMillis,
                durationSecs = 300,
                avgCoherence = 55.0,
                historyPattern = { i -> 20.0 + (i * 0.2) + (Math.random() * 5) }
            )
        )

        // 3. MIÉRCOLES 27 NOV
        calendar.set(2025, Calendar.NOVEMBER, 27, 21, 15)
        dummySessions.add(
            createDummySession(
                id = "dummy_nov_27",
                startTime = calendar.timeInMillis,
                durationSecs = 900,
                avgCoherence = 88.5,
                historyPattern = { i -> 85.0 + Math.sin(i * 0.1) * 5 + (Math.random() * 2) }
            )
        )

        // 4. JUEVES 28 NOV
        calendar.set(2025, Calendar.NOVEMBER, 28, 14, 45)
        dummySessions.add(
            createDummySession(
                id = "dummy_nov_28",
                startTime = calendar.timeInMillis,
                durationSecs = 450,
                avgCoherence = 62.0,
                historyPattern = { i -> if (i < 200) 80.0 + (Math.random() * 5) else 40.0 + (Math.random() * 10) }
            )
        )

        // 5. VIERNES 29 NOV
        calendar.set(2025, Calendar.NOVEMBER, 29, 8, 0)
        dummySessions.add(
            createDummySession(
                id = "dummy_nov_29",
                startTime = calendar.timeInMillis,
                durationSecs = 1200,
                avgCoherence = 96.0,
                historyPattern = { i -> 94.0 + (Math.random() * 4) }
            )
        )

        return dummySessions
    }

    private fun generateSmoothData(
        count: Int,
        startValue: Float,
        min: Float,
        max: Float,
        volatility: Float
    ): List<Float> {
        val data = mutableListOf<Float>()
        var currentValue = startValue

        repeat(count) {
            // Cambiamos el valor ligeramente respecto al anterior (Random Walk)
            val change = (Math.random().toFloat() - 0.5f) * volatility
            currentValue = (currentValue + change).coerceIn(min, max)
            data.add(currentValue)
        }
        return data
    }

    private fun createDummySession(
        id: String,
        startTime: Long,
        durationSecs: Long,
        avgCoherence: Double,
        historyPattern: (Int) -> Double
    ): Session {
        val count = durationSecs.toInt()

        // 1. Generamos historial de Coherencia
        val historyInts = List(count) { i ->
            historyPattern(i).toInt().coerceIn(0, 100)
        }

        // 2. Generamos historiales SUAVES para las otras métricas
        // Frecuencia Cardíaca: Empieza en 70, varía entre 55-100, volatilidad media
        val heartRateHistory = generateSmoothData(count, 70f, 55f, 100f, 2.0f).map { it.toInt() }

        // HRV: Empieza en 50, varía entre 20-90, volatilidad alta
        val hrvHistory = generateSmoothData(count, 50f, 20f, 90f, 5.0f).map { it.toInt() }

        // Temperatura: Empieza en 36.5, varía muy poco (0.05 grados), volatilidad baja
        val temperatureHistory =
            generateSmoothData(count, 36.5f, 36.0f, 37.5f, 0.05f)

        // Conductancia: Empieza en 2.0, varía lento
        val conductanceHistory =
            generateSmoothData(count, 2.0f, 0.5f, 5.0f, 0.1f)

        // 3. Creamos los DataPoints sincronizados
        val dummyDataPoints = historyInts.mapIndexed { index, coherenceValue ->
            SessionDataPoint(
                timestamp = startTime + (index * 1000),
                coherence = coherenceValue.toFloat(),
                heartRate = heartRateHistory[index],
                hrv = hrvHistory[index].toFloat(),
                temperature = temperatureHistory[index],
                conductance = conductanceHistory[index]
            )
        }

        return Session(
            id = id,
            startTime = startTime,
            endTime = startTime + (durationSecs * 1000),
            finalAvgCoherence = avgCoherence.toInt(),
            finalDurationSeconds = durationSecs,
            coherenceHistory = historyInts,
            heartRateHistory = heartRateHistory,
            hrvHistory = hrvHistory,
            temperatureHistory = temperatureHistory,
            conductanceHistory = conductanceHistory,
            dataPoints = dummyDataPoints
        )
    }
}

// --- ESTADO DE LA UI ---
data class BiofeedbackUiState(
    val currentData: BiofeedbackData = BiofeedbackData(),
    val connectionState: ConnectionState = ConnectionState.Disconnected,
    val allSessions: List<Session> = emptyList(),

    val gsrValueDisplay: Float = 0f,

    // Estado de Sesión
    val isSessionActive: Boolean = false,
    val remainingSeconds: Long = 0,
    val sessionProgress: Float = 0f,
    val stressLevel: StressLevel = StressLevel.UNKNOWN,

    val batteryLevel: Int = 0,
    val isLoading: Boolean = false,
    val error: String? = null,
    val lastSessionDuration: Long = 0,
    val lastSessionScore: Int = 0,

    // Preferencias
    val isDarkMode: Boolean = false,
    val isSoundEnabled: Boolean = true,
    val areNotificationsEnabled: Boolean = true,
    val targetSessionDurationMinutes: Int = 5,

    // Recordatorios
    val isReminderEnabled: Boolean = false,
    val reminderHour: Int = 8,
    val reminderMinute: Int = 0
)