package com.example.common.bus

import android.os.Bundle
import android.os.Looper
import android.os.Parcelable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.Serializable

/**
 * author: wyb
 * date: 2018/4/16.
 * 传递事件类
 */
class EventBus private constructor() {
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

/**
 * 单独某一个对象发送
 */
fun String.post() = EventBus.instance.post(Event(this))

fun String.post(value: Boolean) = EventBus.instance.post(Event(this, value))

fun String.post(value: Int) = EventBus.instance.post(Event(this, value))

fun String.post(value: String) = EventBus.instance.post(Event(this, value))

fun String.post(args: Bundle) = EventBus.instance.post(Event(this, args))

fun String.post(any: Serializable) = EventBus.instance.post(Event(this, any))

fun String.post(any: Parcelable) = EventBus.instance.post(Event(this, any))

fun String.event() = Event(this)

fun String.event(value: Boolean) = Event(this, value)

fun String.event(value: Int) = Event(this, value)

fun String.event(value: String) = Event(this, value)

fun String.event(args: Bundle) = Event(this, args)

fun String.event(any: Serializable) = Event(this, any)

fun String.event(any: Parcelable) = Event(this, any)