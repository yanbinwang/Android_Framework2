package com.example.framework.utils.function.value

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.GradientDrawable.OVAL
import android.os.Bundle
import android.os.Looper
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
 * 将任意类型安全转换为 Boolean
 * - Boolean 类型：直接返回
 * - Number 类型：非零为 true，零为 false
 * - CharSequence 类型：委托给 [toSafeBoolean] 处理
 * - 其他类型或 null：返回 default
 */
fun Any?.toBoolean(default: Boolean = false): Boolean {
    return when (this) {
        is Boolean -> this
        is Number -> this.toInt() != 0
        is CharSequence -> this.toSafeBoolean(default)
        null -> default
        else -> default
    }
}

/**
 * 防空转换 Boolean
 * - 空字符串 / "." → 返回 default
 * - 匹配真值集合（"true"/"yes"/"y"/"1"，忽略大小写）→ true
 * - 其他所有非空值 → false
 */
private val TRUE_VALUES = setOf("true", "yes", "y", "1")

fun CharSequence?.toSafeBoolean(default: Boolean = false): Boolean {
    if (this.isNullOrEmpty() || this == ".") return default
    return this.toString().trim().lowercase() in TRUE_VALUES
}

/**
 * 判断某个对象上方是否具备某个注解
 * 1) isAnnotationPresent 不会检查父类/接口上的注解。如果你的注解标在基类 Activity 上，子类调用此方法会返回 false。
 * 2) 若需支持继承，应改用 AnnotationUtils.findAnnotation() (Spring) 或自行遍历 superclass chain
 *
 * if (activity.hasAnnotation(SocketRequest::class.java)) {
 *   SocketEventHelper.checkConnection(forceConnect = true)
 * }
 * //自定义一个注解
 * annotation class SocketRequest
 * @SocketRequest为注解，通过在application中做registerActivityLifecycleCallbacks监听回调，可以找到全局打了这个注解的activity，从而做一定的操作
 */
fun Any?.hasAnnotation(cls: Class<out Annotation>): Boolean {
    this ?: return false
    // isAnnotationPresent 底层走反射，不适合在列表滚动、高频回调中使用。适合在初始化、路由注册、生命周期回调等低频场景
    return this::class.java.isAnnotationPresent(cls)
}

/**
 * 清空 Fragment 缓存
 */
@Suppress("RestrictedApi")
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
private val COLOR_PATTERN = Pattern.compile("^#([0-9A-Fa-f]{3}|[0-9A-Fa-f]{4}|[0-9A-Fa-f]{6}|[0-9A-Fa-f]{8})$")

@ColorInt
fun String?.parseColor(defaultColor: Int = Color.WHITE): Int {
    this ?: return defaultColor
    if (!COLOR_PATTERN.matcher(this).matches()) return defaultColor
    return try {
        toColorInt()
    } catch (_: IllegalArgumentException) {
        defaultColor
    }
}

/**
 * 不指定name，默认返回class命名
 */
fun Class<*>.getSimpleName(name: String? = null): String {
    return name ?: this.simpleName.lowercase(Locale.getDefault())
}

/**
 * 获取正常颜色
 */
@ColorInt
fun ColorStateList.getNormalColor(): Int {
    return defaultColor
}

/**
 * 获取高亮颜色（按下/选中/勾选 都一样）
 */
@ColorInt
fun ColorStateList.getHighLightColor(): Int {
    return getColorForState(intArrayOf(android.R.attr.state_pressed), defaultColor)
}

/**
 * 创建按钮/文本/背景的颜色状态选择器
 * 统一处理：按下、选中、勾选 = 高亮色 | 默认 = 正常色
 */
fun createColorSelector(@ColorInt normal: Int, @ColorInt highLight: Int): ColorStateList {
    val states = arrayOf(
        // 勾选
        intArrayOf(android.R.attr.state_checked),
        // 按下
        intArrayOf(android.R.attr.state_pressed),
        // 选中
        intArrayOf(android.R.attr.state_selected),
        // 默认（所有其他情况）
        intArrayOf()
    )
    val colors = intArrayOf(highLight, highLight, highLight, normal)
    return ColorStateList(states, colors)
}

/**
 * 创建带描边的圆角矩形 Drawable（适配服务器返回的颜色字符串,减少本地背景文件的绘制）
 * @param colorString 背景色字符串（支持 #3/4/6/8 位格式，null 时用 parseColor 默认白色）
 * @param radius 圆角半径（px，默认 0）
 * @param strokeWidth 描边宽度（px，默认 -1 表示不绘制描边）
 * @param strokeColor 描边颜色（ColorInt，默认透明，仅 strokeWidth > 0 时生效）
 * @return 圆角矩形 Drawable
 */
fun createRectangleDrawable(colorString: String, radius: Float = 0f, strokeWidth: Int = -1, @ColorInt strokeColor: Int = Color.TRANSPARENT): Drawable {
    return GradientDrawable().apply {
        setColor(colorString.parseColor())
        cornerRadius = radius
        if (-1 != strokeWidth) {
            setStroke(strokeWidth, strokeColor)
        }
    }
}

