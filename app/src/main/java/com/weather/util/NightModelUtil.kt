package com.weather.util

import android.view.View
import com.weather.MyApplication
import com.weather.R
import skin.support.SkinCompatManager
import skin.support.content.res.SkinCompatResources

object NightModelUtil {

    //夜间模式
    fun initNightModel(onNight: Boolean){
        if(onNight){
            SkinCompatManager.getInstance().loadSkin("dark", SkinCompatManager.SKIN_LOADER_STRATEGY_BUILD_IN); // 后缀加载
        }else{
            SkinCompatManager.getInstance().restoreDefaultTheme()
        }
        val decor = ActivityUtil.instance.currentActivity!!.window.decorView
        if (!onNight) {
            decor.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        } else {
            decor.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        }
    }

}