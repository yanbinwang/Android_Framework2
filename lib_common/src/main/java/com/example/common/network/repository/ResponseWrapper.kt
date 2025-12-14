package com.example.common.network.repository

/**
 * 交互失败统一返回的对象
 */
data class ResponseWrapper(
    var errCode: Int? = null, // 状态码（和code本质没区别，多了自定义的FAILURE->-1）
    var errMessage: String? = null, // 错误信息
    var throwable: Throwable? = null // 异常信息
) : Throwable()