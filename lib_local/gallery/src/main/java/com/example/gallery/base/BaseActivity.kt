package com.example.gallery.base

import android.content.Intent
import android.content.res.Resources
import android.os.Build
import android.os.Bundle
import android.transition.Slide
import android.transition.Visibility
import android.view.Gravity
import android.view.Window
import android.window.OnBackInvokedCallback
import android.window.OnBackInvokedDispatcher
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.example.common.utils.ScreenUtil.screenHeight
import com.example.common.utils.ScreenUtil.screenWidth
import com.example.common.utils.manager.AppManager
import com.example.common.utils.removeNavigationBarDrawable
import com.example.common.utils.setNavigationBarDrawable
import com.example.common.utils.setNavigationBarLightMode
import com.example.common.utils.setStatusBarLightMode
import com.example.framework.utils.function.value.isMainThread
import com.example.gallery.R
import com.example.gallery.base.bridge.PageCloseable
import com.gyf.immersionbar.ImmersionBar
import me.jessyan.autosize.AutoSizeCompat
import me.jessyan.autosize.AutoSizeConfig
import java.util.concurrent.ConcurrentHashMap

/**
 * 针对所有相册页面的基类
 */
abstract class BaseActivity : AppCompatActivity(), PageCloseable {
    private val immersionBar by lazy { ImmersionBar.with(this) }
    private val dataManager by lazy { ConcurrentHashMap<MutableLiveData<*>, Observer<Any?>>() }

    override fun onCreate(savedInstanceState: Bundle?) {
        // 开启谷歌全屏模式
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        // 设置相册整体动画
        setActivityAnimations()
        // 强制补动画（外部跳转生效）
        overridePendingTransition(R.anim.set_translate_right_in, R.anim.set_translate_left_out)
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
        window.exitTransition = slideEnter
        // 当 B 返回 A 时，B 退出的过程 -> 应用于返回的 Activity（B）
        window.returnTransition = slideExit
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
     * ViewModel 中定义无值事件（用 Unit 替代 Any）
     * val reason by lazy { MutableLiveData<Unit>() } // 无值事件
     * Unit 类型的 value 是 Unit 实例（非 null），会触发回调
     */
    protected fun <T> MutableLiveData<T>?.observe(block: T.() -> Unit) {
        this ?: return
        val observer = Observer<Any?> { value ->
            if (value != null) {
                (value as? T)?.let { block(it) }
            }
        }
        dataManager[this] = observer
        observe(this@BaseActivity, observer)
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
    override fun navigateBack() {
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

    override fun onDestroy() {
        super.onDestroy()
        window?.removeNavigationBarDrawable()
        clearOnBackPressedListener()
        AppManager.removeActivity(this)
        for ((key, value) in dataManager) {
            key.removeObserver(value)
        }
        dataManager.clear()
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(R.anim.set_translate_left_in, R.anim.set_translate_right_out)
    }

}