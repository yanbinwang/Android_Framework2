package com.example.common.widget.xrecyclerview.refresh

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.AnimationDrawable
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import androidx.annotation.ColorRes
import com.example.common.R
import com.example.common.databinding.ViewRefreshHeaderBinding
import com.example.framework.utils.function.doOnDestroy
import com.example.framework.utils.function.inflate
import com.example.framework.utils.function.value.orFalse
import com.example.framework.utils.function.value.toSafeInt
import com.example.framework.utils.function.view.doOnceAfterLayout
import com.example.framework.utils.function.view.getLifecycleOwner
import com.example.framework.utils.function.view.setResource
import com.example.framework.utils.function.view.size
import com.example.framework.utils.function.view.tint
import com.example.framework.widget.BaseViewGroup
import com.scwang.smart.refresh.layout.api.RefreshHeader
import com.scwang.smart.refresh.layout.api.RefreshKernel
import com.scwang.smart.refresh.layout.api.RefreshLayout
import com.scwang.smart.refresh.layout.constant.RefreshState
import com.scwang.smart.refresh.layout.constant.SpinnerStyle

/**
 * @description 自定义头部
 * @author yan
 * https://www.gaitubao.com/xuanzhuan/
 * 默认情况下采用逐帧可以控制动画的开始和停止展现上更好，
 * 如果ui不提供对应图片，手机端去对应网站45°生成8张旋转逐帧图
 */
@SuppressLint("RestrictedApi")
class ProjectRefreshHeader @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : BaseViewGroup(context, attrs, defStyleAttr), RefreshHeader {
    private var animation: AnimationDrawable? = null
    private val binding by lazy { ViewRefreshHeaderBinding.bind(context.inflate(R.layout.view_refresh_header)) }
    internal var onDragListener: ((isDragging: Boolean, percent: Float, offset: Int, height: Int, maxDragHeight: Int) -> Unit)? = null

    init {
        binding.root.doOnceAfterLayout {
            it.getLifecycleOwner().doOnDestroy {
                release()
            }
        }
        binding.ivProgress.let {
            it.setResource(R.drawable.animation_list_loading)
            it.tint(R.color.appTheme)
            animation = it.drawable as? AnimationDrawable
        }
    }

