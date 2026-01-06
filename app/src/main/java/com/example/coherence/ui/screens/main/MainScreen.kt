package com.example.coherence.ui.screens.main

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.coherence.domain.repository.ConnectionState
import com.example.coherence.ui.screens.main.components.CoherenceCard
import com.example.coherence.ui.screens.main.components.ConnectionStatusIndicator
import com.example.coherence.ui.screens.main.components.MetricInfo
import com.example.coherence.ui.screens.main.components.MetricInfoDialog
import com.example.coherence.ui.screens.main.components.MetricsGrid
import com.example.coherence.ui.screens.main.components.StressLevelBar
import com.example.coherence.ui.viewmodel.BiofeedbackViewModel

@Composable
fun MainScreen(
    modifier: Modifier = Modifier,
    uiState: com.example.coherence.ui.viewmodel.BiofeedbackUiState,
    viewModel: BiofeedbackViewModel,
    onNavigateToExercise: () -> Unit,
    onShowBottomBar: (Boolean) -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }

    // Al entrar a MainScreen, aseguramos que la BottomBar sea visible
    LaunchedEffect(Unit) {
        onShowBottomBar(true)
    }
    // Estado para el diálogo de información
    var selectedInfo by remember { mutableStateOf<MetricInfo?>(null) }

    // Manejo de errores
    LaunchedEffect(uiState.error) {
        uiState.error?.let { errorMsg ->
            snackbarHostState.showSnackbar(message = errorMsg)
            viewModel.clearError()
        }
    }


    val isConnected = uiState.connectionState is ConnectionState.Connected

    // Diálogo Pop-up de información
    if (selectedInfo != null) {
        MetricInfoDialog(
            title = selectedInfo!!.title,
            description = selectedInfo!!.description,
            optimalRange = selectedInfo!!.range,
            icon = selectedInfo!!.icon,
            iconColor = selectedInfo!!.color,
            onDismiss = { selectedInfo = null }
        )
    }

    // --- CONTENEDOR PRINCIPAL ---
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Contenido del Dashboard
        DashboardContent(
            uiState = uiState,
            isConnected = isConnected,
            viewModel = viewModel,
            onNavigateToExercise = onNavigateToExercise,
            onInfoSelect = { selectedInfo = it }
        )

        // Snackbar posicionado manualmente para respetar la navegación
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .padding(bottom = 90.dp)
        )
    }
}

// --- COMPONENTE: CONTENIDO DEL DASHBOARD ---
@Composable
fun DashboardContent(
    uiState: com.example.coherence.ui.viewmodel.BiofeedbackUiState,
    isConnected: Boolean,
    viewModel: BiofeedbackViewModel,
    onNavigateToExercise: () -> Unit,
    onInfoSelect: (MetricInfo) -> Unit
) {
    var showSaveDialog by remember { mutableStateOf(false) }

    if (showSaveDialog) {
        AlertDialog(
            onDismissRequest = { showSaveDialog = false },
            title = { Text("Finalizar Sesión") },
            text = { Text("¿Quieres guardar los datos de esta sesión en tu historial?") },
            confirmButton = {
                TextButton(onClick = { viewModel.toggleSession(); showSaveDialog = false }) {
                    Text("SÍ, GUARDAR")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.toggleSession(); showSaveDialog = false }) {
                    Text("NO")
                }
            },
            containerColor = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
            .padding(bottom = 8.dp)
            .padding(top = 8.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        ConnectionStatusIndicator(
            connectionState = uiState.connectionState,
            batteryLevel = uiState.batteryLevel,
            onConnectClick = { viewModel.connectToDevice() },
            modifier = Modifier.fillMaxWidth()
        )

        CoherenceCard(
            coherence = uiState.currentData.coherence.toInt(),
            sessionActive = uiState.isSessionActive,
            isStable = uiState.currentData.isStable,
            remainingTime = if (uiState.isSessionActive) formatTime(uiState.remainingSeconds) else null,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f),
            onClick = {
                onInfoSelect(
                    MetricInfo(
                    title = "Coherencia Cardíaca",
                    description = "Es el estado de sincronización óptima entre tu ritmo cardíaco, respiración y sistema nervioso.",
                    range = "Alta: > 70% (Flujo)\nMedia: 40-70% (Equilibrio)\nBaja: < 40% (Regular)",
                    icon = Icons.Default.Spa,
                    color = Color(0xFF00E676)
                )
                )
            }
        )

        MetricsGrid(
            heartRate = uiState.currentData.heartRate,
            spo2 = uiState.currentData.spo2,
            touchValue = uiState.gsrValueDisplay,
            temperature = uiState.currentData.temperature,
            heartRateVariability = uiState.currentData.heartRateVariability,
            modifier = Modifier.fillMaxWidth()
        )

        StressLevelBar(
            stressLevel = uiState.stressLevel,
            coherence = uiState.currentData.coherence.toInt(),
            modifier = Modifier.fillMaxWidth(),
            onClick = {
                onInfoSelect(
                    MetricInfo(
                    title = "Nivel de Estrés",
                    description = "Estimación de carga fisiológica basada en HRV, temperatura y GSR.",
                    range = "Verde: Calma\nAmarillo: Alerta\nRojo: Estrés alto",
                    icon = Icons.Default.Psychology,
                    color = Color(0xFFFF3D00)
                )
                )
            }
        )

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = {
                    if (uiState.isSessionActive) {
                        showSaveDialog = true
                    } else {
                        viewModel.toggleSession()
                    }
                },
                enabled = isConnected,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (uiState.isSessionActive)
                        MaterialTheme.colorScheme.error
                    else
                        MaterialTheme.colorScheme.primary
                ),
                shape = MaterialTheme.shapes.large
            ) {
                Icon(
                    imageVector = if (uiState.isSessionActive) Icons.Default.Stop else Icons.Default.PlayArrow,
                    contentDescription = null
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = if (uiState.isSessionActive) "FINALIZAR SESIÓN" else "INICIAR SESIÓN",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            if (uiState.isSessionActive) {
                OutlinedButton(
                    onClick = { onNavigateToExercise() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    border = ButtonDefaults.outlinedButtonBorder.copy(width = 2.dp)
                ) {
                    Icon(Icons.Default.SelfImprovement, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("ABRIR GUÍA DE RESPIRACIÓN", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

fun formatTime(seconds: Long): String {
    val minutes = seconds / 60
    val remaining = seconds % 60
    return "%02d:%02d".format(minutes, remaining)
}