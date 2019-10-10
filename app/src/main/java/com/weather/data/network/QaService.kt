package com.weather.data.network

import com.weather.data.model.QA
import retrofit2.http.GET

interface QaService {

    @GET("qa")
    suspend fun getAllQa():List<QA>

}