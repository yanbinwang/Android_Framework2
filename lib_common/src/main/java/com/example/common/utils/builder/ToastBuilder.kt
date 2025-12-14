package com.example.common.utils.builder

import android.content.Context
import android.os.Looper
import android.view.Gravity
import android.widget.Toast
import androidx.annotation.DrawableRes
import com.example.common.BaseApplication
import com.example.common.R
import com.example.common.databinding.ViewToastImageStyleBinding
import com.example.common.utils.function.setPrimaryClip
import com.example.common.utils.function.string
import com.example.framework.utils.function.inflate
import java.lang.ref.WeakReference

/**
 * 全局提示框定制
 * 1. 支持富文本（Spanned）
 * 如果需要显示带格式的文本（如加粗、颜色），可在 custom 方法中处理：
 * kotlin
 * ToastBuilder.custom { toast ->
 *     val spanned = Html.fromHtml("<b>这是加粗文本</b>")
 *     toast.setText(spanned)
 *     toast.setGravity(Gravity.CENTER, 0, 0)
 * }
 * 2. 添加动画扩展点
 * 在 customBuilder 中开放动画接口，支持淡入淡出、滑动等效果：
 * kotlin
 * fun customWithAnimation(
 *     length: Int = Toast.LENGTH_SHORT,
 *     enterAnim: (View) -> Unit, // 入场动画
 *     exitAnim: (View) -> Unit // 离场动画（可选）
 * ) {
 *     custom(length) { toast ->
 *         val customView = toast.view ?: return@custom // 确保有自定义视图
 *         enterAnim(customView)
 *         // 监听 Toast 消失时执行离场动画（需通过反射或回调实现，Toast 原生不支持）
 *     }
 * }
 *
 * 3. 适配暗黑模式
 * 在自定义布局中使用 ContextCompat.getColorStateList 或 android:theme，确保不同模式下样式一致：
 * kotlin
 * customView.findViewById<TextView>(R.id.toast_text).setTextColor(
 *     ContextCompat.getColorStateList(toast.context, R.color.primary_text)
 * )
 */
@Suppress("UNCHECKED_CAST")
object ToastBuilder {
    /**
     * 全局context
     */
    private val mContext get() = BaseApplication.instance.applicationContext
    /**
     * 弱引用toast
     */
    private var currentToast: WeakReference<Toast>? = null

    /**
     * 传入引用string格式的toast
     * ToastBuilder.short(R.string.homeRecommendedQuestsReceiveSuccess) { resId, length ->
     * val toast = Toast.makeText(MyApplication.instance, null, length)
     * toast?.setGravity(Gravity.CENTER, 0, 0)
     * toast?.duration = length
     *
     * val view = BaseApplication.instance.inflate(R.layout.toast_home_quest_success)
     * view.imgIcon.setImageResource(R.mipmap.icon_home_quest_dialog_coupon)
     * view.txtTitle.setI18nRes(resId)
     * view.txtAmount.text = "$" + bean.rewardNum
     * toast?.view = view
     * toast
     * }
     */
    private var defaultResBuilder: (resId: Int, length: Int) -> Toast = { resId, length ->
        val toast = Toast.makeText(mContext, null, length)
        toast.setText(resId)
        toast
    }

    /**
     * 传入文字的toast
     */
    private var defaultTextBuilder: (message: String, length: Int) -> Toast = { message, length ->
        val toast = Toast.makeText(mContext, null, length)
        toast.setText(message)
        toast
    }

    /**
     * 全局调取toast方法
     */
    @JvmStatic
    fun short(resId: Int, toastBuilder: ((resId: Int, length: Int) -> Toast) = this.defaultResBuilder) {
        showToast(Toast.LENGTH_SHORT, resId) { input, len ->
            (toastBuilder as? (Any, Int) -> Toast)?.invoke(input, len)
        }
    }

    @JvmStatic
    fun short(message: String, toastBuilder: ((message: String, length: Int) -> Toast) = this.defaultTextBuilder) {
        showToast(Toast.LENGTH_SHORT, message) { input, len ->
            (toastBuilder as? (Any, Int) -> Toast)?.invoke(input, len)
        }
    }

    @JvmStatic
    fun long(resId: Int, toastBuilder: ((resId: Int, length: Int) -> Toast) = this.defaultResBuilder) {
        showToast(Toast.LENGTH_LONG, resId) { input, len ->
            (toastBuilder as? (Any, Int) -> Toast)?.invoke(input, len)
        }
    }

    @JvmStatic
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

    @JvmStatic
    fun custom(length: Int = Toast.LENGTH_SHORT, customBuilder: (Context, Toast) -> Unit) {
        if (Looper.getMainLooper() != Looper.myLooper()) return
        cancelToast()
        val toast = Toast.makeText(mContext, "", length)
        currentToast = WeakReference(toast)
        customBuilder(mContext, toast)
        toast.show()
    }

    @JvmStatic
    fun cancelToast() {
        currentToast?.get()?.cancel()
    }

    /**
     * application中初始化全局的toast
     * 部分手機定制導致顯示不全，樣式不統一，故而再重寫一次，統一樣式
     */
    @JvmStatic
    fun setResToastBuilder(builder: (resId: Int, length: Int) -> Toast) {
        defaultResBuilder = builder
    }

    @JvmStatic
    fun setTextToastBuilder(builder: (message: String, length: Int) -> Toast) {
        defaultTextBuilder = builder
    }

}

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

/**
 * 带提示的复制
 */
fun String?.copy(label: String = "Label") {
    this ?: return
    setPrimaryClip(label)
    setToastView(R.mipmap.ic_toast, string(R.string.copySuccess))
}

/**
 * 设置自定义toast提示view
 */
fun setToastView(@DrawableRes resId: Int, message: String) {
    ToastBuilder.custom { context, toast ->
        toast.setGravity(Gravity.CENTER, 0, 0)
        toast.duration = Toast.LENGTH_SHORT
        val binding = ViewToastImageStyleBinding.bind(context.inflate(R.layout.view_toast_image_style))
        binding.ivType.setImageResource(resId)
        binding.tvLabel.text = message
        toast.view = binding.root
    }
}