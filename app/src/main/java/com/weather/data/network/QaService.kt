package com.weather.data.network

import com.weather.data.model.QA
import retrofit2.http.GET
import retrofit2.http.POST

interface QaService {

    @POST("qa")
    suspend fun getAllQa():List<QA>

}