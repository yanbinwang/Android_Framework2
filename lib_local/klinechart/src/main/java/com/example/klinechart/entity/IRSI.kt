package com.example.klinechart.entity

/**
 * RSI指标接口
 * 相关说明: https://baike.baidu.com/item/RSI%E6%8C%87%E6%A0%87
 */
interface IRSI {

    /**
     * RSI值
     */
    fun getRsi(): Float

}