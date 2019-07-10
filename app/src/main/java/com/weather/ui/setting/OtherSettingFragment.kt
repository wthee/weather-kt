package com.weather.ui.setting

import android.content.DialogInterface
import android.graphics.Color
import android.os.Bundle
import android.widget.RadioGroup
import android.util.DisplayMetrics
import android.graphics.drawable.ColorDrawable
import android.view.*
import androidx.fragment.app.DialogFragment
import com.weather.MainActivity.Companion.editor
import com.weather.R
import com.weather.ui.main.WeatherFragment
import com.weather.ui.main.WeatherFragment.Companion.adapter1
import com.weather.ui.main.WeatherFragment.Companion.adapter2
import com.weather.ui.main.WeatherFragment.Companion.viewModel
import com.weather.util.DrawerUtil


class OtherSettingFragment : DialogFragment() {

    companion object {
        @Volatile
        private var instance: OtherSettingFragment? = null

        fun getInstance() = instance ?: synchronized(this) {
            instance
                ?: OtherSettingFragment().also { instance = it }
        }
    }

    private lateinit var radioGroup1: RadioGroup
    private lateinit var radioGroup2: RadioGroup
    private lateinit var dm: DisplayMetrics
    private lateinit var params: WindowManager.LayoutParams

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

        radioGroup2.setOnCheckedChangeListener { _, checkedId ->
            WeatherFragment.nlIsGone = checkedId == R.id.rb3
            editor.putBoolean("nl", WeatherFragment.nlIsGone)
            editor.apply()
            adapter1.notifyDataSetChanged()
            adapter2.notifyDataSetChanged()
        }

        if (WeatherFragment.bjType == 0) radioGroup1.check(R.id.rb1) else radioGroup1.check(
            R.id.rb2
        )
        if (WeatherFragment.nlIsGone) radioGroup2.check(R.id.rb3) else radioGroup2.check(
            R.id.rb4
        )

        view.setOnTouchListener(DrawerUtil.onTouch(view,this))

        return view
    }

    override fun onStart() {
        super.onStart()

        val dw = dialog?.window
        dw!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT)) //一定要设置背景

        dm = DisplayMetrics()
        activity!!.windowManager.defaultDisplay.getMetrics(dm)

        params = dw.attributes
        //屏幕底部
        params.gravity = Gravity.BOTTOM
        params.width = ViewGroup.LayoutParams.MATCH_PARENT
        params.height = ViewGroup.LayoutParams.WRAP_CONTENT

        params.windowAnimations = R.style.BottomDialogAnimation
        dw.attributes = params
    }


    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        MainSettingFragment.getInstance()
            .show(activity!!.supportFragmentManager.beginTransaction(), "setting")
    }
}