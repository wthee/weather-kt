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
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.core.content.edit
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.weather.MainActivity
import com.weather.MainActivity.Companion.isFirstOpen
import com.weather.MainActivity.Companion.sp
import com.weather.MyApplication
import com.weather.adapters.WeatherAdapter
import com.weather.databinding.FragmentMainWeatherBinding
import com.weather.ui.info.WeatherInfoFragment
import com.weather.ui.main.WeatherViewModel.Companion.today
import com.weather.ui.setting.MainSettingFragment
import com.weather.util.*
import kotlin.system.exitProcess

class WeatherFragment : Fragment() {

    companion object {
        var lunarGone = false
        var styleType = 0

        lateinit var imm: InputMethodManager
        lateinit var adapter: WeatherAdapter
        lateinit var viewModel: WeatherViewModel

        var toUpdate = true

        var title = ""
    }

    private lateinit var binding: FragmentMainWeatherBinding

    //返回确认
    private var firstTime: Long = 0


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMainWeatherBinding.inflate(inflater, container, false)
        viewModel = InjectorUtil.getWeatherViewModelFactory().create(WeatherViewModel::class.java)
        //键盘
        imm = activity!!.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        initView()
        //设置observe
        setObserve()
        viewModel.changeCity("杭州")
        return binding.root
    }

    override fun onResume() {
        super.onResume()
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

        viewModel.weather.observe(viewLifecycleOwner, { weather ->
            if (weather != null) {
                binding.apply {
                    input.hint = "更新于 " + weather.basic.updateTime.formatDate().substring(5, 16)
                    input.text = null
                    setting.text = MainActivity.citys[MainActivity.cityIndex]
                    pb.visibility = View.GONE
                    //下次打开APP显示的city
                    sp.edit {
                        putString("city", setting.text.toString())
                    }

                    adapter = WeatherAdapter()
                    binding.recycler.adapter = adapter
                    adapter.submitList(weather.daily){
                        noWea.visibility = View.GONE
                    }

                }

                //通知桌面小部件更新
                val intent = Intent(AppWidgetManager.ACTION_APPWIDGET_UPDATE)
                MyApplication.context.sendBroadcast(intent)
            }
        })
        //今日实时天气
        viewModel.nowWeather.observe(viewLifecycleOwner, Observer { nowWeather ->
            if (nowWeather != null) {
                title = MainActivity.citys[MainActivity.cityIndex]
                binding.apply {
                    nowTem.text = nowWeather.now.temp + "℃"
                    nowWea.text = nowWeather.now.text
                }
            }
        })
        //刷新判断
        viewModel.isRefresh.observe(viewLifecycleOwner, Observer { isRefresh ->
            if (!isRefresh) {
                binding.refresh.finishRefresh()
            }
        })
    }

    @SuppressLint("ClickableViewAccessibility")
    @Suppress("DEPRECATION")
    private fun initView() {
        binding.apply {
            //左上城市名点击事件
            setting.setOnClickListener {
                MainSettingFragment.getInstance()
                    .show(activity!!.supportFragmentManager.beginTransaction(), "setting")
            }

            //左上城市名长按事件
            setting.setOnLongClickListener {
                MainActivity.onNight = !MainActivity.onNight
                sp.edit {
                    putBoolean("onNight", MainActivity.onNight)
                }
                NightModelUtil.initNightModel(MainActivity.onNight)
                return@setOnLongClickListener true
            }

            //今日天气
            now.setOnClickListener {
                WeatherInfoFragment(today).show(
                    activity!!
                        .supportFragmentManager
                        .beginTransaction(), "setting"
                )
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
                    if (input != "" && viewModel.checkCity(input) != "0") {
                        MainActivity.citys[MainActivity.cityIndex] = input
                        sp.edit {
                            putString(Constant.CITYS, MainActivity.citys.toJsonString())
                        }

                        toUpdate = true
                        viewModel.changeCity(input)
                    }
                }

                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                    return
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    return
                }

            })

            //点击关闭键盘
            recycler.setOnTouchListener(View.OnTouchListener { _, ev ->
                hideAndClear()
                return@OnTouchListener false
            })
            mainLayout.setOnTouchListener(View.OnTouchListener { _, ev ->
                hideAndClear()
                return@OnTouchListener true
            })
        }

    }

    //关闭键盘，取消输入框焦点
    private fun hideAndClear() {
        binding.input.clearFocus()
        imm.hideSoftInputFromWindow(binding.input.windowToken, 0)
    }

}
