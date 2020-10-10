package com.weather.widget

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.preference.PreferenceManager
import android.util.Log
import com.weather.MyApplication
import com.weather.data.network.WeatherNetWork
import com.weather.util.RainFilterUtil
import com.weather.ui.main.WeatherViewModel
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.*

class WidgetUpdateService : Service() {

    override fun onBind(intent: Intent): IBinder {
        return Binder()
    }

    override fun onCreate() {
        super.onCreate()

        val notificationManager =
            MyApplication.context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val mChannel : NotificationChannel
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mChannel = NotificationChannel("1", "自动更新服务运行中", NotificationManager
                .IMPORTANCE_NONE)
            notificationManager.createNotificationChannel(mChannel)
            val notification =  Notification.Builder(applicationContext, "1")
                .build()
            startForeground(1, notification)
        }

        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(MyApplication.context)
        val timer = Timer()
        timer.schedule(object : TimerTask() {
            override fun run() {
                val city = sharedPreferences.getString("city" ,"ip")
                GlobalScope.launch {
                    //TODO 获取天气
//                    WeatherViewModel.weatherTemp = WeatherNetWork.getInstance().fetchWeather(mutableMapOf(
//                        "version" to "v1",
//                        "city" to city!!))
//                    RainFilterUtil.getRainInfo(WeatherViewModel.weatherTemp)
                    val intent = Intent(AppWidgetManager.ACTION_APPWIDGET_UPDATE)
                    MyApplication.context.sendBroadcast(intent)
                }
            }
        }, 0, 30 * 60 * 1000) //30min

    }
}
