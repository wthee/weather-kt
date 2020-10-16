package com.weather.data.network

import com.weather.data.model.QA

class WeatherNetWork {

    private val qaService = ServiceCreator.create(QaService::class.java)


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