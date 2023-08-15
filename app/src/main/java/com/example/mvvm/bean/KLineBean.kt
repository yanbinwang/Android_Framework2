package com.example.mvvm.bean

import com.example.framework.utils.function.value.toSafeFloat
import com.github.fujianlian.klinechart.KLineEntity

/**
 * 服务器给定的数据对象，做一个转换
 */
data class KLineBean(
    var Date: String? = null,
    var Open: String? = null,
    var High: String? = null,
    var Low: String? = null,
    var Close: String? = null,
    var Volume: String? = null
) {

    fun getEntity(): KLineEntity {
        return KLineEntity().also {
            it.Date = Date
            it.openPrice = Open.toSafeFloat()
            it.closePrice = Close.toSafeFloat()
            it.highPrice = High.toSafeFloat()
            it.lowPrice = Low.toSafeFloat()
            it.volume = Volume.toSafeFloat()
        }
    }

}