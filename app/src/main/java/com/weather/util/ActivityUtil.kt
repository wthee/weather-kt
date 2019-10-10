package com.weather.util


import androidx.appcompat.app.AppCompatActivity

import java.lang.ref.WeakReference
import android.text.TextUtils
import android.content.ComponentName
import android.content.Context.ACTIVITY_SERVICE
import androidx.core.content.ContextCompat.getSystemService
import android.app.ActivityManager
import android.content.Context


class ActivityUtil private constructor() {

    private var sCurrentActivityWeakRef: WeakReference<AppCompatActivity>? = null

    var currentActivity: AppCompatActivity?
        get() {
            var currentActivity: AppCompatActivity? = null
            if (sCurrentActivityWeakRef != null) {
                currentActivity = sCurrentActivityWeakRef!!.get()
            }
            return currentActivity
        }
        set(activity) {
            sCurrentActivityWeakRef = WeakReference<AppCompatActivity>(activity)
        }


    companion object {

        val instance = ActivityUtil()
    }


}