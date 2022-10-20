package com.example.common.bus

import android.os.Bundle
import android.os.Parcelable
import com.example.common.constant.Extras
import java.io.Serializable

/**
 * author: wyb
 * date: 2018/4/16.
 * 传递事件类
 */
class Event {
    private var action: String? = null //广播名称
    private var args: Bundle? = null //额外参数
    private var clazz: Class<*>? = null

    /**
     * 不含任何参数的广播
     */
    constructor(action: String) {
        this.action = action
    }

    /**
     * 带布尔类型的广播
     */
    constructor(action: String, value: Boolean) {
        this.action = action
        this.clazz = Boolean::class.java
        if (args == null) args = Bundle()
        args?.putBoolean(action, value)
    }

    /**
     * 带int类型的广播
     */
    constructor(action: String, value: Int) {
        this.action = action
        this.clazz = Int::class.java
        if (args == null) args = Bundle()
        args?.putInt(action, value)
    }

    /**
     * 带字符串类型的广播
     */
    constructor(action: String, value: String) {
        this.action = action
        this.clazz = String::class.java
        if (args == null) args = Bundle()
        args?.putString(action, value)
    }

    /**
     * 带数据类的广播
     */
    constructor(action: String, args: Bundle) {
        this.action = action
        this.clazz = Bundle::class.java
        this.args = args
    }

    /**
     * 带对象的广播
     */
    constructor(action: String, any: Serializable) {
        this.action = action
        this.clazz = Serializable::class.java
        this.args = Bundle().apply { putSerializable(Extras.BUNDLE_BEAN, any) }
    }

    /**
     * 带对象的广播
     */
    constructor(action: String, any: Parcelable) {
        this.action = action
        this.clazz = Parcelable::class.java
        this.args = Bundle().apply { putParcelable(Extras.BUNDLE_BEAN, any) }
    }

    /**
     * 获取广播名
     */
    fun getAction() = action

    /**
     * 获取默认布尔值
     */
    fun getBoolean(defaultValue: Boolean = false): Boolean {
        return if (args == null) defaultValue else args?.getBoolean(action, defaultValue) ?: false
    }

    /**
     * 获取默认int值
     */
    fun getInt(defaultValue: Int = 0): Int {
        return if (args == null) defaultValue else args?.getInt(action, defaultValue) ?: 0
    }

    /**
     * 获取默认字符串值
     */
    fun getString(): String {
        return if (args == null) "" else args?.getString(action) ?: ""
    }

    /**
     * 获取默认类值
     */
    fun getBundle() = args

    /**
     * 获取默认对象
     */
    fun getSerializable() = args?.getSerializable(Extras.BUNDLE_BEAN)

    /**
     * 获取默认对象
     */
    fun <T : Parcelable> getParcelable() = args?.getParcelable<T>(Extras.BUNDLE_BEAN)

    /**
     * 是否是当前对象
     */
    fun Event?.isEvent(action: String, block: () -> Unit): Event? {
        this ?: return null
        if (this.action == action) block()
        return this
    }

    fun <K> Event?.isEvent(action: String, block: K?.() -> Unit): Event? {
        this ?: return null
        if (this.action == action) {
            if (this.clazz == String::class.java) block(this.getString() as? K)
            if (this.clazz == Int::class.java) block(this.getInt() as? K)
            if (this.clazz == Boolean::class.java) block(this.getBoolean() as? K)
            if (this.clazz == Serializable::class.java) block(this.getSerializable() as? K)
            if (this.clazz == Parcelable::class.java) block(this.getParcelable() as? K)
        }
        return this
    }

}