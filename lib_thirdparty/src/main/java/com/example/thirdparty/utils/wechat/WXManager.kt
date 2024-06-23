package com.example.thirdparty.utils.wechat

import android.annotation.SuppressLint
import android.content.Context
import android.content.IntentFilter
import android.os.Build
import androidx.lifecycle.LifecycleOwner
import com.example.common.BaseApplication
import com.example.common.config.Constants
import com.example.framework.utils.function.doOnDestroy
import com.example.thirdparty.utils.wechat.service.WXReceiver
import com.tencent.mm.opensdk.constants.ConstantsAPI
import com.tencent.mm.opensdk.openapi.IWXAPI
import com.tencent.mm.opensdk.openapi.WXAPIFactory

@SuppressLint("UnspecifiedRegisterReceiverFlag")
class WXManager private constructor() {
    //WXAPI 是第三方app和微信通信的openApi接口
    private var api: IWXAPI? = null
    //动态监听微信启动广播进行注册到微信
    private val wxReceiver by lazy { WXReceiver(api) }
    //上下文
    private val mContext by lazy { BaseApplication.instance.applicationContext }

    companion object {
        @JvmStatic
        val instance by lazy { WXManager() }
    }

    /**
     * 注册到微信
     */
    fun regToWx(owner: LifecycleOwner? = null): IWXAPI? {
        //通过WXAPIFactory工厂，获取IWXAPI的实例
        api = WXAPIFactory.createWXAPI(mContext, Constants.WX_APP_ID, true)
        //将应用的appId注册到微信
        api?.registerApp(Constants.WX_APP_ID)
        //动态监听微信启动广播进行注册到微信
        val intentFilter = IntentFilter(ConstantsAPI.ACTION_REFRESH_WXAPP)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            mContext?.registerReceiver(wxReceiver, intentFilter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            mContext?.registerReceiver(wxReceiver, intentFilter)
        }
        owner?.doOnDestroy { unRegToWx() }
        return api
    }

    /**
     * 注销微信
     */
    fun unRegToWx() {
        api?.unregisterApp()
        mContext?.unregisterReceiver(wxReceiver)
        api = null
    }

}