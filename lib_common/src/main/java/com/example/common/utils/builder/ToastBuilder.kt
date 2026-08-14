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
import com.example.common.utils.function.string
import com.example.framework.utils.function.inflate
import com.example.framework.utils.function.value.htmlToSpanned
import java.lang.ref.WeakReference

/**
 * 全局提示框定制
 * 1) 支持富文本（Spanned）
 * 如果需要显示带格式的文本（如加粗、颜色），可在 showCustom 方法中处理：
 * ToastBuilder.showCustom { context, toast ->
 *     val spanned = Html.fromHtml("<b>这是加粗文本</b>")
 *     toast.setText(spanned)
 *     toast.setGravity(Gravity.CENTER, 0, 0)
 * }
 *
 * 2) 添加动画扩展点
 * 在 customWithAnimation 中开放动画接口，支持淡入淡出、滑动等效果：
 * fun customWithAnimation(
 *     length: Int = Toast.LENGTH_SHORT,
 *     enterAnim: (View) -> Unit, // 入场动画
 *     exitAnim: (View) -> Unit // 离场动画（可选）
 * ) {
 *     showCustom(length) { context, toast ->
 *         val customView = toast.view ?: return@custom // 确保有自定义视图
 *         enterAnim(customView)
 *         // 监听 Toast 消失时执行离场动画（需通过反射或回调实现，Toast 原生不支持）
 *     }
 * }
 *
 * 3) 适配暗黑模式
 * 在自定义布局中使用 ContextCompat.getColorStateList 或 android:theme，确保不同模式下样式一致：
 * customView.findViewById<TextView>(R.id.toast_text).setTextColor(
 *     ContextCompat.getColorStateList(toast.context, R.color.primary_text)
 * )
 */
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
        Toast.makeText(appContext, resId, length)
    }
    /**
     * 传入文字的 Toast
     */
    private var defaultTextBuilder: (message: String, length: Int) -> Toast = { message, length ->
        Toast.makeText(appContext, message, length)
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
        showToast(resId, Toast.LENGTH_SHORT, toastBuilder)
    }

    fun short(message: String, toastBuilder: ((message: String, length: Int) -> Toast) = this.defaultTextBuilder) {
        showToast(message, Toast.LENGTH_SHORT, toastBuilder)
    }

    fun long(resId: Int, toastBuilder: ((resId: Int, length: Int) -> Toast) = this.defaultResBuilder) {
        showToast(resId, Toast.LENGTH_LONG, toastBuilder)
    }

    fun long(message: String, toastBuilder: ((message: String, length: Int) -> Toast) = this.defaultTextBuilder) {
        showToast(message, Toast.LENGTH_LONG, toastBuilder)
    }

    /**
     * 显示 Toast 的公共方法
     */
    private fun <T> showToast(input: T, length: Int, builder: (T, Int) -> Toast) {
        // 子线程不显示
        if (Looper.getMainLooper() != Looper.myLooper()) return
        // 输出内容不符合不显示
        if (when (input) {
            is Int -> input == -1
            is String -> input.isEmpty()
            else -> false
        }) return
        // 取消当前的 Toast
        cancelToast()
        // 构建新的 Toast
        builder(input, length).apply {
            currentToast = WeakReference(this)
            show()
        }
    }

    /**
     * 自定义 Toast 的提示 View
     */
    fun showCustom(length: Int, gravity: Int = Gravity.CENTER, customBuilder: (Context, Toast) -> Unit) {
        if (Looper.getMainLooper() != Looper.myLooper()) return
        val ctx = appContext ?: return
        cancelToast()
        val toast = Toast.makeText(ctx, null, length)
        toast.setGravity(gravity, 0, 0)
        currentToast = WeakReference(toast)
        customBuilder(ctx, toast)
        toast.show()
    }

    /**
     * 设置自定义 Toast 提示 View
     */
    fun showImageToast(@DrawableRes resId: Int, message: String, length: Int = Toast.LENGTH_SHORT) {
        showCustom(length) { context, toast ->
            val binding = ViewToastImageStyleBinding.bind(context.inflate(R.layout.view_toast_image_style))
            binding.ivType.setImageResource(resId)
            binding.tvLabel.text = message
            toast.view = binding.root
        }
    }

    /**
     * 设置富文本提示 ("<b>这是加粗文本</b>")
     */
    fun showHtmlToast(html: String, length: Int = Toast.LENGTH_SHORT) {
        showCustom(length) { _, toast ->
            val spanned = html.htmlToSpanned() ?: return@showCustom
            toast.setText(spanned)
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

fun Int?.longToast() {
    this ?: return
    ToastBuilder.long(this)
}

fun String?.longToast() {
    this ?: return
    ToastBuilder.long(this)
}

fun String?.htmlToast(length: Int = Toast.LENGTH_SHORT) {
    this ?: return
    ToastBuilder.showHtmlToast(this, length)
}

/**
 * 带提示的复制
 */
fun String?.copyToast(label: String = "Label", length: Int = Toast.LENGTH_SHORT) {
    this ?: return
    setPrimaryClip(label)
    showImageToast(R.mipmap.ic_toast, string(R.string.copySuccess), length)
}