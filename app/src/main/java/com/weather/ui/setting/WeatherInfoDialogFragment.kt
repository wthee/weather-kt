package com.weather.ui.setting

import android.graphics.*
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.*
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.MarkerView
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.utils.MPPointF
import com.nineoldandroids.view.ViewHelper
import com.weather.R
import com.weather.data.Data
import com.weather.util.WeaColorUtil
import kotlin.collections.ArrayList
import java.text.DecimalFormat


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

    private lateinit var lineChart: LineChart
    private lateinit var mPointValues: ArrayList<Entry>
    private lateinit var mPointValues1: ArrayList<Entry>
    private lateinit var mLables: ArrayList<String>
    private lateinit var dataSets: List<LineDataSet>
    private lateinit var lineData: LineData
    private var axisWidth: Float = 1f
    private var axisColor: Int = Color.parseColor("#C0C0C0")
    private lateinit var item: Data

    private lateinit var dm: DisplayMetrics
    private lateinit var params: WindowManager.LayoutParams
    private var offsetY = 0
    private  var lastY :Int = 0

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        var view = inflater.inflate(R.layout.weather_info, container,false)
        lineChart = view.findViewById(R.id.lineChart)
        item = arguments!!.get("item") as Data

        initLineChart()//初始化

        var day = view.findViewById(R.id.weaDay) as TextView
        var air = view.findViewById(R.id.air) as LinearLayout
        var air_lv = view.findViewById(R.id.air_lv) as TextView
        var air_tip = view.findViewById(R.id.air_tips) as TextView
        var alarm = view.findViewById(R.id.alarm) as LinearLayout
        var alarm_type = view.findViewById(R.id.alarm_type) as TextView
        var alarm_lv = view.findViewById(R.id.alarm_lv) as TextView
        var alarm_tip = view.findViewById(R.id.alarm_content) as TextView

        var cyLv = view.findViewById(R.id.chuanyi_title) as TextView
        var cyTips = view.findViewById(R.id.chuanyi_tip) as TextView

        day.text = item.day

        if (item.alarm!=null&&item.alarm.alarm_type!="") {
            alarm.visibility = View.VISIBLE
            alarm_type.text = item.alarm.alarm_type+"预警："
            alarm_lv.text = item.alarm.alarm_level
            alarm_lv.setTextColor(WeaColorUtil.formColor(item.alarm.alarm_level))
            alarm_tip.text = "\t\t\t\t"+item.alarm.alarm_content
        }else{
            alarm.visibility = View.GONE
        }

        if (item.air_tips!=null) {
            air.visibility = View.VISIBLE
            air_lv.text = item.air.toString() +" "+ item.air_level
            air_lv.setTextColor(WeaColorUtil.formColor(item.air_level))
            air_tip.text = "\t\t\t\t"+item.air_tips
        }else{
            air.visibility = View.GONE
        }

        cyLv.text = item.index[3].level
        cyLv.setTextColor(WeaColorUtil.formColor(item.index[3].level))
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

    private fun setStyle() {

        //linechart style
        lineChart.description = null
        lineChart.setDrawBorders(false)
        lineChart.isScaleXEnabled = false
        lineChart.isScaleYEnabled = false

        //x
        var xAxis = lineChart.xAxis
        xAxis.setDrawGridLines(true)
        xAxis.setDrawAxisLine(false)
        xAxis.textSize = 12f
        xAxis.valueFormatter = IndexAxisValueFormatter(mLables)
        xAxis.gridColor = Color.parseColor("#33000000")
        xAxis.axisLineWidth = axisWidth
        xAxis.axisLineColor = axisColor
        xAxis.textColor = axisColor
        xAxis.granularity = 1f
        xAxis.yOffset = -3f
        xAxis.position = XAxis.XAxisPosition.BOTTOM

        //y
        var yAxis = lineChart.axisLeft
        var yAxis1 = lineChart.axisRight
        yAxis.axisLineWidth = axisWidth
        yAxis.setDrawGridLines(false)
        yAxis1.setDrawGridLines(false)
        yAxis.setDrawAxisLine(false)
        yAxis1.setDrawAxisLine(false)
        yAxis.textColor = Color.TRANSPARENT
        yAxis1.textColor = Color.TRANSPARENT
        yAxis.xOffset = 7f
        yAxis1.xOffset = 7f

        //legend
        var legend = lineChart.legend
        legend.isEnabled = false

        //data
        setDataStyle(dataSets)

    }

    private fun setDataStyle(datasets: List<LineDataSet>){
        datasets.forEach {
            it.mode = LineDataSet.Mode.CUBIC_BEZIER
            it.lineWidth = 4f
            it.color = Color.parseColor("#2296eb")
            it.setCircleColor(Color.parseColor("#2296eb"))
            it.circleHoleRadius = 3f
            it.circleRadius = 4f
            it.isHighlightEnabled = true
            it.highLightColor = Color.TRANSPARENT
            it.valueTextSize = 14f
            it.valueTextColor = Color.parseColor("#000000")
        }
        var df = DecimalFormat("##0")
        dataSets[0].valueFormatter = object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                return df.format(value)+"℃"
            }
        }
        datasets[1].setDrawCircles(false)
        datasets[1].setDrawCircleHole(false)
        datasets[1].lineWidth = -1f
        datasets[1].isHighlightEnabled = false
        datasets[1].color = Color.parseColor("#ffffff")
        var index = -1
        dataSets[1].valueFormatter = object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                index++
                return item.hours[index%item.hours.size].wea
            }
        }
    }

    private fun initLineChart(){

        mPointValues = arrayListOf()
        mPointValues1 = arrayListOf()
        mLables = arrayListOf()

        item.hours.forEachIndexed { index, hour ->
            mLables.add(hour.day.substring(3, 5) + ":00")
            var y: Float = hour.tem.substring(0, hour.tem.lastIndex).toFloat()
            mPointValues.add(Entry(index.toFloat(),y))
        }
        var dataSet = LineDataSet(mPointValues,null)

        item.hours.forEachIndexed { index, hour ->
            var y: Float = hour.tem.substring(0, hour.tem.lastIndex).toFloat()
            mPointValues1.add(Entry(index.toFloat(),y+(dataSet.yMax-dataSet.yMin)/8))
        }

        var dataSet1 = LineDataSet(mPointValues1,null)

        dataSets = listOf(dataSet,dataSet1)
        lineData = LineData(dataSets)
        setStyle()
        setmarkView()

        lineChart.data = lineData
        lineChart.invalidate()
    }

    private fun setmarkView() {
        var mv = XYMarkerView()
        lineChart.marker = mv
    }



    inner class XYMarkerView : MarkerView(context, R.layout.chart_marker_view) {
        private val tvContent: TextView = rootView.findViewById(R.id.test)
        private var index: Int = 0

        val ARROW_SIZE = 20 // 箭头的大小
        private val CIRCLE_OFFSET = 10f//因为我这里的折点是圆圈，所以要偏移，防止直接指向了圆心
        private val STOKE_WIDTH = 5f//这里对于stroke_width的宽度也要做一定偏移

        override fun refreshContent(e: Entry, highlight: Highlight) {
            super.refreshContent(e, highlight)
            index = highlight.dataSetIndex//这个方法用于获得折线是哪根
            var hour = item.hours[e.x.toInt()]
            tvContent.text = hour.win + hour.win_speed
        }

        override fun getOffsetForDrawingAtPoint(posX: Float, posY: Float): MPPointF {
            val offset = offset
            val chart = lineChart
            val width = width.toFloat()
            val height = height.toFloat()
            // posY \posX 指的是markerView左上角点在图表上面的位置
            //处理Y方向
            if (posY <= height + ARROW_SIZE) {// 如果点y坐标小于markerView的高度，如果不处理会超出上边界，处理了之后这时候箭头是向上的，我们需要把图标下移一个箭头的大小
                offset.y = ARROW_SIZE.toFloat()
            } else {//否则属于正常情况，因为我们默认是箭头朝下，然后正常偏移就是，需要向上偏移markerView高度和arrow size，再加一个stroke的宽度，因为你需要看到对话框的上面的边框
                offset.y = -height - ARROW_SIZE.toFloat() - STOKE_WIDTH // 40 arrow height   5 stroke width
            }
            //处理X方向，分为3种情况，1、在图表左边 2、在图表中间 3、在图表右边
            //
            if (posX > chart.width - width) {//如果超过右边界，则向左偏移markerView的宽度
                offset.x = -width
            } else {//默认情况，不偏移（因为是点是在左上角）
                offset.x = 0f
                if (posX > width / 2) {//如果大于markerView的一半，说明箭头在中间，所以向右偏移一半宽度
                    offset.x = -(width / 2)
                }
            }
            return offset
        }

        override fun draw(canvas: Canvas, posX: Float, posY: Float) {
            val paint = Paint()//绘制边框的画笔
            paint.strokeWidth = STOKE_WIDTH
            paint.style = Paint.Style.STROKE
            paint.strokeJoin = Paint.Join.ROUND
            paint.color = Color.TRANSPARENT

            val whitePaint = Paint()//绘制底色的画笔
            whitePaint.style = Paint.Style.FILL
            whitePaint.color = Color.parseColor("#2296eb")

            val chart = lineChart
            val width = width.toFloat()
            val height = height.toFloat()

            val offset = getOffsetForDrawingAtPoint(posX, posY)
            val saveId = canvas.save()

            var path: Path
            if (posY < height + ARROW_SIZE) {//处理超过上边界
                path = Path()
                path.moveTo(0f, 0f)
                if (posX > chart.width - width) {//超过右边界
                    path.lineTo(width - ARROW_SIZE, 0f)
                    path.lineTo(width, -ARROW_SIZE + CIRCLE_OFFSET)
                    path.lineTo(width, 0f)
                } else {
                    if (posX > width / 2) {//在图表中间
                        path.lineTo(width / 2 - ARROW_SIZE / 2, 0f)
                        path.lineTo(width / 2, -ARROW_SIZE + CIRCLE_OFFSET)
                        path.lineTo(width / 2 + ARROW_SIZE / 2, 0f)
                    } else {//超过左边界
                        path.lineTo(0f, -ARROW_SIZE + CIRCLE_OFFSET)
                        path.lineTo(0f + ARROW_SIZE, 0f)
                    }
                }
                path.lineTo(0 + width, 0f)
                path.lineTo(0 + width, 0 + height)
                path.lineTo(0f, 0 + height)
                path.lineTo(0f, 0f)
                path.offset(posX + offset.x, posY + offset.y)
            } else {//没有超过上边界
                path = Path()
                path.moveTo(0f, 0f)
                path.lineTo(0 + width, 0f)
                path.lineTo(0 + width, 0 + height)
                if (posX > chart.width - width) {
                    path.lineTo(width, height + ARROW_SIZE - CIRCLE_OFFSET)
                    path.lineTo(width - ARROW_SIZE, 0 + height)
                    path.lineTo(0f, 0 + height)
                } else {
                    if (posX > width / 2) {
                        path.lineTo(width / 2 + ARROW_SIZE / 2, 0 + height)
                        path.lineTo(width / 2, height + ARROW_SIZE - CIRCLE_OFFSET)
                        path.lineTo(width / 2 - ARROW_SIZE / 2, 0 + height)
                        path.lineTo(0f, 0 + height)
                    } else {
                        path.lineTo(0f + ARROW_SIZE, 0 + height)
                        path.lineTo(0f, height + ARROW_SIZE - CIRCLE_OFFSET)
                        path.lineTo(0f, 0 + height)
                    }
                }
                path.lineTo(0f, 0f)
                path.offset(posX + offset.x, posY + offset.y)
            }

            // translate to the correct position and draw
            canvas.drawPath(path, whitePaint)
            canvas.drawPath(path, paint)
            canvas.translate(posX + offset.x, posY + offset.y)
            draw(canvas)
            canvas.restoreToCount(saveId)
        }




    }

}