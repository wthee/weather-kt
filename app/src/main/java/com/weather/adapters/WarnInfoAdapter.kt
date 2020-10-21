package com.weather.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.weather.databinding.ItemWarnBinding
import com.weather.util.ZhColorUtil
import interfaces.heweather.com.interfacesmodule.bean.WarningBean

class WarnInfoAdapter() :
    ListAdapter<WarningBean.WarningBeanBase, WarnInfoAdapter.ViewHolder>(WarnDiffCallback()) {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {

        return ViewHolder(
            ItemWarnBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }


    class ViewHolder(private val binding: ItemWarnBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: WarningBean.WarningBeanBase) {
            binding.apply {
                warnType.text = item.typeName + "预警："
                warnLv.text = item.level
                warnLv.setTextColor(ZhColorUtil.formColor(item.level))
                warnText.text = item.text
                warnSender.text = item.sender
                warnTitle.text = item.title
            }
        }

    }
}


class WarnDiffCallback : DiffUtil.ItemCallback<WarningBean.WarningBeanBase>() {

    override fun areItemsTheSame(
        oldItem: WarningBean.WarningBeanBase,
        newItem: WarningBean.WarningBeanBase
    ): Boolean {
        return oldItem == newItem
    }

    override fun areContentsTheSame(
        oldItem: WarningBean.WarningBeanBase,
        newItem: WarningBean.WarningBeanBase
    ): Boolean {
        return oldItem.id == newItem.id
    }
}