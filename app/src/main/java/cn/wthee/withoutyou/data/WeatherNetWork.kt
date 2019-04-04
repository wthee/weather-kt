package cn.wthee.withoutyou.data

import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import cn.wthee.withoutyou.MyApplication
import cn.wthee.withoutyou.util.ActivityUtil
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import okhttp3.*
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream

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
    private lateinit var temp :Weather
    private var url = "https://www.tianqiapi.com/api/?version=v2&appid=1001&appsecret=1002&"
    private lateinit var cityNames: List<CityName>
    var newUrl = url
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
            if (city.equals(it)) {
                if (it.equals("wthee")) {
                    //ok_button = "🤪"
                } else if (it.equals("WLQ")) {
                    //ok_button = "💔"
                } else {
                    //ok_button = "返回"
                }
                Toast.makeText(MyApplication.context,showResult[index],Toast.LENGTH_LONG).show()
            }
        }
        if (city.length > 1) {
            cityNames.forEach{
                if (city.equals(it.cityZh)){
                    newUrl= url + "city=$city"
                    return true
                }
            }
            if (city == "ip") {
                newUrl= url + "ip"
                return true
            }
        }
        return false
    }

    fun changeType(){
        weather.postValue(temp)
    }

    fun getCity(city: String): LiveData<Weather> {
        if(checkCity(city)){
            var okHttpClient = OkHttpClient()
            var request = Request.Builder()
                .url(newUrl)
                .build()

            okHttpClient.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                }

                override fun onResponse(call: Call, response: Response) {
                    temp = Gson().fromJson(response.body()!!.string(), Weather::class.java)
                    temp.data.toHashSet().forEach {
                        if (!it.wea.contains("雨")) {
                            temp.data.remove(it)
                        }else{
                            it.tem = it.tem2 + "-" + it.tem1 + "℃"
                            var date = it.date.split('-')
                            it.y = date[0]
                            it.m = date[1]
                            it.d = date[2]
                            it.tip = when (it.wea.length) {
                                1 -> "下雨天，记得带伞"
                                2 -> when (it.wea) {
                                    "小雨" -> "雨虽小，注意保暖别感冒"
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
                    weather.postValue(temp)
                }
            })
        }
        return weather
    }

    companion object {
        @Volatile
        private var instant: WeatherNetWork? = null

        fun getInstance() = instant ?: synchronized(this) {
            instant ?: WeatherNetWork().also { instant = it }
        }
    }
}