/**
 * 创建圆形 Drawable（适配服务器返回的颜色字符串）
 * @param colorString 颜色字符串（支持 #3/4/6/8 位格式，null 时用 parseColor 默认白色）
 * @return 圆形 Drawable
 */
fun createOvalDrawable(colorString: String): Drawable {
    return GradientDrawable().apply {
        shape = OVAL
        setColor(colorString.parseColor())
    }
}

/**
 * 比较两个 Drawable 是否来自同一资源或完全相同（仅支持 API 21+）
 */
fun areDrawablesSame(d1: Drawable?, d2: Drawable?): Boolean {
    // 处理 null 情况（两个都为 null 才相同）
    if (d1 == null && d2 == null) return true
    if (d1 == null || d2 == null) return false
    // 快速比较实例引用：同一个实例
    if (d1 === d2) return true
    // 通过 constantState 对比（同一资源/同一类型的 Drawable 会相同）
    val cs1 = d1.constantState
    val cs2 = d2.constantState
    // 防御性编程：避免 constantState 为 null 时误判
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
//    var memory = 0L
//    try {
//        val localBufferedReader = BufferedReader(FileReader("/proc/meminfo"), 8192)
//        // 系统内存信息文件,读取meminfo第一行，系统总内存大小
//        val arrayOfString = localBufferedReader.readLine().split("\\s+".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
//        // 获得系统总内存，单位是KB
//        val systemMemory = Integer.valueOf(arrayOfString[1]).toSafeInt()
//        // int值乘以1024转换为long类型
//        memory = systemMemory.toSafeLong() * 1024
//        localBufferedReader.close()
//    } catch (e: IOException) {
//        e.printStackTrace()
//    }
//    return memory
    return try {
        BufferedReader(FileReader("/proc/meminfo"), 8192).use { reader ->
            val parts = reader.readLine().split("\\s+".toRegex())
            parts.getOrNull(1)?.toLongOrNull()?.times(1024) ?: 0L
        }
    } catch (e: Exception) {
        e.printStackTrace()
        0L
    }
}

/**
 * 获取手机cpu信息-报错或获取失败显示""
 */
fun getCpuInfo(): String {
//    return try {
//        val localBufferedReader = BufferedReader(FileReader("/proc/cpuinfo"))
//        val info = localBufferedReader.readLine().split(":\\s+".toRegex(), 2).toTypedArray()[1]
//        localBufferedReader.close()
//        return if ("0" == info || info.isEmpty()) "" else info
//    } catch (e: Exception) {
//        e.printStackTrace()
//        ""
//    }
    return try {
        BufferedReader(FileReader("/proc/cpuinfo")).use { reader ->
            val line = reader.readLine() ?: return@use ""
            line.split(":\\s+".toRegex(), limit = 2).getOrNull(1)?.takeIf { it.isNotEmpty() && it != "0" } ?: ""
        }
    } catch (e: Exception) {
        e.printStackTrace()
        ""
    }
}

/**
 * 检测设备是否已 Root
 * 综合判断：特征文件 + su 命令可用性 + 已知 Root 管理器包名
 * 注意：此方法仅为启发式检测，无法做到 100% 准确，且可能被 SELinux/沙箱拦截
 */
fun mobileIsRoot(): Boolean {
//    try {
//        for (element in arrayOf("/system/bin/", "/system/xbin/", "/system/sbin/", "/sbin/", "/vendor/bin/")) {
//            if (File(element + "su").exists()) return true
//        }
//    } catch (e: Exception) {
//        e.printStackTrace()
//    }
//    return false
    return try {
        // 传统路径检测（兼容老设备）
        val legacyPaths = arrayOf(
            "/system/bin/su", "/system/xbin/su", "/system/sbin/su",
            "/sbin/su", "/vendor/bin/su", "/data/local/xbin/su",
            "/data/local/bin/su"
        )
        if (legacyPaths.any { File(it).exists() }) return true

        // 尝试执行 which su（Magisk 等方案通常能响应）
        val process = Runtime.getRuntime().exec(arrayOf("which", "su"))
        val result = process.inputStream.bufferedReader().use { it.readText() }
        process.waitFor()
        result.isNotBlank()
    } catch (_: Exception) {
        // SecurityException / IOException 等均视为未 Root
        false
    }
}

/**
 * 增强版 Root 检测
 * 注意：此方法为启发式检测，无法覆盖已隐藏包名的 Magisk/KSU/APatch 等现代方案
 * @param context Application Context 即可
 * @param extraRootPackages 额外的 Root 管理器包名集合（由业务方按需传入）
 * @return true 表示检测到 Root 迹象
 */
fun mobileIsRootEnhanced(context: Context, extraRootPackages: Set<String> = emptySet()): Boolean {
    if (mobileIsRoot()) return true
    val knownPackages = setOf("com.topjohnwu.magisk", "me.weishu.kernelsu", "me.bmax.apatch") + extraRootPackages
    return try {
        context.packageManager.getInstalledPackages(0).any { it.packageName in knownPackages }
    } catch (_: Exception) {
        false
    }
}

/**
 *  fun init() = frag.execute {
 *      //...
 *  }
 */
inline fun <T> T.execute(block: T.() -> Unit) = apply(block)