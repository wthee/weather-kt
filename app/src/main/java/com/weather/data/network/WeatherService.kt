package com.weather.data.network

import com.weather.data.model.weather.NowWeather
import com.weather.data.model.weather.Weather
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.QueryMap


interface WeatherService {

    /*
    https://www.tianqiapi.com/api/?version=v1&city=%E6%9D%AD%E5%B7%9E
    mapOf("version" to "v1", "city" to leader)
    */
    @GET(".")
    fun getWeather(@QueryMap map:Map<String ,String>): Call<Weather>

    @GET(".")
    fun getNowWeather(@QueryMap map:Map<String ,String>): Call<NowWeather>

}