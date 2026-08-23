package com.example.klinechart.bean

/**
 * 蜡烛图实体接口
 */
interface ICandle {

    /**
     * 开盘价
     */
    var openPrice: Float

    /**
     * 收盘价
     */
    var closePrice: Float

    /**
     * 最高价
     */
    var highPrice: Float

    /**
     * 最低价
     */
    var lowPrice: Float

    // 以下为 MA 数据
    /**
     * 五(月，日，时，分，5分等)均价
     */
    var ma5Price: Float

    /**
     * 十(月，日，时，分，5分等)均价
     */
    var ma10Price: Float

    /**
     * 二十(月，日，时，分，5分等)均价
     */
    var ma20Price: Float

    /**
     * 三十(月，日，时，分，5分等)均价
     */
    var ma30Price: Float

    /**
     * 六十(月，日，时，分，5分等)均价
     */
    var ma60Price: Float

    // 以下为 BOLL 数据
    /**
     * 上轨线
     */
    var up: Float

    /**
     * 中轨线
     */
    var mb: Float

    /**
     * 下轨线
     */
    var dn: Float

}