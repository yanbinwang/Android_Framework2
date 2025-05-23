package com.example.common.widget.textview.edittext

import android.view.View
import android.widget.EditText
import androidx.annotation.StringRes
import androidx.lifecycle.LifecycleOwner
import com.example.common.utils.builder.shortToast
import com.example.framework.utils.function.value.ELFormat.EMAIL
import com.example.framework.utils.function.value.ELFormat.MOBILE
import com.example.framework.utils.function.value.ELFormat.PASSWORD
import com.example.framework.utils.function.value.add
import com.example.framework.utils.function.value.divide
import com.example.framework.utils.function.value.matches
import com.example.framework.utils.function.value.multiply
import com.example.framework.utils.function.value.subtract
import com.example.framework.utils.function.view.OnMultiTextWatcher
import com.example.framework.utils.function.view.clear
import com.example.framework.utils.function.view.getNumber
import com.example.framework.utils.function.view.hideKeyboard
import com.example.framework.utils.function.view.onDone
import com.example.framework.utils.function.view.showInput
import java.math.BigDecimal
import java.util.regex.Pattern

/**
 * kt中的接口是可以实现的，实现后的方法只有继承的类才能使用
 * 当前edittext的实现是方便项目使用对应自定义控件的
 * 假定当前输入框页面的交互逻辑是底部提交按钮置灰，只有都输入值的时候才会亮起
 */
interface EditTextImpl {

    // <editor-fold defaultstate="collapsed" desc="内容是否为空">
    fun EditText.notEmpty(@StringRes res: Int = -1): Boolean {
        return text.toString().notEmpty(res)
    }

    fun ClearEditText.notEmpty(@StringRes res: Int = -1): Boolean {
        return editText.notEmpty(res)
    }

    fun PasswordEditText.notEmpty(@StringRes res: Int = -1): Boolean {
        return editText.notEmpty(res)
    }

