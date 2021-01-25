package com.weather.ui

import android.os.Bundle
import android.widget.FrameLayout
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.weather.R

/**
 * 底部弹窗基类
 *
 */
open class CommonBottomSheetDialogFragment: BottomSheetDialogFragment() {

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        // 设置动画
        dialog?.window?.attributes?.windowAnimations = R.style.DialogAnimation
    }
}