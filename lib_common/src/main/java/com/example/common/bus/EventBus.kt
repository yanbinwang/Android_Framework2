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
    private val busDefault get() = org.greenrobot.eventbus.EventBus.getDefault()

    fun register(subscriber: Any) = run { if (!busDefault.isRegistered(subscriber)) busDefault.register(subscriber) }

    fun unregister(subscriber: Any) = run { if (busDefault.isRegistered(subscriber)) busDefault.unregister(subscriber) }

    fun post(vararg objs: Event) {
        for (obj in objs) {
            when (Looper.getMainLooper()) {
                Looper.myLooper() -> busDefault.post(obj)
                else -> GlobalScope.launch(Dispatchers.Main) { busDefault.post(obj) }
            }
        }
    }

    companion object {
        @JvmStatic
        val instance by lazy { EventBus() }
    }

}