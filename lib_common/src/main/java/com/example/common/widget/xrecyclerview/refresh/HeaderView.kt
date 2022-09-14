package com.example.common.widget.xrecyclerview.refresh

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.databinding.DataBindingUtil
import com.example.base.utils.LogUtil
import com.example.base.widget.BaseViewGroup
import com.example.common.R
import com.example.common.databinding.ViewRefreshHeaderBinding
import com.lcodecore.tkrefreshlayout.IHeaderView
import com.lcodecore.tkrefreshlayout.OnAnimEndListener

/**
 * @description 自定义头部
 * @author yan
 */
class HeaderView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : BaseViewGroup(context, attrs, defStyleAttr), IHeaderView {
    private val binding by lazy<ViewRefreshHeaderBinding> { DataBindingUtil.bind(LayoutInflater.from(context).inflate(R.layout.view_refresh_header, null))!! }

    override fun onDrawView() {
        if (onFinishView()) addView(binding.root)
    }

    /**
     * 获取刷新的整体view
     */
    override fun getView() = binding.root

    /**
     * 正在下拉的过程
     */
    override fun onPullingDown(fraction: Float, maxHeadHeight: Float, headHeight: Float) {
        log("onPullingDown${Thread.currentThread().name}")
    }

    /**
     * 下拉释放过程
     */
    override fun onPullReleasing(fraction: Float, maxHeadHeight: Float, headHeight: Float) {
        log("onPullReleasing${Thread.currentThread().name}")
    }

    /**
     * 触发执行动画时，文字和图片的样式
     */
    override fun startAnim(maxHeadHeight: Float, headHeight: Float) {
        log("startAnim${Thread.currentThread().name}")
        binding.progress.spin()
    }

    /**
     * 动画执行完毕时，结束
     */
    override fun onFinish(animEndListener: OnAnimEndListener?) {
        log("onFinish${Thread.currentThread().name}")
        binding.progress.stopSpinning()
        animEndListener?.onAnimEnd()
    }

    /**
     * 重置复位
     */
    override fun reset() {
        log("reset${Thread.currentThread().name}")
    }

    private fun log(msg: String) = LogUtil.e("HeaderView", msg)

}