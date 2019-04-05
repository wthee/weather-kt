package com.weather.ui.main

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import androidx.fragment.app.Fragment
import android.widget.ProgressBar
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.weather.databinding.WeatherFragmentBinding
import com.weather.util.ActivityUtil
import com.weather.util.OCAnim
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import android.content.Context
import android.content.SharedPreferences
import android.view.inputmethod.InputMethodManager
import androidx.coordinatorlayout.widget.CoordinatorLayout
import kotlinx.android.synthetic.main.weather_fragment.*
import android.widget.Toast
import com.weather.*
import java.util.*
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.weather.MainActivity
import com.weather.util.InjectorUtil


class WeatherFragment : Fragment() {

    companion object {
        var nlIsGone = false
        var bjType = 0
    }

    private lateinit var viewModel: WeatherViewModel
    private lateinit var binding: WeatherFragmentBinding
    private lateinit var adapter1: WeatherAdapter1
    private lateinit var adapter2: WeatherAdapter2
    private lateinit var progressBar: ProgressBar
    private lateinit var swipe: SwipeRefreshLayout
    private lateinit var setting: TextView
    private lateinit var city1: RadioButton
    private lateinit var city2: RadioButton
    private lateinit var city3: RadioButton
    private lateinit var rb1: RadioButton
    private lateinit var rb2: RadioButton
    private lateinit var rb3: RadioButton
    private lateinit var rb4: RadioButton
    private lateinit var rb5: RadioButton
    private lateinit var rb6: RadioButton
    private lateinit var input: TextInputEditText
    private lateinit var modify: TextInputEditText
    private lateinit var modifyLayout: TextInputLayout
    private lateinit var settingView: View
    private lateinit var radioGroupCity: RadioGroup
    private lateinit var radioGroup1: RadioGroup
    private lateinit var radioGroup2: RadioGroup
    private lateinit var radioGroup3: RadioGroup
    private lateinit var mainLayout: CoordinatorLayout
    private lateinit var imm: InputMethodManager

    private lateinit var curDate: Date
    private lateinit var endDate: Date
    private var density: Float = 0f
    private var settingViewHight: Int = 0
    private var back = 0
    private var lastCity = "ip"
    private var saveC1 = "ip"
    private var saveC2 = "北京"
    private var saveC3 = "上海"
    private var settingViewisClose = true

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var editor: SharedPreferences.Editor


    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {
        sharedPreferences = MainActivity.sharedPreferences
        editor = MainActivity.editor

        nlIsGone = sharedPreferences.getBoolean("nl",
            nlIsGone
        )
        bjType = sharedPreferences.getInt("type",
            bjType
        )
        lastCity = sharedPreferences.getString("city", lastCity)
        saveC1 = sharedPreferences.getString("city1", saveC1)
        saveC2 = sharedPreferences.getString("city2", saveC2)
        saveC3 = sharedPreferences.getString("city3", saveC3)
        settingViewisClose = sharedPreferences.getBoolean("settingViewisClose", true)

        binding = WeatherFragmentBinding.inflate(inflater, container, false)
        val factory = InjectorUtil.getWeatherViewModelFactory(lastCity)
        viewModel = ViewModelProviders.of(this, factory).get(WeatherViewModel::class.java)
        imm = activity!!.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        curDate = Date(System.currentTimeMillis())
        endDate = Date(System.currentTimeMillis())
        density = ActivityUtil.instance.currentActivity!!.resources.displayMetrics.density
        initView()
        setOb()
        return binding.root
    }

    /**
     * 初始化viewTreeObserver事件监听,重写OnPreDrawListener获取组件高度
     */
    private fun initOnPreDrawListener() {
        var viewTreeObserver = activity!!.window.decorView.viewTreeObserver;
        viewTreeObserver.addOnPreDrawListener(object : ViewTreeObserver.OnPreDrawListener {
            override fun onPreDraw(): Boolean {
                settingViewHight = settingView.getMeasuredHeight();
                // 移除OnPreDrawListener事件监听
                activity!!.window.decorView.viewTreeObserver.removeOnPreDrawListener(this);
                if(settingViewisClose){
                    settingView.visibility = View.GONE
                }else{
                    settingView.visibility = View.VISIBLE
                }
                return true
            }
        })
    }

