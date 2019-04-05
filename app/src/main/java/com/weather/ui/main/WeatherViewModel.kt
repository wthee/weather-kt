package com.weather.ui.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.weather.data.Weather
import com.weather.data.WeatherRepository

class WeatherViewModel(
    private val repository: WeatherRepository,
    private val city: String
) : ViewModel() {
    var weather: LiveData<Weather> = repository.getCity(city)

    fun changeCity(city: String) = repository.getCity(city)
    fun checkCity(city: String) = repository.checkCity(city)
    fun changeType() = repository.changeType()

}
