package com.example.thirdparty.pay.bean

import com.example.framework.utils.function.value.add

data class BalanceBean(
    var balance: String? = null,//总金额
    var sendBalance: String? = null,//实际支付金额
    var filePriceNum: Int? = null,//文件和hash的套餐数量
    var webPriceNum: Int? = null//网页取证的套餐数量
) {
    /**
     * 获取实际余额
     */
    val actualBalance get() = balance.add(sendBalance.orEmpty())
}