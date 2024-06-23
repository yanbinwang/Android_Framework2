package com.example.thirdparty.utils.wechat.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.common.config.Constants
import com.tencent.mm.opensdk.openapi.IWXAPI

/**
 * 微信动态注册广播
 */
class WXReceiver(private val api: IWXAPI?) : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        //将该app注册到微信
        api?.registerApp(Constants.WX_APP_ID)
    }
}