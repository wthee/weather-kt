package com.weather.util

import android.app.Service
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator

object VibratorUtil {
    private val vibrator =
        ActivityUtil.instance.currentActivity!!.getSystemService(Service.VIBRATOR_SERVICE) as Vibrator

    fun vibratorStart(pattern: IntArray, amplitudes: IntArray, isRepeat: Int) {
        var longPattern = LongArray(pattern.size)
        pattern.forEachIndexed { index, i ->
            longPattern[index] = i.toLong()
        }
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            var vibe = VibrationEffect.createWaveform (longPattern,amplitudes,isRepeat)
            vibrator.vibrate(vibe)
        }else{
            vibrator.vibrate(longPattern,isRepeat)
        }
    }
}