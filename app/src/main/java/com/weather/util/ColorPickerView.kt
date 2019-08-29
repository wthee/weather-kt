package com.weather.util

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.SweepGradient
import android.view.MotionEvent
import android.view.View

class ColorPickerView : View {

    private var paintCirclePhantom: Paint? = null
    var paintCircle: Paint? = null
    var paintCenterShadow: Paint? = null
    var paintCenter: Paint? = null
    var paintGrayShadow: Paint? = null
    var paintGray: Paint? = null
    private var paintLightShadow: Paint? = null
    private var paintLight: Paint? = null
    var zoom: Double = 0.toDouble()
    var arrColorGray: IntArray? = null
    val arrColorCircle = intArrayOf(-0x10000, -0xff01, -0xffff01, -0xff0001, -0xff0100, -0x100, -0x10000)
    private var mRedrawHSV: Boolean = false
    var isIsPressCenter: Boolean = false
    var isIsMoveCenter: Boolean = false

    private var CenterX = 100
    private var CenterY = 100
    private var CenterRadius = 30
    var strColor = ""

    private var l: OnColorBackListener? = null

    constructor(context: Context) : super(context) {
        val density = getContext().resources.displayMetrics.density
        val Zoom = density / 2.0 + 0.5
        val color = Color.parseColor("#FFFFFF")
        init(color, Zoom)
    }

    constructor(context: Context, color: Int, Zoom: Double) : super(context) {
        init(color, Zoom)
    }

    private fun init(color: Int, Zoom: Double) {
        this.zoom = Zoom
        CenterX = (100 * Zoom).toInt()
        CenterY = (100 * Zoom).toInt()
        CenterRadius = (30 * Zoom).toInt()
        paintCirclePhantom = Paint(Paint.ANTI_ALIAS_FLAG)
        paintCircle = Paint(Paint.ANTI_ALIAS_FLAG)
        paintCenterShadow = Paint(Paint.ANTI_ALIAS_FLAG)
        paintCenter = Paint(Paint.ANTI_ALIAS_FLAG)
        paintGrayShadow = Paint(Paint.ANTI_ALIAS_FLAG)
        paintGray = Paint(Paint.ANTI_ALIAS_FLAG)
        paintLightShadow = Paint(Paint.ANTI_ALIAS_FLAG)
        paintLight = Paint(Paint.ANTI_ALIAS_FLAG)
        paintCirclePhantom!!.color = -0x1000000
        paintCirclePhantom!!.style = Paint.Style.STROKE
        paintCirclePhantom!!.strokeWidth = (32 * Zoom).toFloat()

        paintCircle!!.shader = SweepGradient(0f, 0f, arrColorCircle, null)
        paintCircle!!.style = Paint.Style.STROKE
        paintCircle!!.strokeWidth = (32 * Zoom).toFloat()

        paintCenterShadow!!.color = -0x1000000
        paintCenterShadow!!.strokeWidth = (5 * Zoom).toFloat()

        paintCenter!!.color = color
        paintCenter!!.strokeWidth = (5 * Zoom).toFloat()

        paintGrayShadow!!.color = -0x1000000
        paintGrayShadow!!.strokeWidth = (30 * Zoom).toFloat()

        arrColorGray = intArrayOf(-0x1, color, -0x1000000)
        paintGray!!.strokeWidth = (30 * Zoom).toFloat()

        paintLightShadow!!.color = -0x1000000
        paintLightShadow!!.strokeWidth = (60 * Zoom).toFloat()

        paintLight!!.strokeWidth = (60 * Zoom).toFloat()

        mRedrawHSV = true
    }

    override fun onDraw(canvas: Canvas) {
        canvas.translate(CenterX.toFloat(), CenterY.toFloat())
        val r = CenterX - paintCircle!!.strokeWidth * 0.5f
        val color = paintCenter!!.color
        strColor = "#" + Integer.toHexString(color).substring(2).toUpperCase()

        if (mRedrawHSV) {
            arrColorGray?.set(1, color)
            paintGray!!.shader = LinearGradient(CenterX.toFloat(), (-CenterY).toFloat(), CenterX.toFloat(),
                    (100 * zoom).toFloat(), arrColorGray!!, null,
                    Shader.TileMode.CLAMP)
        }

        canvas.drawOval(RectF(-r + 3, -r + 3, r + 3, r + 3),
                paintCirclePhantom!!)
        canvas.drawOval(RectF(-r, -r, r, r), paintCircle!!)
        canvas.drawCircle(3f, 3f, CenterRadius.toFloat(), paintCenterShadow!!)
        canvas.drawCircle(0f, 0f, CenterRadius.toFloat(), paintCenter!!)
        canvas.drawRect(RectF(CenterX + (18 * zoom).toFloat(), (-CenterY + 3).toFloat(),
                CenterX + (48 * zoom).toFloat(), (103 * zoom).toFloat()),
                paintGrayShadow!!)
        canvas.drawRect(RectF(CenterX + (15 * zoom).toFloat(), (-CenterY).toFloat(),
                CenterX + (45 * zoom).toFloat(), (100 * zoom).toFloat()), paintGray!!)

        if (isIsPressCenter) {
            paintCenter!!.style = Paint.Style.STROKE

            if (isIsMoveCenter)
                paintCenter!!.alpha = 0xFF
            else
                paintCenter!!.alpha = 0x66

            canvas.drawCircle(0f, 0f,
                    CenterRadius + paintCenter!!.strokeWidth, paintCenter!!)
            paintCenter!!.style = Paint.Style.FILL
            paintCenter!!.color = color
        }

        mRedrawHSV = true
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        setMeasuredDimension(CenterX * 2 + 50, CenterY * 2 + 23)
    }

