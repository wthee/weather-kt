package com.weather.data

class WeatherRepository(
    private val netWork: WeatherNetWork
) {
    fun getWeather(city: String) = netWork.getWeather(city)
    fun getNowWeather(city: String) = netWork.getNowWeather(city)
    fun changeCity(city: String) = netWork.changeCity(city)
    fun changeType() = netWork.changeType()
    fun checkCity(city: String) = netWork.checkCity(city)

    companion object {
        @Volatile
        private var instance: WeatherRepository? = null

        fun getInstance(netWork: WeatherNetWork) = instance
            ?: synchronized(this) {
                instance ?: WeatherRepository(netWork).also { instance = it }
            }
    }
}