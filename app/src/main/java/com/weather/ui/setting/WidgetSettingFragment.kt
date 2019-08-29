package com.weather.ui.setting

import android.appwidget.AppWidgetManager
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import android.widget.LinearLayout
import android.widget.RadioGroup
import androidx.annotation.ColorInt
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.jrummyapps.android.colorpicker.ColorPickerDialog
import com.jrummyapps.android.colorpicker.ColorPickerDialogListener
import android.util.DisplayMetrics
import android.graphics.drawable.ColorDrawable
import android.view.*
import android.widget.TextView
import androidx.core.content.edit
import androidx.fragment.app.DialogFragment
import com.weather.MainActivity
import com.weather.MainActivity.Companion.isDiyTips
import com.weather.MainActivity.Companion.sharedPreferences
import com.weather.MainActivity.Companion.wColor
import com.weather.MainActivity.Companion.widgetTips
import com.weather.MyApplication
import com.weather.R
import com.weather.ui.main.WeatherFragment
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

    private lateinit var colorpicker : Button
    private lateinit var groupTips : RadioGroup
    private lateinit var groupDiyTips : RadioGroup
    private lateinit var yourtip : TextInputEditText
    private lateinit var yourtipLayout : TextInputLayout
    private lateinit var editTip : LinearLayout
    private lateinit var diyClick : TextView
    private val DIALGE_ID = 0

    private lateinit var dm: DisplayMetrics
    private lateinit var params: WindowManager.LayoutParams
    private var diyClicked = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        var view = inflater.inflate(R.layout.setting_widget, container,false)

        colorpicker = view.findViewById(R.id.colorpicker)
        groupTips = view.findViewById(R.id.groupTips)
        groupDiyTips = view.findViewById(R.id.groupDiyTips)
        yourtip = view.findViewById(R.id.yourTip)
        yourtipLayout = view.findViewById(R.id.yourTipLayout)
        diyClick = view.findViewById(R.id.diyClick)
        editTip = view.findViewById(R.id.editTip)
        colorpicker.setBackgroundColor(MainActivity.wColor)

        initView()

        view.setOnTouchListener(DrawerUtil.onTouch(view,this))

        return view
    }

    private fun initView() {
        if(MainActivity.diyTips.isNotEmpty()){
            yourtip.hint = MainActivity.diyTips
        }else{
            yourtip.hint = "输入内容"
        }

        diyClick.setOnClickListener {
            diyClicked  = true
            WidgetSettingClickFragment.getInstance()
                .show(fragmentManager!!, "settingclick")
            this.dismiss()
        }

        colorpicker.setOnClickListener {
            opeAdvancenDialog()
        }

        if(MainActivity.widgetTips) {
            groupTips.check(R.id.tips_o)
            editTip.visibility = View.VISIBLE
        } else {
            groupTips.check(R.id.tips_c)
            editTip.visibility = View.GONE
        }
        if(isDiyTips) groupDiyTips.check(R.id.diytips_o) else groupDiyTips.check(R.id.diytips_c)
        yourtipLayout.visibility = if(isDiyTips) View.VISIBLE else View.GONE

        groupTips.setOnCheckedChangeListener { _, checkedId ->
            MainActivity.widgetTips = if(checkedId == R.id.tips_o){
                editTip.visibility = View.VISIBLE
                true
            }else{
                editTip.visibility = View.GONE
                false
            }
            sharedPreferences.edit{
                putBoolean("widgetTips", widgetTips)
            }
            val intent = Intent(AppWidgetManager.ACTION_APPWIDGET_UPDATE)
            MyApplication.context.sendBroadcast(intent)
        }

        groupDiyTips.setOnCheckedChangeListener { _, checkedId ->
            isDiyTips = checkedId == R.id.diytips_o
            sharedPreferences.edit{
                putBoolean("isDiyTips", isDiyTips)
            }

            yourtipLayout.visibility = if (MainActivity.isDiyTips) {
                yourtip.text = null
                yourtip.requestFocus()
                WeatherFragment.imm.showSoftInput(yourtip, 0)
                View.VISIBLE
            } else {
                yourtip.clearFocus()
                WeatherFragment.imm.hideSoftInputFromWindow(yourtip.windowToken, 0)
                var intent = Intent(AppWidgetManager.ACTION_APPWIDGET_UPDATE)
                MyApplication.context.sendBroadcast(intent)
                View.GONE
            }

        }

        yourtip.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if(s!!.isNotEmpty()){
                    MainActivity.diyTips = s.toString()
                    sharedPreferences.edit {
                        putString("diyTips", MainActivity.diyTips)
                    }
                    var intent = Intent(AppWidgetManager.ACTION_APPWIDGET_UPDATE)
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
        if(!WidgetSettingClickFragment.getInstance().isAdded && !diyClicked){
            MainSettingFragment.getInstance()
                .show(fragmentManager!!, "setting")
        }
        diyClicked = false
    }

    private fun opeAdvancenDialog() {
        val color = MainActivity.wColor
        val colorPickerDialog = ColorPickerDialog.newBuilder().setColor(color)
            .setDialogTitle(R.string.widgetcolor)
            .setDialogType(ColorPickerDialog.TYPE_CUSTOM)
            .setShowAlphaSlider(true)
            .setDialogId(DIALGE_ID)
            .setAllowPresets(false)
            .create()
        colorPickerDialog.setColorPickerDialogListener(pickerDialogListener)
        @Suppress("DEPRECATION")
        colorPickerDialog.show(activity!!.fragmentManager, "color-picker-dialog")
    }

    private val pickerDialogListener = object : ColorPickerDialogListener {
        override fun onColorSelected(dialogId: Int, @ColorInt color: Int) {
            if (dialogId == DIALGE_ID) {
                wColor = color
                sharedPreferences.edit{
                    putInt("widgetColor",color)
                }

                colorpicker.setBackgroundColor(color)

                var intent = Intent(AppWidgetManager.ACTION_APPWIDGET_UPDATE)
                MyApplication.context.sendBroadcast(intent)
            }
        }

        override fun onDialogDismissed(dialogId: Int) {

        }
    }
}