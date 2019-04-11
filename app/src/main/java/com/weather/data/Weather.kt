package com.weather.data

class CityName(
    val cityZh: String
)

data class NowWeather(
        val air: String,
        val air_level: String,
        val air_pm25: String,
        val air_tips: String,
        val alarm: Alarm,
        val tem: String,
        val update_time: String,
        val wea: String
)


class Weather(
    val city: String,
    val `data`: ArrayList<Data>,
    val update_time: String
)

class Data(
    val air: Int,
    val air_level: String,
    val air_tips: String,
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
)

class Alarm(
    val alarm_content: String,
    val alarm_level: String,
    val alarm_type: String
)

class Hour(
    val day: String,
    val tem: String,
    val wea: String
)

class Index(
    val desc: String,
    val level: String,
    val title: String
)