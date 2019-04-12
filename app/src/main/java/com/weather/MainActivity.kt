package com.weather

import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.preference.PreferenceManager
import androidx.appcompat.app.AppCompatDelegate
import androidx.databinding.DataBindingUtil
import com.weather.databinding.MainActivityBinding
import com.weather.util.ActivityUtil
import android.app.Activity
import android.view.View


class MainActivity : AppCompatActivity() {

    companion object {
        var onNight = true
        var wColor: Int = -16777216
        var widgetTips = true
        var isDiyTips = false
        var diyTips = ""
        lateinit var sharedPreferences: SharedPreferences
        lateinit var editor: SharedPreferences.Editor
    }

    private lateinit var binding: MainActivityBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(MyApplication.context)
        editor = sharedPreferences.edit()
        wColor = sharedPreferences.getInt("widgetColor", wColor)
        widgetTips = sharedPreferences.getBoolean("widgetTips", widgetTips)
        isDiyTips = sharedPreferences.getBoolean("isDiyTips", isDiyTips)
        diyTips = sharedPreferences.getString("diyTips", diyTips)
        onNight = sharedPreferences.getBoolean("onNight",false)
        setAndroidNativeLightStatusBar(this, onNight)
        if(onNight){
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        }else{
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }
        binding = DataBindingUtil.setContentView(this, R.layout.main_activity)
        ActivityUtil.instance.currentActivity = this
    }

    private fun setAndroidNativeLightStatusBar(activity: Activity, onNight: Boolean) {
        val decor = activity.window.decorView
        if (!onNight) {
            decor.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        } else {
            decor.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        }
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(R.anim.activity_out,0)
    }
}
