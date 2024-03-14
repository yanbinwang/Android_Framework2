package com.example.thirdparty.pay.utils.alipay

import android.text.TextUtils
import androidx.fragment.app.FragmentActivity
import com.alipay.sdk.app.PayTask
import com.example.common.event.EventCode.EVENT_PAY_CANCEL
import com.example.common.event.EventCode.EVENT_PAY_FAILURE
import com.example.common.event.EventCode.EVENT_PAY_SUCCESS
import com.example.common.utils.builder.shortToast
import com.example.common.utils.file.isAvailable
import com.example.framework.utils.function.doOnDestroy
import com.example.framework.utils.logWTF
import com.example.thirdparty.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext

/**
 * 支付宝支付
 */
class AlipayBuilder(private val mActivity: FragmentActivity) : CoroutineScope {
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
     * 发起支付时需要传入服务器真理好的payInfo
     */
    fun pay(payInfo: String) {
        //未安装
        if (!mActivity.isAvailable("com.eg.android.AlipayGphone")) {
            results(R.string.alipayUnInstalled, 2)
            return
        }
        R.string.payInitiate.shortToast()
        payJob?.cancel()
        payJob = launch {
            val result = withContext(IO) {
                //构造PayTask 对象
                val payTask = PayTask(mActivity)
                //调用支付接口，获取支付结果
                payTask.pay(payInfo, true)
            }
            "支付结果:\n$result".logWTF
            if (result.isNullOrEmpty()) {
                results(R.string.payFailure, 2)
            } else {
                /**
                 * 同步返回的结果必须放置到服务端进行验证（验证的规则请看https://doc.open.alipay.com/docs/doc.htm?
                 * spm=a219a.7629140.0.0.M0HfOm&treeId=59&articleId=103671&docType=1) 建议商户依赖异步通知
                 */
                val resultStatus = PayResult(result).resultStatus
                //判断resultStatus 为“9000”则代表支付成功，具体状态码代表含义可参考接口文档
                if (TextUtils.equals(resultStatus, "9000")) {
                    results(R.string.paySuccess, 0)
                } else {
                    //用户中途取消
                    if (TextUtils.equals(resultStatus, "6001")) {
                        results(R.string.payCancel, 1)
                        //正在处理中，支付结果未知（有可能已经支付成功），请查询商户订单列表中订单的支付状态
                    } else {
                        results(R.string.payFailure, 2)
                    }
                }
            }
        }
    }

    /**
     * 统一处理
     */
    private fun results(resId: Int, type: Int) {
        resId.shortToast()
        when (type) {
            0 -> EVENT_PAY_SUCCESS.post()
            1 -> EVENT_PAY_CANCEL.post()
            else -> EVENT_PAY_FAILURE.post()
        }
    }

}