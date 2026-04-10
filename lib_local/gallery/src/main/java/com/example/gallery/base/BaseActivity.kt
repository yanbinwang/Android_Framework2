package com.example.gallery.base

import android.annotation.SuppressLint
import android.content.Intent
import android.content.res.Resources
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.transition.Slide
import android.transition.Visibility
import android.view.Gravity
import android.view.ViewGroup
import android.view.Window
import android.widget.ImageButton
import android.window.OnBackInvokedCallback
import android.window.OnBackInvokedDispatcher
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.annotation.ColorRes
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.menu.ActionMenuItemView
import androidx.appcompat.widget.ActionMenuView
import androidx.appcompat.widget.Toolbar
import androidx.core.view.isNotEmpty
import com.example.common.utils.ScreenUtil.screenHeight
import com.example.common.utils.ScreenUtil.screenWidth
import com.example.common.utils.ScreenUtil.shouldUseWhiteSystemBarsForRes
import com.example.common.utils.function.getStatusBarHeight
import com.example.common.utils.manager.AppManager
import com.example.common.utils.removeNavigationBarDrawable
import com.example.common.utils.setNavigationBarDrawable
import com.example.common.utils.setNavigationBarLightMode
import com.example.common.utils.setStatusBarLightMode
import com.example.framework.utils.function.value.isMainThread
import com.example.framework.utils.function.view.doOnceAfterLayout
import com.example.framework.utils.function.view.padding
import com.example.framework.utils.function.view.size
import com.example.framework.utils.function.view.textColor
import com.example.framework.utils.function.view.textSize
import com.example.gallery.R
import com.example.gallery.base.bridge.Bye
import com.gyf.immersionbar.ImmersionBar
import me.jessyan.autosize.AutoSizeCompat
import me.jessyan.autosize.AutoSizeConfig

/**
 * 针对所有相册页面的基类
 */
abstract class BaseActivity : AppCompatActivity(), Bye {
    private val immersionBar by lazy { ImmersionBar.with(this) }

    companion object {

        /**
         * 启动相册页面就拿一次系统默认状态栏高度（由于第一个界面一定是相册库而不是裁剪,故而该值几乎是启动相册库后就不变的）
         */
        private val defaultStatusBarHeight = getStatusBarHeight()

        /**
         * 兼容控件内toolbar
         */
        @JvmStatic
        fun setSupportToolbar(toolbar: Toolbar?) {
            toolbar.doOnceAfterLayout { tb ->
                // 取当前页面状态栏高度
                var statusBarHeight = getStatusBarHeight()
                // 如果当前高度不对（比如从系统相机跳过来变成 0）就直接用"一开始就存好的默认高度"
                if (statusBarHeight != defaultStatusBarHeight) {
                    statusBarHeight = defaultStatusBarHeight
                }
                // 设置高度
                tb.size(height = tb.measuredHeight + statusBarHeight)
                // 设置左、右内边距全为0
                tb.padding(top = statusBarHeight, start = 0, end = 0)
                // 取出系统按钮
                val systemNavBtn = getNavButtonView(tb)
                // 去除水波纹
                systemNavBtn?.background = null
                // 去除长按文字
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    systemNavBtn?.tooltipText = null
                }
                systemNavBtn?.setContentDescription(null)
                systemNavBtn?.setOnLongClickListener { _ -> true }
            }
        }

        /**
         * 反射获取 Toolbar 中的私有字段 mNavButtonView（返回按钮）
         */
        private fun getNavButtonView(toolbar: Toolbar?): ImageButton? {
            try {
                // 获取 Toolbar 类中的 mNavButtonView 字段
                val field = Toolbar::class.java.getDeclaredField("mNavButtonView")
                // 设置字段可访问（私有字段需要开启）
                field.isAccessible = true
                // 获取字段值（即返回按钮的 ImageButton 实例）
                return field.get(toolbar) as? ImageButton
            } catch (e: Exception) {
                // 转换异常
                e.printStackTrace()
            }
            return null
        }

