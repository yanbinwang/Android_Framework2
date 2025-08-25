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
import com.example.common.BaseApplication.Companion.isFirstLaunch
import com.example.common.BaseApplication.Companion.lastClickTime
import com.example.framework.utils.LogUtil.e
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
            val clazzName = activity.javaClass.simpleName.lowercase(Locale.getDefault())
            if (clazzName == "splashactivity") {
                lastClickTime.set(SystemClock.elapsedRealtime())
            }
        }
//        activity.window?.decorView?.viewTreeObserver?.addOnGlobalLayoutListener {
//            proxyOnClick(activity.window.decorView, 5)
//        }
        val decorView = activity.window?.decorView
        decorView?.viewTreeObserver?.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                proxyOnClick(decorView, 5)
                // 判断 ViewTreeObserver 是否仍有效 在极少数情况下（如 Activity 销毁时布局尚未完成），viewTreeObserver 可能已失效，此时调用 removeOnGlobalLayoutListener 会抛出异常
                if (decorView.viewTreeObserver.isAlive) {
                    decorView.viewTreeObserver?.removeOnGlobalLayoutListener(this)
                }
            }
        })
    }

    private fun proxyOnClick(view: View?, recycledDeep: Int) {
        var recycledContainerDeep = recycledDeep
        if (view?.visibility == View.VISIBLE) {
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

    private fun getClickListenerForView(view: View?) {
        try {
            val viewClazz = Class.forName("android.view.View")
            //事件监听器都是这个实例保存的
            val listenerInfoMethod = viewClazz.getDeclaredMethod("getListenerInfo")
            if (!listenerInfoMethod.isAccessible) listenerInfoMethod.isAccessible = true
            val listenerInfoObj = listenerInfoMethod.invoke(view)
            val listenerInfoClazz = Class.forName("android.view.View\$ListenerInfo")
            val onClickListenerField = listenerInfoClazz.getDeclaredField("mOnClickListener")
            if (!onClickListenerField.isAccessible) onClickListenerField.isAccessible = true
            val mOnClickListener = onClickListenerField[listenerInfoObj] as? View.OnClickListener
            if (mOnClickListener !is ProxyOnclickListener) {
                //自定义代理事件监听器
                onClickListenerField[listenerInfoObj] = ProxyOnclickListener(mOnClickListener)
            } else {
                e("OnClickListenerProxy", "setted proxy listener ")
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private class ProxyOnclickListener(private var onclick: View.OnClickListener?) : View.OnClickListener {
        private var lastClickTime: Long = 0

        override fun onClick(v: View?) {
            //点击时间控制
            val currentTime = System.currentTimeMillis()
            val minClickDelayTime = 500
            if (currentTime - lastClickTime > minClickDelayTime) {
                lastClickTime = currentTime
                if (onclick != null) onclick?.onClick(v)
            }
        }
    }

}