package com.example.framework.utils.function

import android.annotation.SuppressLint
import android.app.Activity
import android.app.ActivityManager
import android.app.Application
import android.app.Service
import android.content.BroadcastReceiver
import android.content.ClipData
import android.content.ClipboardManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import android.provider.Settings
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.ColorRes
import androidx.annotation.DimenRes
import androidx.annotation.DrawableRes
import androidx.annotation.FontRes
import androidx.annotation.LayoutRes
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleService
import com.example.framework.utils.function.value.orFalse
import com.example.framework.utils.function.value.orZero
import com.example.framework.utils.function.value.toSafeLong
import java.io.Serializable
import java.util.WeakHashMap


//------------------------------------context扩展函数类------------------------------------
/**
 * 获取resources中的color
 */
fun Context.color(@ColorRes res: Int): Int {
    return ContextCompat.getColor(this, res)
}

/**
 * 获取resources中的drawable
 */
fun Context.drawable(@DrawableRes res: Int): Drawable? {
    return ContextCompat.getDrawable(this, res)
}

/**
 * 獲取Typeface字體(res下新建一个font文件夹)
 * ResourcesCompat.getFont(this, R.font.font_semi_bold)
 */
fun Context.font(@FontRes res: Int): Typeface? {
    return ResourcesCompat.getFont(this, res)
}

/**
 * 获取Resources中的Dimes
 */
fun Context.dimen(@DimenRes res: Int): Float {
    return try {
        resources.getDimension(res)
    } catch (e: Exception) {
        e.printStackTrace()
        0f
    }
}

/**
 * 获取Resources中的String
 */
fun Context.string(@StringRes res: Int): String {
    return try {
        resources.getString(res)
    } catch (e: Exception) {
        e.printStackTrace()
        ""
    }
}

/**
 * 生成View
 */
fun Context.inflate(@LayoutRes res: Int, root: ViewGroup? = null): View {
    return LayoutInflater.from(this).inflate(res, root)
}

fun Context.inflate(@LayoutRes res: Int, root: ViewGroup?, attachToRoot: Boolean): View {
    return LayoutInflater.from(this).inflate(res, root, attachToRoot)
}

/**
 * 获取资源文件id
 */
@SuppressLint("ResourceType")
fun Context.defTypeId(name: String, defType: String): Int {
    return try {
        resources.getIdentifier(name, defType, packageName)
    } catch (e: Exception) {
        e.printStackTrace()
        0
    }
}

/**
 * 通过字符串获取drawable下的xml文件
 */
fun Context.defTypeDrawable(name: String): Drawable? {
    return drawable(defTypeId(name, "drawable"))
}

/**
 * 通过字符串获取mipmap下的图片文件
 */
fun Context.defTypeMipmap(name: String): Drawable? {
    return drawable(defTypeId(name, "mipmap"))
}

/**
 * 获取drawable下指定类型的xml文件 (LayerDrawable,BitmapDrawable,ColorDrawable,VectorDrawable等)
 */
inline fun <reified T : Drawable> Context?.getTypedDrawable(@DrawableRes res: Int): T? {
    this ?: return null
    val drawable = ResourcesCompat.getDrawable(resources, res, theme)
    return drawable as? T
}

/**
 * 粘贴板操作
 */
fun Context.setPrimaryClip(label: String, text: String) {
    (getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager)?.setPrimaryClip(ClipData.newPlainText(label, text))
}

fun Context.getPrimaryClip(): String {
    val clipboardManager = getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
    // 判断剪切版时候有内容
    if (!clipboardManager?.hasPrimaryClip().orFalse) return ""
    // 获取 text
    return clipboardManager?.primaryClip?.getItemAt(0)?.text.toString()
}

/**
 *  获取android当前可用运行内存大小(byte)
 */
fun Context.getAvailMemory(): Long {
    val manager = getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
    val memoryInfo = ActivityManager.MemoryInfo()
    manager?.getMemoryInfo(memoryInfo)
    return memoryInfo.availMem
}

/**
 * 获取当前应用使用的内存大小(byte)
 */
