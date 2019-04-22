package com.weather.data.network

import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Build
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.weather.MyApplication
import com.weather.util.ActivityUtil
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.weather.data.City
import com.weather.data.Data
import com.weather.data.NowWeather
import com.weather.data.Weather
import com.weather.data.network.api.WeatherService
import com.weather.util.LunarUtil
import kotlinx.coroutines.*
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.http.*
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*
import javax.net.ssl.HttpsURLConnection
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine


class WeatherNetWork {

    private val weatherService = ServiceCreator.create(WeatherService::class.java)

    suspend fun fetchWeather(map:Map<String,String>) = weatherService.reqGetWea(map).await()

    suspend fun fetchNowWeather(map:Map<String,String>) = weatherService.reqGetNowWea(map).await()

    private suspend fun <T> Call<T>.await(): T {
        return suspendCoroutine { continuation ->
            enqueue(object : Callback<T> {
                override fun onFailure(call: Call<T>, t: Throwable) {
                    continuation.resumeWithException(t)
                }

                override fun onResponse(call: Call<T>, response: Response<T>) {
                    val body = response.body()
                    if (body != null) continuation.resume(body)
                    else continuation.resumeWithException(RuntimeException("response body is null"))
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