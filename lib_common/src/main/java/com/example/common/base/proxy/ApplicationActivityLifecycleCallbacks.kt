package com.example.common.base.proxy

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Application.ActivityLifecycleCallbacks
import android.os.Build
import android.os.Bundle
import android.os.SystemClock
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.AbsListView
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import com.example.common.BaseApplication.Companion.isFirstLaunch
import com.example.common.BaseApplication.Companion.lastClickTime
import com.example.common.R
import com.example.common.utils.setNavigationBarLightMode
import com.example.common.utils.setStatusBarLightMode
import com.example.common.utils.setSystemBarDrawable
import com.example.framework.utils.logE
import com.gyf.immersionbar.ImmersionBar
import java.util.Locale

/**
 * Created by WangYanBin on 2020/8/10.
 */
@SuppressLint("DiscouragedPrivateApi", "PrivateApi")
class ApplicationActivityLifecycleCallbacks : ActivityLifecycleCallbacks {
    // key：activity 类名前缀 value：对应系统栏配置
    private val thirdPageConfigMap: Map<String, ThirdSystemBarConfig> = mapOf(
//        "io.rong.imkit" to ThirdSystemBarConfig(
//            statusBarColorRes = R.color.rongStatusBar,
//            statusBarLightMode = false,
//            navBarColorRes = R.color.rongNavigationBar,
//            navBarLightMode = false
//        ),
//        "zendeskxxx" to ThirdSystemBarConfig(
//            statusBarColorRes = R.color.zendeskStatusBar,
//            statusBarLightMode = false,
//            navBarColorRes = R.color.zendeskNavigationBar,
//            navBarLightMode = false
//        )
    )

    override fun onActivityPaused(activity: Activity) {
    }

    override fun onActivityResumed(activity: Activity) {
    }

    override fun onActivityStarted(activity: Activity) {
    }

