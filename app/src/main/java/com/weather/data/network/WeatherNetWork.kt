package com.weather.data.network

import android.util.Log
import android.widget.Toast
import com.weather.MyApplication
import kotlinx.coroutines.suspendCancellableCoroutine
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class WeatherNetWork {

    private val weatherService = ServiceCreator.create(WeatherService::class.java)

    suspend fun fetchWeather(map:Map<String,String>) = weatherService.reqGetWea(map).await()
    suspend fun fetchNowWeather(map:Map<String,String>) = weatherService.reqGetNowWea(map).await()

    private suspend fun <T> Call<T>.await(): T {
        return suspendCancellableCoroutine { continuation ->
            enqueue(object : Callback<T> {
                override fun onFailure(call: Call<T>, t: Throwable) {
                    Toast.makeText(MyApplication.context,"网络未连接~",Toast.LENGTH_SHORT).show()
                }

                override fun onResponse(call: Call<T>, response: Response<T>) {
                    Log.e("widget","response")
                    val body = response.body()
                    if (body != null) {
                        continuation.resume(body)
                    } else {
                        continuation.resumeWithException(RuntimeException("body is null"))
                    }
                }
            })
        }
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