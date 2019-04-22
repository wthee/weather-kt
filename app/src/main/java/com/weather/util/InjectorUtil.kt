package com.weather.util


import com.weather.data.WeatherNetWork
import com.weather.data.WeatherRepository
import com.weather.viewmodels.WeatherFactory


object InjectorUtil {

    private fun getWeatherRepository() = WeatherRepository.getInstance(WeatherNetWork.getInstance())
    fun getWeatherViewModelFactory(city: String) =
        WeatherFactory(getWeatherRepository(), city)

}