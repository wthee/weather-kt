package com.weather.data.model

data class Weather(
    val `data`: MutableList<Data>,
    val city: String,
    val cityEn: String,
    val cityid: String,
    val country: String,
    val countryEn: String,
    val update_time: String
)

data class Data(
    var air: String,
    var air_level: String,
    var air_tips: String,
    val alarm: Alarm,
    val date: String,
    var day: String,
    var day_after: String,
    val hours: List<Hour>,
    val humidity: String,
    val index: List<Index>,
    val pressure: String,
    val sunrise: String,
    val sunset: String,
    var tem: String,
    val tem1: String,
    val tem2: String,
    val visibility: String,
    var wea: String,
    val wea_day: String,
    val wea_day_img: String,
    val wea_img: String,
    val wea_night: String,
    val wea_night_img: String,
    val week: String,
    val win: List<String>,
    val win_speed: String,
    var date_nl: String,//农历
    var tems: String,
    var tip: String,
    var y: String,
    var m: String,
    var d: String
)


data class Hour(
    val hours: String,
    var tem: String,
    val wea: String,
    val wea_img: String,
    val win: String,
    val win_speed: String
)

data class Index(
    val desc: String,
    val level: String,
    val title: String
)

class Alarm(
    val alarm_content: String,
    val alarm_level: String,
    val alarm_type: String
)
