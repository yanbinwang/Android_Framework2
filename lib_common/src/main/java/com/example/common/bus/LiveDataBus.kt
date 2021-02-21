package com.example.common.bus

import androidx.lifecycle.MutableLiveData
import com.example.common.constant.Constants
import java.util.*

/**
 *  Created by wangyanbin
 *  项目消息分发
 */
class LiveDataBus private constructor() {
    private val bus by lazy { HashMap<String, BusMutableLiveData<Any>>() }

    companion object {
        @JvmStatic
        val instance: LiveDataBus by lazy {
            LiveDataBus()
        }
    }

    //订阅方法，传入消息名称，类型，通过observe订阅
    fun <T> toFlowable(key: String, type: Class<T>): MutableLiveData<T> {
        if (!bus.containsKey(key)) {
            bus[key] = BusMutableLiveData()
        }
        return bus[key] as MutableLiveData<T>
    }

    //通知方法，传入消息名称，通过postValue发送数据
    fun post(key: String): MutableLiveData<Any> {
        return toFlowable(key, Any::class.java)
    }

    //订阅
    fun toFlowable(): MutableLiveData<LiveDataEvent> {
        return toFlowable(Constants.LIVE_DATA_KEY, LiveDataEvent::class.java)
    }

    //通知
    fun post(event: LiveDataEvent) {
        toFlowable(Constants.LIVE_DATA_KEY, LiveDataEvent::class.java).postValue(event)
    }

}