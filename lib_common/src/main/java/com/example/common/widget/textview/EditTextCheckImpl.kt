package com.example.common.widget.textview

import android.widget.EditText
import androidx.annotation.StringRes
import com.example.common.utils.builder.shortToast

interface EditTextCheckImpl {
    companion object {
        val regMail = Regex("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+\$")
        val regPass = Regex("^(?![0-9]+\$)(?![a-zA-Z]+\$)[0-9A-Za-z]{6,20}\$")
    }

    fun EditText.lengthLimit(min: Int, max: Int, @StringRes res: Int = -1): Boolean {
        return if (text.length in min..max) {
            true
        } else {
            res.shortToast()
            false
        }
    }

    fun ClearEditText.lengthLimit(min: Int, max: Int, @StringRes res: Int = -1): Boolean {
        return getEditText().lengthLimit(min, max, res)
    }

    fun PassEditText.lengthLimit(min: Int, max: Int, @StringRes res: Int = -1): Boolean {
        return getEditText().lengthLimit(min, max, res)
    }

    fun EditText.notEmpty(@StringRes res: Int = -1): Boolean {
        return if (!text.isNullOrEmpty()) {
            true
        } else {
            res.shortToast()
            false
        }
    }

    fun ClearEditText.notEmpty(@StringRes res: Int = -1): Boolean {
        return getEditText().notEmpty(res)
    }

    fun PassEditText.notEmpty(@StringRes res: Int = -1): Boolean {
        return getEditText().notEmpty(res)
    }

    fun EditText.checkEmailReg(needToast: Boolean = true): Boolean {
        if (!notEmpty()) {
            if (needToast) "郵箱不能為空".shortToast()
            return false
        }
        if (regMail.matches(text)) return true
        if (needToast) "郵箱格式錯誤".shortToast()
        return false
    }

    fun ClearEditText.checkEmailReg(needToast: Boolean = true): Boolean {
        return getEditText().checkEmailReg(needToast)
    }

    fun PassEditText.checkEmailReg(needToast: Boolean = true): Boolean {
        return getEditText().checkEmailReg(needToast)
    }

    fun EditText.checkPassReg(needToast: Boolean = true): Boolean {
        if (!notEmpty()) {
            if (needToast) "密碼不能為空".shortToast()
            return false
        }
        if (!regPass.matches(text)) {
            if (needToast) "密碼由6~20位的字母和數字組成".shortToast()
            return false
        }
        return true
    }

    fun ClearEditText.checkPassReg(needToast: Boolean = true): Boolean {
        return getEditText().checkPassReg(needToast)
    }

    fun PassEditText.checkPassReg(needToast: Boolean = true): Boolean {
        return getEditText().checkPassReg(needToast)
    }

}