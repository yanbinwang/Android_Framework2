package com.example.base.widget

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.widget.LinearLayout

/**
 * @description 根据比重动态分配宽高,默认居中上下结构，4等分
 * @author yan
 */
class WeightLayout @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : LinearLayout(context, attrs, defStyleAttr) {
    var weight = 4

    init {
        gravity = Gravity.CENTER
        orientation = VERTICAL
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec / weight, widthMeasureSpec)
    }

}