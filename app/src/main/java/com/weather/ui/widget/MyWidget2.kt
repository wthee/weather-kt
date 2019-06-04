package com.weather.ui.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import android.content.ComponentName
import android.util.Log
import android.view.View
import com.weather.MainActivity
import com.weather.MainActivity.Companion.diyTips
import com.weather.MainActivity.Companion.isDiyTips
import com.weather.MainActivity.Companion.wColor
import com.weather.MainActivity.Companion.widgetTips
import com.weather.R
import com.weather.ui.main.WeatherViewModel

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

                val wea = WeatherViewModel.weatherTemp

                val views: RemoteViews = RemoteViews(context.packageName, R.layout.widget_2)

                val appInfo1 = MainActivity.sharedPreferences.getString("appInfo1", "com.weather")!!
                val appInfo2 = MainActivity.sharedPreferences.getString("appInfo2", "com.weather")!!
                val appInfo3 = MainActivity.sharedPreferences.getString("appInfo3", "com.weather")!!

                views.setOnClickPendingIntent(R.id.appwidget2_now_time,
                    MyWidget.getPI(context, appInfo1)
                )
                views.setOnClickPendingIntent(R.id.appwidget2_now_date,
                    MyWidget.getPI(context, appInfo2)
                )
                views.setOnClickPendingIntent(R.id.rightView2,
                    MyWidget.getPI(context, appInfo3)
                )


                if(wea.data.size>0){
                    views.setTextViewText(R.id.appwidget2_date, wea.data[0].m + wea.data[0].d)
                    views.setTextViewText(R.id.appwidget2_city, wea.city)
                    views.setTextViewText(R.id.appwidget2_wea, wea.data[0].wea)
                    val tips = wea.data[0].tip.split("，")
                    if(tips.size>1){
                        views.setTextViewText(R.id.appwidget2_tip, tips[0])
                        views.setTextViewText(R.id.appwidget2_tip2, tips[1])
                    }
                    else{
                        views.setTextViewText(R.id.appwidget2_tip, wea.data[0].tip)
                    }
                }

                if(isDiyTips){
                    val tips = diyTips.split("，")
                    if(tips.size>1){
                        views.setTextViewText(R.id.appwidget2_tip, tips[0])
                        views.setTextViewText(R.id.appwidget2_tip2, tips[1])
                    }
                    else{
                        views.setTextViewText(R.id.appwidget2_tip, diyTips)
                        views.setTextViewText(R.id.appwidget2_tip2, "")
                    }
                }

                if(widgetTips) {
                    views.setViewVisibility(R.id.appwidget2_tip, View.VISIBLE)
                    views.setViewVisibility(R.id.appwidget2_tip2, View.VISIBLE)
                } else{
                    views.setViewVisibility(R.id.appwidget2_tip, View.GONE)
                    views.setViewVisibility(R.id.appwidget2_tip2, View.GONE)
                }

                views.setViewVisibility(R.id.appwidget2_city,if(wea.data.size>0) View.VISIBLE else View.GONE)
                views.setViewVisibility(R.id.appwidget2_date, if(wea.data.size>0) View.VISIBLE else View.GONE)
                views.setViewVisibility(R.id.center2, if(wea.data.size>0) View.VISIBLE else View.GONE)
                views.setViewVisibility(R.id.appwidget2_wea, if(wea.data.size>0) View.VISIBLE else View.GONE)

                views.setTextColor(R.id.appwidget2_now_time, wColor)
                views.setTextColor(R.id.appwidget2_now_date, wColor)
                views.setTextColor(R.id.center2, wColor)
                views.setTextColor(R.id.appwidget2_city, wColor)
                views.setTextColor(R.id.appwidget2_date, wColor)
                views.setTextColor(R.id.appwidget2_wea, wColor)
                views.setTextColor(R.id.appwidget2_tip, wColor)
                views.setTextColor(R.id.appwidget2_tip2, wColor)
                // Instruct the widget manager to update the widget
                appWidgetManager.updateAppWidget(appWidgetId, views)
            }catch (e: Exception){
                Log.e("exception","")
            }
        }
    }
}

