package com.weather.viewmodels

import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.weather.MyApplication
import com.weather.data.*
import com.weather.util.RainUtil
import kotlinx.coroutines.launch

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


    fun getWeather(city: String) {
        viewModelScope.launch {
            when (checkCity(city)) {
                1 -> {
                    weatherTemp = repository.getWeather(mapOf("version" to "v1", "city" to city))
                    weather.postValue(formatWeather (weatherTemp))
                }
                0 -> {
                    weatherTemp = repository.getWeather( mapOf("version" to "v1", "ip" to ""))
                    weather.postValue(formatWeather (weatherTemp))
                }
            }

        }
    }

    fun getNowWeather(city: String){
        viewModelScope.launch {
            when (checkCity(city)) {
                1 -> {
                    nowWeatherTemp = repository.getNowWeather(mapOf("version" to "v6", "city" to city))
                    nowWeather.postValue(formatNowWeather (nowWeatherTemp))
                }
                0 -> {
                    nowWeatherTemp = repository.getNowWeather( mapOf("version" to "v6", "ip" to ""))
                    nowWeather.postValue(formatNowWeather (nowWeatherTemp))
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

    fun checkCity(city: String): Int {
        myUser.forEachIndexed { index, it ->
            if (city == it) {
                Toast.makeText(MyApplication.context, showResult[index], Toast.LENGTH_LONG).show()
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

    private fun getLeaderWeather(city: String, getData: IGetData) {
        viewModelScope.launch {
            var leader = " "
            GetAllCity.getInstance().citys.forEach {
                if(city == it.cityZh){
                    leader =  it.leaderZh
                    return@forEach
                }
                var temp = repository.getWeather(mapOf("version" to "v1", "city" to leader))
                getData.get(temp)
            }
        }
    }


    private fun formatWeather(weatherTemp: Weather): Weather {
        today = weatherTemp.data[0]
        if (WeatherViewModel.today.air == 0) {
            getLeaderWeather(weatherTemp.city, object : IGetData {
                override fun get(wea: Weather) {
                    today.air = wea.data[0].air
                    today.air_level = wea.data[0].air_level
                    today.air_tips = wea.data[0].air_tips
                }

            })
        }else{
        }
        RainUtil.getRainInfo(weatherTemp)
        return weatherTemp
    }

    private fun formatNowWeather(nowWeatherTemp: NowWeather): NowWeather {
        nowWeatherTemp.tem = nowWeatherTemp.tem + "℃"
        return nowWeatherTemp
    }


    interface IGetData {
        fun get(wea: Weather)
    }

    //彩蛋？？？
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
