package com.example.common.bus

import android.os.Looper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

/**
 * author: wyb
 * date: 2018/4/16.
 * 传递事件类
 */
class EventBus {

    companion object {
        @JvmStatic
        val instance by lazy { EventBus() }
    }

    fun post(vararg objs: Event) {
        for (obj in objs) {
            when (Looper.getMainLooper()) {
                Looper.myLooper() -> org.greenrobot.eventbus.EventBus.getDefault().post(obj)
                else -> GlobalScope.launch(Dispatchers.Main) { org.greenrobot.eventbus.EventBus.getDefault().post(obj) }
            }
        }
    }

}