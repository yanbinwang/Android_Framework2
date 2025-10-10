package com.example.thirdparty.pay.utils

import androidx.fragment.app.FragmentActivity
import com.example.common.base.bridge.BaseView
import com.example.framework.utils.function.doOnDestroy
import com.example.thirdparty.pay.bean.PayBean
import com.example.thirdparty.pay.utils.alipay.AlipayPay
import com.example.thirdparty.pay.utils.wechat.WXPay
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

/**
 * 支付类
 */
class PayBuilder(private val mActivity: FragmentActivity, private val mView: BaseView? = null) : CoroutineScope {
    private val alipay by lazy { AlipayPay(mActivity) }
    private val wechat by lazy { WXPay(mActivity) }
    private var payJob: Job? = null
    private val job = SupervisorJob()
    override val coroutineContext: CoroutineContext get() = Main.immediate + job

    init {
        mActivity.doOnDestroy {
            payJob?.cancel()
            job.cancel()
        }
    }

    /**
     * 创建一笔用于拉起第三方应用的订单
     * 1支付宝 2微信
     */
    fun create(orderNo: String, type: String = "1") {
        payJob?.cancel()
        payJob = launch {
//            pay()
        }
    }

    /**
     * 发起支付
     */
    private fun pay(bean: PayBean, type: String = "1") {
        when (type) {
            "1" -> alipay?.pay(bean.sign)
            else -> wechat?.pay(bean.wxPayReq)
        }
    }

}