package com.example.common.widget

import android.animation.Animator
import android.animation.ValueAnimator
import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import android.os.Parcelable
import android.util.AttributeSet
import android.view.animation.Interpolator
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.core.content.withStyledAttributes
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import com.example.common.R
import com.example.framework.utils.function.value.orZero
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

/**
 * 可展开/折叠的布局容器。
 * 通过修改自身尺寸实现平滑的展开/折叠动画，支持垂直/水平方向、视差滚动效果，
 * 并自动处理配置变更时的动画中断与状态保存恢复。
 *
 * 使用示例：
 * <ExpandableLayout
 *     android:id="@+id/expandable"
 *     android:layout_width="match_parent"
 *     android:layout_height="wrap_content"
 *     app:el_duration="500"
 *     app:el_expanded="false"
 *     app:el_parallax="0">
 *     <!-- 子内容 -->
 * </ExpandableLayout>
 *
 * expandable.setOnExpansionUpdateListener { fraction, state ->
 *     if (state == State.EXPANDED || state == State.COLLAPSED) {
 *         homeMoreTv.visibility = if (state == State.EXPANDED) View.GONE else View.VISIBLE
 *     }
 * }
 * expandable.toggle()
 */
class ExpandableLayout @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : FrameLayout(context, attrs, defStyleAttr) {
    private var parallax = 0f
    private var expansion = 0f
    private var orientation = VERTICAL
    private var duration = DURATION
    private var state = State.COLLAPSED
    private var animator: ValueAnimator? = null
    private var listener: OnExpansionUpdateListener? = null
    private var interpolator: Interpolator = FastOutSlowInInterpolator()

    companion object {
        private const val HORIZONTAL = 0
        private const val VERTICAL = 1
        private const val DURATION = 300
        private const val KEY_SUPER_STATE = "super_state"
        private const val KEY_EXPANSION = "expansion"
    }

    /**
     * 展开/折叠的状态枚举
     */
    enum class State(val value: Int) {
        COLLAPSED(0),    // 完全折叠
        COLLAPSING(1),   // 正在折叠
        EXPANDING(2),    // 正在展开
        EXPANDED(3);     // 完全展开
    }

    /**
     * 从 XML 属性中读取初始配置，包括动画时长、初始展开状态、方向和视差系数，
     * 并根据初始 expansion 值设置对应的 State。
     */
    init {
        context.withStyledAttributes(attrs, R.styleable.ExpandableLayout) {
            duration = getInt(R.styleable.ExpandableLayout_el_duration, DURATION)
            expansion = if (getBoolean(R.styleable.ExpandableLayout_el_expanded, false)) 1f else 0f
            orientation = getInt(R.styleable.ExpandableLayout_android_orientation, VERTICAL)
            parallax = getFloat(R.styleable.ExpandableLayout_el_parallax, 1f)
        }
        state = if (expansion == 0f) State.COLLAPSED else State.EXPANDED
        setParallax(parallax)
    }

    /**
     * 保存当前展开状态到 Bundle 中，确保配置变更（如屏幕旋转）后能恢复正确的展开/折叠状态。
     * 注意：仅保存终态（0f 或 1f），动画中间态不保留。
     */
    override fun onSaveInstanceState(): Parcelable {
        val superState = super.onSaveInstanceState()
        val bundle = Bundle()
        expansion = if (isExpanded()) 1f else 0f
        bundle.putFloat(KEY_EXPANSION, expansion)
        bundle.putParcelable(KEY_SUPER_STATE, superState)
        return bundle
    }

    /**
     * 从 Bundle 中恢复展开状态和父类状态。
     * 如果传入的 Parcelable 不是预期的 Bundle 类型，则回退到默认的恢复逻辑。
     */
    override fun onRestoreInstanceState(parcelable: Parcelable?) {
        if (parcelable is Bundle) {
            expansion = parcelable.getFloat(KEY_EXPANSION).orZero
            state = if (expansion == 1f) State.EXPANDED else State.COLLAPSED
            val superState = parcelable.getParcelable<Parcelable>(KEY_SUPER_STATE)
            super.onRestoreInstanceState(superState)
            return
        }
        super.onRestoreInstanceState(parcelable)
    }

    /**
     * 配置变更（如语言切换、夜间模式）时取消正在进行的动画，
     * 避免动画引用旧配置导致异常或视觉错乱。
     */
    override fun onConfigurationChanged(newConfig: Configuration?) {
        animator?.cancel()
        super.onConfigurationChanged(newConfig)
    }

