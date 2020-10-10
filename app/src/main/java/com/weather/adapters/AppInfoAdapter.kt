package com.weather.adapters


import android.appwidget.AppWidgetManager
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.core.content.edit
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.weather.MainActivity.Companion.sharedPreferences
import com.weather.MyApplication
import com.weather.R
import com.weather.data.model.AppInfo
import com.weather.databinding.ItemAppinfoBinding
import com.weather.ui.setting.WidgetSettingClickFragment
import com.weather.ui.setting.WidgetSettingClickFragment.Companion.mSourceList
import com.weather.ui.setting.WidgetSettingClickFragment.Companion.pn
import skin.support.content.res.SkinCompatResources

//应用列表
class AppInfoAdapter : ListAdapter<AppInfo, AppInfoAdapter.ViewHolder>(AppInfoDiffCallback()), Filterable {

    private lateinit var mFilterList : MutableList<AppInfo>

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(charSequence: CharSequence): FilterResults {
                //搜索关键词
                val charString = charSequence.toString()
                mFilterList = if (charString.isEmpty()) {
                    mSourceList
                } else {
                    val filteredList = arrayListOf<AppInfo>()
                    for (str in mSourceList) {
                        if (str.appName.contains(charString)) {
                            filteredList.add(str)
                        }
                    }
                    filteredList
                }

                val filterResults = FilterResults()
                filterResults.values = mFilterList
                return filterResults
            }

            override fun publishResults(
                charSequence: CharSequence,
                filterResults: FilterResults
            ) {

                mFilterList = filterResults.values as MutableList<AppInfo>
                //刷新数据
                submitList(mFilterList)
                notifyDataSetChanged()
            }
        }
    }

    var wc = sharedPreferences.getInt("lastAdapter",
        WidgetSettingClickFragment.lastAdapter)


    fun setWC(value: Int) {
        wc = value
        sharedPreferences.edit {
            putInt("lastAdapter",wc)
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        return ViewHolder(
            ItemAppinfoBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )
    }

    private fun createOnClickListener(appInfo: AppInfo): View.OnClickListener {
        return View.OnClickListener {
            pn[wc] = appInfo.packageName
            sharedPreferences.edit{
                putString("appInfo1",pn[0])
                putString("appInfo2",pn[1])
                putString("appInfo3",pn[2])
            }

            val intent = Intent(AppWidgetManager.ACTION_APPWIDGET_UPDATE)
            MyApplication.context.sendBroadcast(intent)
            notifyDataSetChanged()
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, p: Int) {
        val info = getItem(p)

        holder.apply {
            bind(info, createOnClickListener(info))
            itemView.tag = info
            if(info.packageName == pn[wc]){
                itemView.setBackgroundColor(SkinCompatResources.getColor(MyApplication.context,R.color.placeholder_text))
            }else{
                itemView.setBackgroundColor(SkinCompatResources.getColor(MyApplication.context,R.color.background))
            }
        }
    }


    class ViewHolder(
        private val binding: ItemAppinfoBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(appInfo: AppInfo, listener: View.OnClickListener) {
            binding.apply {
                appName.text = appInfo.appName
                packageName.text = appInfo.packageName
                appIcon.background = appInfo.appIcon
                root.setOnClickListener(listener)
            }
        }
    }
}


private class AppInfoDiffCallback : DiffUtil.ItemCallback<AppInfo>() {

    override fun areItemsTheSame(oldItem: AppInfo, newItem: AppInfo): Boolean {
        return oldItem == newItem
    }

    override fun areContentsTheSame(oldItem: AppInfo, newItem: AppInfo): Boolean {
        return oldItem.packageName == newItem.packageName
    }
}