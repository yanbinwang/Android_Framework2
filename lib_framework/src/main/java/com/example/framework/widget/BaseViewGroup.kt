package com.example.framework.widget

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isEmpty
import androidx.core.view.isNotEmpty

/**
 * 自定义控件继承viewgroup需要清除边距，使用当前类做处理
 * 自定义控件如果宽度是手机宽度，则可用当前BaseViewGroup，否则推荐使用继承FrameLayout
 */
abstract class BaseViewGroup @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : ViewGroup(context, attrs, defStyleAttr) {
    //检测布局绘制->只容许容器内有一个插入的xml
    protected val isInflate get() = childCount <= 0

    /**
     * MeasureSpec.EXACTLY：父布局已经明确指定了子布局的大小，子布局应该按照这个指定大小来布局。
     * MeasureSpec.AT_MOST：子布局最大可以达到父布局指定的大小，但子布局可以根据自身内容调整大小，不过不能超过父布局指定的最大值。
     * MeasureSpec.UNSPECIFIED：父布局对子布局的大小没有限制，子布局可以根据自身内容来决定大小。
     */
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        // 判空
        if (isEmpty()) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec)
            return
        }
        val child = getChildAt(0)
        // 强制子视图使用父容器的测量规格（无论子视图布局参数）
        child.measure(widthMeasureSpec, heightMeasureSpec)
        // 父容器尺寸与子视图完全一致
        setMeasuredDimension(child.measuredWidth, child.measuredHeight)
    }

    /**
     * 所有子类的子视图都撑满容器
     */
    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        if (isNotEmpty()) {
            val child = getChildAt(0)
            // 子视图撑满父容器
            child.layout(0, 0, measuredWidth, measuredHeight)
        }
    }

    /**
     * 当页面被绘制好了，如果本身内部没有容器，回调onInflate抽象方法，让自定义view去在onInflate方法里去addView
     */
    override fun onFinishInflate() {
        super.onFinishInflate()
        if (isInflate) onInflate()
    }

    /**
     * 虽然自定义view在调用addView的时候已经用了isInflate做判断，但是我们还是重写一下该方法，抛一个错
     */
    override fun addView(child: View?, index: Int, params: LayoutParams?) {
        require(isEmpty()) { "容器只能包含一个子视图（XML 根布局）" }
        super.addView(child, index, params)
    }

    /**
     * 容器在new的时候不会走onFinishInflate方法，需要手动调取
     */
    abstract fun onInflate()

}