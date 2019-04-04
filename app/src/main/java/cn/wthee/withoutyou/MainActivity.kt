package cn.wthee.withoutyou

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import cn.wthee.withoutyou.databinding.MainActivityBinding
import cn.wthee.withoutyou.ui.main.WeatherFragment
import cn.wthee.withoutyou.util.ActivityUtil

class MainActivity : AppCompatActivity() {

    private lateinit var binding: MainActivityBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this,R.layout.main_activity)
        ActivityUtil.instance.currentActivity = this
    }

}
