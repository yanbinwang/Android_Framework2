package com.example.common.widget.xrecyclerview.refresh

import android.content.Context
import android.util.AttributeSet
import com.example.base.utils.function.inflate
import com.example.base.utils.logE
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
    private val TAG = "HeaderView"
    private val binding by lazy { ViewRefreshHeaderBinding.bind(context.inflate(R.layout.view_refresh_header)) }

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
        "onPullingDown${Thread.currentThread().name}".logE(TAG)
    }

    /**
     * 下拉释放过程
     */
    override fun onPullReleasing(fraction: Float, maxHeadHeight: Float, headHeight: Float) {
        "onPullReleasing${Thread.currentThread().name}".logE(TAG)
    }

    /**
     * 触发执行动画时，文字和图片的样式
     */
    override fun startAnim(maxHeadHeight: Float, headHeight: Float) {
        "startAnim${Thread.currentThread().name}".logE(TAG)
        binding.progress.spin()
    }

    /**
     * 动画执行完毕时，结束
     */
    override fun onFinish(animEndListener: OnAnimEndListener?) {
        "onFinish${Thread.currentThread().name}".logE(TAG)
        binding.progress.stopSpinning()
        animEndListener?.onAnimEnd()
    }

    /**
     * 重置复位
     */
    override fun reset() {
        "reset${Thread.currentThread().name}".logE(TAG)
    }

}