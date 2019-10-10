package com.weather.util

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.weather.data.model.City
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream

class GetAllCity {

    companion object {

        @Volatile
        private var instance: GetAllCity? = null

        fun getInstance() = instance ?: synchronized(this) {
            instance
                ?: GetAllCity().also { instance = it }
        }
    }

    var citys: List<City>

    init {
        var iS = ActivityUtil.instance.currentActivity!!.resources.assets.open("city.json")
        val listType = object : TypeToken<List<City>>() {}.type
        citys = Gson().fromJson(readStreamToString(iS), listType)
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
}