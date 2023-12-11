package com.example.framework.utils.builder

import android.os.CountDownTimer
import android.os.Looper
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import com.example.framework.utils.WeakHandler
import com.example.framework.utils.function.value.second
import java.util.Timer
import java.util.TimerTask
import java.util.concurrent.ConcurrentHashMap

class TimerBuilder(private val observer: LifecycleOwner) : LifecycleEventObserver {
    private val timerMap by lazy { ConcurrentHashMap<String, Pair<Timer?, TimerTask?>>() }
    private val countDownMap by lazy { ConcurrentHashMap<String, CountDownTimer?>() }

    companion object {
        private const val TIMER_DEFAULT_TAG = "TIMER_DEFAULT"
        private const val COUNT_DOWN_DEFAULT_TAG = "COUNT_DOWN_DEFAULT"
        private val handler by lazy { WeakHandler(Looper.getMainLooper()) }

        /**
         * 延时任务-容易造成内存泄漏
         */
        fun schedule(run: (() -> Unit), millisecond: Long = 1000) {
            handler.postDelayed({
                run.invoke()
            }, millisecond)
        }
    }

    /**
     * 计时(累加)-开始
     */
    fun startTask(tag: String = TIMER_DEFAULT_TAG, run: (() -> Unit), millisecond: Long = 1000) {
        if (timerMap[tag] == null) {
            timerMap[tag] = Timer() to object : TimerTask() {
                override fun run() {
                    handler.post { run.invoke() }
                }
            }
        }
        timerMap[tag]?.apply { first?.schedule(second, 0, millisecond) }
    }

    /**
     * 计时（累加）-结束
     */
    fun stopTask(tag: String = TIMER_DEFAULT_TAG) {
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
    fun startCountDown(tag: String = COUNT_DOWN_DEFAULT_TAG, onTick: ((second: Long) -> Unit), onFinish: (() -> Unit), second: Int = 1) {
        if (countDownMap[tag] == null) {
            countDownMap[tag] = object : CountDownTimer(second.second, 1.second) {
                override fun onTick(millisUntilFinished: Long) {
                    onTick.invoke((millisUntilFinished / 1000L))
                }

                override fun onFinish() {
                    stopCountDown(tag)
                    onFinish.invoke()
                }
            }
        }
        countDownMap[tag]?.start()
    }

    /**
     * 倒计时-结束
     */
    fun stopCountDown(tag: String = COUNT_DOWN_DEFAULT_TAG) {
        countDownMap[tag]?.cancel()
        countDownMap.remove(tag)
    }

    fun stopCountDown(vararg tags: String) {
        tags.forEach { stopCountDown(it) }
    }

    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        when (event) {
            Lifecycle.Event.ON_DESTROY -> {
                destroy()
                observer.lifecycle.removeObserver(this)
            }
            else -> {}
        }
    }

    /**
     * 完全销毁所有定时器
     */
    private fun destroy() {
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