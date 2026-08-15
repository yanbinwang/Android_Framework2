package com.example.common.utils.builder

import android.os.Looper
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.StringRes
import com.example.common.R
import com.example.common.utils.builder.SnackBarBuilder.SnackBarAction
import com.example.common.utils.function.color
import com.google.android.material.snackbar.BaseTransientBottomBar.ANIMATION_MODE_FADE
import com.google.android.material.snackbar.Snackbar
import java.lang.ref.WeakReference

/**
 * jetpack提示框
 */
object SnackBarBuilder {
    /**
     * 弱引用 SnackBar
     */
    private var currentSnackBar: WeakReference<Snackbar>? = null
    /**
     * 传入引用 String 格式的 SnackBar
     */
    private var defaultResBuilder: (root: View, resId: Int, length: Int) -> Snackbar = { root, resId, length ->
        Snackbar.make(root, resId, length)
    }
    /**
     * 传入文字的 SnackBar
     */
    private var defaultTextBuilder: (root: View, message: String, length: Int) -> Snackbar = { root, message, length ->
        Snackbar.make(root, message, length)
    }

    /**
     * 使用全局 defaultResBuilder 构建 SnackBar 实例供扩展函数在需要覆盖属性（如 gravity）时获取基线实例，不会修改全局 builder 本身，仅返回一个新创建的 SnackBar 对象
     */
    internal fun buildResToast(view: View, resId: Int, length: Int): Snackbar {
        return defaultResBuilder(view, resId, length)
    }

    internal fun buildTextToast(view: View, message: String, length: Int): Snackbar {
        return defaultTextBuilder(view, message, length)
    }

    /**
     * 全局的 SnackBar
     */
    fun setResSnackBarBuilder(builder: (view: View, message: Int, length: Int) -> Snackbar) {
        defaultResBuilder = builder
    }

    fun setTextSnackBarBuilder(builder: (view: View, message: String, length: Int) -> Snackbar) {
        defaultTextBuilder = builder
    }

    /**
     * 快捷创建 Text Action
     */
    fun snackBarAction(text: String, onClick: () -> Unit): SnackBarAction {
        return SnackBarAction.Text(text) { onClick() }
    }

    fun snackBarAction(@StringRes resId: Int, onClick: () -> Unit): SnackBarAction {
        return SnackBarAction.ResText(resId) { onClick() }
    }

    /**
     * @param length
     * 1) SHORT/LONG → 轻反馈，带一点上下文，自动消失
     * 2) INDEFINITE → 需要用户"做点什么"才能继续
     * @param action
     * 1) SnackBarAction.Text("撤销") { undo() }
     * 2) SnackBarAction.ResText(R.string.undo) { undo() }
     */
    fun show(root: View, resId: Int, length: Int = Toast.LENGTH_SHORT, action: SnackBarAction? = null, snackBuilder: ((root: View, resId: Int, length: Int) -> Snackbar) = defaultResBuilder) {
        showSnackBar(root, resId, length, action, snackBuilder)
    }

    fun show(root: View, message: String, length: Int = Toast.LENGTH_SHORT, action: SnackBarAction? = null, snackBuilder: ((root: View, message: String, length: Int) -> Snackbar) = defaultTextBuilder) {
        showSnackBar(root, message, length, action, snackBuilder)
    }

