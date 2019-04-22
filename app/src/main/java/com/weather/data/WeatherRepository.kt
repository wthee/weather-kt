package com.weather.data

import com.weather.data.network.WeatherNetWork
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


class WeatherRepository(
    private val netWork: WeatherNetWork
) {

    suspend fun getWeather(map:Map<String,String>) = withContext(Dispatchers.IO) {
        netWork.fetchWeather(map)
    }

    suspend fun getNowWeather(map:Map<String,String>) = withContext(Dispatchers.IO) {
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