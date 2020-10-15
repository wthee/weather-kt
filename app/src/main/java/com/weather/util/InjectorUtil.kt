package com.weather.util


import com.weather.data.WeatherRepository
import com.weather.data.network.WeatherNetWork
import com.weather.ui.info.WeatherInfoFactory
import com.weather.ui.main.WeatherFactory


object InjectorUtil {

    private fun getWeatherRepository() = WeatherRepository.getInstance(WeatherNetWork.getInstance())
    fun getWeatherViewModelFactory() =
        WeatherFactory(getWeatherRepository())

    fun getWeatherInfoViewModelFactory() = WeatherInfoFactory()
}