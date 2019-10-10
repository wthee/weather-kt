package com.weather.ui.setting

import android.annotation.SuppressLint
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioGroup
import androidx.core.content.edit
import androidx.core.view.forEachIndexed
import androidx.fragment.app.DialogFragment
import com.weather.MainActivity.Companion.sharedPreferences
import com.weather.MyApplication
import com.weather.R
import com.weather.ui.main.WeatherFragment.Companion.adapter
import com.weather.ui.main.WeatherFragment.Companion.lunarGone
import com.weather.ui.main.WeatherFragment.Companion.styleType
import com.weather.ui.main.WeatherFragment.Companion.viewModel
import com.weather.util.DrawerUtil


class OtherSettingFragment : DialogFragment() {

    companion object {
        @SuppressLint("StaticFieldLeak")
        @Volatile
        private var instance: OtherSettingFragment? = null

        fun getInstance() = instance ?: synchronized(this) {
            instance
                ?: OtherSettingFragment().also { instance = it }
        }
    }

    private lateinit var groupStyle: RadioGroup
    private lateinit var groupNL: RadioGroup

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.setting_other, container,false)
        groupStyle = view.findViewById(R.id.groupStyle)
        groupNL = view.findViewById(R.id.groupNL)

        initView()

        //滑动关闭
        DrawerUtil.bindAllViewOnTouchListener(view,this)
        return view
    }

    override fun onStart() {
        super.onStart()
        DrawerUtil.setBottomDrawer(dialog, activity, ViewGroup.LayoutParams.WRAP_CONTENT)
    }

    private fun initView(){
        //点击切换布局
        groupStyle.forEachIndexed { index, v ->
            v.setOnClickListener {
                styleType = index
                sharedPreferences.edit {
                    putInt("type", styleType)
                }
                viewModel.changeType()
            }
        }

        //点击切换农历显示
        groupNL.forEachIndexed { index, v ->
            v.setOnClickListener {
                lunarGone = index == 0
                sharedPreferences.edit {
                    putBoolean("nl", lunarGone)
                }
                adapter.notifyDataSetChanged()
            }
        }

        //恢复选择
        if (styleType == 0) groupStyle.check(R.id.styleDefault) else groupStyle.check(
            R.id.styleClassical
        )
        if (lunarGone) groupNL.check(R.id.lunarClose) else groupNL.check(
            R.id.lunarOpen
        )
    }
    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        if(MyApplication().isForeground()){
            MainSettingFragment.getInstance()
                .show(fragmentManager!!, "setting")
        }
    }
}