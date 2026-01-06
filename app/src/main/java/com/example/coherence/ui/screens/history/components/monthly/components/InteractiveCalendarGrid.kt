package com.example.coherence.ui.screens.history.components.monthly.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@Composable
fun InteractiveCalendarGrid(
    monthStartMillis: Long,
    daysWithSessions: Set<Int>, // Días que tienen puntito (actividad)
    selectedDay: Int?,          // Día actualmente seleccionado (para resaltar)
    onDayClick: (Int) -> Unit   // Acción al tocar un día
) {
    // 1. Configuración del Calendario
    val calendar = Calendar.getInstance().apply { timeInMillis = monthStartMillis }
    val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)

    // Formato para el título (ej: "Noviembre 2025")
    val monthTitle = SimpleDateFormat("MMMM yyyy", Locale("es", "ES"))
        .format(calendar.time)
        .replaceFirstChar { it.uppercase() }

    // 2. Calcular el desplazamiento (Offset)
    // Determinamos en qué día de la semana cae el día 1 del mes.
    val firstDayOfMonth = (calendar.clone() as Calendar).apply {
        set(Calendar.DAY_OF_MONTH, 1)
    }.get(Calendar.DAY_OF_WEEK)

    // Ajuste para que la semana empiece en Lunes (L=0, M=1, ... D=6)
    // Calendar.SUNDAY = 1, MONDAY = 2, etc.
    val offset = if (firstDayOfMonth == Calendar.SUNDAY) 6 else firstDayOfMonth - 2

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Encabezado
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.CalendarMonth, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text("Calendario de Actividad", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Text(monthTitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

            // Encabezados de días (L M M J V S D)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                listOf("L", "M", "M", "J", "V", "S", "D").forEach { day ->
                    Text(
                        text = day,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.width(32.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Grid de Días
            // Calculamos cuántas filas necesitamos
            val totalCells = daysInMonth + offset
            val rows = (totalCells / 7) + if (totalCells % 7 == 0) 0 else 1

            for (row in 0 until rows) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    for (col in 0 until 7) {
                        val cellIndex = (row * 7) + col
                        val dayNumber = cellIndex - offset + 1

                        // Verificamos si es un día válido del mes
                        if (dayNumber in 1..daysInMonth) {
                            val hasSession = daysWithSessions.contains(dayNumber)
                            val isSelected = selectedDay == dayNumber

                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape) // Recorte circular para el ripple y fondo
                                    .background(
                                        if (isSelected) MaterialTheme.colorScheme.primaryContainer
                                        else Color.Transparent
                                    )
                                    .clickable { onDayClick(dayNumber) }, // ¡Interacción aquí!
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "$dayNumber",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = if (hasSession || isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer
                                        else if (hasSession) MaterialTheme.colorScheme.onSurface
                                        else MaterialTheme.colorScheme.onSurfaceVariant
                                    )

                                    // El puntito indicador de actividad
                                    if (hasSession) {
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Box(
                                            modifier = Modifier
                                                .size(4.dp)
                                                .background(
                                                    if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer
                                                    else MaterialTheme.colorScheme.primary,
                                                    CircleShape
                                                )
                                        )
                                    }
                                }
                            }
                        } else {
                            // Espacio vacío para mantener la alineación
                            Spacer(modifier = Modifier.width(32.dp))
                        }
                    }
                }
                // Espacio entre filas
                if (row < rows - 1) Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}