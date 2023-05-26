package com.example.multimedia.utils.helper

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
import com.example.common.utils.helper.ConfigHelper.isAppOnForeground
import com.example.framework.utils.WeakHandler
import com.example.framework.utils.function.inflate
import com.example.framework.utils.function.value.orFalse
import com.example.framework.utils.function.value.timer
import com.example.framework.utils.function.value.toSafeInt
import com.example.multimedia.R
import com.example.multimedia.databinding.ViewTimeTickBinding
import java.util.Timer
import java.util.TimerTask

/**
 * @author yan
 * @description 录屏小组件工具栏
 */
class TimeTickHelper(context: Context, move: Boolean = true) {
    private val binding by lazy { ViewTimeTickBinding.bind(context.inflate(R.layout.view_time_tick)) }
    private var timer: Timer? = null
    private var timerTask: TimerTask? = null
    private var tickDialog: AlertDialog? = null
    private val weakHandler by lazy { WeakHandler(Looper.getMainLooper()) }

    companion object {
        @Volatile
        var timerCount: Long = 0//录制时的时间在应用回退到页面时赋值页面的时间
    }

    /**
     * 初始化时调用，点击事件，弹框的初始化
     */
    init {
        if (null == tickDialog) {
            //设置一个自定义的弹框
            val builder = AlertDialog.Builder(context, R.style.AndDialogStyle)
            builder.setView(binding.root)
            tickDialog = builder.create()
            tickDialog?.apply {
                window?.setType(if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY else WindowManager.LayoutParams.TYPE_SYSTEM_ALERT)
                window?.decorView?.setPadding(0, 0, 0, 0)
                window?.decorView?.background = ColorDrawable(Color.TRANSPARENT)
                setCancelable(false)
                binding.root.post {
                    val params = window?.attributes
                    params?.gravity = Gravity.TOP or Gravity.END
                    params?.verticalMargin = 0f
                    params?.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                    params?.height = binding.root.measuredHeight
                    window?.attributes = params
                    window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))//透明
                    //配置移动，只支持上下
                    if (null != params && move) {
                        binding.root.setOnTouchListener(object : View.OnTouchListener {
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
    fun onStart() {
        timerCount = 0
        if (timer == null) {
            timer = Timer()
            timerTask = object : TimerTask() {
                override fun run() {
                    weakHandler.post {
                        timerCount++
                        //每秒做一次检测，当程序退到后台显示计时器
                        if (null != tickDialog) {
                            if (!isAppOnForeground()) {
                                if (!tickDialog?.isShowing.orFalse) tickDialog?.show()
                            } else {
                                tickDialog?.dismiss()
                            }
                        }
                        binding.tvTimer.text = (timerCount - 1).timer()
                    }
                }
            }
            timer?.schedule(timerTask, 0, 1000)
        }
    }

    /**
     * 挂载的服务销毁同时调用，结束计时器,弹框等
     */
    fun onDestroy() {
        timerCount = 0
        timerTask?.cancel()
        timer?.cancel()
        timerTask = null
        timer = null
        tickDialog?.dismiss()
        tickDialog = null
    }

}