package com.weather.adapters

import android.view.View
import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.weather.util.ZhColorUtil


@BindingAdapter("autoColor")
fun autoColor(view: View, type: Any){
    if(type!="\t"&&type!=""){
        view as TextView
        view.setTextColor(ZhColorUtil.formColor(type.toString()))
    }
}

@BindingAdapter("isGone")
fun isGone(view: View, isGone: Boolean){
    if(isGone){
        view.visibility = View.GONE
    }else{
        view.visibility = View.VISIBLE
    }
}