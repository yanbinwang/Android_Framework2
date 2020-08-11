package com.example.common.base.proxy

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Application.ActivityLifecycleCallbacks
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.AbsListView
import com.example.base.utils.LogUtil.e

/**
 * Created by WangYanBin on 2020/8/10.
 */
@SuppressLint("DiscouragedPrivateApi", "PrivateApi")
class ApplicationActivityLifecycleCallbacks : ActivityLifecycleCallbacks {

    override fun onActivityPaused(activity: Activity?) {
    }

    override fun onActivityResumed(activity: Activity?) {
    }

    override fun onActivityStarted(activity: Activity?) {
    }

    override fun onActivityDestroyed(activity: Activity?) {
    }

    override fun onActivitySaveInstanceState(activity: Activity?, outState: Bundle?) {
    }

    override fun onActivityStopped(activity: Activity?) {
    }

    override fun onActivityCreated(activity: Activity?, savedInstanceState: Bundle?) {
        activity?.window?.decorView?.viewTreeObserver?.addOnGlobalLayoutListener {
            proxyOnClick(activity.window.decorView, 5)
        }
    }

    private fun proxyOnClick(view: View?, recycledDeep: Int) {
        var recycledContainerDeep = recycledDeep
        if (view?.visibility == View.VISIBLE) {
            if (view is ViewGroup) {
                val existAncestorRecycle = recycledContainerDeep > 0
                if (view !is AbsListView || existAncestorRecycle) {
                    getClickListenerForView(view)
                    if (existAncestorRecycle) {
                        recycledContainerDeep++
                    }
                } else {
                    recycledContainerDeep = 1
                }
                val childCount = view.childCount
                for (i in 0 until childCount) {
                    val child = view.getChildAt(i)
                    proxyOnClick(child, recycledContainerDeep)
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
            if (!listenerInfoMethod.isAccessible) {
                listenerInfoMethod.isAccessible = true
            }
            val listenerInfoObj = listenerInfoMethod.invoke(view)
            val listenerInfoClazz = Class.forName("android.view.View\$ListenerInfo")
            val onClickListenerField = listenerInfoClazz.getDeclaredField("mOnClickListener")
            if (!onClickListenerField.isAccessible) {
                onClickListenerField.isAccessible = true
            }
            val mOnClickListener = onClickListenerField[listenerInfoObj] as? View.OnClickListener
            if (mOnClickListener !is ProxyOnclickListener) {
                //自定义代理事件监听器
                val onClickListenerProxy: View.OnClickListener =
                    ProxyOnclickListener(mOnClickListener)
                //更换
                onClickListenerField[listenerInfoObj] = onClickListenerProxy
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