fun Context.getSampleMemory(): Long {
    val activityManager = getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
    var memory = 0L
    try {
        val memInfo = activityManager?.getProcessMemoryInfo(intArrayOf(android.os.Process.myPid()));
        if (memInfo?.size.orZero > 0) {
            memInfo ?: return 0
            val totalPss = memInfo[0].totalPss
            if (totalPss >= 0) {
                memory = totalPss.toSafeLong()
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return memory * 1024
}

/**
 * 判断手机是否开启开发者模式
 */
fun Context.isAdbEnabled(): Boolean {
    return Settings.Secure.getInt(contentResolver, Settings.Global.ADB_ENABLED, 0) > 0
}

/**
 * 是否安装了XXX应用
 */
fun Context.isAvailable(packageName: String): Boolean {
    return run {
        try {
            packageManager.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }.orFalse
}

/**
 * 开启服务
 */
fun Context.startService(cls: Class<out Service>, vararg pairs: Pair<String, Any?>) {
    startService(getIntent(cls, *pairs))
}

fun Context.startForegroundService(cls: Class<out Service>, vararg pairs: Pair<String, Any?>) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        startForegroundService(getIntent(cls, *pairs))
    } else {
        startService(getIntent(cls, *pairs))
    }
}

/**
 * 停止服务
 */
fun Context.stopService(cls: Class<out Service>) {
    stopService(getIntent(cls))
}

/**
 * 检测服务是否正在运行
 */
val serviceStateMap by lazy { WeakHashMap<Class<*>, Boolean>() } // 服务状态跟踪器（使用 WeakHashMap 避免内存泄漏）

fun Context.isServiceRunning(serviceClass: Class<*>): Boolean {
    val activityManager = getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
    // 检查自维护的服务状态
    val isServiceMarkedRunning = serviceStateMap[serviceClass] ?: false
    // 检查应用进程是否存活（避免进程被杀后状态未更新）
    val isProcessAlive = activityManager?.runningAppProcesses?.any { processInfo ->
        processInfo.uid == applicationInfo.uid &&
                processInfo.processName == packageName &&
                processInfo.importance <= ActivityManager.RunningAppProcessInfo.IMPORTANCE_VISIBLE
    } ?: false
    return isServiceMarkedRunning && isProcessAlive
}

/**
 * 扩展 LifecycleService 自动管理状态,让服务继承 TrackableLifecycleService
 */
abstract class TrackableLifecycleService : LifecycleService() {
    override fun onCreate() {
        super.onCreate()
        serviceStateMap[this::class.java] = true
    }

    override fun onDestroy() {
        super.onDestroy()
//        serviceStateMap[this::class.java] = false
        serviceStateMap.remove(this::class.java)
    }
}

/**
 * 辅助服务是否开启
 * // 检查权限是否开启
 * if (!isAccessibilityServiceEnabled(MyAccessibilityService::class.java)) {
 *     // 跳转到无障碍设置页面(pullUpAccessibility())
 *     val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
 *     startActivity(intent)
 * }
 */
fun Context?.isAccessibilityServiceEnabled(service: Class<*>): Boolean {
    this ?: return false
    val enabledServices = Settings.Secure.getString(contentResolver, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES) ?: return false
    val splitter = TextUtils.SimpleStringSplitter(':')
    splitter.setString(enabledServices)
    while (splitter.hasNext()) {
        val componentName = splitter.next()
        val serviceComponent = ComponentName(this, service)
        if (componentName == serviceComponent.flattenToString()) {
            return true
        }
    }
    return false
}

/**
 *  获取对应Class类页面中Intent的消息
 */
fun Context.startActivity(cls: Class<out Activity>, vararg pairs: Pair<String, Any?>) {
    startActivity(getIntent(cls, *pairs).apply {
        if (this@startActivity is Application) {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
    })
}

fun Activity.startActivityForResult(cls: Class<out Activity>, requestCode: Int, vararg pairs: Pair<String, Any?>) {
    startActivityForResult(getIntent(cls, *pairs), requestCode)
}

fun Context.getIntent(cls: Class<out Context>, vararg pairs: Pair<String, Any?>): Intent {
    val intent = Intent(this, cls)
    pairs.forEach {
        val key = it.first
        when (val value = it.second) {
            is Int -> intent.putExtra(key, value)
            is Byte -> intent.putExtra(key, value)
            is Char -> intent.putExtra(key, value)
            is Long -> intent.putExtra(key, value)
            is Float -> intent.putExtra(key, value)
            is Short -> intent.putExtra(key, value)
            is Double -> intent.putExtra(key, value)
            is Boolean -> intent.putExtra(key, value)
            is String -> intent.putExtra(key, value)
            is Bundle -> intent.putExtra(key, value)
            is IntArray -> intent.putExtra(key, value)
            is ByteArray -> intent.putExtra(key, value)
            is CharArray -> intent.putExtra(key, value)
            is LongArray -> intent.putExtra(key, value)
            is FloatArray -> intent.putExtra(key, value)
            is Parcelable -> intent.putExtra(key, value)
            is ShortArray -> intent.putExtra(key, value)
            is DoubleArray -> intent.putExtra(key, value)
            is BooleanArray -> intent.putExtra(key, value)
            is CharSequence -> intent.putExtra(key, value)
            is Serializable -> intent.putExtra(key, value)
        }
    }
    return intent
}

fun Activity.setResult(resultCode: Int, vararg pairs: Pair<String, Any?>) {
    val intent = Intent()
    pairs.forEach {
        val key = it.first
        when (val value = it.second) {
            is Int -> intent.putExtra(key, value)
            is Byte -> intent.putExtra(key, value)
            is Char -> intent.putExtra(key, value)
            is Long -> intent.putExtra(key, value)
            is Float -> intent.putExtra(key, value)
            is Short -> intent.putExtra(key, value)
            is Double -> intent.putExtra(key, value)
            is Boolean -> intent.putExtra(key, value)
            is String -> intent.putExtra(key, value)
            is Bundle -> intent.putExtra(key, value)
            is IntArray -> intent.putExtra(key, value)
            is ByteArray -> intent.putExtra(key, value)
            is CharArray -> intent.putExtra(key, value)
            is LongArray -> intent.putExtra(key, value)
            is FloatArray -> intent.putExtra(key, value)
            is Parcelable -> intent.putExtra(key, value)
            is ShortArray -> intent.putExtra(key, value)
            is DoubleArray -> intent.putExtra(key, value)
            is BooleanArray -> intent.putExtra(key, value)
            is CharSequence -> intent.putExtra(key, value)
            is Serializable -> intent.putExtra(key, value)
        }
    }
    setResult(resultCode, intent)
}

/**
 * 页面间取值扩展
 * 1) intent 本身不为空，但 intent.extras 可能为 null, 没有传递参数时，extras 就是 null
 * 2) inline：编译期把函数代码直接粘贴到调用处，省掉函数调用开销。
 *    reified：靠 inline 帮忙，保留泛型真实类型，运行时不擦除。
 */
fun Activity.intentString(key: String, default: String = ""): String {
    return intent.extras?.getString(key) ?: default
}

fun Activity.intentInt(key: String, default: Int = 0): Int {
    return intent.extras?.getInt(key, default) ?: default
}

fun Activity.intentLong(key: String, default: Long = 0L): Long {
    return intent.extras?.getLong(key, default) ?: default
}

fun Activity.intentFloat(key: String, default: Float = 0f): Float {
    return intent.extras?.getFloat(key, default) ?: default
}

fun Activity.intentDouble(key: String, default: Double = 0.0): Double {
    return intent.extras?.getDouble(key, default) ?: default
}

fun Activity.intentBoolean(key: String, default: Boolean = false): Boolean {
    return intent.extras?.getBoolean(key, default) ?: default
}

inline fun <reified T : Serializable> Activity.intentSerializable(key: String): T? {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        intent.extras?.getSerializable(key, T::class.java)
    } else {
        intent.extras?.getSerializable(key) as? T
    }
}

inline fun <reified T : Serializable> Activity.intentSerializableArrayList(name: String): ArrayList<T>? {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        intent.extras?.getSerializable(name, ArrayList::class.java)
    } else {
        intent.extras?.getSerializable(name)
    } as? ArrayList<T>
}

inline fun <reified T : Parcelable> Activity.intentParcelable(key: String): T? {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        intent.extras?.getParcelable(key, T::class.java)
    } else {
        intent.extras?.getParcelable(key) as? T
    }
}

