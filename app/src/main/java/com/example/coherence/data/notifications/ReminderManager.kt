package com.example.coherence.data.notifications

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import java.util.Calendar

class ReminderManager(private val context: Context) {

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun scheduleReminder(hour: Int, minute: Int) {
        // 1. Configurar el calendario
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)

            // Si la hora ya pasó hoy, programar para mañana
            if (before(Calendar.getInstance())) {
                add(Calendar.DATE, 1)
            }
        }

        // 2. Crear el Intent apuntando a ReminderReceiver
        // IMPORTANTE: Pasamos la hora y minuto para poder reprogramar mañana
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            putExtra("message", "Es hora de tu sesión de coherencia diaria ")
            putExtra("hour", hour)
            putExtra("minute", minute)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            REMINDER_REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // 3. Programar la alarma exacta
        try {
            // Usamos setExactAndAllowWhileIdle para asegurar que suene incluso en modo ahorro (Doze)
            // Esto es necesario para alarmas/recordatorios puntuales
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                pendingIntent
            )
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }

    fun cancelReminder() {
        // Debe coincidir exactamente con el Intent de creación (misma clase)
        val intent = Intent(context, ReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            REMINDER_REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }

    fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Recordatorios Diarios",
                NotificationManager.IMPORTANCE_HIGH // Importancia ALTA para que suene/vibre
            ).apply {
                description = "Canal para recordatorios de práctica de coherencia"
            }
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    companion object {
        const val REMINDER_REQUEST_CODE = 1001
        const val CHANNEL_ID = "daily_reminder_channel"
    }
}