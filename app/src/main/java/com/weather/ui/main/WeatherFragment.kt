package com.weather.ui.main

import android.annotation.SuppressLint
import android.appwidget.AppWidgetManager
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.weather.databinding.WeatherFragmentBinding
import com.google.android.material.textfield.TextInputEditText
import android.content.Context
import android.content.Intent
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.edit
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.weather.*
import com.weather.MainActivity.Companion.isFirstOpen
import com.weather.MainActivity.Companion.sharedPreferences
import com.weather.ui.info.WeatherInfoFragment
import com.weather.ui.setting.MainSettingFragment
import com.weather.util.*
import com.weather.ui.main.WeatherViewModel.Companion.today

class WeatherFragment : Fragment() {

    companion object {
        var nlIsGone = false
        var bjType = 0
        var lastCity = "ip"
        lateinit var imm: InputMethodManager
        lateinit var adapter1: WeatherAdapter1
        lateinit var adapter2: WeatherAdapter2
        lateinit var viewModel: WeatherViewModel

        var saveC1 = "ip"
        var saveC2 = "北京"
        var saveC3 = "上海"

        var title = ""
    }

    private lateinit var binding: WeatherFragmentBinding
    private lateinit var progressBar: ProgressBar
    private lateinit var swipe: SwipeRefreshLayout
    private lateinit var recyclerView: RecyclerView
    private lateinit var setting: TextView
    private lateinit var input: TextInputEditText
    private lateinit var mainLayout: CoordinatorLayout

    //now
    private lateinit var nowWea: TextView
    private lateinit var nowTem: TextView

    private var firstTime: Long = 0
    private var density: Float = 0f

    private var settingViewisClose = true


    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {
        settingViewisClose = sharedPreferences.getBoolean("settingViewisClose", true)

        binding = WeatherFragmentBinding.inflate(inflater, container, false)
        val factory = InjectorUtil.getWeatherViewModelFactory()
        viewModel = ViewModelProviders.of(this, factory).get(
            WeatherViewModel::class.java)
        imm = activity!!.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        density = ActivityUtil.instance.currentActivity!!.resources.displayMetrics.density
        initView()
        setOb()
        return binding.root
    }

    override fun onResume() {
        super.onResume()

        nlIsGone = sharedPreferences.getBoolean("nl",
            nlIsGone
        )
        bjType = sharedPreferences.getInt("type",
            bjType
        )
        lastCity = sharedPreferences.getString("city",
            lastCity
        )!!

        viewModel.getWeather(lastCity)
        viewModel.getNowWeather(lastCity)

        view!!.isFocusableInTouchMode = true
        view!!.requestFocus()
        view!!.setOnKeyListener(object : View.OnKeyListener {
            override fun onKey(view: View, i: Int, keyEvent: KeyEvent): Boolean {
                val secondTime = System.currentTimeMillis();
                if (keyEvent.action == KeyEvent.ACTION_DOWN && i == KeyEvent.KEYCODE_BACK) {
                    if (secondTime - firstTime < 2000) {
                        System.exit(0)
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

    private fun setOb() {
        adapter1 = WeatherAdapter1()
        binding.recycler.adapter = adapter1
        adapter2 = WeatherAdapter2()
        binding.recycler.adapter = adapter2

        viewModel.weather.observe(viewLifecycleOwner, Observer { weather ->
            if (weather != null) {

                binding.apply {
                    this.weather = weather
                    hint = "更新于 " + weather.update_time
                }

                title = weather.city
                input.text = null
                progressBar.visibility = View.GONE
                swipe.isRefreshing = false
                sharedPreferences.edit {
                    putString("city", weather.city)
                }
                if (bjType == 0) {
                    binding.recycler.adapter = adapter1
                    adapter1.submitList(weather.data)
                    //adapter1.notifyDataSetChanged()
                }
                if (bjType == 1) {
                    binding.recycler.adapter = adapter2
                    adapter2.submitList(weather.data)
                    //adapter2.notifyDataSetChanged()
                }

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

                val intent = Intent(AppWidgetManager.ACTION_APPWIDGET_UPDATE)
                MyApplication.context.sendBroadcast(intent)
            }
        })

        viewModel.nowWeather.observe(viewLifecycleOwner, Observer { now ->
            if(now!=null){
                binding.apply {
                    this.now = now
                }
            }
        })

    }

    @SuppressLint("ClickableViewAccessibility")
    @Suppress("DEPRECATION")
    private fun initView() {
        progressBar = binding.pb
        swipe = binding.swipe
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
            activity!!.recreate()
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

        swipe.setColorSchemeColors(activity!!.resources.getColor(R.color.colorAccent))
        swipe.setProgressBackgroundColorSchemeColor(activity!!.resources.getColor(R.color.line))

        swipe.setOnRefreshListener {
            viewModel.changeCity(setting.text.toString())
        }


        input.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                viewModel.changeCity(s.toString())
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                return
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                return
            }

        })


        recyclerView.setOnTouchListener(View.OnTouchListener { _, _ ->
            hideAndClear()
            return@OnTouchListener false
        })
        mainLayout.setOnTouchListener(View.OnTouchListener { _, _ ->
            hideAndClear()
            return@OnTouchListener true
        })

    }


    private fun hideAndClear() {
        input.clearFocus()
        imm.hideSoftInputFromWindow(input.windowToken, 0)
    }
}
