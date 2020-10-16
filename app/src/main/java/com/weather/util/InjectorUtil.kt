package com.weather.util


import com.weather.ui.info.WeatherInfoFactory
import com.weather.ui.main.WeatherFactory


object InjectorUtil {

    fun getWeatherViewModelFactory() = WeatherFactory()

    fun getWeatherInfoViewModelFactory() = WeatherInfoFactory()
}