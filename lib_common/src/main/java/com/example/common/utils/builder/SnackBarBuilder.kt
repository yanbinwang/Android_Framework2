package com.example.common.utils.builder

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.google.android.material.snackbar.BaseTransientBottomBar.ANIMATION_MODE_FADE
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
        showSnackBar(root, Snackbar.LENGTH_SHORT, resId, { view, input, len ->
            (snackBuilder as? (View, Any, Int) -> Snackbar)?.invoke(view, input, len)
        })
    }

    @JvmStatic
    fun short(root: View, message: String, snackBuilder: ((root: View, message: String, length: Int) -> Snackbar) = defaultTextBuilder) {
        showSnackBar(root, Snackbar.LENGTH_SHORT, message, { view, input, len ->
            (snackBuilder as? (View, Any, Int) -> Snackbar)?.invoke(view, input, len)
        })
    }

    @JvmStatic
    fun long(root: View, resId: Int, snackBuilder: ((root: View, resId: Int, length: Int) -> Snackbar) = defaultResBuilder) {
        showSnackBar(root, Snackbar.LENGTH_LONG, resId, { view, input, len ->
            (snackBuilder as? (View, Any, Int) -> Snackbar)?.invoke(view, input, len)
        })
    }

    @JvmStatic
    fun long(root: View, message: String, snackBuilder: ((root: View, message: String, length: Int) -> Snackbar) = defaultTextBuilder) {
        showSnackBar(root, Snackbar.LENGTH_LONG, message, { view, input, len ->
            (snackBuilder as? (View, Any, Int) -> Snackbar)?.invoke(view, input, len)
        })
    }

    @JvmStatic
    fun indefinite(root: View, resId: Int, snackBuilder: ((root: View, resId: Int, length: Int) -> Snackbar) = defaultResBuilder) {
        showSnackBar(root, Snackbar.LENGTH_INDEFINITE, resId, { view, input, len ->
            (snackBuilder as? (View, Any, Int) -> Snackbar)?.invoke(view, input, len)
        })
    }

    @JvmStatic
    fun indefinite(root: View, message: String, snackBuilder: ((root: View, message: String, length: Int) -> Snackbar) = defaultTextBuilder) {
        showSnackBar(root, Snackbar.LENGTH_INDEFINITE, message, { view, input, len ->
            (snackBuilder as? (View, Any, Int) -> Snackbar)?.invoke(view, input, len)
        })
    }

    /**
     * 显示 Snack 的公共方法
     */
    private fun showSnackBar(root: View, length: Int, input: Any, builder: (View, Any, Int) -> Snackbar?, isTop: Boolean = false) {
        if (Looper.getMainLooper() != Looper.myLooper()) return
        if (input is Int && input == -1 || input is String && input.isEmpty()) return
        cancelSnackBar()
        builder(root, input, length)?.apply {
            currentSnackBar = WeakReference(this)
            if (isTop) {
                setupTopSnackBar(this)
            }
            show()
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
    fun custom(root: View, length: Int = Snackbar.LENGTH_LONG, customBuilder: (Snackbar) -> Snackbar, isTop: Boolean = false) {
        //自定义构建逻辑
        if (Looper.getMainLooper() != Looper.myLooper()) return
        cancelSnackBar()
        val snackBar = Snackbar.make(root, "", length)
        currentSnackBar = WeakReference(snackBar)
        val configuredSnackBar = customBuilder(snackBar)
        if (isTop) {
            setupTopSnackBar(configuredSnackBar)
        }
        configuredSnackBar.show()
    }

    private fun setupTopSnackBar(snackBar: Snackbar) {
        // 添加样式
        val snackBarView = snackBar.view
        val params = snackBarView.layoutParams as? FrameLayout.LayoutParams
        params?.gravity = Gravity.TOP
        snackBarView.layoutParams = params
        // 移除 Snackbar 根视图可能存在的内边距
        snackBarView.setPadding(0, 0, 0, 0)
        // 设置 Snackbar 根视图的布局参数，确保宽度撑满
        val snackBarLayoutParams = snackBarView.layoutParams
        snackBarLayoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT
        snackBarView.layoutParams = snackBarLayoutParams
//        // snackBar默认只有透明和方向俩动画，并且调用的是ValueAnimator，意味着我们不管怎么定义，它都会在show的时候强制先执行
//        // 为解决这个问题，干脆先将使徒设为不可见，并在300（DEFAULT_DURATION默认动画时间150）过后，再执行我们的动画
//        //要么全局样式使用<item name="motionDurationLong2">0</item>要么就是渐隐，又或者映射
        snackBar.animationMode = ANIMATION_MODE_FADE
        modifySnackbarAnimationDuration(snackBar)
        // 添加动画效果
        snackBar.addCallback(object : Snackbar.Callback() {
            override fun onShown(sb: Snackbar?) {
                super.onShown(sb)
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
//                // 退出动画
//                snackBarView.animate()
//                    .translationY(-snackBarView.height.toFloat())
//                    .setDuration(300)
//                    .setListener(object : AnimatorListenerAdapter() {
//                        override fun onAnimationEnd(animation: Animator) {
//                            super.onAnimationEnd(animation)
////                            snackBarView.visibility = View.GONE
//                        }
//                    })
//                    .start()
            }
        })
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