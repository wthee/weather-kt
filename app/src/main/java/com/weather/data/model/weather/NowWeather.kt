package com.weather.data.model.weather

class NowWeather(
    val air: String,
    val date: String,
    val air_level: String,
    val air_pm25: String,
    val air_tips: String,
    val alarm: Alarm,
    var tem: String,
    val update_time: String,
    val wea: String,
    var city: String
)