    override fun onResume() {
        super.onResume()
        view!!.isFocusableInTouchMode = true
        view!!.requestFocus()
        view!!.setOnKeyListener(object : View.OnKeyListener {
            override fun onKey(view: View, i: Int, keyEvent: KeyEvent): Boolean {
                if (keyEvent.action === KeyEvent.ACTION_DOWN && i == KeyEvent.KEYCODE_BACK) {
                    back++

                    if (back == 1) {
                        curDate = Date(System.currentTimeMillis())
                    }
                    if (back == 2) {
                        endDate = Date(System.currentTimeMillis())
                        back = 0
                    }
                    if (settingView.visibility == View.VISIBLE)
                        OCAnim.animateClose(settingView, settingViewHight)
                    else {
                        val diff = endDate.time - curDate.time
                        if (diff in 10..1000) {
                            return false
                        }
                        Toast.makeText(activity, "再按一次退出", Toast.LENGTH_SHORT).show()
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
                setting.text = weather.city
                input.text = null
                input.hint = "更新于 " + weather.update_time
                progressBar.visibility = View.GONE
                swipe.isRefreshing = false
                editor.putString("city", weather.city)
                editor.apply()
                if (bjType == 0) {
                    adapter1 = WeatherAdapter1()
                    binding.recycler.adapter = adapter1
                    adapter1.submitList(weather.data)
                    adapter1.notifyDataSetChanged()
                }
                if (bjType == 1) {
                    adapter2 = WeatherAdapter2()
                    binding.recycler.adapter = adapter2
                    adapter2.submitList(weather.data)
                    adapter2.notifyDataSetChanged()
                }
            }
        })
    }

    private fun resumeAllView(){
        city1.text = saveC1
        city2.text = saveC2
        city3.text = saveC3
        if (lastCity == city1.text) city1.isChecked = true
        if (lastCity == city2.text) city2.isChecked = true
        if (lastCity == city3.text) city3.isChecked = true
        if (bjType == 0) radioGroup1.check(R.id.rb1) else radioGroup1.check(R.id.rb2)
        if (nlIsGone) radioGroup2.check(R.id.rb3) else radioGroup2.check(R.id.rb4)
        if (MainActivity.onNight) radioGroup3.check(R.id.rb6) else radioGroup3.check(R.id.rb5)
    }

    private fun initView() {
        progressBar = binding.pb
        swipe = binding.swipe
        setting = binding.setting
        settingView = binding.settingView
        radioGroupCity = binding.groupCity
        city1 = binding.city1
        city2 = binding.city2
        city3 = binding.city3
        rb1 = binding.rb1
        rb2 = binding.rb2
        rb3 = binding.rb3
        rb4 = binding.rb4
        rb5 = binding.rb5
        rb6 = binding.rb6
        radioGroup1 = binding.groupBJ
        radioGroup2 = binding.groupNL
        radioGroup3 = binding.groupDN
        input = binding.input
        modify = binding.modify
        modifyLayout = binding.modifyLayout
        mainLayout = binding.mainLayout

        initOnPreDrawListener()

        resumeAllView()

        setting.setOnClickListener {
            if (settingView.visibility == View.GONE) {
                OCAnim.animateOpen(settingView, settingViewHight)
            } else {
                OCAnim.animateClose(settingView, settingViewHight)
            }
            hideAndClear()
        }

        swipe.setColorSchemeColors(activity!!.resources.getColor(R.color.colorAccent))
        swipe.setProgressBackgroundColorSchemeColor(activity!!.resources.getColor(R.color.line))

        swipe.setOnRefreshListener {
            viewModel.changeCity(setting.text.toString())
        }

        city1.setOnClickListener {
            viewModel.changeCity(city1.text.toString())
        }
        city2.setOnClickListener {
            viewModel.changeCity(city2.text.toString())
        }
        city3.setOnClickListener {
            viewModel.changeCity(city3.text.toString())
        }


        city1.setOnLongClickListener {
            modifyLayout.visibility = if (modifyLayout.visibility == View.GONE) {
                modify.text = null
                modify.requestFocus()
                imm.showSoftInput(modify, 0)
                View.VISIBLE
            } else {
                modify.clearFocus()
                imm.hideSoftInputFromWindow(modify.windowToken, 0)
                View.GONE
            }
            groupCity.visibility = if (groupCity.visibility == View.GONE) View.VISIBLE else View.GONE
            return@setOnLongClickListener false
        }
        city2.setOnLongClickListener {
            modifyLayout.visibility = if (modifyLayout.visibility == View.GONE) {
                modify.text = null
                modify.requestFocus()
                imm.showSoftInput(modify, 0)
                View.VISIBLE
            } else {
                modify.clearFocus()
                imm.hideSoftInputFromWindow(modify.windowToken, 0)
                View.GONE
            }
            groupCity.visibility = if (groupCity.visibility == View.GONE) View.VISIBLE else View.GONE
            return@setOnLongClickListener false
        }
        city3.setOnLongClickListener {
            modifyLayout.visibility = if (modifyLayout.visibility == View.GONE) {
                modify.text = null
                modify.requestFocus()
                imm.showSoftInput(modify, 0)
                View.VISIBLE
            } else {
                modify.clearFocus()
                imm.hideSoftInputFromWindow(modify.windowToken, 0)
                View.GONE
            }
            groupCity.visibility = if (groupCity.visibility == View.GONE) View.VISIBLE else View.GONE
            return@setOnLongClickListener false
        }

        radioGroup1.setOnCheckedChangeListener { group, checkedId ->
            bjType = if (checkedId == R.id.rb1) 0 else 1
            editor.putInt("type", bjType)
            editor.apply()
            viewModel.changeType()
        }

        radioGroup2.setOnCheckedChangeListener { group, checkedId ->
            nlIsGone = checkedId == R.id.rb3
            editor.putBoolean("nl", nlIsGone)
            editor.apply()
            adapter1.notifyDataSetChanged()
            adapter2.notifyDataSetChanged()
        }

        rb5.setOnClickListener {
            if (MainActivity.onNight) {
                MainActivity.onNight = false
                editor.putBoolean("onNight", MainActivity.onNight)
                editor.putBoolean("settingViewisClose", false)
                editor.apply()
                activity!!.recreate()
            }
        }

        rb6.setOnClickListener {
            if (!MainActivity.onNight) {
                MainActivity.onNight = true
                editor.putBoolean("onNight", MainActivity.onNight)
                editor.putBoolean("settingViewisClose", false)
                editor.apply()
                activity!!.recreate()
            }
        }


        modify.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (viewModel.checkCity(s.toString())) {
                    if (city1.isChecked) {
                        city1.text = s
                        editor.putString("city1",s.toString())
                        modifyLayout.visibility = View.GONE
                    }
                    if (city2.isChecked) {
                        city2.text = s
                        editor.putString("city2",s.toString())
                        modifyLayout.visibility = View.GONE
                    }
                    if (city3.isChecked) {
                        city3.text = s
                        editor.putString("city3",s.toString())
                        modifyLayout.visibility = View.GONE
                    }
                    groupCity.visibility = View.VISIBLE
                    editor.apply()
                    viewModel.changeCity(s.toString())
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                return
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                return
            }

        })

        input.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                viewModel.changeCity(s.toString())
                if (s.toString() != "") {
                    when (s.toString()) {
                        city1.text.toString() -> city1.isChecked = true
                        city2.text.toString() -> city2.isChecked = true
                        city3.text.toString() -> city3.isChecked = true
                        else -> {
                            city1.isChecked = false
                            city2.isChecked = false
                            city3.isChecked = false
                        }
                    }
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                return
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                return
            }

        })

        binding.recycler.setOnTouchListener(View.OnTouchListener { v, event ->
            hideAndClear()
            return@OnTouchListener false
        })
        mainLayout.setOnTouchListener(View.OnTouchListener { v, event ->
            hideAndClear()
            return@OnTouchListener true
        })
    }




    private fun hideAndClear() {
        input.clearFocus()
        modify.clearFocus()
        groupCity.visibility = View.VISIBLE
        modifyLayout.visibility = View.GONE
        imm.hideSoftInputFromWindow(input.windowToken, 0)
        imm.hideSoftInputFromWindow(modify.windowToken, 0)
    }
}