    /**
     * 显示 SnackBar 的公共方法
     * 系统级维持默认，底部弹出，可定制背景，textview大小等
     */
    private fun <T> showSnackBar(root: View, input: T, length: Int, action: SnackBarAction? = null, builder: (View, T, Int) -> Snackbar) {
        if (Looper.getMainLooper() != Looper.myLooper()) return
        if (when (input) {
            is Int -> input == -1
            is String -> input.isEmpty()
            else -> false
        }) return
        cancelSnackBar()
        builder(root, input, length).apply {
            currentSnackBar = WeakReference(this)
            if (null != action) {
                when (action) {
                    is SnackBarAction.Text -> setAction(action.text, action.listener)
                    is SnackBarAction.ResText -> setAction(action.resId, action.listener)
                }
//                // 定制俩textview大小/样式
//                val snackbarText = view.findViewById<SnackbarContentLayout>(R.id.snackbar_text)
//                val snackbarAction = view.findViewById<SnackbarContentLayout>(R.id.snackbar_action)
            }
            // 背景颜色
            setBackgroundTint(color(R.color.appTheme))
            // 右侧按钮颜色
            setActionTextColor(color(R.color.textWhite))
            // 展示
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
    fun showCustom(root: View, length: Int = Snackbar.LENGTH_LONG, customBuilder: (Snackbar) -> Snackbar, isTop: Boolean = false, hideDefaultContent: Boolean = true, onShown: (() -> Unit)? = null, onDismissed: (() -> Unit)? = null) {
        // 自定义构建逻辑
        if (Looper.getMainLooper() != Looper.myLooper()) return
        cancelSnackBar()
        val snackBar = Snackbar.make(root, "", length)
        currentSnackBar = WeakReference(snackBar)
        val configuredSnackBar = customBuilder(snackBar)
        // 自定义 Snackbar 需要撑满屏幕
        val snackBarView = configuredSnackBar.view
        // 移除 Snackbar 根视图可能存在的内边距
        snackBarView.setPadding(0, 0, 0, 0)
        // 设置 Snackbar 根视图的布局参数，确保宽度撑满
        val snackBarLayoutParams = snackBarView.layoutParams
        snackBarLayoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT
        snackBarView.layoutParams = snackBarLayoutParams
        // 清理原生内容
        if (hideDefaultContent) {
            snackBarView.findViewById<TextView>(com.google.android.material.R.id.snackbar_text)?.visibility = View.GONE
            snackBarView.findViewById<TextView>(com.google.android.material.R.id.snackbar_action)?.visibility = View.GONE
        }
        // 对于从顶部向下弹出的弹框，做进阶的定制
        if (isTop) {
            val params = snackBarView.layoutParams as? FrameLayout.LayoutParams
            params?.gravity = Gravity.TOP
            snackBarView.layoutParams = params
            /**
             * Snackbar 默认只有透明和方向俩动画，并且调用的是 ValueAnimator，意味着我们不管怎么定义，它都会在 show 的时候强制先执行
             * 为解决这个问题，干脆先将使徒设为不可见，并在300（DEFAULT_DURATION默认动画时间150）过后，再执行我们的动画
             * 又或者全局样式使用<item name="motionDurationLong2">0</item>要么就是渐隐，又或者映射
             */
            snackBar.animationMode = ANIMATION_MODE_FADE
            modifySnackbarAnimationDuration(snackBar)
            // 添加动画效果
            snackBar.addCallback(object : Snackbar.Callback() {
                override fun onShown(sb: Snackbar?) {
                    super.onShown(sb)
                    onShown?.invoke()
                    // 先将视图移到顶部不可见位置
                    snackBarView.translationY = -snackBarView.height.toFloat()
                    // 执行进入动画
                    snackBarView.animate()
                        .translationY(0f)
                        .setDuration(300)
                        .start()
                }

                override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
                    super.onDismissed(transientBottomBar, event)
                    onDismissed?.invoke()
//                    if (snackBarView.isAttachedToWindow && snackBarView.isVisible) {
//                        // 退出动画
//                        snackBarView.animate()
//                            .translationY(-snackBarView.height.toFloat())
//                            .setDuration(300)
//                            .start()
//                    }
                }
            })
        }
        configuredSnackBar.show()
    }

    private fun modifySnackbarAnimationDuration(snackbar: Snackbar) {
        try {
            val baseTransientBottomBarClass = Class.forName("com.google.android.material.snackbar.BaseTransientBottomBar")
            // 修改淡入动画时长为 0
            val animationFadeInDurationField = baseTransientBottomBarClass.getDeclaredField("animationFadeInDuration")
            animationFadeInDurationField.isAccessible = true
            animationFadeInDurationField.set(snackbar, 0)
            // 修改淡出动画时长为 0
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

        data class Text(val text: String, override val listener: View.OnClickListener) : SnackBarAction

        data class ResText(@StringRes val resId: Int, override val listener: View.OnClickListener) : SnackBarAction

    }

}

fun Int?.snackBar(root: View, length: Int = Snackbar.LENGTH_SHORT, action: SnackBarAction? = null) {
    this ?: return
    SnackBarBuilder.show(root, this, length, action)
}

fun String?.snackBar(root: View, length: Int = Snackbar.LENGTH_SHORT, action: SnackBarAction? = null) {
    this ?: return
    SnackBarBuilder.show(root, this, length, action)
}