    fun String?.notEmpty(@StringRes res: Int = -1): Boolean {
        return if (!isNullOrEmpty()) {
            true
        } else {
            if (-1 != res) res.shortToast()
            false
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="内容是否在输入范围内">
    fun EditText.lengthLimit(min: Int, max: Int, @StringRes res: Int = -1): Boolean {
        return text.toString().lengthLimit(min, max, res)
    }

    fun ClearEditText.lengthLimit(min: Int, max: Int, @StringRes res: Int = -1): Boolean {
        return editText.lengthLimit(min, max, res)
    }

    fun String?.lengthLimit(min: Int, max: Int, @StringRes res: Int = -1): Boolean {
        this ?: return false
        return if (length in min..max) {
            true
        } else {
            if (-1 != res) res.shortToast()
            false
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="内容是否符合密码要求">
    fun EditText.checkPassReg(@StringRes res: Int = -1, @StringRes res2: Int = -1): Boolean {
        return text.toString().checkPassReg(res, res2)
    }

    fun PasswordEditText.checkPassReg(@StringRes res: Int = -1, @StringRes res2: Int = -1): Boolean {
        return editText.checkPassReg(res, res2)
    }

    fun String?.checkPassReg(@StringRes res: Int = -1, @StringRes res2: Int = -1): Boolean {
        this ?: return false
        if (!notEmpty()) {
            if (-1 != res) res.shortToast()
            return false
        }
        if (!matches(PASSWORD)) {
            if (-1 != res2) res2.shortToast()
            return false
        }
        return true
    }

    fun String?.passwordLevel(): Int {
        this ?: return 0
        //纯数字、纯字母、纯特殊字符
        if (this.length < 8 || Pattern.matches("^\\d+$", this) || matches("^[a-z]+$") || matches("^[A-Z]+$") || matches("^[@#$%^&]+$")) return 1
        //字母+数字、字母+特殊字符、数字+特殊字符
        if (matches("^(?!\\d+$)(?![a-z]+$)[a-z\\d]+$") || matches("^(?!\\d+$)(?![A-Z]+$)[A-Z\\d]+$") || matches("^(?![a-z]+$)(?![@#$%^&]+$)[a-z@#$%^&]+$") || matches("^(?![A-Z]+$)(?![@#$%^&]+$)[A-Z@#$%^&]+$") || matches("^(?![a-z]+$)(?![A-Z]+$)[a-zA-Z]+$") || matches("^(?!\\d+)(?![@#$%^&]+$)[\\d@#$%^&]+$")) return 2
        //字母+数字+特殊字符
        if (matches("^(?!\\d+$)(?![a-z]+$)(?![A-Z]+$)(?![@#$%^&]+$)[\\da-zA-Z@#$%^&]+$")) return 3
        return 3
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="内容是否符合邮箱要求">
    fun EditText.checkEmailReg(@StringRes res: Int = -1, @StringRes res2: Int = -1): Boolean {
        return text.toString().checkEmailReg(res, res2)
    }

    fun ClearEditText.checkEmailReg(@StringRes res: Int = -1, @StringRes res2: Int = -1): Boolean {
        return editText.checkEmailReg(res, res2)
    }

    fun String?.checkEmailReg(@StringRes res: Int = -1, @StringRes res2: Int = -1): Boolean {
        this ?: return false
        if (!notEmpty()) {
            if (-1 != res) res.shortToast()
            return false
        }
        if (matches(EMAIL)) return true
        if (-1 != res2) res2.shortToast()
        return false
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="内容是否符合手机要求">
    fun EditText.checkMobileReg(@StringRes res: Int = -1, @StringRes res2: Int = -1): Boolean {
        return text.toString().checkMobileReg(res, res2)
    }

    fun ClearEditText.checkMobileReg(@StringRes res: Int = -1, @StringRes res2: Int = -1): Boolean {
        return editText.checkMobileReg(res, res2)
    }

    fun String?.checkMobileReg(@StringRes res: Int = -1, @StringRes res2: Int = -1): Boolean {
        this ?: return false
        if (!notEmpty()) {
            if (-1 != res) res.shortToast()
            return false
        }
        if (!matches(MOBILE)) {
            if (-1 != res2) res2.shortToast()
            return false
        }
        return true
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="内容是否符合验证码要求">
    fun EditText.checkVerifyReg(@StringRes res: Int = -1, @StringRes res2: Int = -1, length: Int = 6): Boolean {
        return text.toString().checkVerifyReg(res, res2, length)
    }

    fun ClearEditText.checkVerifyReg(@StringRes res: Int = -1, @StringRes res2: Int = -1, length: Int = 6): Boolean {
        return editText.checkVerifyReg(res, res2, length)
    }

    fun String?.checkVerifyReg(@StringRes res: Int = -1, @StringRes res2: Int = -1, length: Int = 6): Boolean {
        this ?: return false
        if (!notEmpty()) {
            if (-1 != res) res.shortToast()
            return false
        }
        if (this.length != length) {
            if (-1 != res2) res2.shortToast()
            return false
        }
        return true
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="控件取值计算/监听等构造函数">
    fun ClearEditText?.getNumber(): String {
        this ?: return "0"
        return editText.getNumber()
    }

    fun ClearEditText?.add(number: String?) {
        this ?: return
        setText(getNumber().add(number))
    }

    fun ClearEditText?.subtract(number: String?) {
        this ?: return
        setText(getNumber().subtract(number))
    }

    fun ClearEditText?.multiply(number: String?) {
        this ?: return
        setText(getNumber().multiply(number))
    }

    fun ClearEditText?.divide(number: String?, scale: Int = 0, mode: Int = BigDecimal.ROUND_DOWN) {
        this ?: return
        setText(getNumber().divide(number, scale, mode))
    }

    fun ClearEditText?.isZero(): Boolean {
        return getNumber() == "0"
    }

    fun ClearEditText?.text(): String {
        this ?: return ""
        return getText()
    }

    fun PasswordEditText?.text(): String {
        this ?: return ""
        return getText()
    }

    fun ClearEditText?.clear() {
        if (this == null) return
        editText.clear()
    }

    fun PasswordEditText?.clear() {
        if (this == null) return
        editText.clear()
    }

    fun ClearEditText?.showInput(observer: LifecycleOwner) {
        if (this == null) return
        editText.showInput(observer)
    }

    fun PasswordEditText?.showInput(observer: LifecycleOwner) {
        if (this == null) return
        editText.showInput(observer)
    }

    fun ClearEditText?.hideKeyboard() {
        if (this == null) return
        editText.hideKeyboard()
    }

    fun PasswordEditText?.hideKeyboard() {
        if (this == null) return
        editText.hideKeyboard()
    }

    fun ClearEditText?.onDone(listener: () -> Unit) {
        if (this == null) return
        editText.onDone(listener)
    }

    fun PasswordEditText?.onDone(listener: () -> Unit) {
        if (this == null) return
        editText.onDone(listener)
    }

    fun View?.textWatcher(watcher: OnMultiTextWatcher) {
        when (this) {
            is EditText -> this.addTextChangedListener(watcher)
            is ClearEditText -> this.editText.addTextChangedListener(watcher)
            is PasswordEditText -> this.editText.addTextChangedListener(watcher)
        }
    }

    fun OnMultiTextWatcher.textWatchers(vararg views: View?) {
        for (view in views) {
            when (view) {
                is EditText -> view.addTextChangedListener(this)
                is ClearEditText -> view.editText.addTextChangedListener(this)
                is PasswordEditText -> view.editText.addTextChangedListener(this)
            }
        }
    }
    // </editor-fold>

}

/**
 * @description 自定义Edittext，有一层的嵌套，自定义的edittext都需继承此接口，
 * 便于BaseBottomSheetDialogFragment操作虚拟键盘
 * @author yan
 */
interface SpecialEditText