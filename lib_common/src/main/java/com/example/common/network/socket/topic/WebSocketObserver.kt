package com.example.common.network.socket.topic

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import com.example.common.network.socket.topic.interf.SocketObserver
import com.example.framework.utils.function.value.hasAnnotation
import java.lang.ref.WeakReference
import java.util.concurrent.atomic.AtomicReference

/**
 * socket生命周期管理，适用于多个界面多个wss订阅
 * 写在BaseActivity中OnCreate-》WebSocketObserver.addObserver(this)
 * 写的fragment中，如果是viewpager2没太大问题，如果是FragmentManager的话，不建议写
 */
object WebSocketObserver : LifecycleEventObserver {
    //用于存储页面生命周期的集合
    private val atomicRefList by lazy { AtomicReference(ArrayList<WeakReference<LifecycleOwner>>()) }

    /**
     * 添加生命周期
     */
    @JvmStatic
    fun addObserver(owner: LifecycleOwner) {
        add(owner)
    }

    /**
     * 添加
     */
    @JvmStatic
    private fun add(owner: LifecycleOwner) {
        if (!owner.isSocketObserver) return
        atomicRefList.get().add(WeakReference(owner))
        owner.lifecycle.addObserver(this)
    }

    /**
     * 删除
     */
    @JvmStatic
    private fun remove(owner: LifecycleOwner) {
        if (!owner.isSocketObserver) return
        atomicRefList.get().removeAll { it.get() == owner }
        owner.lifecycle.removeObserver(this)
    }

    /**
     * 针对activity
     */
    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        val clazz = source::class.java.getAnnotation(SocketObserver::class.java)
        clazz ?: return
        val value = clazz.value
        when (event) {
            Lifecycle.Event.ON_RESUME -> WebSocketConnect.topic(*value)
            Lifecycle.Event.ON_PAUSE -> WebSocketConnect.untopic(*value)
            Lifecycle.Event.ON_DESTROY -> remove(source)
            else -> {}
        }
//        val anno = source::class.java.annotations.find { it::class.java == SocketRequest::class.java } as? SocketRequest
//            ?: return
    }

//    /**
//     * 针对Fragment订阅
//     * Fragment在使用FragmentManager时，重写onResume和onHiddenChanged都会调取
//     */
//    @JvmStatic
//    fun onStateChanged(owner: LifecycleOwner, hidden: Boolean) {
//        //注解校验
//        if (!owner.isSocketObserver) return
//        //提取一下页面注解的类
//        val clazz = owner::class.java.getAnnotation(SocketObserver::class.java)
//        clazz ?: return
//        //获取注解类里批量订阅的wss地址
//        val value = clazz.value
//        //检测一下本地管控生命周期的嘞里是否存储了这个值
//        val source = atomicRefList.get().find { it.get() == owner }
//        if (null == source) {
//            //添加进集合，做生命周期监听
//            add(owner, false)
//            owner.doOnDestroy {
//                WebSocketConnect.untopic(*value)
//                remove(owner, false)
//            }
//        }
//        //子页面可见
//        if (!hidden) {
//            WebSocketConnect.topic(*value)
//        } else {
//            WebSocketConnect.untopic(*value)
//        }
//    }

}

val LifecycleOwner.isSocketObserver get() = hasAnnotation(SocketObserver::class.java)