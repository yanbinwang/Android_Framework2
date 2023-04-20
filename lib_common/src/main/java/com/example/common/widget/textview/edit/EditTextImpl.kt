package com.example.common.widget.textview.edit

import android.widget.EditText
import androidx.annotation.StringRes
import com.example.common.utils.builder.shortToast
import com.example.framework.utils.function.value.matchEmail
import com.example.framework.utils.function.value.matchPassword
import java.util.regex.Pattern

/**
 * kt中的接口是可以实现的，实现后的方法只有继承的类才能使用
 */
interface EditTextImpl {

    /**
     * 检测内容文本是否在输入范围内
     */
    fun EditText.lengthLimit(min: Int, max: Int, @StringRes res: Int = -1): Boolean {
        return if (text.length in min..max) {
            true
        } else {
            if (-1 != res) res.shortToast()
            false
        }
    }

    fun ClearEditText.lengthLimit(min: Int, max: Int, @StringRes res: Int = -1): Boolean {
        return editText.lengthLimit(min, max, res)
    }

    fun PasswordEditText.lengthLimit(min: Int, max: Int, @StringRes res: Int = -1): Boolean {
        return editText.lengthLimit(min, max, res)
    }

    /**
     * 检测内容文本是否为空
     */
    fun EditText.notEmpty(@StringRes res: Int = -1): Boolean {
        return if (!text.isNullOrEmpty()) {
            true
        } else {
            if (-1 != res) res.shortToast()
            false
        }
    }

    fun ClearEditText.notEmpty(@StringRes res: Int = -1): Boolean {
        return editText.notEmpty(res)
    }

    fun PasswordEditText.notEmpty(@StringRes res: Int = -1): Boolean {
        return editText.notEmpty(res)
    }

    /**
     * 检测内容文本是否符合邮箱要求
     */
    fun EditText.checkEmailReg(hasToast: Boolean = true): Boolean {
        if (!notEmpty()) {
            if (hasToast) "邮箱不能为空".shortToast()
            return false
        }
        if (text.toString().matchEmail()) return true
        if (hasToast) "邮箱格式错误".shortToast()
        return false
    }

    fun ClearEditText.checkEmailReg(hasToast: Boolean = true): Boolean {
        return editText.checkEmailReg(hasToast)
    }

    fun PasswordEditText.checkEmailReg(hasToast: Boolean = true): Boolean {
        return editText.checkEmailReg(hasToast)
    }

    /**
     * 检测内容文本是否符合密码要求
     */
    fun EditText.checkPassReg(hasToast: Boolean = true): Boolean {
        if (!notEmpty()) {
            if (hasToast) "密码不能为空".shortToast()
            return false
        }
        if (!text.toString().matchPassword()) {
            if (hasToast) "密码由6~20位的字母和數字組成".shortToast()
            return false
        }
        return true
    }

    fun ClearEditText.checkPassReg(hasToast: Boolean = true): Boolean {
        return editText.checkPassReg(hasToast)
    }

    fun PasswordEditText.checkPassReg(hasToast: Boolean = true): Boolean {
        return editText.checkPassReg(hasToast)
    }

}

/**
 * 返回密码强度
 */
fun String?.passwordLevel(): Int {
    if (this.isNullOrEmpty()) return 0
    //纯数字、纯字母、纯特殊字符
    if (this.length < 8 || Pattern.matches("^\\d+$", this) || Pattern.matches("^[a-z]+$", this) || Pattern.matches("^[A-Z]+$", this) || Pattern.matches("^[@#$%^&]+$", this)) return 1
    //字母+数字、字母+特殊字符、数字+特殊字符
    if (Pattern.matches("^(?!\\d+$)(?![a-z]+$)[a-z\\d]+$", this) || Pattern.matches("^(?!\\d+$)(?![A-Z]+$)[A-Z\\d]+$", this) || Pattern.matches("^(?![a-z]+$)(?![@#$%^&]+$)[a-z@#$%^&]+$", this) || Pattern.matches("^(?![A-Z]+$)(?![@#$%^&]+$)[A-Z@#$%^&]+$", this) || Pattern.matches("^(?![a-z]+$)(?![A-Z]+$)[a-zA-Z]+$", this) || Pattern.matches("^(?!\\d+)(?![@#$%^&]+$)[\\d@#$%^&]+$", this)) return 2
    //字母+数字+特殊字符
    if (Pattern.matches("^(?!\\d+$)(?![a-z]+$)(?![A-Z]+$)(?![@#$%^&]+$)[\\da-zA-Z@#$%^&]+$", this)) return 3
    return 3
}