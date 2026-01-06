package com.example.coherence.data.notifications

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.graphics.Color
import androidx.core.app.NotificationCompat
import com.example.coherence.MainActivity
import com.example.coherence.R

class ReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        // 1. Recuperar datos
        val message = intent.getStringExtra("message") ?: "Es hora de respirar"
        val hour = intent.getIntExtra("hour", -1)
        val minute = intent.getIntExtra("minute", -1)

        // 2. Intent principal (al tocar la notificación)
        val activityIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            activityIntent,
            PendingIntent.FLAG_IMMUTABLE
        )

        // 3. Intent para el botón de Acción "Iniciar ahora"
        // (En este caso abre la app, pero podrías dirigirlo a una pantalla específica)
        val actionPendingIntent = PendingIntent.getActivity(
            context,
            1,
            activityIntent,
            PendingIntent.FLAG_IMMUTABLE
        )

        // 4. Construir la Notificación MODERNA
        // Intenta usar un color hexadecimal que combine con tu app (ej. Morado o Azul)
        val accentColor = Color.parseColor("#6750A4")

        // Opcional: Cargar una imagen grande (si tienes un logo o imagen bonita en drawable)
        // val largeIconBitmap = BitmapFactory.decodeResource(context.resources, R.drawable.tu_imagen_bonita)

        val notification = NotificationCompat.Builder(context, ReminderManager.CHANNEL_ID)
            // Icono pequeño (debe ser blanco con fondo transparente para Android moderno)
            .setSmallIcon(R.drawable.ic_launcher_foreground)

            // Color de acento (tinta el icono y el nombre de la app)
            .setColor(accentColor)

            // Título y Texto
            .setContentTitle("Momento de Coherencia 🌿")
            .setContentText(message)

            // ESTILO GRANDE: Permite texto más largo y se ve más "rico" al expandir
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText("$message\nTu mente y cuerpo agradecerán esta pausa. Tómate unos minutos para sincronizarte."))

            // Prioridad y Categoría
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC) // Se ve en pantalla de bloqueo

            // BOTÓN DE ACCIÓN: Lo hace interactivo y moderno
            .addAction(
                android.R.drawable.ic_media_play, // Icono del botón
                "Iniciar ahora", // Texto del botón
                actionPendingIntent
            )

            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(1, notification)

        // 5. Re-programar para mañana (Lógica de repetición)
        if (hour != -1 && minute != -1) {
            val reminderManager = ReminderManager(context)
            reminderManager.scheduleReminder(hour, minute)
        }
    }
}