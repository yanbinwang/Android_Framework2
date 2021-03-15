package com.example.common.utils.helper

import android.os.CountDownTimer
import android.os.Looper
import com.example.common.utils.handler.WeakHandler
import java.util.*

/**
 *  Created by wangyanbin
 *  时间工具类
 *  默认1秒，分计数和倒计时
 */
object TimeTaskHelper {
    private var timer: Timer? = null
    private var timerTask: TimerTask? = null
    private var countDownTimer: CountDownTimer? = null
    private val weakHandler by lazy { WeakHandler(Looper.getMainLooper()) }

    /**
     * 计时-开始
     */
    @JvmStatic
    fun startTask(millisecond: Long = 1000, onCountDownListener: OnCountUpListener?) {
        if (timer == null) {
            timer = Timer()
            timerTask = object : TimerTask() {
                override fun run() {
                    weakHandler.post { onCountDownListener?.run() }
                }
            }
            timer?.schedule(timerTask, millisecond, millisecond) //1s后执行timer，之后每隔1s执行一次
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
    fun startCountDown(second: Long, onCountDownListener: OnCountDownListener?) {
        if (null == countDownTimer) {
            countDownTimer = object : CountDownTimer(second * 1000, 1000) {
                override fun onTick(millisUntilFinished: Long) {
                    onCountDownListener?.onTick(millisUntilFinished / 1000)
                }

                override fun onFinish() {
                    onCountDownListener?.onFinish()
                }
            }
        }
        countDownTimer?.start()
    }

    /**
     * 倒计时-结束
     */
    @JvmStatic
    fun stopCountDown() {
        countDownTimer?.cancel()
        countDownTimer = null
    }

    /**
     * 页面销毁时调取
     */
    @JvmStatic
    fun destroy() {
        stopTask()
        stopCountDown()
    }

    interface OnCountUpListener {

        fun run()

    }

    interface OnCountDownListener {

        fun onTick(second: Long)//返回秒

        fun onFinish()

    }

}