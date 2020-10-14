package com.weather.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.weather.MainActivity
import com.weather.MyApplication
import com.weather.R
import com.weather.databinding.ItemWeather1Binding
import com.weather.databinding.ItemWeatherBinding
import com.weather.util.LunarUtil
import com.weather.util.WeatherUtil.dateToWeek
import com.weather.util.WeatherUtil.formatTip
import interfaces.heweather.com.interfacesmodule.bean.weather.WeatherDailyBean
import java.text.SimpleDateFormat
import java.util.*

class WeatherAdapter(
    private val style: Int
) :
    ListAdapter<WeatherDailyBean.DailyBean, WeatherAdapter.ViewHolder>(WeatherDiffCallback()) {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {

        return ViewHolder(
            if (style == 0)
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
            bind(data, createOnClickListener(data), style)
            itemView.tag = data
        }
    }


    class ViewHolder(
        private val binding: ViewBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(
            item: WeatherDailyBean.DailyBean,
            listener: View.OnClickListener,
            style: Int
        ) {
            binding.root.animation =
                AnimationUtils.loadAnimation(MyApplication.context, R.anim.item_load)
            //农历
            val today = Calendar.getInstance()
            today.time = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA)
                .parse(item.fxDate + " 00:00:00") ?: Date()
            val dateNlText = LunarUtil(today).toString()
            //是否显示农历
            val showNL = MainActivity.spSetting.getBoolean("show_nl", true)
            when (style) {
                0 -> {
                    binding as ItemWeatherBinding
                    binding.apply {
                        date.text = item.fxDate
                        dateNl.text = dateNlText
                        week.text = dateToWeek(item.fxDate)
                        tems.text = "${item.tempMin}℃ ~ ${item.tempMax}℃"
                        wea.text = item.textDay
                        tip.text = formatTip(item)
                        root.setOnClickListener(listener)
                        dateNl.visibility = if (showNL) View.VISIBLE else View.GONE
                    }
                }
                1 -> {
                    binding as ItemWeather1Binding
                    binding.apply {
                        month.text = item.fxDate.substring(5, 7)
                        day.text = item.fxDate.substring(8, 10)
                        dateNl.text = dateNlText
                        week.text = dateToWeek(item.fxDate)
                        tems.text = "${item.tempMin}℃ ~ ${item.tempMax}℃"
                        wea.text = item.textDay
                        tip.text = formatTip(item)
                        root.setOnClickListener(listener)
                        dateNl.visibility = if (showNL) View.VISIBLE else View.GONE
                    }
                }
            }
        }

    }
}


class WeatherDiffCallback : DiffUtil.ItemCallback<WeatherDailyBean.DailyBean>() {

    override fun areItemsTheSame(
        oldItem: WeatherDailyBean.DailyBean,
        newItem: WeatherDailyBean.DailyBean
    ): Boolean {
        return oldItem == newItem
    }

    override fun areContentsTheSame(
        oldItem: WeatherDailyBean.DailyBean,
        newItem: WeatherDailyBean.DailyBean
    ): Boolean {
        return oldItem.fxDate == newItem.fxDate
    }
}