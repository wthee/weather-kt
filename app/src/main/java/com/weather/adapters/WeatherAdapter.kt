package com.weather.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.weather.data.model.weather.Data
import com.weather.databinding.ItemWeather1Binding
import com.weather.databinding.ItemWeatherBinding
import com.weather.ui.info.WeatherInfoFragment
import com.weather.ui.main.WeatherFragment
import com.weather.util.ActivityUtil
import com.weather.util.LunarUtil
import interfaces.heweather.com.interfacesmodule.bean.weather.WeatherDailyBean
import java.text.SimpleDateFormat
import java.util.*

class WeatherAdapter :
    ListAdapter<WeatherDailyBean.DailyBean, WeatherAdapter.ViewHolder>(WeatherDiffCallback()) {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {

        return ViewHolder(
            if (WeatherFragment.styleType == 0)
                ItemWeatherBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            else
                ItemWeather1Binding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    private fun createOnClickListener(item: WeatherDailyBean.DailyBean): View.OnClickListener {
        return View.OnClickListener {
            //TODO
//            val wf = WeatherInfoFragment(item)
//            wf.show(
//                ActivityUtil.instance.currentActivity!!.supportFragmentManager.beginTransaction(),
//                "123"
//            )
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val data = getItem(position)
        holder.apply {
            bind(data, createOnClickListener(data))
            itemView.tag = data
        }
    }


    class ViewHolder(
        private val binding: ViewBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: WeatherDailyBean.DailyBean, listener: View.OnClickListener) {
            val today = Calendar.getInstance()
            today.time =
                SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(item.fxDate + " 00:00:00")
            val dateNlText = LunarUtil(today).toString()
            when (WeatherFragment.styleType) {
                0 -> {
                    binding as ItemWeatherBinding
                    binding.apply {
                        date.text = item.fxDate
                        dateNl.text = dateNlText
                        //TODO 星期
//                        week.text = item.week
                        tems.text = "${item.tempMin}℃ ~ ${item.tempMax}℃"
                        wea.text = item.textDay
                        tip.text = formatTip(item)
                        root.setOnClickListener(listener)
                        dateNl.visibility = if (WeatherFragment.lunarGone) {
                            View.GONE
                        } else {
                            View.VISIBLE
                        }
                    }
                }
                1 -> {
                    binding as ItemWeather1Binding
                    binding.apply {
                        //TODO
//                        day.text = item.d
//                        month.text = item.m
//                        dateNl.text = dateNlText
//                        week.text = item.week
//                        tems.text = item.tems
//                        wea.text = item.wea
                        tip.text = formatTip(item)
                        root.setOnClickListener(listener)
                        dateNl.visibility = if (WeatherFragment.lunarGone) {
                            View.GONE
                        } else {
                            View.VISIBLE
                        }
                    }
                }
            }
        }

        private fun formatTip(dailyBean: WeatherDailyBean.DailyBean) =
            when (dailyBean.textDay.length) {
                1 -> "下雨天，记得带伞"
                2 -> when (dailyBean.textDay) {
                    "小雨" -> "雨虽小，注意别感冒"
                    "中雨" -> "记得随身携带雨伞"
                    "大雨" -> "出门最好穿雨衣"
                    "阵雨" -> "阵雨来袭，记得带伞"
                    "暴雨" -> "尽量避免户外活动"
                    else -> "没有你的天气"
                }
                3 -> {
                    if (dailyBean.textDay.contains("转"))
                        "天气多变，照顾好自己"
                    else
                        when (dailyBean.textDay) {
                            "雷阵雨" -> "尽量减少户外活动"
                            "大暴雨" -> "尽量避免户外活动"
                            "雨夹雪" -> "道路湿滑，出行要谨慎"
                            else -> "没有你的天气"
                        }
                }
                else -> "天气多变，照顾好自己"
            }
    }
}


class WeatherDiffCallback : DiffUtil.ItemCallback<WeatherDailyBean.DailyBean>() {

    override fun areItemsTheSame(oldItem: WeatherDailyBean.DailyBean, newItem: WeatherDailyBean.DailyBean): Boolean {
        return oldItem == newItem
    }

    override fun areContentsTheSame(oldItem: WeatherDailyBean.DailyBean, newItem: WeatherDailyBean.DailyBean): Boolean {
        return oldItem.fxDate == newItem.fxDate
    }
}