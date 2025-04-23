package com.example.framework.utils.builder

import android.os.CountDownTimer
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.example.framework.utils.function.doOnDestroy
import com.example.framework.utils.function.value.second
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.ConcurrentHashMap

/**
 * 定时器构建类
 */
class TimerBuilder(private val observer: LifecycleOwner) {
//    private val timerMap by lazy { ConcurrentHashMap<String, Pair<Timer?, TimerTask?>>() }
    private val timerMap by lazy { ConcurrentHashMap<String, Job>() }
    private val countDownMap by lazy { ConcurrentHashMap<String, CountDownTimer?>() }

    companion object {
        //默认计时器tag
        private const val TASK_DEFAULT_TAG = "TASK_DEFAULT"
        //默认倒计时tag
        private const val COUNT_DOWN_DEFAULT_TAG = "COUNT_DOWN_DEFAULT"
//        //默认全局延时handler
//        private val handler by lazy { WeakHandler(Looper.getMainLooper()) }

        /**
         * delayMillis：延时时间（单位：毫秒）
         */
        @JvmStatic
        fun schedule(observer: LifecycleOwner, run: (() -> Unit), delayMillis: Long = 1000) {
            observer.lifecycleScope.launch {
                delay(delayMillis)
                withContext(Main) { run() }
            }
        }
    }

    init {
        //完全销毁所有定时器
        observer.doOnDestroy {
            timerMap.values.forEach { it.cancel() }
            countDownMap.values.forEach { it?.cancel() }
//            for ((key, value) in countDownMap) {
//                value?.cancel()
//            }
            timerMap.clear()
            countDownMap.clear()
        }
    }

    /**
     * 计时(累加)-开始
     * delay（延迟时间）
     * 作用：表示任务首次执行前需要等待的时间（单位：毫秒）
     * 任务将在调用 schedule() 方法后延迟 2 秒（2000ms）执行第一次
     * timer.schedule(task, 2000, 3000);
     *
     * period（周期时间）
     * 作用：表示任务每次执行完成后，下一次执行的间隔时间（单位：毫秒）
     * 任务首次执行延迟 2 秒，之后每隔 3 秒重复执行一次
     * timer.schedule(task, 2000, 3000);
     */
    fun startTask(tag: String = TASK_DEFAULT_TAG, run: (() -> Unit), delay: Long = 0, period: Long = 1000) {
        stopTask(tag)//先停止旧的任务
//        if (timerMap[tag] == null) {
//            timerMap[tag] = Timer() to object : TimerTask() {
//                override fun run() {
//                    handler.post {
//                        run.invoke()
//                    }
//                }
//            }
//        }
//        timerMap[tag]?.apply {
//            first?.schedule(second, delay, period)
//        }
        if (timerMap[tag] == null) {
            val job = observer.lifecycleScope.launch {
                delay(delay)
                flow {
                    while (true) {
                        emit(Unit)
                        delay(period)
                    }
                }.flowOn(IO).collect {
                    withContext(Main) { run() }
                }
            }
            timerMap[tag] = job
        }
    }

    /**
     * 计时（累加）-结束
     */
    fun stopTask(tag: String = TASK_DEFAULT_TAG) {
        timerMap[tag]?.cancel()
        timerMap.remove(tag)
//        timerMap[tag]?.apply {
//            first?.cancel()
//            second?.cancel()
//        }
//        timerMap.remove(tag)
    }

    fun stopTask(vararg tags: String) {
        tags.forEach {
            stopTask(it)
        }
    }

    /**
     * 倒计时-开始
     * millisInFuture：-》总时长/总周期时间
     * 从调用start（）到倒计时结束并调用onFinish（）的未来毫秒数（单位：毫秒）
     *
     * countDownInterval:-》间隔时间
     * 接收onTick（长）回调的时间间隔（单位：毫秒）
     */
    fun startCountDown(tag: String = COUNT_DOWN_DEFAULT_TAG, onTick: ((second: Long) -> Unit), onFinish: (() -> Unit), millisInFuture: Long = 1.second, countDownInterval: Long = 1000) {
        stopCountDown(tag)
        if (countDownMap[tag] == null) {
            countDownMap[tag] = object : CountDownTimer(millisInFuture, countDownInterval) {
                override fun onTick(millisUntilFinished: Long) {
                    onTick.invoke((millisUntilFinished / 1000L))
                }

                override fun onFinish() {
                    stopCountDown(tag)
                    onFinish.invoke()
                }
            }
            countDownMap[tag]?.start()
        }
    }

    /**
     * 倒计时-结束
     */
    fun stopCountDown(tag: String = COUNT_DOWN_DEFAULT_TAG) {
        countDownMap[tag]?.cancel()
        countDownMap.remove(tag)
    }

    fun stopCountDown(vararg tags: String) {
        tags.forEach {
            stopCountDown(it)
        }
    }

}