package com.example.common.widget.xrecyclerview.refresh

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.AnimationDrawable
import android.util.AttributeSet
import android.view.View
import com.example.base.utils.function.inflate
import com.example.base.utils.function.view.gone
import com.example.base.utils.function.view.visible
import com.example.base.widget.BaseViewGroup
import com.example.common.R
import com.example.common.databinding.ViewRefreshFooterBinding
import com.scwang.smart.refresh.layout.api.RefreshFooter
import com.scwang.smart.refresh.layout.api.RefreshKernel
import com.scwang.smart.refresh.layout.api.RefreshLayout
import com.scwang.smart.refresh.layout.constant.RefreshState
import com.scwang.smart.refresh.layout.constant.SpinnerStyle

/**
 * author:wyb
 * 自定义刷新控件底部
 */
@SuppressLint("RestrictedApi")
class ProjectRefreshFooter @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : BaseViewGroup(context, attrs, defStyleAttr), RefreshFooter {
    private var noMoreData = false
    private var animation: AnimationDrawable? = null
    private val binding by lazy { ViewRefreshFooterBinding.bind(context.inflate(R.layout.view_refresh_footer)) }

    init {
        binding.ivProgress.let {
            it.setImageResource(R.drawable.layer_list_refresh)
            animation = it.drawable as? AnimationDrawable
        }
        setNoMoreData(noMoreData)
    }

    override fun onDrawView() {
        if (onFinishView()) addView(binding.root)
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

    override fun onMoving(isDragging: Boolean, percent: Float, offset: Int, height: Int, maxDragHeight: Int) {
        if (!isDragging) return
        if (noMoreData) {
            animation?.stop()
            binding.tvMsg.visible()
            binding.ivProgress.gone()
            return
        }
        animation?.start()
    }

    override fun onReleased(refreshLayout: RefreshLayout, height: Int, maxDragHeight: Int) {
    }

    override fun onStartAnimator(refreshLayout: RefreshLayout, height: Int, maxDragHeight: Int) {
    }

    override fun onFinish(refreshLayout: RefreshLayout, success: Boolean): Int {
        animation?.stop()
        return 0
    }

    override fun onHorizontalDrag(percentX: Float, offsetX: Int, offsetMax: Int) {
    }

    override fun isSupportHorizontalDrag(): Boolean {
        return false
    }

    override fun setNoMoreData(noMoreData: Boolean): Boolean {
        this.noMoreData = noMoreData
        if (noMoreData) {
            animation?.stop()
            binding.tvMsg.visible()
            binding.ivProgress.gone()
        } else {
            binding.tvMsg.gone()
            binding.ivProgress.visible()
        }
        return true
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        animation?.stop()
    }

//    /**
//     * 获取刷新的整体view
//     */
//    override fun getView() = binding.root
//
//    /**
//     * 正在上拉的过程
//     */
//    override fun onPullingUp(fraction: Float, maxBottomHeight: Float, bottomHeight: Float) {
//        "onPullingUp".logE(TAG)
//        if (paging?.hasNextPage().orFalse) {
//            binding.tvMsg.text = "我也是有底线的~"
//        } else {
//            binding.tvMsg.text = "上拉加载更多"
//        }
//    }
//
//    /**
//     * 上拉释放过程
//     */
//    override fun onPullReleasing(fraction: Float, maxBottomHeight: Float, bottomHeight: Float) {
//        "onPullReleasing".logE(TAG)
//        if (paging?.hasNextPage().orFalse) {
//            binding.tvMsg.text = "我也是有底线的~"
//        } else {
//            binding.tvMsg.text = "释放加载更多"
//        }
//    }
//
//    /**
//     * 触发执行动画时，文字和图片的样式
//     */
//    override fun startAnim(maxBottomHeight: Float, bottomHeight: Float) {
//        "startAnim".logE(TAG)
//        if (paging?.hasNextPage().orFalse) {
//            binding.tvMsg.text = "我也是有底线的~"
//            binding.progress.stopSpinning()
//        } else {
//            binding.tvMsg.text = "加载中"
//            binding.progress.spin()
//        }
//    }
//
//    /**
//     * 动画执行完毕时，结束
//     */
//    override fun onFinish() {
//        "onFinish".logE(TAG)
//        binding.tvMsg.text = ""
//        binding.progress.stopSpinning()
//    }
//
//    /**
//     * 重置复位
//     */
//    override fun reset() {
//        "reset".logE(TAG)
//        binding.tvMsg.text = "上拉加载更多"
//        binding.progress.stopSpinning()
//    }

}