        /**
         * 处理纯图片的按钮
         * 页面的onCreateOptionsMenu中调取,此时Toolbar已经加载完成
         */
        @JvmStatic
        fun setSupportMenuView(toolbar: Toolbar, @ColorRes colorRes: Int) {
            for (i in 0 until toolbar.childCount) {
                val child = toolbar.getChildAt(i)
                if (child is ActionMenuView) {
                    // 设定的按钮被绘制为ActionMenuView,本身高度看似撑满屏幕并且绘制也是,但其内部的view还是带有一定的上下边距
                    child.doOnceAfterLayout {
                        adjustActionMenuView(toolbar, it, colorRes)
                    }
                }
            }
        }

        /**
         * 1) 32ms = 屏幕一帧的时间（约 30fps） 既不卡 UI，又能最快感知到菜单出现
         * 2) 大多数手机 60fps → 16ms 刷新一次 , 低一点 30fps → 32ms 刷新一次 , 32ms 就是「等下一帧渲染完」
         */
        @JvmStatic
        fun setSupportMenuViewAsync(toolbar: Toolbar, @ColorRes colorRes: Int) {
            val interval = 32L   // 每帧检查一次
            val maxRetry = 30    // 最多重试30次 ≈ 1秒超时
            var retry = 0        // 正确计数
            val runnable = object : Runnable {
                override fun run() {
                    // View 已销毁 / 超时 → 停止轮询
                    if (!toolbar.isAttachedToWindow || retry >= maxRetry) {
                        toolbar.removeCallbacks(this)
                        return
                    }
                    // 查找菜单
                    var found = false
                    for (i in 0 until toolbar.childCount) {
                        val child = toolbar.getChildAt(i)
                        if (child is ActionMenuView) {
                            if (child.isNotEmpty()) {
                                adjustActionMenuView(toolbar, child, colorRes)
                                found = true
                            }
                        }
                    }
                    // 找到/没找到
                    if (found) {
                        toolbar.removeCallbacks(this)
                    } else {
                        // 正确计数
                        retry++
                        toolbar.postDelayed(this, interval)
                    }
                }
            }
            toolbar.post(runnable)
        }

        @SuppressLint("RestrictedApi")
        private fun adjustActionMenuView(toolbar: Toolbar, menuView: ActionMenuView, @ColorRes colorRes: Int) {
            for (i in 0..<menuView.childCount) {
                val itemView = menuView.getChildAt(i)
                // 打破 ActionMenuItemView 的高度限制
                if (itemView is ActionMenuItemView) {
                    // 取消最小高度限制
                    itemView.setMinHeight(0)
                    // 取消最大高度限制
                    itemView.setMaxHeight(Int.MAX_VALUE)
                    // 强制 ActionMenuItemView 高度占满父容器（ActionMenuView）
                    val lp = itemView.layoutParams
                    lp.height = ViewGroup.LayoutParams.MATCH_PARENT
                    itemView.setLayoutParams(lp)
                    // 清除 ActionMenuItemView 自身的 padding
                    itemView.setPadding(itemView.getPaddingLeft(), 0, itemView.getPaddingRight(), 0)
                    // 内容居中
                    itemView.gravity = Gravity.CENTER
                    // 颜色调整
                    if (!shouldUseWhiteSystemBarsForRes(colorRes)) {
                        itemView.textColor(R.color.galleryFontDark)
                    } else {
                        itemView.textColor(R.color.galleryFontLight)
                    }
                    // 字体大小
                    itemView.textSize(R.dimen.textSize14)
                    // 大小修正 -> 判断这个按钮有没有 ICON
                    val hasIcon = itemView.itemData?.icon != null
                    if (hasIcon) {
                        val adjustHeight = toolbar.measuredHeight - defaultStatusBarHeight
                        itemView.size(width = adjustHeight)
                    }
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        // 开启谷歌全屏模式
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setActivityAnimations()
        // 强制补动画（外部跳转生效）
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            overridePendingTransition(R.anim.set_translate_right_in, R.anim.set_translate_left_out, Color.TRANSPARENT)
        } else {
            overridePendingTransition(R.anim.set_translate_right_in, R.anim.set_translate_left_out)
        }
        // 禁用ActionBar
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE)
        // 添加至统一页面管理类
        AppManager.addActivity(this)
        // 子页不实现方法走默认窗体配置(状态栏+导航栏)
        if (isImmersionBarEnabled()) initImmersionBar()
    }

