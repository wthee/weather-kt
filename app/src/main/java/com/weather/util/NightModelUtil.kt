package com.weather.util

import android.view.View
import androidx.core.content.res.ResourcesCompat

object NightModelUtil {

    private val activity = ActivityUtil.instance.currentActivity

    //夜间模式
    fun initNightModel(onNight: Boolean){
//        if(onNight){
//            ResourcesCompat.getInstance().loadSkin("dark", SkinCompatManager.SKIN_LOADER_STRATEGY_BUILD_IN); // 后缀加载
//        }else{
//            ResourcesCompat.getInstance().restoreDefaultTheme()
//        }
        val decor = ActivityUtil.instance.currentActivity!!.window.decorView
        if (!onNight) {
            decor.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        } else {
            decor.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        }
        StatusBarUtil.setStatusBarDarkTheme(activity,!onNight)
    }

}