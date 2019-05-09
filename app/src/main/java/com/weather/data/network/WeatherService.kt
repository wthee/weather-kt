package com.weather.data.network

import com.weather.data.model.weather.NowWeather
import com.weather.data.model.weather.Weather
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.QueryMap


interface WeatherService {

    @GET(".")
    fun reqGetWea(@QueryMap map:Map<String ,String>): Call<Weather>

    @GET(".")
    fun reqGetNowWea(@QueryMap map:Map<String ,String>): Call<NowWeather>

}