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
import android.view.View
import com.weather.MainActivity
import com.weather.MainActivity.Companion.diyTips
import com.weather.MainActivity.Companion.isDiyTips
import com.weather.MainActivity.Companion.wColor
import com.weather.MainActivity.Companion.widgetTips
import com.weather.R

class MyWidget : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        // There may be multiple widgets active, so update all of them
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onEnabled(context: Context) {

    }

    override fun onDisabled(context: Context) {
        // Enter relevant functionality for when the last widget is disabled
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        val mgr = AppWidgetManager.getInstance(context)
        val cn = ComponentName(context, MyWidget::class.java!!)
        onUpdate(context, mgr, mgr.getAppWidgetIds(cn))
    }


    companion object {
        internal fun updateAppWidget(
                context: Context, appWidgetManager: AppWidgetManager,
                appWidgetId: Int
        ) {
            try {

                var wea = WeatherNetWork.weatherTemp
                var views: RemoteViews = RemoteViews(context.packageName, R.layout.widget)
                var intent = Intent(context, MainActivity::class.java)
                var pi = PendingIntent.getActivity(context, 0, intent, 0)
                views.setOnClickPendingIntent(R.id.appwidget, pi)

                if(wea.data.size>0){
                    views.setTextViewText(R.id.appwidget_city, wea.city)
                    views.setTextViewText(R.id.appwidget_date, wea.data[0].m + "/" + wea.data[0].d)
                    views.setTextViewText(R.id.center, "┃┃┃")
                    views.setTextViewText(R.id.appwidget_wea, wea.data[0].wea)
                    views.setTextViewText(R.id.appwidget_tip, wea.data[0].tip)
                }else{
                    views.setTextViewText(R.id.appwidget_city,"")
                    views.setTextViewText(R.id.appwidget_date, "")
                    views.setTextViewText(R.id.center, "")
                    views.setTextViewText(R.id.appwidget_wea, "")
                    views.setTextViewText(R.id.appwidget_tip, "")
                }

                if(isDiyTips){
                    views.setTextViewText(R.id.appwidget_tip, diyTips)
                }

                if(widgetTips) {
                    views.setViewVisibility(R.id.appwidget_tip, View.VISIBLE)
                } else{
                    views.setViewVisibility(R.id.appwidget_tip, View.GONE)

                }

                views.setTextColor(R.id.appwidget_now_time, wColor)
                views.setTextColor(R.id.appwidget_now_date, wColor)
                views.setTextColor(R.id.center, wColor)
                views.setTextColor(R.id.appwidget_city, wColor)
                views.setTextColor(R.id.appwidget_date, wColor)
                views.setTextColor(R.id.appwidget_wea, wColor)
                views.setTextColor(R.id.appwidget_tip, wColor)

                // Instruct the widget manager to update the widget
                appWidgetManager.updateAppWidget(appWidgetId, views)
            }catch (e: Exception){
                Log.e("exception","")
            }
        }
    }
}

