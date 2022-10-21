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
class EventBus private constructor() {

    companion object {
        @JvmStatic
        val instance by lazy { EventBus() }
    }

    private val busDefault get() = org.greenrobot.eventbus.EventBus.getDefault()

    fun register(subscriber: Any) = run { if (!busDefault.isRegistered(subscriber)) busDefault.register(subscriber) }

    fun unregister(subscriber: Any) = run { if (busDefault.isRegistered(subscriber)) busDefault.unregister(subscriber) }

    fun post(vararg objs: Event) {
        objs.forEach {
            when (Looper.getMainLooper()) {
                Looper.myLooper() -> busDefault.post(it)
                else -> GlobalScope.launch(Dispatchers.Main) { busDefault.post(it) }
            }
        }
    }

}

/**
 * 单独某一个对象发送
 */
fun String.post(any: Any? = null) = EventBus.instance.post(this.event(any))

fun String.event(any: Any? = null) = Event(this, any)