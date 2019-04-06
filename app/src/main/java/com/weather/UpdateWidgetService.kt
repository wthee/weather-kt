package com.weather

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.appwidget.AppWidgetManager
import android.widget.RemoteViews
import java.util.*
import android.content.ComponentName




class UpdateWidgetService : Service() {

    private var timer: Timer? = null

    override fun onBind(intent: Intent): IBinder? {
        throw UnsupportedOperationException("Not yet implemented")
    }

    override fun onCreate() {
        timer = Timer()
        timer!!.schedule(object : TimerTask() {
            override fun run() {
                update()
            }
        }, 0, 1000)
        super.onCreate()
    }

    private fun update(){
        var now = Date().toLocaleString()
        var date = now.substring(0,now.indexOf("日")+1)
        var time = now.substring(now.indexOf("日")+2)
        val views = RemoteViews(MyApplication.context.packageName, R.layout.widget)
        //设置Widget中Textview的显示内容
        views.setTextViewText(R.id.appwidget_now_date, date)
        views.setTextViewText(R.id.appwidget_now_time, time)
        val componentName = ComponentName(this@UpdateWidgetService, MyWidget::class.java)
        val awm = AppWidgetManager.getInstance(applicationContext)
        awm.updateAppWidget(componentName, views)
    }


    override fun onDestroy() {
        super.onDestroy()
        onCreate()
    }

}
