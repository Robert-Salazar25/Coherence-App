package com.example.coherence.data.ble

import com.example.coherence.domain.model.BiofeedbackData
import com.example.coherence.domain.repository.ConnectionState
import kotlinx.coroutines.flow.Flow

// CAMBIO CLAVE: Debe ser 'interface', no 'class'
interface BiofeedbackBleManager {

    // En una interfaz, no inicializamos las variables, solo definimos el tipo
    val biofeedbackData: Flow<BiofeedbackData>
    val connectionState: Flow<ConnectionState>
    val batteryLevel: Flow<Int>

    // Definimos las funciones sin cuerpo {}
    suspend fun connect(address: String)
    suspend fun disconnect()

    suspend fun startScanning()
}