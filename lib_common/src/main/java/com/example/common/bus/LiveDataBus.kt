package com.example.common.bus

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.example.common.constant.Constants

/**
 *  Created by wangyanbin
 *  项目消息分发
 */
class LiveDataBus private constructor() {
    private val bus by lazy { HashMap<String, MutableLiveDataBus<Any>>() }

    companion object {
        @JvmStatic
        val instance: LiveDataBus by lazy { LiveDataBus() }
    }

    /**
     * 订阅方法，传入消息名称，类型，通过observe订阅
     */
    fun <T> toFlowable(key: String): MutableLiveData<T> {
        if (!bus.containsKey(key)) {
            bus[key] = MutableLiveDataBus()
        }
        return bus[key] as MutableLiveData<T>
    }

    /**
     * 项目订阅
     */
    fun observe(owner: LifecycleOwner, observer: Observer<LiveDataEvent>) = toFlowable<LiveDataEvent>(Constants.LIVE_DATA_KEY).observe(owner, observer)

    /**
     * 项目通知
     */
    fun post(vararg objs: LiveDataEvent) {
        for (obj in objs) {
            toFlowable<LiveDataEvent>(Constants.LIVE_DATA_KEY).postValue(obj)
        }
    }

    fun set(vararg objs: LiveDataEvent) {
        for (obj in objs) {
            toFlowable<LiveDataEvent>(Constants.LIVE_DATA_KEY).value = obj
        }
    }

}