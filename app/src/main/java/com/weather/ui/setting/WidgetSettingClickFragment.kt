package com.weather.ui.setting

import android.content.DialogInterface
import android.content.pm.ApplicationInfo
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.*
import android.widget.ProgressBar
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import androidx.core.content.edit
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.RecyclerView
import com.weather.MainActivity
import com.weather.MainActivity.Companion.sharedPreferences
import com.weather.R
import com.weather.adapters.AppInfoAdapter
import com.weather.data.model.AppInfo
import com.weather.util.ActivityUtil
import com.weather.util.DrawerUtil


class WidgetSettingClickFragment : DialogFragment() {

    companion object {
        @Volatile
        private var instance: WidgetSettingClickFragment? = null

        fun getInstance() = instance ?: synchronized(this) {
            instance
                ?: WidgetSettingClickFragment().also { instance = it }
        }

        var pn = arrayListOf(
            MainActivity.sharedPreferences.getString("appInfo1","com.weather"),
            MainActivity.sharedPreferences.getString("appInfo2","com.weather"),
            MainActivity.sharedPreferences.getString("appInfo3","com.weather")
        )
        var myAppIndex = 0
        var myAppIndexNoSys = 0
        var lastAdapter = 0
        var showSys = false
        var mSourceList = arrayListOf<AppInfo>()
        var applist =  arrayListOf<AppInfo>()
        var applistNoSys =  arrayListOf<AppInfo>()
    }

    private lateinit var dm: DisplayMetrics
    private lateinit var params: WindowManager.LayoutParams
    private lateinit var recycler: RecyclerView
    private lateinit var adapter: AppInfoAdapter
    private lateinit var groupCE: RadioGroup
    private lateinit var ce1: RadioButton
    private lateinit var ce2: RadioButton
    private lateinit var ce3: RadioButton
    private lateinit var toolbar: Toolbar
    private lateinit var loading: ProgressBar
    private var i = 0
    private var iNoSys = 0
    private var mark = arrayListOf(0,0,0)
    private var markNoSys = arrayListOf(0,0,0)
    private val TITLE_SHOW_SYS = "显示系统应用"
    private val TITLE_HID_SYS = "隐藏系统应用"
    private val TEXT_SELECTAPP = "选择要打开的应用:"
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        var view = inflater.inflate(R.layout.setting_widget_click, container,false) as View

        showSys = MainActivity.sharedPreferences.getBoolean("showSys", false)

        toolbar = view.findViewById(R.id.widgetToolbar)
        toolbar.title = TEXT_SELECTAPP
        ActivityUtil.instance.currentActivity!!.setSupportActionBar(toolbar)
        setHasOptionsMenu(true)

        recycler = view.findViewById(R.id.app_info_list)
        loading = view.findViewById(R.id.loadingApp)

        loading.visibility = View.VISIBLE

        groupCE = view.findViewById(R.id.groupCE)
        ce1 = view.findViewById(R.id.ce1)
        ce2 = view.findViewById(R.id.ce2)
        ce3 = view.findViewById(R.id.ce3)

        ce1.setOnClickListener {
            adapter.setWC(0)
            recycler.scrollToPosition(if(showSys) mark[0] else markNoSys[0])
            adapter.notifyDataSetChanged()
        }
        ce2.setOnClickListener {
            adapter.setWC(1)
            recycler.scrollToPosition(if(showSys) mark[1] else markNoSys[1])
            adapter.notifyDataSetChanged()
        }
        ce3.setOnClickListener {
            adapter.setWC(2)
            recycler.scrollToPosition(if(showSys) mark[2] else markNoSys[2])
            adapter.notifyDataSetChanged()
        }


