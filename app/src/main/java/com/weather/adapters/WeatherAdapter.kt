package com.weather.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.weather.R
import com.weather.data.model.Data
import com.weather.databinding.ItemWeatherBinding
import com.weather.ui.info.WeatherInfoFragment
import com.weather.ui.main.WeatherFragment
import com.weather.util.ActivityUtil

class WeatherAdapter : ListAdapter<Data, WeatherAdapter.ViewHolder>(WeatherDiffCallback()) {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        return ViewHolder(
            DataBindingUtil.inflate(
                LayoutInflater.from(parent.context),
                R.layout.item_weather, parent, false
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
        private val binding: ItemWeatherBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: Data, listener: View.OnClickListener) {
            binding.apply {
                data = item
                onClick = listener
                isGone = if (WeatherFragment.lunarGone) {
                    View.GONE
                } else {
                    View.VISIBLE
                }
                bjtype = WeatherFragment.styleType == 0
                executePendingBindings()
            }
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