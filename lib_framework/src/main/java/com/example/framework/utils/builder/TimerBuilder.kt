package com.example.framework.utils.builder

import android.os.CountDownTimer
import android.os.Looper
import androidx.lifecycle.LifecycleOwner
import com.example.framework.utils.WeakHandler
import com.example.framework.utils.function.doOnDestroy
import com.example.framework.utils.function.value.second
import java.util.Timer
import java.util.TimerTask
import java.util.concurrent.ConcurrentHashMap

class TimerBuilder(observer: LifecycleOwner) {
    private val timerMap by lazy { ConcurrentHashMap<String, Pair<Timer?, TimerTask?>>() }
    private val countDownMap by lazy { ConcurrentHashMap<String, CountDownTimer?>() }

    companion object {
        private const val TASK_DEFAULT_TAG = "TASK_DEFAULT"
        private const val COUNT_DOWN_DEFAULT_TAG = "COUNT_DOWN_DEFAULT"
        private val handler by lazy { WeakHandler(Looper.getMainLooper()) }

        /**
         * 延时任务-容易造成内存泄漏
         */
        @JvmStatic
        @Synchronized
        fun schedule(run: (() -> Unit), millisecond: Long = 1000) {
            handler.postDelayed({
                run.invoke()
            }, millisecond)
        }
    }

    init {
        observer.doOnDestroy {
            destroy()
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

    /**
     * 计时(累加)-开始
     */
    @Synchronized
    fun startTask(tag: String = TASK_DEFAULT_TAG, run: (() -> Unit), millisecond: Long = 1000) {
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
    @Synchronized
    fun stopTask(tag: String = TASK_DEFAULT_TAG) {
        timerMap[tag]?.apply {
            first?.cancel()
            second?.cancel()
        }
        timerMap.remove(tag)
    }

    @Synchronized
    fun stopTask(vararg tags: String) {
        tags.forEach { stopTask(it) }
    }

    /**
     * 倒计时-开始
     * second-秒
     */
    @Synchronized
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
    @Synchronized
    fun stopCountDown(tag: String = COUNT_DOWN_DEFAULT_TAG) {
        countDownMap[tag]?.cancel()
        countDownMap.remove(tag)
    }

    @Synchronized
    fun stopCountDown(vararg tags: String) {
        tags.forEach { stopCountDown(it) }
    }

}