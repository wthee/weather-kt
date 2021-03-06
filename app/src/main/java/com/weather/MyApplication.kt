package com.weather

import android.annotation.SuppressLint
import android.app.ActivityManager
import android.app.Application
import android.content.Context
import android.text.TextUtils
import androidx.multidex.MultiDex
import com.weather.util.Logger
import interfaces.heweather.com.interfacesmodule.view.HeConfig


class MyApplication : Application() {

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
        MultiDex.install(this)
    }

    override fun onCreate() {
        super.onCreate()
        context = this
        Logger.init(this)
        HeConfig.init("HE2010101543431425","08e54876334d4a61a44bf2dcf4bc7383")
        HeConfig.switchToDevService()
    }

    companion object {
        @SuppressLint("StaticFieldLeak")
        lateinit var context: Context
    }
}