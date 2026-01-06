package com.example.coherence.ui.screens.exercise

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.coherence.domain.model.BiofeedbackData
import com.example.coherence.ui.screens.exercise.components.BreathingAnimation
import com.example.coherence.ui.screens.exercise.components.BreathingPhase
import com.example.coherence.ui.screens.exercise.components.ExerciseMetricsRow
import com.example.coherence.data.sound.SoundManager
import com.example.coherence.ui.viewmodel.BiofeedbackViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExerciseScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: BiofeedbackViewModel? = null
) {
    val context = LocalContext.current
    val soundManager = remember { SoundManager(context) }

    // Observamos datos solo para mostrarlos en pequeño abajo
    val uiState by viewModel?.uiState?.collectAsStateWithLifecycle()
        ?: remember { mutableStateOf(null) }
    val currentData = uiState?.currentData ?: BiofeedbackData()

    // Sonido
    val isSoundEnabled = uiState?.isSoundEnabled ?: true
    LaunchedEffect(isSoundEnabled) { soundManager.setSoundEnabled(isSoundEnabled) }
    DisposableEffect(Unit) { onDispose { soundManager.release() } }

    // Estados de animación
    var screenState by remember { mutableStateOf(ExerciseScreenState.PREPARING) }
    var preparationTimeLeft by remember { mutableIntStateOf(3) }
    var currentPhase by remember { mutableStateOf(BreathingPhase.EXHALE) }

    // Temporizador visual (solo para la animación, no afecta a la grabación)
    var exerciseTimeLeft by remember { mutableLongStateOf(60000L) }
    var phaseTimeLeft by remember { mutableIntStateOf(0) }
    var phaseDuration by remember { mutableLongStateOf(6000L) }

    LaunchedEffect(Unit) {
        // 1. Preparación
        while (preparationTimeLeft > 0 && isActive) {
            delay(1000)
            preparationTimeLeft--
        }

        // 2. Inicio
        if (isActive) {
            screenState = ExerciseScreenState.EXERCISING
            // NOTA: No iniciamos sesión aquí, ya lo hizo la MainScreen
        }

        val cycle = listOf(
            BreathingPhase.INHALE to 4000L,
            BreathingPhase.HOLD to 3000L,
            BreathingPhase.EXHALE to 6000L
        )

        // 3. Bucle de respiración
        if(screenState == ExerciseScreenState.EXERCISING){
            while (isActive && exerciseTimeLeft > 0) {
                for ((phase, duration) in cycle) {
                    if (!isActive || exerciseTimeLeft <= 0) break

                    when(phase) {
                        BreathingPhase.INHALE -> soundManager.playInhale()
                        BreathingPhase.EXHALE -> soundManager.playExhale()
                        else -> {}
                    }

                    currentPhase = phase
                    phaseDuration = duration
                    phaseTimeLeft = (duration / 1000).toInt()

                    var timeInPhase = 0L
                    while (timeInPhase < duration && isActive && exerciseTimeLeft > 0) {
                        delay(1000)
                        timeInPhase += 1000
                        exerciseTimeLeft -= 1000
                        if (phaseTimeLeft > 0) phaseTimeLeft--
                    }
                }
            }
        }

        // 4. Fin del tiempo visual
        if (isActive) {
            delay(1000)
            onBack() // Volvemos a la MainScreen (la sesión sigue grabando hasta que el usuario la pare)
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            // Barra superior transparente
            CenterAlignedTopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.Close, "Volver")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(Color.Transparent)
            )
        }
    ) { paddingValues ->
        Box(
            modifier = modifier.fillMaxSize().padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            Crossfade(targetState = screenState, label = "state") { state ->
                when (state) {
                    ExerciseScreenState.PREPARING -> {
                        Box(Modifier.fillMaxSize().background(Color.Black.copy(0.8f)), Alignment.Center) {
                            Text("$preparationTimeLeft", style = MaterialTheme.typography.displayLarge, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                    else -> {
                        Column(
                            Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Spacer(Modifier.weight(0.5f))

                            // Animación Grande
                            Box(Modifier.weight(2f), Alignment.Center) {
                                BreathingAnimation(
                                    isRunning = true,
                                    currentPhase = currentPhase,
                                    phaseDuration = phaseDuration,
                                    phaseTimeLeft = phaseTimeLeft
                                )
                            }

                            // Métricas pequeñas de apoyo
                            ExerciseMetricsRow(
                                heartRate = currentData.heartRate,
                                spo2 = currentData.spo2,
                                coherence = currentData.coherence.toInt()
                            )

                            Spacer(Modifier.height(20.dp))
                        }
                    }
                }
            }
        }
    }
}

enum class ExerciseScreenState { PREPARING, EXERCISING, GET_READY }