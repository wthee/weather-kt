package com.weather.util

import com.weather.MainActivity
import interfaces.heweather.com.interfacesmodule.bean.weather.WeatherDailyBean
import java.text.SimpleDateFormat
import java.util.*

object WeatherUtil {

    fun getCity() =  MainActivity.citys[MainActivity.cityIndex]
    fun setCity(city: String) {
        MainActivity.citys[MainActivity.cityIndex] = city
    }

    //TODO 优化 https://dev.heweather.com/docs/start/icons
    fun formatTip(dailyBean: WeatherDailyBean.DailyBean) =
        when (dailyBean.textDay.length) {
            1 -> "下雨天，记得带伞"
            2 -> when (dailyBean.textDay) {
                "小雨" -> "雨虽小，注意别感冒"
                "中雨" -> "记得随身携带雨伞"
                "大雨" -> "出门最好穿雨衣"
                "阵雨" -> "阵雨来袭，记得带伞"
                "暴雨" -> "尽量避免户外活动"
                else -> "没有你的天气"
            }
            3 -> {
                if (dailyBean.textDay.contains("转"))
                    "天气多变，照顾好自己"
                else
                    when (dailyBean.textDay) {
                        "雷阵雨" -> "尽量减少户外活动"
                        "大暴雨" -> "尽量避免户外活动"
                        "雨夹雪" -> "道路湿滑，出行要谨慎"
                        else -> "没有你的天气"
                    }
            }
            else -> "天气多变，照顾好自己"
        }

    /**
     * 根据日期获取 星期
     * @param datetime
     * @return
     */
    fun dateToWeek(datetime: String): String {
        val f = SimpleDateFormat("yyyy-MM-dd")
        val weekDays = arrayOf("星期日", "星期一", "星期二", "星期三", "星期四", "星期五", "星期六")
        val cal: Calendar = Calendar.getInstance()
        val date = f.parse(datetime)
        cal.time = date
        //一周的第几天
        var w: Int = cal.get(Calendar.DAY_OF_WEEK) - 1
        if (w < 0) w = 0
        return weekDays[w]
    }
}

//2020-10-13T21:35+08:00
fun String.formatDate(): String {
    if (this.length == 19)
        return this
    else
        return this.replace("T", " ").substring(0, this.lastIndexOf("+")) + ":00"

}