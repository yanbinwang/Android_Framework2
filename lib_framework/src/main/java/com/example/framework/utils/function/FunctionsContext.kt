package com.example.framework.utils.function

import android.annotation.SuppressLint
import android.app.Activity
import android.app.ActivityManager
import android.app.Application
import android.app.Service
import android.content.BroadcastReceiver
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import android.provider.Settings
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
fun Context.color(@ColorRes res: Int) = ContextCompat.getColor(this, res)

/**
 * 获取resources中的drawable
 */
fun Context.drawable(@DrawableRes res: Int) = ContextCompat.getDrawable(this, res)

/**
 * 獲取Typeface字體(res下新建一个font文件夹)
 * ResourcesCompat.getFont(this, R.font.font_semi_bold)
 */
fun Context.font(@FontRes res: Int) = ResourcesCompat.getFont(this, res)

/**
 * 获取Resources中的Dimes
 */
fun Context.dimen(@DimenRes res: Int) = resources.getDimension(res)

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
 * 获取资源文件id
 */
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
 * 生成View
 */
fun Context.inflate(@LayoutRes res: Int, root: ViewGroup? = null): View = LayoutInflater.from(this).inflate(res, root)

fun Context.inflate(@LayoutRes res: Int, root: ViewGroup?, attachToRoot: Boolean): View = LayoutInflater.from(this).inflate(res, root, attachToRoot)

/**
 * 粘贴板操作
 */
fun Context.setPrimaryClip(label: String, text: String) = (getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager)?.setPrimaryClip(ClipData.newPlainText(label, text))

fun Context.getPrimaryClip(): String {
    val clipboardManager = getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
    //判断剪切版时候有内容
    if (!clipboardManager?.hasPrimaryClip().orFalse) return ""
    //获取 text
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
fun Context.sampleMemory(): Long {
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
fun Context.isAdbEnabled() = (Settings.Secure.getInt(contentResolver, Settings.Global.ADB_ENABLED, 0) > 0)

/**
 * 是否安装了XXX应用
 */
fun Context.isAvailable(packageName: String): Boolean {
    return run {
        try {
            packageManager.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES)
            true
        } catch (e: Exception) {
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
val serviceStateMap by lazy { WeakHashMap<Class<*>, Boolean>() }// 服务状态跟踪器（使用 WeakHashMap 避免内存泄漏）

fun Context.isServiceRunning(serviceClass: Class<*>): Boolean {
    val activityManager = getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
    // 1. 检查自维护的服务状态
    val isServiceMarkedRunning = serviceStateMap[serviceClass] ?: false
    // 2. 检查应用进程是否存活（避免进程被杀后状态未更新）
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
 *  获取对应class类页面中intent的消息
 */
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
            is String? -> intent.putExtra(key, value)
            is Bundle? -> intent.putExtra(key, value)
            is IntArray? -> intent.putExtra(key, value)
            is ByteArray? -> intent.putExtra(key, value)
            is CharArray? -> intent.putExtra(key, value)
            is LongArray? -> intent.putExtra(key, value)
            is FloatArray? -> intent.putExtra(key, value)
            is Parcelable? -> intent.putExtra(key, value)
            is ShortArray? -> intent.putExtra(key, value)
            is DoubleArray? -> intent.putExtra(key, value)
            is BooleanArray? -> intent.putExtra(key, value)
            is CharSequence? -> intent.putExtra(key, value)
            is Serializable? -> intent.putExtra(key, value)
        }
    }
    return intent
}

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

/**
 * 页面间取值扩展
 */
fun Activity.intentString(key: String, default: String = "") = intent.getStringExtra(key) ?: default

fun Activity.intentStringNullable(key: String) = intent.getStringExtra(key)

fun Activity.intentInt(key: String, default: Int = 0) = intent.getIntExtra(key, default)

fun Activity.intentDouble(key: String, default: Double = 0.0) = intent.getDoubleExtra(key, default)

fun Activity.intentFloat(key: String, default: Float = 0f) = intent.getFloatExtra(key, default)

fun Activity.intentBoolean(key: String, default: Boolean = false) = intent.getBooleanExtra(key, default)

fun <T : Serializable> Activity.intentSerializable(key: String) = intent.getSerializableExtra(key) as? T

//fun <T : Serializable> Activity.intentSerializable(key: String, default: T) = intent.getSerializableExtra(key) as? T ?: default

fun <T : Parcelable> Activity.intentParcelable(key: String) = intent.getParcelableExtra(key) as? T

fun Fragment.intentString(key: String, default: String = "") = arguments?.getString(key) ?: default

fun Fragment.intentStringNullable(key: String) = arguments?.getString(key)

fun Fragment.intentInt(key: String, default: Int = 0) = arguments?.getInt(key, default)

fun Fragment.intentDouble(key: String, default: Double = 0.0) = arguments?.getDouble(key, default)

fun Fragment.intentFloat(key: String, default: Float = 0f) = arguments?.getFloat(key, default)

fun Fragment.intentBoolean(key: String, default: Boolean = false) = arguments?.getBoolean(key, default)

fun <T : Serializable> Fragment.intentSerializable(key: String) = arguments?.getSerializable(key) as? T

//fun <T : Serializable> Fragment.intentSerializable(key: String, default: T) = arguments?.getSerializable(key) as? T ?: default

fun <T : Parcelable> Fragment.intentParcelable(key: String) = arguments?.getParcelable(key) as? T

/**
 * 页面广播-》（Context.RECEIVER_EXPORTED 表示可以接收应用外部广播，Context.RECEIVER_NOT_EXPORTED 应用内部广播(否则安卓14报错)）
 * mActivity.doOnReceiver(receiver, IntentFilter().apply {
 * addAction(RECEIVER_USB)
 * addAction(RECEIVER_USB_ATTACHED)
 * addAction(RECEIVER_USB_DETACHED)
 * })
 */
@SuppressLint("UnspecifiedRegisterReceiverFlag")
fun Context?.doOnReceiver(owner: LifecycleOwner?, receiver: BroadcastReceiver, intentFilter: IntentFilter, end: () -> Unit = {}) {
    this ?: return
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        registerReceiver(receiver, intentFilter, Context.RECEIVER_EXPORTED)
    } else {
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

fun FragmentActivity?.doOnReceiver(receiver: BroadcastReceiver, intentFilter: IntentFilter, end: () -> Unit = {}) = doOnReceiver(this, receiver, intentFilter, end)

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