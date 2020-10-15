package com.weather.util

import com.google.gson.Gson

object Constant {
    //sharedPreferences
    const val WIDGET_TEXT_COLOR =  "widgetColor"
    const val WIDGET_TIP_SHOW =  "widgetTips"
    const val CITYS =  "citys"
    const val CITY_INDEX =  "cityIndex"
    const val LAST_API_UPDATE_TIME = "lastApiUpdateTime"

    val defaultCitys =  arrayListOf("杭州","北京")
    val CITYS_DEFAULT =  defaultCitys.ListoJson()
}

fun ArrayList<String>.ListoJson() = Gson().toJson(this)