package com.example.base.utils.function

import android.app.Activity
import android.app.Service
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Point
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.view.animation.AnimationSet
import android.view.animation.ScaleAnimation
import androidx.annotation.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import java.io.Serializable

//------------------------------------context扩展函数类------------------------------------
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
 * 获取resources中的color
 */
fun Context.color(@ColorRes res: Int) = ContextCompat.getColor(this, res)

/**
 * 获取resources中的drawable
 */
fun Context.drawable(@DrawableRes res: Int) = ContextCompat.getDrawable(this, res)

/**
 * 通过字符串获取drawable下的xml文件
 */
fun Context.drawableId(name: String): Int {
    return try {
        resources.getIdentifier(name, "drawable", packageName)
    } catch (_: Exception) {
        0
    }
}

/**
 * 获取Resources中的String
 */
fun Context.string(@StringRes res: Int): String {
    return try {
        resources.getString(res)
    } catch (ignore: Exception) {
        ""
    }
}

/**
 * 生成View
 */
fun Context.inflate(@LayoutRes res: Int, root: ViewGroup? = null) = LayoutInflater.from(this).inflate(res, root)

fun Context.inflate(@LayoutRes res: Int, root: ViewGroup?, attachToRoot: Boolean) = LayoutInflater.from(this).inflate(res, root, attachToRoot)

fun Context.setPrimaryClip(label: String, text: String) = (getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager).setPrimaryClip(ClipData.newPlainText(label, text))

fun Context.getPrimaryClip(): String {
    val clipboardManager = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    //判断剪切版时候有内容
    if (!clipboardManager.hasPrimaryClip()) return ""
    //获取 text
    return clipboardManager.primaryClip?.getItemAt(0)?.text.toString()
}

/**
 * 开启一个网页
 */
fun Context.openWebsite(url: String) = startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))

/**
 * 拨打电话
 */
fun Context.telPhone(mobile: String) = startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:${mobile}")))

/**
 * 进入动画
 */
fun Context.inAnimation(): AnimationSet {
    val inAnimation = AnimationSet(this, null)
    val alpha = AlphaAnimation(0.0f, 1.0f)
    alpha.duration = 90
    val scale1 = ScaleAnimation(0.8f, 1.05f, 0.8f, 1.05f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f)
    scale1.duration = 135
    val scale2 = ScaleAnimation(1.05f, 0.95f, 1.05f, 0.95f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f)
    scale2.duration = 105
    scale2.startOffset = 135
    val scale3 = ScaleAnimation(0.95f, 1f, 0.95f, 1.0f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f)
    scale3.duration = 60
    scale3.startOffset = 240
    inAnimation.addAnimation(alpha)
    inAnimation.addAnimation(scale1)
    inAnimation.addAnimation(scale2)
    inAnimation.addAnimation(scale3)
    return inAnimation
}

/**
 * 退出动画
 */
fun Context.outAnimation(): AnimationSet {
    val outAnimation = AnimationSet(this, null)
    val alpha = AlphaAnimation(1.0f, 0.0f)
    alpha.duration = 150
    val scale = ScaleAnimation(1.0f, 0.6f, 1.0f, 0.6f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f)
    scale.duration = 150
    outAnimation.addAnimation(alpha)
    outAnimation.addAnimation(scale)
    return outAnimation
}

/**
 * dip转px
 */
fun Context.dip2px(dipValue: Float) = (dipValue * resources.displayMetrics.density + 0.5f).toInt()

/**
 * px转dip
 */
fun Context.px2dip(pxValue: Float) = (pxValue / resources.displayMetrics.density + 0.5f).toInt()

/**
 * 获取屏幕长宽比
 */
fun Context.getScreenRate(): Float {
    val pixel = getScreenMetrics()
    val height = pixel.y.toFloat()
    val width = pixel.x.toFloat()
    return height / width
}

/**
 * 获取屏幕宽度和高度，单位为px
 */
fun Context.getScreenMetrics(): Point {
    val dm = resources.displayMetrics
    val widthScreen = dm.widthPixels
    val heightScreen = dm.heightPixels
    return Point(widthScreen, heightScreen)
}

/**
 * 获取本地的dp值
 */
fun Context.getXmlDef(id: Int): Int {
    val value = TypedValue()
    resources.getValue(id, value, true)
    return TypedValue.complexToFloat(value.data).toInt()
}

/**
 * 页面间取值扩展
 */
fun Activity.intentString(key: String, default: String = "") = intent.getStringExtra(key) ?: default

fun Activity.intentStringNullable(key: String) = intent.getStringExtra(key)

fun Activity.intentInt(key: String, default: Int = 0) = intent.getIntExtra(key, default)

fun Activity.intentDouble(key: String, default: Double = 0.0) = intent.getDoubleExtra(key, default)

fun Activity.intentFloat(key: String, default: Float = 0f) = intent.getFloatExtra(key, default)

fun <T> Activity.intentSerializable(key: String) = intent.getSerializableExtra(key) as? T

fun <T> Activity.intentSerializable(key: String, default: T) = intent.getSerializableExtra(key) as? T ?: default

fun Activity.intentBoolean(key: String, default: Boolean = false) = intent.getBooleanExtra(key, default)

fun Activity.intentParcelable(key: String): Parcelable? {
    return intent.getParcelableExtra(key)
}

fun Fragment.intentString(key: String, default: String = "") = arguments?.getString(key) ?: default

fun Fragment.intentStringNullable(key: String) = arguments?.getString(key)

fun Fragment.intentInt(key: String, default: Int = 0) = arguments?.getInt(key, default)

fun Fragment.intentDouble(key: String, default: Double = 0.0) = arguments?.getDouble(key, default)

fun Fragment.intentFloat(key: String, default: Float = 0f) = arguments?.getFloat(key, default)

fun <T> Fragment.intentSerializable(key: String) = arguments?.getSerializable(key) as? T

fun <T> Fragment.intentSerializable(key: String, default: T) = arguments?.getSerializable(key) as? T ?: default

fun Fragment.intentBoolean(key: String, default: Boolean = false) = arguments?.getBoolean(key, default)

fun Fragment.intentParcelable(key: String): Parcelable? {
    return arguments?.getParcelable(key)
}

/**
 * 可在协程类里传入AppComActivity，然后init{}方法里调取，销毁内部的job
 */
fun Lifecycle?.doOnDestroy(func: () -> Unit) {
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

fun AppCompatActivity?.doOnDestroy(func: () -> Unit) = this?.lifecycle?.doOnDestroy(func)

fun Fragment?.doOnDestroy(func: () -> Unit) = this?.lifecycle?.doOnDestroy(func)

fun LifecycleOwner?.doOnDestroy(func: () -> Unit) = this?.lifecycle?.doOnDestroy(func)