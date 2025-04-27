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
        for (i in 0 until childCount) {
            val child = getChildAt(i)
            // 让子视图使用父视图的测量规格进行测量
            child.measure(widthMeasureSpec, heightMeasureSpec)
        }
        // 根据父视图的测量规格设置自身的测量尺寸
        setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.getSize(heightMeasureSpec))
//        // 判空
//        if (isEmpty()) {
//            super.onMeasure(widthMeasureSpec, heightMeasureSpec)
//            return
//        }
//        // 获取插入的root
//        val child = getChildAt(0)
//        val lp = child.layoutParams
//        // 测量子视图
//        if (child is ConstraintLayout) {
//            // 如果子视图是 ConstraintLayout，强制使用父容器的测量规格
//            val childWidthMeasureSpec = MeasureSpec.makeMeasureSpec(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.EXACTLY)
//            val childHeightMeasureSpec = MeasureSpec.makeMeasureSpec(MeasureSpec.getSize(heightMeasureSpec), MeasureSpec.EXACTLY)
//            child.measure(childWidthMeasureSpec, childHeightMeasureSpec)
//        } else {
//            // 对于其他类型的子视图，使用常规的测量规格生成方法
//            val childWidthMeasureSpec = getChildMeasureSpec(widthMeasureSpec, 0, lp.width)
//            val childHeightMeasureSpec = getChildMeasureSpec(heightMeasureSpec, 0, lp.height)
//            child.measure(childWidthMeasureSpec, childHeightMeasureSpec)
//        }
//        // 获取父视图的测量模式和大小
//        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
//        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
//        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
//        val heightSize = MeasureSpec.getSize(heightMeasureSpec)
//        var measuredWidth = 0
//        var measuredHeight = 0
//        // 根据测量模式确定最终宽度
//        when (widthMode) {
//            MeasureSpec.EXACTLY -> {
//                measuredWidth = widthSize
//            }
//            MeasureSpec.AT_MOST -> {
//                measuredWidth = if (lp.width == LayoutParams.MATCH_PARENT) {
//                    widthSize
//                } else {
//                    child.measuredWidth
//                }
//            }
//            MeasureSpec.UNSPECIFIED -> {
//                measuredWidth = child.measuredWidth
//            }
//        }
//        // 根据测量模式确定最终高度
//        when (heightMode) {
//            MeasureSpec.EXACTLY -> {
//                measuredHeight = heightSize
//            }
//            MeasureSpec.AT_MOST -> {
//                measuredHeight = if (lp.height == LayoutParams.MATCH_PARENT) {
//                    heightSize
//                } else {
//                    child.measuredHeight
//                }
//            }
//            MeasureSpec.UNSPECIFIED -> {
//                measuredHeight = child.measuredHeight
//            }
//        }
//        // 设置父视图的测量尺寸
//        setMeasuredDimension(measuredWidth, measuredHeight)
    }

    /**
     * 所有子类的子视图都撑满容器
     */
    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
//        for (i in 0 until childCount) {
//            getChildAt(i).layout(0, 0, measuredWidth, measuredHeight)
//        }
        if (isNotEmpty()) {
            val child = getChildAt(0)
            val lp = child.layoutParams
            var left = paddingLeft
            var top = paddingTop
            if (lp is MarginLayoutParams) {
                left += lp.leftMargin
                top += lp.topMargin
            }
            val right = left + child.measuredWidth
            val bottom = top + child.measuredHeight
            child.layout(left, top, right, bottom)
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