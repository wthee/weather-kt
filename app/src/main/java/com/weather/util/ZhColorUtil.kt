package com.weather.util

import android.graphics.Color

object ZhColorUtil {

    fun formColor(color: String):Int{
        var colorInt = when(color[0]){
            //预警
            '红' ->{
                Color.parseColor("#EE1A00")
            }
            '黄' ->{
                Color.parseColor("#F5AF1D")
            }
            '蓝' ->{
                Color.parseColor("#0077F5")
            }
            '橙' ->{
                Color.parseColor("#F77F14")
            }

            //天气质量
            '优' ->{
                Color.parseColor("#A7CF8C")
            }
            '良' ->{
                Color.parseColor("#F7DA64")
            }
            '轻' ->{
                Color.parseColor("#F29E39")
            }
            '中' ->{
                Color.parseColor("#DE6A71")
            }
            '重' ->{
                Color.parseColor("#B9377A")
            }
            '严' ->{
                Color.parseColor("#881326")
            }

            else ->{
                Color.parseColor("#2296eb")
            }
        }
        if(color.contains('热')) colorInt = Color.parseColor("#EE1A00")
        if(color.contains('冷')) colorInt = Color.parseColor("#0077F5")
        return colorInt
    }

}