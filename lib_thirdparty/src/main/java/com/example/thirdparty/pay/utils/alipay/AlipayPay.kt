package com.example.thirdparty.pay.utils.alipay

import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import com.alipay.sdk.app.PayTask
import com.example.common.event.EventCode.EVENT_PAY_CANCEL
import com.example.common.event.EventCode.EVENT_PAY_FAILURE
import com.example.common.event.EventCode.EVENT_PAY_SUCCESS
import com.example.common.utils.builder.shortToast
import com.example.framework.utils.function.doOnDestroy
import com.example.framework.utils.function.isAvailable
import com.example.framework.utils.logWTF
import com.example.thirdparty.R
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * 支付宝支付
 */
class AlipayPay(private val mActivity: FragmentActivity)  {
    private var payJob: Job? = null

    init {
        mActivity.doOnDestroy {
            payJob?.cancel()
        }
    }

    /**
     * 发起支付时需要传入服务器真理好的payInfo
     */
    fun launchPay(payInfo: String?) {
        // 未安装
        if (!mActivity.isAvailable("com.eg.android.AlipayGphone")) {
            handlePayResult(R.string.alipayUnInstalled, 2)
            return
        }
        handlePayResult(R.string.payInitiate)
        payJob?.cancel()
        payJob = mActivity.lifecycleScope.launch {
            val payResult = try {
                withContext(IO) {
                    // 双重校验：Activity 未销毁 + payInfo 合法
                    if (mActivity.isFinishing || mActivity.isDestroyed || payInfo.isNullOrEmpty()) {
                        // 提前返回null，避免创建PayTask
                        null
                    } else {
                        // 构造PayTask 对象
                        val payTask = PayTask(mActivity)
                        // 调用支付接口，获取支付结果
                        payTask.pay(payInfo, true)
                    }
                }
            } catch (e: Exception) {
                // 捕获所有可能的异常（Activity销毁/SDK内部异常等）
                e.printStackTrace()
                "支付宝支付调用异常: ${e.message}".logWTF
                null
            }
            "支付结果:\n$payResult".logWTF
            if (payResult.isNullOrEmpty()) {
                handlePayResult(R.string.payFailure, 2)
                return@launch
            }
            /**
             * 同步返回的结果必须放置到服务端进行验证（验证的规则请看https://doc.open.alipay.com/docs/doc.htm?
             * spm=a219a.7629140.0.0.M0HfOm&treeId=59&articleId=103671&docType=1) 建议商户依赖异步通知
             */
            val resultStatus = AlipayPayResult(payResult).resultStatus
            // 判断resultStatus 为“9000”则代表支付成功，具体状态码代表含义可参考接口文档
            when(resultStatus) {
                // 支付成功
                "9000" -> handlePayResult(R.string.paySuccess, 0)
                // 用户取消支付
                "6001" -> handlePayResult(R.string.payCancel, 1)
                // 其他状态（4000/5000/6002等）：支付失败 / 正在处理中，支付结果未知（有可能已经支付成功），请查询商户订单列表中订单的支付状态
                else -> handlePayResult(R.string.payFailure, 2)
            }
        }
    }

    /**
     * 统一处理
     */
    private fun handlePayResult(resId: Int, type: Int = -1) {
        resId.shortToast()
        when (type) {
            0 -> EVENT_PAY_SUCCESS.post()
            1 -> EVENT_PAY_CANCEL.post()
            2 -> EVENT_PAY_FAILURE.post()
        }
    }

}