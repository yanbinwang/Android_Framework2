package com.example.common.bus

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.example.common.constant.Constants

/**
 *  Created by wangyanbin
 *  项目消息分发
 */
class LiveDataBus private constructor() { private lateinit var owner: LifecycleOwner
    private val bus by lazy { HashMap<String, MutableLiveDataBus<Any>>() }

    companion object {
        @JvmStatic
        val instance: LiveDataBus by lazy { LiveDataBus() }
    }

    /**
     * 项目注册
     */
    fun register(lifecycleOwner: LifecycleOwner): LiveDataBus {
        owner = lifecycleOwner
        return this
    }

    /**
     * 项目注销
     */
    fun unregister(): LiveDataBus {
        bus.clear()
        return this
    }

    /**
     * 项目订阅
     */
    fun observe(observer: Observer<LiveDataEvent>) = subscribe<LiveDataEvent>(Constants.LIVE_DATA_BUS_KEY).observe(owner, observer)

    /**
     * 订阅方法，传入消息名称，类型，通过observe订阅
     * 如果map中未找到，则声明一个
     */
    fun <T> subscribe(key: String): MutableLiveDataBus<T> {
        if (!bus.containsKey(key)) {
            bus[key] = MutableLiveDataBus()
        }
        return bus[key] as MutableLiveDataBus<T>
    }

    /**
     * 主子线程都能执行
     * 如果在主线程执行一个已发布的任务之前多次调用此方法，则只会分派最后一个值。
     */
    fun post(vararg objs: LiveDataEvent) {
        for (obj in objs) {
            subscribe<LiveDataEvent>(Constants.LIVE_DATA_BUS_KEY).postValue(obj)
        }
    }

    /**
     * 必须在主线程执行
     * 连续发送数据，每次数据都能被接收到，不会丢失数据，但只能接收到最后一次发送的数据
     */
    fun set(vararg objs: LiveDataEvent) {
        for (obj in objs) {
            subscribe<LiveDataEvent>(Constants.LIVE_DATA_BUS_KEY).value = obj
        }
    }

//    private val bus by lazy { HashMap<String, MutableLiveDataBus<Any>>() }
//
//    companion object {
//        @JvmStatic
//        val instance: LiveDataBus by lazy { LiveDataBus() }
//    }
//
//    /**
//     * 订阅方法，传入消息名称，类型，通过observe订阅
//     */
//    fun <T> toFlowable(key: String): MutableLiveData<T> {
//        if (!bus.containsKey(key)) {
//            bus[key] = MutableLiveDataBus()
//        }
//        return bus[key] as MutableLiveData<T>
//    }
//
//    /**
//     * 项目订阅
//     */
//    fun observe(owner: LifecycleOwner, observer: Observer<LiveDataEvent>) = toFlowable<LiveDataEvent>(Constants.LIVE_DATA_BUS_KEY).observe(owner, observer)
//
//    /**
//     * 主子线程都能执行
//     * 如果在主线程执行一个已发布的任务之前多次调用此方法，则只会分派最后一个值。
//     */
//    fun post(vararg objs: LiveDataEvent) {
//        for (obj in objs) {
//            toFlowable<LiveDataEvent>(Constants.LIVE_DATA_BUS_KEY).postValue(obj)
//        }
//    }
//
//    /**
//     * 必须在主线程执行
//     * 连续发送数据，每次数据都能被接收到，不会丢失数据，但只能接收到最后一次发送的数据
//     */
//    fun set(vararg objs: LiveDataEvent) {
//        for (obj in objs) {
//            toFlowable<LiveDataEvent>(Constants.LIVE_DATA_BUS_KEY).value = obj
//        }
//    }

}