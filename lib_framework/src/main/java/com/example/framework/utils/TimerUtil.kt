package com.example.framework.utils

import android.os.CountDownTimer
import android.os.Looper
import java.util.*
import java.util.concurrent.ConcurrentHashMap

/**
 *  Created by wangyanbin
 *  定时器工具类
 */
object TimerUtil {
    private val weakHandler by lazy { WeakHandler(Looper.getMainLooper()) }
    private val timerMap by lazy { ConcurrentHashMap<String, Pair<Timer?, TimerTask?>>() }
    private val countDownMap by lazy { ConcurrentHashMap<String, CountDownTimer?>() }

    /**
     * 延时任务-容易造成内存泄漏
     */
    @JvmOverloads
    @JvmStatic
    fun schedule(run: (() -> Unit)?, millisecond: Long = 1000) {
        weakHandler.postDelayed({
            run?.invoke()
        }, millisecond)
    }

    /**
     * 计时(累加)-开始
     */
    @JvmOverloads
    @JvmStatic
    fun startTask(tag: String = "TIMER_DEFULT", run: (() -> Unit)?, millisecond: Long = 1000) {
        if (timerMap[tag] == null) {
            val timer = Timer()
            val timerTask = object : TimerTask() {
                override fun run() {
                    weakHandler.post { run?.invoke() }
                }
            }
            timerMap[tag] = timer to timerTask
        }
        timerMap[tag]?.apply { first?.schedule(second, 0, millisecond) }
    }

    /**
     * 计时（累加）-结束
     */
    @JvmStatic
    fun stopTask(tag: String = "TIMER_DEFULT") {
        timerMap[tag]?.apply {
            first?.cancel()
            second?.cancel()
        }
        timerMap.remove(tag)
    }

    @JvmStatic
    fun stopTask(vararg tags: String) {
        tags.forEach { stopTask(it) }
    }

    /**
     * 倒计时-开始
     * second-秒
     */
    @JvmStatic
    fun startCountDown(tag: String = "COUNT_DOWN_DEFULT", onTick: ((second: Long) -> Unit)?, onFinish: (() -> Unit)?, second: Long = 1) {
        if (countDownMap[tag] == null) {
            val countDownTimer = object : CountDownTimer(second * 1000, 1000) {
                override fun onTick(millisUntilFinished: Long) {
                    onTick?.invoke((millisUntilFinished / 1000))
                }

                override fun onFinish() {
                    onFinish?.invoke()
                    stopCountDown(tag)
                }
            }
            countDownMap[tag] = countDownTimer
        }
        countDownMap[tag]?.start()
    }

    /**
     * 倒计时-结束
     */
    @JvmStatic
    fun stopCountDown(tag: String = "COUNT_DOWN_DEFULT") {
        countDownMap[tag]?.cancel()
        countDownMap.remove(tag)
    }

    @JvmStatic
    fun stopCountDown(vararg tags: String) {
        tags.forEach { stopCountDown(it) }
    }

    /**
     * 完全销毁所有定时器
     */
    @JvmStatic
    fun destroy() {
        for ((_, value) in timerMap) {
            value.apply {
                first?.cancel()
                second?.cancel()
            }
        }
        for ((_, value) in countDownMap) {
            value?.cancel()
        }
        timerMap.clear()
        countDownMap.clear()
    }

}