package com.example.coherence.ui.screens.history

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Analytics
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.coherence.ui.theme.CoherenceTheme
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material3.TabRow
import androidx.compose.ui.draw.clip
import java.util.Date
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.drawText
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.compose.rememberNavController
import com.example.coherence.domain.model.Session
import com.example.coherence.ui.screens.history.components.monthly.MonthlyHistoryView
import com.example.coherence.ui.screens.history.components.weekly.WeeklyHistoryView

import kotlinx.coroutines.delay
import kotlin.math.roundToInt

@Composable
fun HistoryScreen(
    navController: androidx.navigation.NavController,
    sessions: List<Session>,
    onDeleteSession: (Session) -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Semanal", "Mensual")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        ScreenTopBar(
            title = "Historial",
            onBack = { navController.popBackStack() }
        )

        TabRow(selectedTabIndex = selectedTab) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { Text(title) }
                )
            }
        }

        Box(modifier = Modifier.weight(1f)) {
            when (selectedTab) {
                0 -> WeeklyHistoryView(
                    sessions = sessions,
                    onSessionClick = { sessionId ->
                        navController.navigate("sessionDetail/$sessionId")
                    },
                    onDeleteSession = onDeleteSession
                )
                1 -> MonthlyHistoryView(
                    sessions = sessions,
                    onSessionClick = { sessionId ->
                        navController.navigate("sessionDetail/$sessionId")
                    },
                    onDeleteSession = onDeleteSession
                )
            }
        }
    }
}

@Composable
private fun ScreenTopBar(title: String, onBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start
    ) {
        IconButton(onClick = onBack) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Volver"
            )
        }
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(start = 12.dp)
        )
    }
}


@Composable
fun EmptyHistoryState(modifier: Modifier = Modifier) {
    Box(modifier = modifier.padding(32.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Icon(imageVector = Icons.Outlined.Analytics, contentDescription = null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.secondary)
            Text("Tu historial está vacío", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
            Text(
                text = "Cuando completes tu primer ejercicio de coherencia, tus resultados aparecerán aquí para que puedas seguir tu progreso.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

// --- PREVIEWS ---
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Preview(showBackground = true, name = "History Screen con Tabs")
@Composable
fun HistoryScreen_Preview() {
    CoherenceTheme {
        // 1. Creamos datos falsos básicos para visualizar el diseño
        val dummySessions = listOf(
            Session(
                id = "preview_1",
                startTime = System.currentTimeMillis(),
                endTime = System.currentTimeMillis() + 600000,
                finalAvgCoherence = 85,
                finalDurationSeconds = 600, // 10 min
                coherenceHistory = listOf(80, 85, 90),
                dataPoints = emptyList(),
                // Campos adicionales para compatibilidad con el modelo completo
                heartRateHistory = listOf(70, 72, 75),
                hrvHistory = listOf(50, 55, 60),
                temperatureHistory = emptyList(),
                conductanceHistory = emptyList()
            ),
            Session(
                id = "preview_2",
                startTime = System.currentTimeMillis() - (24 * 60 * 60 * 1000), // Ayer
                endTime = System.currentTimeMillis(),
                finalAvgCoherence = 45,
                finalDurationSeconds = 300, // 5 min
                coherenceHistory = listOf(40, 45, 42),
                dataPoints = emptyList(),
                heartRateHistory = listOf(80, 82, 85),
                hrvHistory = listOf(30, 35, 32),
                temperatureHistory = emptyList(),
                conductanceHistory = emptyList()
            )
        )

        // 2. Pasamos el NavController simulado y la lista de sesiones
        HistoryScreen(
            navController = rememberNavController(),
            sessions = dummySessions,
            onDeleteSession = {} // <--- NUEVO: Lambda vacía para que el preview funcione
        )
    }
}