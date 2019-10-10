package com.weather

import android.annotation.SuppressLint
import android.app.ActivityManager
import android.app.Application
import android.content.Context
import android.text.TextUtils
import androidx.multidex.MultiDex
import com.weather.util.Logger
import skin.support.SkinCompatManager
import skin.support.app.SkinAppCompatViewInflater
import skin.support.constraint.SkinCompatConstraintLayout
import skin.support.constraint.app.SkinConstraintViewInflater
import skin.support.design.app.SkinMaterialViewInflater


class MyApplication : Application() {

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
        MultiDex.install(this)
    }

    override fun onCreate() {
        super.onCreate()
        SkinCompatManager.withoutActivity(this)
            .addInflater(SkinAppCompatViewInflater())           // 基础控件换肤初始化
            .addInflater( SkinMaterialViewInflater())            // material design 控件换肤初始化[可选]
            .addInflater(SkinConstraintViewInflater())
            .loadSkin()
        context = this
        Logger.init(this)
    }

    fun isForeground(): Boolean {
        if (context != null) {
            val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            val cn = am.getRunningTasks(1)[0].topActivity
            val currentPackageName = cn.packageName
            return !TextUtils.isEmpty(currentPackageName) && currentPackageName == context.packageName
        }
        return false
    }

    companion object {
        @SuppressLint("StaticFieldLeak")
        lateinit var context: Context
    }
}