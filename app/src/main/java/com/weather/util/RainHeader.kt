package com.weather.util

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationSet
import android.view.animation.ScaleAnimation
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.core.content.res.ResourcesCompat
import com.nineoldandroids.view.ViewHelper
import com.scwang.smartrefresh.header.internal.pathview.PathsView
import com.scwang.smartrefresh.layout.api.RefreshHeader
import com.scwang.smartrefresh.layout.api.RefreshKernel
import com.scwang.smartrefresh.layout.api.RefreshLayout
import com.scwang.smartrefresh.layout.constant.RefreshState
import com.scwang.smartrefresh.layout.constant.SpinnerStyle
import com.scwang.smartrefresh.layout.util.SmartUtil
import com.weather.R


class RainHeader @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr), RefreshHeader {


    private lateinit var mHeaderText: TextView//标题文本
    private lateinit var mArrowView: PathsView//下拉箭头
    private lateinit var mProgressView: ImageView//刷新动画视图
    private lateinit var mRefreshAnimation: AnimationSet //刷新动画
    private var mPercent = 0f

    init {
        initView(context)
    }

    private fun initView(context: Context) {
        gravity = Gravity.CENTER
        mHeaderText = TextView(context)
        mHeaderText.setTextColor(ResourcesCompat.getColor(resources, R.color.main_text, null))
        mArrowView = PathsView(context)
        mProgressView = ImageView(context)
        mProgressView.setImageDrawable(ResourcesCompat.getDrawable(resources, R.drawable.weather, null))
        mArrowView.parserPaths("M20,12l-1.41,-1.41L13,16.17V4h-2v12.17l-5.58,-5.59L4,12l8,8 8,-8z")
        mArrowView.background = ResourcesCompat.getDrawable(resources, R.drawable.down, null)

        //Header布局开始
        addView(View(context), SmartUtil.dp2px(10f), SmartUtil.dp2px(10f))
        addView(mProgressView, SmartUtil.dp2px(20f), SmartUtil.dp2px(20f))
        addView(mArrowView, SmartUtil.dp2px(20f), SmartUtil.dp2px(20f))
//        addView(View(context), SmartUtil.dp2px(10f), SmartUtil.dp2px(10f))
//        addView(
//            mHeaderText,
//            LayoutParams.WRAP_CONTENT,
//            LayoutParams.WRAP_CONTENT
//        )
        //Header布局结束

        minimumHeight = SmartUtil.dp2px(30f)
    }

    override fun getView(): View {
        return this//真实的视图就是自己，不能返回null
    }

    override fun getSpinnerStyle(): SpinnerStyle {
        return SpinnerStyle.Translate//指定为平移，不能null
    }

    override fun onStartAnimator(layout: RefreshLayout, headHeight: Int, maxDragHeight: Int) {
        mProgressView.startAnimation(mRefreshAnimation)//开始动画
    }

    override fun onFinish(layout: RefreshLayout, success: Boolean): Int {
        ViewHelper.setScaleX(mProgressView, 1f)
        ViewHelper.setScaleY(mProgressView, 1f)
        ViewHelper.setScaleX(mArrowView, 1f)
        ViewHelper.setScaleY(mArrowView, 1f)
//        if (success) {
//            mHeaderText.text = "数据已更新"
//        } else {
//            mHeaderText.text = "数据未更新"
//        }
        return resources.getInteger(R.integer.refresh_repeat)//延迟之后再弹回
    }

    override fun onStateChanged(
        refreshLayout: RefreshLayout,
        oldState: RefreshState,
        newState: RefreshState
    ) {
        when (newState) {
            RefreshState.None, RefreshState.PullDownToRefresh -> {
                //mHeaderText.text = "下拉开始刷新"
                ViewHelper.setAlpha(mArrowView, 1f)
                mProgressView.clearAnimation()//停止动画
                mArrowView.visibility = View.VISIBLE//显示下拉箭头
                mProgressView.visibility = View.GONE//隐藏动画
            }
            RefreshState.Refreshing -> {
                //mHeaderText.text = "正在获取数据"
                //刷新动画
                val mRefreshScale = ScaleAnimation(//缩放
                    mPercent, 1.5f,
                    mPercent, 1.5f,
                    Animation.RELATIVE_TO_SELF, 0.5f,
                    Animation.RELATIVE_TO_SELF, 0.5f
                )
                mRefreshScale.repeatCount = -1
                mRefreshScale.repeatMode = Animation.REVERSE
//                val mRefreshAlpha = AlphaAnimation(0.1f, 0.5f)//透明
//                mRefreshAlpha.repeatCount = -1
//                mRefreshAlpha.repeatMode = Animation.REVERSE
                //添加到动画集合
                mRefreshAnimation = AnimationSet(true)
                mRefreshAnimation.addAnimation(mRefreshScale)
//                mRefreshAnimation.addAnimation(mRefreshAlpha)
                mRefreshAnimation.duration = resources.getInteger(R.integer.refresh_repeat).toLong()
                mProgressView.startAnimation(mRefreshAnimation)
            }
            RefreshState.ReleaseToRefresh -> {
                //mHeaderText.text = "释放刷新"
                VibratorUtil.vibratorStart(
                    resources.getIntArray(R.array.vibrator_refresh_time),
                    resources.getIntArray(R.array.vibrator_refresh_amplitudes),
                    -1
                )
                mProgressView.visibility = View.VISIBLE//显示加载动画
                mArrowView.visibility = View.GONE//隐藏箭头
            }
        }
    }

    override fun isSupportHorizontalDrag(): Boolean {
        return false
    }

    override fun onInitialized(kernel: RefreshKernel, height: Int, maxDragHeight: Int) {}
    override fun onHorizontalDrag(percentX: Float, offsetX: Int, offsetMax: Int) {}
    override fun setPrimaryColors(@ColorInt vararg colors: Int) {}
    override fun onReleased(refreshLayout: RefreshLayout, height: Int, maxDragHeight: Int) {

    }

    override fun onMoving(
        isDragging: Boolean,
        percent: Float,
        offset: Int,
        height: Int,
        maxDragHeight: Int
    ) {
        //放大或缩小刷新图标
        mPercent = percent
        ViewHelper.setScaleX(mArrowView, percent)
        ViewHelper.setScaleY(mArrowView, percent)
        ViewHelper.setAlpha(mArrowView, percent)
    }
}