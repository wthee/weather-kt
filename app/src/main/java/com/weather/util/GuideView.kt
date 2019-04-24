package com.weather

import android.app.Dialog
import android.graphics.Color
import android.graphics.Rect
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.*
import android.widget.TextView
import androidx.core.graphics.ColorUtils
import androidx.fragment.app.DialogFragment

class GuideView(yourview: View, guideN: Int) : DialogFragment() {


    private var guideParent = yourview
    private var guideN = guideN
    private lateinit var dm: DisplayMetrics
    private lateinit var params: WindowManager.LayoutParams
    private var bgWidth = 0//背景宽
    private var bgHeight = 0//背景高
    private var guideX = 0//需要引导的view的x坐标
    private var guideY = 0//需要引导的view的y坐标
    private var guideWidth = 0//需要引导的view的宽
    private var guideHeight = 0//需要引导的view的高

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {


        var view = inflater.inflate(R.layout.guide1, container, false)
        var title = view.findViewById(R.id.title) as TextView
        when(guideN){
            1 ->{
                title.text = resources.getString(R.string.guide1)
            }
            2->{
                title.text = resources.getString(R.string.guide2)
            }
            3 ->{
                title.text = resources.getString(R.string.guide3)
            }
            4->{
                title.text = resources.getString(R.string.guide4)
            }
        }


        view.setOnClickListener {
            this.dismiss()
        }

        return view
    }

    override fun onStart() {
        super.onStart()

        dm = DisplayMetrics()
        activity!!.windowManager.defaultDisplay.getMetrics(dm)

        val dw = dialog.window
        dw!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT)) //一定要设置背景

        params = dw.attributes
        //params.dimAmount = 0f

        initSize()

        params.x = guideX
        params.y = guideY

        dw.attributes = params
    }

    private fun initSize() {

        bgWidth = dm.widthPixels//设置背景宽为屏幕宽
        bgHeight = dm.heightPixels//设置背景高为屏幕高
        var location =  IntArray(2)
        guideParent.getLocationOnScreen(location)//得到需要引导的view在整个屏幕中的坐标
        guideWidth = guideParent.width
        guideHeight = guideParent.height
        guideX = guideWidth  + location[0]  - bgWidth /2
        guideY = 50 + guideHeight  + location[1] - bgHeight /2
            //density = dm.density;//获取像素密度 2.0，2.5，3.0
            //distance = 10 * density;//箭头与需要引导的view之间的距离

    }

}