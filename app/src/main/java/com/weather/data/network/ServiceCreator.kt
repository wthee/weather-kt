package com.weather.data.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


object ServiceCreator {

    private const val QA_URL = "http://47.98.60.104:8080/api/"


    fun <T> create(serviceClass: Class<T>): T {
        val builder = Retrofit.Builder()
            .baseUrl(QA_URL)
            .addConverterFactory(GsonConverterFactory.create())

        return builder.build().create(serviceClass)
    }

}