package com.weather.ui.setting

import android.appwidget.AppWidgetManager
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.RadioGroup
import android.widget.TextView
import androidx.core.content.edit
import androidx.fragment.app.DialogFragment
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.weather.MainActivity
import com.weather.MainActivity.Companion.isDiyTips
import com.weather.MainActivity.Companion.sharedPreferences
import com.weather.MainActivity.Companion.widgetTextColor
import com.weather.MainActivity.Companion.widgetTips
import com.weather.MyApplication
import com.weather.R
import com.weather.ui.main.WeatherFragment
import com.weather.util.ColorPickerView
import com.weather.util.DrawerUtil


class WidgetSettingFragment : DialogFragment() {

    companion object {
        @Volatile
        private var instance: WidgetSettingFragment? = null

        fun getInstance() = instance ?: synchronized(this) {
            instance
                ?: WidgetSettingFragment().also { instance = it }
        }
    }

    private lateinit var colorPicker: ColorPickerView
    private lateinit var colorGradient: ColorPickerView
    private lateinit var groupTips: RadioGroup
    private lateinit var groupDiyTips: RadioGroup
    private lateinit var yourtip: TextInputEditText
    private lateinit var yourtipLayout: TextInputLayout
    private lateinit var editTip: LinearLayout
    private lateinit var diyClick: TextView
    private lateinit var widgetText: TextView
    private var firstCursor = 0
    private var secondCursor = 50
    private var diyClicked = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.setting_widget, container, false)

        colorPicker = view.findViewById(R.id.colorPicker)
        colorGradient = view.findViewById(R.id.colorGradient)
        groupTips = view.findViewById(R.id.groupTips)
        groupDiyTips = view.findViewById(R.id.groupDiyTips)
        yourtip = view.findViewById(R.id.yourTip)
        yourtipLayout = view.findViewById(R.id.yourTipLayout)
        diyClick = view.findViewById(R.id.diyClick)
        editTip = view.findViewById(R.id.editTip)
        widgetText = view.findViewById(R.id.widgetText)

        firstCursor = sharedPreferences.getInt("firstCursor",0)
        secondCursor = sharedPreferences.getInt("secondCursor",50)

        initView()
        bindListener()
        //滑动关闭
        DrawerUtil.bindAllViewOnTouchListener(view, this, arrayListOf(colorPicker,colorGradient))

        return view
    }


    override fun onStart() {
        super.onStart()
        DrawerUtil.setBottomDrawer(dialog, activity, ViewGroup.LayoutParams.WRAP_CONTENT)
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        if(MyApplication().isForeground()){
            if (!WidgetSettingClickFragment.getInstance().isAdded && !diyClicked) {
                MainSettingFragment.getInstance()
                    .show(fragmentManager!!, "setting")
            }
            diyClicked = false
        }
    }

    private fun bindListener() {
        colorPicker.progress = firstCursor
        colorGradient.progress = secondCursor

        colorPicker.setOnDrawListener(object : ColorPickerView.OnDrawListener {
            override fun onDrawStart(seekBar: ColorPickerView) {
                colorPicker.setBackgroundGradientColors(ColorPickerView.DEFAULT_COLORS)
            }

            override fun onDrawFinish(seekBar: ColorPickerView) {
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

        colorGradient.setOnDrawListener(object : ColorPickerView.OnDrawListener {
            override fun onDrawStart(seekBar: ColorPickerView) {
            }

            override fun onDrawFinish(seekBar: ColorPickerView) {
                //改变字体颜色
                changeWidgetTextColor()
            }
        })

        //自定义点击事件
        diyClick.setOnClickListener {
            diyClicked = true
            WidgetSettingClickFragment.getInstance()
                .show(fragmentManager!!, "settingclick")
            this.dismiss()
        }

        //显示/隐藏桌面提示
        groupTips.setOnCheckedChangeListener { _, checkedId ->
            widgetTips = if (checkedId == R.id.tips_o) {
                editTip.visibility = View.VISIBLE
                true
            } else {
                editTip.visibility = View.GONE
                false
            }
            sharedPreferences.edit {
                putBoolean("widgetTips", widgetTips)
            }
            val intent = Intent(AppWidgetManager.ACTION_APPWIDGET_UPDATE)
            MyApplication.context.sendBroadcast(intent)
        }

        //默认/自定义提示内容
        groupDiyTips.setOnCheckedChangeListener { _, checkedId ->
            isDiyTips = checkedId == R.id.diytips_o
            sharedPreferences.edit {
                putBoolean("isDiyTips", isDiyTips)
            }

            yourtipLayout.visibility = if (isDiyTips) {
                yourtip.text = null
                yourtip.requestFocus()
                WeatherFragment.imm.showSoftInput(yourtip, 0)
                View.VISIBLE
            } else {
                yourtip.clearFocus()
                WeatherFragment.imm.hideSoftInputFromWindow(yourtip.windowToken, 0)
                val intent = Intent(AppWidgetManager.ACTION_APPWIDGET_UPDATE)
                MyApplication.context.sendBroadcast(intent)
                View.GONE
            }
        }

        //自定义内容
        yourtip.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (s!!.isNotEmpty()) {
                    MainActivity.diyTips = s.toString()
                    sharedPreferences.edit {
                        putString("diyTips", MainActivity.diyTips)
                    }
                    val intent = Intent(AppWidgetManager.ACTION_APPWIDGET_UPDATE)
                    MyApplication.context.sendBroadcast(intent)
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

    private fun initView(){
        //填充自定义提示内容
        if (MainActivity.diyTips.isNotEmpty()) {
            yourtip.hint = MainActivity.diyTips
        } else {
            yourtip.hint = "输入内容"
        }

        //是否自定义提示
        if (widgetTips) {
            groupTips.check(R.id.tips_o)
            editTip.visibility = View.VISIBLE
        } else {
            groupTips.check(R.id.tips_c)
            editTip.visibility = View.GONE
        }

        //显示/隐藏输入框
        if (isDiyTips) groupDiyTips.check(R.id.diytips_o) else groupDiyTips.check(R.id.diytips_c)
        yourtipLayout.visibility = if (isDiyTips) View.VISIBLE else View.GONE
    }

    //改变颜色
    private fun changeWidgetTextColor(){
        val textColor = colorGradient.getThumbColor()
        widgetText.setTextColor(textColor)
        sharedPreferences.edit {
            putInt("widgetColor", textColor)
            putInt("firstCursor",colorPicker.progress)
            putInt("secondCursor",colorGradient.progress)
        }
        widgetTextColor = textColor
        //通知桌面小部件更新
        val intent = Intent(AppWidgetManager.ACTION_APPWIDGET_UPDATE)
        MyApplication.context.sendBroadcast(intent)
    }
}