package com.example.common.network.socket.topic

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import com.example.common.network.socket.topic.interf.SocketObserver
import com.example.framework.utils.function.value.hasAnnotation
import java.lang.ref.WeakReference
import java.util.concurrent.atomic.AtomicReference

/**
 * WebSocket生命周期管理，适用于多个界面多个wss订阅
 * 1) 写在BaseActivity中OnCreate -》 WebSocketObserver.addObserver(this)
 * 2) 写的Fragment中，如果是ViewPager2没太大问题，如果是FragmentManager的话，不建议写
 */
object WebSocketObserver : LifecycleEventObserver {
    // 用于存储页面生命周期的集合
    private val atomicRefList by lazy { AtomicReference(ArrayList<WeakReference<LifecycleOwner>>()) }

    /**
     * 添加生命周期
     * 只在onCreate主线程调取该方法,杜绝在子线程调取,避免引发ConcurrentModificationException
     */
    @JvmStatic
    fun addObserver(owner: LifecycleOwner) {
        add(owner)
    }

    /**
     * 添加
     */
    private fun add(owner: LifecycleOwner) {
        if (!owner.isSocketObserver) return
        val list = atomicRefList.get()
        val isExisted = list.any { it.get() == owner }
        if (isExisted) return
        list.add(WeakReference(owner))
        owner.lifecycle.addObserver(this)
        list.removeAll { it.get() == null }
    }

    /**
     * 删除
     */
    private fun remove(owner: LifecycleOwner) {
        if (!owner.isSocketObserver) return
        val list = atomicRefList.get()
        list.removeAll { it.get() == owner }
        list.removeAll { it.get() == null }
        owner.lifecycle.removeObserver(this)
    }

    /**
     * 生命周期回调监听
     * 针对Fragment:
     * supportFragmentManager.fragments
     *     .filterIsInstance<OrderFragment>() // 精准筛选目标 Fragment
     *     .firstOrNull()
     *     ?.takeIf { !it.isHidden && it.isAdded } // 双重校验：可见 + 已附加到 Activity
     *     ?.refreshOrderData(data)
     */
    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        val socketAnnotation = source::class.java.getAnnotation(SocketObserver::class.java)
        socketAnnotation ?: return
        val topics = socketAnnotation.value
        when (event) {
            Lifecycle.Event.ON_RESUME -> WebSocketConnect.topic(source, *topics)
            Lifecycle.Event.ON_PAUSE -> WebSocketConnect.untopic(*topics)
            Lifecycle.Event.ON_DESTROY -> {
                WebSocketConnect.untopic(*topics)
                remove(source)
            }
            else -> {}
        }
//        val anno = source::class.java.annotations.find { it::class.java == SocketRequest::class.java } as? SocketRequest ?: return
    }

}

val LifecycleOwner.isSocketObserver get() = hasAnnotation(SocketObserver::class.java)