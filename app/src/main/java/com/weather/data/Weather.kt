package com.weather.data

class Weather(
    val city: String,
    val cityid: String,
    val data: ArrayList<Data>,
    val update_time: String
)

class Data(
    val date: String,
    val date_nl: String,
    val tem1: String,
    val tem2: String,
    val wea: String,
    val wea_c: String,
    val wea_img: String,
    val week: String,
    val win: String,
    var tem: String,
    var tip: String,
    var y: String,
    var m: String,
    var d: String
)