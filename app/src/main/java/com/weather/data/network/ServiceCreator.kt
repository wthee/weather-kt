package com.weather.data.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


object ServiceCreator {

    private const val WEATHER_URL = "https://www.tianqiapi.com/api/"
    private const val QA_URL = "http://47.98.60.104:8080/api/"


    fun <T> create(serviceClass: Class<T>, type: Int): T {
        val url = when(type){
            0 -> WEATHER_URL
            1 -> QA_URL
            else -> ""
        }

        val builder = Retrofit.Builder()
            .baseUrl(url)
            .addConverterFactory(GsonConverterFactory.create())

        return builder.build().create(serviceClass)
    }

}