package com.weather.ui.info

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
import interfaces.heweather.com.interfacesmodule.bean.WeatherGridBean
import interfaces.heweather.com.interfacesmodule.bean.base.Code
import interfaces.heweather.com.interfacesmodule.bean.base.Lang
import interfaces.heweather.com.interfacesmodule.bean.base.Unit
import interfaces.heweather.com.interfacesmodule.bean.weather.WeatherDailyBean
import interfaces.heweather.com.interfacesmodule.bean.weather.WeatherHourlyBean
import interfaces.heweather.com.interfacesmodule.bean.weather.WeatherNowBean
import interfaces.heweather.com.interfacesmodule.view.HeWeather
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*


class WeatherInfoViewModel() : ViewModel() {


    val hourlyInfos = MutableLiveData<WeatherHourlyBean>()
    private val unit = Unit.METRIC


    //获取当前城市7天天气
    fun getHourlyWeather(city: String) {
        HeWeather.getWeather168Hourly(MyApplication.context,
            WeatherUtil.checkCity(city),
            Lang.ZH_HANS,
            unit,
            object : HeWeather.OnResultWeatherHourlyListener {
                override fun onError(p0: Throwable?) {
                }

                override fun onSuccess(p0: WeatherHourlyBean?) {
                    if (Code.OK.code.equals(p0?.code, ignoreCase = true)) {
                        //记录接口上次数据更新时间
                        hourlyInfos.postValue(p0)
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
