package com.weather.ui.main

import android.annotation.SuppressLint
import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.view.animation.LayoutAnimationController
import android.view.inputmethod.InputMethodManager
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.edit
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textfield.TextInputEditText
import com.scwang.smartrefresh.layout.SmartRefreshLayout
import com.weather.GuideView
import com.weather.MainActivity
import com.weather.MainActivity.Companion.isFirstOpen
import com.weather.MainActivity.Companion.sharedPreferences
import com.weather.MyApplication
import com.weather.R
import com.weather.adapters.WeatherAdapter
import com.weather.databinding.WeatherFragmentBinding
import com.weather.ui.info.WeatherInfoFragment
import com.weather.ui.main.WeatherViewModel.Companion.today
import com.weather.ui.setting.MainSettingFragment
import com.weather.util.InjectorUtil
import com.weather.util.NightModelUtil
import kotlin.system.exitProcess

class WeatherFragment : Fragment() {

    companion object {
        var lunarGone = false
        var styleType = 0


        var weatherFragment = WeatherFragment()
        lateinit var imm: InputMethodManager
        lateinit var adapter: WeatherAdapter
        lateinit var viewModel: WeatherViewModel

        var cityIndex: Int = 1
        var toUpdate = true
        var saveC1 = sharedPreferences.getString("city1", "ip")!!
        var saveC2 = sharedPreferences.getString("city2", "北京")!!
        var saveC3 = sharedPreferences.getString("city3", "上海")!!

        var title = ""
    }

    private lateinit var binding: WeatherFragmentBinding
    private lateinit var progressBar: ProgressBar
    private lateinit var refresh: SmartRefreshLayout
    private lateinit var recyclerView: RecyclerView
    private lateinit var setting: TextView
    private lateinit var input: TextInputEditText
    private lateinit var mainLayout: ConstraintLayout
    //今日实时天气、温度
    private lateinit var nowWea: TextView
    private lateinit var nowTem: TextView
    //返回确认
    private var firstTime: Long = 0


    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {

        binding = WeatherFragmentBinding.inflate(inflater, container, false)
        val factory = InjectorUtil.getWeatherViewModelFactory()
        viewModel = ViewModelProviders.of(this, factory).get(
            WeatherViewModel::class.java)
        imm = activity!!.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        initView()
        //为livedata设置observe
        setObserve()
        return binding.root
    }

    override fun onResume() {
        super.onResume()

        saveC1 = sharedPreferences.getString("city1", saveC1)!!
        saveC2 = sharedPreferences.getString("city2", saveC2)!!
        saveC3 = sharedPreferences.getString("city3", saveC3)!!

        lunarGone = sharedPreferences.getBoolean("nl",
            lunarGone
        )
        styleType = sharedPreferences.getInt("type",
            styleType
        )
        cityIndex = sharedPreferences.getInt("cityIndex",
            cityIndex
        )!!

        toUpdate = true
        swipToChangeCity(cityIndex)

        //返回两次退出应用
        view!!.isFocusableInTouchMode = true
        view!!.requestFocus()
        view!!.setOnKeyListener(object : View.OnKeyListener {
            override fun onKey(view: View, i: Int, keyEvent: KeyEvent): Boolean {
                val secondTime = System.currentTimeMillis()
                if (keyEvent.action == KeyEvent.ACTION_DOWN && i == KeyEvent.KEYCODE_BACK) {
                    if (secondTime - firstTime < 2000) {
                        exitProcess(0)
                    } else {
                        Toast.makeText(activity, "再按一次退出", Toast.LENGTH_SHORT).show()
                        firstTime = System.currentTimeMillis()
                    }
                    return true
                }
                return false
            }
        })
    }

