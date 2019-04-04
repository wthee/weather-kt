package cn.wthee.highnoon.util

import cn.wthee.withoutyou.data.WeatherNetWork
import cn.wthee.withoutyou.data.WeatherRepository
import cn.wthee.withoutyou.ui.main.WeatherFactory


object InjectorUtil {

    private fun getWeatherRepository() = WeatherRepository.getInstance(WeatherNetWork.getInstance())
    fun getWeatherViewModelFactory(city: String) = WeatherFactory(getWeatherRepository(),city)

}