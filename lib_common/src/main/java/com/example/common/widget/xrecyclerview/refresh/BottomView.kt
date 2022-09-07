package com.example.common.widget.xrecyclerview.refresh

import android.content.Context
import android.os.Looper
import android.view.View
import android.widget.TextView
import com.example.base.utils.LogUtil
import com.example.base.utils.WeakHandler
import com.example.common.R
import com.example.common.widget.ProgressWheel
import com.lcodecore.tkrefreshlayout.IBottomView

/**
 * author:wyb
 * 自定义刷新控件底部
 */
class BottomView(var context: Context) : IBottomView {
    private var progress: ProgressWheel? = null
    private var tvMsg: TextView? = null
    private val weakHandler by lazy { WeakHandler(Looper.getMainLooper()) }

    /**
     * 获取刷新的整体view
     */
    override fun getView(): View {
        val rootView = View.inflate(context, R.layout.view_refresh_bottom, null)
        progress = rootView.findViewById(R.id.progress)
        tvMsg = rootView.findViewById(R.id.tv_msg)
        return rootView
    }

    /**
     * 当控件上拉时触发
     */
    override fun onPullingUp(fraction: Float, maxBottomHeight: Float, bottomHeight: Float) {
        log("onPullingUp")
        weakHandler.post { tvMsg?.text = "上拉加载更多" }
    }

    /**
     * 刷新被复原时--复位
     */
    override fun onPullReleasing(fraction: Float, maxBottomHeight: Float, bottomHeight: Float) {
        log("onPullReleasing")
        weakHandler.post { tvMsg?.text = "释放加载更多" }
    }

    /**
     * 触发执行动画时，文字和图片的样式
     */
    override fun startAnim(maxBottomHeight: Float, bottomHeight: Float) {
        log("startAnim")
        weakHandler.post {
            tvMsg?.text = "加载中"
            progress?.spin()
        }
    }

    /**
     * 动画执行完毕时，结束
     */
    override fun onFinish() {
        log("onFinish")
        weakHandler.post {
            tvMsg?.text = ""
            progress?.stopSpinning()
        }
    }

    /**
     * 重置复位
     */
    override fun reset() {
        log("reset")
    }

    private fun log(msg: String) = LogUtil.e("BottomView", msg)

}