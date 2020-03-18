package com.weather.util

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.drawable.BitmapDrawable
import android.os.Handler
import android.util.AttributeSet
import android.widget.SeekBar
import androidx.appcompat.widget.AppCompatSeekBar
import com.weather.R
import skin.support.content.res.SkinCompatResources


class ColorSeekBar : AppCompatSeekBar {

    companion object{
        val DEFAULT_COLORS = arrayListOf(0x000000, 0xFF0000, 0xFF00FF,
            0x0000FF, 0x00FFFF, 0x00FF00, 0xFFFF00, 0xFFFFFF)
    }

    //背景画笔
    private lateinit var mBackgroundPaint: Paint

    //进度画笔
    private lateinit var mProgressPaint: Paint

    //第二进度画笔
    private lateinit var mSecondProgressPaint: Paint

    //游标画笔
    private lateinit var mThumbPaint: Paint

    //默认
    private val TRACKTOUCH_NONE = -1
    //开始拖动
    private val TRACKTOUCH_START = 0
    private var mTrackTouch = TRACKTOUCH_NONE

    private var mOnChangeListener: OnChangeListener? = null
    private var mOnDrawListener: OnDrawListener? = null


    private var isTrackingTouch = false
    private var mTrackingTouchSleepTime = 0L
    private val mHandler = Handler()
    private val mRunnable = Runnable { setTrackTouch(TRACKTOUCH_NONE) }

    //背景渐变颜色数组
    private var backgroundColors = gradientColor(Color.GRAY,Color.GRAY,max)


    constructor(context: Context) : super(context) {
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(context)
    }

