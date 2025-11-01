package com.example.klinechart.entity

/**
 * 蜡烛图实体接口
 */
interface ICandle {

    /**
     * 开盘价
     */
    fun getOpenPrice(): Float

    /**
     * 最高价
     */
    fun getHighPrice(): Float

    /**
     * 最低价
     */
    fun getLowPrice(): Float

    /**
     * 收盘价
     */
    fun getClosePrice(): Float

    // 以下为MA数据
    /**
     * 五(月，日，时，分，5分等)均价
     */
    fun getMA5Price(): Float

    /**
     * 十(月，日，时，分，5分等)均价
     */
    fun getMA10Price(): Float

    /**
     * 二十(月，日，时，分，5分等)均价
     */
    fun getMA20Price(): Float

    /**
     * 三十(月，日，时，分，5分等)均价
     */
    fun getMA30Price(): Float

    /**
     * 六十(月，日，时，分，5分等)均价
     */
    fun getMA60Price(): Float

    // 以下为BOLL数据
    /**
     * 上轨线
     */
    fun getUp(): Float

    /**
     * 中轨线
     */
    fun getMb(): Float

    /**
     * 下轨线
     */
    fun getDn(): Float

}