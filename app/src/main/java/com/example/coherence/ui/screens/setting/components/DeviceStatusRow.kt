package com.example.coherence.ui.screens.setting.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.BluetoothConnected
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.coherence.domain.repository.ConnectionState

@Composable
fun DeviceStatusRow(
    connectionState: ConnectionState,
    onConnectToggle: () -> Unit
) {
    val isConnected = connectionState is ConnectionState.Connected
    val isConnecting = connectionState is ConnectionState.Connecting

    val statusText = when(connectionState) {
        is ConnectionState.Connected -> "Conectado"
        is ConnectionState.Connecting -> "Conectando..."
        is ConnectionState.Disconnected -> "Desconectado"
        is ConnectionState.Error -> "Error de conexión"
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onConnectToggle)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Icon(
            imageVector = if (isConnected) Icons.Default.BluetoothConnected else Icons.Default.Bluetooth,
            contentDescription = "Estado del Bluetooth",
            tint = if (isConnected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(24.dp)
        )
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 16.dp)
        ) {
            Text(
                text = if (isConnected) "Sensor Biofeedback" else "Dispositivo no encontrado",
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = statusText,
                style = MaterialTheme.typography.bodySmall,
                color = if (connectionState is ConnectionState.Error) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Text(
            text = if (isConnected) "Desconectar" else if (isConnecting) "..." else "Conectar",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.SemiBold
        )
    }
}