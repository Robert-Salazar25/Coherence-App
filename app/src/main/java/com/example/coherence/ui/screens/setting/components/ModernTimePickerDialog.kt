package com.example.coherence.ui.screens.setting.components

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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

// Función auxiliar para mostrar el reloj nativo de Android
@Composable
fun ModernTimePickerDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    initialHour: Int,
    initialMinute: Int,
    onTimeChange: (Int, Int) -> Unit
) {

    val initialIsPm = initialHour >= 12
    val initial12Hour = when{
        initialHour == 0 -> 12
        initialHour > 12 -> initialHour - 12
        else -> initialHour
    }

    var selectedHour by remember { mutableIntStateOf(initial12Hour) }
    var selectedMinute by remember { mutableIntStateOf(initialMinute) }
    var isPm by remember { mutableStateOf(initialIsPm) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(32.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp,
            modifier = Modifier.width(340.dp).padding(16.dp)
        ) {
            Column (
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ){
                Text(
                    text = "PROGRAMAR",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = 2.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(32.dp))

                Row (
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ){

                    NumberInput(
                        value = selectedHour,
                        range = 1..12,
                        onValueChange = {selectedHour = it}
                    )

                    Text(
                        text = ":",
                        style = MaterialTheme.typography.displayMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 8.dp).offset(y = (-4).dp)
                    )

                    NumberInput(
                        value = selectedMinute,
                        range = 0..59,
                        onValueChange = {
                            selectedMinute = it
                        }
                    )

                    Spacer(modifier = Modifier.width(16.dp))

                    AmPmSelector(
                        isPm = isPm,
                        onPmChange = {
                            isPm = it
                        }
                    )
                }
                Spacer(modifier = Modifier.height(32.dp))

                Row (
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ){
                    TextButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "Cancelar",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Button(
                        onClick = {
                            val finalHour = when{
                                isPm && selectedHour == 12 -> 12
                                isPm -> selectedHour + 12
                                !isPm && selectedHour == 12 -> 0
                                else -> selectedHour
                            }

                            onTimeChange(finalHour, selectedMinute)
                            onConfirm()
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary)
                    ) {
                        Text("Guardar")
                    }
                }
            }
        }
    }
}

@Composable
fun NumberInput(
    value: Int,
    range: IntRange,
    onValueChange: (Int) -> Unit
){
    Column (
        horizontalAlignment = Alignment.CenterHorizontally
    ){
        IconButton(onClick = {
            val newValue = if(value + 1 > range.last) range.first else value + 1
            onValueChange(newValue)
        }) {
            Icon(
                Icons.Default.KeyboardArrowUp,
                null,
                tint = MaterialTheme.colorScheme.primary)
        }

        Surface (
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surfaceContainerHighest,
            modifier = Modifier.size(80.dp, 70.dp)
        ){
            Box(contentAlignment = Alignment.Center){
                Text(
                    text = "%02d".format(value),
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        IconButton(onClick = {
            val newValue = if(value - 1 < range.first) range.last else value - 1
            onValueChange(newValue)
        }) {
            Icon(
                Icons.Default.KeyboardArrowDown,
                null,
                tint = MaterialTheme.colorScheme.primary)
        }
    }
}

@Composable
fun AmPmSelector(
    isPm: Boolean,
    onPmChange: (Boolean) -> Unit
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        // Usamos surfaceVariant que es más seguro para el fondo del contenedor
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier
            .width(50.dp)
            .height(100.dp)
            .clip(RoundedCornerShape(12.dp))
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // --- OPCIÓN AM ---
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    // Si es AM (!isPm), pintamos el fondo PRIMARIO, si no, TRANSPARENTE
                    .background(if (!isPm) MaterialTheme.colorScheme.primary else Color.Transparent)
                    .clickable { onPmChange(false) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "AM",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    // Si es AM, texto onPrimary (blanco), si no, onSurface (negro/gris)
                    color = if (!isPm) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                )
            }

            // --- OPCIÓN PM ---
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    // Si es PM (isPm), pintamos el fondo PRIMARIO, si no, TRANSPARENTE
                    .background(if (isPm) MaterialTheme.colorScheme.primary else Color.Transparent)
                    .clickable { onPmChange(true) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "PM",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    // Si es PM, texto onPrimary (blanco), si no, onSurface (negro/gris)
                    color = if (isPm) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}