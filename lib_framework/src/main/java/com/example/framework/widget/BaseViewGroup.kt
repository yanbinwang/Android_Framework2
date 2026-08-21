package com.example.framework.widget

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isEmpty
import androidx.core.view.isNotEmpty
import androidx.lifecycle.LifecycleOwner
import com.example.framework.utils.function.view.doOnceAfterLayout
import com.example.framework.utils.function.view.getLifecycleOwner

/**
 * 自定义控件继承 ViewGroup 需要清除边距，使用当前类做处理
 * 1) 自定义控件如果宽度是手机宽度，则可用当前 BaseViewGroup，否则推荐使用继承 FrameLayout
 * 2) 如果嵌套 NestedScrollView 记得添加属性 android:fillViewport="true" 保证子布局撑满
 */
abstract class BaseViewGroup @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : ViewGroup(context, attrs, defStyleAttr) {
    private var isAdded = false
    protected var lifecycleOwner: LifecycleOwner? = null
    protected val shouldInflate get() = childCount <= 0 // 检测布局绘制 -> 只容许容器内有一个插入的xml

    /**
     * 自动绑定 LifecycleOwner , 分两种触发条件
     * 1) XML 布局已附加到窗口的情况 (类加载器执行完毕)
     * 2) 父容器已经在屏幕上显示了（如 Activity 里的布局） 此时父容器调用 addView() 方法 (parentAlreadyOnScreen.addView(yourNewView))
     */
    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        getLifecycleOwner()?.let { addLifecycleOwner(it) }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        // 解绑后重置标记
        isAdded = false
        // 清空引用
        lifecycleOwner = null
    }

    /**
     * widthMeasureSpec 和 heightMeasureSpec 是由系统传入的测量规格参数，它们封装了父容器对该控件在宽度和高度上的测量要求，包括测量模式和尺寸大小
     *
     * 测量模式：
     * MeasureSpec.EXACTLY：父布局已经明确指定了子布局的大小，子布局应该按照这个指定大小来布局
     *  必须调用 setMeasuredDimension(width, height)，且传入的值就是 MeasureSpec 里的 size，子视图自己的内容多长多宽完全不重要。哪怕内容只有 10px，父容器说你是 200px，你就是 200px
     *
     * MeasureSpec.AT_MOST：子布局最大可以达到父布局指定的大小，但子布局可以根据自身内容调整大小，不过不能超过父布局指定的最大值
     *  先测量自己的内容（文字、图片、子子视图等），算出一个“理想尺寸”，然后取 min(理想尺寸, MeasureSpec.size)，绝对不能超过 size，但可以比 size 小
     *
     * MeasureSpec.UNSPECIFIED：父布局对子布局的大小没有限制，子布局可以根据自身内容来决定大小
     *  完全忽略 MeasureSpec 里的 size（它通常是 0 或无意义值）, 想多大就多大，返回自己内容的真实自然尺寸
     *
     * 实现步骤：
     * 1) 通过 MeasureSpec.getMode 和 MeasureSpec.getSize 方法来获取传入的测量模式和尺寸
     * 2) 根据不同的测量模式，结合自定义控件的特性，计算出合适的宽度和高度
     * 3) 通过 setMeasuredDimension 方法将计算出的宽度和高度设置给控件
     *
     * MeasureSpec.makeMeasureSpec：用于创建测量规格（MeasureSpec）的静态方法。测量规格是一个 32 位的整数，它封装了父容器对子视图的尺寸要求，包含两个部分：尺寸大小和测量模式
     */
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        // 如果没有子视图，使用默认的测量逻辑
        if (isEmpty()) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec)
            return
        }
        // 父容器可用宽高（即自定义控件本身被绘制在 xml 时设置好 wrap/match 时得到的系统给的宽高）
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)
        // 父容器测量模式
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        // 获取子视图
        val child = getChildAt(0)
        // 获取子视图布局参数，用于描述视图（View）在父容器（ViewGroup）中如何布局的一组参数
        val childLayoutParams = child.layoutParams
        // 生成子视图测量规格
        val childWidthMeasureSpec: Int
        val childHeightMeasureSpec: Int
        /**
         * 【特殊兜底】仅针对代码 new 且未设置宽高的子 View
         * 原因：new View() 默认 LayoutParams 为 WRAP_CONTENT，但作为 BaseViewGroup 的直接子 View，业务上应撑满父容器。若不在此处矫正为 EXACTLY，未设宽高的子 View 会缩成内容大小，导致布局异常
         * 注：XML inflate 的子 View 宽高由 XML 属性决定，不会命中此分支
         */
        if (childLayoutParams.width == LayoutParams.WRAP_CONTENT && childLayoutParams.height == LayoutParams.WRAP_CONTENT) {
            // 指定测量模式为 EXACTLY：表示子视图必须精确使用这个尺寸。当作 MATCH_PARENT 处理
            childWidthMeasureSpec = MeasureSpec.makeMeasureSpec(widthSize, MeasureSpec.EXACTLY)
            childHeightMeasureSpec = MeasureSpec.makeMeasureSpec(heightSize, MeasureSpec.EXACTLY)
        } else if (child is ConstraintLayout) {
            // 对于 ConstraintLayout，直接使用父容器的测量规格（内部具有 0dp 等各种约束条件）
            childWidthMeasureSpec = widthMeasureSpec
            childHeightMeasureSpec = heightMeasureSpec
        } else {
            // 根据父容器的测量规格和子视图的 LayoutParams 确定子视图的测量规格
            childWidthMeasureSpec = resolveChildMeasureSpec(widthSize, widthMode, childLayoutParams.width)
            childHeightMeasureSpec = resolveChildMeasureSpec(heightSize, heightMode, childLayoutParams.height)
        }
        // 测量子视图 -> 父容器传递 widthMeasureSpec 和 heightMeasureSpec 参数，描述子视图的可用空间和约束条件
        child.measure(childWidthMeasureSpec, childHeightMeasureSpec)
        // 计算最终尺寸
        val finalWidth = resolveFinalSize(widthSize, widthMode, childLayoutParams.width, child.measuredWidth)
        val finalHeight = resolveFinalSize(heightSize, heightMode, childLayoutParams.height, child.measuredHeight)
        // 设置父容器的测量尺寸
        setMeasuredDimension(finalWidth, finalHeight)
    }

    /**
     * 根据父容器测量规格和子视图 LayoutParams 生成子视图的 MeasureSpec
     */
    private fun resolveChildMeasureSpec(parentSize: Int, parentMode: Int, childDimension: Int): Int {
        return when (childDimension) {
            // 如果子视图宽度/高度为 MATCH_PARENT，使用父容器的精确宽度/高度
            LayoutParams.MATCH_PARENT -> {
                if (parentMode == MeasureSpec.UNSPECIFIED) {
                    // 父容器无限制，子视图也无限制
                    MeasureSpec.makeMeasureSpec(parentSize, MeasureSpec.UNSPECIFIED)
                } else {
                    // 父容器有确定尺寸，子视图填充
                    MeasureSpec.makeMeasureSpec(parentSize, MeasureSpec.EXACTLY)
                }
            }
            // 如果子视图宽度/高度为 WRAP_CONTENT，使用父容器的 AT_MOST 模式
            LayoutParams.WRAP_CONTENT -> {
                if (parentMode == MeasureSpec.UNSPECIFIED) {
                    // 父容器无限制，子视图也无限制
                    MeasureSpec.makeMeasureSpec(parentSize, MeasureSpec.UNSPECIFIED)
                } else {
                    // 子视图不能超过父容器
                    MeasureSpec.makeMeasureSpec(parentSize, MeasureSpec.AT_MOST)
                }
            }
            // 如果子视图有固定宽度/高度，使用精确模式，以子视图为主
            else -> {
                MeasureSpec.makeMeasureSpec(childDimension, MeasureSpec.EXACTLY)
            }
        }
    }

    /**
     * 根据父容器测量规格、子视图 LayoutParams 和子视图已测量尺寸，计算最终尺寸
     */
    private fun resolveFinalSize(parentSize: Int, parentMode: Int, childDimension: Int, childMeasuredSize: Int): Int {
        return when (parentMode) {
            MeasureSpec.EXACTLY -> parentSize
            MeasureSpec.AT_MOST -> {
                if (childDimension == LayoutParams.MATCH_PARENT) {
                    parentSize
                } else {
                    childMeasuredSize.coerceAtMost(parentSize)
                }
            }
            MeasureSpec.UNSPECIFIED -> childMeasuredSize
            else -> parentSize
        }
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
     * onFinishInflate() 仅表示 XML 解析完成，但此时视图可能还未经过测量（measure）和布局（layout）流程，宽高可能尚未确定
     */
    override fun onFinishInflate() {
        super.onFinishInflate()
        // 确保 onInflate() 在视图完成布局后再执行，避免因布局未就绪导致的测量问题，如Viewpager2缓存导致页面高度为0
        doOnceAfterLayout {
            if (shouldInflate) onInflate()
        }
    }

    /**
     * 虽然自定义view在调用addView的时候已经用了isInflate做判断，但是我们还是重写一下该方法，抛一个错
     */
    override fun addView(child: View?, index: Int, params: LayoutParams?) {
        require(isEmpty()) { "容器只能包含一个子视图（XML 根布局）" }
        super.addView(child, index, params)
    }

    /**
     * 手动绑定 LifecycleOwner（用于代码创建的 View）
     * 必须在 addView 之前调用，否则会因 isAdded=true 而被忽略。
     * 适用场景：View 的挂载父容器与其业务生命周期归属不一致时
     * 例如在 A 页面预创建 View 但需跟随 B 页面生命周期、全局悬浮窗需跟随特定 Fragment 等。若未手动绑定，将在 onAttachedToWindow 时自动从 ViewTree 中查找。
     */
    open fun addLifecycleOwner(owner: LifecycleOwner) {
        if (isAdded || lifecycleOwner != null) return
        lifecycleOwner = owner
        isAdded = true
    }

    /**
     * 容器在new的时候不会走onFinishInflate方法，需要手动调取
     */
    abstract fun onInflate()

}