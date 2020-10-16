package com.weather.ui.info

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.weather.MyApplication
import com.weather.util.WeatherUtil
import interfaces.heweather.com.interfacesmodule.bean.WarningBean
import interfaces.heweather.com.interfacesmodule.bean.air.AirDailyBean
import interfaces.heweather.com.interfacesmodule.bean.base.Code
import interfaces.heweather.com.interfacesmodule.bean.base.Lang
import interfaces.heweather.com.interfacesmodule.bean.weather.WeatherHourlyBean
import interfaces.heweather.com.interfacesmodule.view.HeWeather


class WeatherInfoViewModel() : ViewModel() {

    val hourlyInfos = MutableLiveData<WeatherHourlyBean>()
    val warningInfo = MutableLiveData<WarningBean>()
    val airDailyBean = MutableLiveData<AirDailyBean>()

    //获取当前城市7天天气
    fun getHourlyWeather(city: String) {
        HeWeather.getWeather24Hourly(MyApplication.context,
            WeatherUtil.checkCity(city),
            object : HeWeather.OnResultWeatherHourlyListener {
                override fun onError(p0: Throwable?) {
                }

                override fun onSuccess(p0: WeatherHourlyBean?) {
                    if (Code.OK.code.equals(p0?.code, ignoreCase = true)) {
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

    //获取预警消息
    fun getWarning(city: String) {
        HeWeather.getWarning(MyApplication.context,
            WeatherUtil.checkCity(city),
            object : HeWeather.OnResultWarningListener {
                override fun onError(p0: Throwable?) {
                }

                override fun onSuccess(p0: WarningBean?) {
                    if (Code.OK.code.equals(p0?.code, ignoreCase = true)) {
                        warningInfo.postValue(p0)
                    } else {
                        //在此查看返回数据失败的原因
                        val status: String = p0?.code!!
                        WeatherUtil.toastError(status)
                    }
                }
            }
        )
    }

    //获取空气质量
    fun getAirInfo(city: String) {
        HeWeather.getAir5D(MyApplication.context,
            WeatherUtil.checkCity(city),
            Lang.ZH_HANS,
            object : HeWeather.OnResultAirDailyListener {
                override fun onError(p0: Throwable?) {
                }

                override fun onSuccess(p0: AirDailyBean?) {
                    if (Code.OK.code.equals(p0?.code, ignoreCase = true)) {
                        airDailyBean.postValue(p0)
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
