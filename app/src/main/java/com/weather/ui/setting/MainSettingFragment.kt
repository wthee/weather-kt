package com.weather.ui.setting

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import androidx.core.content.edit
import androidx.core.view.forEachIndexed
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.chip.Chip
import com.weather.MainActivity
import com.weather.MainActivity.Companion.onNight
import com.weather.MainActivity.Companion.sp
import com.weather.R
import com.weather.databinding.FragmentSettingMainBinding
import com.weather.databinding.LayoutChipBinding
import com.weather.ui.main.WeatherFragment
import com.weather.ui.main.WeatherFragment.Companion.imm
import com.weather.ui.main.WeatherFragment.Companion.toUpdate
import com.weather.ui.main.WeatherFragment.Companion.viewModel
import com.weather.util.NightModelUtil

class MainSettingFragment : BottomSheetDialogFragment() {

    companion object {
        fun getInstance(): MainSettingFragment {
            return MainSettingFragment()
        }
    }


    private lateinit var binding : FragmentSettingMainBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSettingMainBinding.inflate(inflater, container, false)
        bindingListener()
        binding.apply {
            //城市
            MainActivity.citys.forEachIndexed { index, city ->
                val chip = LayoutChipBinding.inflate(layoutInflater).root
                chip.text = city
                chip.isCheckable = true
                chip.isClickable = true
                binding.groupCity.addView(chip)
                if (index == MainActivity.cityIndex) {
                    chip.isChecked = true
                }
            }
            //恢复选择
            if (WeatherFragment.styleType == 0) groupStyle.check(R.id.styleDefault) else groupStyle.check(
                R.id.styleClassical
            )
            if (WeatherFragment.lunarGone) groupNL.check(R.id.lunarClose) else groupNL.check(
                R.id.lunarOpen
            )
            //取消输入框焦点
            mainView.setOnClickListener {
                modify.clearFocus()
                modifyLayout.visibility = View.GONE
                groupCity.visibility = View.VISIBLE
                imm.hideSoftInputFromWindow(modify.windowToken, 0)
            }
            //点击切换布局
            groupStyle.setOnCheckedChangeListener { _, checkedId ->
                when(checkedId){
                    R.id.styleDefault -> WeatherFragment.styleType =  0
                    R.id.styleClassical -> WeatherFragment.styleType = 1
                }
                sp.edit {
                    putInt("type", WeatherFragment.styleType)
                }
                viewModel.changeType()
            }
            //点击切换农历显示
            groupNL.setOnCheckedChangeListener { _, checkedId ->
                WeatherFragment.lunarGone = checkedId == R.id.lunarClose
                sp.edit {
                    putBoolean("nl", WeatherFragment.lunarGone)
                }
                WeatherFragment.adapter.notifyDataSetChanged()
            }
        }

        return binding.root
    }

    private fun bindingListener() {
        //TODO 切换&修改城市
        binding.apply {
            groupCity.setOnCheckedChangeListener { group, checkedId ->
//                val v = group.findViewById<Chip>(checkedId)
//
//                toUpdate = true
//                viewModel.changeCity(v.text.toString())
            }

            //长按显示修改城市输入框
            groupCity.forEachIndexed  { _, view ->
                val cityView = view as Chip
                cityView.setOnLongClickListener {
                    cityView.isChecked = true
                    modifyLayout.visibility = View.VISIBLE
                    modify.text = null
                    modify.requestFocus()
                    imm.showSoftInput(modify, 0)
                    groupCity.visibility =
                        if (groupCity.visibility == View.GONE) View.VISIBLE else View.GONE
                    return@setOnLongClickListener false
                }
            }

            // 夜间模式
            groupDN.setOnCheckedChangeListener { _, checkedId ->
                onNight = checkedId == R.id.nightModelOpen
                sp.edit {
                    putBoolean("onNight", onNight)
                }
                NightModelUtil.initNightModel(onNight)
            }

            //小部件设置
            widgetsetting.setOnClickListener {
                WidgetSettingFragment.getInstance()
                    .show(parentFragmentManager, "widget")
                dialog?.dismiss()
            }

            //修改城市名
            modify.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable?) {
                    if (viewModel.checkCity(s.toString()) != "0") {
                        modifyLayout.visibility = View.GONE
                        groupCity.visibility = View.VISIBLE
                        modify.text = null
                        toUpdate = true
                        viewModel.changeCity(s.toString())
                    }
                }

                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                    return
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    return
                }

            })
        }
    }

}