package com.example.coherence.ui.screens.sessiondetail

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.coherence.domain.model.Session
import com.example.coherence.ui.screens.sessiondetail.components.SessionDetailContent
import com.example.coherence.ui.theme.CoherenceTheme
import com.example.coherence.ui.viewmodel.BiofeedbackViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

// Clase auxiliar para las gráficas (para mantener tu diseño de TimeSeriesDataPoint)
data class TimeSeriesDataPoint(val timestamp: Long, val value: Float)

// 1. PANTALLA PRINCIPAL (Conectada al ViewModel)
@Composable
fun SessionDetailScreen(
    sessionId: String, // ID es String (UUID)
    onBack: () -> Unit,
    viewModel: BiofeedbackViewModel, // Recibimos el ViewModel
    modifier: Modifier = Modifier
) {
    // Observamos los datos reales
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Buscamos la sesión específica
    val session = remember(uiState.allSessions, sessionId) {
        uiState.allSessions.find { it.id == sessionId }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        ScreenTopBar(title = "Detalle de Sesión", onBack = onBack)

        if (session == null) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Cargando o sesión no encontrada...", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            // Pasamos la sesión real al diseño visual
            SessionDetailContent(session = session)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ScreenTopBar(
    title: String,
    onBack: () -> Unit
) {
    CenterAlignedTopAppBar(
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Volver"
                )
            }
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = MaterialTheme.colorScheme.background,
            titleContentColor = MaterialTheme.colorScheme.onBackground
        )
    )
}


// --- PREVIEW ---

@Preview(showBackground = true, name = "Detalle de Sesión")
@Composable
fun SessionDetailScreen_Preview() {
    CoherenceTheme {
        // Datos falsos completos para el preview
        val dummySession = Session(
            id = "preview_1",
            startTime = System.currentTimeMillis(),
            endTime = System.currentTimeMillis() + 1200000,
            finalAvgCoherence = 85,
            finalDurationSeconds = 1200,
            // Llenamos todas las listas con datos simulados
            coherenceHistory = listOf(60, 70, 80, 85, 90, 88, 92, 95),
            heartRateHistory = listOf(75, 78, 80, 76, 72, 70, 68, 65),
            hrvHistory = listOf(40, 45, 50, 55, 60, 65, 70, 75),
            temperatureHistory = listOf(36.5f, 36.6f, 36.7f, 36.8f, 36.8f, 36.9f, 37.0f, 37.0f),
            conductanceHistory = listOf(2.5f, 2.4f, 2.3f, 2.2f, 2.1f, 2.0f, 1.9f, 1.8f),
            dataPoints = emptyList()
        )

        SessionDetailContent(session = dummySession)
    }
}