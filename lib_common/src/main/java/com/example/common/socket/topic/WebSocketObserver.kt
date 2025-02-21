package com.example.common.socket.topic

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import com.example.common.socket.topic.interf.SocketObserver
import com.example.framework.utils.function.value.hasAnnotation
import java.lang.ref.WeakReference
import java.util.concurrent.atomic.AtomicReference

/**
 * socket生命周期管理，适用于多个界面多个wss订阅
 * 写在BaseActivity中OnCreate-》WebSocketRequest.addObserver(this)
 */
object WebSocketObserver : LifecycleEventObserver {
    //    private val list by lazy { ArrayList<WeakReference<LifecycleOwner>>() }
    private val atomicListRef by lazy { AtomicReference<ArrayList<WeakReference<LifecycleOwner>>>() }

    @JvmStatic
    fun addObserver(owner: LifecycleOwner) {
        add(owner)
    }

    @JvmStatic
    private fun add(owner: LifecycleOwner) {
        if (!owner.isSocketObserver) return
        atomicListRef.get().add(WeakReference(owner))
        owner.lifecycle.addObserver(this)
    }

    @JvmStatic
    private fun remove(owner: LifecycleOwner) {
        if (!owner.isSocketObserver) return
        atomicListRef.get().removeAll { it.get() == owner }
        owner.lifecycle.removeObserver(this)
    }

    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        val clazz = source::class.java.getAnnotation(SocketObserver::class.java)
        if (null != clazz) {
            val topicUrl = clazz.value
            when (event) {
                Lifecycle.Event.ON_RESUME -> WebSocketConnect.topic(*topicUrl)
                Lifecycle.Event.ON_PAUSE -> WebSocketConnect.untopic(*topicUrl)
                Lifecycle.Event.ON_DESTROY -> remove(source)
                else -> {}
            }
        }
//        val anno = source::class.java.annotations.find { it::class.java == SocketRequest::class.java } as? SocketRequest
//            ?: return
    }
}

val LifecycleOwner.isSocketObserver get() = hasAnnotation(SocketObserver::class.java)