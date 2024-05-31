package com.example.thirdparty.utils

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import com.example.common.config.Constants.WX_APP_ID
import com.example.thirdparty.pay.utils.wechat.WechatPayBuilder
import com.example.thirdparty.share.utils.wechat.ShareResult
import com.example.thirdparty.share.utils.wechat.WechatShareBuilder
import com.tencent.mm.opensdk.constants.ConstantsAPI
import com.tencent.mm.opensdk.modelmsg.SendMessageToWX
import com.tencent.mm.opensdk.modelpay.PayReq
import com.tencent.mm.opensdk.openapi.IWXAPI
import com.tencent.mm.opensdk.openapi.WXAPIFactory

@SuppressLint("UnspecifiedRegisterReceiverFlag")
class WXManager private constructor() {
    private var mContext: Context? = null

    //WXAPI 是第三方app和微信通信的openApi接口
    private var api: IWXAPI? = null

    //支付
    private val pay by lazy { WechatPayBuilder() }

    //分享
    private val share by lazy { WechatShareBuilder() }

    //动态监听微信启动广播进行注册到微信
    private val broadcastReceiver by lazy { WXBroadcastReceiver(api) }

    companion object {
        @JvmStatic
        val instance by lazy { WXManager() }
    }

    /**
     * 初始化
     */
    fun init(mContext: Context) {
        this.mContext = mContext.applicationContext
        regToWx()
    }

    /**
     * 注册到微信
     */
    private fun regToWx() {
        // 通过WXAPIFactory工厂，获取IWXAPI的实例
        api = WXAPIFactory.createWXAPI(mContext, WX_APP_ID, true)
        //将应用的appId注册到微信
        api?.registerApp(WX_APP_ID)
        //动态监听微信启动广播进行注册到微信
        val intentFilter = IntentFilter(ConstantsAPI.ACTION_REFRESH_WXAPP)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            mContext?.registerReceiver(
                broadcastReceiver,
                intentFilter,
                Context.RECEIVER_NOT_EXPORTED
            )
        } else {
            mContext?.registerReceiver(broadcastReceiver, intentFilter)
        }
    }

    /**
     * 注销微信
     */
    fun unRegToWx() {
        api?.unregisterApp()
        mContext?.unregisterReceiver(broadcastReceiver)
        api = null
        mContext = null
    }

    /**
     * 获取微信openApi接口
     */
    fun getWXAPI(): IWXAPI? {
        return api
    }

    /**
     * 发起支付
     */
    fun pay(req: PayReq?) {
        pay.pay(req)
    }

    /**
     * 分享
     */
    fun share(result: ShareResult?, mTargetScene: Int = SendMessageToWX.Req.WXSceneSession) {
        share.setResult(result)
        share.shareToWx(mTargetScene)
    }

}

/**
 * 微信动态注册广播
 */
private class WXBroadcastReceiver(private val api: IWXAPI?) : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        //将该app注册到微信
        api?.registerApp(WX_APP_ID)
    }
}