inline fun <reified T : Parcelable> Activity.intentParcelableArrayList(name: String): ArrayList<T>? {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        intent.extras?.getParcelableArrayList(name, T::class.java)
    } else {
        intent.extras?.getParcelableArrayList(name)
    }
}

fun Fragment.intentString(key: String, default: String = ""): String {
    return arguments?.getString(key) ?: default
}

fun Fragment.intentInt(key: String, default: Int = 0): Int {
    return arguments?.getInt(key, default) ?: default
}

fun Fragment.intentLong(key: String, default: Long = 0L): Long {
    return arguments?.getLong(key, default) ?: default
}

fun Fragment.intentFloat(key: String, default: Float = 0f): Float {
    return arguments?.getFloat(key, default) ?: default
}

fun Fragment.intentDouble(key: String, default: Double = 0.0): Double {
    return arguments?.getDouble(key, default) ?: default
}

fun Fragment.intentBoolean(key: String, default: Boolean = false): Boolean {
    return arguments?.getBoolean(key, default) ?: default
}

inline fun <reified T : Serializable> Fragment.intentSerializable(key: String): T? {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        arguments?.getSerializable(key, T::class.java)
    } else {
        arguments?.getSerializable(key) as? T
    }
}

inline fun <reified T : Serializable> Fragment.intentSerializableArrayList(name: String): ArrayList<T>? {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        arguments?.getSerializable(name, ArrayList::class.java)
    } else {
        arguments?.getSerializable(name)
    } as? ArrayList<T>
}

