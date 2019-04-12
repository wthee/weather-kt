package com.weather.util

import android.app.AlertDialog
import android.app.FragmentManager
import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.RadioGroup
import android.widget.TextView
import androidx.annotation.ColorInt
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.jrummyapps.android.colorpicker.ColorPickerDialog
import com.jrummyapps.android.colorpicker.ColorPickerDialogListener
import com.weather.MainActivity.Companion.diyTips
import com.weather.MainActivity.Companion.editor
import com.weather.MainActivity.Companion.isDiyTips
import com.weather.MainActivity.Companion.wColor
import com.weather.MainActivity.Companion.widgetTips
import com.weather.MyApplication
import com.weather.R
import com.weather.data.Data
import com.weather.ui.main.WeatherFragment.Companion.imm
import lecho.lib.hellocharts.view.LineChartView
import lecho.lib.hellocharts.gesture.ContainerScrollType
import lecho.lib.hellocharts.gesture.ZoomType
import lecho.lib.hellocharts.model.*


class AlertDialogUtil {

    private lateinit var lineChart: LineChartView
    private lateinit var hoursOf: ArrayList<String>
    private lateinit var temH: ArrayList<Int>
    private lateinit var mPointValues: ArrayList<PointValue>
    private lateinit var mAxisXValues: ArrayList<AxisValue>

    private lateinit var colorpicker : Button
    private lateinit var groupTips : RadioGroup
    private lateinit var groupDiyTips : RadioGroup
    private lateinit var yourtip : TextInputEditText
    private lateinit var yourtipLayout : TextInputLayout
    private val DIALGE_ID = 0

    fun showWeatherInfoDialog(context: Context, item: Data){
        var view = LayoutInflater.from(context).inflate(R.layout.weather_info, null)
        lineChart = view.findViewById(R.id.chart)

        initLineChart(item)//初始化

        var air = view.findViewById(R.id.air) as LinearLayout
        var lv = view.findViewById(R.id.air_lv) as TextView
        var tips = view.findViewById(R.id.air_tips) as TextView
        var cyLv = view.findViewById(R.id.chuanyi_title) as TextView
        var cyTips = view.findViewById(R.id.chuanyi_tip) as TextView

        if (item.air_tips!=null) {
            air.visibility = View.VISIBLE
            lv.text = "空气质量："+item.air+" "+ item.air_level
            tips.text = "\t\t\t\t"+item.air_tips
        }else{
            air.visibility = View.GONE
        }
        cyLv.text = item.index[3].title +"："+ item.index[3].level
        cyTips.text = "\t\t\t\t"+item.index[3].desc

        var alertDialog = AlertDialog.Builder(context)
        alertDialog.setView(view)
        alertDialog.setTitle(item.day)
        alertDialog.show()
    }

