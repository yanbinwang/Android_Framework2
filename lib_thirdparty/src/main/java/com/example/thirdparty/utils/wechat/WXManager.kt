package com.example.thirdparty.utils.wechat

import android.annotation.SuppressLint
import android.content.IntentFilter
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LifecycleOwner
import com.example.common.config.Constants
import com.example.framework.utils.function.doOnReceiver
import com.example.thirdparty.utils.wechat.service.WXReceiver
import com.tencent.mm.opensdk.constants.ConstantsAPI
import com.tencent.mm.opensdk.openapi.IWXAPI
import com.tencent.mm.opensdk.openapi.WXAPIFactory
import java.lang.ref.WeakReference
import java.util.concurrent.ConcurrentHashMap

/**
 * 无论创建多少个 IWXAPI 实例（只要 AppId 一致），微信 SDK 内部都会关联到同一个应用的通信通道：
 * 每个 IWXAPI 实例的核心功能（调起微信、接收分享 / 登录回调、注册 AppId 等）完全独立且等效；
 * 即使 A 页面的 IWXAPI 实例、B 页面的 IWXAPI 实例同时存在，也不会出现 “抢占通信通道”“回调错乱” 的问题，微信 SDK 会正常处理所有有效实例的请求。
 */
@SuppressLint("UnspecifiedRegisterReceiverFlag")
class WXManager private constructor() {
    // 保证多页面并发调用 regToWx/unRegToWx 时的线程安全，避免 HashMap 在多线程下的并发修改异常
    private val wxApiMap by lazy { ConcurrentHashMap<WeakReference<LifecycleOwner>, IWXAPI>() }

    companion object {
        @JvmStatic
        val instance by lazy { WXManager() }
    }

    /**
     * 注册到微信
     */
    fun regToWx(mActivity: FragmentActivity?): IWXAPI? {
        mActivity ?: return null
        // 如果之前的 FragmentActivity 存在，取消并从集合中移除
        unRegToWx(mActivity)
        // 通过WXAPIFactory工厂，获取IWXAPI的实例
        val api = WXAPIFactory.createWXAPI(mActivity, Constants.WX_APP_ID, true)
        // 将应用的appId注册到微信
        api.registerApp(Constants.WX_APP_ID)
        // 创建注册广播
        val wxReceiver = WXReceiver(api)
        // 动态监听微信启动广播进行注册到微信
        mActivity.apply {
            doOnReceiver(this, wxReceiver, IntentFilter(ConstantsAPI.ACTION_REFRESH_WXAPP), false) {
                unRegToWx(this)
            }
        }
//        val intentFilter = IntentFilter(ConstantsAPI.ACTION_REFRESH_WXAPP)
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
//            mActivity.registerReceiver(wxReceiver, intentFilter, Context.RECEIVER_NOT_EXPORTED)
//        } else {
//            mActivity.registerReceiver(wxReceiver, intentFilter)
//        }
//        // 注销广播接收器
//        mActivity.doOnDestroy {
//            try {
//                mActivity.unregisterReceiver(wxReceiver)
//            } catch (e: Exception) {
//                e.printStackTrace()
//            }
//            unRegToWx(mActivity)
//        }
        // 存储该api
        wxApiMap[WeakReference(mActivity)] = api
        // 返回该实例
        return api
    }

    /**
     * 注销微信
     */
    fun unRegToWx(owner: LifecycleOwner?) {
        owner ?: return
        // 遍历集合中所有值
        val iterator = wxApiMap.entries.iterator()
        while (iterator.hasNext()) {
            val entry = iterator.next()
            // 取出存储时的WeakReference（RefA）
            val keyWeakRef = entry.key
            // 获取RefA包装的页面实例
            val targetOwner = keyWeakRef.get()
            // 对比「包装的页面实例」，而非「WeakReference对象本身」，增加targetOwner == null 的判断，清理无效条目
            if (targetOwner == owner || targetOwner == null) {
                entry.value.unregisterApp()
                iterator.remove()
                // 仅在匹配到当前页面时break，清理无效条目时继续遍历
                if (targetOwner == owner) {
                    break
                }
            }
        }
    }

    /**
     * Application的onTerminate或别处页面需要全局清空调取
     */
    fun unRegToWx() {
        wxApiMap.values.forEach {
            it.unregisterApp()
        }
        wxApiMap.clear()
    }

}