    /**
     * 1) 页面关闭（Activity finish / Fragment destroy）
     * 2) 代码主动 remove（smartRefreshLayout.removeView(header) 或父容器被移除
     */
    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        animation?.stop()
    }

    override fun onInflate() {
        if (shouldInflate) addView(binding.root)
    }

    /**
     * 状态改变事件 RefreshState
     * @param refreshLayout RefreshLayout
     * @param oldState 改变之前的状态
     * @param newState 改变之后的状态
     */
    override fun onStateChanged(refreshLayout: RefreshLayout, oldState: RefreshState, newState: RefreshState) {
    }

    /**
     * 获取实体视图
     * @Returns: 实体视图
     */
    override fun getView(): View {
        return binding.root
    }

    /**
     * 获取变换方式 SpinnerStyle 必须返回 非空
     * @Returns: 变换方式
     */
    override fun getSpinnerStyle(): SpinnerStyle {
        return SpinnerStyle.Translate
    }

    /**
     * 设置主题颜色
     * @param colors 对应Xml中配置的 srlPrimaryColor srlAccentColor
     */
    override fun setPrimaryColors(vararg colors: Int) {
    }

    /**
     * 尺寸定义完成 （如果高度不改变（代码修改：setHeader），只调用一次, 在RefreshLayout#onMeasure中调用）
     * @param kernel RefreshKernel
     * @param height HeaderHeight or FooterHeight
     * @param maxDragHeight 最大拖动高度
     */
    override fun onInitialized(kernel: RefreshKernel, height: Int, maxDragHeight: Int) {
    }

    /**
     * 手指拖动下拉（会连续多次调用，添加isDragging并取代之前的onPulling、onReleasing）
     * @param isDragging true 手指正在拖动 false 回弹动画
     * @param percent 下拉的百分比 值 = offset/footerHeight (0 - percent - (footerHeight+maxDragHeight) / footerHeight )
     * @param offset 下拉的像素偏移量  0 - offset - (footerHeight+maxDragHeight)
     * @param height 高度 HeaderHeight or FooterHeight (offset 可以超过 height 此时 percent 大于 1)
     * @param maxDragHeight 最大拖动高度 offset 可以超过 height 参数 但是不会超过 maxDragHeight
     */
    override fun onMoving(isDragging: Boolean, percent: Float, offset: Int, height: Int, maxDragHeight: Int) {
        onDragListener?.invoke(isDragging, percent, offset, height, maxDragHeight)
//        if (isDragging) animation?.start()
    }

    /**
     * 释放时刻（调用一次，将会触发加载）
     * @param refreshLayout RefreshLayout
     * @param height 高度 HeaderHeight or FooterHeight
     * @param maxDragHeight 最大拖动高度
     */
    override fun onReleased(refreshLayout: RefreshLayout, height: Int, maxDragHeight: Int) {
        // 松开时才开始做动画
        if (animation?.isRunning.orFalse) return
        animation?.selectDrawable(0)
        animation?.start()
    }

    /**
     * 开始动画
     * @param refreshLayout RefreshLayout
     * @param height HeaderHeight or FooterHeight
     * @param maxDragHeight 最大拖动高度
     */
    override fun onStartAnimator(refreshLayout: RefreshLayout, height: Int, maxDragHeight: Int) {
    }

    /**
     * 动画结束
     * @param refreshLayout RefreshLayout
     * @param success 数据是否成功刷新或加载
     * @return 完成动画所需时间 如果返回 Integer.MAX_VALUE 将取消本次完成事件，继续保持原有状态
     */
    override fun onFinish(refreshLayout: RefreshLayout, success: Boolean): Int {
        animation?.stop()
        animation?.selectDrawable(0)
        return 0
    }

    /**
     * 水平方向的拖动
     * @param percentX 下拉时，手指水平坐标对屏幕的占比（0 - percentX - 1）
     * @param offsetX 下拉时，手指水平坐标对屏幕的偏移（0 - offsetX - LayoutWidth）
     * @param offsetMax 最大的偏移量
     */
    override fun onHorizontalDrag(percentX: Float, offsetX: Int, offsetMax: Int) {
    }

    /**
     * 是否支持水平方向的拖动（将会影响到onHorizontalDrag的调用）
     * @Returns: 水平拖动需要消耗更多的时间和资源，所以如果不支持请返回false
     */
    override fun isSupportHorizontalDrag(): Boolean {
        return false
    }

    /**
     * 显示刷新动画并且触发刷新事件
     * @param duration 拖拽动画持续时间
     * @param dragRate 拉拽的高度比率
     * @param animationOnly 只有动画
     * @Returns: 返回 False 代表本Header不支持自动刷新
     */
    override fun autoOpen(duration: Int, dragRate: Float, animationOnly: Boolean): Boolean {
        return false
    }

    /**
     * 转圈颜色
     */
    fun setProgressTint(@ColorRes color: Int) {
        binding.ivProgress.tint(color)
    }

    /**
     * 应用状态栏占位，重新计算并设置 Header 的总高度
     * @param statusBarHeight 状态栏实际像素高度
     * @param headerHeight 设计稿定义的纯刷新头部高度（px）
     * @param dragScaleFactor 下拉放大倍率，默认 2.5
     * 算式 : 40.pt (顶部高度) * 2.5 = 100.pt
     * binding.root.size(MATCH_PARENT, 100.pt + statusBarHeight) -> 不传 headerHeight 的写法
     */
    fun applyStatusBarInset(statusBarHeight: Int, headerHeight: Int, dragScaleFactor: Float) {
        val scaledHeaderHeight = (headerHeight * dragScaleFactor).toSafeInt()
        val totalHeight = scaledHeaderHeight + statusBarHeight
        binding.root.size(MATCH_PARENT, totalHeight)
    }

    /**
     * 显式销毁，供外部在确定不再使用时调用
     */
    fun release() {
        animation?.stop()
        binding.ivProgress.setImageDrawable(null)
        animation = null
    }

}