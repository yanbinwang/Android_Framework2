package com.example.mvvm.utils.log

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import com.example.framework.utils.function.value.hasAnnotation
import com.example.framework.utils.function.value.safeGet
import com.example.mvvm.utils.log.interf.LogRequest
import java.lang.ref.WeakReference
import java.util.concurrent.ConcurrentHashMap

/**
 * 日志记录
 * 需要记录日志的activity和fragment加上isLogRequest注解
 */
object ServerLogRequest : LifecycleEventObserver {
    private val logMap by lazy { ConcurrentHashMap<WeakReference<LifecycleOwner>, ServerLogProxy>() }

    // <editor-fold defaultstate="collapsed" desc="订阅相关">
    /**
     * BaseActivity 中调取
     */
    fun addObserver(owner: LifecycleOwner) {
        add(owner)
    }

    /**
     * 动态添加生命周期管理
     */
    private fun add(owner: LifecycleOwner) {
        if (!owner.isLogRequest) return
        if (logMap.any { entry ->
            val target = entry.key.get()
            target === owner
        }) return
        logMap[WeakReference(owner)] = ServerLogProxy()
        owner.lifecycle.addObserver(this)
    }

    /**
     * 生命周期管控
     */
    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        val clazz = source::class.java.getAnnotation(LogRequest::class.java)
        if (null != clazz) {
            when (event) {
                Lifecycle.Event.ON_PAUSE -> push(source)
                Lifecycle.Event.ON_DESTROY -> remove(source)
                else -> {}
            }
        }
    }

    private fun remove(owner: LifecycleOwner) {
        if (!owner.isLogRequest) return
        destroy(owner)
        logMap.entries.removeAll { entry ->
            val target = entry.key.get()
            target == null || target === owner
        }
        owner.lifecycle.removeObserver(this)
    }

    private fun push(owner: LifecycleOwner) {
        logMap.entries.filter {
            it.key.get() == owner
        }.safeGet(0)?.value?.push()
    }

    private fun destroy(owner: LifecycleOwner) {
        logMap.entries.filter {
            it.key.get() == owner
        }.safeGet(0)?.value?.destroy()
    }
    // </editor-fold>

    /**
     * 要捕获记录的时候添加
     */
    @Synchronized
    fun LifecycleOwner?.record(type: Int?) {
        this ?: return
        logMap.entries.filter {
            it.key.get() == this
        }.safeGet(0)?.value?.record(type)
    }

}

val LifecycleOwner.isLogRequest get() = hasAnnotation(LogRequest::class.java)