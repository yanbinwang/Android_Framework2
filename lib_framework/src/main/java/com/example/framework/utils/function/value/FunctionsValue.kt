package com.example.framework.utils.function.value

import android.content.pm.PackageManager
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
 * 根据全类名加载Class，失败返回null
 */
fun String.loadClass(): Class<*>? {
    return try {
        Class.forName(this)
    } catch (e: ClassNotFoundException) {
        e.printStackTrace()
        null
    }
}

/**
 * 获取【当前类直接声明】的实例字段值（private/protected/public）
 * 1) 仅检索当前类源码直接定义的字段，不会向上查找父类，无法获取父类任何字段
 * 2) 仅支持类实例对象调用；Class 对象调用会直接抛出找不到字段异常
 * # ========== 【三方/AndroidX类反射模板】精准keep成员 ==========
 * # -keepclassmembers class 完整类名 {
 * #     private <fields>;    # 保留全部私有字段
 * #     private <methods>;   # 保留全部私有方法
 * # }
 * Toolbar:
 *  (1) val navBtn = toolbar.getDeclaredFieldValue<ImageButton>("mNavButtonView")
 */
@Suppress("UNCHECKED_CAST")
fun <T> Any?.getDeclaredFieldValue(fieldName: String): T? {
    this ?: return null
    return try {
        val field = this::class.java.getDeclaredField(fieldName)
        field.isAccessible = true
        // 实例字段取值，必须传入类实例对象
        field.get(this) as? T
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

/**
 * 获取【当前类直接声明】的静态字段值（static，private/protected/public）
 * 1) 仅检索当前类源码直接定义的静态字段，不会向上查找父类，无法获取父类任何静态字段
 * 2) 支持两种调用入口：类实例对象 / Class 对象
 * ConstraintLayout:
 *  (1) constraintLayout.getDeclaredStaticField<Boolean>("USE_CONSTRAINTS_HELPER")
 *  (2) ConstraintLayout::class.java.getDeclaredStaticField<Boolean>("USE_CONSTRAINTS_HELPER")
 */
@Suppress("UNCHECKED_CAST")
fun <T> Any?.getDeclaredStaticField(fieldName: String): T? {
    this ?: return null
    return try {
        val targetClass = if (this is Class<*>) this else this::class.java
        val field = targetClass.getDeclaredField(fieldName)
        field.isAccessible = true
        // 静态字段取值固定传null
        field.get(null) as? T
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

/**
 * 调用【当前类直接声明】的实例方法（private/protected/public【可使用 getMethod()】）
 * 1) 仅检索当前实例所属类源码直接定义的实例方法，不会自动向上遍历父类；若要访问父类内部定义的实例方法，需要直接拿到父类 Class 对象另行反射
 * 2) 仅支持类实例对象调用；Class 对象调用会直接抛出找不到方法异常
 * DebuggingUtil:
 *  (1) debuggingUtil.invokeDeclaredInstanceMethod<Unit>("init", Context::class.java to applicationContext, Class::class.java to MainActivity::class.java)
 */
@Suppress("UNCHECKED_CAST")
fun <T> Any?.invokeDeclaredInstanceMethod(methodName: String, vararg params: Pair<Class<*>, Any?>): T? {
    this ?: return null
    return try {
        // 拆分出类型数组、实参数组
        val paramTypes = params.map { it.first }.toTypedArray()
        val args = params.map { it.second }.toTypedArray()
        val method = this::class.java.getDeclaredMethod(methodName, *paramTypes)
        method.isAccessible = true
        // 实例方法执行，传入类实例对象
        method.invoke(this, *args) as? T
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

/**
 * 调用【当前类直接声明】的静态方法（static，private/protected/public）
 * 1) 仅检索 targetClass 源码直接定义的静态方法，不会自动向上遍历父类；若要访问父类内部定义的静态方法，请直接传入【父类的Class对象】调用本API
 * 2) 支持两种调用入口：类实例对象 / Class 对象
 * DebuggingUtil:
 *  (1) DebuggingUtil::class.java.invokeDeclaredStaticMethod<Unit>("init", Context::class.java to applicationContext, Class::class.java to MainActivity::class.java)
 *  (2) val clazz = Class.forName("com.example.debugging.utils.DebuggingUtil")
 *      clazz.invokeDeclaredStaticMethod<Unit>("init", Context::class.java to applicationContext, Class::class.java to MainActivity::class.java)
 */
@Suppress("UNCHECKED_CAST")
fun <T> Any?.invokeDeclaredStaticMethod(methodName: String, vararg params: Pair<Class<*>, Any?>): T? {
    this ?: return null
    return try {
        val paramTypes = params.map { it.first }.toTypedArray()
        val args = params.map { it.second }.toTypedArray()
        val targetClass = if (this is Class<*>) this else this::class.java
        val method = targetClass.getDeclaredMethod(methodName, *paramTypes)
        method.isAccessible = true
        // 静态方法执行，第一个参数固定传null
        method.invoke(null, *args) as? T
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

/**
 * 调用【静态public方法】
 * 1) 使用 getMethod()，只会查找 public 静态方法，包含父类继承来的public静态方法
 * 2) 支持3种调用入口：类实例对象 / Class对象 / 全类名字符串
 * 3) 参数采用 Pair<参数类型, 参数值> 成对绑定，避免类型数组与实参数组长度错位
 * 4) 自动判空：this == null 直接返回 null
 * @param methodName public静态方法名
 * @param params 参数对：Pair<参数Class类型, 参数值>
 * @return 方法返回值，反射失败/找不到方法/类加载失败/参数不匹配返回null
 * DebuggingUtil:
 *  (1) com.xxx.Test".invokeStaticPublicMethod<Int>("getVersion")
 */
@Suppress("UNCHECKED_CAST")
fun <T> Any?.invokeStaticPublicMethod(methodName: String, vararg params: Pair<Class<*>, Any?>): T? {
    this ?: return null
    return try {
        val paramTypes = params.map { it.first }.toTypedArray()
        val args = params.map { it.second }.toTypedArray()
        val targetClass = if (this is Class<*>) this else this::class.java
        val method = targetClass.getMethod(methodName, *paramTypes)
        method.isAccessible = true
        // 静态public方法，invoke传null
        method.invoke(null, *args) as? T
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
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
 * 获取 Android 总运行内存大小 (byte)
 */
fun getTotalMemory(): Long {
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
 * 获取手机 CPU 型号 (AMD Ryzen 9 9955HX 16-Core Processor 这类可读名称)
 * 1) arm64原生设备内核默认无 model name / Processor，会返回空，厂商未魔改则不提供该信息
 * 2) minSdk23 targetSdk37 可用；读取/proc非官方SDK，存在未来被Google限制风险
 */
fun getCpuModelName(): String {
    return try {
        BufferedReader(FileReader("/proc/cpuinfo")).useLines { lines ->
            // 优先取 model name (x86/部分ARM)，其次取 Processor (传统ARM)
            lines
                .firstOrNull {
                    it.startsWith("model name") || it.startsWith("Processor")
                }
                ?.split(":\\s+".toRegex(), limit = 2)
                ?.getOrNull(1)
                ?.trim()
                ?.takeIf { it.isNotBlank() } ?: ""
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
    return try {
        val legacyPaths = arrayOf("/system/bin/su", "/system/xbin/su", "/sbin/su", "/vendor/bin/su", "/data/local/bin/su")
        legacyPaths.any { File(it).exists() }
    } catch (_: Exception) {
        false
    }
}

/**
 * 增强版 Root 检测
 * 此方法为启发式检测，无法覆盖已隐藏包名的 Magisk/KSU/APatch 等现代方案
 * @param packageManager 通过 context(Application Context皆可) 获取
 * @param extraRootPackages 额外的 Root 管理器包名集合（由业务方按需传入）
 * @return true 表示检测到 Root 迹象
 */
fun mobileIsRootEnhanced(packageManager: PackageManager, extraRootPackages: Set<String> = emptySet()): Boolean {
    if (mobileIsRoot()) return true
    val knownPackages = setOf("com.topjohnwu.magisk", "me.weishu.kernelsu", "me.bmax.apatch") + extraRootPackages
    return try {
        packageManager.getInstalledPackages(0).any { it.packageName in knownPackages }
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