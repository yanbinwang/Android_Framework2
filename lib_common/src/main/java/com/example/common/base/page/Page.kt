package com.example.common.base.page

/**
 *  Created by wangyanbin
 *  项目中分页统一外层套该类(合并部分接口带有data部分带有list)
 */
data class Page<T>(
    var code: Int? = null,
    var count: Int? = null,
    var data: MutableList<T>? = null,
    var list: MutableList<T>? = null,
    var obs: String? = null//图片域名
)