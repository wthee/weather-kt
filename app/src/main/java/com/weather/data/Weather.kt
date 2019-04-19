package com.weather.data

import java.io.Serializable

data class City(
    val cityZh: String,
    val id: String,
    val leaderZh: String
)

class NowWeather(
        val air: String,
        val date: String,
        val air_level: String,
        val air_pm25: String,
        val air_tips: String,
        val alarm: Alarm,
        var tem: String,
        val update_time: String,
        val wea: String
)


class Weather(
    val city: String,
    val `data`: ArrayList<Data>,
    val update_time: String
)

class Data(
    var air: Int,
    var air_level: String,
    var air_tips: String,
    val alarm: Alarm,
    val date: String,
    val day: String,
    val hours: ArrayList<Hour>,
    val humidity: Int,
    val index: ArrayList<Index>,
    val tem: String,
    val tem1: String,
    val tem2: String,
    val wea: String,
    val week: String,

    var date_nl: String,
    var tems: String,
    var tip: String,
    var y: String,
    var m: String,
    var d: String
): Serializable

class Alarm(
    val alarm_content: String,
    val alarm_level: String,
    val alarm_type: String
)

class Hour(
    val day: String,
    val tem: String,
    val wea: String,
    val win: String,
    val win_speed: String
)
class Index(
    val desc: String,
    val level: String,
    val title: String
)