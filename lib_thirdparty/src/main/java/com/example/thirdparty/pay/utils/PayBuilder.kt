package com.example.thirdparty.pay.utils

import androidx.fragment.app.FragmentActivity
import com.example.common.base.bridge.BaseView
import com.example.framework.utils.function.doOnDestroy
import com.example.thirdparty.pay.bean.PayBean
import com.example.thirdparty.pay.utils.alipay.AlipayBuilder
import com.example.thirdparty.pay.utils.wechat.WechatBuilder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

/**
 * 支付类
 */
class PayBuilder(private val mActivity: FragmentActivity) : CoroutineScope {
    private val alipay by lazy { AlipayBuilder(mActivity) }
    private val wechat by lazy { WechatBuilder(mActivity) }
    private var payJob: Job? = null
    private val job = SupervisorJob()
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    init {
        mActivity.doOnDestroy {
            payJob?.cancel()
            job.cancel()
        }
    }

    /**
     * 创建一笔用于拉起第三方应用的订单-0支付宝 1微信
     */
    fun create(mView: BaseView? = null) {
        payJob?.cancel()
        payJob = launch {

//            pay()
        }
    }

    /**
     * 发起支付
     */
    private fun pay(bean: PayBean, type: Int = 0) {
        when (type) {
            0 -> alipay.pay(bean.sign.orEmpty())
            1 -> wechat.pay(bean.wxPayReq)
        }
    }

}