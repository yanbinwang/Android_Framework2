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
        if (args == null) args = Bundle()
        args?.putBoolean(action, value)
    }

    /**
     * 带int类型的广播
     */
    constructor(action: String, value: Int) {
        this.action = action
        if (args == null) args = Bundle()
        args?.putInt(action, value)
    }

    /**
     * 带字符串类型的广播
     */
    constructor(action: String, value: String) {
        this.action = action
        if (args == null) args = Bundle()
        args?.putString(action, value)
    }

    /**
     * 带数据类的广播
     */
    constructor(action: String, args: Bundle) {
        this.action = action
        this.args = args
    }

    /**
     * 带对象的广播
     */
    constructor(action: String, any: Serializable) {
        this.action = action
        this.args = Bundle().apply { putSerializable(Extras.BUNDLE_BEAN, any) }
    }

    /**
     * 带对象的广播
     */
    constructor(action: String, any: Parcelable) {
        this.action = action
        this.args = Bundle().apply { putParcelable(Extras.BUNDLE_BEAN, any) }
    }

    /**
     * 获取广播名
     */
    fun getAction() = action

    /**
     * 获取默认布尔值
     */
    fun getBoolean(defaultValue: Boolean): Boolean {
        return if (args == null) defaultValue else args?.getBoolean(action, defaultValue) ?: false
    }

    //获取默认int值
    fun getInt(defaultValue: Int): Int {
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

}
