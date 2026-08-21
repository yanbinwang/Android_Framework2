package com.example.common.utils.builder

import android.graphics.Color
import android.os.Looper
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.Toast
import androidx.annotation.ColorRes
import androidx.annotation.StringRes
import com.example.common.R
import com.example.common.utils.builder.SnackBarBuilder.SnackBarAction
import com.google.android.material.snackbar.BaseTransientBottomBar.ANIMATION_MODE_FADE
import com.google.android.material.snackbar.Snackbar
import java.lang.ref.WeakReference

/**
 * Jetpack 提示框
 * 1) Dialog / PopupWindow
 * 通过 WindowManager.addView() 创建一个全新的、独立的 Window。系统要分配独立的 DecorView、LayoutParams、InputMethod 绑定等一整套窗口资源
 * 2) Snackbar
 * 寄生而非独立，不创建新 Window。只是往当前 Activity 的 DecorView（通常是 CoordinatorLayout 或其父容器）里 addView() 了一个普通的子 View。本质上就是一个带入场/出场动画的自定义 View，和普通按钮、TextView 没有本质区别
 */
object SnackBarBuilder {
    /**
     * 弱引用 SnackBar
     */
    private var currentSnackBar: WeakReference<Snackbar>? = null
    /**
     * 传入引用 String 格式的 SnackBar
     */
    private var defaultResBuilder: (root: View, resId: Int, length: Int, navigationBarColor: Int, action: SnackBarAction?) -> Snackbar = { root, resId, length, _, _ ->
        Snackbar.make(root, resId, length)
    }
    /**
     * 传入文字的 SnackBar
     */
    private var defaultTextBuilder: (root: View, message: String, length: Int, navigationBarColor: Int, action: SnackBarAction?) -> Snackbar = { root, message, length, _, _ ->
        Snackbar.make(root, message, length)
    }

    /**
     * 使用全局 defaultResBuilder 构建 SnackBar 实例供扩展函数在需要覆盖属性（如 gravity）时获取基线实例，不会修改全局 builder 本身，仅返回一个新创建的 SnackBar 对象
     */
    internal fun buildResToast(view: View, @StringRes resId: Int, length: Int, @ColorRes navigationBarColor: Int, action: SnackBarAction?): Snackbar {
        return defaultResBuilder(view, resId, length, navigationBarColor, action)
    }

    internal fun buildTextToast(view: View, message: String, length: Int, @ColorRes navigationBarColor: Int, action: SnackBarAction?): Snackbar {
        return defaultTextBuilder(view, message, length, navigationBarColor, action)
    }

    /**
     * 全局的 SnackBar
     */
    fun setResSnackBarBuilder(builder: (view: View, resId: Int, length: Int, navigationBarColor: Int, action: SnackBarAction?) -> Snackbar) {
        defaultResBuilder = builder
    }

    fun setTextSnackBarBuilder(builder: (view: View, message: String, length: Int, navigationBarColor: Int, action: SnackBarAction?) -> Snackbar) {
        defaultTextBuilder = builder
    }

    /**
     * 快捷创建 Text Action
     */
    fun snackBarAction(text: String, @ColorRes actionTextColorRes: Int? = null, onClick: () -> Unit): SnackBarAction {
        return SnackBarAction.Text(text, actionTextColorRes) { onClick() }
    }

    fun snackBarAction(@StringRes resId: Int, @ColorRes actionTextColorRes: Int? = null, onClick: () -> Unit): SnackBarAction {
        return SnackBarAction.ResText(resId, actionTextColorRes) { onClick() }
    }

