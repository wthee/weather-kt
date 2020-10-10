package com.weather.data

import com.weather.MyApplication
import com.weather.data.network.WeatherNetWork
import interfaces.heweather.com.interfacesmodule.bean.base.Lang
import interfaces.heweather.com.interfacesmodule.bean.weather.WeatherDailyBean
import interfaces.heweather.com.interfacesmodule.bean.weather.WeatherNowBean
import interfaces.heweather.com.interfacesmodule.view.HeWeather
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.withContext

class WeatherRepository(
    private val netWork: WeatherNetWork
) {

    companion object {
        @Volatile
        private var instance: WeatherRepository? = null

        fun getInstance(netWork: WeatherNetWork) = instance
            ?: synchronized(this) {
                instance ?: WeatherRepository(netWork).also { instance = it }
            }
    }
}