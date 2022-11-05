package com.weather.ui.info

import android.graphics.Color
import android.graphics.Matrix
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.ValueFormatter
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.weather.R
import com.weather.adapters.IndicesInfoAdapter
import com.weather.adapters.WarnInfoAdapter
import com.weather.databinding.FragmentWeatherInfoBinding
import com.weather.ui.CommonBottomSheetDialogFragment
import com.weather.ui.main.WeatherFragment
import com.weather.util.InjectorUtil
import com.weather.util.WeatherUtil
import com.weather.util.ZhColorUtil
import com.weather.util.formatDate
import interfaces.heweather.com.interfacesmodule.bean.weather.WeatherHourlyBean
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*


class WeatherInfoFragment(
    private val date: String
) : CommonBottomSheetDialogFragment() {

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

    private val viewModel by activityViewModels<WeatherInfoViewModel> {
        InjectorUtil.getWeatherInfoViewModelFactory()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentWeatherInfoBinding.inflate(inflater, container, false)

        axisColor = ResourcesCompat.getColor(resources, R.color.theme, null)
        gridColor = ResourcesCompat.getColor(resources, R.color.hr, null)
        labColor = ResourcesCompat.getColor(resources, R.color.main_text, null)
        lineChart = binding.lineChart

        val city = WeatherUtil.getCity()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd")
        val today = dateFormat.format(Date(System.currentTimeMillis()))
        val isToday = date == today
        binding.weaDay.text =
            date.substring(8, 10) + "日 " + WeatherUtil.dateToWeek(date) + WeatherUtil.dayAfter(date)

        //获取小时预警
        if (isToday) {
            lineChart.visibility = View.VISIBLE
            lineChart.setNoDataText("数据获取中...")
            lineChart.setNoDataTextColor(R.color.main_text)
            //小时预警
            viewModel.getHourlyWeather(city)
            viewModel.hourlyInfos.observe(viewLifecycleOwner, Observer {
                //初始化温度折线图
                initLineChart(it)
            })
            //获取预警信息
            val adapter = WarnInfoAdapter()
            binding.listWarn.adapter = adapter
            viewModel.getWarning(city)
            viewModel.warningInfo.observe(viewLifecycleOwner, Observer {
                if (it != null && it.beanBaseList.size > 0) {
                    adapter.submitList(it.beanBaseList)
                }
            })
            //获取生活指数信息
            val adapter2 = IndicesInfoAdapter()
            binding.listIndices.adapter = adapter2
            viewModel.getIndices(city)
            viewModel.indicesBean.observe(viewLifecycleOwner, Observer {
                if (it != null && it.dailyList.size > 0) {
                    adapter2.submitList(it.dailyList)
                }
            })
        } else {
            binding.nodata.visibility = View.VISIBLE
        }

        //获取空气质量
        viewModel.getAirInfo(city)
        viewModel.airDailyBean.observe(viewLifecycleOwner, Observer {
            if (it != null && it.airDaily.size > 0) {
                binding.apply {
                    val air = it.airDaily.find { daily ->
                        daily.fxDate == date
                    }
                    if (air != null) {
                        layoutAir.visibility = View.VISIBLE
                        airAqi.text = air.aqi + "/" + air.category
                        airAqi.setTextColor(ZhColorUtil.formColor(air.category ?: "良"))
                    }
                }
            }
        })


        //显示日月信息
        val sunMoonData = WeatherFragment.sunMoonDatas.find {
            it.date == date
        }
        //日出、日落信息
        if (sunMoonData != null && sunMoonData.sunRise != null && sunMoonData.sunSet != null) {
            binding.apply {
                sunRiseSet.text = "${sunMoonData.sunRise} / ${sunMoonData.sunSet}"
            }
        }
        return binding.root
    }


    private fun setStyle(hourlyBean: WeatherHourlyBean) {
        //linechart style
        lineChart.description = null
        lineChart.setDrawBorders(false)
        lineChart.isScaleXEnabled = true
        lineChart.isScaleYEnabled = false
        val m = Matrix()
        //两个参数分别是x,y轴的缩放比例。例如：将x轴的数据放大为之前的1.5倍
        m.postScale(hourlyBean.hourly.size / 2 / 8f * 2f, 1f)
        lineChart.viewPortHandler.refresh(m, lineChart, false)//将图表动画显示之前进行缩放
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
        setData(dataSet, hourlyBean)

    }

    private fun setData(dataset: LineDataSet, hourlyBean: WeatherHourlyBean) {
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
            // format
            val df = DecimalFormat("##0")
            valueFormatter = object : ValueFormatter() {
                override fun getPointLabel(entry: Entry?): String {
                    return df.format(entry!!.y) + "℃\n" + hourlyBean.hourly[entry.x.toInt()].text
                }
            }
        }
    }

    private fun initLineChart(data: WeatherHourlyBean) {
        //折线点上的值
        mPointValues = arrayListOf()
        mLables = arrayListOf()

        //x轴时间
        data.hourly.forEachIndexed { index, hour ->
            if (index < 10) {
                mLables.add(hour.fxTime.formatDate().substring(11, 16))
                val y: Float = hour.temp.toFloat()
                mPointValues.add(Entry(index.toFloat(), y))
            }
        }
        dataSet = LineDataSet(mPointValues, null)

        lineData = LineData(dataSet)
        setStyle(data)
        lineChart.data = lineData
        lineChart.invalidate()
    }


}