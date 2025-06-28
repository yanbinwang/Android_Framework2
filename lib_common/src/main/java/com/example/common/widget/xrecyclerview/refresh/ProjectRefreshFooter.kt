package com.example.common.widget.xrecyclerview.refresh

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.AnimationDrawable
import android.util.AttributeSet
import android.view.View
import androidx.annotation.ColorRes
import com.example.common.R
import com.example.common.databinding.ViewRefreshFooterBinding
import com.example.framework.utils.function.inflate
import com.example.framework.utils.function.view.gone
import com.example.framework.utils.function.view.setResource
import com.example.framework.utils.function.view.tint
import com.example.framework.utils.function.view.visible
import com.example.framework.widget.BaseViewGroup
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
    private val mBinding by lazy { ViewRefreshFooterBinding.bind(context.inflate(R.layout.view_refresh_footer)) }
    internal var onDragListener: ((isDragging: Boolean, percent: Float, offset: Int, height: Int, maxDragHeight: Int) -> Unit)? = null

    init {
//        mBinding.root.size(MATCH_PARENT, 40.pt)
        mBinding.ivProgress.let {
            it.setResource(R.drawable.animation_list_loadmore)
            it.tint(R.color.appTheme)
            animation = it.drawable as? AnimationDrawable
        }
        setNoMoreData(noMoreData)
    }

    override fun onInflate() {
        if (isInflate) addView(mBinding.root)
    }

    override fun onStateChanged(refreshLayout: RefreshLayout, oldState: RefreshState, newState: RefreshState) {
    }

    override fun getView(): View {
        return mBinding.root
    }

    override fun getSpinnerStyle(): SpinnerStyle {
        return SpinnerStyle.Translate
    }

    override fun setPrimaryColors(vararg colors: Int) {
    }

    override fun onInitialized(kernel: RefreshKernel, height: Int, maxDragHeight: Int) {
    }

    override fun onMoving(isDragging: Boolean, percent: Float, offset: Int, height: Int, maxDragHeight: Int) {
        onDragListener?.invoke(isDragging, percent, offset, height, maxDragHeight)
        if (!isDragging) return
        if (noMoreData) {
            animation?.stop()
            mBinding.tvMsg.visible()
            mBinding.ivProgress.gone()
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

    override fun autoOpen(duration: Int, dragRate: Float, animationOnly: Boolean): Boolean {
        return true
    }

    override fun setNoMoreData(noMoreData: Boolean): Boolean {
        this.noMoreData = noMoreData
        if (noMoreData) {
            animation?.stop()
            mBinding.tvMsg.visible()
            mBinding.ivProgress.gone()
        } else {
            mBinding.tvMsg.gone()
            mBinding.ivProgress.visible()
        }
        return true
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        animation?.stop()
    }

    /**
     * 转圈颜色
     */
    fun setProgressTint(@ColorRes color: Int) {
        mBinding.ivProgress.tint(color)
    }

}