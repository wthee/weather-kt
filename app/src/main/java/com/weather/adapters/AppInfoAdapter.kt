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
import com.weather.data.AppInfo
import com.weather.databinding.ItemAppinfoBinding
import com.weather.setting.WidgetSettingClickFragment


class AppInfoAdapter : ListAdapter<AppInfo, AppInfoAdapter.ViewHolder>(AppInfoDiffCallback()) {

    private var clickIndex  = arrayListOf(0,0,0)
    private var pn = arrayListOf("com.weather","com.weather","com.weather")
    private var wc = 0

    fun getClickIndex() = clickIndex
    fun getPackageName() = pn
    fun setWC(which: Int){
        wc = which
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
            clickIndex[wc] = p
            pn[wc] = appInfo.packageName
            it.setBackgroundColor(Color.parseColor("#2296eb"))
            MainActivity.editor.putString("appInfo1",pn[0])
            MainActivity.editor.putString("appInfo2",pn[1])
            MainActivity.editor.putString("appInfo3",pn[2])
            MainActivity.editor.apply()
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
            if(p == clickIndex[wc]){
                itemView.setBackgroundColor(Color.parseColor("#2296eb"))
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