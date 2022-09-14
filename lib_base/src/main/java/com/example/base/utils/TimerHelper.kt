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
    private var countDownTimer: CountDownTimer? = null

    /**
     * 延时任务-容易造成内存泄漏
     */
    @JvmOverloads
    @JvmStatic
    fun schedule(run: (() -> Unit)?, millisecond: Long = 1000) {
        Timer().schedule(object : TimerTask() {
            override fun run() {
                WeakHandler(Looper.getMainLooper()).post { run?.invoke() }
            }
        }, millisecond)
    }

    /**
     * 计时(累加)-开始
     */
    @JvmOverloads
    @JvmStatic
    fun startTask(run: (() -> Unit)?, millisecond: Long = 1000) {
        stopTask()
        timer = Timer()
        timerTask = object : TimerTask() {
            override fun run() {
                WeakHandler(Looper.getMainLooper()).post { run?.invoke() }
            }
        }
        timer?.schedule(timerTask, 0, millisecond)
    }

    /**
     * 计时（累加）-结束
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
    @JvmOverloads
    @JvmStatic
    fun startDownTask(onTick: ((second: Long) -> Unit)?, onFinish: (() -> Unit)?, second: Long = 1) {
        stopDownTask()
        countDownTimer = object : CountDownTimer(second * 1000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                onTick?.invoke((millisUntilFinished / 1000))
            }

            override fun onFinish() {
                onFinish?.invoke()
                stopDownTask()
            }
        }.start()
    }

    /**
     * 倒计时-结束
     */
    @JvmStatic
    fun stopDownTask() {
        countDownTimer?.cancel()
        countDownTimer = null
    }

    /**
     * 页面销毁时调取-如同时使用了俩种计时方法
     */
    @JvmStatic
    fun destroy() {
        stopTask()
        stopDownTask()
    }

}