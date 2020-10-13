package com.weather

import android.Manifest
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.weather.databinding.MainActivityBinding
import com.weather.util.ActivityUtil
import com.weather.util.Constant
import com.weather.util.NightModelUtil
import com.weather.util.StatusBarUtil
import com.weather.widget.WidgetUpdateService


class MainActivity : AppCompatActivity() {

    companion object {
        var onNight = true
        var widgetTextColor: Int = -16777216
        var widgetTips = true
        var isDiyTips = false
        var diyTips = ""
        var isFirstOpen = true
        var isFirstOpenSetting = true
        lateinit var sp: SharedPreferences
        var citys = Constant.defaultCitys
        var cityIndex = 0
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

        initSharedPreferences()
        changeStatusBar()
        binding = MainActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ActivityUtil.instance.currentActivity = this
        NightModelUtil.initNightModel(onNight)
        startService()
        getAuthority()
    }

    private fun initSharedPreferences() {
        sp = getSharedPreferences("setting", MODE_PRIVATE)
        //城市集合
        citys = Gson().fromJson(
            sp.getString(Constant.CITYS, Constant.CITYS_DEFAULT),
            object : TypeToken<ArrayList<String>>() {}.type
        )
        cityIndex = sp.getInt(Constant.CITY_INDEX, 0)
        widgetTextColor = sp.getInt(Constant.WIDGET_TEXT_COLOR, widgetTextColor)
        widgetTips = sp.getBoolean(Constant.WIDGET_TIP_SHOW, widgetTips)
        isDiyTips = sp.getBoolean("isDiyTips", isDiyTips)
        diyTips = sp.getString("diyTips", diyTips)!!
        onNight = sp.getBoolean("onNight", false)
        isFirstOpen = sp.getBoolean("isFirstOpen", true)
        isFirstOpenSetting = sp.getBoolean("isFirstOpenSetting", true)
    }

    //状态栏颜色适配
    private fun changeStatusBar() {
        StatusBarUtil.setRootViewFitsSystemWindows(this, true);
        StatusBarUtil.setTranslucentStatus(this)
        if (!StatusBarUtil.setStatusBarDarkTheme(this, true)) {
            //如果不支持设置深色风格 为了兼容总不能让状态栏白白的看不清, 于是设置一个状态栏颜色为半透明,
            //这样半透明+白=灰, 状态栏的文字能看得清
            StatusBarUtil.setStatusBarColor(this, 0x55000000);
        }
    }

    //申请权限
    private fun getAuthority() {
        val permissions = arrayOf(
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE
        )
        val mPermissions = ArrayList<String>()
        for (string in permissions) {
            if (ContextCompat.checkSelfPermission(
                    this@MainActivity,
                    string
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                mPermissions.add(string)
            }
        }
        if (mPermissions.size > 0) {
            Toast.makeText(this, "分享功能需要读写权限", Toast.LENGTH_LONG).show()
            ActivityCompat.requestPermissions(this, permissions, 1)
        }
    }

    //启动后台服务
    private fun startService() {
        val service = Intent(this@MainActivity, WidgetUpdateService::class.java)
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O) {
            startForegroundService(service)
        } else {
            startService(service)
        }
    }
}
