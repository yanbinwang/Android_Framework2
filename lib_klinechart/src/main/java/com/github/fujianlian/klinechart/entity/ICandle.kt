package com.github.fujianlian.klinechart.entity

/**
 * 蜡烛图实体接口
 * Created by tifezh on 2016/6/9.
 */
interface ICandle {
    /**
     * 开盘价
     */
    val openPrice: Float

    /**
     * 最高价
     */
    val highPrice: Float

    /**
     * 最低价
     */
    val lowPrice: Float

    /**
     * 收盘价
     */
    val closePrice: Float

    // 以下为MA数据
    /**
     * 五(月，日，时，分，5分等)均价
     */
    val mA5Price: Float

    /**
     * 十(月，日，时，分，5分等)均价
     */
    val mA10Price: Float

    /**
     * 二十(月，日，时，分，5分等)均价
     */
    val mA20Price: Float

    /**
     * 三十(月，日，时，分，5分等)均价
     */
    val mA30Price: Float

    /**
     * 六十(月，日，时，分，5分等)均价
     */
    val mA60Price: Float

    // 以下为BOLL数据
    /**
     * 上轨线
     */
    val up: Float

    /**
     * 中轨线
     */
    val mb: Float

    /**
     * 下轨线
     */
    val dn: Float
}