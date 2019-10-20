package com.weather.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.weather.R
import com.weather.data.model.weather.Data
import com.weather.databinding.ItemWeather1Binding
import com.weather.databinding.ItemWeatherBinding
import com.weather.ui.info.WeatherInfoFragment
import com.weather.ui.main.WeatherFragment
import com.weather.util.ActivityUtil

class WeatherAdapter : ListAdapter<Data, WeatherAdapter.ViewHolder>(WeatherDiffCallback()) {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {

        var layoutId = R.layout.item_weather
        when(WeatherFragment.styleType){
            0 -> layoutId = R.layout.item_weather
            1 -> layoutId = R.layout.item_weather1
        }

        return ViewHolder(
            DataBindingUtil.inflate(
                LayoutInflater.from(parent.context),
                layoutId, parent, false
            )
        )
    }

    private fun createOnClickListener(item: Data): View.OnClickListener {
        return View.OnClickListener {
            var wf = WeatherInfoFragment(item)
            wf.show(ActivityUtil.instance.currentActivity!!.supportFragmentManager.beginTransaction(),"123")
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
        private val binding: ViewDataBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: Data, listener: View.OnClickListener) {
            when(WeatherFragment.styleType){
                0 -> {
                    binding as ItemWeatherBinding
                    binding.apply {
                        data = item
                        onClick = listener
                        isGone = if (WeatherFragment.lunarGone) {
                            View.GONE
                        } else {
                            View.VISIBLE
                        }
                    }
                }
                1 -> {
                    binding as ItemWeather1Binding
                    binding.apply {
                        data = item
                        onClick = listener
                        isGone = if (WeatherFragment.lunarGone) {
                            View.GONE
                        } else {
                            View.VISIBLE
                        }
                    }
                }
            }
            binding.executePendingBindings()
        }
    }
}



class WeatherDiffCallback : DiffUtil.ItemCallback<Data>() {

    override fun areItemsTheSame(oldItem: Data, newItem: Data): Boolean {
        return oldItem == newItem
    }

    override fun areContentsTheSame(oldItem: Data, newItem: Data): Boolean {
        return oldItem.date == newItem.date
    }
}