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
import com.example.common.utils.function.pt
import com.example.framework.utils.function.inflate
import com.example.framework.utils.function.view.setResource
import com.example.framework.utils.function.view.padding
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
    private val binding by lazy { ViewRefreshHeaderBinding.bind(context.inflate(R.layout.view_refresh_header, this, false)) }
    internal var onDragListener: ((isDragging: Boolean, percent: Float, offset: Int, height: Int, maxDragHeight: Int) -> Unit)? = null

    init {
        binding.root.size(MATCH_PARENT, 40.pt)
        binding.ivProgress.let {
            it.setResource(R.drawable.animation_list_loading)
            it.tint(R.color.appTheme)
            animation = it.drawable as? AnimationDrawable
        }
    }

    /**
     * 顶部如果直接是刷新，需要让刷新的头在状态栏下方展示并刷新，故而调用此代码重新设置一下顶部的高度和padding
     */
    fun setStatusBarSpacing(statusBarHeight: Int) {
        binding.root.apply {
            size(LayoutParams.MATCH_PARENT, 80.pt + statusBarHeight)
            padding(top = statusBarHeight)
        }
    }

    fun setProgressTint(@ColorRes color: Int) {
        binding.ivProgress.tint(color)
    }

    override fun onInflateView() {
        if (isInflate()) addView(binding.root)
    }

    override fun onStateChanged(refreshLayout: RefreshLayout, oldState: RefreshState, newState: RefreshState) {
    }

    override fun getView(): View {
        return binding.root
    }

    override fun getSpinnerStyle(): SpinnerStyle {
        return SpinnerStyle.Translate
    }

    override fun setPrimaryColors(vararg colors: Int) {
    }

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
        animation?.start()//松开时才开始做动画
    }

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
        return 0
    }

    override fun onHorizontalDrag(percentX: Float, offsetX: Int, offsetMax: Int) {
    }

    override fun isSupportHorizontalDrag(): Boolean {
        return false
    }

}