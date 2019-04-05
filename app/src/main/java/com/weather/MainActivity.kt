package com.weather

import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.preference.PreferenceManager
import androidx.appcompat.app.AppCompatDelegate
import androidx.databinding.DataBindingUtil
import com.weather.databinding.MainActivityBinding
import com.weather.util.ActivityUtil
import com.weather.R

class MainActivity : AppCompatActivity() {

    companion object {
        var onNight = true
        lateinit var sharedPreferences: SharedPreferences
        lateinit var editor: SharedPreferences.Editor
    }

    private lateinit var binding: MainActivityBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(MyApplication.context)
        editor = sharedPreferences.edit()
        onNight = sharedPreferences.getBoolean("onNight",false)
        if(onNight){
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        }else{
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }
        binding = DataBindingUtil.setContentView(this, R.layout.main_activity)
        ActivityUtil.instance.currentActivity = this
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(R.anim.activity_out,0)
    }
}
