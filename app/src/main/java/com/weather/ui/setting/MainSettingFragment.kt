package com.weather.ui.setting

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import androidx.core.content.edit
import androidx.core.view.forEachIndexed
import androidx.core.view.get
import androidx.fragment.app.DialogFragment
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.weather.GuideView
import com.weather.MainActivity.Companion.isFirstOpenSetting
import com.weather.MainActivity.Companion.onNight
import com.weather.MainActivity.Companion.sharedPreferences
import com.weather.R
import com.weather.ui.main.WeatherFragment.Companion.cityIndex
import com.weather.ui.main.WeatherFragment.Companion.imm
import com.weather.ui.main.WeatherFragment.Companion.saveC1
import com.weather.ui.main.WeatherFragment.Companion.saveC2
import com.weather.ui.main.WeatherFragment.Companion.saveC3
import com.weather.ui.main.WeatherFragment.Companion.title
import com.weather.ui.main.WeatherFragment.Companion.toUpdate
import com.weather.ui.main.WeatherFragment.Companion.viewModel
import com.weather.util.DrawerUtil
import com.weather.util.NightModelUtil

class MainSettingFragment : DialogFragment() {

    companion object {
        fun getInstance(): MainSettingFragment {
            return MainSettingFragment()
        }
    }

    private lateinit var widgetsetting: TextView
    private lateinit var othersetting: TextView
    private lateinit var modify: TextInputEditText
    private lateinit var modifyLayout: TextInputLayout
    private lateinit var groupCity: RadioGroup
    private lateinit var groupDN: RadioGroup
    private lateinit var mainView: LinearLayout
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.setting_main, container, false)

        mainView = view.findViewById(R.id.mainView)
        widgetsetting = view.findViewById(R.id.widgetset)
        othersetting = view.findViewById(R.id.othersetting)
        modify = view.findViewById(R.id.modify)
        modifyLayout = view.findViewById(R.id.modifyLayout)
        groupCity = view.findViewById(R.id.groupCity)
        groupDN = view.findViewById(R.id.groupDN)

        initView()
        bindingListener()

        DrawerUtil.bindAllViewOnTouchListener(view, this,null)
        //取消输入框焦点
        mainView.setOnClickListener {
            modify.clearFocus()
            modifyLayout.visibility = View.GONE
            groupCity.visibility = View.VISIBLE
            imm.hideSoftInputFromWindow(modify.windowToken, 0)
        }
        //第一次打开，显示提示内容
        if (isFirstOpenSetting) {
            view.post {
                GuideView(groupCity[2], 4)
                    .show(fragmentManager!!, "test")
                GuideView(groupCity[2], 3)
                    .show(fragmentManager!!, "test")
                sharedPreferences.edit {
                    putBoolean("isFirstOpenSetting", false)
                }
                isFirstOpenSetting = sharedPreferences.getBoolean("isFirstOpen", false)
            }
        }
        return view
    }

    override fun onStart() {
        super.onStart()
        DrawerUtil.setBottomDrawer(dialog, activity,ViewGroup.LayoutParams.WRAP_CONTENT)
    }

    private fun bindingListener() {
        //切换&修改城市
        groupCity.setOnCheckedChangeListener { group, checkedId ->
            val v = group.findViewById<RadioButton>(checkedId)
            when(checkedId){
                R.id.city1 -> cityIndex = 1
                R.id.city2 -> cityIndex = 2
                R.id.city3 -> cityIndex = 3
            }
            toUpdate = true
            viewModel.changeCity(v.text.toString())
        }

        //长按显示修改城市输入框
        groupCity.forEachIndexed { _, view ->
            val cityView = view as RadioButton
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
            sharedPreferences.edit {
                putBoolean("onNight", onNight)
            }
            NightModelUtil.initNightModel(onNight)
        }

        //小部件设置
        widgetsetting.setOnClickListener {
            WidgetSettingFragment.getInstance()
                .show(fragmentManager!!, "widget")
            this.dismiss()
        }

        //显示风格
        othersetting.setOnClickListener {
            StyleSettingFragment.getInstance()
                .show(fragmentManager!!, "other")
            this.dismiss()
        }

        //修改城市名
        modify.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (viewModel.checkCity(s.toString()) != "0") {
                    groupCity.forEachIndexed { index, view ->
                        if ((view as RadioButton).isChecked) {
                            view.text = s
                            when (index) {
                                0 -> saveC1 = view.text.toString()
                                1 -> saveC2 = view.text.toString()
                                2 -> saveC3 = view.text.toString()
                            }
                            sharedPreferences.edit {
                                putString("city" + (index + 1), s.toString())
                            }
                            cityIndex = index + 1
                        }
                    }
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

    //打开设置页面时，显示已选择城市
    private fun initView() {

        saveC1 = sharedPreferences.getString("city1", saveC1)!!
        saveC2 = sharedPreferences.getString("city2", saveC2)!!
        saveC3 = sharedPreferences.getString("city3", saveC3)!!

        groupCity.forEachIndexed { index, view ->
            (view as RadioButton).text = when (index) {
                0 -> saveC1
                1 -> saveC2
                2 -> saveC3
                else -> "ip"
            }
            if(title == view.text) groupCity.check(view.id)
        }

        if (onNight) groupDN.check(R.id.nightModelOpen) else groupDN.check(R.id.nightModelClose)
    }
}