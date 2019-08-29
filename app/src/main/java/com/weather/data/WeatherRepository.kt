package com.weather.data

import com.weather.data.network.WeatherNetWork
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.withContext


class WeatherRepository(
    private val netWork: WeatherNetWork
) {

    suspend fun getWeather(map:MutableMap<String,String>) = withContext(IO) {
        netWork.fetchWeather(map)
    }

    suspend fun getNowWeather(map:MutableMap<String,String>) = withContext(IO) {
        netWork.fetchNowWeather(map)
    }



    companion object {
        @Volatile
        private var instance: WeatherRepository? = null

        fun getInstance(netWork: WeatherNetWork) = instance
            ?: synchronized(this) {
                instance ?: WeatherRepository(netWork).also { instance = it }
            }
    }
}