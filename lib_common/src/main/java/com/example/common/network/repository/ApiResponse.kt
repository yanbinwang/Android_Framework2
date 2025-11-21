package com.example.common.network.repository

/**
 * author: wyb
 * date: 2019/7/29.
 * 接口外层地址（与服务器约定好对应格式）
 */
data class ApiResponse<T>(
    var success: Boolean? = null, // 后台这个参数和errCode都成功才算请求成功
    var errCode: String? = null, // 请求状态码，只做成功判断，失败后会判断statusCode
    var errMessage: String? = null, // 请求信息，不拿取只承接
    var data: T? = null, // 数据
    // 失败接口特有，请求失败时会返回
    var statusCode: Int? = null, // 错误状态码
    var content: String? = null, // 错误信息
    // 分页接口特有，在分页回调的时候会返回对应的值
    var totalCount: Int? = null
)

/**
 * 处理特殊情况，code=200，但是data后端偷懒直接不给值或者给空值
 * 比如发送验证码接口，提交接口等，此时我们给一个指定的对象，保证有值返回
 */
class EmptyBean