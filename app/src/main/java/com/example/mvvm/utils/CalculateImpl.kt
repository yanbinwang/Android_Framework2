package com.example.mvvm.utils

import com.example.framework.utils.function.value.add
import com.example.framework.utils.function.value.divide
import com.example.framework.utils.function.value.multiply
import com.example.framework.utils.function.value.subtract
import com.example.framework.utils.function.value.toSafeDouble
import java.math.RoundingMode

/**
 * @description 计算帮助类 -> 一般来说不会用到，涉及到带有小数点的统一都用string接取，转换为BigDecimal计算最为精确
 * @author yan
 */
interface CalculateImpl {

    /**
     * 加法
     */
    operator fun Number?.plus(other: Number?): Double {
        return add(other).toSafeDouble()
    }

    /**
     * 减法
     */
    operator fun Number?.minus(other: Number?): Double {
        return subtract(other).toSafeDouble()
    }

    /**
     * 乘法
     */
    operator fun Number?.times(other: Number?): Double {
        return multiply(other).toSafeDouble()
    }

    /**
     * 除法
     */
    operator fun Number?.div(other: Number?): Double {
        return divide(other, getScale(), getRoundingMode()).toSafeDouble()
    }

    /**
     * 保留小数位
     */
    fun getScale(): Int

    /**
     * 取余方式
     */
    fun getRoundingMode(): RoundingMode

}