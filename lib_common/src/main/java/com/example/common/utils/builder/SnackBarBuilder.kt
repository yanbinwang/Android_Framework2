package com.example.common.utils.builder

import android.os.Looper
import android.view.View
import com.google.android.material.snackbar.Snackbar
import java.lang.ref.WeakReference

/**
 * // 短时间显示
 * Snackbar.make(view, "短时间显示的 Snackbar", Snackbar.LENGTH_SHORT).show()
 *
 * // 长时间显示
 * Snackbar.make(view, "长时间显示的 Snackbar", Snackbar.LENGTH_LONG).show()
 *
 * // 无限期显示，直到用户手动关闭或执行操作
 * val indefiniteSnackbar = Snackbar.make(view, "无限期显示的 Snackbar", Snackbar.LENGTH_INDEFINITE)
 * indefiniteSnackbar.show()
 *
 * // 添加操作按钮
 * val snackbar = Snackbar.make(view, "数据已删除", Snackbar.LENGTH_LONG)
 * snackbar.setAction("撤销") {
 *     // 处理撤销操作
 * }
 * snackbar.show()
 *
 * // 设置操作按钮的文本颜色
 * val snackbar = Snackbar.make(view, "数据已删除", Snackbar.LENGTH_LONG)
 * snackbar.setAction("撤销") {
 *     // 处理撤销操作
 * }
 * snackbar.setActionTextColor(ContextCompat.getColor(this, R.color.colorAccent))
 * snackbar.show()
 *
 * // 设置 Snackbar 的背景颜色
 * val snackbar = Snackbar.make(view, "这是一个自定义背景颜色的 Snackbar", Snackbar.LENGTH_SHORT)
 * snackbar.setBackgroundTint(ContextCompat.getColor(this, R.color.custom_snackbar_background))
 * snackbar.show()
 *
 * // 自定义 Snackbar 的视图
 * val snackbar = Snackbar.make(view, "自定义视图的 Snackbar", Snackbar.LENGTH_LONG)
 * val snackbarView = snackbar.view
 * // 修改视图属性，例如设置自定义布局
 * val textView = snackbarView.findViewById<com.google.android.material.snackbar.SnackbarContentLayout>(com.google.android.material.R.id.snackbar_text)
 * textView.textSize = 18f
 * snackbar.show()
 */
@Suppress("UNCHECKED_CAST")
object SnackBarBuilder {
    /**
     * 弱引用snackBar
     */
    private var currentSnackBar: WeakReference<Snackbar>? = null

    /**
     * 传入引用string格式的snackBar
     */
    private var defaultResBuilder: (root: View, resId: Int, length: Int) -> Snackbar = { root, resId, length ->
        Snackbar.make(root, resId, length)
    }

    /**
     * 传入文字的toast
     */
    private var defaultTextBuilder: (root: View, message: String, length: Int) -> Snackbar = { root, message, length ->
        Snackbar.make(root, message, length)
    }

    @JvmStatic
    fun short(root: View, resId: Int, snackBuilder: ((root: View, resId: Int, length: Int) -> Snackbar) = defaultResBuilder) {
        showSnackBar(root, Snackbar.LENGTH_SHORT, resId) { view, input, len ->
            (snackBuilder as? (View, Any, Int) -> Snackbar)?.invoke(view, input, len)
        }
    }

    @JvmStatic
    fun short(root: View, message: String, snackBuilder: ((root: View, message: String, length: Int) -> Snackbar) = defaultTextBuilder) {
        showSnackBar(root, Snackbar.LENGTH_SHORT, message) { view, input, len ->
            (snackBuilder as? (View, Any, Int) -> Snackbar)?.invoke(view, input, len)
        }
    }

    @JvmStatic
    fun long(root: View, resId: Int, snackBuilder: ((root: View, resId: Int, length: Int) -> Snackbar) = defaultResBuilder) {
        showSnackBar(root, Snackbar.LENGTH_LONG, resId) { view, input, len ->
            (snackBuilder as? (View, Any, Int) -> Snackbar)?.invoke(view, input, len)
        }
    }

