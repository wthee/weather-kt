package com.weather.data.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ServiceCreator {

    const val BASE_URL = "https://www.tianqiapi.com/api/"


    private val builder = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())

    private val retrofit = builder.build()

    fun <T> create(serviceClass: Class<T>): T = retrofit.create(serviceClass)

}