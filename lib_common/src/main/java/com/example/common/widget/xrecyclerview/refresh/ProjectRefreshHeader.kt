package com.example.common.widget.xrecyclerview.refresh

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.AnimationDrawable
import android.util.AttributeSet
import android.view.View
import com.example.base.utils.function.inflate
import com.example.base.utils.function.view.padding
import com.example.base.utils.function.view.size
import com.example.base.utils.function.view.tint
import com.example.base.widget.BaseViewGroup
import com.example.common.R
import com.example.common.constant.Constants
import com.example.common.databinding.ViewRefreshHeaderBinding
import com.example.common.utils.function.imageResource
import com.example.common.utils.function.pt
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

    init {
        binding.root.size(LayoutParams.MATCH_PARENT, 40.pt)
        binding.ivProgress.let {
            it.padding(top = 2.5.pt, bottom = 2.5.pt)
            it.imageResource(R.drawable.animation_list_loading)
            it.tint(R.color.blue_3d81f2)
            animation = it.drawable as? AnimationDrawable
        }
    }

    fun setStatusBarPadding() {
        binding.root.apply {
            size(LayoutParams.MATCH_PARENT, 40.pt + Constants.STATUS_BAR_HEIGHT)
            padding(top = Constants.STATUS_BAR_HEIGHT)
        }
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
//        if (isDragging) {
//            animation?.start()
//        }
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