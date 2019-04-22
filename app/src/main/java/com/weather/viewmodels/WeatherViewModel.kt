package com.weather.viewmodels

import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.weather.MyApplication
import com.weather.data.*
import com.weather.util.ActivityUtil
import com.weather.util.LunarUtil
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.*

class WeatherViewModel(
    private val repository: WeatherRepository
) : ViewModel() {

    var weather = MutableLiveData<Weather>()
    var nowWeather = MutableLiveData<NowWeather>()

    companion object {
        lateinit var weatherTemp: Weather
        lateinit var nowWeatherTemp: NowWeather
        lateinit var today: Data
    }

    private var citys: List<City>


    init {
        var iS = ActivityUtil.instance.currentActivity!!.resources.assets.open("city.json")
        val listType = object : TypeToken<List<City>>() {}.type
        citys = Gson().fromJson(readStreamToString(iS), listType)
    }


    fun getWeather(city: String) {
        viewModelScope.launch {
            when (checkCity(city)) {
                1 -> {
                    weatherTemp = repository.getWeather(mapOf("version" to "v1", "city" to city))
                    weather.value = formatWeather (weatherTemp)
                }
                0 -> {
                    weatherTemp = repository.getWeather( mapOf("version" to "v1", "ip" to ""))
                    weather.value = formatWeather (weatherTemp)
                }
                else -> {

                }
            }

        }
    }

    fun getNowWeather(city: String){
        viewModelScope.launch {
            when (checkCity(city)) {
                1 -> {
                    nowWeatherTemp = repository.getNowWeather(mapOf("version" to "v6", "city" to city))
                    nowWeather.value = formatNowWeather (nowWeatherTemp)
                }
                0 -> {
                    nowWeatherTemp = repository.getNowWeather( mapOf("version" to "v6", "ip" to ""))
                    nowWeather.value = formatNowWeather (nowWeatherTemp)
                }
                else -> {

                }
            }

        }
    }



    fun changeCity(city: String) {
        getWeather(city)
        getNowWeather(city)
    }

    fun changeType() {
        weather.postValue(weatherTemp)
    }

    private fun formatWeather(weatherTemp: Weather): Weather {
        today = weatherTemp.data[0]
        if (today.air == 0) {
            getLeaderWeather(weatherTemp.city, object : IGetData {
                override fun get(wea: Weather) {
                    today.air = wea.data[0].air
                    today.air_level = wea.data[0].air_level
                    today.air_tips = wea.data[0].air_tips
                }

            })
        }
        weatherTemp.data.toHashSet().forEach {
            if (!it.wea.contains("雨")) {
                weatherTemp.data.remove(it)
            } else {
                it.tems = it.tem2.substring(0, it.tem2.length - 1) + "-" + it.tem1
                var date = it.date.split('-')

                it.y = date[0]
                it.m = date[1]
                it.d = date[2]
                var today = Calendar.getInstance()
                today.time = SimpleDateFormat("yyyy-MM-dd").parse(it.date)
                var nl = LunarUtil(today).toString()
                var start = nl.indexOf("月") + 1
                var end = nl.lastIndex + 1
                it.date_nl = nl.substring(start, end)
                it.tip = when (it.wea.length) {
                    1 -> "下雨天，记得带伞"
                    2 -> when (it.wea) {
                        "小雨" -> "雨虽小，注意别感冒"
                        "中雨" -> "记得随身携带雨伞"
                        "大雨" -> "出门最好穿雨衣"
                        "阵雨" -> "阵雨来袭，记得带伞"
                        "暴雨" -> "尽量避免户外活动"
                        else -> "error"
                    }
                    3 -> {
                        if (it.wea.contains("转"))
                            "天气多变，照顾好自己"
                        else
                            when (it.wea) {
                                "雷阵雨" -> "尽量减少户外活动"
                                "大暴雨" -> "尽量避免户外活动"
                                "雨夹雪" -> "道路湿滑，出行要谨慎"
                                else -> "error"
                            }
                    }
                    else -> "天气多变，照顾好自己"
                }
            }

        }
        return weatherTemp
    }

    private fun formatNowWeather(nowWeatherTemp: NowWeather): NowWeather {
        nowWeatherTemp.tem = nowWeatherTemp.tem + "℃"
        return nowWeatherTemp
    }

    private fun getLeaderWeather(city: String, getData: IGetData) {
        viewModelScope.launch {
            var leader = " "
            citys.forEach {
                if(city == it.cityZh){
                    leader =  it.leaderZh
                    return@forEach
                }
            }
            var temp = repository.getWeather(mapOf("version" to "v1", "city" to leader))
            getData.get(temp)
        }
    }

    interface IGetData {
        fun get(wea: Weather)
    }


    fun checkCity(city: String): Int {
        myUser.forEachIndexed { index, it ->
            if (city == it) {
                Toast.makeText(MyApplication.context, showResult[index], Toast.LENGTH_LONG).show()
            }
        }
        if (city.length > 1) {
            citys.forEach {
                if (city == it.cityZh) {
                    return 1
                }
            }
            if (city == "ip") {
                return 0
            }
        }
        return -1
    }



    @Throws(IOException::class)
    private fun readStreamToString(inputStream: InputStream): String {
        val byteArrayOutputStream = ByteArrayOutputStream()
        val buffer = ByteArray(1024)
        var len = inputStream.read(buffer)
        while (len != -1) {
            byteArrayOutputStream.write(buffer, 0, len)
            len = inputStream.read(buffer)
        }
        val result = byteArrayOutputStream.toString()
        inputStream.close()
        byteArrayOutputStream.close()
        return result
    }

    private var myUser = arrayListOf(
        "隐约雷鸣 阴霾天空 但盼风雨来 能留你在此",
        "WLQ",
        "wthee",
        "随便取个昵称",
        "木木木汐",
        "荻花題葉",
        "桃花太红李太白"
    )
    private var showResult = arrayListOf(
        "隐约雷鸣 阴霾天空\n即使天无雨 我亦留此地",
        "没有你的天气",
        "你好！我是这款app的作者wthee",
        "缘起，在人群中，我看见你！\n缘灭，我看见你，在人群中！",
        "没有你的街道，尽是寂寥；\n没有你的时光，近似毒药。",
        "我所知道关于你的，只有天气了",
        "没有你的酷安，都是基佬"
    )
}
