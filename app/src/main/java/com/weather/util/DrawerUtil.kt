package com.weather.util

import android.view.MotionEvent
import android.view.View
import android.view.animation.Animation
import android.view.animation.TranslateAnimation
import androidx.fragment.app.DialogFragment
import com.nineoldandroids.view.ViewHelper

object DrawerUtil {

    private var offsetY = 0
    private var lastY: Int = 0

    fun onTouch(view: View,df: DialogFragment): View.OnTouchListener{
        return View.OnTouchListener { v, event ->
            var y = event.rawY.toInt()
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    lastY = event.rawY.toInt()
                }
                MotionEvent.ACTION_MOVE -> {
                    offsetY = y - lastY
                    if (offsetY > 0) {
                        ViewHelper.setTranslationY(view, offsetY.toFloat())
                    }
                }
                MotionEvent.ACTION_UP -> {
                    if (offsetY > 0) {
                        if (offsetY < view.height / 4) {
                            //设置动画
                            var anim = TranslateAnimation(0f, 0f, 0f, -offsetY.toFloat());
                            anim.duration = 150
                            anim.setAnimationListener(object : Animation.AnimationListener {
                                override fun onAnimationRepeat(animation: Animation?) {}

                                override fun onAnimationStart(animation: Animation?) {}

                                override fun onAnimationEnd(animation: Animation?) {
                                    view.clearAnimation()
                                    ViewHelper.setTranslationY(view, 0f)
                                }
                            })
                            view.startAnimation(anim)
                        } else {
                            df.dismiss()
                        }
                        offsetY = 0
                    }
                }
            }
            true
        }
    }
}