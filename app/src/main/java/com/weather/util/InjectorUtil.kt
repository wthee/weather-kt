package com.weather.util


import com.weather.data.network.WeatherNetWork
import com.weather.data.WeatherRepository
import com.weather.viewmodels.WeatherFactory


object InjectorUtil {

    private fun getWeatherRepository() = WeatherRepository.getInstance(WeatherNetWork.getInstance())
    fun getWeatherViewModelFactory() =
        WeatherFactory(getWeatherRepository())
}