package com.weather.ui.setting


import android.annotation.SuppressLint
import android.content.DialogInterface
import android.content.pm.ApplicationInfo
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.*
import android.widget.*
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import androidx.core.content.edit
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.forEachIndexed
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.weather.MainActivity.Companion.sp
import com.weather.MyApplication
import com.weather.R
import com.weather.adapters.AppInfoAdapter
import com.weather.data.model.AppInfo
import com.weather.util.ActivityUtil


class WidgetSettingClickFragment : BottomSheetDialogFragment() {

    companion object {
        @SuppressLint("StaticFieldLeak")
        @Volatile
        private var instance: WidgetSettingClickFragment? = null

        fun getInstance() = instance ?: synchronized(this) {
            instance
                ?: WidgetSettingClickFragment().also { instance = it }
        }

        var pn = arrayListOf(
            sp.getString("appInfo1", "com.weather"),
            sp.getString("appInfo2", "com.weather"),
            sp.getString("appInfo3", "com.weather")
        )
        var myAppIndex = 0
        var myAppIndexNoSys = 0
        var lastAdapter = 0
        var showSys = false
        var mSourceList = arrayListOf<AppInfo>()
        var applist = arrayListOf<AppInfo>()
        var applistNoSys = arrayListOf<AppInfo>()
    }

    private lateinit var recycler: RecyclerView
    private lateinit var adapter: AppInfoAdapter
    private lateinit var groupCE: RadioGroup
    private lateinit var groupSysApp: RadioGroup
    private lateinit var toolbar: Toolbar
    private lateinit var loading: ProgressBar
    private lateinit var sysApp: TextView
    private var i = 0
    private var iNoSys = 0
    private var mark = arrayListOf(0, 0, 0)
    private var markNoSys = arrayListOf(0, 0, 0)
    private val TITLE_SHOW_SYS = "显示系统应用"
    private val TITLE_HID_SYS = "隐藏系统应用"
    private val TEXT_SELECTAPP = "选择要打开的应用: "
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_setting_widget_clickevent, container, false) as View

        showSys = sp.getBoolean("showSys", false)

        toolbar = view.findViewById(R.id.widgetToolbar)
        toolbar.title = TEXT_SELECTAPP
        toolbar.setTitleTextColor(ResourcesCompat.getColor(resources, R.color.main_text, null))
        ActivityUtil.instance.currentActivity!!.setSupportActionBar(toolbar)
        setHasOptionsMenu(true)

        recycler = view.findViewById(R.id.app_info_list)
        loading = view.findViewById(R.id.loadingApp)
        groupCE = view.findViewById(R.id.groupCE)
        groupSysApp = view.findViewById(R.id.groupSysApp)
        loading.visibility = View.VISIBLE

        //执行完弹出动画，再加载
        view.postDelayed({
            initView()
            bindListener()
        }, resources.getInteger(R.integer.slide).toLong())

        return view
    }

    override fun onStart() {
        super.onStart()
        val dm = DisplayMetrics()
        activity!!.windowManager.defaultDisplay.getMetrics(dm)
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        if (MyApplication().isForeground()) {
            WidgetSettingFragment.getInstance()
                .show(parentFragmentManager, "widget")
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        activity!!.menuInflater.inflate(R.menu.widget_setting_menu, menu)

        val searchItem = menu.findItem(R.id.search)
        val searchView = searchItem.actionView as SearchView
        val searchIcon =
            searchView.findViewById<ImageView>(androidx.appcompat.R.id.search_button)
        val searchClose =
            searchView.findViewById<ImageView>(androidx.appcompat.R.id.search_close_btn)
        val searchBack =
            searchView.findViewById<LinearLayout>(androidx.appcompat.R.id.search_plate)
        val searchInput =
            searchView.findViewById<com.google.android.material.textview.MaterialTextView>(androidx.appcompat.R.id.search_src_text)

        menu.javaClass.getDeclaredMethod("setOptionalIconsVisible", java.lang.Boolean.TYPE)
            .apply {
                isAccessible = true
                invoke(menu, true)
            }

        searchIcon.setColorFilter(ResourcesCompat.getColor(resources, R.color.main_text, null))
        searchClose.setColorFilter(ResourcesCompat.getColor(resources, R.color.main_text, null))
        searchBack.setBackgroundColor(ResourcesCompat.getColor(resources, R.color.background, null))
        searchInput.setTextColor(ResourcesCompat.getColor(resources, R.color.main_text, null))
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                adapter.filter.filter(newText)
                return true
            }
        })
    }

    private fun initView() {
        getAppList()
        getMark()

        loading.visibility = View.GONE
        adapter = AppInfoAdapter()
        recycler.adapter = adapter

        if (showSys) {
            toolbar.title = TEXT_SELECTAPP + i
            groupSysApp.check(R.id.sysApp_show)
            adapter.submitList(applist)
        } else {
            toolbar.title = TEXT_SELECTAPP + iNoSys
            groupSysApp.check(R.id.sysApp_hide)
            adapter.submitList(applistNoSys)
        }

        when (adapter.wc) {
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
    }

    private fun bindListener() {
        groupCE.forEachIndexed { index, v ->
            v.setOnClickListener {
                adapter.setWC(index)
                recycler.scrollToPosition(if (showSys) mark[index] else markNoSys[index])
                adapter.notifyDataSetChanged()
            }
        }
        groupSysApp.setOnCheckedChangeListener { _, checkId ->
            showSys = checkId == R.id.sysApp_show
            if (showSys) {
                adapter.submitList(applist)
                toolbar.title = TEXT_SELECTAPP + i
                mSourceList = applist
            } else {
                adapter.submitList(applistNoSys)
                toolbar.title = TEXT_SELECTAPP + iNoSys
                mSourceList = applistNoSys
            }
            sp.edit {
                putBoolean("showSys", showSys)
            }
            getMark()
            adapter.notifyDataSetChanged()
        }
    }

    //当前选择的应用的下标
    private fun getMark() {
        if (showSys) {
            applist.forEachIndexed { index, appInfo ->
                if (pn[0] == appInfo.packageName) {
                    mark[0] = index
                }
                if (pn[1] == appInfo.packageName) {
                    mark[1] = index
                }
                if (pn[2] == appInfo.packageName) {
                    mark[2] = index
                }
            }
            mSourceList = applist
        } else {
            applistNoSys.forEachIndexed { index, appInfo ->
                if (pn[0] == appInfo.packageName) {
                    markNoSys[0] = index
                }
                if (pn[1] == appInfo.packageName) {
                    markNoSys[1] = index
                }
                if (pn[2] == appInfo.packageName) {
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
            if (packageInfo.packageName == "com.weather") {
                myAppIndex = i
                myAppIndexNoSys = iNoSys
            }
            i++
            applist.add(appInfo)
        }
    }
}