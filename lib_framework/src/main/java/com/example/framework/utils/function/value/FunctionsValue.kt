package com.example.framework.utils.function.value

import android.graphics.Color
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.GradientDrawable.OVAL
import android.os.Bundle
import android.os.Looper
import android.view.View
import androidx.annotation.ColorInt
import androidx.core.graphics.toColorInt
import com.example.framework.BuildConfig
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.io.IOException
import java.util.Locale
import java.util.regex.Pattern

//------------------------------------方法工具类------------------------------------
/**
 * 当前是否是主线程
 */
val isMainThread get() = Looper.getMainLooper() == Looper.myLooper()

/**
 * 是否是debug包
 */
val isDebug get() = BuildConfig.DEBUG

/**
 * Boolean防空
 */
val Boolean?.orFalse get() = this ?: false

/**
 * Boolean防空
 */
val Boolean?.orTrue get() = this ?: true

/**
 * 转Boolean
 */
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
 * 清空fragment缓存
 */
fun Bundle?.clearFragmentSavedState() {
    this ?: return
    remove("android:support:fragments")
    remove("android:fragments")
}

/**
 * 安全解析颜色字符串为 [ColorInt]，支持 null 处理和格式验证
 * @param defaultColor 非法格式或 null 时使用的默认颜色（默认值：白色 #FFFFFF）
 * @return 解析后的颜色值（符合 [ColorInt] 规范的 32 位 ARGB 整数）
 */
@ColorInt
fun String?.parseColor(defaultColor: Int = Color.WHITE): Int {
    return this?.let { colorString ->
        val colorPattern = Pattern.compile("^#([0-9A-Fa-f]{3}|[0-9A-Fa-f]{4}|[0-9A-Fa-f]{6}|[0-9A-Fa-f]{8})$")
        if (colorPattern.matcher(colorString).matches()) {
            try {
                colorString.toColorInt()
            } catch (_: IllegalArgumentException) {
                defaultColor
            }
        } else {
            defaultColor
        }
    } ?: defaultColor
}

/**
 * 不指定name，默认返回class命名
 */
fun Class<*>.getSimpleName(name: String? = null): String {
    return name ?: this.simpleName.lowercase(Locale.getDefault())
}

/**
 * 减少本地背景文件的绘制，直接代码绘制
 * colorString 颜色字符 -> "#cf111111"
 * radius 圆角 -> 传入X.ptFloat,代码添加一个对应圆角的背景
 */
fun createCornerDrawable(colorString: String, radius: Float = 0f): Drawable {
    return GradientDrawable().apply {
        setColor(colorString.parseColor())
        cornerRadius = radius
    }
}

fun createOvalDrawable(colorString: String): Drawable {
    return GradientDrawable().apply {
        shape = OVAL
        setColor(colorString.parseColor())
    }
}

/**
 * 比较两个 Drawable 是否来自同一资源或完全相同（仅支持 API 21+）
 * 1. 先检查实例引用是否相同
 * 2. 再检查 constantState
 */
fun areDrawablesSame(d1: Drawable?, d2: Drawable?): Boolean {
    // 处理 null 情况
    if (d1 == null && d2 == null) return true
    if (d1 == null || d2 == null) return false
    // 快速比较实例引用
    if (d1 === d2) return true
    // 使用 constantState 比较
    val cs1 = d1.constantState
    val cs2 = d2.constantState
    // 防御性编程：处理 constantState 为 null 的罕见情况
    return when {
        cs1 == null && cs2 == null -> false
        cs1 == null || cs2 == null -> false
        else -> cs1 == cs2
    }
}

/**
 * 获取android总运行内存大小(byte)
 */
fun getMemInfo(): Long {
    var memory = 0L
    try {
        val localBufferedReader = BufferedReader(FileReader("/proc/meminfo"), 8192)
        //系统内存信息文件,读取meminfo第一行，系统总内存大小
        val arrayOfString = localBufferedReader.readLine().split("\\s+".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        //获得系统总内存，单位是KB
        val systemMemory = Integer.valueOf(arrayOfString[1]).toSafeInt()
        //int值乘以1024转换为long类型
        memory = systemMemory.toSafeLong() * 1024
        localBufferedReader.close()
    } catch (e: IOException) {
        e.printStackTrace()
    }
    return memory
}

/**
 * 获取手机cpu信息-报错或获取失败显示""
 */
fun getCpuInfo(): String {
    return try {
        val localBufferedReader = BufferedReader(FileReader("/proc/cpuinfo"))
        val info = localBufferedReader.readLine().split(":\\s+".toRegex(), 2).toTypedArray()[1]
        localBufferedReader.close()
        return if ("0" == info || info.isEmpty()) "" else info
    } catch (e: Exception) {
        e.printStackTrace()
        ""
    }
}

/**
 * 是否Root-报错或获取失败都为未Root
 */
fun mobileIsRoot(): Boolean {
    try {
        for (element in arrayOf("/system/bin/", "/system/xbin/", "/system/sbin/", "/sbin/", "/vendor/bin/")) {
            if (File(element + "su").exists()) return true
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return false
}

/**
 *  fun init() = frag.execute {
 *      //...
 *  }
 */
inline fun <T> T.execute(block: T.() -> Unit) = apply(block)