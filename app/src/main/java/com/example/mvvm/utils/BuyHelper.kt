package com.example.mvvm.utils

import androidx.lifecycle.LifecycleOwner
import com.example.framework.utils.builder.TimerBuilder
import com.example.framework.utils.function.value.safeGet
import com.example.framework.utils.function.value.safeSize
import com.example.framework.utils.function.value.timer

class BuyHelper(observer: LifecycleOwner) {
    private val timer by lazy { TimerBuilder(observer) }

    companion object {
        private const val BUY_TAG = "BUY"
    }

    /**
     * 传入秒
     */
    fun getCountDown(second: Long? = 0) {
        timer.apply {
            stopCountDown(BUY_TAG)
            startCountDown(BUY_TAG, {
                setCountDown(it)
            }, {
                setCountDown(0)
            })
        }
    }

    private fun setCountDown(second: Long) {
        val list = second.timer().split(":")
        val isChange = list.safeSize == 2
        val hour = if (isChange) "00" else list.safeGet(0)
        val second = if (isChange) list.safeGet(0) else list.safeGet(1)
        var minute = if (isChange) list.safeGet(1) else list.safeGet(2)

    }

}