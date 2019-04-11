package com.weather.widgets

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.weather.data.WeatherNetWork
import android.content.ComponentName
import android.util.Log
import com.weather.MainActivity
import com.weather.R

class MyWidget2 : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        // There may be multiple widgets active, so update all of them
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onEnabled(context: Context) {
//        var intent = Intent(context,WidgetSetting::class.java)
//        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK );
//        context.startActivity(intent)
    }

    override fun onDisabled(context: Context) {
        // Enter relevant functionality for when the last widget is disabled
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        val mgr = AppWidgetManager.getInstance(context)
        val cn = ComponentName(context, MyWidget2::class.java!!)
        onUpdate(context, mgr, mgr.getAppWidgetIds(cn))
    }


    companion object {
        internal fun updateAppWidget(
            context: Context, appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {
            try {

                var wea = WeatherNetWork.weatherTemp
                var views: RemoteViews = RemoteViews(context.packageName, R.layout.widget2)
                var intent = Intent(context, MainActivity::class.java)
                var pi = PendingIntent.getActivity(context, 0, intent, 0)
                views.setOnClickPendingIntent(R.id.appwidget2, pi)

                views.setTextViewText(R.id.appwidget2_city, wea.city)
                views.setTextViewText(R.id.appwidget2_date, wea.data[0].m + wea.data[0].d)
                views.setTextViewText(R.id.appwidget2_wea, wea.data[0].wea)

                views.setTextColor(R.id.appwidget2_now_time, WidgetSetting.wColor)
                views.setTextColor(R.id.appwidget2_now_date, WidgetSetting.wColor)
                views.setTextColor(R.id.center, WidgetSetting.wColor)
                views.setTextColor(R.id.appwidget2_city, WidgetSetting.wColor)
                views.setTextColor(R.id.appwidget2_date, WidgetSetting.wColor)
                views.setTextColor(R.id.appwidget2_wea, WidgetSetting.wColor)
                // Instruct the widget manager to update the widget
                appWidgetManager.updateAppWidget(appWidgetId, views)
            }catch (e: Exception){
                Log.e("exception","")
            }
        }
    }
}

