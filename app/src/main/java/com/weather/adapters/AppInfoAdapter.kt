package com.weather.adapters


import android.appwidget.AppWidgetManager
import android.content.Intent
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.weather.MainActivity
import com.weather.MainActivity.Companion.editor
import com.weather.MyApplication
import com.weather.R
import com.weather.data.model.AppInfo
import com.weather.databinding.ItemAppinfoBinding
import com.weather.ui.setting.WidgetSettingClickFragment
import com.weather.ui.setting.WidgetSettingClickFragment.Companion.mSourceList
import com.weather.ui.setting.WidgetSettingClickFragment.Companion.pn


class AppInfoAdapter : ListAdapter<AppInfo, AppInfoAdapter.ViewHolder>(AppInfoDiffCallback()), Filterable {


    private lateinit var mFilterList : MutableList<AppInfo>

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(charSequence: CharSequence): FilterResults {
                val charString = charSequence.toString()
                if (charString.isEmpty()) {
                    mFilterList = mSourceList
                } else {
                    val filteredList = arrayListOf<AppInfo>()
                    for (str in mSourceList) {
                        //这里根据需求，添加匹配规则
                        if (str.appName.contains(charString)) {
                            filteredList.add(str)
                        }
                    }
                    mFilterList = filteredList
                }

                val filterResults = FilterResults()
                filterResults.values = mFilterList
                return filterResults
            }

            override fun publishResults(
                charSequence: CharSequence,
                filterResults: FilterResults
            ) {

                @SuppressWarnings("unchecked")
                mFilterList = filterResults.values as MutableList<AppInfo>
                //刷新数据
                submitList(mFilterList)
                notifyDataSetChanged()
            }
        }
    }

    var wc = MainActivity.sharedPreferences.getInt("lastAdapter",
        WidgetSettingClickFragment.lastAdapter)


    fun setWC(value: Int) {
        wc = value
        MainActivity.editor.putInt("lastAdapter",wc)
        MainActivity.editor.apply()
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        return ViewHolder(
            DataBindingUtil.inflate(
                LayoutInflater.from(parent.context),
                R.layout.item_appinfo, parent, false
            )
        )
    }

    private fun createOnClickListener(appInfo: AppInfo): View.OnClickListener {
        return View.OnClickListener {

            pn[wc] = appInfo.packageName

            editor.putString("appInfo1",pn[0])
            editor.putString("appInfo2",pn[1])
            editor.putString("appInfo3",pn[2])
            editor.apply()
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
                itemView.setBackgroundColor(Color.parseColor("#C0C0C0"))
            }else{
                if(MainActivity.onNight){
                    itemView.setBackgroundColor(Color.parseColor("#434343"))
                }else{
                    itemView.setBackgroundColor(Color.parseColor("#ffffff"))
                }
            }
        }
    }


    class ViewHolder(
        private val binding: ItemAppinfoBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(appInfo: AppInfo, listener: View.OnClickListener) {
            binding.apply {
                info = appInfo
                onClick = listener
                executePendingBindings()
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