package com.example.common.base.page

import java.util.*

/**
 * author:wyb
 */
class PageParams {
    private var map: MutableMap<String, Any> = HashMap()

    fun append(key: String, value: Any?): PageParams {
        if (value != null) {
            map[key] = value
        }
        return this
    }

    fun setMap(map: MutableMap<String, Any>): PageParams {
        this.map = map
        return this
    }

    fun getParams():MutableMap<String, Any>{
        return map
    }

}