    /**
     * @param length
     * 1) SHORT/LONG → 轻反馈，带一点上下文，自动消失 ≈ 2000ms / 3500ms
     * 2) INDEFINITE → 需要用户"做点什么"才能继续
     * @param action
     * 1) SnackBarAction.Text("撤销") { undo() }
     * 2) SnackBarAction.ResText(R.string.undo) { undo() }
     */
    fun show(root: View, @StringRes resId: Int, length: Int = Toast.LENGTH_SHORT, @ColorRes navigationBarColor: Int = R.color.appNavigationBar, action: SnackBarAction? = null, snackBuilder: ((root: View, resId: Int, length: Int, navigationBarColor: Int, action: SnackBarAction?) -> Snackbar) = defaultResBuilder) {
        showSnackBar(root, resId, length, navigationBarColor, action, snackBuilder)
    }

    fun show(root: View, message: String, length: Int = Toast.LENGTH_SHORT, @ColorRes navigationBarColor: Int = R.color.appNavigationBar, action: SnackBarAction? = null, snackBuilder: ((root: View, message: String, length: Int, navigationBarColor: Int, action: SnackBarAction?) -> Snackbar) = defaultTextBuilder) {
        showSnackBar(root, message, length, navigationBarColor, action, snackBuilder)
    }

    /**
     * 显示 SnackBar 的公共方法
     * 系统级维持默认，底部弹出，可定制背景，textview大小等
     */
    private fun <T> showSnackBar(root: View, input: T, length: Int, @ColorRes navigationBarColor: Int, action: SnackBarAction?, builder: (View, T, Int, Int, SnackBarAction?) -> Snackbar) {
        if (Looper.getMainLooper() != Looper.myLooper()) return
        if (when (input) {
                is Int -> input == -1
                is String -> input.isEmpty()
                else -> false
            }
        ) return
        cancelSnackBar()
        builder(root, input, length, navigationBarColor, action).apply {
            currentSnackBar = WeakReference(this)
            show()
        }
    }