        view.postDelayed({
            getAppList()
            getMark()

            loading.visibility = View.GONE
            adapter = AppInfoAdapter()
            recycler.adapter = adapter

            if(showSys){
                toolbar.title = TEXT_SELECTAPP + i
                adapter.submitList(applist)
            }else{
                toolbar.title = TEXT_SELECTAPP + iNoSys
                adapter.submitList(applistNoSys)
            }

            adapter.notifyDataSetChanged()

            when(adapter.wc){
                0 -> {
                    groupCE.check(R.id.ce1)
                }
                1 -> {
                    groupCE.check(R.id.ce2)
                }
                2 -> {
                    groupCE.check(R.id.ce3)
                }
            }

        },300)

        view.setOnTouchListener(DrawerUtil.onTouch(view,this))

        return view
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        activity!!.menuInflater.inflate(R.menu.widget_setting_menu, menu)

        val searchItem = menu!!.findItem(R.id.search)
        val showSysItem = menu.findItem(R.id.show_sys)
        val searchView = searchItem.actionView as SearchView

        showSysItem.title = if(showSys) TITLE_HID_SYS else TITLE_SHOW_SYS

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(query: String?): Boolean {
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                adapter.filter.filter(newText)
                return true
            }

        })
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(item!!.itemId == R.id.show_sys){
            if(!showSys) {
                item.title = TITLE_HID_SYS
                adapter.submitList(applist)
                toolbar.title = TEXT_SELECTAPP + i
                showSys = true
                mSourceList =
                    applist
            } else {
                item.title = TITLE_SHOW_SYS
                adapter.submitList(applistNoSys)
                toolbar.title = TEXT_SELECTAPP + iNoSys
                showSys = false
                mSourceList =
                    applistNoSys
            }
            sharedPreferences.edit{
                putBoolean("showSys",showSys)
            }
            getMark()
            adapter.notifyDataSetChanged()
        }
        return true
    }

    private fun getMark(){

        if(showSys){
            applist.forEachIndexed { index, appInfo ->
                if(pn[0] == appInfo.packageName){
                    mark[0] = index
                }
                if(pn[1] == appInfo.packageName){
                    mark[1] = index
                }
                if(pn[2] == appInfo.packageName){
                    mark[2] = index
                }
            }
            mSourceList =
                applist
        }else{
            applistNoSys.forEachIndexed{ index, appInfo ->
                if(pn[0] == appInfo.packageName){
                    markNoSys[0] = index
                }
                if(pn[1] == appInfo.packageName){
                    markNoSys[1] = index
                }
                if(pn[2] == appInfo.packageName){
                    markNoSys[2] = index
                }
            }
            mSourceList =
                applistNoSys
        }
    }

    private fun getAppList() {
        applistNoSys = arrayListOf()
        applist = arrayListOf()
        val pm = activity!!.packageManager
        // Return a List of all packages that are installed on the device.
        val packages = pm.getInstalledPackages(0)
        i = 0
        iNoSys = 0

        for (packageInfo in packages) {

            var appInfo = AppInfo(
                packageInfo.applicationInfo.loadLabel(pm).toString(),
                packageInfo.packageName,
                packageInfo.versionName,
                packageInfo.applicationInfo.loadIcon(pm)
            )

            if (packageInfo.applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM == 0) {
                // 非系统应用
                iNoSys++
                applistNoSys.add(appInfo)
            }
            if(packageInfo.packageName =="com.weather"){
                myAppIndex = i
                myAppIndexNoSys = iNoSys
            }
            i++
            applist.add(appInfo)
        }
    }

    override fun onStart() {
        super.onStart()

        val dw = dialog?.window
        dw!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        dm = DisplayMetrics()
        activity!!.windowManager.defaultDisplay.getMetrics(dm)

        params = dw.attributes
        //屏幕底部
        params.gravity = Gravity.BOTTOM
        params.width = ViewGroup.LayoutParams.MATCH_PARENT
        params.height = dm.heightPixels /3 * 2

        params.windowAnimations = R.style.BottomDialogAnimation
        dw.attributes = params
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        WidgetSettingFragment.getInstance()
            .show(fragmentManager!!, "widget")
    }
}