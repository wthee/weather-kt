package com.weather.ui.setting

import android.appwidget.AppWidgetManager
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.edit
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.forEachIndexed
import androidx.core.view.get
import androidx.fragment.app.activityViewModels
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.chip.Chip
import com.weather.MainActivity
import com.weather.MyApplication
import com.weather.R
import com.weather.databinding.FragmentSettingMainBinding
import com.weather.databinding.LayoutChipBinding
import com.weather.databinding.LayoutChipEditableBinding
import com.weather.ui.main.WeatherFragment
import com.weather.ui.main.WeatherFragment.Companion.toUpdate
import com.weather.ui.main.WeatherViewModel
import com.weather.util.ColorSeekBar
import com.weather.util.Constant
import com.weather.util.InjectorUtil
import com.weather.util.WeatherUtil


class MainSettingFragment : BottomSheetDialogFragment() {

    companion object {
        fun getInstance(): MainSettingFragment {
            return MainSettingFragment()
        }
    }

    private lateinit var binding: FragmentSettingMainBinding
    private var firstCursor = 0
    private var secondCursor = 50
    private val viewModel by activityViewModels<WeatherViewModel> {
        InjectorUtil.getWeatherViewModelFactory()
    }

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

        //加载城市
        loadCity()
        //监听事件
        setListener()
        return binding.root
    }

    override fun onActivityCreated(arg0: Bundle?) {
        super.onActivityCreated(arg0)
        dialog?.window?.attributes?.windowAnimations = R.style.DialogAnimation
    }

    private fun setListener(){
        binding.apply {
            //管理城市
            editCity.setOnClickListener {
                //图标更换&布局显示/隐藏
                groupCityEdit.visibility =
                    if (groupCityEdit.visibility == View.GONE) {
                        editCity.background =
                            ResourcesCompat.getDrawable(resources, R.drawable.ic_back, null)
                        View.VISIBLE
                    } else {
                        editCity.background =
                            ResourcesCompat.getDrawable(resources, R.drawable.ic_edit, null)
                        View.GONE
                    }
                groupCity.visibility =
                    if (groupCity.visibility == View.GONE)
                        View.VISIBLE
                    else
                        View.GONE
            }
            //切换城市
            groupCity.setOnCheckedChangeListener { group, checkedId ->
                val v = group.findViewById<Chip>(checkedId)
                toUpdate = true
                //点击位置
                group.forEachIndexed { index, view ->
                    if(view.id == checkedId){
                        MainActivity.sp.edit {
                            putInt(Constant.CITY_INDEX, index)
                        }
                    }
                }
                viewModel.changeCity(v.text.toString())
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
            //取消输入框焦点
            mainView.setOnClickListener {
                addCityEditText.clearFocus()
                addCityInputLayout.visibility = View.GONE
                groupCity.visibility = View.VISIBLE
                WeatherFragment.imm.hideSoftInputFromWindow(addCityEditText.windowToken, 0)
            }
            //添加城市名
            addCityEditText.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable?) {
                    if (viewModel.checkCity(s.toString()) != "0") {
                        addCityInputLayout.visibility = View.GONE
                        groupCity.visibility = View.VISIBLE
                        addCityEditText.text = null
                        WeatherUtil.addCity(s.toString())
                        //更新布局
                        loadCity(showEdit = true)
                    }
                }

                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                    return
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    return
                }

            })
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


    private fun loadCity(showEdit: Boolean = false){
        binding.apply {
            groupCity.removeAllViews()
            groupCityEdit.removeAllViews()
            groupCity.visibility = if(showEdit) View.GONE else View.VISIBLE
            groupCityEdit.visibility = if(showEdit) View.VISIBLE else View.GONE
            //加载城市
            val cityIndex = WeatherUtil.getCityIndex()
            WeatherUtil.getAllCity().forEachIndexed { index, city ->
                //显示城市
                val chip = LayoutChipBinding.inflate(layoutInflater).root
                chip.text = city
                groupCity.addView(chip)
                if (index == cityIndex) {
                    chip.isChecked = true
                }
                //可编辑城市
                val chipEdit = LayoutChipEditableBinding.inflate(layoutInflater).root
                chipEdit.text = city
                groupCityEdit.addView(chipEdit)
                //添加按钮
                if (WeatherUtil.getAllCity().lastIndex == index) {
                    val add = LayoutChipBinding.inflate(layoutInflater).root
                    add.text = "新增城市"
                    add.isCheckable = false
                    groupCityEdit.addView(add)
                    add.setOnClickListener {
                        //TODO 数量限制
                        if (WeatherUtil.getAllCity().size < 5) {

                        }
                        //TODO 弹输入框
                        addCityInputLayout.visibility = View.VISIBLE
                        addCityEditText.text = null
                        addCityEditText.requestFocus()
                        WeatherFragment.imm.showSoftInput(addCityEditText, 0)
                    }
                }
                //删除点击
                chipEdit.setOnCloseIconClickListener {
                    if (WeatherUtil.getAllCity().size > 1) {
                        WeatherUtil.deleteCity(chipEdit.text.toString())
                        //移除
                        groupCity.removeView(chip)
                        groupCityEdit.removeView(it)
                        groupCity.check(groupCity.get(0).id)
                    } else {
                        Toast.makeText(requireContext(), "无法删除最后一个城市~", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }
}