    /**
     * 自定义布局
     * SnackBarBuilder.custom(it, Snackbar.LENGTH_LONG, { snackbar ->
     *   //透明背景
     *   snackbar.setBackgroundTint(Color.TRANSPARENT)
     *   // 获取 Snackbar 的根视图
     *   val snackbarView = snackbar.view
     *   // 隐藏默认的文本和动作视图
     *   val snackbarText = snackbarView.findViewById<TextView>(com.google.android.material.R.id.snackbar_text)
     *   snackbarText.gone()
     *   val snackbarAction = snackbarView.findViewById<TextView>(com.google.android.material.R.id.snackbar_action)
     *   snackbarAction.gone()
     *   // 加载自定义视图
     *   val binding = ViewSnackbarImageStyleBinding.bind(this.inflate(R.layout.view_snackbar_image_style))
     *   binding.ivType.setImageResource(R.mipmap.ic_toast)
     *   binding.tvLabel.text = "复制成功"
     *   //父布局
     *   val root = snackbarView as? ViewGroup
     *   // 移除默认视图
     *   root?.removeAllViews()
     *   // 添加自定义视图
     *   root?.addView(binding.root)
     *   // // 空出顶部导航栏
     *   // binding.root.margin(top = getStatusBarHeight())
     *   return@custom snackbar
     * }, true)
     */
    fun showCustom(root: View, length: Int = Snackbar.LENGTH_LONG, customBuilder: (Snackbar, ViewGroup?) -> Snackbar, isTop: Boolean = false, onShown: (() -> Unit)? = null, onDismissed: (() -> Unit)? = null) {
        // 自定义构建逻辑
        if (Looper.getMainLooper() != Looper.myLooper()) return
        cancelSnackBar()
        val snackBar = Snackbar.make(root, "", length)
        // 透明背景
        snackBar.setBackgroundTint(Color.TRANSPARENT)
        currentSnackBar = WeakReference(snackBar)
        // 清空容器 + 重置样式
        val snackBarView = snackBar.view as? ViewGroup
        snackBarView?.removeAllViews()
        snackBarView?.setPadding(0, 0, 0, 0)
        snackBarView?.layoutParams = snackBarView.layoutParams.apply {
            width = ViewGroup.LayoutParams.MATCH_PARENT
        }
        // 构建配置
        val configuredSnackBar = customBuilder(snackBar, snackBarView)
        // 顶部向下弹出做进阶的定制
        if (isTop) {
            val params = snackBarView?.layoutParams as? FrameLayout.LayoutParams
            params?.gravity = Gravity.TOP
            snackBarView?.layoutParams = params
            /**
             * SnackBar 默认只有透明和方向俩动画，并且调用的是 ValueAnimator，意味着我们不管怎么定义，它都会在 show 的时候强制先执行
             * 为解决这个问题，干脆先将使徒设为不可见，并在300（DEFAULT_DURATION默认动画时间150）过后，再执行我们的动画
             * 又或者全局样式使用<item name="motionDurationLong2">0</item>要么就是渐隐，又或者映射
             */
            snackBar.animationMode = ANIMATION_MODE_FADE
            modifyAnimationDuration(snackBar)
            // 添加动画效果
            snackBar.addCallback(object : Snackbar.Callback() {
                override fun onShown(sb: Snackbar?) {
                    super.onShown(sb)
                    onShown?.invoke()
                    // 先将视图移到顶部不可见位置
                    snackBarView?.translationY = -snackBarView.height.toFloat()
                    // 执行进入动画
                    snackBarView?.animate()
                        ?.translationY(0f)
                        ?.setDuration(300)
                        ?.start()
                }

                override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
                    super.onDismissed(transientBottomBar, event)
                    onDismissed?.invoke()
//                    if (snackBarView.isAttachedToWindow && snackBarView.isVisible) {
//                        // 退出动画
//                        snackBarView?.animate()
//                            ?.translationY(-snackBarView.height.toFloat())
//                            ?.setDuration(300)
//                            ?.start()
//                    }
                }
            })
        }
        configuredSnackBar.show()
    }

    private fun modifyAnimationDuration(snackbar: Snackbar) {
        try {
            val baseTransientBottomBarClass = Class.forName("com.google.android.material.snackbar.BaseTransientBottomBar")
            // 修改淡入动画时长为 0
            val animationFadeInDurationField = baseTransientBottomBarClass.getDeclaredField("animationFadeInDuration")
            animationFadeInDurationField.isAccessible = true
            animationFadeInDurationField.set(snackbar, 0)
            // 修改淡出动画时长为 300
            val animationFadeOutDurationField = baseTransientBottomBarClass.getDeclaredField("animationFadeOutDuration")
            animationFadeOutDurationField.isAccessible = true
            animationFadeOutDurationField.set(snackbar, 300)
            // 修改滑动动画时长为 0
            val animationSlideDurationField = baseTransientBottomBarClass.getDeclaredField("animationSlideDuration")
            animationSlideDurationField.isAccessible = true
            animationSlideDurationField.set(snackbar, 0)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun cancelSnackBar() {
        currentSnackBar?.get()?.dismiss()
    }

    /**
     * Snackbar Action 封装
     * 保证文字/图标与点击事件永远成对出现
     */
    sealed interface SnackBarAction {
        val listener: View.OnClickListener
        val actionTextColorRes: Int? // null 时跟随 textRes，非 null 时使用指定颜色

        data class Text(val text: String, override val actionTextColorRes: Int? = null, override val listener: View.OnClickListener) : SnackBarAction

        data class ResText(@StringRes val resId: Int, override val actionTextColorRes: Int? = null, override val listener: View.OnClickListener) : SnackBarAction
    }

}

fun Int?.snackBar(root: View, length: Int = Snackbar.LENGTH_SHORT, @ColorRes navigationBarColor: Int = R.color.appNavigationBar, action: SnackBarAction? = null) {
    this ?: return
    SnackBarBuilder.show(root, this, length, navigationBarColor, action)
}

fun String?.snackBar(root: View, length: Int = Snackbar.LENGTH_SHORT, @ColorRes navigationBarColor: Int = R.color.appNavigationBar, action: SnackBarAction? = null) {
    this ?: return
    SnackBarBuilder.show(root, this, length, navigationBarColor, action)
}