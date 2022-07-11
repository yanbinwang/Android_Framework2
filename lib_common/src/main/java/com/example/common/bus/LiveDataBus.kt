package com.example.common.bus

import androidx.lifecycle.MutableLiveData
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
    fun toFlowable() = toFlowable<LiveDataEvent>(Constants.LIVE_DATA_KEY)

    /**
     * 项目通知
     */
    fun post(vararg objs: LiveDataEvent) {
        for (obj in objs) {
            toFlowable<LiveDataEvent>(Constants.LIVE_DATA_KEY).postValue(obj)
        }
    }

}