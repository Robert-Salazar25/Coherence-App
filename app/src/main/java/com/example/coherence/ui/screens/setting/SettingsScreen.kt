package com.example.coherence.ui.screens.setting

import android.Manifest
import android.app.Activity
import android.app.AlarmManager
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.coherence.domain.repository.ConnectionState
import com.example.coherence.ui.screens.setting.components.DeviceStatusRow
import com.example.coherence.ui.screens.setting.components.DurationSelectionDialog
import com.example.coherence.ui.screens.setting.components.ModernTimePickerDialog
import com.example.coherence.ui.screens.setting.components.SettingsRow
import com.example.coherence.ui.screens.setting.components.SettingsSection
import com.example.coherence.ui.screens.setting.components.SettingsSwitchRow
import com.example.coherence.data.notifications.ReminderManager
import com.example.coherence.data.sound.SoundManager
import com.example.coherence.ui.viewmodel.BiofeedbackViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: BiofeedbackViewModel
) {
    // 1. Observamos el estado real del ViewModel
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val context = LocalContext.current
    val view = LocalView.current
    val colorScheme = MaterialTheme.colorScheme

    // 3. Actualizar barra de estado inmediatamente al cambiar el tema
    LaunchedEffect(uiState.isDarkMode) {
        val window = (context as Activity).window
        val controller = WindowCompat.getInsetsController(window, view)

        // Actualizar barra de estado en SettingsScreen
        window.statusBarColor = colorScheme.background.toArgb()
        controller.isAppearanceLightStatusBars = !uiState.isDarkMode
        controller.show(WindowInsetsCompat.Type.statusBars())
    }

    val bluetoothPermissionsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        // Si se conceden los permisos, intentamos conectar
        val allGranted = permissions.entries.all { it.value }
        if (allGranted) {
            viewModel.connectToDevice()
        } else {
            // Opcional: Mostrar un mensaje de que se necesitan permisos
        }
    }

    // Inicializamos los Managers
    val soundManager = remember { SoundManager(context) }
    val reminderManager = remember { ReminderManager(context) }

    // Crear canal de notificaciones al entrar (necesario para Android 8+)
    LaunchedEffect(Unit) {
        reminderManager.createNotificationChannel()
    }

    // Determinamos si está conectado
    val isConnected = uiState.connectionState is ConnectionState.Connected

    // Estado para diálogos
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showDurationDialog by remember { mutableStateOf(false) }
    var showTimePickerDialog by remember { mutableStateOf(false) } // <--- NUEVO

    // --- LÓGICA DEL RELOJ MODERNO ---
    if (showTimePickerDialog) {
        // Variables temporales para guardar la selección
        var tempHour by remember { mutableIntStateOf(uiState.reminderHour) }
        var tempMinute by remember { mutableIntStateOf(uiState.reminderMinute) }

        ModernTimePickerDialog(
            initialHour = uiState.reminderHour,
            initialMinute = uiState.reminderMinute,
            onDismiss = { showTimePickerDialog = false },
            onTimeChange = { h, m ->
                tempHour = h
                tempMinute = m
            },
            onConfirm = {
                // 1. Guardar la hora en la configuración
                viewModel.setReminder(true, tempHour, tempMinute)

                // 2. Programar la alarma (Verificando permisos de Android 12+)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    val alarmManager = context.getSystemService(AlarmManager::class.java)
                    if (alarmManager?.canScheduleExactAlarms() == false) {
                        // Si falta el permiso de alarma exacta, abrir ajustes
                        val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                        context.startActivity(intent)
                    } else {
                        reminderManager.scheduleReminder(tempHour, tempMinute)
                    }
                } else {
                    reminderManager.scheduleReminder(tempHour, tempMinute)
                }
                showTimePickerDialog = false
            }
        )
    }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            if (isGranted) {
                showTimePickerDialog = true // <--- ESTO ES IMPORTANTE
            }
        }
    )

    // --- DIÁLOGO DE CONFIRMACIÓN BORRAR ---
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("¿Borrar historial?") },
            text = { Text("Se eliminarán todas las sesiones guardadas. Esta acción no se puede deshacer.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.clearHistory()
                        showDeleteDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Borrar todo")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // --- DIÁLOGO DE DURACIÓN ---
    if (showDurationDialog) {
        DurationSelectionDialog(
            currentDuration = uiState.targetSessionDurationMinutes,
            onDismiss = { showDurationDialog = false },
            onDurationSelected = { newDuration ->
                viewModel.setSessionDuration(newDuration)
                showDurationDialog = false
            }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
    ) {
        // Cabecera
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Volver"
                )
            }
            Text(
                text = "Ajustes",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(start = 12.dp)
            )
        }

        // Sección 1: Dispositivo
        SettingsSection(title = "Dispositivo de Biofeedback") {
            DeviceStatusRow(
                connectionState = uiState.connectionState,
                onConnectToggle = {
                    if (isConnected) {
                        viewModel.disconnectDevice()
                    } else {
                        // --- NUEVO: Lógica de permisos ---
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                            // Android 12+
                            bluetoothPermissionsLauncher.launch(
                                arrayOf(
                                    Manifest.permission.BLUETOOTH_SCAN,
                                    Manifest.permission.BLUETOOTH_CONNECT
                                )
                            )
                        } else {
                            // Android 11 e inferiores
                            bluetoothPermissionsLauncher.launch(
                                arrayOf(
                                    Manifest.permission.ACCESS_FINE_LOCATION,
                                    Manifest.permission.ACCESS_COARSE_LOCATION
                                )
                            )
                        }
                    }
                }
            )
        }

        // Sección 2: Sesión
        SettingsSection(title = "Sesión de Ejercicio") {
            SettingsRow(
                icon = Icons.Default.History,
                title = "Duración de la Sesión",
                subtitle = "${uiState.targetSessionDurationMinutes} minutos",
                onClick = { showDurationDialog = true }
            )
            SettingsSwitchRow(
                icon = Icons.Default.VolumeUp,
                title = "Sonidos guía",
                subtitle = "Activa sonidos para guiar la respiración",
                checked = uiState.isSoundEnabled,
                onCheckedChange = { isChecked ->
                    soundManager.playSwitchSound()
                    viewModel.setSoundEnabled(isChecked)
                }
            )
        }

        // Sección 3: General
        SettingsSection(title = "General") {
            // 1. Modo Oscuro
            SettingsSwitchRow(
                icon = Icons.Default.Palette,
                title = "Modo Oscuro",
                subtitle = "Reduce la fatiga visual",
                checked = uiState.isDarkMode,
                onCheckedChange = { viewModel.setDarkMode(it) }
            )

            // 3. Recordatorio Diario
            SettingsSwitchRow(
                icon = Icons.Default.Alarm,
                title = "Recordatorio diario",
                subtitle = if (uiState.isReminderEnabled)
                    "Programado a las ${
                        String.format(
                            "%02d:%02d",
                            uiState.reminderHour,
                            uiState.reminderMinute
                        )
                    }"
                else "Desactivado",
                checked = uiState.isReminderEnabled,
                onCheckedChange = { isChecked ->
                    if (isChecked) {
                        // Si es Android 13+, pedimos permiso de notificación primero
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        } else {
                            // Si es anterior, mostramos el reloj directamente
                            showTimePickerDialog = true
                        }
                    } else {
                        // Apagar recordatorio
                        viewModel.setReminder(false, uiState.reminderHour, uiState.reminderMinute)
                        reminderManager.cancelReminder()
                    }
                }
            )
        }

        // Sección 4: Datos y Legal
        SettingsSection(title = "Datos y Legal") {
            SettingsRow(
                icon = Icons.Default.DeleteForever,
                title = "Borrar historial de sesiones",
                subtitle = "Esta acción no se puede deshacer",
                isDestructive = true,
                onClick = { showDeleteDialog = true }
            )
            SettingsRow(
                icon = Icons.Default.Policy,
                title = "Política de Privacidad",
                onClick = { /* TODO: Abrir URL */ }
            )
        }

        // Mostrar mensaje de error si existe
        if (uiState.error != null) {
            Text(
                text = uiState.error ?: "",
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(16.dp),
                style = MaterialTheme.typography.bodySmall
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}