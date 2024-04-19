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
 */
object ServerLogRequest : LifecycleEventObserver {
    private val list by lazy { ArrayList<Pair<ServerLogProxy, WeakReference<LifecycleOwner>>>() }

    /**
     * baseActivity中调取
     */
    @JvmStatic
    fun addObserver(owner: LifecycleOwner) {
        add(owner)
    }

    @JvmStatic
    private fun add(owner: LifecycleOwner) {
        if (!owner.isLogRequest) return
        list.add(ServerLogProxy(owner) to WeakReference(owner))
        owner.lifecycle.addObserver(this)
    }

    @JvmStatic
    private fun remove(owner: LifecycleOwner) {
        if (!owner.isLogRequest) return
        list.removeAll { it.second.get() == owner }
        owner.lifecycle.removeObserver(this)
    }

    /**
     * 要捕获记录的时候添加
     */
    @JvmStatic
    fun record(owner: LifecycleOwner, type: Int?) {
        val proxy = list.filter { it.second.get() == owner }.safeGet(0)?.first
        proxy?.record(type)
    }

    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        val clazz = source::class.java.getAnnotation(LogRequest::class.java)
        if (null != clazz) {
            when (event) {
                Lifecycle.Event.ON_DESTROY -> remove(source)
                else -> {}
            }
        }
    }
}

val LifecycleOwner.isLogRequest get() = hasAnnotation(LogRequest::class.java)