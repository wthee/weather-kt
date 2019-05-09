package com.weather.ui.widget

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

        var notificationManager =
            MyApplication.context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        var mChannel : NotificationChannel
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mChannel = NotificationChannel("1", "自动更新服务运行中...", NotificationManager
                .IMPORTANCE_HIGH)
            notificationManager.createNotificationChannel(mChannel);
            var notification =  Notification.Builder(applicationContext, "1")
                .build()
            startForeground(1, notification)
        }

        var sharedPreferences = PreferenceManager.getDefaultSharedPreferences(MyApplication.context)
        var timer = Timer()
        timer.schedule(object : TimerTask() {
            override fun run() {
                Log.e("widget","running")
                var city = sharedPreferences.getString("city" ,"ip")
                GlobalScope.launch {
                    WeatherViewModel.weatherTemp = WeatherNetWork.getInstance().fetchWeather(mapOf(
                        "version" to "v1",
                        "city" to city))
                    RainFilterUtil.getRainInfo(WeatherViewModel.weatherTemp)
                    var intent = Intent(AppWidgetManager.ACTION_APPWIDGET_UPDATE)
                    MyApplication.context.sendBroadcast(intent)
                    Log.e("widget","update")
                }
            }
        }, 0, 30 * 60 * 1000) //30min

    }
}
