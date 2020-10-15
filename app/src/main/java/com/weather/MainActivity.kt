package com.weather

import android.Manifest
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.lifecycle.viewModelScope
import androidx.preference.PreferenceManager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.weather.data.model.AppInfo
import com.weather.data.network.WeatherNetWork
import com.weather.databinding.MainActivityBinding
import com.weather.ui.main.WeatherFragment
import com.weather.util.ActivityUtil
import com.weather.util.Constant
import com.weather.util.StatusBarUtil
import com.weather.widget.WidgetUpdateService
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch


class MainActivity : AppCompatActivity() {

    companion object {
        var widgetTextColor: Int = -16777216
        lateinit var sp: SharedPreferences
        lateinit var spSetting: SharedPreferences

        //彩蛋？？？
        var questions = arrayListOf<String>()
        var answers = arrayListOf<String>()
    }

    init {
        MainScope().launch {
            val qas = WeatherNetWork.getInstance().fetchQa()
            qas.forEach {
                questions.add(it.question)
                answers.add(it.answer)
            }
        }
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
        startService()
        getAuthority()
        MainScope().launch {
            getAppList()
            //通知桌面小部件更新
            val intent = Intent(AppWidgetManager.ACTION_APPWIDGET_UPDATE)
            MyApplication.context.sendBroadcast(intent)
        }

        supportFragmentManager.beginTransaction()
            .replace(R.id.mainLayout, WeatherFragment())
            .commit()
    }

    private fun initSharedPreferences() {
        sp = getSharedPreferences("setting", MODE_PRIVATE)
        spSetting = PreferenceManager.getDefaultSharedPreferences(this)
        widgetTextColor = sp.getInt(Constant.WIDGET_TEXT_COLOR, widgetTextColor)
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

    //获取应用列表
    private fun getAppList() {
        val pm = this.packageManager
        // Return a List of all packages that are installed on the device.
        val packages = pm.getInstalledPackages(0)

        for (packageInfo in packages) {
            val appName = packageInfo.applicationInfo.loadLabel(pm).toString()
            val packageName = packageInfo.packageName
            if (packageInfo.applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM == 1) {
                //时钟
                if(appName.contains("时") && packageName.toLowerCase().contains("clock")){
                    MainActivity.sp.edit {
                        putString("appInfo1", packageName)
                    }
                }
                //日历
                if(appName.contains("日")  && packageName.toLowerCase().contains("calendar")){
                    MainActivity.sp.edit {
                        putString("appInfo2", packageName)
                    }
                }
            }

        }
    }
}
