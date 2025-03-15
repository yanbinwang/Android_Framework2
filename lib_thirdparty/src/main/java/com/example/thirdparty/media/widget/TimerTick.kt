package com.example.thirdparty.media.widget

import android.app.AlertDialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Looper
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import androidx.lifecycle.Lifecycle
import com.example.common.utils.helper.ConfigHelper.appIsOnForeground
import com.example.framework.utils.WeakHandler
import com.example.framework.utils.function.doOnDestroy
import com.example.framework.utils.function.inflate
import com.example.framework.utils.function.value.orFalse
import com.example.framework.utils.function.value.second
import com.example.framework.utils.function.value.timeCountDown
import com.example.framework.utils.function.value.toSafeInt
import com.example.thirdparty.R
import com.example.thirdparty.databinding.ViewTimeTickBinding
import java.util.Timer
import java.util.TimerTask
import androidx.core.graphics.drawable.toDrawable

/**
 * @author yan
 * @description 录屏小组件工具栏
 */
class TimerTick(mContext: Context, move: Boolean = true) {
    private var timer: Timer? = null
    private var timerTask: TimerTask? = null
    private var tickDialog: AlertDialog? = null
    private val weakHandler by lazy { WeakHandler(Looper.getMainLooper()) }
    private val mBinding by lazy { ViewTimeTickBinding.bind(mContext.inflate(R.layout.view_time_tick)) }

    companion object {
        @Volatile
        var timerSecond = 0//录制时的时间在应用回退到页面时赋值页面的时间
    }

    /**
     * 初始化时调用，点击事件，弹框的初始化
     */
    init {
        if (null == tickDialog) {
            //设置一个自定义的弹框
            val builder = AlertDialog.Builder(mContext, R.style.AndDialogStyle)
            builder.setView(mBinding.root)
            tickDialog = builder.create()
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
                    window?.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())//透明
                    //配置移动，只支持上下
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
    }

    /**
     * 开启定时器计时按秒累加，毫秒级的操作不能被获取
     */
    fun start(observer: Lifecycle) {
        timerSecond = 0
        if (timer == null) {
            timer = Timer()
            timerTask = object : TimerTask() {
                override fun run() {
                    weakHandler.post {
                        timerSecond++
                        //每秒做一次检测，当程序退到后台显示计时器
                        if (null != tickDialog) {
                            if (!appIsOnForeground()) {
                                if (!tickDialog?.isShowing.orFalse) tickDialog?.show()
                            } else {
                                tickDialog?.dismiss()
                            }
                        }
                        mBinding.tvTimer.text = (timerSecond - 1).second.timeCountDown()
                    }
                }
            }
            timer?.schedule(timerTask, 0, 1000)
        }
        observer.doOnDestroy { destroy() }
    }

    /**
     * 挂载的服务销毁同时调用，结束计时器,弹框等
     */
    fun destroy() {
        timerSecond = 0
        timerTask?.cancel()
        timer?.cancel()
        timerTask = null
        timer = null
        tickDialog?.dismiss()
        tickDialog = null
    }

}