package com.weather

import android.Manifest
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.MotionEvent
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.app.SkinAppCompatDelegateImpl
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.weather.databinding.MainActivityBinding
import com.weather.ui.main.WeatherFragment.Companion.cityIndex
import com.weather.ui.main.WeatherFragment.Companion.weatherFragment
import com.weather.widget.WidgetUpdateService
import java.util.*
import com.weather.util.*
import skin.support.content.res.SkinCompatResources


class MainActivity : AppCompatActivity() {

    companion object {
        var onNight = true
        var widgetTextColor: Int = -16777216
        var widgetTips = true
        var isDiyTips = false
        var diyTips = ""
        var isFirstOpen = true
        var isFirstOpenSetting = true
        lateinit var sharedPreferences: SharedPreferences
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
        binding = DataBindingUtil.setContentView(this, R.layout.main_activity)

        ActivityUtil.instance.currentActivity = this
        NightModelUtil.initNightModel(onNight)
        startService()
        getAuthority()
    }

    override fun getDelegate(): AppCompatDelegate {
        return SkinAppCompatDelegateImpl.get(this, this)
    }

    //全局滑动监听
    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        when (ev.action) {
            MotionEvent.ACTION_DOWN -> TouchUtil.setDownXY(ev.x,ev.y)
            MotionEvent.ACTION_MOVE -> TouchUtil.setMoveXY(ev.x,ev.y)
            MotionEvent.ACTION_UP -> {
                TouchUtil.setUpXY(ev.x,ev.y)
                if(TouchUtil.actionUp() == -1){
                    //左
                    cityIndex = if((cityIndex + 1) % 3 == 0) 3 else (cityIndex + 1) % 3
                    weatherFragment.swipToChangeCity(cityIndex)
                    return true
                }
                if(TouchUtil.actionUp() == 1){
                    //右
                    cityIndex = if(cityIndex - 1 == 0) 3 else cityIndex - 1
                    weatherFragment.swipToChangeCity(cityIndex)
                    return true
                }
                if(TouchUtil.actionUp() == 0){
                    //点击
                    return super.dispatchTouchEvent(ev)
                }
            }
        }
        return super.dispatchTouchEvent(ev)
    }

    private fun initSharedPreferences(){
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(MyApplication.context)
        widgetTextColor = sharedPreferences.getInt("widgetColor", widgetTextColor)
        widgetTips = sharedPreferences.getBoolean("widgetTips", widgetTips)
        isDiyTips = sharedPreferences.getBoolean("isDiyTips", isDiyTips)
        diyTips = sharedPreferences.getString("diyTips", diyTips)!!
        onNight = sharedPreferences.getBoolean("onNight",false)
        isFirstOpen = sharedPreferences.getBoolean("isFirstOpen",true)
        isFirstOpenSetting = sharedPreferences.getBoolean("isFirstOpenSetting",true)
    }

    //状态栏颜色适配
    private fun changeStatusBar(){
        StatusBarUtil.setRootViewFitsSystemWindows(this,true);
        StatusBarUtil.setTranslucentStatus(this)
        if (!StatusBarUtil.setStatusBarDarkTheme(this, true)) {
            //如果不支持设置深色风格 为了兼容总不能让状态栏白白的看不清, 于是设置一个状态栏颜色为半透明,
            //这样半透明+白=灰, 状态栏的文字能看得清
            StatusBarUtil.setStatusBarColor(this,0x55000000);
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
            if (ContextCompat.checkSelfPermission(this@MainActivity, string) != PackageManager.PERMISSION_GRANTED) {
                mPermissions.add(string)
            }
        }
        if (mPermissions.size > 0) {
            Toast.makeText(this,"分享功能需要读写权限",Toast.LENGTH_LONG).show()
            ActivityCompat.requestPermissions(this, permissions, 1)
        }
    }

    //启动后台服务
    private fun startService(){
        val service = Intent(this@MainActivity, WidgetUpdateService::class.java)
        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.O ){
            startForegroundService(service)
        }else{
            startService(service)
        }
    }
}
