package com.weather.ui.info

import android.graphics.*
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
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
import com.weather.R
import com.weather.data.model.weather.Data
import com.weather.databinding.FragmentWeatherInfoBinding
import com.weather.util.ShareUtil
import java.text.DecimalFormat


class WeatherInfoFragment(itemBundle: Data) : DialogFragment() {

    private lateinit var binding: FragmentWeatherInfoBinding
    private lateinit var lineChart: LineChart
    private lateinit var mPointValues: ArrayList<Entry>
    private lateinit var mLables: ArrayList<String>
    private lateinit var dataSet: LineDataSet
    private lateinit var lineData: LineData
    private var axisWidth: Float = 1f
    private var axisColor: Int = 0
    private var gridColor: Int = 0
    private var labColor: Int = 0
    private var item = itemBundle

    private lateinit var dm: DisplayMetrics
    private lateinit var params: WindowManager.LayoutParams

    @Suppress("DEPRECATION")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentWeatherInfoBinding.inflate(inflater,container,false)


        axisColor = ResourcesCompat.getColor(resources,R.color.theme, null)
        gridColor = ResourcesCompat.getColor(resources,R.color.hr, null)
        labColor = ResourcesCompat.getColor(resources,R.color.main_text, null)
        lineChart = binding.lineChart

        //初始化温度折线图
        initLineChart()

        val air = binding.air
        val alarm = binding.alarm

        alarm.visibility = if ( item.alarm.alarm_type != "") {
            View.VISIBLE
        } else {
            View.GONE
        }

        air.visibility = if (item.air_tips != "") {
            View.VISIBLE
        } else {
            View.GONE
        }

        //点击分享
        binding.weaDay.setOnClickListener {
            val sView = binding.root
            sView.isDrawingCacheEnabled = true
            sView.buildDrawingCache()
            val bp = Bitmap.createBitmap(sView.drawingCache)
            val uri = Uri.parse(
                MediaStore.Images.Media.insertImage(
                    activity!!.contentResolver,
                    bp,
                    null,
                    null
                )
            )
            ShareUtil.shareImg(uri,this.context!!)
        }


        return binding.root
    }



    private fun setStyle() {

        //linechart style
        lineChart.description = null
        lineChart.setDrawBorders(false)
        lineChart.isScaleXEnabled = true
        lineChart.isScaleYEnabled = false
        val m = Matrix()
        //两个参数分别是x,y轴的缩放比例。例如：将x轴的数据放大为之前的1.5倍
        m.postScale(item.hours.size / 8f * 2f,1f)
        lineChart.viewPortHandler.refresh(m, lineChart,false)//将图表动画显示之前进行缩放
        //x
        lineChart.xAxis.apply {
            setDrawAxisLine(false)
            setDrawGridLines(false)
            valueFormatter = IndexAxisValueFormatter(mLables)
            textSize = 12f
            textColor = labColor
            granularity = 1f
            yOffset = -3f
            position = XAxis.XAxisPosition.BOTTOM
        }

        lineChart.axisLeft.apply {
            axisLineWidth = axisWidth
            xOffset = 10f
            setDrawGridLines(false)
            setDrawAxisLine(false)
            textColor = Color.TRANSPARENT
        }

        lineChart.axisRight.apply {
            axisLineWidth = axisWidth
            xOffset = 10f
            setDrawGridLines(false)
            setDrawAxisLine(false)
            textColor = Color.TRANSPARENT
        }

        //legend
        val legend = lineChart.legend
        legend.isEnabled = false

        //data
        setData(dataSet)

    }

    private fun setData(dataset: LineDataSet) {
        dataset.apply {
            mode = LineDataSet.Mode.CUBIC_BEZIER
            lineWidth = 4f
            color = axisColor
            setCircleColor(axisColor)
            circleHoleRadius = 3f
            circleRadius = 4f
            isHighlightEnabled = true
            highLightColor = Color.TRANSPARENT
            valueTextSize = 14f
            valueTextColor = labColor
            val df = DecimalFormat("##0")
            valueFormatter = object : ValueFormatter() {
                override fun getPointLabel(entry: Entry?): String {
                    return df.format(entry!!.y) + "℃\n" + item.hours[entry.x.toInt()].wea
                }
            }
        }
    }

    private fun initLineChart() {
        //折线点上的值
        mPointValues = arrayListOf()
        mLables = arrayListOf()

        item.hours.forEachIndexed { index, hour ->
            mLables.add(hour.hours.substring(0, 2) + ":00")
            val y: Float = hour.tem.toFloat()
            mPointValues.add(Entry(index.toFloat(), y))
        }
        dataSet = LineDataSet(mPointValues, null)

        lineData = LineData(dataSet)
        setStyle()
        setmarkView()

        lineChart.data = lineData
        lineChart.invalidate()
    }

    private fun setmarkView() {
        val mv = XYMarkerView()
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
            val hour = item.hours[e.x.toInt()]
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

            val path: Path
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