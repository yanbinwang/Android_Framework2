package com.example.common.widget.textview.edittext.callback

import android.widget.EditText
import androidx.annotation.StringRes
import com.example.common.R
import com.example.common.utils.builder.shortToast
import com.example.common.widget.textview.edittext.ClearEditText
import com.example.common.widget.textview.edittext.PasswordEditText
import com.example.framework.utils.function.value.ELFormat.EMAIL
import com.example.framework.utils.function.value.ELFormat.PASSWORD
import com.example.framework.utils.function.value.regCheck
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
            if (hasToast) R.string.email_empty.shortToast()
            return false
        }
        if (text.toString().regCheck(EMAIL)) return true
        if (hasToast) R.string.email_error.shortToast()
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
            if (hasToast) R.string.password_empty.shortToast()
            return false
        }
        if (!text.toString().regCheck(PASSWORD)) {
            if (hasToast) R.string.password_error.shortToast()
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

    /**
     * 返回密码强度
     */
    fun String?.passwordLevel(): Int {
        if (this.isNullOrEmpty()) return 0
        //纯数字、纯字母、纯特殊字符
        if (this.length < 8 || Pattern.matches("^\\d+$", this) || regCheck("^[a-z]+$") || regCheck("^[A-Z]+$") || regCheck("^[@#$%^&]+$")) return 1
        //字母+数字、字母+特殊字符、数字+特殊字符
        if (regCheck("^(?!\\d+$)(?![a-z]+$)[a-z\\d]+$") || regCheck("^(?!\\d+$)(?![A-Z]+$)[A-Z\\d]+$") || regCheck("^(?![a-z]+$)(?![@#$%^&]+$)[a-z@#$%^&]+$") || regCheck("^(?![A-Z]+$)(?![@#$%^&]+$)[A-Z@#$%^&]+$") || regCheck("^(?![a-z]+$)(?![A-Z]+$)[a-zA-Z]+$") || regCheck("^(?!\\d+)(?![@#$%^&]+$)[\\d@#$%^&]+$")) return 2
        //字母+数字+特殊字符
        if (regCheck("^(?!\\d+$)(?![a-z]+$)(?![A-Z]+$)(?![@#$%^&]+$)[\\da-zA-Z@#$%^&]+$")) return 3
        return 3
    }

}