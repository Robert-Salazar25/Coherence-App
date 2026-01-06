package com.example.coherence.data.sound

import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator

class SoundManager(context: Context) {
    // Usamos ToneGenerator para no depender de archivos mp3
    private var toneGenerator: ToneGenerator? = null
    private var isEnabled: Boolean = true

    init {
        try {
            // STREAM_MUSIC asegura que el volumen dependa del volumen multimedia del teléfono
            // 80 es el volumen (0-100)
            toneGenerator = ToneGenerator(AudioManager.STREAM_MUSIC, 80)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun setSoundEnabled(enabled: Boolean) {
        this.isEnabled = enabled
    }

    fun playInhale() {
        if (!isEnabled) return
        try {
            // Tono agudo y suave para inhalar (High Pitch)
            // 150ms de duración
            toneGenerator?.startTone(ToneGenerator.TONE_CDMA_HIGH_L, 150)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun playExhale() {
        if (!isEnabled) return
        try {
            // Tono más grave para exhalar (Low Pitch)
            // 150ms de duración
            toneGenerator?.startTone(ToneGenerator.TONE_CDMA_LOW_L, 150)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun playSwitchSound() {
        // Este sonido se reproduce siempre para dar feedback táctil
        try {
            // Un "pip" muy corto (50ms) tipo click
            toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP, 50)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun release() {
        toneGenerator?.release()
        toneGenerator = null
    }
}