package com.weather.util

import com.weather.data.model.weather.Weather
import java.text.SimpleDateFormat
import java.util.*


object RainFilterUtil {
    fun getRainInfo(weatherTemp: Weather) {
        weatherTemp.data.toHashSet().forEach { weather ->
            if (!weather.wea.contains("雨")) {
                weatherTemp.data.remove(weather)
            } else {
                //温度范围
                weather.tems = weather.tem2 + "-" + weather.tem1 + "℃"
                //日期
                val day = weather.day.substring(0, weather.day.indexOfFirst { it == '（' })
                val after = getDay(weather.date)
                weather.day = day
                weather.day_after = if(after != 0L)  "（" + after + "天后）" else "（今天）"
                var date = weather.date.split('-')
                weather.y = date[0]
                weather.m = date[1]
                weather.d = date[2]
                var today = Calendar.getInstance()
                today.time =
                    SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(weather.date + " 00:00:00")
                weather.date_nl = LunarUtil(today).toString()
                //温馨提示
                weather.tip = when (weather.wea.length) {
                    1 -> "下雨天，记得带伞"
                    2 -> when (weather.wea) {
                        "小雨" -> "雨虽小，注意别感冒"
                        "中雨" -> "记得随身携带雨伞"
                        "大雨" -> "出门最好穿雨衣"
                        "阵雨" -> "阵雨来袭，记得带伞"
                        "暴雨" -> "尽量避免户外活动"
                        else -> "没有你的天气"
                    }
                    3 -> {
                        if (weather.wea.contains("转"))
                            "天气多变，照顾好自己"
                        else
                            when (weather.wea) {
                                "雷阵雨" -> "尽量减少户外活动"
                                "大暴雨" -> "尽量避免户外活动"
                                "雨夹雪" -> "道路湿滑，出行要谨慎"
                                else -> "没有你的天气"
                            }
                    }
                    else -> "天气多变，照顾好自己"
                }
            }
        }
    }

    /*
   判读时间差距，两个时间相差多少天
    */
    private fun getDay(date: String): Long {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd")
        val currentTime = dateFormat.parse(dateFormat.format(Date()))//现在系统当前时间
        val futureTime = dateFormat.parse(date)//未来时间
        val diff = futureTime.time - currentTime.time
        return diff / (1000 * 60 * 60 * 24)
    }
}