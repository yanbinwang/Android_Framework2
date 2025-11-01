package com.example.klinechart.entity

/**
 * KDJ指标(随机指标)接口
 */
interface IKLine : ICandle, IMACD, IKDJ, IRSI, IVolume, IWR {

    fun getDate(): String

}