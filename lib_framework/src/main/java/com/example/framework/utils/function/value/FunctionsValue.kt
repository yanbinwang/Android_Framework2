package com.example.framework.utils.function.value

import android.graphics.Color
import android.os.Bundle
import android.os.Looper
import androidx.annotation.ColorInt
import com.example.framework.BuildConfig
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.io.IOException
import java.util.Locale

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
 * 获取Color String中的color
 * eg: "#ffffff"
 */
@ColorInt
fun String?.parseColor() = Color.parseColor(this ?: "#ffffff")

/**
 * 不指定name，默认返回class命名
 */
fun Class<*>.getSimpleName(name: String? = null): String {
    return name ?: this.simpleName.lowercase(Locale.getDefault())
}

/**
 * 获取android总运行内存大小(byte)
 */
fun getTotalMemory(): Long {
    //系统内存信息文件
    val infoStr: String
    val arrayOfString: Array<String>
    var memory = 0L
    try {
        val localFileReader = FileReader("/proc/meminfo")
        val localBufferedReader = BufferedReader(localFileReader, 8192)
        //读取meminfo第一行，系统总内存大小
        infoStr = localBufferedReader.readLine()
        arrayOfString = infoStr.split("\\s+".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        //获得系统总内存，单位是KB
        val systemMemory = Integer.valueOf(arrayOfString[1]).toSafeInt()
        //int值乘以1024转换为long类型
        memory = systemMemory.toSafeLong() * 1024
        localBufferedReader.close()
    } catch (_: IOException) {
    }
    return memory
}

/**
 * 获取手机cpu信息-报错或获取失败显示""
 */
fun getCpuInfo(): String {
    try {
        val result = BufferedReader(FileReader("/proc/cpuinfo")).readLine().split(":\\s+".toRegex(), 2).toTypedArray()[1]
        return if ("0" == result || result.isEmpty()) "" else result
    } catch (_: Exception) {
    }
    return ""
}

/**
 * 是否Root-报错或获取失败都为未Root
 */
fun isRoot(): Boolean {
    var file: File
    val paths = arrayOf("/system/bin/", "/system/xbin/", "/system/sbin/", "/sbin/", "/vendor/bin/")
    try {
        for (element in paths) {
            file = File(element + "su")
            if (file.exists()) return true
        }
    } catch (_: Exception) {
    }
    return false
}

/**
 *  fun init() = frag.execute {
 *      //...
 *  }
 */
inline fun <T> T.execute(block: T.() -> Unit) = apply(block)