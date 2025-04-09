package com.example.common.base.binding.adapter

/**
 * 适配器枚举类型
 */
sealed class BaseItemType {
    data object BEAN : BaseItemType()
    data object LIST : BaseItemType()
}