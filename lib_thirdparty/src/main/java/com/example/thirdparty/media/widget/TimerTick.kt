package com.example.thirdparty.media.widget

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.graphics.Color
import android.os.Build
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import androidx.core.graphics.drawable.toDrawable
import androidx.lifecycle.LifecycleOwner
import com.example.common.utils.helper.ConfigHelper.appIsOnForeground
import com.example.framework.utils.builder.TimerBuilder
import com.example.framework.utils.function.doOnDestroy
import com.example.framework.utils.function.inflate
import com.example.framework.utils.function.value.formatAsCountdown
import com.example.framework.utils.function.value.orFalse
import com.example.framework.utils.function.value.second
import com.example.framework.utils.function.value.toSafeInt
import com.example.thirdparty.R
import com.example.thirdparty.databinding.ViewTimeTickBinding

/**
 * @author yan
 * @description 录屏小组件工具栏
 */
@SuppressLint("ClickableViewAccessibility")
class TimerTick(mContext: Context, private val observer: LifecycleOwner, move: Boolean = true) {
    private val timer by lazy { TimerBuilder(observer) }
    private val tickDialog by lazy { AlertDialog.Builder(mContext, R.style.AndDialogStyle).apply { setView(mBinding.root) }.create() }
    private val mBinding by lazy { ViewTimeTickBinding.bind(mContext.inflate(R.layout.view_time_tick)) }

    companion object {
        // 录制时的时间在应用回退到页面时赋值页面的时间
        @Volatile
        var timerSecond = 0

        // 默认计时器tag
        private const val TASK_DISPLAY_TICK_TAG = "TASK_DISPLAY_TICK"
    }

    /**
     * 初始化时调用，点击事件，弹框的初始化
     */
    init {
        observer.doOnDestroy {
            destroy()
        }
        //设置自定义的弹框
        tickDialog?.apply {
            window?.setType(if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY else WindowManager.LayoutParams.TYPE_SYSTEM_ALERT)
            window?.decorView?.setPadding(0, 0, 0, 0)
            window?.decorView?.background = Color.TRANSPARENT.toDrawable()
            setCancelable(false)
            mBinding.root.post {
                val params = window?.attributes
                params?.gravity = Gravity.TOP or Gravity.END
                params?.verticalMargin = 0f
                params?.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                params?.height = mBinding.root.measuredHeight
                window?.attributes = params
                // 透明
                window?.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
                // 配置移动，只支持上下
                if (move) {
                    params ?: return@post
                    mBinding.root.setOnTouchListener(object : View.OnTouchListener {
                        private var lastX = 0
                        private var lastY = 0
                        private var paramX = 0
                        private var paramY = 0

                        override fun onTouch(v: View?, event: MotionEvent?): Boolean {
                            when (event?.action) {
                                MotionEvent.ACTION_DOWN -> {
                                    lastX = event.rawX.toSafeInt()
                                    lastY = event.rawY.toSafeInt()
                                    paramX = params.x
                                    paramY = params.y
                                }

                                MotionEvent.ACTION_MOVE -> {
                                    val dx = event.rawX.toSafeInt() - lastX
                                    val dy = event.rawY.toSafeInt() - lastY
                                    params.x = paramX - dx
                                    params.y = paramY + dy
                                    window?.attributes = params
                                }
                            }
                            return true
                        }
                    })
                }
            }
        }
    }

    /**
     * 开启定时器计时按秒累加，毫秒级的操作不能被获取
     */
    fun start() {
        timerSecond = 0
        timer.startTask(TASK_DISPLAY_TICK_TAG, {
            timerSecond++
            // 每秒做一次检测，当程序退到后台显示计时器
            if (null != tickDialog) {
                if (!appIsOnForeground()) {
                    if (!tickDialog?.isShowing.orFalse) tickDialog?.show()
                } else {
                    tickDialog?.dismiss()
                }
            }
            mBinding.tvTimer.text = (timerSecond - 1).second.formatAsCountdown()
        })
    }

    /**
     * 挂载的服务销毁同时调用，结束计时器,弹框等
     */
    fun destroy() {
        timerSecond = 0
        tickDialog?.dismiss()
        mBinding.unbind()
    }

}