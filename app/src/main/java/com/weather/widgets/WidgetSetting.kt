package com.weather.widgets

import android.app.PendingIntent.getActivity
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import androidx.appcompat.widget.Toolbar
import com.weather.R
import androidx.annotation.ColorInt
import com.jrummyapps.android.colorpicker.ColorPickerDialog
import com.jrummyapps.android.colorpicker.ColorPickerDialogListener
import com.weather.MainActivity
import com.weather.MyApplication
import kotlinx.android.synthetic.main.activity_widget_setting.*


class WidgetSetting : AppCompatActivity() {

    companion object {
        var wColor: Int = -16777216
    }

    private var editor: SharedPreferences.Editor = MainActivity.editor

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_widget_setting)
        setToolBar()



        colorpicker.setTextColor(wColor)

        colorpicker.setOnClickListener {
            opeAdvancenDialog()
        }

    }

    val DIALGE_ID = 0

    private fun opeAdvancenDialog() {
        val color = wColor
        //传入的默认color
        val colorPickerDialog = ColorPickerDialog.newBuilder().setColor(color)
                .setDialogTitle(R.string.widgetcolor)
                //设置dialog标题
                .setDialogType(ColorPickerDialog.TYPE_CUSTOM)
                //设置为自定义模式
                .setShowAlphaSlider(true)
                //设置有透明度模式，默认没有透明度
                .setDialogId(DIALGE_ID)
                //设置Id,回调时传回用于判断
                .setAllowPresets(false)
                //不显示预知模式
                .create()
        //Buider创建
        colorPickerDialog.setColorPickerDialogListener(pickerDialogListener)
        //设置回调，用于获取选择的颜色
        colorPickerDialog.show(fragmentManager, "color-picker-dialog")
    }

    private val pickerDialogListener = object : ColorPickerDialogListener {
        override fun onColorSelected(dialogId: Int, @ColorInt color: Int) {
            if (dialogId == DIALGE_ID) {
                wColor = color
                editor.putInt("widgetColor",color)
                editor.apply()

                colorpicker.setTextColor(color)

                var intent = Intent(AppWidgetManager.ACTION_APPWIDGET_UPDATE)
                MyApplication.context.sendBroadcast(intent)
            }
        }

        override fun onDialogDismissed(dialogId: Int) {

        }
    }

    private fun setToolBar() {
        var toolbar = findViewById<Toolbar>(R.id.widgetToolbar)
        setSupportActionBar(toolbar)
        supportActionBar!!.setHomeButtonEnabled(true)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        this.supportActionBar!!.setDisplayShowTitleEnabled(false)
    }

    //Toolbar的事件---返回
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId === android.R.id.home) {
            finish()
        }
        return super.onOptionsItemSelected(item)
    }
}
