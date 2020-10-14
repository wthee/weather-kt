package com.weather.ui.setting

import android.os.Bundle
import androidx.core.content.edit
import androidx.fragment.app.activityViewModels
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreference
import androidx.preference.SwitchPreferenceCompat
import com.weather.MainActivity
import com.weather.R
import com.weather.ui.main.WeatherFragment
import com.weather.ui.main.WeatherViewModel
import com.weather.util.InjectorUtil

class SettingsFragment : PreferenceFragmentCompat() {

    val viewModel by activityViewModels<WeatherViewModel> {
        InjectorUtil.getWeatherViewModelFactory()
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey)

        //改变布局
        val changeStyle = findPreference<Preference>("change_style")
        changeStyle?.setOnPreferenceClickListener {
            val style = if(MainActivity.spSetting.getInt("change_style", 0) ==0)
                1
            else
                0
            MainActivity.spSetting.edit {
                putInt("change_style", style)
            }
            viewModel.changeStyle.postValue(style)
            return@setOnPreferenceClickListener true
        }

        //农历显示
        val showNL = findPreference<SwitchPreferenceCompat>("show_nl")
        showNL?.setOnPreferenceChangeListener { _, newValue ->
            viewModel.changeNl.postValue(newValue as Boolean)
            return@setOnPreferenceChangeListener true
        }

    }

}