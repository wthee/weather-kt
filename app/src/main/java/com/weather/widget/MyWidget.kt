package com.weather.widget

import android.app.PendingIntent
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

class MyWidget : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
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
        val cn = ComponentName(context, MyWidget::class.java)
        onUpdate(context, mgr, mgr.getAppWidgetIds(cn))
    }

    companion object {

        fun getPI(context: Context,appInfo: String): PendingIntent{
            val packageManager = context.packageManager
            val intent = Intent(packageManager.getLaunchIntentForPackage(appInfo))
            return PendingIntent.getActivity(context, 0,intent,0)
        }

        internal fun updateAppWidget(
                context: Context, appWidgetManager: AppWidgetManager,
                appWidgetId: Int
        ) {
            try {

                val wea = WeatherViewModel.weatherTemp.daily
                val views = RemoteViews(context.packageName, R.layout.widget_1)

                val appInfo1 = MainActivity.sp.getString("appInfo1", "com.weather")!!
                val appInfo2 = MainActivity.sp.getString("appInfo2", "com.weather")!!
                val appInfo3 = "com.weather"

                views.setOnClickPendingIntent(R.id.appwidget_now_time,
                    getPI(context, appInfo1)
                )
                views.setOnClickPendingIntent(R.id.appwidget_now_date,
                    getPI(context, appInfo2)
                )
                views.setOnClickPendingIntent(R.id.rightView1,
                    getPI(context, appInfo3)
                )

                if(wea.size>0){
                    views.setTextViewText(R.id.appwidget_city, WeatherUtil.getCity())
                    views.setTextViewText(R.id.appwidget_date, wea[0].fxDate.substring(5, 7) + "/" + wea[0].fxDate.substring(8, 10))
                    views.setViewVisibility(R.id.center, View.VISIBLE)
                    views.setTextViewText(R.id.appwidget_wea, wea[0].textDay)
                    views.setTextViewText(R.id.appwidget_tip, WeatherUtil.formatTip(wea[0]))
                }

                views.setViewVisibility(R.id.appwidget_city,if(wea.size>0) View.VISIBLE else View.GONE)
                views.setViewVisibility(R.id.appwidget_date, if(wea.size>0) View.VISIBLE else View.GONE)
                views.setViewVisibility(R.id.center, if(wea.size>0) View.VISIBLE else View.GONE)
                views.setViewVisibility(R.id.appwidget_wea, if(wea.size>0) View.VISIBLE else View.GONE)

                views.setTextColor(R.id.appwidget_now_time, widgetTextColor)
                views.setTextColor(R.id.appwidget_now_date, widgetTextColor)
                views.setTextColor(R.id.center, widgetTextColor)
                views.setTextColor(R.id.appwidget_city, widgetTextColor)
                views.setTextColor(R.id.appwidget_date, widgetTextColor)
                views.setTextColor(R.id.appwidget_wea, widgetTextColor)
                views.setTextColor(R.id.appwidget_tip, widgetTextColor)

                appWidgetManager.updateAppWidget(appWidgetId, views)
            }catch (e: Exception){
                Log.e("exception","")
            }
        }
    }
}

