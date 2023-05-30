package com.example.mvvm.utils

import com.example.framework.utils.function.value.add
import com.example.framework.utils.function.value.divide
import com.example.framework.utils.function.value.multiply
import com.example.framework.utils.function.value.subtract
import com.example.framework.utils.function.value.toSafeBigDecimal

/**
 * @description 计算帮助类
 * @author yan
 */
interface CalculateImpl {

//    /**
//     * 需要用到的页面继承当前接口
//     * val a :Double?=0.0
//     * val b :Double?=null
//     * a  +  b
//     */
//    operator fun Double?.plus(other: Double?): Double {
//        return (this ?: 0.0) + (other ?: 0.0)
//    }

    /**
     * 加法
     */
    operator fun String?.plus(other: String?): String {
        return this.add(other.toSafeBigDecimal().toPlainString())
    }

    /**
     * 减法
     */
    operator fun String?.minus(other: String?): String {
        return this.subtract(other.toSafeBigDecimal().toPlainString())
    }

    /**
     * 乘法
     */
    operator fun String?.times(other: String?): String {
        return this.multiply(other.toSafeBigDecimal().toPlainString())
    }

    /**
     * 除法->有误
     */
    operator fun String?.div(other: String?): String {
        return this.divide(other.toSafeBigDecimal().toPlainString())
    }

}