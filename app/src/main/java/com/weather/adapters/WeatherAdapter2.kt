package com.weather

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.weather.data.Data
import com.weather.databinding.ItemWeather2Binding
import com.weather.ui.main.WeatherFragment

class WeatherAdapter2 : ListAdapter<Data, WeatherAdapter2.ViewHolder>(WeatherDiffCallback()) {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        return ViewHolder(
            DataBindingUtil.inflate(
                LayoutInflater.from(parent.context),
                R.layout.item_weather2, parent, false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val data = getItem(position)
        holder.apply {
            bind(data)
            itemView.tag = data
        }
    }

    class ViewHolder(
        private val binding: ItemWeather2Binding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: Data) {
            binding.apply {
                data = item
                isGone = if(WeatherFragment.nlIsGone){
                    View.GONE
                }else{
                    View.VISIBLE
                }
                executePendingBindings()
            }
        }
    }
}
