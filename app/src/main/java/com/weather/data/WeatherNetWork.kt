package com.weather.data

import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Build
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.weather.MyApplication
import com.weather.util.ActivityUtil
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.weather.util.LunarUtil
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.http.*
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
    private var nowWeather = MutableLiveData<NowWeather>()

    private var urlv1 = "https://www.tianqiapi.com/api/?version=v1&"
    private var urlv6 = "https://www.tianqiapi.com/api/?version=v6&"
    private var citys: List<City>
    private lateinit var map1: Map<String,String>
    private lateinit var map6: Map<String,String>
    var newUrlv1 = urlv1
    var newUrlv6 = urlv6

    init {
        var iS = ActivityUtil.instance.currentActivity!!.resources.assets.open("city.json")
        val listType = object : TypeToken<List<City>>() {}.type
        citys = Gson().fromJson(readStreamToString(iS),listType)
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



    fun changeType(){
        weather.postValue(weatherTemp)
    }

    fun changeCity(city: String){
        getWeather(city)
        getNowWeather(city)
    }

    fun getWeather(city: String): LiveData<Weather> {
        if(checkCity(city)){
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
                val retrofit: Retrofit =  Retrofit.Builder()
                    .baseUrl("https://www.tianqiapi.com/api/")
                    .build()
                val igService = retrofit.create(IGetRequest::class.java)
                igService.reqGetWea(map1).enqueue(object : Callback<ResponseBody> {
                    override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                        Toast.makeText(MyApplication.context,"网络出错~",Toast.LENGTH_SHORT).show()
                    }
                    override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                        getWeatherFromJson(response.body()!!.string())
                        var intent = Intent(AppWidgetManager.ACTION_APPWIDGET_UPDATE)
                        MyApplication.context.sendBroadcast(intent)
                    }
                })
            }else{
                Thread{
                    var url = URL(newUrlv1)
                    var conn = url.openConnection() as HttpsURLConnection
                    var inStream = conn.inputStream
                    getWeatherFromJson(readStreamToString(inStream))
                    var intent = Intent(AppWidgetManager.ACTION_APPWIDGET_UPDATE)
                    MyApplication.context.sendBroadcast(intent)
                }.start()
            }
        }

        return weather
    }

    fun getNowWeather(city: String): LiveData<NowWeather> {
        if(checkCity(city)){
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
                val retrofit: Retrofit =  Retrofit.Builder()
                    .baseUrl("https://www.tianqiapi.com/api/")
                    .build()
                val igService = retrofit.create(IGetRequest::class.java)
                igService.reqGetWea(map6).enqueue(object : Callback<ResponseBody> {
                    override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                        Toast.makeText(MyApplication.context,"网络出错~",Toast.LENGTH_SHORT).show()
                    }

                    override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                        getNowWeatherFromJson(response.body()!!.string())
                    }
                })
            }else{
                Thread{
                    var url = URL(newUrlv6)
                    var conn = url.openConnection() as HttpsURLConnection
                    var inStream = conn.inputStream;
                    getNowWeatherFromJson(readStreamToString(inStream))
                }.start()
            }
        }

        return nowWeather
    }

    fun checkCity(city: String):Boolean{
        myUser.forEachIndexed{index,it ->
            if (city == it) {
                Toast.makeText(MyApplication.context,showResult[index],Toast.LENGTH_LONG).show()
            }
        }
        if (city.length > 1) {
            citys.forEach{
                if (city == it.cityZh){
                    newUrlv1= urlv1 + "city=$city"
                    newUrlv6= urlv6 + "city=$city"
                    map1 = mapOf("version" to "v1", "city" to city)
                    map6 = mapOf("version" to "v6", "city" to city)
                    return true
                }
            }
            if (city == "ip") {
                newUrlv1= urlv1 + "ip"
                newUrlv6= urlv6 + "ip"
                map1 = mapOf("version" to "v1", "ip" to "")
                map6 = mapOf("version" to "v6", "ip" to "")
                return true
            }
        }
        return false
    }

    private fun getWeatherFromJson(str: String){
        weatherTemp = Gson().fromJson(str, Weather::class.java)
        today = weatherTemp.data[0]
        if(today.air==0){
            getLeaderWeather(weatherTemp.city,object :IGetLw{
                override fun setLeader(wea: Weather) {
                    today.air = wea.data[0].air
                    today.air_level = wea.data[0].air_level
                    today.air_tips = wea.data[0].air_tips
                }

            })
        }
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
                        "大雨" -> "出门最好穿雨衣"
                        "阵雨" -> "阵雨来袭，记得带伞"
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
                                "雨夹雪" -> "道路湿滑，出行要谨慎"
                                else -> "error"
                            }
                    }
                    else -> "天气多变，照顾好自己"
                }
            }

        }
        weather.postValue(weatherTemp)
    }

    private fun getNowWeatherFromJson(str: String){
        nowWeatherTemp = Gson().fromJson(str, NowWeather::class.java)
        nowWeatherTemp.tem = nowWeatherTemp.tem + "℃"
        nowWeather.postValue(nowWeatherTemp)
    }

    private fun getLeaderWeather(city: String,setLw:IGetLw){
        var leader = " "
        citys.forEach {
            if(city == it.cityZh){
                leader =  it.leaderZh
                return@forEach
            }
        }
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
            val retrofit: Retrofit =  Retrofit.Builder()
                .baseUrl("https://www.tianqiapi.com/api/")
                .build()
            val igService = retrofit.create(IGetRequest::class.java)
            igService.reqGetWea(mapOf("version" to "v1", "city" to leader)).enqueue(object : Callback<ResponseBody> {

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    Toast.makeText(MyApplication.context,"网络出错~",Toast.LENGTH_SHORT).show()
                }

                override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                    setLw.setLeader(Gson().fromJson(response.body()!!.string(), Weather::class.java)!!)
                }
            })

        }else{
            Thread{
                var url = URL("https://www.tianqiapi.com/api/?version=v1&city=$leader")
                var conn = url.openConnection() as HttpsURLConnection
                var inStream = conn.inputStream
                setLw.setLeader(Gson().fromJson(readStreamToString(inStream), Weather::class.java)!!)
            }.start()
        }
    }

    interface IGetRequest{
        @GET(".")
        fun reqGetWea(@QueryMap map:Map<String ,String>): Call<ResponseBody>
    }

    interface IGetLw{
        fun setLeader(wea:Weather)
    }

    companion object {
        lateinit var weatherTemp : Weather
        lateinit var nowWeatherTemp : NowWeather
        lateinit var today :Data
        @Volatile
        private var instance: WeatherNetWork? = null

        fun getInstance() = instance ?: synchronized(this) {
            instance ?: WeatherNetWork().also { instance = it }
        }
    }
}