package com.example.common.socket.topic

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import com.example.framework.utils.function.value.hasAnnotation
import com.example.mvvm.utils.socket.topic.interf.SocketRequest
import java.lang.ref.WeakReference

/**
 * socket生命周期管理，适用于多个界面多个wss订阅
 * 写在baseactivity中OnCreate-》WebSocketRequest.addObserver(this)
 */
object WebSocketRequest : LifecycleEventObserver {
    private val list by lazy { ArrayList<WeakReference<LifecycleOwner>>() }

    @JvmStatic
    fun addObserver(owner: LifecycleOwner) {
        add(owner)
    }

    @JvmStatic
    private fun add(owner: LifecycleOwner) {
        if (!owner.isSocketRequest) return
        list.add(WeakReference(owner))
        owner.lifecycle.addObserver(this)
    }

    @JvmStatic
    private fun remove(owner: LifecycleOwner) {
        if (!owner.isSocketRequest) return
        list.removeAll { it.get() == owner }
        owner.lifecycle.removeObserver(this)
    }

    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        val clazz = source::class.java.getAnnotation(SocketRequest::class.java)
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

val LifecycleOwner.isSocketRequest get() = hasAnnotation(SocketRequest::class.java)