package com.weather.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class WeatherFactory() : ViewModelProvider.NewInstanceFactory() {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return WeatherViewModel() as T
    }
}