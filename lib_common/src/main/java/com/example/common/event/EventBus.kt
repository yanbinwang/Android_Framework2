package com.example.common.event

import android.os.Looper
import androidx.lifecycle.Lifecycle
import com.example.framework.utils.function.doOnDestroy
import kotlinx.coroutines.Dispatchers.Main
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

    fun register(subscriber: Any, lifecycle: Lifecycle) {
        if (!busDefault.isRegistered(subscriber)) {
            busDefault.register(subscriber)
            lifecycle.doOnDestroy {
                busDefault.unregister(subscriber)
            }
        }
    }

    fun unregister(subscriber: Any) {
        if (busDefault.isRegistered(subscriber)) busDefault.unregister(subscriber)
    }

//    fun post(vararg objs: Event) {
//        objs.forEach {
//            when (Looper.getMainLooper()) {
//                Looper.myLooper() -> busDefault.post(it)
//                else -> GlobalScope.launch(Main) { busDefault.post(it) }
//            }
//        }
//    }

    fun post(event: Event) {
        when (Looper.getMainLooper()) {
            Looper.myLooper() -> busDefault.post(event)
            else -> GlobalScope.launch(Main) { busDefault.post(event) }
        }
    }

}