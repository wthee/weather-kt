package com.weather.ui.setting

import android.content.DialogInterface
import android.graphics.Color
import android.os.Bundle
import android.widget.RadioGroup
import com.weather.R
import com.weather.ui.main.WeatherFragment
import android.util.DisplayMetrics
import android.graphics.drawable.ColorDrawable
import android.view.*
import androidx.fragment.app.DialogFragment
import com.nineoldandroids.view.ViewHelper
import com.weather.MainActivity.Companion.editor
import com.weather.ui.main.WeatherFragment.Companion.adapter1
import com.weather.ui.main.WeatherFragment.Companion.adapter2
import com.weather.ui.main.WeatherFragment.Companion.viewModel


class OtherSettingDialogFragment : DialogFragment() {

    companion object {
        @Volatile
        private var instance: OtherSettingDialogFragment? = null

        fun getInstance() = instance ?: synchronized(this) {
            instance ?: OtherSettingDialogFragment().also { instance = it }
        }
    }

    private lateinit var radioGroup1: RadioGroup
    private lateinit var radioGroup2: RadioGroup
    private lateinit var dm: DisplayMetrics
    private lateinit var params: WindowManager.LayoutParams
    private var offsetY = 0
    private  var lastY :Int = 0

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        var view = inflater.inflate(R.layout.setting_other, container,false)
        radioGroup1 = view.findViewById(R.id.groupBJ)
        radioGroup2 = view.findViewById(R.id.groupNL)

        radioGroup1.setOnCheckedChangeListener { _, checkedId ->
            WeatherFragment.bjType = if (checkedId == R.id.rb1) 0 else 1
            editor.putInt("type", WeatherFragment.bjType)
            editor.apply()
            viewModel.changeType()
        }

        radioGroup2.setOnCheckedChangeListener { group, checkedId ->
            WeatherFragment.nlIsGone = checkedId == R.id.rb3
            editor.putBoolean("nl", WeatherFragment.nlIsGone)
            editor.apply()
            adapter1.notifyDataSetChanged()
            adapter2.notifyDataSetChanged()
        }

        if (WeatherFragment.bjType == 0) radioGroup1.check(R.id.rb1) else radioGroup1.check(R.id.rb2)
        if (WeatherFragment.nlIsGone) radioGroup2.check(R.id.rb3) else radioGroup2.check(R.id.rb4)

        view.setOnTouchListener { v, event ->
            var y = event.rawY.toInt()
            when(event.action){
                MotionEvent.ACTION_DOWN ->{
                    lastY = event.rawY.toInt()
                }
                MotionEvent.ACTION_MOVE ->{
                    offsetY = y - lastY
                    if(offsetY>0){
                        ViewHelper.setTranslationY(view, offsetY.toFloat())
                    }
                }
                MotionEvent.ACTION_UP ->{
                    if(offsetY>0){
                        if(offsetY<view.height / 3){
                            ViewHelper.setTranslationY(view,0.toFloat())
                        }else{
                            this.dismiss()
                        }
                    }
                }
            }
            return@setOnTouchListener true
        }
        return view
    }

    override fun onStart() {
        super.onStart()

        val dw = dialog.window
        dw!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT)) //一定要设置背景

        dm = DisplayMetrics()
        activity!!.windowManager.defaultDisplay.getMetrics(dm)

        params = dw.attributes
        //屏幕底部
        params.gravity = Gravity.BOTTOM
        params.width = dm.widthPixels //屏幕宽度
        params.height = ViewGroup.LayoutParams.WRAP_CONTENT

        params.windowAnimations = R.style.BottomDialogAnimation
        dw.attributes = params
    }

    override fun onDismiss(dialog: DialogInterface?) {
        super.onDismiss(dialog)
        SettingDialogFragment.getInstance().show(activity!!.supportFragmentManager.beginTransaction(), "setting")
    }
}