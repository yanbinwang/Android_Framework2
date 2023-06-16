package com.example.common.widget.textview.edittext.callback

import android.view.View
import android.widget.EditText
import androidx.annotation.StringRes
import com.example.common.utils.builder.shortToast
import com.example.common.widget.textview.edittext.ClearEditText
import com.example.common.widget.textview.edittext.PasswordEditText
import com.example.framework.utils.function.value.ELFormat.EMAIL
import com.example.framework.utils.function.value.ELFormat.MOBILE
import com.example.framework.utils.function.value.ELFormat.PASSWORD
import com.example.framework.utils.function.value.add
import com.example.framework.utils.function.value.divide
import com.example.framework.utils.function.value.multiply
import com.example.framework.utils.function.value.regCheck
import com.example.framework.utils.function.value.subtract
import com.example.framework.utils.function.view.OnMultiTextWatcher
import com.example.framework.utils.function.view.getNumber
import com.example.framework.utils.function.view.onDone
import com.example.framework.utils.function.view.text
import java.math.BigDecimal
import java.util.regex.Pattern

/**
 * kt中的接口是可以实现的，实现后的方法只有继承的类才能使用
 * 当前edittext的实现是方便项目使用对应自定义控件的
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
    fun EditText.checkEmailReg(@StringRes res: Int = -1): Boolean {
        if (!notEmpty()) {
            if (-1 != res) res.shortToast()
            return false
        }
        if (text().regCheck(EMAIL)) return true
        if (-1 != res) res.shortToast()
        return false
    }

    fun ClearEditText.checkEmailReg(@StringRes res: Int = -1): Boolean {
        return editText.checkEmailReg(res)
    }

    fun PasswordEditText.checkEmailReg(@StringRes res: Int = -1): Boolean {
        return editText.checkEmailReg(res)
    }

    /**
     * 检测内容文本是否符合密码要求
     */
    fun EditText.checkPassReg(@StringRes res: Int = -1): Boolean {
        if (!notEmpty()) {
            if (-1 != res) res.shortToast()
            return false
        }
        if (!text().regCheck(PASSWORD)) {
            if (-1 != res) res.shortToast()
            return false
        }
        return true
    }

    fun ClearEditText.checkPassReg(@StringRes res: Int = -1): Boolean {
        return editText.checkPassReg(res)
    }

    fun PasswordEditText.checkPassReg(@StringRes res: Int = -1): Boolean {
        return editText.checkPassReg(res)
    }

    /**
     * 检测内容文本是否符合手机号要求
     */
    fun EditText.checkMobileReg(@StringRes res: Int = -1): Boolean {
        if (!notEmpty()) {
            if (-1 != res) res.shortToast()
            return false
        }
        if (!text().regCheck(MOBILE)) {
            if (-1 != res) res.shortToast()
            return false
        }
        return true
    }

    fun ClearEditText.checkMobileReg(@StringRes res: Int = -1): Boolean {
        return editText.checkMobileReg(res)
    }

    fun PasswordEditText.checkMobileReg(@StringRes res: Int = -1): Boolean {
        return editText.checkMobileReg(res)
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

    fun ClearEditText?.text(): String {
        this ?: return ""
        return getText()
    }

    fun PasswordEditText?.text(): String {
        this ?: return ""
        return getText()
    }

    fun ClearEditText?.getNumber(): String {
        this ?: return "0"
        return editText.getNumber()
    }

    fun ClearEditText?.add(number: String) {
        this ?: return
        setText(getNumber().add(number))
    }

    fun ClearEditText?.subtract(number: String) {
        this ?: return
        setText(getNumber().subtract(number))
    }

    fun ClearEditText?.multiply(number: String) {
        this ?: return
        setText(getNumber().multiply(number))
    }

    fun ClearEditText?.divide(number: String, scale: Int = 0, mode: Int = BigDecimal.ROUND_DOWN) {
        this ?: return
        setText(getNumber().divide(number, scale, mode))
    }

    fun ClearEditText?.onDone(listener: () -> Unit) {
        if (this == null) return
        editText.onDone(listener)
    }

    fun PasswordEditText?.onDone(listener: () -> Unit) {
        if (this == null) return
        editText.onDone(listener)
    }

    fun OnMultiTextWatcher.textWatcher(vararg views: View) {
        for (view in views) {
            when (view) {
                is EditText -> view.addTextChangedListener(this)
                is ClearEditText -> view.editText.addTextChangedListener(this)
                is PasswordEditText -> view.editText.addTextChangedListener(this)
            }
        }
    }

}