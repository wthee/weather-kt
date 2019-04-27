package com.weather

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.*
import android.widget.LinearLayout
import android.widget.TextView
import androidx.databinding.DataBindingUtil
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
import com.weather.data.Data
import com.weather.databinding.WeatherInfoBinding
import com.weather.util.TranslateWithTouchUtil
import com.weather.util.WeaColorUtil
import java.text.DecimalFormat


class WeatherInfoDialogFragment : DialogFragment() {

    companion object {
        fun getInstance(item: Data): WeatherInfoDialogFragment {
            var instance = WeatherInfoDialogFragment()
            val args = Bundle()
            args.putSerializable("item", item)
            instance!!.arguments = args
            return instance
        }
    }

    private lateinit var binding: WeatherInfoBinding
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

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater,R.layout.weather_info,container,false)

        item = arguments!!.get("item") as Data

        binding.apply{
            data = item
        }

        lineChart = binding.lineChart

        initLineChart()//初始化

        var air = binding.air
        var alarm = binding.alarm

        if (item.alarm != null && item.alarm.alarm_type != "") {
            alarm.visibility = View.VISIBLE
        } else {
            alarm.visibility = View.GONE
        }

        if (item.air_tips != null) {
            air.visibility = View.VISIBLE
        } else {
            air.visibility = View.GONE
        }

        binding.root.setOnTouchListener(TranslateWithTouchUtil.onTouch(binding.root,this))

        return binding.root
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

    private fun setDataStyle(datasets: List<LineDataSet>) {
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
                return df.format(value) + "℃"
            }
        }
        datasets[1].setDrawCircles(false)
        datasets[1].setDrawCircleHole(false)
        datasets[1].lineWidth = -1f
        datasets[1].isHighlightEnabled = false
        if(MainActivity.onNight){
            datasets[1].color = Color.parseColor("#434343")
        }else{
            datasets[1].color = Color.parseColor("#ffffff")
        }
        var index = -1
        dataSets[1].valueFormatter = object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                index++
                return item.hours[index % item.hours.size].wea
            }
        }
    }

    private fun initLineChart() {

        mPointValues = arrayListOf()
        mPointValues1 = arrayListOf()
        mLables = arrayListOf()

        item.hours.forEachIndexed { index, hour ->
            mLables.add(hour.day.substring(3, 5) + ":00")
            var y: Float = hour.tem.substring(0, hour.tem.lastIndex).toFloat()
            mPointValues.add(Entry(index.toFloat(), y))
        }
        var dataSet = LineDataSet(mPointValues, null)

        item.hours.forEachIndexed { index, hour ->
            var y: Float = hour.tem.substring(0, hour.tem.lastIndex).toFloat()
            var dif = dataSet.yMax - dataSet.yMin  //差值
            var offsetY = if (dif <= 1f) {  //偏移量
                lineChart.axisLeft.axisMaximum = dataSet.yMax + 1
                lineChart.axisLeft.axisMinimum = dataSet.yMin - 1
                2.0f / 8
            } else {
                dif / 8
            }
            mPointValues1.add(Entry(index.toFloat(), y + offsetY))
        }

        var dataSet1 = LineDataSet(mPointValues1, null)

        dataSets = listOf(dataSet, dataSet1)
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