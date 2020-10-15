package com.weather.ui.main

import android.util.Log
import android.widget.Toast
import androidx.core.content.edit
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.weather.MainActivity
import com.weather.MyApplication
import com.weather.data.WeatherRepository
import com.weather.data.model.weather.Data
import com.weather.data.network.WeatherNetWork
import com.weather.ui.main.WeatherFragment.Companion.toUpdate
import com.weather.util.*
import interfaces.heweather.com.interfacesmodule.bean.base.Code
import interfaces.heweather.com.interfacesmodule.bean.base.Lang
import interfaces.heweather.com.interfacesmodule.bean.base.Unit
import interfaces.heweather.com.interfacesmodule.bean.weather.WeatherDailyBean
import interfaces.heweather.com.interfacesmodule.bean.weather.WeatherNowBean
import interfaces.heweather.com.interfacesmodule.view.HeWeather
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*


class WeatherViewModel(
    private val repository: WeatherRepository
) : ViewModel() {

    var weather = MutableLiveData<WeatherDailyBean>()
    var nowWeather = MutableLiveData<WeatherNowBean>()
    var isRefresh = MutableLiveData<Boolean>()
    var changeStyle = MutableLiveData<Int>()
    var changeNl = MutableLiveData<Boolean>()

    private val unit = Unit.METRIC

    companion object {
        lateinit var weatherTemp: WeatherDailyBean
        lateinit var nowWeatherTemp: WeatherNowBean
        lateinit var today: Data
        var nowTime: Long = 0

    }

    //获取当前城市7天天气
    private fun getWeather(city: String) {
        viewModelScope.launch {
            val lastApiUpdateTime = (MainActivity.sp
                .getString(Constant.LAST_API_UPDATE_TIME, "2000-01-01 00:00:00")
                ?: "2000-01-01 00:00:00").formatDate()

            //更新判断：强制更新or数据为空or距离上次更新时间大于多少？
            if (isUpdate(lastApiUpdateTime) || weather.value == null || toUpdate) {
                //输入校验
                if (WeatherUtil.checkCity(city) != "0") {
                    Log.i("city", "上次接口数据更新时间：${lastApiUpdateTime}, 正在获取${city}数据...")
                    HeWeather.getWeather7D(MyApplication.context,
                        WeatherUtil.checkCity(city),
                        Lang.ZH_HANS,
                        unit,
                        object : HeWeather.OnResultWeatherDailyListener {
                            override fun onError(p0: Throwable?) {
                                toUpdate = false
                                isRefresh.postValue(false)
                            }

                            override fun onSuccess(p0: WeatherDailyBean?) {
                                if (Code.OK.code.equals(p0?.code, ignoreCase = true)) {
                                    //记录接口上次数据更新时间
                                    MainActivity.sp.edit {
                                        putString(
                                            Constant.LAST_API_UPDATE_TIME,
                                            p0?.basic!!.updateTime
                                        )
                                    }
                                    Log.i("city", "接口数据更新时间：${lastApiUpdateTime}, 已获取${city}数据!")
                                    RainFilterUtil.getRainInfo(p0)
                                    weatherTemp = p0!!
                                    weather.postValue(p0)
                                } else {
                                    //在此查看返回数据失败的原因
                                    val status: String = p0?.code!!
                                    WeatherUtil.toastError(status)
                                }
                                toUpdate = false
                                isRefresh.postValue(false)
                            }
                        }
                    )
                }
            }

        }
    }

    //获取当前城市实时天气
    private fun getNowWeather(city: String) {
        viewModelScope.launch {
            HeWeather.getWeatherNow(
                MyApplication.context,
                WeatherUtil.checkCity(city),
                Lang.ZH_HANS,
                unit,
                object : HeWeather.OnResultWeatherNowListener {
                    override fun onError(p0: Throwable?) {

                    }

                    override fun onSuccess(p0: WeatherNowBean?) {
                        if (Code.OK.code.equals(p0?.code, ignoreCase = true)) {
                            nowWeatherTemp = p0!!
                            nowWeather.postValue(p0)
                        } else {
                            //在此查看返回数据失败的原因
                            val status: String = p0?.code!!
                            WeatherUtil.toastError(status)
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

    fun getWidgetWeather(city: String) {
        getWeather(city)
    }

    //接口是否更新判断
    private fun isUpdate(apiUpdateTime: String): Boolean {
        val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        val date = simpleDateFormat.parse(apiUpdateTime)
        nowTime = System.currentTimeMillis()
        return nowTime - date.time > 60 * 60 * 60 * 1000
    }

}
