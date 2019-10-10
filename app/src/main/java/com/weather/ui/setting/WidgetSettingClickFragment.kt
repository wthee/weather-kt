package com.weather.ui.setting


import android.annotation.SuppressLint
import android.content.DialogInterface
import android.content.pm.ApplicationInfo
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.util.DisplayMetrics
import android.view.*
import android.widget.ProgressBar
import android.widget.RadioGroup
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import androidx.core.content.edit
import androidx.core.view.forEachIndexed
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.RecyclerView
import com.weather.MainActivity.Companion.sharedPreferences
import com.weather.MyApplication
import com.weather.R
import com.weather.adapters.AppInfoAdapter
import com.weather.data.model.AppInfo
import com.weather.util.ActivityUtil
import com.weather.util.DrawerUtil
import skin.support.content.res.SkinCompatResources


class WidgetSettingClickFragment : DialogFragment() {

    companion object {
        @SuppressLint("StaticFieldLeak")
        @Volatile
        private var instance: WidgetSettingClickFragment? = null

        fun getInstance() = instance ?: synchronized(this) {
            instance
                ?: WidgetSettingClickFragment().also { instance = it }
        }

        var pn = arrayListOf(
            sharedPreferences.getString("appInfo1","com.weather"),
            sharedPreferences.getString("appInfo2","com.weather"),
            sharedPreferences.getString("appInfo3","com.weather")
        )
        var myAppIndex = 0
        var myAppIndexNoSys = 0
        var lastAdapter = 0
        var showSys = false
        var mSourceList = arrayListOf<AppInfo>()
        var applist =  arrayListOf<AppInfo>()
        var applistNoSys =  arrayListOf<AppInfo>()
    }

    private lateinit var recycler: RecyclerView
    private lateinit var adapter: AppInfoAdapter
    private lateinit var groupCE: RadioGroup
    private lateinit var toolbar: Toolbar
    private lateinit var loading: ProgressBar
    private var i = 0
    private var iNoSys = 0
    private var mark = arrayListOf(0,0,0)
    private var markNoSys = arrayListOf(0,0,0)
    private val TITLE_SHOW_SYS = "显示系统应用"
    private val TITLE_HID_SYS = "隐藏系统应用"
    private val TEXT_SELECTAPP = "选择要打开的应用: "
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.setting_widget_click, container,false) as View

        showSys = sharedPreferences.getBoolean("showSys", false)

        toolbar = view.findViewById(R.id.widgetToolbar)
        toolbar.title = TEXT_SELECTAPP
        toolbar.setTitleTextColor(SkinCompatResources.getColor(context,R.color.main_text))
        ActivityUtil.instance.currentActivity!!.setSupportActionBar(toolbar)
        setHasOptionsMenu(true)

        recycler = view.findViewById(R.id.app_info_list)
        loading = view.findViewById(R.id.loadingApp)
        groupCE = view.findViewById(R.id.groupCE)
        loading.visibility = View.VISIBLE

        groupCE.forEachIndexed { index, v ->
            v.setOnClickListener {
                adapter.setWC(index)
                recycler.scrollToPosition(if(showSys) mark[index] else markNoSys[index])
                adapter.notifyDataSetChanged()
            }
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

        DrawerUtil.bindAllViewOnTouchListener(view,this)

        return view
    }

    override fun onStart() {
        super.onStart()
        val dm = DisplayMetrics()
        activity!!.windowManager.defaultDisplay.getMetrics(dm)
        DrawerUtil.setBottomDrawer(dialog, activity,(dm.heightPixels * 0.618).toInt())
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        if(MyApplication().isForeground()){
            WidgetSettingFragment.getInstance()
                .show(fragmentManager!!, "widget")
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        activity!!.menuInflater.inflate(R.menu.widget_setting_menu, menu)

        val searchItem = menu.findItem(R.id.search)
        val showSysItem = menu.findItem(R.id.show_sys)
        val searchView = searchItem.actionView as SearchView

        showSysItem.title = if(showSys) {
            addColor(TITLE_HID_SYS,SkinCompatResources.getColor(context,R.color.main_text))
        } else {
            addColor(TITLE_SHOW_SYS,SkinCompatResources.getColor(context,R.color.main_text))
        }


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
        if(item.itemId == R.id.show_sys){
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

    /*
         * Add color to a given text
         */
    private fun addColor(text: CharSequence, color: Int): SpannableStringBuilder {
        val builder = SpannableStringBuilder(text)
        if (color != 0) {
            builder.setSpan(
                ForegroundColorSpan(color), 0, text.length,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }
        return builder
    }

    //当前选择的应用的下标
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
            mSourceList = applist
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
            mSourceList = applistNoSys
        }
    }

    //获取应用列表
    private fun getAppList() {
        applistNoSys = arrayListOf()
        applist = arrayListOf()
        val pm = activity!!.packageManager
        // Return a List of all packages that are installed on the device.
        val packages = pm.getInstalledPackages(0)
        i = 0
        iNoSys = 0

        for (packageInfo in packages) {

            val appInfo = AppInfo(
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
}