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
        @Volatile private var instant: WeatherRepository? = null

        fun getInstance(netWork: WeatherNetWork) = instant
            ?: synchronized(this){
            instant
                ?: WeatherRepository(netWork).also { instant = it }
        }
    }
}