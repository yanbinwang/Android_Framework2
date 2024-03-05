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
) {
    /**
     * 判断此次请求是否成功
     */
    val results get() = successful()
}

/**
 * 处理特殊情况，code=200，但是data后端偷懒直接不给值或者给空值
 * 比如发送验证码接口，提交接口等，此时我们给一个指定的对象，保证有值返回
 */
class EmptyBean