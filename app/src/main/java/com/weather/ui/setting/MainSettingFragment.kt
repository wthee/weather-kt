package com.weather.ui.setting

import android.appwidget.AppWidgetManager
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.edit
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.weather.MainActivity
import com.weather.MyApplication
import com.weather.R
import com.weather.databinding.FragmentSettingMainBinding
import com.weather.databinding.LayoutChipBinding
import com.weather.databinding.LayoutChipEditableBinding
import com.weather.util.ColorSeekBar
import com.weather.util.Constant

class MainSettingFragment : BottomSheetDialogFragment() {

    companion object {
        fun getInstance(): MainSettingFragment {
            return MainSettingFragment()
        }
    }

    private lateinit var binding: FragmentSettingMainBinding
    private var firstCursor = 0
    private var secondCursor = 50

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSettingMainBinding.inflate(inflater, container, false)

        //显示设置页面
        childFragmentManager.beginTransaction()
            .replace(R.id.setting_test, SettingsFragment())
            .commit()

        //seekbar 初始游标位置
        firstCursor = MainActivity.sp.getInt("firstCursor", 0)
        secondCursor = MainActivity.sp.getInt("secondCursor", 50)

        binding.apply {
            //加载城市
            MainActivity.citys.forEachIndexed { index, city ->
                val chip = LayoutChipBinding.inflate(layoutInflater).root
                chip.text = city
                groupCity.addView(chip)
                if (index == MainActivity.cityIndex) {
                    chip.isChecked = true
                }
                //可编辑城市
                val chipEdit = LayoutChipEditableBinding.inflate(layoutInflater).root
                chipEdit.text = city
                groupCityEdit.addView(chipEdit)
            }
            //管理城市
            editCity.setOnClickListener {
                groupCityEdit.visibility = View.VISIBLE
                groupCity.visibility = View.GONE
            }
            //颜色监听
            colorPicker.progress = firstCursor
            colorGradient.progress = secondCursor
            colorPicker.setOnDrawListener(object : ColorSeekBar.OnDrawListener {
                override fun onDrawStart(seekBar: ColorSeekBar) {
                    colorPicker.setBackgroundGradientColors(ColorSeekBar.DEFAULT_COLORS)
                }

                override fun onDrawFinish(seekBar: ColorSeekBar) {
                    //绘制结束，设置渐变中间色
                    colorGradient.setBackgroundGradientColors(
                        arrayListOf(
                            Color.BLACK,
                            colorPicker.getThumbColor(),
                            Color.WHITE
                        )
                    )
                    colorGradient.postInvalidate()
                }
            })
            colorGradient.setOnDrawListener(object : ColorSeekBar.OnDrawListener {
                override fun onDrawStart(seekBar: ColorSeekBar) {
                }

                override fun onDrawFinish(seekBar: ColorSeekBar) {
                    //改变字体颜色
                    changeWidgetTextColor()
                }
            })
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

        }
    }

    //改变颜色
    private fun changeWidgetTextColor() {
        val textColor = binding.colorGradient.getThumbColor()
        binding.widgetText.setTextColor(textColor)
        MainActivity.sp.edit {
            putInt(Constant.WIDGET_TEXT_COLOR, textColor)
            putInt("firstCursor", binding.colorPicker.progress)
            putInt("secondCursor", binding.colorGradient.progress)
        }
        MainActivity.widgetTextColor = textColor
        //通知桌面小部件更新
        val intent = Intent(AppWidgetManager.ACTION_APPWIDGET_UPDATE)
        MyApplication.context.sendBroadcast(intent)
    }


}