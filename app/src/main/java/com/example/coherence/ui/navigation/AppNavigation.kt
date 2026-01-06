package com.example.coherence.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.coherence.ui.permission.RequestBlePermissions
import com.example.coherence.ui.screens.exercise.ExerciseScreen
import com.example.coherence.ui.screens.history.HistoryScreen
import com.example.coherence.ui.screens.main.MainScreen
import com.example.coherence.ui.screens.sessiondetail.SessionDetailScreen
import com.example.coherence.ui.screens.setting.SettingsScreen
import com.example.coherence.ui.screens.welcome.WelcomeScreen
import com.example.coherence.ui.viewmodel.BiofeedbackViewModel

sealed class Screen(val route: String){
    object Welcome : Screen("welcome")
    object Main : Screen("main")
    object Exercise : Screen("exercise")
    object History : Screen("history")
    object Settings : Screen("settings")
    object SessionDetail : Screen("sessionDetail/{sessionId}") {
        fun createRoute(sessionId: String) = "sessionDetail/$sessionId"
    }
}

@Composable
fun AppNavigation(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    viewModel: BiofeedbackViewModel,
    onShowBottomBar: (Boolean) -> Unit
) {

    NavHost(
        navController = navController,
        startDestination = Screen.Welcome.route,
        modifier = modifier
    ) {

        // --- PANTALLA DE BIENVENIDA ---
        composable(Screen.Welcome.route) {
            // Ocultamos la barra inferior
            LaunchedEffect(Unit) { onShowBottomBar(false) }

            var launchPermissionRequest by remember { mutableStateOf(false) }

            if (launchPermissionRequest) {
                RequestBlePermissions(
                    onPermissionsGranted = {
                        navController.navigate(Screen.Main.route) {
                            popUpTo(Screen.Welcome.route) { inclusive = true }
                        }
                    },
                    onPermissionsDenied = {
                        launchPermissionRequest = false
                    }
                )
            }

            // Usamos el nuevo componente
            WelcomeScreen (
                onStartClick = {
                    launchPermissionRequest = true
                }
            )
        }

        // --- PANTALLA PRINCIPAL (DASHBOARD) ---
        composable(Screen.Main.route) {
            // Recolectamos el estado aquí
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()

            // Aseguramos que la barra se vea
            LaunchedEffect(Unit) { onShowBottomBar(true) }

            MainScreen(
                uiState = uiState, // <--- Asegúrate de pasar uiState si MainScreen lo pide
                viewModel = viewModel,
                onNavigateToExercise = {
                    navController.navigate(Screen.Exercise.route)
                },
                onShowBottomBar = onShowBottomBar
            )
        }

        // --- EJERCICIO ---
        composable(Screen.Exercise.route) {
            // En ejercicio solemos ocultar la barra para inmersión, o mostrarla según prefieras
            LaunchedEffect(Unit) { onShowBottomBar(false) }

            ExerciseScreen(
                onBack = {
                    navController.popBackStack()
                },
                viewModel = viewModel
            )
        }

        // --- HISTORIAL ---
        composable(Screen.History.route){
            LaunchedEffect(Unit) { onShowBottomBar(true) }

            val uiState by viewModel.uiState.collectAsStateWithLifecycle()

            HistoryScreen(
                navController = navController,
                sessions = uiState.allSessions,
                onDeleteSession = { session ->
                    viewModel.deleteSession(session)
                }
            )
        }

        // --- DETALLE SESIÓN ---
        composable(
            route = Screen.SessionDetail.route,
            arguments = listOf(navArgument("sessionId") { type = NavType.StringType })
        ) { backStackEntry ->
            LaunchedEffect(Unit) { onShowBottomBar(true) }

            val sessionId = backStackEntry.arguments?.getString("sessionId")
            if (sessionId != null) {
                SessionDetailScreen(
                    sessionId = sessionId,
                    onBack = { navController.popBackStack() },
                    viewModel = viewModel
                )
            }
        }

        // --- AJUSTES ---
        composable(Screen.Settings.route){
            LaunchedEffect(Unit) { onShowBottomBar(true) }

            SettingsScreen(
                onBack = {
                    navController.popBackStack()
                },
                viewModel = viewModel
            )
        }
    }
}