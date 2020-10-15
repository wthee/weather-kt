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
import com.weather.MyApplication
import com.weather.ui.main.WeatherFragment
import com.weather.ui.main.WeatherViewModel
import com.weather.util.InjectorUtil
import com.weather.util.WeatherUtil
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.lang.Exception
import java.util.*

class WidgetUpdateService : Service() {



    override fun onBind(intent: Intent): IBinder {
        return Binder()
    }

    override fun onCreate() {
        super.onCreate()

        val notificationManager =
            MyApplication.context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val mChannel: NotificationChannel
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mChannel = NotificationChannel(
                "1", "自动更新服务运行中", NotificationManager
                    .IMPORTANCE_NONE
            )
            notificationManager.createNotificationChannel(mChannel)
            val notification = Notification.Builder(applicationContext, "1")
                .build()
            startForeground(1, notification)
        }

        val timer = Timer()
        timer.schedule(object : TimerTask() {
            override fun run() {
                GlobalScope.launch {
                    try {
                        WeatherFragment.companionViewModel.getWidgetWeather(WeatherUtil.getCity())
                        delay(10 * 1000)
                        val intent = Intent(AppWidgetManager.ACTION_APPWIDGET_UPDATE)
                        MyApplication.context.sendBroadcast(intent)
                    }catch (e: Exception){

                    }
                }
            }
        }, 0, 30 * 60 * 1000) //30min

    }
}
