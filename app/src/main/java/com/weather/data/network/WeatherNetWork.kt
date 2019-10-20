package com.weather.data.network

import com.weather.data.model.QA
import com.weather.data.model.weather.NowWeather
import com.weather.data.model.weather.Weather

class WeatherNetWork {

    private val weatherService = ServiceCreator.create(WeatherService::class.java,0)
    private val qaService = ServiceCreator.create(QaService::class.java,1)

    suspend fun fetchWeather(map:MutableMap<String,String>): Weather {
        addAppKey(map,"v9")
        return weatherService.getWeather(map)
    }
    suspend fun fetchNowWeather(map:MutableMap<String,String>): NowWeather {
        addAppKey(map,"v6")
        return weatherService.getNowWeather(map)
    }

    suspend fun fetchQa():List<QA>{
        return qaService.getAllQa()
    }


    private fun addAppKey(map: MutableMap<String, String>, version: String){
        map["version"] = version
        map["appid"] = "23238842"
        map["appsecret"] = "QgdXYe3r"
    }

    companion object {

        @Volatile
        private var instance: WeatherNetWork? = null

        fun getInstance() = instance ?: synchronized(this) {
            instance
                ?: WeatherNetWork().also { instance = it }
        }
    }
}