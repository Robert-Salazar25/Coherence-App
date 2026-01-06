package com.example.coherence.ui.screens.main.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryStd
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.coherence.domain.repository.ConnectionState
import com.example.coherence.ui.theme.CONNECTED_COLOR
import com.example.coherence.ui.theme.CoherenceTheme
import com.example.coherence.ui.theme.DISCONNECTED_COLOR

@Composable
fun ConnectionStatusIndicator(
    connectionState: ConnectionState,
    batteryLevel: Int = 0,
    onConnectClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    // Determinamos colores y textos según el estado complejo
    val (bgColor, contentColor) = when (connectionState) {
        is ConnectionState.Connected -> CONNECTED_COLOR.copy(alpha = 0.1f) to CONNECTED_COLOR
        is ConnectionState.Connecting -> Color(0xFFFFB74D).copy(alpha = 0.1f) to Color(0xFFF57C00) // Naranja
        is ConnectionState.Disconnected -> DISCONNECTED_COLOR.copy(alpha = 0.1f) to DISCONNECTED_COLOR
        is ConnectionState.Error -> DISCONNECTED_COLOR.copy(alpha = 0.1f) to DISCONNECTED_COLOR
    }

    Surface(
        onClick = {
            // Solo permitimos hacer click para conectar si NO está conectado ni cargando
            if (connectionState is ConnectionState.Disconnected || connectionState is ConnectionState.Error) {
                onConnectClick()
            }
        },
        modifier = modifier,
        color = bgColor,
        shape = MaterialTheme.shapes.medium
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            // --- ICONO O SPINNER ---
            when (connectionState) {
                is ConnectionState.Connecting -> {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = contentColor,
                        strokeWidth = 2.dp
                    )
                }
                is ConnectionState.Error -> {
                    Icon(
                        imageVector = Icons.Default.ErrorOutline,
                        contentDescription = "Error",
                        tint = contentColor
                    )
                }
                is ConnectionState.Connected -> {
                    Icon(
                        imageVector = Icons.Default.Wifi,
                        contentDescription = "Conectado",
                        tint = contentColor
                    )
                }
                is ConnectionState.Disconnected -> {
                    Icon(
                        imageVector = Icons.Default.WifiOff,
                        contentDescription = "Desconectado",
                        tint = contentColor
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // --- TEXTO DE ESTADO ---
            Column {
                Text(
                    text = when (connectionState) {
                        is ConnectionState.Connected -> "CONECTADO"
                        is ConnectionState.Connecting -> "CONECTANDO..."
                        is ConnectionState.Disconnected -> "DESCONECTADO (Tocar para conectar)"
                        is ConnectionState.Error -> "ERROR DE CONEXIÓN"
                    },
                    color = contentColor,
                    style = MaterialTheme.typography.labelLarge
                )

                // Subtítulo (Batería o Mensaje de Error)
                if (connectionState is ConnectionState.Connected) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.BatteryStd,
                            contentDescription = "Batería",
                            tint = contentColor,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "$batteryLevel%",
                            color = contentColor,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                } else if (connectionState is ConnectionState.Error) {
                    Text(
                        text = connectionState.message,
                        color = contentColor,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}

@Preview
@Composable
fun ConnectionStatusIndicatorPreview() {
    CoherenceTheme {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            ConnectionStatusIndicator(
                connectionState = ConnectionState.Connected,
                batteryLevel = 85
            )
            ConnectionStatusIndicator(
                connectionState = ConnectionState.Connecting
            )
            ConnectionStatusIndicator(
                connectionState = ConnectionState.Disconnected
            )
            ConnectionStatusIndicator(
                connectionState = ConnectionState.Error("No se encontró el dispositivo")
            )
        }
    }
}