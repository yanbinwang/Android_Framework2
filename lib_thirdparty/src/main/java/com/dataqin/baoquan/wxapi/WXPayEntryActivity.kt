package com.dataqin.baoquan.wxapi

import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.common.event.EventCode.EVENT_PAY_CANCEL
import com.example.common.event.EventCode.EVENT_PAY_FAILURE
import com.example.common.event.EventCode.EVENT_PAY_SUCCESS
import com.example.common.utils.builder.shortToast
import com.example.thirdparty.R
import com.example.thirdparty.utils.WXManager
import com.tencent.mm.opensdk.modelbase.BaseReq
import com.tencent.mm.opensdk.modelbase.BaseResp
import com.tencent.mm.opensdk.openapi.IWXAPIEventHandler

/**
 *  Created by wangyanbin
 * 微信客户端支付示例
 * 该类只负责掉起微信和支付操作的界面
 * 需要支付的界面需要配置一个去除activity动画样式的style
 * 否则会造成闪屏
 * <activity
 * android:name=".wxapi.WXPayEntryActivity"
 * android:exported="true"
 * android:launchMode="singleInstance"
 * android:screenOrientation="portrait"
 * android:configChanges="orientation|screenSize|keyboardHidden|screenLayout|uiMode"
 * android:theme="@style/TransparentTheme"
 * android:windowSoftInputMode="stateHidden|adjustPan" />
 */
class WXPayEntryActivity : AppCompatActivity(), IWXAPIEventHandler {
    private val wxApi by lazy { WXManager.instance.regToWx(this) }// IWXAPI 是第三方app和微信通信的openapi接口

    override fun onCreate(savedInstanceState: Bundle?) {
        wxApi?.handleIntent(intent, this)
        super.onCreate(savedInstanceState)
        overridePendingTransition(R.anim.set_alpha_in, R.anim.set_alpha_none)
        requestedOrientation = if (Build.VERSION.SDK_INT == Build.VERSION_CODES.O) {
            ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        } else {
            ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(R.anim.set_alpha_none, R.anim.set_alpha_in)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        setIntent(intent)
        wxApi?.handleIntent(intent, this)
    }

    override fun onReq(req: BaseReq?) {
    }

    /**
     * 微信发起后回调
     */
    override fun onResp(resp: BaseResp?) {
        when (resp?.errCode) {
            //支付成功
            BaseResp.ErrCode.ERR_OK -> results(R.string.paySuccess, 0)
            //支付取消
            BaseResp.ErrCode.ERR_USER_CANCEL -> results(R.string.payCancel, 1)
            //支付失败
            else -> results(R.string.payFailure, 2)
        }
        finish()
    }

    /**
     * 统一处理
     */
    private fun results(resId: Int, type: Int = -1) {
        resId.shortToast()
        when (type) {
            0 -> EVENT_PAY_SUCCESS.post()
            1 -> EVENT_PAY_CANCEL.post()
            2 -> EVENT_PAY_FAILURE.post()
        }
    }

}