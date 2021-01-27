package com.weather.data.network

import com.weather.data.model.QA

class WeatherNetWork {

    private val qaService = ServiceCreator.create(QaService::class.java)


    suspend fun fetchQa():List<QA>{
        return qaService.getAllQa()
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