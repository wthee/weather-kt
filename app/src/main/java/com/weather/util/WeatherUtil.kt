package com.weather.util

import android.widget.Toast
import androidx.core.content.edit
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.weather.MainActivity
import com.weather.MainActivity.Companion.answers
import com.weather.MainActivity.Companion.questions
import com.weather.MyApplication
import com.weather.data.model.SunMoonData
import interfaces.heweather.com.interfacesmodule.bean.base.Code
import interfaces.heweather.com.interfacesmodule.bean.weather.WeatherDailyBean
import java.text.SimpleDateFormat
import java.util.*

object WeatherUtil {

    //获取当前显示城市名
    fun getCity() = getAllCity()[getCityIndex()]

    //获取所有城市
    fun getAllCity(): ArrayList<String> {
        val citys: ArrayList<String> = Gson().fromJson(
            MainActivity.sp.getString(Constant.CITYS, Constant.CITYS_DEFAULT),
            object : TypeToken<ArrayList<String>>() {}.type
        )
        return if (citys.size == 0) Constant.defaultCitys else citys
    }

    //修改当前城市名
    fun setCity(city: String) {
        getAllCity().apply {
            this[getCityIndex()] = city
            MainActivity.sp.edit {
                putString(Constant.CITYS, this@apply.ListoJson())
            }
        }
    }

    //删除城市名
    fun deleteCity(city: String) {
        getAllCity().apply {
            this.remove(city)
            MainActivity.sp.edit {
                putString(Constant.CITYS, this@apply.ListoJson())
                putInt(Constant.CITY_INDEX, 0)
            }
        }
    }

    //添加城市名
    fun addCity(city: String) {
        getAllCity().apply {
            this.add(city)
            MainActivity.sp.edit {
                putInt(Constant.CITY_INDEX, this@apply.size - 1)
                putString(Constant.CITYS, this@apply.ListoJson())
            }
        }
    }


    //获取城市下标
    fun getCityIndex() = MainActivity.sp.getInt(Constant.CITY_INDEX, 0)


    //TODO 优化 https://dev.heweather.com/docs/start/icons
    fun formatTip(dailyBean: WeatherDailyBean.DailyBean) =
        when (dailyBean.textDay.length) {
            1 -> "下雨天，记得带伞"
            2 -> when (dailyBean.textDay) {
                "小雨" -> "雨虽小，注意别感冒"
                "中雨" -> "记得随身携带雨伞"
                "大雨" -> "出门最好穿雨衣"
                "阵雨" -> "阵雨来袭，记得带伞"
                "暴雨" -> "尽量避免户外活动"
                else -> "没有你的天气"
            }
            3 -> {
                if (dailyBean.textDay.contains("转"))
                    "天气多变，照顾好自己"
                else
                    when (dailyBean.textDay) {
                        "雷阵雨" -> "尽量减少户外活动"
                        "大暴雨" -> "尽量避免户外活动"
                        "雨夹雪" -> "道路湿滑，出行要谨慎"
                        else -> "没有你的天气"
                    }
            }
            else -> "天气多变，照顾好自己"
        }

    /**
     * 根据日期获取 星期
     * @param datetime
     * @return
     */
    fun dateToWeek(datetime: String): String {
        val f = SimpleDateFormat("yyyy-MM-dd")
        val weekDays = arrayOf("星期日", "星期一", "星期二", "星期三", "星期四", "星期五", "星期六")
        val cal: Calendar = Calendar.getInstance()
        val date = f.parse(datetime)
        cal.time = date
        //一周的第几天
        var w: Int = cal.get(Calendar.DAY_OF_WEEK) - 1
        if (w < 0) w = 0
        return weekDays[w]
    }

    //检查城市
    fun checkCity(city: String): String {
        questions.forEachIndexed { index, it ->
            if (city.toLowerCase(Locale.ROOT) == it.toLowerCase(Locale.ROOT)) {
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

    //返回接口错误信息
    fun toastError(status: String) {
        val code = Code.toEnum(status)
        Toast.makeText(
            MyApplication.context,
            code.txt,
            Toast.LENGTH_SHORT
        ).show()
    }


}

//2020-10-13T21:35+08:00
fun String.formatDate(): String {
    if (this.length == 19)
        return this
    else
        return this.replace("T", " ").substring(0, this.lastIndexOf("+")) + ":00"

}

fun WeatherDailyBean.getSunDate(): ArrayList<SunMoonData> {
    val sunMoonDatas = arrayListOf<SunMoonData>()
    this.daily.forEach {
        sunMoonDatas.add(
            SunMoonData(
                it.fxDate,
                it.sunrise,
                it.sunset,
                it.moonRise,
                it.moonSet,
                it.moonPhase
            )
        )
    }
    return sunMoonDatas
}