inline fun <reified T : Parcelable> Fragment.intentParcelable(key: String): T? {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        arguments?.getParcelable(key, T::class.java)
    } else {
        arguments?.getParcelable(key)
    }
}

inline fun <reified T : Parcelable> Fragment.intentParcelableArrayList(name: String): ArrayList<T>? {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        arguments?.getParcelableArrayList(name, T::class.java)
    } else {
        arguments?.getParcelableArrayList(name)
    }
}

/**
 * 页面广播
 * 1) RECEIVER_EXPORTED 和 RECEIVER_NOT_EXPORTED 是 Android 13+ 注册广播时唯一需要选择的两个参数，没有其他常用替代值，否则报错 IllegalArgumentException
 * 2) Context.RECEIVER_EXPORTED -> 表示可以接收应用外部广播
 *    Context.RECEIVER_NOT_EXPORTED -> 应用内部广播
 * 3) Android 13+ 要求显式设置 android:exported="true/false"，无默认；
 *    Android 13- 默认 android:exported="true"（等价于 RECEIVER_EXPORTED）
 * 4) 使用
 * mActivity.doOnReceiver(receiver, IntentFilter().apply {
 *   addAction(RECEIVER_USB)
 *   addAction(RECEIVER_USB_ATTACHED)
 *   addAction(RECEIVER_USB_DETACHED)
 * })
 */
@SuppressLint("UnspecifiedRegisterReceiverFlag")
fun Context?.doOnReceiver(owner: LifecycleOwner?, receiver: BroadcastReceiver, intentFilter: IntentFilter, isExported: Boolean = true, end: () -> Unit = {}) {
    this ?: return
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        // 13+ 必须显式指定，二选一
        val flag = if (isExported) Context.RECEIVER_EXPORTED else Context.RECEIVER_NOT_EXPORTED
        registerReceiver(receiver, intentFilter, flag)
    } else {
        // 13- 默认导出，如需不导出需在清单文件配置 android:exported="false"
        registerReceiver(receiver, intentFilter)
    }
    owner.doOnDestroy {
        try {
            unregisterReceiver(receiver)
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            end()
        }
    }
}

fun FragmentActivity?.doOnReceiver(receiver: BroadcastReceiver, intentFilter: IntentFilter, isExported: Boolean = true, end: () -> Unit = {}) {
    doOnReceiver(this, receiver, intentFilter, isExported, end)
}

/**
 * 可在协程类里传入AppComActivity，然后init{}方法里调取，销毁内部的job
 */
inline fun Lifecycle?.doOnDestroy(crossinline func: () -> Unit) {
    this ?: return
    addObserver(object : LifecycleEventObserver {
        override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
            when (event) {
                Lifecycle.Event.ON_DESTROY -> {
                    func()
                    source.lifecycle.removeObserver(this)
                }
                else -> {}
            }
        }
    })
}

inline fun AppCompatActivity?.doOnDestroy(crossinline func: () -> Unit) = this?.lifecycle?.doOnDestroy(func)

inline fun Fragment?.doOnDestroy(crossinline func: () -> Unit) = this?.lifecycle?.doOnDestroy(func)

inline fun LifecycleOwner?.doOnDestroy(crossinline func: () -> Unit) = this?.lifecycle?.doOnDestroy(func)

inline fun ViewDataBinding?.doOnDestroy(crossinline func: () -> Unit) = this?.lifecycleOwner?.doOnDestroy(func)