    private fun setObserve() {

        val controller = LayoutAnimationController(AnimationUtils.loadAnimation(binding.root.context,R.anim.item_load))
        controller.order = LayoutAnimationController.ORDER_NORMAL
        controller.delay = 0.2f

        viewModel.weather.observe(viewLifecycleOwner, Observer { weather ->
            if (weather != null) {
                binding.apply {
                    this.weather = weather
                    hint = "更新于 " + weather.update_time
                }

                input.text = null
                setting.text = weather.city
                progressBar.visibility = View.GONE
                //下次打开APP显示的city
                sharedPreferences.edit {
                    putString("city", weather.city)
                }

                binding.recycler.layoutAnimation = controller
                adapter = WeatherAdapter()
                binding.recycler.adapter = adapter
                adapter.submitList(weather.data)

                //操作引导
                if(isFirstOpen){
                    GuideView(setting, 2)
                        .show(activity!!.supportFragmentManager.beginTransaction(),"test")
                    GuideView(setting, 1)
                        .show(activity!!.supportFragmentManager.beginTransaction(),"test")
                    sharedPreferences.edit {
                        putBoolean("isFirstOpen",false)
                    }
                    isFirstOpen = sharedPreferences.getBoolean("isFirstOpen",false)
                }
                //通知桌面小部件更新
                val intent = Intent(AppWidgetManager.ACTION_APPWIDGET_UPDATE)
                MyApplication.context.sendBroadcast(intent)
            }
        })
        //今日实时天气
        viewModel.nowWeather.observe(viewLifecycleOwner, Observer { now ->
            if(now!=null){
                title = now.city
                binding.apply {
                    this.now = now
                }
            }
        })
        //刷新判断
        viewModel.isRefresh.observe(viewLifecycleOwner, Observer {isRefresh ->
            if (!isRefresh){
                refresh.finishRefresh()
            }
        })
    }

    @SuppressLint("ClickableViewAccessibility")
    @Suppress("DEPRECATION")
    private fun initView() {
        progressBar = binding.pb
        refresh = binding.refresh
        recyclerView = binding.recycler
        setting = binding.setting
        nowWea = binding.nowWea
        nowTem = binding.nowTem
        input = binding.input
        mainLayout = binding.mainLayout

        //左上城市名点击事件
        setting.setOnClickListener {
            MainSettingFragment.getInstance().show(activity!!.supportFragmentManager.beginTransaction(),"setting")
        }

        //左上城市名长按事件
        setting.setOnLongClickListener {
            MainActivity.onNight = !MainActivity.onNight
            sharedPreferences.edit {
                putBoolean("onNight", MainActivity.onNight)
            }
            NightModelUtil.initNightModel(MainActivity.onNight)
            return@setOnLongClickListener true
        }

        nowWea.setOnClickListener {
            WeatherInfoFragment(today).show(activity!!
                    .supportFragmentManager
                    .beginTransaction(),"setting")
        }

        nowTem.setOnClickListener {
            nowWea.callOnClick()
        }

        //下拉刷新
        refresh.setHeaderMaxDragRate(1.5f)
        refresh.setOnRefreshListener {
            viewModel.changeCity(setting.text.toString())
        }

        //输入框搜索城市
        input.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val input = s.toString()
                if( input != ""&& viewModel.checkCity(input) != -1){
                    sharedPreferences.edit {
                        putString("city$cityIndex", input)
                    }
                    toUpdate = true
                    viewModel.changeCity(input)
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                return
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                return
            }

        })

        //点击关闭键盘
        recyclerView.setOnTouchListener(View.OnTouchListener { _, ev ->
            hideAndClear()
            return@OnTouchListener false
        })
        mainLayout.setOnTouchListener(View.OnTouchListener { _, ev->
            hideAndClear()
            return@OnTouchListener true
        })
    }

    //关闭键盘，取消输入框焦点
    private fun hideAndClear() {
        input.clearFocus()
        imm.hideSoftInputFromWindow(input.windowToken, 0)
    }

    //切换城市
    fun swipToChangeCity(cityIndex: Int){
        val city = when (cityIndex) {
            1 -> sharedPreferences.getString("city1", saveC1)!!
            2 -> sharedPreferences.getString("city2", saveC2)!!
            3 -> sharedPreferences.getString("city3", saveC3)!!
            else -> "ip"
        }
        sharedPreferences.edit{
            putInt("cityIndex", cityIndex)
        }
        toUpdate = true
        viewModel.changeCity(city)
    }
}
