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
import com.weather.MainActivity
import com.weather.MyApplication
import com.weather.R
import com.weather.ui.main.WeatherFragment
import android.util.DisplayMetrics
import android.graphics.drawable.ColorDrawable
import android.view.*
import androidx.fragment.app.DialogFragment
import com.nineoldandroids.view.ViewHelper
import com.weather.MainActivity.Companion.editor
import com.weather.MainActivity.Companion.isDiyTips


class WidgetSettingDialogFragment : DialogFragment() {

    companion object {
        @Volatile
        private var instance: WidgetSettingDialogFragment? = null

        fun getInstance() = instance ?: synchronized(this) {
            instance ?: WidgetSettingDialogFragment().also { instance = it }
        }
    }

    private lateinit var colorpicker : Button
    private lateinit var groupTips : RadioGroup
    private lateinit var groupDiyTips : RadioGroup
    private lateinit var yourtip : TextInputEditText
    private lateinit var yourtipLayout : TextInputLayout
    private lateinit var editTip : LinearLayout
    private val DIALGE_ID = 0

    private lateinit var dm: DisplayMetrics
    private lateinit var params: WindowManager.LayoutParams
    private var offsetY = 0
    private  var lastY :Int = 0

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        var view = inflater.inflate(R.layout.setting_widget, container,false)

        colorpicker = view.findViewById(R.id.colorpicker)
        groupTips = view.findViewById(R.id.groupTips)
        groupDiyTips = view.findViewById(R.id.groupDiyTips)
        yourtip = view.findViewById(R.id.yourTip)
        yourtipLayout = view.findViewById(R.id.yourTipLayout)
        editTip = view.findViewById(R.id.editTip)
        colorpicker.setBackgroundColor(MainActivity.wColor)

        initView()

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
                        if(offsetY<view.height / 4){
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

    private fun initView() {
        if(MainActivity.diyTips.isNotEmpty()){
            yourtip.hint = MainActivity.diyTips
        }else{
            yourtip.hint = "输入内容"
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
            editor.putBoolean("widgetTips", MainActivity.widgetTips)
            editor.apply()
            var intent = Intent(AppWidgetManager.ACTION_APPWIDGET_UPDATE)
            MyApplication.context.sendBroadcast(intent)
        }

        groupDiyTips.setOnCheckedChangeListener { group, checkedId ->
            isDiyTips = checkedId == R.id.diytips_o
            editor.putBoolean("isDiyTips", MainActivity.isDiyTips)
            editor.apply()

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
                    editor.putString("diyTips", MainActivity.diyTips)
                    var intent = Intent(AppWidgetManager.ACTION_APPWIDGET_UPDATE)
                    MyApplication.context.sendBroadcast(intent)
                }
                editor.apply()
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
        SettingDialogFragment.getInstance().show(fragmentManager, "setting")
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
        colorPickerDialog.show(activity!!.fragmentManager, "color-picker-dialog")
    }

    private val pickerDialogListener = object : ColorPickerDialogListener {
        override fun onColorSelected(dialogId: Int, @ColorInt color: Int) {
            if (dialogId == DIALGE_ID) {
                MainActivity.wColor = color
                MainActivity.editor.putInt("widgetColor",color)
                MainActivity.editor.apply()

                colorpicker.setBackgroundColor(color)

                var intent = Intent(AppWidgetManager.ACTION_APPWIDGET_UPDATE)
                MyApplication.context.sendBroadcast(intent)
            }
        }

        override fun onDialogDismissed(dialogId: Int) {

        }
    }
}