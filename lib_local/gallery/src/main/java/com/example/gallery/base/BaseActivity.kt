package com.example.gallery.base

import android.content.Intent
import android.content.res.Resources
import android.os.Build
import android.os.Bundle
import android.transition.Slide
import android.transition.Visibility
import android.view.Gravity
import android.view.View
import android.view.ViewTreeObserver
import android.view.Window
import android.window.OnBackInvokedCallback
import android.window.OnBackInvokedDispatcher
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.example.common.base.bridge.BaseImpl
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
import me.jessyan.autosize.internal.CancelAdapt
import me.jessyan.autosize.internal.CustomAdapt
import java.util.concurrent.ConcurrentHashMap

/**
 * 针对所有相册页面的基类
 */
abstract class BaseActivity : AppCompatActivity(), BaseImpl, PageCloseable {
    private var onWindowInsetsChanged: ((insets: WindowInsetsCompat) -> Unit)? = null
    private val immersionBar by lazy { ImmersionBar.with(this) }
    private val dataManager by lazy { ConcurrentHashMap<MutableLiveData<*>, Observer<Any?>>() }

    override fun onCreate(savedInstanceState: Bundle?) {
        // 开启谷歌全屏模式
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        // 设置相册整体动画 (Activity 内部的 View 层级（内容区域）栈内 Activity 之间的共享元素过渡或内容过渡)
        setActivityAnimations()
        /**
         * 强制补动画（外部跳转生效）
         * 主要处理 Activity 作为整体被创建/销毁时 的窗口级过渡：
         * 1) 外部（如通知栏、桌面、其他App）启动该 Activity
         * 2) 栈内正常 startActivity() 且目标 Activity 不在栈中（新建实例）
         * 3) finish() 退出时
         * 不生效场景：
         * 1) FLAG_ACTIVITY_REORDER_TO_FRONT / singleTask 复用已有实例时，因为 Activity 没有被重新创建，Window 级别的 pending transition 不会被触发 (onNewIntent 里无法用它补动画)
         */
        overridePendingTransition(R.anim.set_translate_right_in, R.anim.set_translate_left_out)
        // 禁用ActionBar
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE)
        // 布局开始之前回调
        initBefore()
        // 添加至统一页面管理类
        AppManager.addActivity(this)
        // 子页不实现方法走默认窗体配置(状态栏+导航栏)
        if (isImmersionBarEnabled()) initImmersionBar()
        initView(savedInstanceState)
        initEvent()
        initData()
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

    override fun initImmersionBar(statusBarDark: Boolean, navigationBarDark: Boolean, navigationBarColor: Int) {
        super.initImmersionBar(statusBarDark, navigationBarDark, navigationBarColor)
        window?.apply {
            setStatusBarLightMode(statusBarDark)
            setNavigationBarLightMode(navigationBarDark)
            setNavigationBarDrawable(navigationBarColor) {
                onWindowInsetsChanged?.invoke(it)
            }
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
     * 注册一次性 OnPreDraw 监听；view完成第一次绘制前执行block，执行后自动移除监听，防止重复回调与内存泄漏
     * @param targetView 监听依附的View，为空则block不会执行
     * @param block 预绘制回调业务逻辑，仅执行一次
     */
    protected fun doOnViewPreDraw(targetView: View?, block: () -> Unit) {
        targetView ?: return
        val observer = targetView.viewTreeObserver
        if(!observer.isAlive) return
        val listener = object : ViewTreeObserver.OnPreDrawListener {
            override fun onPreDraw(): Boolean {
                try {
                    observer.removeOnPreDrawListener(this)
                } catch (_: IllegalStateException) {
                    // observer已经死亡，移除失败
                }
                block.invoke()
                return true
            }
        }
        observer.addOnPreDrawListener(listener)
    }

    /**
     * ViewModel 中定义无值事件（用 Unit 替代 Any）
     * val reason by lazy { MutableLiveData<Unit>() } // 无值事件
     * Unit 类型的 value 是 Unit 实例（非 null），会触发回调
     */
    protected fun <T> MutableLiveData<T>?.observe(block: T.() -> Unit) {
        this ?: return
        dataManager[this]?.let { oldObserver ->
            removeObserver(oldObserver)
        }
        val storeObserver = Observer<Any?> { value ->
            // 只是内部过滤空逻辑，不代表回调不会进来 null，入参本身依然是可空
            if (value != null) {
                (value as? T)?.let {
                    block(it)
                }
            }
        }
        observe(this@BaseActivity, storeObserver)
        dataManager[this] = storeObserver
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
            onBackInvokedDispatcher.registerOnBackInvokedCallback(
                OnBackInvokedDispatcher.PRIORITY_DEFAULT,
                callback
            )
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
     * 用于设置自定义Insets处理逻辑
     */
    protected fun setOnWindowInsetsChanged(onWindowInsetsChanged: (insets: WindowInsetsCompat) -> Unit) {
        this.onWindowInsetsChanged = onWindowInsetsChanged
    }

    protected fun clearOnWindowInsetsChanged() {
        onWindowInsetsChanged = null
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
        val res = super.getResources()
        if (isMainThread) {
            when (this) {
                is CancelAdapt -> {
                    AutoSizeCompat.cancelAdapt(res)
                }
                is CustomAdapt -> {
                    // CustomAdapt页面：交给AutoSize框架attachBaseContext处理，基类不要做全局覆盖
                }
                else -> {
                    AutoSizeCompat.autoConvertDensityOfGlobal(res)
                }
            }
        }
        return res
    }

    override fun onDestroy() {
        super.onDestroy()
        window?.removeNavigationBarDrawable()
        clearOnBackPressedListener()
        clearOnWindowInsetsChanged()
        AppManager.removeActivity(this)
        for ((liveData, obs) in dataManager) {
            liveData.removeObserver(obs)
        }
        dataManager.clear()
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(R.anim.set_translate_left_in, R.anim.set_translate_right_out)
    }

}