package com.example.common.bus

/**
 * author: wyb
 * date: 2018/4/16.
 * 传递事件类
 */
class Event(var action: String, var value: Any? = null) {

    fun setAction(action: String): Event {
        this.action = action
        return this
    }

    fun setValue(value: Any?): Event {
        this.value = value
        return this
    }

    /**
     * 是否是当前对象
     * 不做任何返回
     */
    fun Event?.isEvent(action: String, block: () -> Unit): Event? {
        this ?: return null
        if (this.action == action) block()
        return this
    }

    /**
     * 是否是当前对象
     * 返回泛型类型
     */
    fun <T> Event?.isEvent(action: String, block: T?.() -> Unit): Event? {
        this ?: return null
        if (this.action == action) block(value as? T)
        return this
    }

}