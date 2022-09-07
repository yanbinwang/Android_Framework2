package com.example.common.widget.xrecyclerview.refresh

import android.content.Context
import android.view.View
import com.example.base.utils.LogUtil
import com.example.common.R
import com.example.common.widget.ProgressWheel
import com.lcodecore.tkrefreshlayout.IHeaderView
import com.lcodecore.tkrefreshlayout.OnAnimEndListener


/**
 * @description 自定义头部
 * @author yan
 */
class HeaderView(var context: Context) : IHeaderView {
    private var progress: ProgressWheel? = null

    /**
     * 获取刷新的整体view
     */
    override fun getView(): View {
        val rootView = View.inflate(context, R.layout.view_refresh_header, null)
        progress = rootView.findViewById(R.id.progress)
        return rootView
    }

    /**
     * 正在下拉的过程
     */
    override fun onPullingDown(fraction: Float, maxHeadHeight: Float, headHeight: Float) {
        log("onPullingDown")
    }

    /**
     * 下拉释放过程
     */
    override fun onPullReleasing(fraction: Float, maxHeadHeight: Float, headHeight: Float) {
        log("onPullReleasing")
    }

    /**
     * 触发执行动画时，文字和图片的样式
     */
    override fun startAnim(maxHeadHeight: Float, headHeight: Float) {
        log("startAnim")
        progress?.spin()
    }

    /**
     * 动画执行完毕时，结束
     */
    override fun onFinish(animEndListener: OnAnimEndListener?) {
        log("onFinish")
        progress?.stopSpinning()
        animEndListener?.onAnimEnd()
    }

    /**
     * 重置复位
     */
    override fun reset() {
        log("reset")
    }

    private fun log(msg: String) = LogUtil.e("HeaderView", msg)

}