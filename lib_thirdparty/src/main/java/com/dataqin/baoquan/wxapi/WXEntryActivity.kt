package com.dataqin.baoquan.wxapi

import android.content.pm.ActivityInfo
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.common.config.Constants
import com.example.thirdparty.R
import com.tencent.mm.opensdk.modelbase.BaseReq
import com.tencent.mm.opensdk.modelbase.BaseResp
import com.tencent.mm.opensdk.openapi.IWXAPIEventHandler
import com.tencent.mm.opensdk.openapi.WXAPIFactory

/**
 *  Created by wangyanbin
 * 微信客户端分享示例
 * 该类只负责掉起微信和支付操作的界面
 * 需要支付的界面需要配置一个去除activity动画样式的style
 * 否则会造成闪屏
 * <activity
 * android:name=".wxapi.WXEntryActivity"
 * android:exported="true"
 * android:screenOrientation="portrait"
 * android:configChanges="orientation|screenSize|keyboardHidden|screenLayout|uiMode"
 * android:theme="@style/TransparentTheme"
 * android:windowSoftInputMode="stateHidden|adjustPan" />
 */
class WXEntryActivity : AppCompatActivity(), IWXAPIEventHandler {
    private val wxApi by lazy { WXAPIFactory.createWXAPI(this, Constants.WX_APP_ID) }// IWXAPI 是第三方app和微信通信的openapi接口

    override fun onCreate(savedInstanceState: Bundle?) {
        wxApi?.handleIntent(intent, this)
        super.onCreate(savedInstanceState)
        overridePendingTransition(R.anim.set_alpha_in, R.anim.set_alpha_none)
        requestedOrientation = if (Build.VERSION.SDK_INT == Build.VERSION_CODES.O) {
            ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        } else {
            ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }
        finish()
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(R.anim.set_alpha_none, R.anim.set_alpha_in)
    }

    override fun onReq(req: BaseReq?) {
    }

    /**
     * 微信发起后回调
     * getType为1，分享为0
     */
    override fun onResp(resp: BaseResp?) {
    }

    override fun onDestroy() {
        super.onDestroy()
        wxApi.unregisterApp()
    }

}