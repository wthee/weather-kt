package com.weather

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import com.weather.util.Logger

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        context = this
        Logger.init(this)
    }
    companion object {
        @SuppressLint("StaticFieldLeak")
        lateinit var context: Context
    }
}