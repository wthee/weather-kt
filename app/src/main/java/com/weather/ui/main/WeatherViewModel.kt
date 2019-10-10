package com.weather.ui.main

import android.util.Log
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.weather.MyApplication
import com.weather.data.WeatherRepository
import com.weather.data.model.Data
import com.weather.data.model.Weather
import com.weather.data.network.WeatherNetWork
import com.weather.ui.main.WeatherFragment.Companion.toUpdate
import com.weather.util.GetAllCity
import com.weather.util.RainFilterUtil
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import android.icu.lang.UCharacter.GraphemeClusterBreak.T
import com.weather.data.model.Hour
import kotlin.math.log


class WeatherViewModel(
    private val repository: WeatherRepository
) : ViewModel() {

    var weather = MutableLiveData<Weather>()
    var isRefresh = MutableLiveData<Boolean>()

    companion object {
        lateinit var weatherTemp: Weather
        lateinit var today: Data
        lateinit var now: Hour
        var lastUpdateTime: Long = 0
        var nowTime: Long = 0
    }


    private fun getWeather(city: String) {
        viewModelScope.launch {
            when (checkCity(city)) {
                1 -> {
                    weatherTemp = formatWeather(repository.getWeather(mutableMapOf("city" to city)))
                }
                0 -> {
                    weatherTemp = formatWeather(repository.getWeather(mutableMapOf("ip" to "")))
                }
            }
            if(isUpdate(weatherTemp.update_time) || weather.value == null || toUpdate){
                weather.postValue(weatherTemp)
            }
            toUpdate = false
            isRefresh.postValue(false)
        }
    }

    fun changeCity(city: String) {
        isRefresh.postValue(false)
        getWeather(city)
    }

    fun changeType() {
        weather.postValue(weatherTemp)
    }

    fun checkCity(city: String): Int {
        questions.forEachIndexed { index, it ->
            if (city.toLowerCase() == it.toLowerCase()) {
                Toast.makeText(MyApplication.context, answers[index], Toast.LENGTH_LONG).show()
            }
        }
        if (city.length > 1) {
            GetAllCity.getInstance().citys.forEach {
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

    private suspend fun formatWeather(weatherTemp: Weather): Weather {
        today = weatherTemp.data[0]
        viewModelScope.launch {
            if (today.air == "0") {
                var leader = " "
                GetAllCity.getInstance().citys.forEach {
                    if (weatherTemp.city == it.cityZh) {
                        leader = it.leaderZh
                        return@forEach
                    }
                }
                var wea = repository.getWeather(mutableMapOf("version" to "v9", "city" to leader))
                today.air = wea.data[0].air
                today.air_level = wea.data[0].air_level
                today.air_tips = wea.data[0].air_tips
            }
        }
        RainFilterUtil.getRainInfo(weatherTemp)
        //当前时间的天气
        today.hours.forEachIndexed { index, it ->
            val hour = it.hours.substring(0,2).toInt()
            val nowHour = Calendar.getInstance(TimeZone.getTimeZone("GMT+8:00")).get(Calendar.HOUR_OF_DAY)
            val evenHour = if(nowHour % 2 == 1) (nowHour + 1) % 24 else nowHour
            if( hour == evenHour){
                now = today.hours[index].copy()
                now.tem += "℃"
            }
        }
        return weatherTemp
    }

    //若上次更新时间距离现在大于1分钟，则更新数据
    private fun isUpdate(updateTime: String): Boolean{
        val format = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        nowTime = System.currentTimeMillis()

        val update =  nowTime - format.parse(updateTime).time  > 30 * 60 * 1000
                && nowTime - lastUpdateTime > 1 * 60 * 1000
        lastUpdateTime = nowTime
        return update
    }

    init {
        viewModelScope.launch{
            val qas = WeatherNetWork.getInstance().fetchQa()
            qas.forEach{
                questions.add(it.question)
                answers.add(it.answer)
            }
        }
    }

    //彩蛋？？？
    private var questions = arrayListOf<String>()
    private var answers = arrayListOf<String>()
}
