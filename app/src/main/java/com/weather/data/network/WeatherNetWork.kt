package com.weather.data.network

import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import com.weather.MyApplication
import com.weather.data.model.weather.NowWeather
import com.weather.data.model.weather.Weather
import kotlinx.coroutines.suspendCancellableCoroutine
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class WeatherNetWork {

    private val weatherService = ServiceCreator.create(WeatherService::class.java)

    suspend fun fetchWeather(map:MutableMap<String,String>):Weather{
        addAppKey(map)
        return weatherService.getWeather(map).await()
    }
    suspend fun fetchNowWeather(map:MutableMap<String,String>):NowWeather{
        addAppKey(map)
        return weatherService.getNowWeather(map).await()
    }

    private suspend fun <T> Call<T>.await(): T {
        return suspendCancellableCoroutine { continuation ->
            enqueue(object : Callback<T> {
                override fun onFailure(call: Call<T>, t: Throwable) {
                    Toast.makeText(MyApplication.context,"网络未连接~",Toast.LENGTH_SHORT).show()
                }

                override fun onResponse(call: Call<T>, response: Response<T>) {
                    val body = response.body()
                    if (body != null) continuation.resume(body)
                    else continuation.resumeWithException(RuntimeException("body is null"))
                }
            })
        }
    }

    fun addAppKey(map: MutableMap<String, String>){
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