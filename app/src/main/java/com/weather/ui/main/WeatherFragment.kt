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
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import com.weather.MainActivity
import com.weather.MainActivity.Companion.sp
import com.weather.MyApplication
import com.weather.R
import com.weather.adapters.WeatherAdapter
import com.weather.data.model.SunMoonData
import com.weather.databinding.FragmentMainWeatherBinding
import com.weather.ui.info.WeatherInfoFragment
import com.weather.ui.main.WeatherViewModel.Companion.weatherTemp
import com.weather.ui.setting.MainSettingFragment
import com.weather.util.InjectorUtil
import com.weather.util.WeatherUtil
import com.weather.util.formatDate
import java.text.SimpleDateFormat
import java.util.*
import kotlin.system.exitProcess

class WeatherFragment : Fragment() {

    companion object {

        lateinit var imm: InputMethodManager
        var toUpdate = true
        lateinit var companionViewModel: WeatherViewModel
        var sunMoonDatas = arrayListOf<SunMoonData>()
    }

    private lateinit var binding: FragmentMainWeatherBinding
    private lateinit var adapter: WeatherAdapter
    
    private val viewModel by activityViewModels<WeatherViewModel> {
        InjectorUtil.getWeatherViewModelFactory()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMainWeatherBinding.inflate(inflater, container, false)
        //下拉刷新
        binding.refresh.setColorSchemeColors(
            ResourcesCompat.getColor(resources, R.color.theme, null)
        )
        //键盘
        imm = requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        initView()
        //设置observe
        setObserve()
        viewModel.changeCity(WeatherUtil.getCity())
        companionViewModel = viewModel
        return binding.root
    }

    private fun setObserve() {
        viewModel.weather.observe(viewLifecycleOwner, Observer { weather ->
            if (weather != null) {
                binding.apply {
                    noWea.visibility = if (weather.daily.size == 0) View.VISIBLE else View.GONE
                    setting.text = WeatherUtil.getCity()
                    input.hint = "更新于 " + weather.basic.updateTime.formatDate().substring(5, 16)
                    input.text = null
                    pb.visibility = View.GONE
                    //下次打开APP显示的city
                    sp.edit {
                        putString("city", setting.text.toString())
                    }

                    adapter = WeatherAdapter(MainActivity.spSetting.getInt("change_style", 0))
                    binding.recycler.adapter = adapter
                    adapter.submitList(weather.daily)

                }
                //通知桌面小部件更新
                val intent = Intent(AppWidgetManager.ACTION_APPWIDGET_UPDATE)
                MyApplication.context.sendBroadcast(intent)
            }
        })
        //今日实时天气
        viewModel.nowWeather.observe(viewLifecycleOwner, Observer { nowWeather ->
            if (nowWeather != null) {
                binding.apply {
                    nowTem.text = nowWeather.now.temp + "℃"
                    nowWea.text = nowWeather.now.text
                    setting.text = WeatherUtil.getCity()
                }
            }
        })
        //刷新判断
        viewModel.isRefresh.observe(viewLifecycleOwner, Observer { isRefresh ->
            if (!isRefresh) binding.refresh.isRefreshing = false
        })
        //布局变更
        viewModel.changeStyle.observe(viewLifecycleOwner, Observer { style ->
            adapter = WeatherAdapter(style)
            binding.recycler.adapter = adapter
            adapter.submitList(weatherTemp.daily)
        })
        //农历显示
        viewModel.changeNl.observe(viewLifecycleOwner, Observer {
            adapter = WeatherAdapter(MainActivity.spSetting.getInt("change_style", 0))
            binding.recycler.adapter = adapter
            adapter.submitList(weatherTemp.daily)
        })
    }

    @SuppressLint("ClickableViewAccessibility")
    @Suppress("DEPRECATION")
    private fun initView() {
        binding.apply {

            //左上城市名点击事件
            setting.setOnClickListener {
                MainSettingFragment.getInstance()
                    .show(parentFragmentManager, "setting")
            }

            //今日天气
            now.setOnClickListener {
                val dateFormat = SimpleDateFormat("yyyy-MM-dd")
                val date = dateFormat.format(Date(System.currentTimeMillis()))
                WeatherInfoFragment(date).show(
                    parentFragmentManager, "setting"
                )
            }

            //下拉刷新
            refresh.setOnRefreshListener {
                toUpdate = true
                viewModel.isRefresh.postValue(true)
                viewModel.changeCity(setting.text.toString())
            }

            //输入框搜索城市
            input.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable?) {
                    val input = s.toString()
                    if (input != "" && WeatherUtil.checkCity(input) != "0") {
                        WeatherUtil.setCity(input)
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
            recycler.setOnTouchListener(View.OnTouchListener { _, _ ->
                hideAndClear()
                return@OnTouchListener false
            })
            mainLayout.setOnTouchListener(View.OnTouchListener { _, _ ->
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
