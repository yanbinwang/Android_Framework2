package com.example.common.utils.builder

import android.os.Looper
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import com.example.common.R
import com.example.common.utils.function.color
import com.google.android.material.snackbar.BaseTransientBottomBar.ANIMATION_MODE_FADE
import com.google.android.material.snackbar.Snackbar
import java.lang.ref.WeakReference
import androidx.core.view.isVisible
import com.example.framework.utils.logWTF

/**
 * jetpack提示框
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
        val snackBar = Snackbar.make(root, "", length)
        snackBar.setText(resId)
        snackBar
    }

    /**
     * 传入文字的toast
     */
    private var defaultTextBuilder: (root: View, message: String, length: Int) -> Snackbar = { root, message, length ->
        val snackBar = Snackbar.make(root, "", length)
        snackBar.setText(message)
        snackBar
    }

    @JvmStatic
    fun short(root: View, resId: Int, action: Int? = null, listener: View.OnClickListener? = null, snackBuilder: ((root: View, resId: Int, length: Int) -> Snackbar) = defaultResBuilder) {
        showSnackBar(root, Snackbar.LENGTH_SHORT, resId, action, listener) { view, input, len ->
            (snackBuilder as? (View, Any, Int) -> Snackbar)?.invoke(view, input, len)
        }
    }

    @JvmStatic
    fun short(root: View, message: String, action: String? = null, listener: View.OnClickListener? = null, snackBuilder: ((root: View, message: String, length: Int) -> Snackbar) = defaultTextBuilder) {
        showSnackBar(root, Snackbar.LENGTH_SHORT, message, action, listener) { view, input, len ->
            (snackBuilder as? (View, Any, Int) -> Snackbar)?.invoke(view, input, len)
        }
    }

    @JvmStatic
    fun long(root: View, resId: Int, action: Int? = null, listener: View.OnClickListener? = null, snackBuilder: ((root: View, resId: Int, length: Int) -> Snackbar) = defaultResBuilder) {
        showSnackBar(root, Snackbar.LENGTH_LONG, resId, action, listener) { view, input, len ->
            (snackBuilder as? (View, Any, Int) -> Snackbar)?.invoke(view, input, len)
        }
    }

    @JvmStatic
    fun long(root: View, message: String, action: String? = null, listener: View.OnClickListener? = null, snackBuilder: ((root: View, message: String, length: Int) -> Snackbar) = defaultTextBuilder) {
        showSnackBar(root, Snackbar.LENGTH_LONG, message, action, listener) { view, input, len ->
            (snackBuilder as? (View, Any, Int) -> Snackbar)?.invoke(view, input, len)
        }
    }

    @JvmStatic
    fun indefinite(root: View, resId: Int, action: Int? = null, listener: View.OnClickListener? = null, snackBuilder: ((root: View, resId: Int, length: Int) -> Snackbar) = defaultResBuilder) {
        showSnackBar(root, Snackbar.LENGTH_INDEFINITE, resId, action, listener) { view, input, len ->
            (snackBuilder as? (View, Any, Int) -> Snackbar)?.invoke(view, input, len)
        }
    }

    @JvmStatic
    fun indefinite(root: View, message: String, action: String? = null, listener: View.OnClickListener? = null, snackBuilder: ((root: View, message: String, length: Int) -> Snackbar) = defaultTextBuilder) {
        showSnackBar(root, Snackbar.LENGTH_INDEFINITE, message, action, listener) { view, input, len ->
            (snackBuilder as? (View, Any, Int) -> Snackbar)?.invoke(view, input, len)
        }
    }

    /**
     * 显示 Snack 的公共方法
     * 系统级维持默认，底部弹出，可定制背景，textview大小等
     */
    private fun showSnackBar(root: View, length: Int, input: Any, action: Any? = null, listener: View.OnClickListener? = null, builder: (View, Any, Int) -> Snackbar?) {
        if (Looper.getMainLooper() != Looper.myLooper()) return
        if (input is Int && input == -1 || input is String && input.isEmpty()) return
        cancelSnackBar()
        builder(root, input, length)?.let {
            currentSnackBar = WeakReference(it)
            when (action) {
                is String -> it.setAction(action, listener)
                is Int -> it.setAction(action, listener)
            }
            //背景
            it.setBackgroundTint(color(R.color.appTheme))
            //右侧按钮背景
            it.setActionTextColor(color(R.color.textWhite))
            //定制俩textview大小/样式
//            val snackbarText = it.view.findViewById<SnackbarContentLayout>(R.id.snackbar_text)
//            val snackbarAction = it.view.findViewById<SnackbarContentLayout>(R.id.snackbar_action)
            it.show()
        }
    }

    /**
     * 自定义布局
     * SnackBarBuilder.custom(it, Snackbar.LENGTH_LONG, { snackbar ->
     *                 //透明背景
     *                 snackbar.setBackgroundTint(Color.TRANSPARENT)
     *                 // 获取 Snackbar 的根视图
     *                 val snackbarView = snackbar.view
     *                 // 隐藏默认的文本和动作视图
     *                 val snackbarText = snackbarView.findViewById<TextView>(com.google.android.material.R.id.snackbar_text)
     *                 snackbarText.gone()
     *                 val snackbarAction = snackbarView.findViewById<TextView>(com.google.android.material.R.id.snackbar_action)
     *                 snackbarAction.gone()
     *                 // 加载自定义视图
     *                 val binding = ViewSnackbarImageStyleBinding.bind(this.inflate(R.layout.view_snackbar_image_style))
     *                 binding.ivType.setImageResource(R.mipmap.ic_toast)
     *                 binding.tvLabel.text = "复制成功"
     *                 //父布局
     *                 val root = snackbarView as? ViewGroup
     *                 // 移除默认视图
     *                 root?.removeAllViews()
     *                 // 添加自定义视图
     *                 root?.addView(binding.root)
     * //                // 空出顶部导航栏
     * //                binding.root.margin(top = getStatusBarHeight())
     *                 return@custom snackbar
     *             }, true)
     */
    @JvmStatic
    fun custom(root: View, length: Int = Snackbar.LENGTH_LONG, customBuilder: (Snackbar) -> Snackbar, isTop: Boolean = false, onShown: (() -> Unit)? = null, onDismissed: (() -> Unit)? = null) {
        //自定义构建逻辑
        if (Looper.getMainLooper() != Looper.myLooper()) return
        cancelSnackBar()
        val snackBar = Snackbar.make(root, "", length)
        currentSnackBar = WeakReference(snackBar)
        val configuredSnackBar = customBuilder(snackBar)
        // 自定义snackBar需要撑满屏幕
        val snackBarView = configuredSnackBar.view
        // 移除 Snackbar 根视图可能存在的内边距
        snackBarView.setPadding(0, 0, 0, 0)
        // 设置 Snackbar 根视图的布局参数，确保宽度撑满
        val snackBarLayoutParams = snackBarView.layoutParams
        snackBarLayoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT
        snackBarView.layoutParams = snackBarLayoutParams
        //对于从顶部向下弹出的弹框，做进阶的定制
        if (isTop) {
            val params = snackBarView.layoutParams as? FrameLayout.LayoutParams
            params?.gravity = Gravity.TOP
            snackBarView.layoutParams = params
//        // snackBar默认只有透明和方向俩动画，并且调用的是ValueAnimator，意味着我们不管怎么定义，它都会在show的时候强制先执行
//        // 为解决这个问题，干脆先将使徒设为不可见，并在300（DEFAULT_DURATION默认动画时间150）过后，再执行我们的动画
//        //要么全局样式使用<item name="motionDurationLong2">0</item>要么就是渐隐，又或者映射
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
        } catch (e: ClassNotFoundException) {
            e.printStackTrace()
        } catch (e: NoSuchFieldException) {
            e.printStackTrace()
        } catch (e: IllegalAccessException) {
            e.printStackTrace()
        }
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

fun Int?.shortSnackBar(root: View, action: Int? = null, listener: View.OnClickListener? = null) {
    this ?: return
    SnackBarBuilder.short(root, this, action, listener)
}

fun String?.shortSnackBar(root: View, action: String? = null, listener: View.OnClickListener? = null) {
    this ?: return
    SnackBarBuilder.short(root, this, action, listener)
}

fun Int?.longSnackBar(root: View, action: Int? = null, listener: View.OnClickListener? = null) {
    this ?: return
    SnackBarBuilder.long(root, this, action, listener)
}

fun String?.longSnackBar(root: View, action: String? = null, listener: View.OnClickListener? = null) {
    this ?: return
    SnackBarBuilder.long(root, this, action, listener)
}

fun Int?.indefiniteSnackBar(root: View, action: Int? = null, listener: View.OnClickListener? = null) {
    this ?: return
    SnackBarBuilder.indefinite(root, this, action, listener)
}

fun String?.indefiniteSnackBar(root: View, action: String? = null, listener: View.OnClickListener? = null) {
    this ?: return
    SnackBarBuilder.indefinite(root, this, action, listener)
}