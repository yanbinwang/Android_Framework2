package com.example.common.event

import com.example.framework.utils.function.value.toNewList

/**
 * author: wyb
 * 传递事件类
 * 1） 广播每次发送，都会轮询一遍所有注册的页面
 * 2） 故而只在指定页面订阅降低开销
 */
data class Event(private val action: Int, private val value: Any? = null) {

    /**
     * 单个对象传递
     */
    fun <K> Event?.isEvent(code: Code<K>, block: K?.() -> Unit): Event? {
        this ?: return null
        if (action == code.action) {
            block(value as? K)
            return null
        }
        return this
    }

    /**
     * 多个相同对象传递
     */
    fun <K> Event?.isEvent(codes: List<Code<K>>, block: K?.() -> Unit): Event? {
        this ?: return null
        if (codes.any { action == it.action }) {
            block(value as? K)
            return null
        }
        return this
    }

    /**
     * 不指定类型的多个对象传递
     */
    fun Event?.isEventAny(codes: List<Code<*>>, block: Any?.() -> Unit): Event? {
        this ?: return null
        if (codes.any { action == it.action }) {
            block(value)
            return null
        }
        return this
    }

}

class Code<T> {

    companion object {
        /**
         * 方便设置不重复的action,每次重启app数值都会重新累加，以此区分此次发送消息对象的唯一性
         */
        private var actionTime = 0

        /**
         * 全局批量自由组合多个Code+数据
         * 缓冲满时，当前这条 emit 挂起 (目前配置 10 条)
         * 等订阅消费腾出缓冲区、emit 恢复后，才会执行下一条事件；
         * 同批次剩余事件只是排队延后，不会丢失、不会乱序，最终全部依次分发。
         * [Code].posts(
         *     PAY to "支付成功",
         *     REFRESH to 123
         * )
         */
        fun posts(vararg pairs: Pair<Code<*>, Any?>) {
            val eventList = pairs.toList().toNewList { (code, data) ->
                Event(code.action, data)
            }
            EventBus.instance.posts(*eventList.toTypedArray())
        }
    }

    var action = actionTime++
        private set

    fun post(obj: T? = null) {
        EventBus.instance.post(Event(action, obj))
    }

}