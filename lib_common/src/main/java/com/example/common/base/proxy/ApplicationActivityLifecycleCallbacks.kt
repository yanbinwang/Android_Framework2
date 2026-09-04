package com.example.common.base.proxy

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Application.ActivityLifecycleCallbacks
import android.os.Bundle
import android.os.SystemClock
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.AbsListView
import androidx.core.view.isVisible
import com.example.common.BaseApplication.Companion.isFirstLaunch
import com.example.common.BaseApplication.Companion.lastClickTime
import com.example.framework.utils.logE
import java.util.Locale

/**
 * Created by WangYanBin on 2020/8/10.
 */
@SuppressLint("DiscouragedPrivateApi", "PrivateApi")
class ApplicationActivityLifecycleCallbacks : ActivityLifecycleCallbacks {

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

}