    @JvmStatic
    fun long(root: View, message: String, snackBuilder: ((root: View, message: String, length: Int) -> Snackbar) = defaultTextBuilder) {
        showSnackBar(root, Snackbar.LENGTH_LONG, message) { view, input, len ->
            (snackBuilder as? (View, Any, Int) -> Snackbar)?.invoke(view, input, len)
        }
    }

    @JvmStatic
    fun indefinite(root: View, resId: Int, snackBuilder: ((root: View, resId: Int, length: Int) -> Snackbar) = defaultResBuilder) {
        showSnackBar(root, Snackbar.LENGTH_INDEFINITE, resId) { view, input, len ->
            (snackBuilder as? (View, Any, Int) -> Snackbar)?.invoke(view, input, len)
        }
    }

    @JvmStatic
    fun indefinite(root: View, message: String, snackBuilder: ((root: View, message: String, length: Int) -> Snackbar) = defaultTextBuilder) {
        showSnackBar(root, Snackbar.LENGTH_INDEFINITE, message) { view, input, len ->
            (snackBuilder as? (View, Any, Int) -> Snackbar)?.invoke(view, input, len)
        }
    }

    /**
     * 显示 Snack 的公共方法
     * // 示例：顶部显示的自定义布局 Snackbar
     * SnackBarBuilder.custom(root = binding.root) { snackbar ->
     *     val inflater = LayoutInflater.from(context)
     *     val customView = inflater.inflate(R.layout.custom_snackbar, null)
     *
     *     // 修改 Snackbar 视图参数（如顶部显示）
     *     val params = snackbar.view.layoutParams as? FrameLayout.LayoutParams
     *     params?.gravity = Gravity.TOP
     *     snackbar.view.layoutParams = params
     *
     *     // 替换默认内容视图
     *     val contentLayout = snackbar.view.findViewById<Snackbar.SnackbarLayout>(com.google.android.material.R.id.snackbar_layout)
     *     contentLayout.removeAllViews()
     *     contentLayout.addView(customView)
     *
     *     return@custom snackbar // 必须返回 Snackbar 实例
     * }
     */
    private fun showSnackBar(root: View, length: Int, input: Any, builder: (View, Any, Int) -> Snackbar?) {
        if (Looper.getMainLooper() != Looper.myLooper()) return
        if (input is Int && input == -1 || input is String && input.isEmpty()) return
        cancelSnackBar()
        builder(root, input, length)?.apply {
            currentSnackBar = WeakReference(this)
            show()
        }
    }

    /**
     * 自定义布局
     */
    @JvmStatic
    fun custom(root: View, length: Int = Snackbar.LENGTH_LONG, customBuilder: (Snackbar) -> Snackbar) {
        //自定义构建逻辑
        if (Looper.getMainLooper() != Looper.myLooper()) return
        cancelSnackBar()
        val snackBar = Snackbar.make(root, "", length)
        currentSnackBar = WeakReference(snackBar)
        customBuilder(snackBar).show() // 允许完全自定义 Snackbar 配置
    }

    @JvmStatic
    fun cancelSnackBar() {
        currentSnackBar?.get()?.dismiss()
    }

    /**
     * 全局的SnackBar
     */
    @JvmStatic
    fun setResSnackBarBuilder(builder: (view: View, message: Int, length: Int) -> Snackbar) {
        defaultResBuilder = builder
    }

    @JvmStatic
    fun setTextSnackBarBuilder(builder: (view: View, message: String, length: Int) -> Snackbar) {
        defaultTextBuilder = builder
    }

}

fun Int?.shortSnackBar(root: View) {
    this ?: return
    SnackBarBuilder.short(root, this)
}

fun String?.shortSnackBar(root: View) {
    this ?: return
    SnackBarBuilder.short(root, this)
}

fun Int?.longSnackBar(root: View) {
    this ?: return
    SnackBarBuilder.long(root, this)
}

fun String?.longSnackBar(root: View) {
    this ?: return
    SnackBarBuilder.long(root, this)
}

fun Int?.indefiniteSnackBar(root: View) {
    this ?: return
    SnackBarBuilder.indefinite(root, this)
}

fun String?.indefiniteSnackBar(root: View) {
    this ?: return
    SnackBarBuilder.indefinite(root, this)
}