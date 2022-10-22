package com.example.common.bus

/**
 * author: wyb
 * date: 2018/4/16.
 * 传递事件类
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

    fun <K> Event?.isEvent(code: Code<K>, block: K?.() -> Unit): Event? {
        this ?: return null
        if (this.action == code.action) {
            block(this.value as? K)
            return null
        }
        return this
    }

}

class Code<T> {

    companion object {
        /**
         * 方便设置不重复的action用
         */
        private var actionTime = 0
    }

    var action = actionTime++

    fun post(obj: T? = null) = EventBus.instance.post(event(obj))

    fun event(obj: T? = null) = Event(action, obj)

}