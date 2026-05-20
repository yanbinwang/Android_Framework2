package com.example.thirdparty.pay.utils.alipay

/**
 * 阿里支付绘制
 */
class AlipayPayResult(rawResult: String?) {
    var resultStatus: String? = null
    var result: String? = null
    var memo: String? = null

    init {
        if (!rawResult.isNullOrEmpty()) {
            val resultParams = rawResult.split(";".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            for (resultParam in resultParams) {
                if (resultParam.startsWith("resultStatus")) {
                    resultStatus = gatValue(resultParam, "resultStatus")
                }
                if (resultParam.startsWith("result")) {
                    result = gatValue(resultParam, "result")
                }
                if (resultParam.startsWith("memo")) {
                    memo = gatValue(resultParam, "memo")
                }
            }
        }
    }

    private fun gatValue(content: String, key: String): String {
        val prefix = "$key={"
        return content.substring(content.indexOf(prefix) + prefix.length, content.lastIndexOf("}"))
    }

    override fun toString(): String {
        return "resultStatus={ $resultStatus };memo={ $memo };result={ $result }"
    }

}