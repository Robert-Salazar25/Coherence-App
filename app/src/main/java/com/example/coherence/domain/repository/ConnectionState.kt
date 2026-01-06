package com.example.coherence.domain.repository

sealed class ConnectionState {
    // Estado inicial o cuando se pierde la conexión
    object Disconnected : ConnectionState()

    // Cuando se está intentando establecer conexión
    object Connecting : ConnectionState()

    // Conexión exitosa y lista para recibir datos
    object Connected : ConnectionState()

    // Si ocurre algún fallo (incluye el mensaje de error)
    data class Error(val message: String) : ConnectionState()
}