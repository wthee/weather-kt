package com.weather.util

import com.weather.data.model.weather.Weather
import interfaces.heweather.com.interfacesmodule.bean.weather.WeatherDailyBean
import java.text.SimpleDateFormat
import java.util.*


object RainFilterUtil {
    fun getRainInfo(weatherTemp: WeatherDailyBean?) {
        weatherTemp!!.daily.toHashSet().forEach { weather ->
            if (!weather.textDay.contains("雨")) {
                weatherTemp.daily.remove(weather)
            } else {
//                //温度范围
//                weather.tems = weather.tem2 + "~" + weather.tem1 + "℃"
//                //日期
//                val day = weather.day.substring(0, weather.day.indexOfFirst { it == '（' })
//                val after = getDay(weather.date)
//                weather.day = day
//                weather.day_after = if(after != 0L)  "（" + after + "天后）" else "（今天）"

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