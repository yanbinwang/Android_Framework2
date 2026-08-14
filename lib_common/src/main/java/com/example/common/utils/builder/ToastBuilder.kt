package com.example.common.utils.builder

import android.content.Context
import android.os.Looper
import android.view.Gravity
import android.widget.Toast
import androidx.annotation.DrawableRes
import com.example.common.BaseApplication
import com.example.common.R
import com.example.common.databinding.ViewToastImageStyleBinding
import com.example.common.utils.builder.ToastBuilder.showImageToast
import com.example.common.utils.function.setPrimaryClip
import com.example.common.utils.i18n.i18String
import com.example.framework.utils.function.inflate
import java.lang.ref.WeakReference

/**
 * 全局提示框定制
 */
@Suppress("UNCHECKED_CAST")
object ToastBuilder {
    /**
     * 全局 Context
     */
    private val appContext get() = BaseApplication.instance.applicationContext
    /**
     * 弱引用 Toast
     */
    private var currentToast: WeakReference<Toast>? = null

    /**
     * 传入引用 String 格式的 Toast
     * ToastBuilder.short(R.string.homeRecommendedQuestsReceiveSuccess) { resId, length ->
     *   val toast = Toast.makeText(MyApplication.instance, null, length)
     *   toast?.setGravity(Gravity.CENTER, 0, 0)
     *   toast?.duration = length
     *   val view = BaseApplication.instance.inflate(R.layout.toast_home_quest_success)
     *   view.imgIcon.setImageResource(R.mipmap.icon_home_quest_dialog_coupon)
     *   view.txtTitle.setI18nRes(resId)
     *   view.txtAmount.text = "$" + bean.rewardNum
     *   toast?.view = view
     *   toast
     * }
     */
    private var defaultResBuilder: (resId: Int, length: Int) -> Toast = { resId, length ->
        val toast = Toast.makeText(appContext, null, length)
        toast.setText(resId)
        toast
    }

    /**
     * 传入文字的 Toast
     */
    private var defaultTextBuilder: (message: String, length: Int) -> Toast = { message, length ->
        val toast = Toast.makeText(appContext, null, length)
        toast.setText(message)
        toast
    }

    /**
     * Application 中初始化全局的 Toast
     * 部分手機定制導致顯示不全，樣式不統一，故而再重寫一次，統一樣式
     */
    fun setResToastBuilder(builder: (resId: Int, length: Int) -> Toast) {
        defaultResBuilder = builder
    }

    fun setTextToastBuilder(builder: (message: String, length: Int) -> Toast) {
        defaultTextBuilder = builder
    }

    /**
     * 全局调取 Toast 方法
     */
    fun short(resId: Int, toastBuilder: ((resId: Int, length: Int) -> Toast) = this.defaultResBuilder) {
        showToast(Toast.LENGTH_SHORT, resId) { input, len ->
            (toastBuilder as? (Any, Int) -> Toast)?.invoke(input, len)
        }
    }

    fun short(message: String, toastBuilder: ((message: String, length: Int) -> Toast) = this.defaultTextBuilder) {
        showToast(Toast.LENGTH_SHORT, message) { input, len ->
            (toastBuilder as? (Any, Int) -> Toast)?.invoke(input, len)
        }
    }

    fun long(resId: Int, toastBuilder: ((resId: Int, length: Int) -> Toast) = this.defaultResBuilder) {
        showToast(Toast.LENGTH_LONG, resId) { input, len ->
            (toastBuilder as? (Any, Int) -> Toast)?.invoke(input, len)
        }
    }

    fun long(message: String, toastBuilder: ((message: String, length: Int) -> Toast) = this.defaultTextBuilder) {
        showToast(Toast.LENGTH_LONG, message) { input, len ->
            (toastBuilder as? (Any, Int) -> Toast)?.invoke(input, len)
        }
    }

    /**
     * 显示 Toast 的公共方法
     */
    private fun showToast(length: Int, input: Any, builder: (Any, Int) -> Toast?) {
        if (Looper.getMainLooper() != Looper.myLooper()) return
        if ((input is Int && input == -1) || (input is String && input.isEmpty())) return
        cancelToast()
        builder(input, length)?.apply {
            currentToast = WeakReference(this)
            show()
        }
    }

    /**
     * 自定义 Toast 的提示 View
     */
    fun showCustom(length: Int = Toast.LENGTH_SHORT, customBuilder: (Context, Toast) -> Unit) {
        if (Looper.getMainLooper() != Looper.myLooper()) return
        cancelToast()
        val toast = Toast.makeText(appContext, "", length)
        currentToast = WeakReference(toast)
        customBuilder(appContext, toast)
        toast.show()
    }

    /**
     * 设置自定义 Toast 提示 View
     */
    fun showImageToast(@DrawableRes resId: Int, message: String) {
        showCustom { context, toast ->
            toast.setGravity(Gravity.CENTER, 0, 0)
            toast.duration = Toast.LENGTH_SHORT
            val binding = ViewToastImageStyleBinding.bind(context.inflate(R.layout.view_toast_image_style))
            binding.ivType.setImageResource(resId)
            binding.tvLabel.text = message
            toast.view = binding.root
        }
    }

    /**
     * 取消当前的 Toast
     */
    fun cancelToast() {
        currentToast?.get()?.cancel()
    }

}

/**
 * string地址/文字引用扩展
 */
fun Int?.shortToast() {
    this ?: return
    ToastBuilder.short(this)
}

fun String?.shortToast() {
    this ?: return
    ToastBuilder.short(this)
}

/**
 * 带提示的复制
 */
fun String?.copy(label: String = "Label") {
    this ?: return
    setPrimaryClip(label)
    showImageToast(R.mipmap.ic_toast, i18String(R.string.copySuccess))
}