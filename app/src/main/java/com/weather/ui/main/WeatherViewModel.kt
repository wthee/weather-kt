package com.weather.ui.main

import android.util.Log
import android.widget.Toast
import androidx.core.content.edit
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.weather.MainActivity
import com.weather.MyApplication
import com.weather.ui.main.WeatherFragment.Companion.sunMoonDatas
import com.weather.ui.main.WeatherFragment.Companion.toUpdate
import com.weather.util.Constant
import com.weather.util.WeatherUtil
import com.weather.util.formatDate
import com.weather.util.getSunDate
import interfaces.heweather.com.interfacesmodule.bean.base.Code
import interfaces.heweather.com.interfacesmodule.bean.weather.WeatherDailyBean
import interfaces.heweather.com.interfacesmodule.bean.weather.WeatherNowBean
import interfaces.heweather.com.interfacesmodule.view.HeWeather
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat


class WeatherViewModel() : ViewModel() {

    var weather = MutableLiveData<WeatherDailyBean>()
    var nowWeather = MutableLiveData<WeatherNowBean>()
    var isRefresh = MutableLiveData<Boolean>()
    var changeStyle = MutableLiveData<Int>()
    var changeNl = MutableLiveData<Boolean>()


    companion object {
        lateinit var weatherTemp: WeatherDailyBean
        lateinit var nowWeatherTemp: WeatherNowBean
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
                    HeWeather.getWeather15D(MyApplication.context,
                        WeatherUtil.checkCity(city),
                        object : HeWeather.OnResultWeatherDailyListener {
                            override fun onError(p0: Throwable?) {
                                toUpdate = false
                                isRefresh.postValue(false)
                                Toast.makeText(
                                    MyApplication.context,
                                    "获取数据失败，请检查网络后重试~",
                                    Toast.LENGTH_SHORT
                                ).show()
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
                                    sunMoonDatas = p0?.getSunDate() ?: ArrayList()
                                    WeatherUtil.getRainInfo(p0)
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
