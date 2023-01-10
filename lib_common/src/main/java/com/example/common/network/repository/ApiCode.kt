package com.example.common.network.repository

/**
 * description 异常编码定义
 * creator yan
 */
object ApiCode {

    /**
     * 成功
     */
    const val SUCCESS = 200

    /**
     * 失败
     * 用于异常捕获的失败，项目请求的失败编码由服务器返回
     */
    const val FAILURE = -1

    /**
     * token过期/被顶号
     */
    const val TOKEN_EXPIRED = 408

}