    /**
     * 复用页面时强制统一动画
     * 虽然定义了全局动画,但使用FLAG_ACTIVITY_REORDER_TO_FRONT拉起栈内已有 Activity 时，触发的是关闭动画对应的配置而非启动动画,故而直接重写
     */
    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        setActivityAnimations()
    }

    private fun setActivityAnimations() {
        val (slideEnter, slideExit) = Pair(
            Slide(Gravity.END).apply { duration = 300; mode = Visibility.MODE_IN },
            Slide(Gravity.START).apply { duration = 300; mode = Visibility.MODE_OUT }
        )
        // 当 A 启动 B 时，A 被覆盖的过程 -> 应用于被启动的 Activity（B）
        window.setExitTransition(slideEnter)
        // 当 B 返回 A 时，B 退出的过程 -> 应用于返回的 Activity（B）
        window.setReturnTransition(slideExit)
    }

    protected open fun isImmersionBarEnabled(): Boolean {
        return true
    }

    protected open fun initImmersionBar(statusBarDark: Boolean = false, navigationBarDark: Boolean = false, navigationBarColor: Int = R.color.bgBlack) {
        window?.apply {
            setStatusBarLightMode(statusBarDark)
            setNavigationBarLightMode(navigationBarDark)
            setNavigationBarDrawable(navigationBarColor)
        }
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
            immersionBar?.apply {
                reset()
                statusBarDarkFont(statusBarDark, 0.2f)
                navigationBarDarkIcon(if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) navigationBarDark else false, 0.2f)
                init()
            }
        }
    }

    /**
     * 保存当前注册的回调（用于移除旧回调）
     */
    private var backCallback: Any? = null
    protected fun setOnBackPressedListener(onBackPressedListener: (() -> Unit)) {
        // 移除旧回调，避免重复执行
        clearOnBackPressedListener()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // API 33+ 使用 OnBackInvokedCallback
            val callback = OnBackInvokedCallback {
                onBackPressedListener.invoke()
            }
            onBackInvokedDispatcher.registerOnBackInvokedCallback(OnBackInvokedDispatcher.PRIORITY_DEFAULT, callback)
            backCallback = callback
        } else {
            // API <33 使用 OnBackPressedCallback
            val callback = object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    onBackPressedListener.invoke()
                }
            }
            onBackPressedDispatcher.addCallback(this, callback)
            backCallback = callback
        }
    }

    /**
     * 移除当前注册的返回回调（恢复默认返回行为）
     */
    protected fun clearOnBackPressedListener() {
        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> {
                (backCallback as? OnBackInvokedCallback)?.let {
                    onBackInvokedDispatcher.unregisterOnBackInvokedCallback(it)
                }
            }
            else -> {
                (backCallback as? OnBackPressedCallback)?.remove()
            }
        }
        backCallback = null
    }

    /**
     * 恢复默认返回行为（移除所有自定义回调）
     */
    protected fun restoreDefaultBackBehavior() {
        clearOnBackPressedListener()
    }

    /**
     * 1) bye() 方法中直接调用了 onBackPressed()
     * 2) 在未重写 onBackPressed() 的情况下，会执行 Activity 类的默认实现
     * 3) 系统默认的 onBackPressed() 最终会调用 finish() 销毁当前 Activity
     */
    override fun bye() {
//        onBackPressed()
        finish()
    }

    override fun getResources(): Resources {
        if (isMainThread) {
            AutoSizeConfig.getInstance()
                .setScreenWidth(screenWidth)
                .setScreenHeight(screenHeight)
            AutoSizeCompat.autoConvertDensityOfGlobal(super.getResources())
        }
        return super.getResources()
    }

    override fun onStop() {
        super.onStop()
        AutoSizeConfig.getInstance().stop(this)
    }

    override fun onRestart() {
        super.onRestart()
        AutoSizeConfig.getInstance().restart()
    }

    override fun finish() {
        super.finish()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            overridePendingTransition(R.anim.set_translate_left_in, R.anim.set_translate_right_out, Color.TRANSPARENT)
        } else {
            overridePendingTransition(R.anim.set_translate_left_in, R.anim.set_translate_right_out)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        window?.removeNavigationBarDrawable()
        clearOnBackPressedListener()
        AppManager.removeActivity(this)
    }

}