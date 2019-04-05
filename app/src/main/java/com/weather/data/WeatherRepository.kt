package com.weather.data

class WeatherRepository(
        private val netWork: WeatherNetWork
) {
    fun getCity(city: String) = netWork.getCity(city)
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