package com.weather.ui.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.weather.data.NowWeather
import com.weather.data.Weather
import com.weather.data.WeatherRepository

class WeatherViewModel(
    private val repository: WeatherRepository,
    private val city: String
) : ViewModel() {
    var weather: LiveData<Weather> = repository.getWeather(city)
    var nowWeather: LiveData<NowWeather> = repository.getNowWeather(city)

    fun changeCity(city: String) = repository.changeCity(city)
    fun checkCity(city: String) = repository.checkCity(city)
    fun changeType() = repository.changeType()

}
