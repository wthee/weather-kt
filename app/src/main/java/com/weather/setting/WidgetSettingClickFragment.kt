package com.weather.setting

import android.content.DialogInterface
import android.graphics.Color
import android.os.Bundle
import android.util.DisplayMetrics
import android.graphics.drawable.ColorDrawable
import android.view.*
import androidx.fragment.app.DialogFragment
import com.weather.R
import com.weather.util.TranslateWithTouchUtil
import android.content.pm.ApplicationInfo
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.recyclerview.widget.RecyclerView
import com.weather.AppInfoAdapter
import com.weather.MainActivity.Companion.editor
import com.weather.MainActivity.Companion.sharedPreferences
import com.weather.data.AppInfo


class WidgetSettingClickFragment : DialogFragment() {

    companion object {
        @Volatile
        private var instance: WidgetSettingClickFragment? = null

        fun getInstance() = instance ?: synchronized(this) {
            instance
                ?: WidgetSettingClickFragment().also { instance = it }
        }
    }

    private lateinit var dm: DisplayMetrics
    private lateinit var params: WindowManager.LayoutParams
    private lateinit var recycler: RecyclerView
    private lateinit var adapter: AppInfoAdapter
    private lateinit var groupCE: RadioGroup
    private lateinit var ce1: RadioButton
    private lateinit var ce2: RadioButton
    private lateinit var ce3: RadioButton
    private var applist =  arrayListOf<AppInfo>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        var view = inflater.inflate(R.layout.setting_widget_click, container,false)
        recycler = view.findViewById(R.id.app_info_list)

        groupCE = view.findViewById(R.id.groupCE)
        ce1 = view.findViewById(R.id.ce1)
        ce2 = view.findViewById(R.id.ce2)
        ce3 = view.findViewById(R.id.ce3)



        groupCE.setOnCheckedChangeListener { group, checkedId ->
            when(checkedId){
                R.id.ce1->{
                    adapter.setWC(0)
                    recycler.scrollToPosition(adapter.getClickIndex()[0])
                }
                R.id.ce2->{
                    adapter.setWC(1)
                    recycler.scrollToPosition(adapter.getClickIndex()[1])
                }
                R.id.ce3->{
                    adapter.setWC(2)
                    recycler.scrollToPosition(adapter.getClickIndex()[2])
                }
            }
            editor.apply()
            adapter.notifyDataSetChanged()
        }

        view.postDelayed({
            getAppList()
            adapter = AppInfoAdapter()
            recycler.adapter = adapter
            adapter.submitList(applist)
            adapter.notifyDataSetChanged()
        },300)


        view.setOnTouchListener(TranslateWithTouchUtil.onTouch(view,this))

        return view
    }


    private fun getAppList() {

        val pm = activity!!.packageManager
        // Return a List of all packages that are installed on the device.
        val packages = pm.getInstalledPackages(0)

        for (packageInfo in packages) {
            // 判断系统/非系统应用
            if (packageInfo.applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM == 0)
            // 非系统应用
            {
                var appInfo = AppInfo(
                    packageInfo.applicationInfo.loadLabel(pm).toString(),
                    packageInfo.packageName,
                    packageInfo.versionName,
                    packageInfo.applicationInfo.loadIcon(pm))
                applist.add(appInfo)
            } else {
                // 系统应用
            }

        }

    }

    override fun onStart() {
        super.onStart()

        val dw = dialog.window
        dw!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        dm = DisplayMetrics()
        activity!!.windowManager.defaultDisplay.getMetrics(dm)

        params = dw.attributes
        //屏幕底部
        params.gravity = Gravity.BOTTOM
        params.width = dm.widthPixels //屏幕宽度
        params.height = dm.heightPixels /3 * 2

        params.windowAnimations = R.style.BottomDialogAnimation
        dw.attributes = params
    }

    override fun onDismiss(dialog: DialogInterface?) {
        super.onDismiss(dialog)
        WidgetSettingFragment.getInstance()
            .show(activity!!.supportFragmentManager.beginTransaction(), "widget")
    }
}