package com.example.thirdparty.pay.utils.wechat

import androidx.fragment.app.FragmentActivity
import com.example.common.config.Constants
import com.example.common.event.EventCode.EVENT_PAY_FAILURE
import com.example.common.utils.builder.shortToast
import com.example.framework.utils.function.doOnDestroy
import com.example.framework.utils.logWTF
import com.example.thirdparty.R
import com.tencent.mm.opensdk.modelpay.PayReq
import com.tencent.mm.opensdk.openapi.WXAPIFactory

class WechatBuilder(mActivity: FragmentActivity) {
    //通过WXAPIFactory工厂，获取IWXAPI的实例
    private val wxApi by lazy { WXAPIFactory.createWXAPI(mActivity, Constants.WX_APP_ID, true) }

    init {
        mActivity.doOnDestroy {
            wxApi.unregisterApp()
        }
    }

    /**
     * 发起支付时都将app注册一下，页面关闭时再注销
     */
    fun pay(req: PayReq?) {
        //将应用的appId注册到微信
        wxApi.registerApp(Constants.WX_APP_ID)
        //未安装
        if (!wxApi.isWXAppInstalled) {
            results(R.string.wechatUnInstalled)
            return
        }
        //版本不支持
        if (!(try {
                wxApi?.wxAppSupportAPI != 0
            } catch (e: Exception) {
                false
            })) {
            results(R.string.wechatSupportError)
            return
        }
        //发起支付
        results(R.string.payInitiate, false)
        val result = wxApi.sendReq(req)
        "支付状态:${result}".logWTF
        if (!result) results(R.string.payCancel)
    }

    /**
     * 统一处理
     */
    private fun results(resId: Int, isPost: Boolean = true) {
        resId.shortToast()
        if (isPost) EVENT_PAY_FAILURE.post()
    }

}