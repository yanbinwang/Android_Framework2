package com.example.common.http.callback

/**
 * author: wyb
 * date: 2019/7/29.
 * 接口外层地址（与服务器约定好对应格式）
 */
class ApiResponse<T> {
    var e: Int = 0//状态码
    var msg: String? = null//信息
    var data: T? = null//数据
}