    override fun onActivityDestroyed(activity: Activity) {
    }

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
    }

    override fun onActivityStopped(activity: Activity) {
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        if (isFirstLaunch.get()) {
            isFirstLaunch.set(false)
        } else {
            // 注意混淆配置添加: -keep class com.example.SplashActivity {*;}
            val clazzName = activity.javaClass.simpleName.lowercase(Locale.getDefault())
            if (clazzName == "splashactivity") {
                lastClickTime.set(SystemClock.elapsedRealtime())
            }
        }
        val decorView = activity.window?.decorView ?: return
        val observer = decorView.viewTreeObserver
        if(!observer.isAlive) return
        // 检测是否三方库
        matchConfig(activity)?.let { config ->
            val isLow = Build.VERSION.SDK_INT < Build.VERSION_CODES.R
            val immersionBar = if (isLow) ImmersionBar.with(activity) else null
            val listener = object : ViewTreeObserver.OnPreDrawListener {
                override fun onPreDraw(): Boolean {
                    // 立即移除
                    try {
                        observer.removeOnPreDrawListener(this)
                    } catch (_: IllegalStateException) {
                        // observer已经死亡，移除失败
                    }
                    // 此时 decorView 已完成 measure/layout，安全操作
                    val window = activity.window
                    window.setStatusBarLightMode(config.statusBarLightMode)
                    window.setNavigationBarLightMode(config.navBarLightMode)
                    window.setSystemBarDrawable(config.statusBarColorRes, config.navBarColorRes) { insets ->
                        val statusBarTop = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top
                        val navBottom = insets.getInsets(WindowInsetsCompat.Type.navigationBars()).bottom
                        val left = insets.getInsets(WindowInsetsCompat.Type.systemBars()).left
                        val right = insets.getInsets(WindowInsetsCompat.Type.systemBars()).right
                        if (decorView.paddingTop != statusBarTop || decorView.paddingLeft != left || decorView.paddingRight != right) {
                            decorView.setPadding(left, statusBarTop, right, navBottom)
                        }
                    }
                    if (isLow) {
                        immersionBar?.apply {
                            reset()
                            statusBarDarkFont(config.statusBarLightMode, 0.2f)
                            navigationBarDarkIcon(if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) config.navBarLightMode else false, 0.2f)
                            init()
                        }
                    }
                    // true: 表示继续本次绘制 false: 会跳过本帧绘制（用于需要重新 layout 的场景）
                    return true
                }
            }
            observer.addOnPreDrawListener(listener)
        }
        // 点击事件防高频点击
        observer.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                // 判断 ViewTreeObserver 是否仍有效 在极少数情况下（如 Activity 销毁时布局尚未完成），viewTreeObserver 可能已失效，此时调用 removeOnGlobalLayoutListener 会抛出异常
                try {
                    observer.removeOnGlobalLayoutListener(this)
                } catch (_: IllegalStateException) {
                    // observer已经死亡，无法移除，忽略
                }
                proxyOnClick(decorView, 5)
            }
        })
    }

    /**
     * 根据Activity类名匹配获取对应的配置，无匹配返回 null
     */
    private fun matchConfig(activity: Activity): ThirdSystemBarConfig? {
        val className = activity.javaClass.name
        return thirdPageConfigMap.entries.firstOrNull { (prefix, _) ->
            className.startsWith(prefix, ignoreCase = true)
        }?.value
    }

    private fun proxyOnClick(view: View, recycledDeep: Int) {
        var recycledContainerDeep = recycledDeep
        if (view.isVisible) {
            if (view is ViewGroup) {
                val existAncestorRecycle = recycledContainerDeep > 0
                if (view !is AbsListView || existAncestorRecycle) {
                    getClickListenerForView(view)
                    if (existAncestorRecycle) recycledContainerDeep++
                } else {
                    recycledContainerDeep = 1
                }
                val childCount = view.childCount
                for (i in 0 until childCount) {
                    proxyOnClick(view.getChildAt(i), recycledContainerDeep)
                }
            } else {
                getClickListenerForView(view)
            }
        }
    }

    private fun getClickListenerForView(view: View) {
        try {
            val viewClazz = Class.forName("android.view.View")
            val listenerInfoMethod = viewClazz.getDeclaredMethod("getListenerInfo")
            if (!listenerInfoMethod.isAccessible) listenerInfoMethod.isAccessible = true
            val listenerInfoInstance = listenerInfoMethod.invoke(view)
            val listenerInfoClazz = Class.forName("android.view.View\$ListenerInfo")
            val onClickListenerField = listenerInfoClazz.getDeclaredField("mOnClickListener")
            if (!onClickListenerField.isAccessible) onClickListenerField.isAccessible = true
            val originClickListener = onClickListenerField[listenerInfoInstance] as? View.OnClickListener
            if (originClickListener !is ProxyOnClickListener) {
                onClickListenerField[listenerInfoInstance] = ProxyOnClickListener(originClickListener)
            } else {
                "setted proxy listener".logE("OnClickListenerProxy")
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    internal class ProxyOnClickListener(private val onClick: View.OnClickListener?) : View.OnClickListener {
        private var lastClickTime = 0L

        override fun onClick(v: View?) {
            // 点击时间控制
            val currentTime = System.currentTimeMillis()
            val minClickDelayTime = 500L
            if (currentTime - lastClickTime > minClickDelayTime) {
                lastClickTime = currentTime
                onClick?.onClick(v)
            }
        }
    }

    /**
     * 第三方页面系统栏配置
     * @param statusBarColorRes 状态栏颜色资源
     * @param statusBarLightMode 状态栏图标是否浅色
     * @param navBarColorRes 导航栏颜色资源
     * @param navBarLightMode 导航栏图标是否浅色
     */
    internal data class ThirdSystemBarConfig(
        val statusBarColorRes: Int = R.color.appStatusBar,
        val statusBarLightMode: Boolean = true,
        val navBarColorRes: Int = R.color.appNavigationBar,
        val navBarLightMode: Boolean = true
    )

}