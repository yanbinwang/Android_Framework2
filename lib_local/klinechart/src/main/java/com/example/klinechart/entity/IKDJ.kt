package com.example.klinechart.entity

/**
 * KDJ指标(随机指标)接口
 * 相关说明:https://baike.baidu.com/item/KDJ%E6%8C%87%E6%A0%87/6328421?fr=aladdin&fromid=3423560&fromtitle=kdj
 */
interface IKDJ {

    /**
     * K值
     */
    fun getK(): Float

    /**
     * D值
     */
    fun getD(): Float

    /**
     * J值
     */
    fun getJ(): Float

}