    private fun ave(s: Int, d: Int, p: Float): Int {
        return s + java.lang.Math.round(p * (d - s))
    }

    private fun interpColor(colors: IntArray, unit: Float): Int {
        if (unit <= 0) {
            return colors[0]
        }
        if (unit >= 1) {
            return colors[colors.size - 1]
        }

        var p = unit * (colors.size - 1)
        val i = p.toInt()
        p -= i.toFloat()

        val c0 = colors[i]
        val c1 = colors[i + 1]
        val a = ave(Color.alpha(c0), Color.alpha(c1), p)
        val r = ave(Color.red(c0), Color.red(c1), p)
        val g = ave(Color.green(c0), Color.green(c1), p)
        val b = ave(Color.blue(c0), Color.blue(c1), p)
        if (l != null) {
            l!!.onColorBack(a, r, g, b)
        }
        return Color.argb(a, r, g, b)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val x = event.x - CenterX
        val y = event.y - CenterY
        val inCenter = java.lang.Math.sqrt((x * x + y * y).toDouble()) <= CenterRadius

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                run {
                    isIsPressCenter = inCenter
                    if (inCenter) {
                        isIsMoveCenter = true
                        invalidate()
                    }
                }
                run {
                    if (isIsPressCenter) {
                        if (isIsMoveCenter != inCenter) {
                            isIsMoveCenter = inCenter
                            invalidate()
                        }
                    } else if (x >= -CenterX && x <= CenterX && y >= -CenterY && y <= CenterY) {
                        val angle = java.lang.Math.atan2(y.toDouble(), x.toDouble()).toFloat()
                        var unit = angle / (2 * PI)
                        if (unit < 0)
                            unit += 1f
                        paintCenter!!.color = interpColor(arrColorCircle, unit)
                        invalidate()
                    } else {
                        val a: Int
                        val r: Int
                        val g: Int
                        val b: Int
                        val c0: Int
                        val c1: Int
                        val p: Float

                        if (y < 0) {
                            c0 = arrColorGray!![0]
                            c1 = arrColorGray!![1]
                            p = (y + 100) / 100
                        } else {
                            c0 = arrColorGray!![1]
                            c1 = arrColorGray!![2]
                            p = y / 100
                        }

                        a = ave(Color.alpha(c0), Color.alpha(c1), p)
                        r = ave(Color.red(c0), Color.red(c1), p)
                        g = ave(Color.green(c0), Color.green(c1), p)
                        b = ave(Color.blue(c0), Color.blue(c1), p)

                        paintCenter!!.color = Color.argb(a, r, g, b)
                        mRedrawHSV = false
                        if (l != null) {
                            l!!.onColorBack(a, r, g, b)
                        }
                        invalidate()
                    }
                }
            }
            MotionEvent.ACTION_MOVE -> {
                if (isIsPressCenter) {
                    if (isIsMoveCenter != inCenter) {
                        isIsMoveCenter = inCenter
                        invalidate()
                    }
                } else if (x >= -CenterX && x <= CenterX && y >= -CenterY && y <= CenterY) {
                    val angle = java.lang.Math.atan2(y.toDouble(), x.toDouble()).toFloat()
                    var unit = angle / (2 * PI)
                    if (unit < 0)
                        unit += 1f
                    paintCenter!!.color = interpColor(arrColorCircle, unit)
                    invalidate()
                } else {
                    val a: Int
                    val r: Int
                    val g: Int
                    val b: Int
                    val c0: Int
                    val c1: Int
                    val p: Float
                    if (y < 0) {
                        c0 = arrColorGray!![0]
                        c1 = arrColorGray!![1]
                        p = (y + 100) / 100
                    } else {
                        c0 = arrColorGray!![1]
                        c1 = arrColorGray!![2]
                        p = y / 100
                    }
                    a = ave(Color.alpha(c0), Color.alpha(c1), p)
                    r = ave(Color.red(c0), Color.red(c1), p)
                    g = ave(Color.green(c0), Color.green(c1), p)
                    b = ave(Color.blue(c0), Color.blue(c1), p)
                    paintCenter!!.color = Color.argb(a, r, g, b)
                    mRedrawHSV = false
                    if (l != null) {
                        l!!.onColorBack(a, r, g, b)
                    }
                    invalidate()
                }
            }
            MotionEvent.ACTION_UP -> {
                if (isIsPressCenter) {
                    isIsPressCenter = false
                    invalidate()
                }
            }
        }
        return true
    }

    interface OnColorBackListener {
        fun onColorBack(a: Int, r: Int, g: Int, b: Int)
    }

    companion object {
        private val PI = Math.PI.toFloat()
    }
}
