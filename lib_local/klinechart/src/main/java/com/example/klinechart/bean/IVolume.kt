package com.example.klinechart.bean

/**
 * 成交量接口
 */
interface IVolume {

    /**
     * 开盘价
     */
    var openPrice: Float

    /**
     * 收盘价
     */
    var closePrice: Float

    /**
     * 成交量
     */
    var volume: Float

    /**
     * 五(月，日，时，分，5分等)均量
     */
    var ma5Volume: Float

    /**
     * 十(月，日，时，分，5分等)均量
     */
    var ma10Volume: Float

}