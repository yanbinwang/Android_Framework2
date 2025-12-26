package com.example.thirdparty.pay.utils

import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import com.example.common.base.bridge.BaseView
import com.example.framework.utils.function.doOnDestroy
import com.example.thirdparty.pay.bean.PayBean
import com.example.thirdparty.pay.utils.alipay.AlipayPay
import com.example.thirdparty.pay.utils.wechat.WXPay
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

/**
 * 支付类
 */
class PayBuilder(private val mActivity: FragmentActivity, private val mView: BaseView? = null)  {
    private val alipay by lazy { AlipayPay(mActivity) }
    private val wechat by lazy { WXPay(mActivity) }
    private var payJob: Job? = null

    init {
        mActivity.doOnDestroy {
            payJob?.cancel()
        }
    }

    /**
     * 创建一笔用于拉起第三方应用的订单
     * 1支付宝 2微信
     */
    fun createPay(orderNo: String, type: String = "1") {
        payJob?.cancel()
        payJob = mActivity.lifecycleScope.launch {
//            pay()
        }
    }

    /**
     * 发起支付
     */
    private fun launchPay(bean: PayBean, type: String = "1") {
        when (type) {
            "1" -> alipay.launchPay(bean.sign)
            else -> wechat.launchPay(bean.wxPayReq)
        }
    }

}