    fun showWidgetSettingDialog(context: Context,fragmentManager: FragmentManager){
        var view = LayoutInflater.from(context).inflate(R.layout.widget_setting, null)

        colorpicker = view.findViewById(R.id.colorpicker)
        groupTips = view.findViewById(R.id.groupTips)
        groupDiyTips = view.findViewById(R.id.groupDiyTips)
        yourtip = view.findViewById(R.id.yourTip)
        yourtipLayout = view.findViewById(R.id.yourTipLayout)
        colorpicker.setBackgroundColor(wColor)

        if(diyTips.isNotEmpty()){
            yourtip.hint = diyTips
        }else{
            yourtip.hint = "输入内容"
        }

        colorpicker.setOnClickListener {
            opeAdvancenDialog(fragmentManager)
        }

        if(widgetTips) groupTips.check(R.id.tips_o) else groupTips.check(R.id.tips_c)
        if(isDiyTips) groupDiyTips.check(R.id.diytips_o) else groupDiyTips.check(R.id.diytips_c)
        yourtipLayout.visibility = if(isDiyTips) View.VISIBLE else View.GONE

        groupTips.setOnCheckedChangeListener { group, checkedId ->
            widgetTips = checkedId == R.id.tips_o
            editor.putBoolean("widgetTips", widgetTips)
            editor.apply()
            var intent = Intent(AppWidgetManager.ACTION_APPWIDGET_UPDATE)
            MyApplication.context.sendBroadcast(intent)
        }

        groupDiyTips.setOnCheckedChangeListener { group, checkedId ->
            isDiyTips  = checkedId == R.id.diytips_o
            editor.putBoolean("isDiyTips", isDiyTips)
            editor.apply()

            yourtipLayout.visibility = if (isDiyTips) {
                yourtip.text = null
                yourtip.requestFocus()
                imm.showSoftInput(yourtip, 0)
                View.VISIBLE
            } else {
                yourtip.clearFocus()
                imm.hideSoftInputFromWindow(yourtip.windowToken, 0)
                var intent = Intent(AppWidgetManager.ACTION_APPWIDGET_UPDATE)
                MyApplication.context.sendBroadcast(intent)
                View.GONE
            }

        }

        yourtip.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if(s!!.isNotEmpty()){
                    diyTips = s.toString()
                    editor.putString("diyTips", diyTips)
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
        var alertDialog = AlertDialog.Builder(context)
        alertDialog.setView(view)
        alertDialog.show()
    }

    private fun opeAdvancenDialog(fragmentManager: android.app.FragmentManager) {
        val color = wColor
        val colorPickerDialog = ColorPickerDialog.newBuilder().setColor(color)
            .setDialogTitle(R.string.widgetcolor)
            .setDialogType(ColorPickerDialog.TYPE_CUSTOM)
            .setShowAlphaSlider(true)
            .setDialogId(DIALGE_ID)
            .setAllowPresets(false)
            .create()
        colorPickerDialog.setColorPickerDialogListener(pickerDialogListener)
        colorPickerDialog.show(fragmentManager, "color-picker-dialog")
    }

    private val pickerDialogListener = object : ColorPickerDialogListener {
        override fun onColorSelected(dialogId: Int, @ColorInt color: Int) {
            if (dialogId == DIALGE_ID) {
                wColor = color
                editor.putInt("widgetColor",color)
                editor.apply()

                colorpicker.setBackgroundColor(color)

                var intent = Intent(AppWidgetManager.ACTION_APPWIDGET_UPDATE)
                MyApplication.context.sendBroadcast(intent)
            }
        }

        override fun onDialogDismissed(dialogId: Int) {

        }
    }


    /**
     * 设置X 轴的显示
     */
    private fun getAxisXLables() {
        for (i in 0 until hoursOf.size) {
            mAxisXValues.add(AxisValue(i.toFloat()).setLabel(hoursOf[i]))
        }
    }

    /**
     * 图表的每个点的显示
     */
    private fun getAxisPoints() {
        for (i in 0 until temH.size) {
            mPointValues.add(PointValue(i.toFloat(), temH[i].toFloat()))
        }
    }

    private fun initLineChart(item: Data) {

        hoursOf = arrayListOf()
        temH = arrayListOf()
        mPointValues = ArrayList()
        mAxisXValues = ArrayList()

        item.hours.forEach {
            hoursOf.add(it.day.substring(3, 5) + ":00" + it.wea)
            temH.add(it.tem.substring(0, it.tem.lastIndex).toInt())
        }

        getAxisXLables()//获取x轴的标注
        getAxisPoints()//获取坐标点

        val line = Line(mPointValues).setColor(Color.parseColor("#2296eb"))  //折线的颜色
        val lines = ArrayList<Line>()
        line.shape =
                ValueShape.CIRCLE//折线图上每个数据点的形状  这里是圆形 （有三种 ：ValueShape.SQUARE  ValueShape.CIRCLE  ValueShape.DIAMOND）
        line.isCubic = true//曲线是否平滑，即是曲线还是折线
        line.isFilled = false//是否填充曲线的面积
        line.setHasLabels(true)//曲线的数据坐标是否加上备注
        line.setHasLines(true)
        line.setHasPoints(true)
        lines.add(line)
        val data = LineChartData()
        data.lines = lines

        //坐标轴
        val axisX = Axis() //X轴
        axisX.setHasTiltedLabels(true)  //X坐标轴字体是斜的显示还是直的，true是斜的显示
        axisX.textColor = Color.GRAY  //设置字体颜色
        axisX.textSize = 12//设置字体大小
        axisX.values = mAxisXValues  //填充X轴的坐标名称
        data.axisXBottom = axisX //x 轴在底部
        axisX.setHasLines(true) //x 轴分割线


        val axisY = Axis()  //Y轴
        axisY.textSize = 12//设置字体大小
        data.axisYLeft = axisY  //Y轴设置在左边


        //设置行为属性，支持缩放、滑动以及平移
        lineChart.isInteractive = true
        lineChart.zoomType = ZoomType.HORIZONTAL
        lineChart.maxZoom = 2f
        lineChart.setContainerScrollEnabled(true, ContainerScrollType.HORIZONTAL)
        lineChart.lineChartData = data
        lineChart.visibility = View.VISIBLE

        val v = lineChart.maximumViewport
        v.top = (item.tem1.substring(0, item.tem1.length - 1).toInt() + 1).toFloat()
        v.bottom = (item.tem2.substring(0, item.tem2.length - 1).toInt() - 1).toFloat()
        lineChart.currentViewport = v
    }


}