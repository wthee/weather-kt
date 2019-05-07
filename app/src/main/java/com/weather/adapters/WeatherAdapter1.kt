package com.weather

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.weather.data.Data
import com.weather.databinding.ItemWeather1Binding
import com.weather.util.ActivityUtil

class WeatherAdapter1 : ListAdapter<Data, WeatherAdapter1.ViewHolder>(WeatherDiffCallback()) {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        return ViewHolder(
            DataBindingUtil.inflate(
                LayoutInflater.from(parent.context),
                R.layout.item_weather1, parent, false
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
        private val binding: ItemWeather1Binding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: Data, listener: View.OnClickListener) {
            binding.apply {
                data = item
                onClick = listener
                isGone = if (WeatherFragment.nlIsGone) {
                    View.GONE
                } else {
                    View.VISIBLE
                }
                executePendingBindings()
            }
        }
    }
}



public class WeatherDiffCallback : DiffUtil.ItemCallback<Data>() {

    override fun areItemsTheSame(oldItem: Data, newItem: Data): Boolean {
        return oldItem == newItem
    }

    override fun areContentsTheSame(oldItem: Data, newItem: Data): Boolean {
        return oldItem.date == newItem.date
    }
}