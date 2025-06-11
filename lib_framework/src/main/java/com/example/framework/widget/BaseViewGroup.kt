package com.example.framework.widget

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isEmpty
import androidx.core.view.isNotEmpty
import androidx.lifecycle.LifecycleOwner
import com.example.framework.utils.function.view.getLifecycleOwner

/**
 * 自定义控件继承ViewGroup需要清除边距，使用当前类做处理
 * 自定义控件如果宽度是手机宽度，则可用当前BaseViewGroup，否则推荐使用继承FrameLayout
 * 如果嵌套NestedScrollView记得添加属性android:fillViewport="true"保证子布局撑满
 */
abstract class BaseViewGroup @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : ViewGroup(context, attrs, defStyleAttr) {
    private var isAdded = false
    protected var lifecycleOwner: LifecycleOwner? = null
    protected val isInflate get() = childCount <= 0 //检测布局绘制->只容许容器内有一个插入的xml

    /**
     * 手动绑定 LifecycleOwner（用于代码创建的 View）
     */
    open fun addLifecycleOwner(owner: LifecycleOwner) {
        if (isAdded) return
        lifecycleOwner = owner
        ensureAdded()
    }

    /**
     * 自动绑定 LifecycleOwner（XML 布局或已附加到窗口的情况）
     */
    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        if (isAdded) return
        lifecycleOwner = getLifecycleOwner()
        ensureAdded()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        isAdded = false // 解绑后重置标记
        lifecycleOwner = null // 清空引用
    }

    private fun ensureAdded() {
        if (isAdded) return
        isAdded = true
    }

    /**
     * widthMeasureSpec和heightMeasureSpec是由系统传入的测量规格参数，它们封装了父容器对该控件在宽度和高度上的测量要求，包括测量模式和尺寸大小
     *
     * 测量模式：
     * MeasureSpec.EXACTLY：父布局已经明确指定了子布局的大小，子布局应该按照这个指定大小来布局。
     * MeasureSpec.AT_MOST：子布局最大可以达到父布局指定的大小，但子布局可以根据自身内容调整大小，不过不能超过父布局指定的最大值。
     * MeasureSpec.UNSPECIFIED：父布局对子布局的大小没有限制，子布局可以根据自身内容来决定大小。
     *
     * 实现步骤：
     * 首先，通过MeasureSpec.getMode和MeasureSpec.getSize方法来获取传入的测量模式和尺寸。
     * 然后，根据不同的测量模式，结合自定义控件的特性，计算出合适的宽度和高度。
     * 最后，通过setMeasuredDimension方法将计算出的宽度和高度设置给控件。
     *
     * MeasureSpec.makeMeasureSpec-->用于创建测量规格（MeasureSpec）的静态方法。测量规格是一个 32 位的整数，它封装了父容器对子视图的尺寸要求，包含两个部分：尺寸大小和测量模式
     */
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        if (isEmpty()) {
            // 如果没有子视图，使用默认的测量逻辑
            super.onMeasure(widthMeasureSpec, heightMeasureSpec)
            return
        }
        // 提取父容器提供的可用宽高及对应测量模式（即自定义控件本身被绘制在xml时设置好wrap/match时得到的系统给的宽高）
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        // 获取子视图
        val child = getChildAt(0)
        // 取得子视图布局参数，用于描述视图（View）在父容器（ViewGroup）中如何布局的一组参数
        val childLayoutParams = child.layoutParams
        // 预设子视图宽高
        val childWidthMeasureSpec: Int
        val childHeightMeasureSpec: Int
        // 处理子视图未设置宽高的情况（代码直接创建，且并未设置size）
        if (childLayoutParams.width == LayoutParams.WRAP_CONTENT && childLayoutParams.height == LayoutParams.WRAP_CONTENT) {
            // 指定测量模式为 EXACTLY：表示子视图必须精确使用这个尺寸。当作 MATCH_PARENT 处理
            childWidthMeasureSpec = MeasureSpec.makeMeasureSpec(widthSize, MeasureSpec.EXACTLY)
            childHeightMeasureSpec = MeasureSpec.makeMeasureSpec(heightSize, MeasureSpec.EXACTLY)
        } else if (child is ConstraintLayout) {
            // 对于 ConstraintLayout，直接使用父容器的测量规格（内部具有0dp等各种约束条件）
            childWidthMeasureSpec = widthMeasureSpec
            childHeightMeasureSpec = heightMeasureSpec
        } else {
            // 根据父容器的测量规格和子视图的 LayoutParams 确定子视图的测量规格
            childWidthMeasureSpec = when (childLayoutParams.width) {
                // 如果子视图宽度为 MATCH_PARENT，使用父容器的精确宽度
                LayoutParams.MATCH_PARENT -> {
                    if (widthMode == MeasureSpec.UNSPECIFIED) {
                        MeasureSpec.makeMeasureSpec(widthSize, MeasureSpec.UNSPECIFIED) // 父容器无限制，子视图也无限制
                    } else {
                        MeasureSpec.makeMeasureSpec(widthSize, MeasureSpec.EXACTLY) // 父容器有确定尺寸，子视图填充
                    }
                }
                // 如果子视图宽度为 WRAP_CONTENT，使用父容器的 AT_MOST 模式
                LayoutParams.WRAP_CONTENT -> {
                    if (widthMode == MeasureSpec.UNSPECIFIED) {
                        MeasureSpec.makeMeasureSpec(widthSize, MeasureSpec.UNSPECIFIED) // 父容器无限制，子视图也无限制
                    } else {
                        MeasureSpec.makeMeasureSpec(widthSize, MeasureSpec.AT_MOST) // 子视图不能超过父容器
                    }
                }
                // 如果子视图有固定宽度，使用精确模式，以子视图为主
                else -> {
                    MeasureSpec.makeMeasureSpec(childLayoutParams.width, MeasureSpec.EXACTLY)
                }
            }
            childHeightMeasureSpec = when (childLayoutParams.height) {
                LayoutParams.MATCH_PARENT -> {
                    if (heightMode == MeasureSpec.UNSPECIFIED) {
                        MeasureSpec.makeMeasureSpec(heightSize, MeasureSpec.UNSPECIFIED) // 父容器无限制，子视图也无限制
                    } else {
                        MeasureSpec.makeMeasureSpec(heightSize, MeasureSpec.EXACTLY) // 父容器有确定尺寸，子视图填充
                    }
                }
                LayoutParams.WRAP_CONTENT -> {
                    if (heightMode == MeasureSpec.UNSPECIFIED) {
                        MeasureSpec.makeMeasureSpec(heightSize, MeasureSpec.UNSPECIFIED) // 父容器无限制，子视图也无限制
                    } else {
                        MeasureSpec.makeMeasureSpec(heightSize, MeasureSpec.AT_MOST) // 子视图不能超过父容器
                    }
                }
                else -> {
                    // 如果子视图有固定高度，使用精确模式
                    MeasureSpec.makeMeasureSpec(childLayoutParams.height, MeasureSpec.EXACTLY)
                }
            }
        }
        // 测量子视图-->父容器传递 widthMeasureSpec 和 heightMeasureSpec 参数，描述子视图的可用空间和约束条件
        child.measure(childWidthMeasureSpec, childHeightMeasureSpec)
        // 计算最终尺寸
        val finalWidth = when (widthMode) {
            MeasureSpec.EXACTLY -> widthSize
            MeasureSpec.AT_MOST -> {
                if (childLayoutParams.width == LayoutParams.MATCH_PARENT) {
                    widthSize
                } else {
                    child.measuredWidth.coerceAtMost(widthSize)
                }
            }
            MeasureSpec.UNSPECIFIED -> child.measuredWidth
            else -> widthSize
        }
        val finalHeight = when (heightMode) {
            MeasureSpec.EXACTLY -> heightSize
            MeasureSpec.AT_MOST -> {
                if (childLayoutParams.height == LayoutParams.MATCH_PARENT) {
                    heightSize
                } else {
                    child.measuredHeight.coerceAtMost(heightSize)
                }
            }
            MeasureSpec.UNSPECIFIED -> child.measuredHeight
            else -> heightSize
        }
        // 设置父容器的测量尺寸
        setMeasuredDimension(finalWidth, finalHeight)
    }

    /**
     * 所有子类的子视图都撑满容器
     */
    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        if (isNotEmpty()) {
            val child = getChildAt(0)
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