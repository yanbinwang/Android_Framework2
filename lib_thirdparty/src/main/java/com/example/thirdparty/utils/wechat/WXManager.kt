package com.example.thirdparty.utils.wechat

import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LifecycleOwner
import com.example.common.config.Constants
import com.tencent.mm.opensdk.openapi.IWXAPI
import com.tencent.mm.opensdk.openapi.WXAPIFactory
import java.lang.ref.WeakReference
import java.util.concurrent.ConcurrentHashMap

/**
 * 无论创建多少个 IWXAPI 实例（只要 AppId 一致），微信 SDK 内部都会关联到同一个应用的通信通道：
 * 每个 IWXAPI 实例的核心功能（调起微信、接收分享 / 登录回调、注册 AppId 等）完全独立且等效；
 * 即使 A 页面的 IWXAPI 实例、B 页面的 IWXAPI 实例同时存在，也不会出现 “抢占通信通道”“回调错乱” 的问题，微信 SDK 会正常处理所有有效实例的请求。
 * ConcurrentHashMap:
 *  Key -> 使用 WeakReference<LifecycleOwner>，满足需求：App退后台页面被系统回收后，GC 会回收页面对象，下次遍历自动移除无效条目
 *  Value -> IWXAPI 不使用弱引用：实例生命周期跟随页面，页面销毁同步释放，无需额外弱引用包装
 */
class WXManager private constructor() {
    // 保证多页面并发调用 regToWx/unRegToWx 时的线程安全，避免 HashMap 在多线程下的并发修改异常
    private val wxApiMap by lazy { ConcurrentHashMap<WeakReference<LifecycleOwner>, IWXAPI>() }

    companion object {
        val instance by lazy { WXManager() }
    }

    /**
     * 注册到微信
     */
    fun regToWx(mActivity: FragmentActivity?): IWXAPI? {
        mActivity ?: return null
        // 先查找当前页面是否已有有效 api，直接复用
        val existApi = wxApiMap.entries.find { entry ->
            entry.key.get() === mActivity
        }?.value
        if (null != existApi) return existApi
        // 如果之前的 FragmentActivity 存在，取消并从集合中移除
        unRegToWx(mActivity)
        // 通过 WXAPIFactory 工厂，获取 IWXAPI 的实例
        val api = WXAPIFactory.createWXAPI(mActivity, Constants.WX_APP_ID, true)
        // 将应用的 appId 注册到微信
        api.registerApp(Constants.WX_APP_ID)
        // 存储该 api
        wxApiMap[WeakReference(mActivity)] = api
        // 返回该实例
        return api
    }

    /**
     * 注销微信
     */
    fun unRegToWx(owner: LifecycleOwner?) {
        owner ?: return
        /**
         * 1) 对比「包装的页面实例」，而非「 WeakReference 对象本身」，增加 targetOwner == null 的判断，清理无效条目
         * 2) 旧页面对象无任何强引用，GC 后 WeakReference.get() = null
         */
        wxApiMap.entries.removeAll { entry ->
            val target = entry.key.get()
            val needRemove = target == null || target === owner
            if (needRemove) {
                entry.value.unregisterApp()
            }
            needRemove
        }
    }

    /**
     * Application 的 onTerminate 或别处页面需要全局清空调取
     */
    fun unRegToWx() {
        wxApiMap.values.forEach { api ->
            api.unregisterApp()
        }
        wxApiMap.clear()
    }

}