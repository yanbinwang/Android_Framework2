package com.example.base.utils

import android.text.InputFilter
import android.text.Spanned
import java.util.regex.Pattern

/**
 *  Created by wangyanbin
 *  edittext的输入过滤器
 *
 *  代码实例：
 *   val filters = arrayOf<InputFilter>(DecimalInputFilter())
 *   it.filters = filters
 *
 *   <EditText
 *      android:id="@+id/et_integral"
 *      android:layout_width="0dp"
 *      android:layout_height="match_parent"
 *      android:layout_weight="1"
 *      android:background="@null"
 *      android:ellipsize="end"
 *      android:hint="请输入积分充值数额"
 *      android:inputType="numberDecimal"
 *      android:lines="1"
 *      android:textColor="@color/black_111b34"
 *      android:textColorHint="@color/grey_c5cad5"
 *      android:textSize="30mm"
 *      android:textStyle="bold" />
 */
class DecimalInputFilter : InputFilter {
    private val mPattern by lazy { Pattern.compile("([0-9]|\\.)*") }
    private val maxValue by lazy { Int.MAX_VALUE }//输入的最大金额
    private val point = "."
    private val zero = "0"
    var decimalPoint = 2 //小数点后的位数

    /**
     * @param source    新输入的字符串
     * @param start     新输入的字符串起始下标，一般为0
     * @param end       新输入的字符串终点下标，一般为source长度-1
     * @param dest      输入之前文本框内容
     * @param dstart    原内容起始坐标，一般为0
     * @param dend      原内容终点坐标，一般为dest长度-1
     * @return          输入内容
     */
    override fun filter(source: CharSequence?, start: Int, end: Int, dest: Spanned?, dstart: Int, dend: Int): CharSequence? {
        val sourceText = source.toString()
        val destText = dest.toString()
        //验证删除等按键
        if (sourceText.isEmpty()) return ""
        val matcher = mPattern.matcher(source)
        //已经输入小数点的情况下，只能输入数字
        if (destText.contains(point)) {
            if (!matcher.matches()) {
                return ""
                //只能输入一个小数点
            } else if (point == source.toString()) {
                return ""
            }
            //验证小数点精度，保证小数点后只能输入两位
            val index = destText.indexOf(point)
            val length = dend - index
            if (length > decimalPoint) return dest?.subSequence(dstart, dend)
        } else {
            /**
             * 没有输入小数点的情况下，只能输入小数点和数字
             * 1. 首位不能输入小数点
             * 2. 如果首位输入0，则接下来只能输入小数点了
             */
            if (!matcher.matches()) {
                return ""
            } else {
                //首位不能输入小数点
                if (point == source.toString() && destText.isEmpty()) {
                    return ""
                    //如果首位输入0，接下来只能输入小数点
                } else if (point != source.toString() && zero == destText) {
                    return ""
                }
            }
        }
        //验证输入金额的大小
        if ((destText + sourceText).toDouble() > maxValue) return dest?.subSequence(dstart, dend)
        return dest?.subSequence(dstart, dend).toString() + sourceText
    }

}