package com.weather.util

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.TimeInterpolator
import android.animation.ValueAnimator
import android.view.View

object OCAnim{
     fun animateOpen(view: View, height: Int) {
        view.visibility = View.VISIBLE
        val animator = createDropAnimator(
            view, 0,
            height
        )
        animator.duration = 400
        animator.interpolator = TimeInterpolator {
            (Math.cos((it + 1) * Math.PI) / 2.0f).toFloat() + 0.5f
        }
        animator.start()
    }


    fun animateClose(view: View, height: Int) {
        val origHeight = view.height
        val animator = createDropAnimator(view, origHeight, 0)
        animator.duration = 400
        animator.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                view.visibility = View.GONE
            }
        })
        animator.start()
    }

    private fun createDropAnimator(v: View, start: Int, end: Int): ValueAnimator {
        val animator = ValueAnimator.ofInt(start, end)
        animator.addUpdateListener { arg0 ->
            val value = arg0.animatedValue as Int
            val layoutParams = v.layoutParams
            layoutParams.height = value
            v.layoutParams = layoutParams
        }
        return animator
    }
}