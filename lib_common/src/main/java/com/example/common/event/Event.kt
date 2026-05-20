package com.example.common.event

/**
 * author: wyb
 * date: 2018/4/16.
 * 传递事件类
 * 1）广播每次发送，都会轮询一遍所有注册的页面
 * 2）故而只在指定页面订阅降低开销
 */
class Event(var action: Int, var value: Any? = null) {

    fun setAction(action: Int): Event {
        this.action = action
        return this
    }

    fun setValue(value: Any?): Event {
        this.value = value
        return this
    }

    /**
     * 单个对象传递
     */
    fun <K> Event?.isEvent(code: Code<K>, block: K?.() -> Unit): Event? {
        this ?: return null
        if (this.action == code.action) {
            block(this.value as? K)
            return null
        }
        return this
    }

    /**
     * 多个相同对象传递
     */
    fun <K> Event?.isEvent(codes: List<Code<K>>, block: K?.() -> Unit): Event? {
        this ?: return null
        if (codes.find { this.action == it.action } != null) {
            block(this.value as? K)
            return null
        }
        return this
    }

    /**
     * 不指定类型的多个对象传递
     */
    fun Event?.isEventAny(codes: List<Code<*>>, block: Any?.() -> Unit): Event? {
        this ?: return null
        if (codes.find { this.action == it.action } != null) {
            block(this.value)
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
    }

    var action = actionTime++
        private set

    fun post(obj: T? = null) = EventBus.instance.post(Event(action, obj))

}