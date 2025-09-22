package com.yanzhenjie.album.mvp

import android.os.Build
import android.os.Bundle
import android.view.Window
import android.window.OnBackInvokedCallback
import android.window.OnBackInvokedDispatcher
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.annotation.ColorRes
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.graphics.ColorUtils.calculateLuminance
import com.example.common.R
import com.example.common.utils.function.color
import com.example.common.utils.function.getStatusBarHeight
import com.example.common.utils.manager.AppManager
import com.example.common.utils.removeNavigationBarDrawable
import com.example.common.utils.setNavigationBarDrawable
import com.example.common.utils.setNavigationBarLightMode
import com.example.common.utils.setStatusBarLightMode
import com.example.framework.utils.function.view.doOnceAfterLayout
import com.example.framework.utils.function.view.padding
import com.example.framework.utils.function.view.size
import com.gyf.immersionbar.ImmersionBar

/**
 * 针对所有相册页面的基类
 */
abstract class BaseActivity : AppCompatActivity(), Bye {
    private val immersionBar by lazy { ImmersionBar.with(this) }

    companion object {

        /**
         * 兼容控件内toolbar
         */
        @JvmStatic
        fun setSupportToolbar(toolbar: Toolbar) {
            toolbar.doOnceAfterLayout {
                it.size(height = it.measuredHeight + getStatusBarHeight())
                it.padding(top = getStatusBarHeight())
            }
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        // 开启谷歌全屏模式
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        // 禁用ActionBar
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE)
        // 添加至统一页面管理类
        AppManager.addActivity(this)
        // 子页不实现方法走默认窗体配置(状态栏+导航栏)
        if (isImmersionBarEnabled()) initImmersionBar()
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
        immersionBar?.apply {
            reset()
            statusBarDarkFont(statusBarDark, 0.2f)
            navigationBarDarkIcon(if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) navigationBarDark else false, 0.2f)//edge会导致低版本ui深浅代码失效,但是会以传入的颜色值为主(偏深为白,反之为黑)
            init()
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
     * 1.bye() 方法中直接调用了 onBackPressed()
     * 2.在未重写 onBackPressed() 的情况下，会执行 Activity 类的默认实现
     * 3.系统默认的 onBackPressed() 最终会调用 finish() 销毁当前 Activity
     */
    override fun bye() {
//        onBackPressed()
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        window?.removeNavigationBarDrawable()
        clearOnBackPressedListener()
        AppManager.removeActivity(this)
    }

}