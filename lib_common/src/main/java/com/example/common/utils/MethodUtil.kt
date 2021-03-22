package com.example.common.utils

import android.annotation.SuppressLint
import android.text.InputFilter
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.widget.EditText
import android.widget.ImageView
import com.example.base.utils.DecimalInputFilter

/**
 *  Created by wangyanbin
 *  按钮，控件行为工具类
 */
@SuppressLint("SetTextI18n")
object MethodUtil {

    /**
     * EditText输入密码是否可见(显隐)
     */
    fun inputTransformation(isDisplay: Boolean, editText: EditText, imageView: ImageView): Boolean {
        if (!isDisplay) {
            //display password text, for example "123456"
            editText.transformationMethod = HideReturnsTransformationMethod.getInstance()
            try {
                editText.setSelection(editText.text.length)
//            imageView.setBackgroundResource(R.mipmap.ic_text_show)
            } catch (e: Exception) {
            }
        } else {
            //hide password, display "."
            editText.transformationMethod = PasswordTransformationMethod.getInstance()
            try {
                editText.setSelection(editText.text.length)
//            imageView.setBackgroundResource(R.mipmap.ic_text_hide)
            } catch (e: Exception) {
            }
        }
        editText.postInvalidate()
        return !isDisplay
    }

    /**
     * EditText输入金额小数限制
     */
    fun decimalFilter(editText: EditText, decimalPoint: Int = 2){
        val decimalInputFilter = DecimalInputFilter()
        decimalInputFilter.decimalPoint = decimalPoint
        val filters = arrayOf<InputFilter>(decimalInputFilter)
        editText.filters = filters
    }

}