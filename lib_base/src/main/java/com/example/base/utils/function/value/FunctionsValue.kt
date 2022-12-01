package com.example.base.utils.function.value

import android.graphics.Color
import android.os.Looper
import androidx.annotation.ColorInt

//------------------------------------方法工具类------------------------------------
/**
 * Boolean防空
 * */
val Boolean?.orFalse get() = this ?: false

/**
 * Boolean防空
 * */
val Boolean?.orTrue get() = this ?: true

/**
 * 转Boolean
 * */
fun Any?.toBoolean(default: Boolean = false) = this as? Boolean ?: default

/**
 * 防空转换Boolean
 */
fun CharSequence?.toSafeBoolean(default: Boolean = false): Boolean {
    if (this.isNullOrEmpty() || this == ".") return default
    return try {
        this.toString().toBoolean()
    } catch (e: Exception) {
        default
    }
}

/**
 * 当前是否是主线程
 */
val isMainThread get() = Looper.getMainLooper() == Looper.myLooper()

/**
 * 判断某个对象上方是否具备某个注解
 * if (activity.hasAnnotation(SocketRequest::class.java)) {
 * SocketEventHelper.checkConnection(forceConnect = true)
 * }
 * //自定义一个注解
 * annotation class SocketRequest
 * @SocketRequest为注解，通过在application中做registerActivityLifecycleCallbacks监听回调，可以找到全局打了这个注解的activity，从而做一定的操作
 */
fun Any?.hasAnnotation(cls: Class<out Annotation>): Boolean {
    this ?: return false
    return this::class.java.isAnnotationPresent(cls)
}

/**
 * 获取Color String中的color
 * eg: "#ffffff"
 */
@ColorInt
fun color(color: String?) = Color.parseColor(color ?: "#ffffff")