    /**
     * 核心测量逻辑：根据当前 expansion 值计算实际显示尺寸，
     * 同时应用视差偏移（translationX/Y）给所有子 View。
     * 当 expansion 为 0 且尺寸为 0 时，将 visibility 设为 GONE 以避免占据空间。
     */
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val width = measuredWidth
        val height = measuredHeight
        val size = if (orientation == LinearLayout.HORIZONTAL) width else height
        visibility = if (expansion == 0f && size == 0) GONE else VISIBLE
        val expansionDelta = size - (size * expansion).roundToInt()
        if (parallax > 0) {
            val parallaxDelta = expansionDelta * parallax
            for (i in 0..<childCount) {
                val child = getChildAt(i)
                if (orientation == HORIZONTAL) {
                    var direction = -1
                    if (layoutDirection == LAYOUT_DIRECTION_RTL) {
                        direction = 1
                    }
                    child.translationX = direction * parallaxDelta
                } else {
                    child.translationY = -parallaxDelta
                }
            }
        }
        if (orientation == HORIZONTAL) {
            setMeasuredDimension(width - expansionDelta, height)
        } else {
            setMeasuredDimension(width, height - expansionDelta)
        }
    }

    /**
     * 切换展开/折叠状态，默认带动画。
     * 等价于 [toggle(true)]。
     */
    fun toggle() {
        toggle(true)
    }

    /**
     * 切换展开/折叠状态。
     * @param animate 是否使用过渡动画；传 false 时立即跳到目标状态。
     */
    fun toggle(animate: Boolean) {
        if (isExpanded()) {
            collapse(animate)
        } else {
            expand(animate)
        }
    }

    /**
     * 展开布局，默认带动画。
     * 等价于 [expand(true)]。
     */
    fun expand() {
        expand(true)
    }

    /**
     * 展开布局。
     * @param animate 是否使用过渡动画。
     */
    fun expand(animate: Boolean) {
        setExpanded(true, animate)
    }

    /**
     * 折叠布局，默认带动画。
     * 等价于 [collapse(true)]。
     */
    fun collapse() {
        collapse(true)
    }

    /**
     * 折叠布局。
     * @param animate 是否使用过渡动画。
     */
    fun collapse(animate: Boolean) {
        setExpanded(false, animate)
    }

    /**
     * 获取当前的展开比例。
     * @return 0f（完全折叠）到 1f（完全展开）之间的浮点值，动画过程中为中间值。
     */
    fun getExpansion(): Float {
        return expansion
    }

    /**
     * 设置展开/折叠状态，默认带动画。
     * 等价于 [setExpanded(expand, true)]。
     * @param expand true 为展开，false 为折叠。
     */
    fun setExpanded(expand: Boolean) {
        setExpanded(expand, true)
    }

    /**
     * 设置展开/折叠状态。
     * 如果目标状态与当前一致则忽略；若当前正处于动画中，会先取消旧动画再启动新动画。
     * @param expand true 为展开，false 为折叠。
     * @param animate 是否使用过渡动画；传 false 时立即生效，不触发 OnExpansionUpdateListener 的中间回调。
     */
    fun setExpanded(expand: Boolean, animate: Boolean) {
        if (expand == isExpanded()) {
            return
        }
        val targetExpansion = if (expand) 1 else 0
        if (animate) {
            animateSize(targetExpansion)
        } else {
            setExpansion(targetExpansion.toFloat())
        }
    }

    /**
     * 创建并启动从当前 expansion 到目标值的属性动画。
     * 每次调用都会取消上一次未完成的动画，保证不会出现多个动画叠加。
     * @param targetExpansion 目标展开值，0 或 1。
     */
    private fun animateSize(targetExpansion: Int) {
        animator?.cancel()
        animator = null
        animator = ValueAnimator.ofFloat(expansion, targetExpansion.toFloat())
        animator?.interpolator = interpolator
        animator?.setDuration(duration.toLong())
        animator?.addUpdateListener { valueAnimator -> setExpansion(valueAnimator.getAnimatedValue() as Float) }
        animator?.addListener(ExpansionListener(targetExpansion))
        animator?.start()
    }

    /**
     * 直接设置展开比例并刷新布局。
     * 这是动画更新和外部手动控制的核心入口，会同步更新 state、visibility，
     * 并通过 requestLayout() 触发重新测量以反映新的尺寸。
     * @param expansion 目标展开比例，有效范围 [0f, 1f]。
     */
    fun setExpansion(expansion: Float) {
        if (this.expansion == expansion) {
            return
        }
        val delta = expansion - this.expansion
        state = when {
            expansion == 0f -> State.COLLAPSED
            expansion == 1f -> State.EXPANDED
            delta < 0 -> State.COLLAPSING
            delta > 0 -> State.EXPANDING
            // 无变化，保留当前状态（防止极端情况）
            else -> state
        }
        visibility = if (state == State.COLLAPSED) GONE else VISIBLE
        this.expansion = expansion
        requestLayout()
        listener?.onExpansionUpdate(expansion, state)
    }

    /**
     * 获取当前动画时长（毫秒）。
     */
    fun getDuration(): Int {
        return duration
    }

    /**
     * 动态设置动画时长。
     * 仅对后续触发的动画生效，不影响正在播放的动画。
     * @param duration 动画时长，单位毫秒。
     */
    fun setDuration(duration: Int) {
        this.duration = duration
    }

    /**
     * 动态设置动画插值器。
     * 仅对后续触发的动画生效。
     * @param interpolator 自定义插值器实例。
     */
    fun setInterpolator(interpolator: Interpolator) {
        this.interpolator = interpolator
    }

    /**
     * 获取当前视差系数。
     * @return 0f（无视差）到 1f（最大视差）之间的值。
     */
    fun getParallax(): Float {
        return parallax
    }

    /**
     * 动态设置视差系数。
     * 值会被自动钳制到 [0f, 1f] 范围内。
     * @param parallax 视差系数，0 表示子 View 跟随容器同步移动，1 表示最大视差偏移。
     */
    fun setParallax(parallax: Float) {
        var parallax = parallax
        parallax = min(1f, max(0f, parallax))
        this.parallax = parallax
    }

    /**
     * 获取当前展开方向。
     * @return [HORIZONTAL] (0) 或 [VERTICAL] (1)。
     */
    fun getOrientation(): Int {
        return orientation
    }

    /**
     * 动态设置展开方向。
     * @param orientation 必须为 0（水平）或 1（垂直），否则抛出 IllegalArgumentException。
     */
    fun setOrientation(orientation: Int) {
        require(orientation in 0..1) { "Orientation must be either 0 (horizontal) or 1 (vertical)" }
        this.orientation = orientation
    }

    /**
     * 获取当前展开/折叠状态。
     * @return 四种状态之一：COLLAPSED / COLLAPSING / EXPANDING / EXPANDED。
     */
    fun getState(): State {
        return state
    }

    /**
     * 注册展开进度监听器。
     * 动画过程中每帧都会回调，可用于联动其他 UI 元素（如箭头旋转、透明度渐变等）。
     * @param listener 监听器实例，传 null 可移除监听。
     */
    fun setOnExpansionUpdateListener(listener: OnExpansionUpdateListener) {
        this.listener = listener
    }

    /**
     * 判断当前是否处于“展开”语义下。
     * 注意：EXPANDING（动画进行中）也返回 true，适用于按钮文案切换等场景；
     * 若需精确判断终态，请使用 [getState] 并与 [State.EXPANDED] 比较。
     * @return true 表示已展开或正在展开。
     */
    fun isExpanded(): Boolean {
        return state == State.EXPANDING || state == State.EXPANDED
    }

    /**
     * 展开进度更新监听接口。
     */
    interface OnExpansionUpdateListener {
        /**
         * 每帧动画更新时回调。
         * @param expansionFraction 当前展开比例 [0f, 1f]。
         * @param state 当前状态。
         */
        fun onExpansionUpdate(expansionFraction: Float, state: State)
    }

    /**
     * 内部动画监听器，负责在动画开始/结束时正确更新 state，
     * 并在动画被取消时跳过终态赋值，防止状态与实际视觉不一致。
     */
    private inner class ExpansionListener(private val targetExpansion: Int) : Animator.AnimatorListener {
        private var canceled = false

        /**
         * 动画开始时立即更新为过渡态（EXPANDING / COLLAPSING）
         */
        override fun onAnimationStart(animation: Animator) {
            state = if (targetExpansion == 0) State.COLLAPSING else State.EXPANDING
        }

        /**
         * 动画正常结束时，强制将 state 和 expansion 设为目标终态，消除浮点精度误差
         */
        override fun onAnimationEnd(animation: Animator) {
            if (!canceled) {
                state = if (targetExpansion == 0) State.COLLAPSED else State.EXPANDED
                setExpansion(targetExpansion.toFloat())
            }
        }

        /**
         * 动画被取消时标记，阻止 onAnimationEnd 中的终态赋值
         */
        override fun onAnimationCancel(animation: Animator) {
            canceled = true
        }

        override fun onAnimationRepeat(animation: Animator) {
        }
    }

}