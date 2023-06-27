package com.example.framework.utils

import android.os.CountDownTimer
import android.os.Looper
import com.example.framework.utils.function.value.second
import java.util.*
import java.util.concurrent.ConcurrentHashMap

/**
 *  Created by wangyanbin
 *  定时器工具类
 */
object TimerUtil {
    private val handler by lazy { WeakHandler(Looper.getMainLooper()) }
    private val timerMap by lazy { ConcurrentHashMap<String, Pair<Timer?, TimerTask?>>() }
    private val countDownMap by lazy { ConcurrentHashMap<String, CountDownTimer?>() }

    /**
     * 延时任务-容易造成内存泄漏
     */
    fun schedule(run: (() -> Unit)?, millisecond: Long = 1000) {
        handler.postDelayed({
            run?.invoke()
        }, millisecond)
    }

    /**
     * 计时(累加)-开始
     */
    fun startTask(tag: String = "TIMER_DEFAULT", run: (() -> Unit)?, millisecond: Long = 1000) {
        if (timerMap[tag] == null) {
            val timer = Timer()
            val timerTask = object : TimerTask() {
                override fun run() {
                    handler.post { run?.invoke() }
                }
            }
            timerMap[tag] = timer to timerTask
        }
        timerMap[tag]?.apply { first?.schedule(second, 0, millisecond) }
    }

    /**
     * 计时（累加）-结束
     */
    fun stopTask(tag: String = "TIMER_DEFAULT") {
        timerMap[tag]?.apply {
            first?.cancel()
            second?.cancel()
        }
        timerMap.remove(tag)
    }

    fun stopTask(vararg tags: String) {
        tags.forEach { stopTask(it) }
    }

    /**
     * 倒计时-开始
     * second-秒
     */
    fun startCountDown(tag: String = "COUNT_DOWN_DEFAULT", onTick: ((second: Long) -> Unit)?, onFinish: (() -> Unit)?, second: Int = 1) {
        if (countDownMap[tag] == null) {
            val countDownTimer = object : CountDownTimer(second.second, 1.second) {
                override fun onTick(millisUntilFinished: Long) {
                    onTick?.invoke((millisUntilFinished / 1000L))
                }

                override fun onFinish() {
                    stopCountDown(tag)
                    onFinish?.invoke()
                }
            }
            countDownMap[tag] = countDownTimer
        }
        countDownMap[tag]?.start()
    }

    /**
     * 倒计时-结束
     */
    fun stopCountDown(tag: String = "COUNT_DOWN_DEFAULT") {
        countDownMap[tag]?.cancel()
        countDownMap.remove(tag)
    }

    fun stopCountDown(vararg tags: String) {
        tags.forEach { stopCountDown(it) }
    }

    /**
     * 完全销毁所有定时器
     */
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