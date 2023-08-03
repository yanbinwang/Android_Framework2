package com.github.fujianlian.klinechart.entity

/**
 * 成交量接口
 * Created by hjm on 2017/11/14 17:46.
 */
interface IVolume {
    /**
     * 开盘价
     */
    fun getOpenPrice(): Float

    /**
     * 收盘价
     */
    fun getClosePrice(): Float

    /**
     * 成交量
     */
    fun getVolume(): Float

    /**
     * 五(月，日，时，分，5分等)均量
     */
    fun getMA5Volume(): Float

    /**
     * 十(月，日，时，分，5分等)均量
     */
    fun getMA10Volume(): Float
}