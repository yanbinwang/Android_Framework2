package com.example.common.widget.textview.edit

import android.widget.EditText
import androidx.annotation.StringRes
import com.example.common.utils.builder.shortToast

/**
 * kt中的接口是可以实现的，实现后的方法只有继承的类才能使用
 */
interface EditTextImpl {

    /**
     * el表达式
     */
    companion object {
        val regMail = Regex("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+\$")
        val regPass = Regex("^(?![0-9]+\$)(?![a-zA-Z]+\$)[0-9A-Za-z]{6,20}\$")
    }

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
        if (regMail.matches(text)) return true
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
        if (!regPass.matches(text)) {
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