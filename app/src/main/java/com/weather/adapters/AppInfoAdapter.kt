package com.weather


import android.appwidget.AppWidgetManager
import android.content.Intent
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.weather.MainActivity.Companion.editor
import com.weather.data.AppInfo
import com.weather.databinding.ItemAppinfoBinding
import com.weather.setting.WidgetSettingClickFragment
import com.weather.setting.WidgetSettingClickFragment.Companion.pn


class AppInfoAdapter : ListAdapter<AppInfo, AppInfoAdapter.ViewHolder>(AppInfoDiffCallback()) {

    var wc = MainActivity.sharedPreferences.getInt("lastAdapter",WidgetSettingClickFragment.lastAdapter)


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

    private fun createOnClickListener(appInfo: AppInfo, p: Int): View.OnClickListener {
        return View.OnClickListener {

            pn[wc] = appInfo.packageName

            editor.putString("appInfo1",pn[0])
            editor.putString("appInfo2",pn[1])
            editor.putString("appInfo3",pn[2])
            editor.apply()
            var intent = Intent(AppWidgetManager.ACTION_APPWIDGET_UPDATE)
            MyApplication.context.sendBroadcast(intent)

            notifyDataSetChanged()
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, p: Int) {
        val info = getItem(p)

        holder.apply {
            bind(info, createOnClickListener(info,p))
            itemView.tag = info
            if(info.packageName == pn[wc]){
                itemView.setBackgroundColor(Color.parseColor("#C0C0C0"))
            }else{
                itemView.setBackgroundColor(Color.parseColor("#ffffff"))
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