package com.weather.ui.main

import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.weather.MyApplication
import com.weather.data.WeatherRepository
import com.weather.data.model.weather.Data
import com.weather.data.model.weather.NowWeather
import com.weather.data.model.weather.Weather
import com.weather.data.network.WeatherNetWork
import com.weather.ui.main.WeatherFragment.Companion.toUpdate
import com.weather.util.GetAllCity
import com.weather.util.RainFilterUtil
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat


class WeatherViewModel(
    private val repository: WeatherRepository
) : ViewModel() {

    var weather = MutableLiveData<Weather>()
    var nowWeather = MutableLiveData<NowWeather>()
    var isRefresh = MutableLiveData<Boolean>()

    companion object {
        lateinit var weatherTemp: Weather
        lateinit var nowWeatherTemp: NowWeather
        lateinit var today: Data
        var lastUpdateTime: Long = 0
        var lastApiUpdateTime: String = "2000-01-01 00:00:00"
        var nowTime: Long = 0
    }


    private fun getWeather(city: String) {
        viewModelScope.launch {
            lastApiUpdateTime =
                if (weather.value != null) weatherTemp.update_time else lastApiUpdateTime
            if (isUpdate(lastApiUpdateTime) || weather.value == null || toUpdate) {
                when (checkCity(city)) {
                    1 -> {
                        weatherTemp =
                            formatWeather(repository.getWeather(mutableMapOf("city" to city)))
                    }
                    0 -> {
                        weatherTemp = formatWeather(repository.getWeather(mutableMapOf("ip" to "")))
                    }
                }
                lastUpdateTime = nowTime
                weather.postValue(weatherTemp)
            }
            toUpdate = false
            isRefresh.postValue(false)
        }
    }

    private fun getNowWeather(city: String) {
        viewModelScope.launch {
            when (checkCity(city)) {
                1 -> {
                    nowWeatherTemp = repository.getNowWeather(mutableMapOf("city" to city))
                }
                0 -> {
                    nowWeatherTemp = repository.getNowWeather(mutableMapOf("ip" to ""))
                }
            }
            nowWeather.postValue(formatNowWeather(nowWeatherTemp, city))
        }
    }

    //获取当前城市的天气信息
    fun changeCity(city: String) {
        getWeather(city)
        getNowWeather(city)
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
        return weatherTemp
    }

    private fun formatNowWeather(nowWeatherTemp: NowWeather, city: String): NowWeather {
        nowWeatherTemp.tem = nowWeatherTemp.tem + "℃"
        nowWeatherTemp.city = city
        return nowWeatherTemp
    }

    private fun isUpdate(updateTime: String): Boolean {
        val format = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        nowTime = System.currentTimeMillis()

        return (nowTime - (format.parse(updateTime).time - 8 * 60 * 60 * 1000) > 30 * 60 * 1000
                && nowTime - lastUpdateTime > 1 * 60 * 1000)
    }

    init {
        viewModelScope.launch {
            val qas = WeatherNetWork.getInstance().fetchQa()
            qas.forEach {
                questions.add(it.question)
                answers.add(it.answer)
            }
        }
    }

    //彩蛋？？？
    private var questions = arrayListOf<String>()
    private var answers = arrayListOf<String>()
}