    //初始化
    private fun init(context: Context) {

        setBackgroundColor(Color.TRANSPARENT)

        //背景画笔
        mBackgroundPaint = Paint()
        mBackgroundPaint.isDither = true
        mBackgroundPaint.isAntiAlias = true
        mBackgroundPaint.color = SkinCompatResources.getColor(context, R.color.hr)

        mProgressPaint = Paint()
        mProgressPaint.isDither = true
        mProgressPaint.isAntiAlias = true
        mProgressPaint.color = SkinCompatResources.getColor(context, R.color.hr)

        mSecondProgressPaint = Paint()
        mSecondProgressPaint.isDither = true
        mSecondProgressPaint.isAntiAlias = true
        mSecondProgressPaint.color = SkinCompatResources.getColor(context, R.color.alpha)

        mThumbPaint = Paint()
        mThumbPaint.isDither = true
        mThumbPaint.isAntiAlias = true
        mThumbPaint.color = SkinCompatResources.getColor(context, R.color.theme)


        thumb = BitmapDrawable()
        setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, i: Int, b: Boolean) {
                if (mTrackTouch == TRACKTOUCH_START) {
                    if (mOnChangeListener != null) {
                        mOnChangeListener!!.onProgressChanged(this@ColorSeekBar)
                    }
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
                isTrackingTouch = true
                mHandler.removeCallbacks(mRunnable)
                if (mTrackTouch == TRACKTOUCH_NONE) {
                    setTrackTouch(TRACKTOUCH_START)
                    if (mOnChangeListener != null) {
                        mOnChangeListener!!.onTrackingTouchStart(this@ColorSeekBar)
                    }
                }
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                isTrackingTouch = false
                if (mTrackTouch == TRACKTOUCH_START) {
                    if (mOnChangeListener != null) {
                        mOnChangeListener!!.onTrackingTouchFinish(this@ColorSeekBar)
                    }
                    mHandler.postDelayed(mRunnable, mTrackingTouchSleepTime)
                }
            }
        })
        
    }

    override fun onDraw(canvas: Canvas) {
        //绘制开始回调
        if (mOnDrawListener != null) {
            mOnDrawListener!!.onDrawStart(this@ColorSeekBar)
        }

        var rSize = height / 4f
        if (isTrackingTouch) {
            rSize = height / 3f
        }
        val height = height / 4 / 3
        var leftPadding = rSize

        if (progress > 0) {
            leftPadding = 0f
        }

        val stepWidth = width.toFloat() / max
        var leftX=leftPadding
        //背景渐变
        mBackgroundPaint!!.color = backgroundColors[0]
        var backgroundRect: RectF
        for(i in 0 until max){
            backgroundRect = RectF(
                leftX, (getHeight() / 2 - height).toFloat(), width.toFloat(),
                (getHeight() / 2 + height).toFloat()
            )
            canvas.drawRoundRect(backgroundRect, rSize, rSize, mBackgroundPaint)
            leftX += stepWidth
            mBackgroundPaint!!.color = backgroundColors[i]
        }


        if (max != 0) {
            val secondRight = (secondaryProgress.toFloat() / max * width).toInt()
            val secondProgressRect = RectF(
                leftPadding.toFloat(), (getHeight() / 2 - height).toFloat(),
                secondRight.toFloat(), (getHeight() / 2 + height).toFloat()
            )
            canvas.drawRoundRect(secondProgressRect, rSize, rSize, mSecondProgressPaint)


            mThumbPaint.color = backgroundColors[progress]


            var cx = progress.toFloat() / max * width
            if (cx + rSize > width) {
                cx = width - rSize
            } else {
                cx = Math.max(cx, rSize)
            }
            val cy = getHeight() / 2f
            canvas.drawCircle(cx, cy, rSize, mThumbPaint)
        }

        //绘制完成回调
        if (mOnDrawListener != null) {
            mOnDrawListener!!.onDrawFinish(this@ColorSeekBar)
        }
    }

    @Synchronized
    override fun setProgress(progress: Int) {
        if (mTrackTouch == TRACKTOUCH_NONE && max != 0) {
            super.setProgress(progress)
        }
        postInvalidate()
    }

    @Synchronized
    override fun setSecondaryProgress(secondaryProgress: Int) {
        super.setSecondaryProgress(secondaryProgress)
        postInvalidate()
    }

    @Synchronized
    override fun setMax(max: Int) {
        super.setMax(max)
        postInvalidate()
    }

    @Synchronized
    private fun setTrackTouch(trackTouch: Int) {
        this.mTrackTouch = trackTouch
    }

    /**
     * 设置背景颜色
     *
     * @param backgroundColor
     */
    fun setBackgroundPaintColor(backgroundColor: Int) {
        mBackgroundPaint!!.setColor(backgroundColor)
        postInvalidate()
    }

    //设置游标颜色
    fun setThumbColor(thumbColor: Int) {
        mThumbPaint.color = thumbColor
        postInvalidate()
    }

    //获取游标颜色
    fun getThumbColor(): Int {
        return mThumbPaint.color
    }

    fun setOnChangeListener(onChangeListener: OnChangeListener) {
        this.mOnChangeListener = onChangeListener
    }

    fun setOnDrawListener(onDrawListener: OnDrawListener) {
        this.mOnDrawListener = onDrawListener
    }


    fun setBackgroundGradientColors(colorArray: ArrayList<Int>){
        backgroundColors = arrayListOf()
        val colorSize = colorArray.size - 1
        colorArray.forEachIndexed { index, i ->
            if(index != colorSize){
                backgroundColors.addAll(gradientColor(colorArray[index],colorArray[index+1], max / colorSize))
            }
        }
        for( i in backgroundColors.size .. max){
            backgroundColors.add(backgroundColors.last())
        }
    }

    /**
     * 传入起始颜色 [startColor] 、终止颜色 [endColor]、渐变步长 [step]
     * 获取渐变数组
     * */
    private fun gradientColor(startColor: Int, endColor: Int, step: Int): ArrayList<Int> {
        //起始颜色
        val startR = startColor and 0xff0000 shr 16
        val startG = startColor and 0x00ff00 shr 8
        val startB = startColor and 0x0000ff
        //终止颜色
        val endR = endColor and 0xff0000 shr 16
        val endG = endColor and 0x00ff00 shr 8
        val endB = endColor and 0x0000ff
        //总差值
        val sR = (endR - startR).toDouble() / step
        val sG = (endG - startG).toDouble() / step
        val sB = (endB - startB).toDouble() / step

        val colorArr = arrayListOf<Int>()

        for (i in 0 until step) {
            //计算每一步的hex值
            val hex =
                    Color.rgb(
                        (sR * i + startR).toInt(),
                        (sG * i + startG).toInt(),
                        (sB * i + startB).toInt()
                    )
            colorArr.add(hex)
        }
        return colorArr
    }

    interface OnChangeListener {
        // 进度改变
        fun onProgressChanged(seekBar: ColorSeekBar)

        //开始拖动
        fun onTrackingTouchStart(seekBar: ColorSeekBar)

        //拖动结束
        fun onTrackingTouchFinish(seekBar: ColorSeekBar)

    }

    interface OnDrawListener {
        //开始绘制
        fun onDrawStart(seekBar: ColorSeekBar)
        //绘制完成
        fun onDrawFinish(seekBar: ColorSeekBar)
    }
}
