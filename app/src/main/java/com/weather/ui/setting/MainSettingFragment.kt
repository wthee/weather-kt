package com.weather.ui.setting

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.DisplayMetrics
import android.view.*
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.weather.GuideView
import com.weather.MainActivity
import com.weather.MainActivity.Companion.editor
import com.weather.MainActivity.Companion.isFirstOpenSetting
import com.weather.R
import com.weather.ui.main.WeatherFragment
import com.weather.ui.main.WeatherFragment.Companion.saveC1
import com.weather.ui.main.WeatherFragment.Companion.saveC2
import com.weather.ui.main.WeatherFragment.Companion.saveC3
import com.weather.ui.main.WeatherFragment.Companion.title
import com.weather.ui.main.WeatherFragment.Companion.viewModel
import com.weather.util.DrawerUtil

class MainSettingFragment : DialogFragment() {

    companion object {
        fun getInstance() : MainSettingFragment {
            return MainSettingFragment()
        }
    }

    private lateinit var widgetsetting: TextView
    private lateinit var othersetting: TextView
    private lateinit var city1: RadioButton
    private lateinit var city2: RadioButton
    private lateinit var city3: RadioButton
    private lateinit var rb5: RadioButton
    private lateinit var rb6: RadioButton
    private lateinit var modify: TextInputEditText
    private lateinit var modifyLayout: TextInputLayout
    private lateinit var groupCity: RadioGroup
    private lateinit var radioGroup3: RadioGroup

    private lateinit var dm: DisplayMetrics
    private lateinit var params: WindowManager.LayoutParams

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        var view = inflater.inflate(R.layout.setting_main, container, false)

        widgetsetting = view.findViewById(R.id.widgetset)
        othersetting = view.findViewById(R.id.othersetting)
        city1 = view.findViewById(R.id.city1)
        city2 = view.findViewById(R.id.city2)
        city3 = view.findViewById(R.id.city3)
        rb5 = view.findViewById(R.id.rb5)
        rb6 = view.findViewById(R.id.rb6)
        modify = view.findViewById(R.id.modify)
        modifyLayout = view.findViewById(R.id.modifyLayout)
        groupCity = view.findViewById(R.id.groupCity)
        radioGroup3 = view.findViewById(R.id.groupDN)

        initView()
        resumeAllView()

        view.setOnTouchListener(DrawerUtil.onTouch(view,this))

        if(isFirstOpenSetting){
            view.post {
                GuideView(city3, 4)
                    .show(activity!!.supportFragmentManager.beginTransaction(),"test")
                GuideView(city3, 3)
                    .show(activity!!.supportFragmentManager.beginTransaction(),"test")
                editor.putBoolean("isFirstOpenSetting",false)
                editor.apply()
                isFirstOpenSetting = MainActivity.sharedPreferences.getBoolean("isFirstOpen",false)
            }
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

        params.gravity = Gravity.BOTTOM
        params.width = ViewGroup.LayoutParams.MATCH_PARENT
        params.height = ViewGroup.LayoutParams.WRAP_CONTENT

        params.windowAnimations = R.style.BottomDialogAnimation
        dw.attributes = params


    }

    private fun initView() {

        widgetsetting.setOnClickListener {
            WidgetSettingFragment.getInstance()
                .show(activity!!.supportFragmentManager.beginTransaction(), "widget")
            this.dismiss()
        }

        othersetting.setOnClickListener {
            OtherSettingFragment.getInstance()
                .show(activity!!.supportFragmentManager.beginTransaction(), "other")
            this.dismiss()
        }

        city1.setOnClickListener {
            viewModel.changeCity(city1.text.toString())
        }
        city2.setOnClickListener {
            viewModel.changeCity(city2.text.toString())
        }
        city3.setOnClickListener {
            viewModel.changeCity(city3.text.toString())
        }


        city1.setOnLongClickListener {
            modifyLayout.visibility = if (modifyLayout.visibility == View.GONE) {
                modify.text = null
                modify.requestFocus()
                WeatherFragment.imm.showSoftInput(modify, 0)
                View.VISIBLE
            } else {
                modify.clearFocus()
                WeatherFragment.imm.hideSoftInputFromWindow(modify.windowToken, 0)
                View.GONE
            }
            groupCity.visibility = if (groupCity.visibility == View.GONE) View.VISIBLE else View.GONE
            return@setOnLongClickListener false
        }

        city2.setOnLongClickListener {
            modifyLayout.visibility = if (modifyLayout.visibility == View.GONE) {
                modify.text = null
                modify.requestFocus()
                WeatherFragment.imm.showSoftInput(modify, 0)
                View.VISIBLE
            } else {
                modify.clearFocus()
                WeatherFragment.imm.hideSoftInputFromWindow(modify.windowToken, 0)
                View.GONE
            }
            groupCity.visibility = if (groupCity.visibility == View.GONE) View.VISIBLE else View.GONE
            return@setOnLongClickListener false
        }

        city3.setOnLongClickListener {
            modifyLayout.visibility = if (modifyLayout.visibility == View.GONE) {
                modify.text = null
                modify.requestFocus()
                WeatherFragment.imm.showSoftInput(modify, 0)
                View.VISIBLE
            } else {
                modify.clearFocus()
                WeatherFragment.imm.hideSoftInputFromWindow(modify.windowToken, 0)
                View.GONE
            }
            groupCity.visibility = if (groupCity.visibility == View.GONE) View.VISIBLE else View.GONE
            return@setOnLongClickListener false
        }


        rb5.setOnClickListener {
            if (MainActivity.onNight) {
                MainActivity.onNight = false
                editor.putBoolean("onNight", MainActivity.onNight)
                editor.apply()
                activity!!.recreate()
            }
        }

        rb6.setOnClickListener {
            if (!MainActivity.onNight) {
                MainActivity.onNight = true
                editor.putBoolean("onNight", MainActivity.onNight)
                editor.apply()
                activity!!.recreate()
            }
        }

        modify.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (viewModel.checkCity(s.toString()) != -1) {
                    if (city1.isChecked) {
                        city1.text = s
                        editor.putString("city1", s.toString())
                        modifyLayout.visibility = View.GONE
                    }
                    if (city2.isChecked) {
                        city2.text = s
                        editor.putString("city2", s.toString())
                        modifyLayout.visibility = View.GONE
                    }
                    if (city3.isChecked) {
                        city3.text = s
                        editor.putString("city3", s.toString())
                        modifyLayout.visibility = View.GONE
                    }
                    groupCity.visibility = View.VISIBLE
                    modify.text = null
                    editor.apply()
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

    private fun resumeAllView() {

        saveC1 = MainActivity.sharedPreferences.getString("city1", saveC1)!!
        saveC2 = MainActivity.sharedPreferences.getString("city2", saveC2)!!
        saveC3 = MainActivity.sharedPreferences.getString("city3", saveC3)!!
        city1.text = saveC1
        city2.text = saveC2
        city3.text = saveC3

        if (title == city1.text) groupCity.check(R.id.city1)
        if (title == city2.text) groupCity.check(R.id.city2)
        if (title == city3.text) groupCity.check(R.id.city3)

        if (MainActivity.onNight) radioGroup3.check(R.id.rb6) else radioGroup3.check(
            R.id.rb5
        )
    }

}