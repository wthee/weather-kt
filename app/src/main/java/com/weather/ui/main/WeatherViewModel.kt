package com.weather.ui.main

import android.util.Log
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.weather.MyApplication
import com.weather.data.WeatherRepository
import com.weather.data.model.weather.Data
import com.weather.data.model.weather.NowWeather
import com.weather.data.network.WeatherNetWork
import com.weather.ui.main.WeatherFragment.Companion.toUpdate
import com.weather.util.GetAllCity
import com.weather.util.RainFilterUtil
import com.weather.util.formatDate
import interfaces.heweather.com.interfacesmodule.bean.base.Code
import interfaces.heweather.com.interfacesmodule.bean.base.Lang
import interfaces.heweather.com.interfacesmodule.bean.base.Unit
import interfaces.heweather.com.interfacesmodule.bean.weather.WeatherDailyBean
import interfaces.heweather.com.interfacesmodule.bean.weather.WeatherNowBean
import interfaces.heweather.com.interfacesmodule.view.HeWeather
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat


class WeatherViewModel(
    private val repository: WeatherRepository
) : ViewModel() {

    var weather = MutableLiveData<WeatherDailyBean>()
    var nowWeather = MutableLiveData<WeatherNowBean>()
    var isRefresh = MutableLiveData<Boolean>()

    private val unit = Unit.METRIC

    companion object {
        lateinit var weatherTemp: WeatherDailyBean
        lateinit var nowWeatherTemp: WeatherNowBean
        lateinit var today: Data
        var lastUpdateTime: Long = 0
        var lastApiUpdateTime: String = "2000-01-01 00:00:00"
        var nowTime: Long = 0
    }


    private fun getWeather(city: String) {
        viewModelScope.launch {
            lastApiUpdateTime = (
                    if (weather.value != null && Companion::weatherTemp.isLateinit)
                        weatherTemp.basic.updateTime
                    else
                        lastApiUpdateTime
                    ).formatDate()
            //更新判断
            if (isUpdate(lastApiUpdateTime) || weather.value == null || toUpdate) {
                //输入校验
                if (checkCity(city) != "0") {
                    HeWeather.getWeather7D(MyApplication.context,
                        checkCity(city),
                        Lang.ZH_HANS,
                        unit,
                        object : HeWeather.OnResultWeatherDailyListener {
                            override fun onError(p0: Throwable?) {

                            }

                            override fun onSuccess(p0: WeatherDailyBean?) {
                                if (Code.OK.code.equals(p0?.code, ignoreCase = true)) {
                                    lastUpdateTime = nowTime
                                    RainFilterUtil.getRainInfo(p0)
                                    weatherTemp = p0!!
                                    weather.postValue(p0)
                                } else {
                                    //在此查看返回数据失败的原因
                                    val status: String = p0?.code!!
                                    val code = Code.toEnum(status)
                                    Log.i("log", "failed code: $code")
                                }
                            }
                        }
                    )
                }
            }
            toUpdate = false
            isRefresh.postValue(false)
        }
    }

    private fun getNowWeather(city: String) {
        viewModelScope.launch {
            HeWeather.getWeatherNow(
                MyApplication.context,
                checkCity(city),
                Lang.ZH_HANS,
                unit,
                object : HeWeather.OnResultWeatherNowListener{
                    override fun onError(p0: Throwable?) {

                    }

                    override fun onSuccess(p0: WeatherNowBean?) {
                        if (Code.OK.code.equals(p0?.code, ignoreCase = true)) {
                            nowWeatherTemp = p0!!
                            nowWeather.postValue(p0)
                        } else {
                            //在此查看返回数据失败的原因
                            val status: String = p0?.code!!
                            val code = Code.toEnum(status)
                            Log.i("log", "failed code: $code")
                        }
                    }
                }
            )
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

    fun checkCity(city: String): String {
        questions.forEachIndexed { index, it ->
            if (city.toLowerCase() == it.toLowerCase()) {
                Toast.makeText(MyApplication.context, answers[index], Toast.LENGTH_LONG).show()
            }
        }
        if (city.length > 1) {
            GetAllCity.getInstance().citys.forEach {
                if (city == it.cityZh) {
                    return "CN" + it.id
                }
            }
            if (city == "杭州") {
                //TODO 经纬
                return "CN101010100"
            }
        }
        return "0"
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
