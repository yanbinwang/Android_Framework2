package com.example.base.utils

import java.math.BigDecimal
import kotlin.math.max

/**
 * description 用到BigDecimal计算请务必加上这个interface
 *             这里比较暴力的解决了除法精度问题
 * creator Hyatt
 */
interface BigDecimalHelper {
    infix operator fun BigDecimal.div(other: BigDecimal): BigDecimal {
        return this.divide(other, max((this.scale() + other.scale()), 8), BigDecimal.ROUND_UP)
    }
}