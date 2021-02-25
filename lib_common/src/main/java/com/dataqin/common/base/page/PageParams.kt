package com.dataqin.common.base.page

import java.util.*

/**
 * author:wyb
 * 页面参数类，跳转的参数，刷新页面页数操作
 */
class PageParams {
    var map: MutableMap<String, Any> = HashMap()

    fun append(key: String, value: Any?): PageParams {
        if (value != null) {
            map[key] = value
        }
        return this
    }

}
