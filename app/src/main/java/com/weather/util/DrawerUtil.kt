package com.weather.util

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.util.DisplayMetrics
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.TranslateAnimation
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentActivity
import com.nineoldandroids.view.ViewHelper
import com.weather.R


object DrawerUtil {

    private var offsetY = 0
    private var lastY: Int = 0

    /**
     * 传入页面对应的 View [rootView] 和 DialogFragment [df]
     * 为页面所有布局和控件添加 OnTouchListener
    **/
    fun bindAllViewOnTouchListener(rootView: View,df: DialogFragment){
        val views = getAllChildViews(rootView)
        views.add(rootView)
        views.forEach {
            it.setOnTouchListener { _, event ->
                val y = event.rawY.toInt()
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        lastY = event.rawY.toInt()
                    }
                    MotionEvent.ACTION_MOVE -> {
                        offsetY = y - lastY
                        if (offsetY > 0) {
                            ViewHelper.setTranslationY(rootView, offsetY.toFloat())
                            ViewHelper.setAlpha(rootView, 1 - offsetY.toFloat() / rootView.height)
                        }
                    }
                    MotionEvent.ACTION_UP -> {
                        if (offsetY > 0) {
                            if (offsetY < rootView.height / 4) {
                                //设置动画
                                val anim = TranslateAnimation(0f, 0f, 0f, -offsetY.toFloat())
                                anim.duration = 150
                                anim.setAnimationListener(object : Animation.AnimationListener {
                                    override fun onAnimationRepeat(animation: Animation?) {}

                                    override fun onAnimationStart(animation: Animation?) {}

                                    override fun onAnimationEnd(animation: Animation?) {
                                        rootView.clearAnimation()
                                        ViewHelper.setTranslationY(rootView, 0f)
                                        ViewHelper.setAlpha(rootView, 1f)
                                    }
                                })
                                rootView.startAnimation(anim)
                            } else {
                                df.dismiss()
                            }
                            offsetY = 0
                        }
                    }
                }
                if (it == rootView){
                    return@setOnTouchListener true
                }
                return@setOnTouchListener false
            }
        }
    }

    /**
     * 把 [dialog]的高度设为 [height] 并位于底部
     **/
    fun setBottomDrawer(dialog: Dialog?, activity: FragmentActivity?, height: Int){
        val dw = dialog?.window
        dw!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val dm = DisplayMetrics()
        activity!!.windowManager.defaultDisplay.getMetrics(dm)

        val params = dw.attributes
        params.gravity = Gravity.BOTTOM
        params.width = ViewGroup.LayoutParams.MATCH_PARENT
        params.height = height

        params.windowAnimations = R.style.BottomDialogAnimation
        dw.attributes = params
    }

    private fun getAllChildViews(view: View): MutableList<View> {
        val allchildren = ArrayList<View>()
        if (view is ViewGroup) {
            for (i in 0 until view.childCount) {
                val viewchild = view.getChildAt(i)
                allchildren.add(viewchild)
                allchildren.addAll(getAllChildViews(viewchild))
            }
        }
        return allchildren
    }

}