package com.example.util

import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Build
import android.os.CombinedVibration
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

class BeepManager(private val context: Context) {

    private var toneGenerator: ToneGenerator? = null

    init {
        try {
            toneGenerator = ToneGenerator(AudioManager.STREAM_NOTIFICATION, 95)
        } catch (_: Exception) {
            toneGenerator = null
        }
    }

    fun playSuccessBeep() {
        try {
            toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP, 120)
            vibrate(pattern = longArrayOf(0, 50, 40, 50))
        } catch (_: Exception) {
            // ignore
        }
    }

    fun playWarningBeep() {
        try {
            toneGenerator?.startTone(ToneGenerator.TONE_PROP_PROMPT, 250)
            vibrate(pattern = longArrayOf(0, 100, 50, 100))
        } catch (_: Exception) {
            // ignore
        }
    }

    fun playErrorBeep() {
        try {
            toneGenerator?.startTone(ToneGenerator.TONE_PROP_NACK, 200)
            vibrate(pattern = longArrayOf(0, 150))
        } catch (_: Exception) {
            // ignore
        }
    }

    private fun vibrate(pattern: LongArray) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                val effect = VibrationEffect.createWaveform(pattern, -1)
                vibratorManager?.vibrate(CombinedVibration.createParallel(effect))
            } else {
                @Suppress("DEPRECATION")
                val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator?.vibrate(VibrationEffect.createWaveform(pattern, -1))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator?.vibrate(pattern, -1)
                }
            }
        } catch (_: Exception) {
            // ignore
        }
    }

    fun release() {
        try {
            toneGenerator?.release()
            toneGenerator = null
        } catch (_: Exception) {
            // ignore
        }
    }
}
