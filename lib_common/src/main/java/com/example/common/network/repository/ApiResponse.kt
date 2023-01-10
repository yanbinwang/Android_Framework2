package com.example.common.network.repository

/**
 * author: wyb
 * date: 2019/7/29.
 * 接口外层地址（与服务器约定好对应格式）
 */
data class ApiResponse<T>(
    var code: Int? = null,//状态码
    var msg: String? = null,//信息
    var data: T? = null,//数据
)