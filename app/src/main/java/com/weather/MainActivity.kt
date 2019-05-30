package com.weather

import android.Manifest
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
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.weather.ui.widget.WidgetUpdateService
import com.weather.util.Logger
import java.util.ArrayList


class MainActivity : AppCompatActivity() {

    companion object {
        var onNight = true
        var wColor: Int = -16777216
        var widgetTips = true
        var isDiyTips = false
        var diyTips = ""
        var isFirstOpen = true
        var isFirstOpenSetting = true
        lateinit var sharedPreferences: SharedPreferences
        lateinit var editor: SharedPreferences.Editor
    }
    private lateinit var binding: MainActivityBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 避免从桌面启动程序后，会重新实例化入口类的activity
        if (!this.isTaskRoot) { // 当前类不是该Task的根部，那么之前启动
            val intent = intent
            if (intent != null) {
                val action = intent.action
                if (intent.hasCategory(Intent.CATEGORY_LAUNCHER) && Intent.ACTION_MAIN == action) { // 当前类是从桌面启动的
                    finish() // finish掉该类，直接打开该Task中现存的Activity
                    return
                }
            }
        }
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(MyApplication.context)
        editor = sharedPreferences.edit()
        wColor = sharedPreferences.getInt("widgetColor", wColor)
        widgetTips = sharedPreferences.getBoolean("widgetTips", widgetTips)
        isDiyTips = sharedPreferences.getBoolean("isDiyTips", isDiyTips)
        diyTips = sharedPreferences.getString("diyTips", diyTips)!!
        onNight = sharedPreferences.getBoolean("onNight",false)
        isFirstOpen = sharedPreferences.getBoolean("isFirstOpen",true)
        isFirstOpenSetting = sharedPreferences.getBoolean("isFirstOpenSetting",true)
        setAndroidNativeLightStatusBar(this, onNight)
        if(onNight){
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        }else{
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }
        binding = DataBindingUtil.setContentView(this, R.layout.main_activity)
        ActivityUtil.instance.currentActivity = this

        val service = Intent(this@MainActivity, WidgetUpdateService::class.java)
        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.O ){
            startForegroundService(service)
        }else{
            startService(service)
        }

        getAuthority()
    }

    private fun setAndroidNativeLightStatusBar(activity: Activity, onNight: Boolean) {
        val decor = activity.window.decorView
        if (!onNight) {
            decor.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        } else {
            decor.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        }
    }

    private fun getAuthority() {
        //申请权限
        val permissions = arrayOf(
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE
        )
        val mPermissions = ArrayList<String>()
        for (string in permissions) {
            if (ContextCompat.checkSelfPermission(this@MainActivity, string) != PackageManager.PERMISSION_GRANTED) {
                mPermissions.add(string)
            }
        }
        if (mPermissions.size > 0) {
            Toast.makeText(this,"分享功能需要读写权限",Toast.LENGTH_LONG).show()
            ActivityCompat.requestPermissions(this, permissions, 1)
        }
    }

}
