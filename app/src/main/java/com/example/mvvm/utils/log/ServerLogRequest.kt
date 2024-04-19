package com.example.mvvm.utils.log

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import com.example.framework.utils.function.value.hasAnnotation
import com.example.framework.utils.function.value.safeGet
import com.example.mvvm.utils.log.interf.LogRequest
import java.lang.ref.WeakReference

/**
 * 日志记录
 * 需要记录日志的activity和fragment加上isLogRequest注解
 */
object ServerLogRequest : LifecycleEventObserver {
    private val list by lazy { ArrayList<Pair<ServerLogProxy, WeakReference<LifecycleOwner>>>() }

    // <editor-fold defaultstate="collapsed" desc="订阅相关">
    /**
     * baseActivity中调取
     */
    @JvmStatic
    fun addObserver(owner: LifecycleOwner) {
        add(owner)
    }

    /**
     * 动态添加生命周期管理
     */
    @JvmStatic
    private fun add(owner: LifecycleOwner) {
        if (!owner.isLogRequest) return
        list.add(ServerLogProxy() to WeakReference(owner))
        owner.lifecycle.addObserver(this)
    }

    /**
     * 页面关闭时销毁
     */
    @JvmStatic
    private fun remove(owner: LifecycleOwner) {
        if (!owner.isLogRequest) return
        destroy(owner)
        list.removeAll { it.second.get() == owner }
        owner.lifecycle.removeObserver(this)
    }

    @JvmStatic
    @Synchronized
    private fun push(owner: LifecycleOwner) {
        proxy(owner)?.push()
    }

    @JvmStatic
    @Synchronized
    private fun destroy(owner: LifecycleOwner) {
        proxy(owner)?.destroy()
    }

    /**
     * 获取当前绑定的生命周期的日志上传类
     */
    @JvmStatic
    private fun proxy(owner: LifecycleOwner): ServerLogProxy? {
        return list.filter { it.second.get() == owner }.safeGet(0)?.first
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
    // </editor-fold>

    /**
     * 要捕获记录的时候添加
     */
    @JvmStatic
    @Synchronized
    fun LifecycleOwner?.record(type: Int?) {
        this ?: return
        proxy(this)?.record(type)
    }

}

val LifecycleOwner.isLogRequest get() = hasAnnotation(LogRequest::class.java)