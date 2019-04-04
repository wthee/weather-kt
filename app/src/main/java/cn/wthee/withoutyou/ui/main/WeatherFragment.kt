package cn.wthee.withoutyou.ui.main

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
import cn.wthee.highnoon.util.InjectorUtil
import cn.wthee.withoutyou.R
import cn.wthee.withoutyou.WeatherAdapter1
import cn.wthee.withoutyou.WeatherAdapter2
import cn.wthee.withoutyou.databinding.WeatherFragmentBinding
import cn.wthee.withoutyou.util.ActivityUtil
import cn.wthee.withoutyou.util.OCAnim
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import android.content.Context
import android.util.DisplayMetrics
import android.view.inputmethod.InputMethodManager
import androidx.coordinatorlayout.widget.CoordinatorLayout
import kotlinx.android.synthetic.main.weather_fragment.*
import android.widget.Toast
import java.util.*




class WeatherFragment : Fragment() {

    companion object {
        var nlIsGone = true
        var bjType = 0
        var back = 0
    }

    private lateinit var viewModel: WeatherViewModel
    private lateinit var binding: WeatherFragmentBinding
    private lateinit var adapter1: WeatherAdapter1
    private lateinit var adapter2: WeatherAdapter2
    private lateinit var progressBar: ProgressBar
    private lateinit var setting: TextView
    private lateinit var city1: RadioButton
    private lateinit var city2: RadioButton
    private lateinit var city3: RadioButton
    private lateinit var input: TextInputEditText
    private lateinit var modify: TextInputEditText
    private lateinit var modifyLayout: TextInputLayout
    private lateinit var settingView: View
    private lateinit var radioGroupCity: RadioGroup
    private lateinit var radioGroup1: RadioGroup
    private lateinit var radioGroup2: RadioGroup
    private lateinit var mainLayout: CoordinatorLayout
    private lateinit var imm :InputMethodManager
    private lateinit var curDate: Date
    private lateinit var endDate: Date
    private var density: Float = 0f
    private var settingViewHight: Int = 0


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = WeatherFragmentBinding.inflate(inflater,container,false)
        val factory = InjectorUtil.getWeatherViewModelFactory("ip")
        viewModel = ViewModelProviders.of(this,factory).get(WeatherViewModel::class.java)
        imm =  activity!!.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        curDate = Date(System.currentTimeMillis())
        endDate = Date(System.currentTimeMillis())
        density = ActivityUtil.instance.currentActivity!!.resources.displayMetrics.density
        settingViewHight = (density * 215 + 0.5).toInt()
        initView()
        setOb()
        return binding.root
    }

    override fun onResume() {
        super.onResume()

        view!!.isFocusableInTouchMode = true
        view!!.requestFocus()
        view!!.setOnKeyListener(object : View.OnKeyListener {
            override fun onKey(view: View, i: Int, keyEvent: KeyEvent): Boolean {
                if (keyEvent.action === KeyEvent.ACTION_DOWN && i == KeyEvent.KEYCODE_BACK) {
                    back++

                    if(back==1){
                        curDate = Date(System.currentTimeMillis())
                    }
                    if(back==2){
                        endDate = Date(System.currentTimeMillis())
                        back=0
                    }
                    if(settingView.visibility == View.VISIBLE)
                        OCAnim.animateClose(settingView,settingViewHight)
                    else{
                        val diff = endDate.time - curDate.time
                        if(diff in 10..1000){
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
        viewModel.weather.observe(viewLifecycleOwner, Observer { weather->
            if(weather!=null){
                setting.text = weather.city
                input.text = null
                input.hint = "更新于 "+weather.update_time
                progressBar.visibility = View.GONE
                if(bjType==0) {
                    adapter1 = WeatherAdapter1()
                    binding.recycler.adapter = adapter1
                    adapter1.submitList(weather.data)
                    adapter1.notifyDataSetChanged()
                }
                if(bjType==1){
                    adapter2 = WeatherAdapter2()
                    binding.recycler.adapter = adapter2
                    adapter2.submitList(weather.data)
                    adapter2.notifyDataSetChanged()
                }
            }
        })
    }

    private fun initView() {
        progressBar = binding.pb
        setting = binding.setting
        settingView = binding.settingView
        radioGroupCity = binding.groupCity
        city1 = binding.city1
        city2 = binding.city2
        city3 = binding.city3
        radioGroup1 = binding.groupBJ
        radioGroup2 = binding.groupNL
        input = binding.input
        modify = binding.modify
        modifyLayout = binding.modifyLayout
        mainLayout = binding.mainLayout


        setting.setOnClickListener {
            if (settingView.visibility == View.GONE){
                OCAnim.animateOpen(settingView,settingViewHight)
            } else{
                OCAnim.animateClose(settingView,settingViewHight)
            }
            hideAndClear()
        }

        radioGroupCity.setOnCheckedChangeListener { group, checkedId ->
            when(checkedId){
                R.id.city1-> viewModel.changeCity(city1.text.toString())
                R.id.city2-> viewModel.changeCity(city2.text.toString())
                R.id.city3-> viewModel.changeCity(city3.text.toString())
            }
        }

        city1.setOnLongClickListener {
            modifyLayout.visibility = if(modifyLayout.visibility==View.GONE)
            {
                modify.text = null
                modify.requestFocus()
                imm.showSoftInput(modify, 0)
                View.VISIBLE
            } else {
                modify.clearFocus()
                imm.hideSoftInputFromWindow(modify.windowToken,0)
                View.GONE
            }
            groupCity.visibility = if(groupCity.visibility==View.GONE) View.VISIBLE else View.GONE
            return@setOnLongClickListener false
        }
        city2.setOnLongClickListener {
            modifyLayout.visibility = if(modifyLayout.visibility==View.GONE)
            {
                modify.text = null
                modify.requestFocus()
                imm.showSoftInput(modify, 0)
                View.VISIBLE
            } else {
                modify.clearFocus()
                imm.hideSoftInputFromWindow(modify.windowToken,0)
                View.GONE
            }
            groupCity.visibility = if(groupCity.visibility==View.GONE) View.VISIBLE else View.GONE
            return@setOnLongClickListener false
        }
        city3.setOnLongClickListener {
            modifyLayout.visibility = if(modifyLayout.visibility==View.GONE)
            {
                modify.text = null
                modify.requestFocus()
                imm.showSoftInput(modify, 0)
                View.VISIBLE
            } else {
                modify.clearFocus()
                imm.hideSoftInputFromWindow(modify.windowToken,0)
                View.GONE
            }
            groupCity.visibility = if(groupCity.visibility==View.GONE) View.VISIBLE else View.GONE
            return@setOnLongClickListener false
        }

        radioGroup1.setOnCheckedChangeListener { group, checkedId ->
            bjType = if(checkedId==R.id.rb1) 0 else 1
            viewModel.changeType()
        }

        radioGroup2.setOnCheckedChangeListener { group, checkedId ->
            nlIsGone = checkedId==R.id.rb3
            adapter1.notifyDataSetChanged()
            adapter2.notifyDataSetChanged()
        }

        modify.addTextChangedListener(object : TextWatcher{
            override fun afterTextChanged(s: Editable?) {
                if(viewModel.checkCity(s.toString())){
                    if(city1.isChecked){
                        city1.text = s
                        modifyLayout.visibility = View.GONE
                    }
                    if(city2.isChecked){
                        city2.text = s
                        modifyLayout.visibility = View.GONE
                    }
                    if(city3.isChecked){
                        city3.text = s
                        modifyLayout.visibility = View.GONE
                    }
                    groupCity.visibility = View.VISIBLE
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

        input.addTextChangedListener(object : TextWatcher{
            override fun afterTextChanged(s: Editable?) {
                viewModel.changeCity(s.toString())
                if(s.toString()!=""){
                    city1.isChecked = false
                    city2.isChecked = false
                    city3.isChecked = false
                    when(s.toString()){
                        city1.text.toString() -> city1.isChecked = true
                        city2.text.toString() -> city2.isChecked = true
                        city3.text.toString() -> city3.isChecked = true
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
            return@OnTouchListener true
        })
        mainLayout.setOnTouchListener(View.OnTouchListener { v, event ->
            hideAndClear()
            return@OnTouchListener true
        })
    }


    private fun hideAndClear(){
        input.clearFocus()
        modify.clearFocus()
        groupCity.visibility = View.VISIBLE
        modifyLayout.visibility = View.GONE
        imm.hideSoftInputFromWindow(input.windowToken,0)
        imm.hideSoftInputFromWindow(modify.windowToken,0)
    }
}
