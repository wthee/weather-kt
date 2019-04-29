package com.weather.setting

import android.content.DialogInterface
import android.content.pm.ApplicationInfo
import android.graphics.Color
import android.os.Bundle
import android.util.DisplayMetrics
import android.graphics.drawable.ColorDrawable
import android.view.*
import android.widget.CheckBox
import androidx.fragment.app.DialogFragment
import com.weather.R
import com.weather.util.TranslateWithTouchUtil
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.RecyclerView
import com.weather.AppInfoAdapter
import com.weather.MainActivity
import com.weather.MainActivity.Companion.editor
import com.weather.MainActivity.Companion.sharedPreferences
import com.weather.data.AppInfo
import com.weather.util.ActivityUtil


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
    }

    private lateinit var dm: DisplayMetrics
    private lateinit var params: WindowManager.LayoutParams
    private lateinit var recycler: RecyclerView
    private lateinit var adapter: AppInfoAdapter
    private lateinit var groupCE: RadioGroup
    private lateinit var ce1: RadioButton
    private lateinit var ce2: RadioButton
    private lateinit var ce3: RadioButton
    private lateinit var sv: SearchView
    private lateinit var toolbar: Toolbar
    private var applist =  arrayListOf<AppInfo>()
    private var applistNoSys =  arrayListOf<AppInfo>()
    private var i = 0
    private var iNoSys = 0
    private var showSys = false
    private var mark = arrayListOf(0,0,0)
    private var markNoSys = arrayListOf(0,0,0)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        var view = inflater.inflate(R.layout.setting_widget_click, container,false) as View
        toolbar = view.findViewById(R.id.widgetToolbar)
        toolbar.title = "选择要打开的应用"
        ActivityUtil.instance.currentActivity!!.setSupportActionBar(toolbar)
        setHasOptionsMenu(true)

        recycler = view.findViewById(R.id.app_info_list)

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
            adapter = AppInfoAdapter()
            recycler.adapter = adapter
            adapter.submitList(applistNoSys)
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

        view.setOnTouchListener(TranslateWithTouchUtil.onTouch(view,this))

        return view
    }


    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        activity!!.menuInflater.inflate(R.menu.widget_setting_menu, menu)

        val searchItem = menu!!.findItem(R.id.search)
        val searchView = searchItem.actionView as SearchView

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(query: String?): Boolean {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

        })

    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        if(item!!.itemId == R.id.show_sys){
            if (!item.isChecked){
                adapter.submitList(applist)
                toolbar.title = "选择要打开的应用($i)"
                showSys = true
                item.isChecked = true
            }else{
                adapter.submitList(applistNoSys)
                toolbar.title = "选择要打开的应用($iNoSys)"
                showSys = false
                item.isChecked = false
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
                packageInfo.applicationInfo.loadIcon(pm))

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
        toolbar.title = "选择要打开的应用($iNoSys)"
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