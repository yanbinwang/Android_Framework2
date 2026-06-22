package com.example.thirdparty.pay.utils.wechat

import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LifecycleOwner
import com.example.common.event.EventCode.EVENT_PAY_FAILURE
import com.example.common.utils.builder.shortToast
import com.example.framework.utils.function.value.orFalse
import com.example.framework.utils.logWTF
import com.example.thirdparty.R
import com.example.thirdparty.utils.wechat.WXManager
import com.tencent.mm.opensdk.modelpay.PayReq

class WXPay(mActivity: FragmentActivity) {
    // 通过WXAPIFactory工厂，获取IWXAPI的实例
    private val wxApi by lazy { WXManager.instance.regToWx(mActivity) }

    /**
     * 发起支付时都将app注册一下，页面关闭时再注销
     */
    fun launchPay(req: PayReq?) {
//        // 将应用的appId注册到微信
//        wxApi?.registerApp(Constants.WX_APP_ID)
        // 未安装
        if (!wxApi?.isWXAppInstalled.orFalse) {
            handlePayResult(R.string.wechatUnInstalled)
            return
        }
        // 版本不支持
        val isWxSupport = try {
            wxApi?.wxAppSupportAPI != 0
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
        if (!isWxSupport) {
            handlePayResult(R.string.wechatSupportError)
            return
        }
        // 发起支付
        handlePayResult(R.string.payInitiate, false)
        val payResult = wxApi?.sendReq(req)
        "支付状态:${payResult}".logWTF
        if (!payResult.orFalse) {
            handlePayResult(R.string.payFailure)
        }
    }

    /**
     * 统一处理
     */
    private fun handlePayResult(resId: Int, isFailure: Boolean = true) {
        resId.shortToast()
        if (isFailure) EVENT_PAY_FAILURE.post()
    }

}