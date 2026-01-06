package com.example.coherence.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// Colores para Light Theme (Modo Claro)
private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF3366CC),      // ACENTO - Azul principal
    onPrimary = Color.White,
    primaryContainer = Color(0xFF3366CC),
    onPrimaryContainer = Color.White,

    secondary = Color(0xFF44CC44),    // ESTRES_BAJO - Verde
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF44CC44),
    onSecondaryContainer = Color.White,

    tertiary = Color(0xFFFFAA00),     // ESTRES_MEDIO - Ámbar
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFFFAA00),
    onTertiaryContainer = Color.White,

    background = Color(0xFFF5F5F5),   // FONDO - Gris claro
    onBackground = Color(0xFF333333), // TEXTO - Gris oscuro

    surface = Color.White,            // Superficies de tarjetas
    onSurface = Color(0xFF333333),    // Texto en superficies

    error = Color(0xFFFF4444),        // ESTRES_ALTO - Rojo
    onError = Color.White
)

// Colores para Dark Theme (Modo Oscuro)
private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF4488FF),      // Azul más claro para dark mode
    onPrimary = Color.White,
    primaryContainer = Color(0xFF3366CC),
    onPrimaryContainer = Color.White,

    secondary = Color(0xFF66DD66),    // Verde más claro
    onSecondary = Color.Black,
    secondaryContainer = Color(0xFF44CC44),
    onSecondaryContainer = Color.White,

    tertiary = Color(0xFFFFBB33),     // Ámbar más claro
    onTertiary = Color.Black,
    tertiaryContainer = Color(0xFFFFAA00),
    onTertiaryContainer = Color.White,

    background = Color(0xFF121212),   // Fondo oscuro
    onBackground = Color(0xFFEEEEEE), // Texto claro

    surface = Color(0xFF1E1E1E),      // Superficies oscuras
    onSurface = Color(0xFFEEEEEE),    // Texto en superficies oscuras

    error = Color(0xFFFF6666),        // Rojo más claro
    onError = Color.White
)

// --- COLORES ADICIONALES ESPECÍFICOS PARA BIOFEEDBACK ---

// Colores para estados de estrés (los usarás directamente)
val ESTRES_ALTO = Color(0xFFFF4444)
val ESTRES_MEDIO = Color(0xFFFFAA00)
val ESTRES_BAJO = Color(0xFF44CC44)

// Colores para métricas específicas
val HEART_RATE_COLOR = Color(0xFFFF5252)      // Rojo para ritmo cardíaco
val GSR_COLOR = Color(0xFFFFC107)             // Ámbar para respuesta emocional
val TEMPERATURE_COLOR = Color(0xFF4CAF50)     // Verde para temperatura
val COHERENCE_COLOR = Color(0xFF7E57C2)       // Púrpura para coherencia

// Colores de conexión
val CONNECTED_COLOR = Color(0xFF3366CC)       // Azul cuando está conectado
val DISCONNECTED_COLOR = Color(0xFFFF4444)    // Rojo cuando está desconectado

// Colores de fondo para componentes
val CARD_BACKGROUND_LIGHT = Color.White
val CARD_BACKGROUND_DARK = Color(0xFF1E1E1E)

// --- TEMA PRINCIPAL DE LA APLICACIÓN ---
@Composable
fun CoherenceTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    // NUEVO: parámetro para controlar si configura automáticamente
    autoConfigureStatusBar: Boolean = false, // CAMBIA A FALSE
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val view = LocalView.current
    // SOLO configurar si autoConfigureStatusBar es true
    if (!view.isInEditMode && autoConfigureStatusBar) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}