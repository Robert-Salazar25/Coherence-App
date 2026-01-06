package com.example.coherence.ui

import android.app.Activity
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.rememberNavController
import com.example.coherence.ui.navigation.AppNavigation
import com.example.coherence.ui.navigation.BottomNavigationBar
import com.example.coherence.ui.navigation.Screen
import com.example.coherence.ui.viewmodel.BiofeedbackViewModel

@Composable
fun CoherenceApp(
    viewModel: BiofeedbackViewModel
) {
    val navController = rememberNavController()
    var currentRoute by remember { mutableStateOf(Screen.Main.route) }
    var shouldShowBottomBar by remember { mutableStateOf(false) }

    // Obtener el estado del tema desde el ViewModel
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val darkTheme = uiState.isDarkMode // Usar el estado del ViewModel, no del sistema

    LaunchedEffect(navController) {
        navController.currentBackStackEntryFlow.collect { backStackEntry ->
            currentRoute = backStackEntry.destination.route ?: Screen.Main.route

            // Solo mostramos bottom bar en estas rutas
            shouldShowBottomBar = currentRoute in setOf(
                Screen.Main.route,
                Screen.History.route,
                Screen.Settings.route
            )
        }
    }

    // CONTROL DE BARRA DE ESTADO - REACCIONA A CAMBIOS DE RUTA Y TEMA
    val window = (LocalContext.current as Activity).window
    val view = LocalView.current
    val colorScheme = MaterialTheme.colorScheme

    LaunchedEffect(currentRoute, darkTheme) {
        val controller = WindowCompat.getInsetsController(window, view)

        when (currentRoute) {
            Screen.Welcome.route -> {
                // WELCOME SCREEN - Barra azul con iconos blancos
                window.statusBarColor = Color(0xFF0D47A1).toArgb()
                window.navigationBarColor = Color.Black.toArgb()
                controller.isAppearanceLightStatusBars = false // Iconos blancos
                controller.isAppearanceLightNavigationBars = false
            }
            else -> {
                // OTRAS PANTALLAS - Usar tema de la app
                // IMPORTANTE: Usar surface para la barra (mejor contraste)
                window.statusBarColor = colorScheme.background.toArgb()
                window.navigationBarColor = colorScheme.background.toArgb()

                // CALCULAR CORRECTAMENTE EL COLOR DE LOS ICONOS
                // En modo oscuro (darkTheme = true): iconos claros (isAppearanceLightStatusBars = false)
                // En modo claro (darkTheme = false): iconos oscuros (isAppearanceLightStatusBars = true)
                val shouldUseLightIcons = !darkTheme
                controller.isAppearanceLightStatusBars = shouldUseLightIcons
                controller.isAppearanceLightNavigationBars = shouldUseLightIcons

                // DEBUG: Verificar valores
                println("TEMA: darkTheme=$darkTheme, iconosClaros=$shouldUseLightIcons")
                println("COLOR BARRA: ${colorScheme.surface}")
            }
        }

        // Asegurar que los cambios se apliquen inmediatamente
        controller.show(WindowInsetsCompat.Type.statusBars())
    }

    val systemBarsPadding = WindowInsets.systemBars.asPaddingValues()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            if (shouldShowBottomBar) {

                Column(
                    modifier = Modifier
                        .padding(bottom = systemBarsPadding.calculateBottomPadding())
                ) {
                    BottomNavigationBar(
                        currentRoute = currentRoute,
                        onItemClick = { route ->
                            if (route != currentRoute) {
                                navController.navigate(route) {
                                    popUpTo(Screen.Main.route) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        AppNavigation(
            navController = navController,
            modifier = Modifier.padding(innerPadding),
            viewModel = viewModel,
            onShowBottomBar = { isVisible -> shouldShowBottomBar = isVisible }
        )
    }
}