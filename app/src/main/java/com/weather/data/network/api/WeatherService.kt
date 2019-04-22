package com.weather.data.network.api

import com.weather.data.NowWeather
import com.weather.data.Weather
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.QueryMap

interface WeatherService {

    @GET(".")
    fun reqGetWea(@QueryMap map:Map<String ,String>): Call<Weather>

    @GET(".")
    fun reqGetNowWea(@QueryMap map:Map<String ,String>): Call<NowWeather>

}