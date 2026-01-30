package com.dataqin.baoquan.wxapi

import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.thirdparty.R
import com.example.thirdparty.utils.wechat.WXManager
import com.tencent.mm.opensdk.constants.ConstantsAPI
import com.tencent.mm.opensdk.modelbase.BaseReq
import com.tencent.mm.opensdk.modelbase.BaseResp
import com.tencent.mm.opensdk.openapi.IWXAPIEventHandler

/**
 *  Created by wangyanbin
 * 微信开放平台的通用回调
 * 1) 微信登录、微信分享、小程序拉起等场景的结果回调（支付之外的开放能力）
 * 2) 需要配置一个去除activity动画样式的style否则会造成闪屏
 * <activity
 * android:name=".wxapi.WXEntryActivity"
 * android:exported="true"
 * android:launchMode="singleTask"
 * android:screenOrientation="portrait"
 * android:configChanges="orientation|screenSize|keyboardHidden|screenLayout|uiMode"
 * android:theme="@style/TransparentTheme"
 * android:windowSoftInputMode="stateHidden|adjustPan" />
 */
class WXEntryActivity : AppCompatActivity(), IWXAPIEventHandler {
    // IWXAPI 是第三方app和微信通信的openapi接口
    private val wxApi by lazy { WXManager.instance.regToWx(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 禁用过渡动画
        overridePendingTransition(R.anim.set_alpha_none, R.anim.set_alpha_none)
        // 强制竖屏（统一适配，避免横屏回调异常）
        requestedOrientation = if (Build.VERSION.SDK_INT == Build.VERSION_CODES.O) {
            ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        } else {
            ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }
        // 处理微信回调Intent
        wxApi?.handleIntent(intent, this)
//        finish()
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        // 处理新的Intent（避免多任务场景下回调丢失）
        setIntent(intent)
        wxApi?.handleIntent(intent, this)
    }

    override fun finish() {
        super.finish()
        // 关闭时也禁用动画，避免闪屏
        overridePendingTransition(R.anim.set_alpha_none, R.anim.set_alpha_none)
    }

    override fun onReq(req: BaseReq?) {
    }

    /**
     * 接收微信回调结果 (resp.type区分类型,通过微信静态类ConstantsAPI观测不同结果)
     * @param resp 回调数据，包含分享/登录/支付的结果
     */
    override fun onResp(resp: BaseResp?) {
//        resp?.let {
//            // 处理业务逻辑：比如分享成功/失败、登录授权结果等
//            when (it.errCode) {
//                BaseResp.ErrCode.ERR_OK -> {
//                    // 操作成功（如分享成功、登录授权成功）
//                }
//                BaseResp.ErrCode.ERR_USER_CANCEL -> {
//                    // 用户取消操作
//                }
//                else -> {
//                    // 操作失败，it.errStr 是失败原因
//                }
//            }
//        }
//        // 处理完回调结果后，再关闭Activity
//        finish()
    }

}