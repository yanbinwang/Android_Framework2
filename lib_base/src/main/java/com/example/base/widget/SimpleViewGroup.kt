package com.example.base.widget

import android.content.Context
import android.util.AttributeSet
import android.view.ViewGroup

/**
 * 自定义控件继承viewgroup需要清除边距，使用当前类做处理
 */
abstract class SimpleViewGroup : ViewGroup {

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet? = null) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : super(context, attrs, defStyleAttr)

    constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0, defStyleRes: Int = 0) : super(context, attrs, defStyleAttr, defStyleRes)

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        for (i in 0 until childCount) {
            getChildAt(i).measure(widthMeasureSpec, heightMeasureSpec)
        }
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        for (i in 0 until childCount) {
            getChildAt(i).layout(0, 0, r, b)
        }
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        if (onFinish()) drawView()
    }

    /**
     * 检测布局绘制
     */
    protected fun onFinish() = childCount <= 0

    /**
     * 容器在new的时候不会走onFinishInflate方法，需要手动调取
     */
    abstract fun drawView()

}