package com.example.common.widget.xrecyclerview.refresh

import android.content.Context
import android.util.AttributeSet
import com.example.base.utils.LogUtil
import com.example.base.utils.function.inflate
import com.example.base.utils.function.value.orFalse
import com.example.base.widget.BaseViewGroup
import com.example.common.R
import com.example.common.base.page.Paging
import com.example.common.databinding.ViewRefreshFooterBinding
import com.lcodecore.tkrefreshlayout.IBottomView

/**
 * author:wyb
 * 自定义刷新控件底部
 */
class FooterView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : BaseViewGroup(context, attrs, defStyleAttr), IBottomView {
    private val binding by lazy { ViewRefreshFooterBinding.bind(context.inflate(R.layout.view_refresh_footer)) }
    var paging: Paging? = null

    override fun onDrawView() {
        if (onFinishView()) addView(binding.root)
    }

    /**
     * 获取刷新的整体view
     */
    override fun getView() = binding.root

    /**
     * 正在上拉的过程
     */
    override fun onPullingUp(fraction: Float, maxBottomHeight: Float, bottomHeight: Float) {
        log("onPullingUp")
        if (paging?.hasNextPage().orFalse) {
            binding.tvMsg.text = "我也是有底线的~"
        } else {
            binding.tvMsg.text = "上拉加载更多"
        }
    }

    /**
     * 上拉释放过程
     */
    override fun onPullReleasing(fraction: Float, maxBottomHeight: Float, bottomHeight: Float) {
        log("onPullReleasing")
        if (paging?.hasNextPage().orFalse) {
            binding.tvMsg.text = "我也是有底线的~"
        } else {
            binding.tvMsg.text = "释放加载更多"
        }
    }

    /**
     * 触发执行动画时，文字和图片的样式
     */
    override fun startAnim(maxBottomHeight: Float, bottomHeight: Float) {
        log("startAnim")
        if (paging?.hasNextPage().orFalse) {
            binding.tvMsg.text = "我也是有底线的~"
            binding.progress.stopSpinning()
        } else {
            binding.tvMsg.text = "加载中"
            binding.progress.spin()
        }
    }

    /**
     * 动画执行完毕时，结束
     */
    override fun onFinish() {
        log("onFinish")
        binding.tvMsg.text = ""
        binding.progress.stopSpinning()
    }

    /**
     * 重置复位
     */
    override fun reset() {
        log("reset")
        binding.tvMsg.text = "上拉加载更多"
        binding.progress.stopSpinning()
    }

    private fun log(msg: String) = LogUtil.e("BottomView", msg)

}