package com.weather.ui.info

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class WeatherInfoFactory(): ViewModelProvider.NewInstanceFactory() {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return WeatherInfoViewModel() as T
    }
}