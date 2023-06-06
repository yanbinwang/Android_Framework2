package com.example.mvvm.utils

import com.example.framework.utils.function.value.removeEndZero
import com.example.framework.utils.function.value.toSafeBigDecimal
import com.example.framework.utils.function.value.toSafeDouble

/**
 * @description 计算帮助类->一般来说不会用到，涉及到带有小数点的统一都用string接取，转换为BigDecimal计算最为精确
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
    operator fun Double?.plus(other: Double?): Double {
        return toSafeBigDecimal().add(other.toSafeBigDecimal()).toPlainString().removeEndZero().toSafeDouble()
    }

    /**
     * 减法
     */
    operator fun Double?.minus(other: Double?): Double {
        return toSafeBigDecimal().subtract(other.toSafeBigDecimal()).toPlainString().removeEndZero().toSafeDouble()
    }

    /**
     * 乘法
     */
    operator fun Double?.times(other: Double?): Double {
        return toSafeBigDecimal().multiply(other.toSafeBigDecimal()).toPlainString().removeEndZero().toSafeDouble()
    }

    /**
     * 除法->有误
     */
    operator fun Double?.div(other: Double?): Double {
        return toSafeBigDecimal().divide(other.toSafeBigDecimal(), getScale(), getMode()).toPlainString().removeEndZero().toSafeDouble()
    }

    fun getScale(): Int

    fun getMode(): Int

}