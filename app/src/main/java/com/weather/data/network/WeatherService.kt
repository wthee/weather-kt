package com.weather.data.network

import com.weather.data.model.weather.NowWeather
import com.weather.data.model.weather.Weather
import retrofit2.http.GET
import retrofit2.http.QueryMap


interface WeatherService {

    /*
    https://www.tianqiapi.com/api/?version=v1&city=%E6%9D%AD%E5%B7%9E&appid=23238842&appsecret=QgdXYe3r
    mapOf("version" to "v1", "city" to leader)
    */
    @GET(".")
    suspend fun getWeather(@QueryMap map:Map<String ,String>): Weather

    @GET(".")
    suspend fun getNowWeather(@QueryMap map:Map<String ,String>): NowWeather

}