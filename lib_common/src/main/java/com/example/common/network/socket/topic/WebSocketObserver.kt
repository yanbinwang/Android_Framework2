package com.example.common.network.socket.topic

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import com.example.common.network.socket.topic.interf.SocketObserver
import com.example.framework.utils.function.value.hasAnnotation
import java.lang.ref.WeakReference
import java.util.concurrent.CopyOnWriteArrayList

/**
 * WebSocket生命周期管理，适用于多个界面多个wss订阅
 * 1) 写在BaseActivity中OnCreate -》 WebSocketObserver.addObserver(this)
 * 2) 写的Fragment中，如果是 ViewPager2 没太大问题，如果是 FragmentManager 的话，不建议写
 */
object WebSocketObserver : LifecycleEventObserver {
    // 用于存储页面生命周期的集合
    private val ownerList by lazy { CopyOnWriteArrayList<WeakReference<LifecycleOwner>>() }

    /**
     * 添加生命周期观察者
     * 内部使用 CopyOnWriteArrayList 保证线程安全，支持多线程并发调用
     * 推荐在 onCreate 等主线程生命周期中调用
     */
    fun addObserver(owner: LifecycleOwner) {
        add(owner)
    }

    /**
     * 添加
     */
    private fun add(owner: LifecycleOwner) {
        if (!owner.isSocketObserver) return
        if (ownerList.any { it.get() === owner }) return
        ownerList.addIfAbsent(WeakReference(owner))
        owner.lifecycle.addObserver(this)
        ownerList.removeAll { it.get() == null }
    }

    /**
     * 删除
     */
    private fun remove(owner: LifecycleOwner) {
        if (!owner.isSocketObserver) return
        ownerList.removeAll { ref ->
            val target = ref.get()
            target == null || target === owner
        }
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