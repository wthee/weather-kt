package com.weather.ui.setting

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.*
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.nineoldandroids.view.ViewHelper
import com.weather.R
import com.weather.data.Data
import lecho.lib.hellocharts.gesture.ContainerScrollType
import lecho.lib.hellocharts.gesture.ZoomType
import lecho.lib.hellocharts.model.*
import lecho.lib.hellocharts.view.LineChartView
import kotlin.collections.ArrayList


class WeatherInfoDialogFragment : DialogFragment() {

    companion object {

        fun getInstance(item: Data) : WeatherInfoDialogFragment {
            var instance = WeatherInfoDialogFragment()
            val args = Bundle()
            args.putSerializable("item", item)
            instance!!.arguments = args
            return instance
        }
    }


    private lateinit var lineChart: LineChartView
    private lateinit var hoursOf: ArrayList<String>
    private lateinit var temH: ArrayList<Int>
    private lateinit var mPointValues: ArrayList<PointValue>
    private lateinit var mAxisXValues: ArrayList<AxisValue>
    private lateinit var item: Data

    private lateinit var dm: DisplayMetrics
    private lateinit var params: WindowManager.LayoutParams
    private var offsetY = 0
    private  var lastY :Int = 0

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        var view = inflater.inflate(R.layout.weather_info, container,false)
        lineChart = view.findViewById(R.id.chart)
        item = arguments!!.get("item") as Data
        initLineChart(item)//初始化
        var day = view.findViewById(R.id.weaDay) as TextView
        var air = view.findViewById(R.id.air) as LinearLayout
        var lv = view.findViewById(R.id.air_lv) as TextView
        var tips = view.findViewById(R.id.air_tips) as TextView
        var cyLv = view.findViewById(R.id.chuanyi_title) as TextView
        var cyTips = view.findViewById(R.id.chuanyi_tip) as TextView

        day.text = item.day

        if (item.air_tips!=null) {
            air.visibility = View.VISIBLE
            lv.text = "空气质量："+item.air+" "+ item.air_level
            tips.text = "\t\t\t\t"+item.air_tips
        }else{
            air.visibility = View.GONE
        }
        cyLv.text = item.index[3].title +"："+ item.index[3].level
        cyTips.text = "\t\t\t\t"+item.index[3].desc


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
                        if(offsetY<view.height / 3){
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