package com.weather.util

import com.weather.data.model.weather.Weather
import java.text.SimpleDateFormat
import java.util.*

object RainFilterUtil {
    fun getRainInfo(weatherTemp: Weather){
        weatherTemp.data.toHashSet().forEach {
            if (!it.wea.contains("雨")) {
                weatherTemp.data.remove(it)
            } else {
                //温度范围
                it.tems = it.tem2 + "-" + it.tem1 + "℃"
                //日期
                var date = it.date.split('-')
                it.y = date[0]
                it.m = date[1]
                it.d = date[2]
                var today = Calendar.getInstance()
                today.time = SimpleDateFormat("yyyy-MM-dd").parse(it.date)
                var nl = LunarUtil(today).toString()
                var start = nl.indexOf("月") + 1
                var end = nl.lastIndex + 1
                it.date_nl = nl.substring(start, end)
                //温馨提示
                it.tip = when (it.wea.length) {
                    1 -> "下雨天，记得带伞"
                    2 -> when (it.wea) {
                        "小雨" -> "雨虽小，注意别感冒"
                        "中雨" -> "记得随身携带雨伞"
                        "大雨" -> "出门最好穿雨衣"
                        "阵雨" -> "阵雨来袭，记得带伞"
                        "暴雨" -> "尽量避免户外活动"
                        else -> "没有你的天气"
                    }
                    3 -> {
                        if (it.wea.contains("转"))
                            "天气多变，照顾好自己"
                        else
                            when (it.wea) {
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
}