package com.weather.data

import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Build
import android.text.format.DateUtils
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.weather.MyApplication
import com.weather.util.ActivityUtil
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.weather.util.LunarUtil
import okhttp3.*
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*
import javax.net.ssl.HttpsURLConnection



class WeatherNetWork {

    private var myUser = arrayListOf(
        "隐约雷鸣 阴霾天空 但盼风雨来 能留你在此",
        "WLQ",
        "wthee",
        "随便取个昵称",
        "木木木汐",
        "荻花題葉",
        "桃花太红李太白")
    private var showResult = arrayListOf(
        "隐约雷鸣 阴霾天空\n即使天无雨 我亦留此地",
        "没有你的天气",
        "你好！我是这款app的作者wthee",
        "缘起，在人群中，我看见你！\n缘灭，我看见你，在人群中！",
        "没有你的街道，尽是寂寥；\n没有你的时光，近似毒药。",
        "我所知道关于你的，只有天气了",
        "没有你的酷安，都是基佬")
    private var weather = MutableLiveData<Weather>()
    private var urlv2 = "https://www.tianqiapi.com/api/?version=v1&"
    private var cityNames: List<CityName>
    var newUrlv2 = urlv2

    init {
        var iS = ActivityUtil.instance.currentActivity!!.resources.assets.open("city.json")
        val listType = object : TypeToken<List<CityName>>() {}.type
        cityNames = Gson().fromJson(readStreamToString(iS),listType)

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

    fun checkCity(city: String):Boolean{
        myUser.forEachIndexed{index,it ->
            if (city == it) {
                Toast.makeText(MyApplication.context,showResult[index],Toast.LENGTH_LONG).show()
            }
        }
        if (city.length > 1) {
            cityNames.forEach{
                if (city == it.cityZh){
                    newUrlv2= urlv2 + "city=$city"
                    return true
                }
            }
            if (city == "ip") {
                newUrlv2= urlv2 + "ip"
                return true
            }
        }
        return false
    }

    fun changeType(){
        weather.postValue(weatherTemp)
    }

    fun getWeather(city: String): LiveData<Weather> {
        if(checkCity(city)){
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
                var okHttpClient = OkHttpClient()
                var request = Request.Builder()
                    .url(newUrlv2)
                    .build()

                okHttpClient.newCall(request).enqueue(object : Callback {
                    override fun onFailure(call: Call, e: IOException) {
                        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                    }

                    override fun onResponse(call: Call, response: Response) {
                        weatherTemp = getWeatherFromJson(response.body()!!.string())
                        var intent = Intent(AppWidgetManager.ACTION_APPWIDGET_UPDATE)
                        MyApplication.context.sendBroadcast(intent)
                    }
                })
            }else{
                Thread{
                    var url = URL(newUrlv2)
                    var conn = url.openConnection() as HttpsURLConnection
                    var inStream = conn.inputStream;
                    // 得到html的二进制数据
                    weatherTemp = getWeatherFromJson(readStreamToString(inStream))
                    var intent = Intent(AppWidgetManager.ACTION_APPWIDGET_UPDATE)
                    MyApplication.context.sendBroadcast(intent)
                }.start()
            }

        }
        return weather
    }



    private fun getWeatherFromJson(str: String): Weather{
        weatherTemp = Gson().fromJson(str, Weather::class.java)
        weatherTemp.data.toHashSet().forEach {
            if (!it.wea.contains("雨")) {
                weatherTemp.data.remove(it)
            }else{
                it.tems = it.tem2.substring(0,it.tem2.length - 1) + "-" + it.tem1
                var date = it.date.split('-')

                it.y = date[0]
                it.m = date[1]
                it.d = date[2]
                var today = Calendar.getInstance()
                today.time = SimpleDateFormat("yyyy-MM-dd").parse(it.date)
                var nl = LunarUtil(today).toString()
                var start = nl.indexOf("月")+1
                var end = nl.lastIndex+1
                it.date_nl =  nl.substring(start,end)
                it.tip = when (it.wea.length) {
                    1 -> "下雨天，记得带伞"
                    2 -> when (it.wea) {
                        "小雨" -> "雨虽小，注意别感冒"
                        "中雨" -> "记得随身携带雨伞"
                        "大雨" -> "出门最好穿雨衣，勿挡视线"
                        "阵雨" -> "阵雨来袭，出门记得带伞"
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
                                "雨夹雪" -> "道路湿滑，步行开车要谨慎"
                                else -> "error"
                            }
                    }
                    else -> "天气多变，照顾好自己"
                }
            }

        }
        weather.postValue(weatherTemp)
        return weatherTemp
    }


    companion object {
        lateinit var weatherTemp : Weather

        @Volatile
        private var instant: WeatherNetWork? = null

        fun getInstance() = instant ?: synchronized(this) {
            instant
                ?: WeatherNetWork().also { instant = it }
        }
    }
}