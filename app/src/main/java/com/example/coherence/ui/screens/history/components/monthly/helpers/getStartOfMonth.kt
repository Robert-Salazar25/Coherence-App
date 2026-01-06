package com.example.coherence.ui.screens.history.components.monthly.helpers

import java.util.Calendar

// Obtiene el inicio del mes
fun getStartOfMonth(calendar: Calendar): Long {
    val cal = calendar.clone() as Calendar
    cal.set(Calendar.DAY_OF_MONTH, 1)
    cal.set(Calendar.HOUR_OF_DAY, 0)
    cal.set(Calendar.MINUTE, 0)
    cal.set(Calendar.SECOND, 0)
    cal.set(Calendar.MILLISECOND, 0)
    return cal.timeInMillis
}