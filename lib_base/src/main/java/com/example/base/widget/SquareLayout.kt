package com.example.base.widget

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout

/**
 * author: wyb
 * date: 2017/8/29.
 * 嵌套的外层布局，使view的宽高一致
 */
class SquareLayout @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : FrameLayout(context, attrs, defStyleAttr) {

    /**
     * 重写此方法后默认调用父类的onMeasure方法,分别将宽度测量空间与高度测量空间传入
     * @param widthMeasureSpec
     * @param heightMeasureSpec
     */
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, widthMeasureSpec)
    }

}