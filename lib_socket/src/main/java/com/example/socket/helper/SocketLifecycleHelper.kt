package com.example.socket.helper

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import com.example.common.socket.interf.SocketRequest
import com.example.framework.utils.function.value.hasAnnotation
import java.lang.ref.WeakReference

/**
 * @description socket生命週期管理類
 * @author yan
 */
object SocketLifecycleHelper : LifecycleEventObserver {
    private val list by lazy { ArrayList<WeakReference<LifecycleOwner>>() }

    fun add(owner: LifecycleOwner) {
        if (!owner.needSocketRequest) return
        list.add(WeakReference(owner))
        owner.lifecycle.addObserver(this)
    }

    fun remove(owner: LifecycleOwner) {
        if (!owner.needSocketRequest) return
        list.removeAll { it.get() == owner }
        owner.lifecycle.removeObserver(this)
    }

    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        val clazz = source::class.java.getAnnotation(SocketRequest::class.java)
        if (null != clazz) {
            val value = clazz.value
            when (event) {
                Lifecycle.Event.ON_RESUME -> SocketConnectHelper.topic(*value)
                Lifecycle.Event.ON_PAUSE -> SocketConnectHelper.untopic(*value)
                else -> {}
            }
        }
    }
}

val LifecycleOwner.needSocketRequest get() = hasAnnotation(SocketRequest::class.java)