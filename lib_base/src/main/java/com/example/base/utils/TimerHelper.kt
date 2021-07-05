package com.example.base.utils

import android.os.CountDownTimer
import android.os.Looper
import java.util.*

/**
 *  Created by wangyanbin
 *  时间工具类
 *  默认1秒，分计数和倒计时
 *  一个页面计时和倒计时有且只有一个存在，多种可考虑使用rxjava自带
 */
object TimerHelper {
    private var timer: Timer? = null
    private var timerTask: TimerTask? = null
    private var downTimer: CountDownTimer? = null
    private val weakHandler by lazy { WeakHandler(Looper.getMainLooper()) }

    /**
     * 延时任务-容易造成内存泄漏
     */
    @JvmStatic
    fun schedule(onTaskListener: OnTaskListener? = null, millisecond: Long = 1000) {
        Timer().schedule(object : TimerTask() {
            override fun run() {
                weakHandler.post { onTaskListener?.run() }
            }
        }, millisecond)
    }

    /**
     * 计时-开始
     */
    @JvmStatic
    fun startTask(onTaskListener: OnTaskListener? = null, millisecond: Long = 1000) {
        if (timer == null) {
            timer = Timer()
            timerTask = object : TimerTask() {
                override fun run() {
                    weakHandler.post { onTaskListener?.run() }
                }
            }
            timer?.schedule(timerTask, 0, millisecond)
        }
    }

    /**
     * 计时-结束
     */
    @JvmStatic
    fun stopTask() {
        timerTask?.cancel()
        timer?.cancel()
        timerTask = null
        timer = null
    }

    /**
     * 倒计时-开始
     * second-秒
     */
    @JvmStatic
    fun startDownTask(onCountDownListener: OnCountDownListener? = null, second: Long = 1) {
        if (null == downTimer) {
            downTimer = object : CountDownTimer(second * 1000, 1000) {
                override fun onTick(millisUntilFinished: Long) {
                    onCountDownListener?.onTick((millisUntilFinished / 1000))
                }

                override fun onFinish() {
                    onCountDownListener?.onFinish()
                    stopDownTask()
                }
            }
        }
        downTimer?.start()
    }

    /**
     * 倒计时-结束
     */
    @JvmStatic
    fun stopDownTask() {
        downTimer?.cancel()
        downTimer = null
    }

    /**
     * 页面销毁时调取
     */
    @JvmStatic
    fun destroy() {
        stopTask()
        stopDownTask()
    }

    interface OnTaskListener {

        fun run()

    }

    interface OnCountDownListener {

        fun onTick(second: Long)//返回秒

        fun onFinish()

    }

}