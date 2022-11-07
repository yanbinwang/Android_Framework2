package com.example.common.base.page

/**
 *  Created by wangyanbin
 *  项目中分页统一外层套该类(合并部分接口带有data部分带有list)
 */
data class Page<T>(
    var total: Int? = null,//总记录数
    var hasNextPage: Boolean? = null,//是否有下一页（是否有更多数据）
    var list: MutableList<T>? = null,
)