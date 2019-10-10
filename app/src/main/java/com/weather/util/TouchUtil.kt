package com.weather.util

import kotlin.math.abs

object TouchUtil {
    private const val LEFT = -1
    private const val RIGHT = 1

    private var downX = 0f
    private var downY = 0f
    private var moveX = 0f
    private var moveY = 0f



    fun setDownXY(x: Float, y: Float): TouchUtil {
        this.downX = x
        this.downY = y
        return this
    }

    fun setMoveXY(x: Float, y: Float): TouchUtil {
        this.moveX = x
        this.moveY = y
        return this
    }
    
    /**
     * 手指抬起时调用
     */
    fun actionUp():Int {
        if(moveX != 0f && abs(moveY - downY) < 200){
            if (moveX - downX > 0
                && (abs(moveX - downX) > 150)) {
                //向右滑动
                return RIGHT
            } else if (moveX - downX < 0
                && (abs(moveX - downX) > 150)) {
                //向左滑动
                return LEFT
            }
        }
        moveX = 0f
        moveY = 0f
        return 0
    }
}