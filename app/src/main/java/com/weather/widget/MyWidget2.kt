package com.weather.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.View
import android.widget.RemoteViews
import com.weather.MainActivity
import com.weather.MainActivity.Companion.widgetTextColor
import com.weather.R
import com.weather.ui.main.WeatherViewModel
import com.weather.util.WeatherUtil

class MyWidget2 : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        // There may be multiple widgets active, so update all of them
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(
                context,
                appWidgetManager,
                appWidgetId
            )
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
        val cn = ComponentName(context, MyWidget2::class.java)
        onUpdate(context, mgr, mgr.getAppWidgetIds(cn))
    }


    companion object {

        internal fun updateAppWidget(
            context: Context, appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {
            try {

                val wea = WeatherViewModel.weatherTemp.daily

                val views = RemoteViews(context.packageName, R.layout.widget_2)

                val appInfo1 = "com.weather"

                views.setOnClickPendingIntent(R.id.appwidget2, MyWidget.getPI(context, appInfo1))


                if(wea.size>0){
                    views.setTextViewText(R.id.appwidget2_city, WeatherUtil.getCity())
                    views.setTextViewText(R.id.appwidget2_date, wea[0].fxDate.substring(5, 7) + wea[0].fxDate.substring(8, 10))
                    views.setTextViewText(R.id.appwidget2_wea, wea[0].textDay)
                    val tips = WeatherUtil.formatTip(wea[0]).split("，")
                    if (tips.size > 1) {
                        views.setTextViewText(R.id.appwidget2_tip, tips[0])
                        views.setTextViewText(R.id.appwidget2_tip2, tips[1])
                    } else {
                        views.setTextViewText(R.id.appwidget2_tip, WeatherUtil.formatTip(wea[0]))
                    }
                }


                views.setViewVisibility(
                    R.id.appwidget2_city,
                    if (wea.size > 0) View.VISIBLE else View.GONE
                )
                views.setViewVisibility(
                    R.id.appwidget2_date,
                    if (wea.size > 0) View.VISIBLE else View.GONE
                )
                views.setViewVisibility(
                    R.id.appwidget2_tip,
                    if (wea.size > 0) View.VISIBLE else View.GONE
                )

                views.setViewVisibility(R.id.center2, if (wea.size > 0) View.VISIBLE else View.GONE)
                views.setViewVisibility(
                    R.id.appwidget2_wea,
                    if (wea.size > 0) View.VISIBLE else View.GONE
                )

                views.setTextColor(R.id.appwidget2_now_time, widgetTextColor)
                views.setTextColor(R.id.appwidget2_now_date, widgetTextColor)
                views.setInt(R.id.center2, "setBackgroundColor", widgetTextColor);
                views.setTextColor(R.id.appwidget2_city, widgetTextColor)
                views.setTextColor(R.id.appwidget2_date, widgetTextColor)
                views.setTextColor(R.id.appwidget2_wea, widgetTextColor)
                views.setTextColor(R.id.appwidget2_tip, widgetTextColor)
                views.setTextColor(R.id.appwidget2_tip2, widgetTextColor)
                // Instruct the widget manager to update the widget
                appWidgetManager.updateAppWidget(appWidgetId, views)
            }catch (e: Exception){
                Log.e("exception","")
            }
        }
    }
}

