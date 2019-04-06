package com.weather

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.weather.data.Weather
import com.weather.data.WeatherNetWork
import java.util.*
import android.content.ComponentName


/**
 * Implementation of App Widget functionality.
 */
class MyWidget : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        // There may be multiple widgets active, so update all of them
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onEnabled(context: Context) {
        context.startService(Intent(context, UpdateWidgetService::class.java))
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
            WeatherNetWork.getInstance().updateWidget(object : WeatherNetWork.setWidget {
                override fun callBack(wea: Weather) {
                    // Construct the RemoteViews object
                    val views = RemoteViews(context.packageName, R.layout.widget)
                    var intent = Intent(context, MainActivity::class.java)
                    var pi = PendingIntent.getActivity(context, 0, intent, 0)
                    views.setOnClickPendingIntent(R.id.appwidget, pi)
                    views.setTextViewText(R.id.appwidget_city, wea.city)
                    views.setTextViewText(R.id.appwidget_date, wea.data[0].m + "/" + wea.data[0].d)
                    views.setTextViewText(R.id.appwidget_wea, wea.data[0].wea)
                    views.setTextViewText(R.id.appwidget_tip, wea.data[0].tip)
                    // Instruct the widget manager to update the widget
                    appWidgetManager.updateAppWidget(appWidgetId, views)
                }
            })
        }
    }
}

