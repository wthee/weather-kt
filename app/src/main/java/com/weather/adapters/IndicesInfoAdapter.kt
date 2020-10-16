package com.weather.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.weather.databinding.ItemIndicesBinding
import com.weather.util.ZhColorUtil
import interfaces.heweather.com.interfacesmodule.bean.IndicesBean

class IndicesInfoAdapter() :
    ListAdapter<IndicesBean.DailyBean, IndicesInfoAdapter.ViewHolder>(IndicesDiffCallback()) {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {

        return ViewHolder(
            ItemIndicesBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }


    class ViewHolder(private val binding: ItemIndicesBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: IndicesBean.DailyBean) {
            binding.apply {
                indicesType.text = item.name
                indicesText.text = item.text
                indicesCategory.text = item.category
                indicesCategory.setTextColor(ZhColorUtil.formColor(item.category))
            }
        }

    }
}


class IndicesDiffCallback : DiffUtil.ItemCallback<IndicesBean.DailyBean>() {

    override fun areItemsTheSame(
        oldItem: IndicesBean.DailyBean,
        newItem: IndicesBean.DailyBean
    ): Boolean {
        return oldItem == newItem
    }

    override fun areContentsTheSame(
        oldItem: IndicesBean.DailyBean,
        newItem: IndicesBean.DailyBean
    ): Boolean {
        return oldItem.name == newItem.name
    }
}