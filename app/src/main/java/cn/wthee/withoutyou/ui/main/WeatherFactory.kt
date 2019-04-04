package cn.wthee.withoutyou.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import cn.wthee.withoutyou.data.WeatherRepository

class WeatherFactory(
    private var repository: WeatherRepository,
    private var city: String
): ViewModelProvider.NewInstanceFactory() {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return WeatherViewModel(repository,city) as T
    }
}