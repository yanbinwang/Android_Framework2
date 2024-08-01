package com.example.framework.utils.function.view

import android.app.Activity
import android.content.Context
import android.graphics.*
import android.text.*
import android.text.method.HideReturnsTransformationMethod
import android.text.method.LinkMovementMethod
import android.text.method.PasswordTransformationMethod
import android.text.style.ClickableSpan
import android.util.TypedValue
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.CheckBox
import android.widget.EditText
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import androidx.annotation.ColorRes
import androidx.annotation.DimenRes
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import com.example.framework.utils.DecimalInputFilter
import com.example.framework.utils.EditTextUtil
import com.example.framework.utils.builder.TimerBuilder
import com.example.framework.utils.function.value.*
import com.example.framework.utils.function.view.ExtraTextViewFunctions.hideSoftKeyboard
import com.example.framework.utils.function.view.ExtraTextViewFunctions.insertAtFocusedPosition
import com.example.framework.utils.function.view.ExtraTextViewFunctions.showSoftKeyboard
import java.math.BigDecimal
import java.util.*

//------------------------------------textview扩展函数类------------------------------------
/**
 * 直线渐变
 */
fun TextView?.linearGradient(startColor: String?, endColor: String?) {
    if (this == null) return
    paint.shader = LinearGradient(0f, 0f, paint.textSize * text.length, 0f, startColor.parseColor(), endColor.parseColor(), Shader.TileMode.CLAMP)
    invalidate()
}

/**
 * 下划线
 */
fun TextView?.underLine(haveLine: Boolean) {
    if (this == null) return
    paintFlags = if (haveLine) {
        paintFlags or Paint.UNDERLINE_TEXT_FLAG
    } else {
        paintFlags xor Paint.UNDERLINE_TEXT_FLAG
    }
}

/**
 * 加粗
 */
fun TextView?.bold(isBold: Boolean) {
    if (this == null) return
    typeface = if (isBold) {
        Typeface.defaultFromStyle(Typeface.BOLD)
    } else {
        Typeface.defaultFromStyle(Typeface.NORMAL)
    }
}

/**
 * 字体颜色
 */
fun TextView?.textColor(@ColorRes color: Int) {
    if (this == null) return
    this.setTextColor(ContextCompat.getColor(context, color))
}

/**
 * 以res设置textSize
 */
fun TextView?.textSize(@DimenRes size: Int) {
    if (this == null) return
    this.setTextSize(TypedValue.COMPLEX_UNIT_PX, context.resources.getDimension(size))
}

/**
 * 以px做单位设置textSize
 */
fun TextView?.setPxTextSize(size: Float) {
    if (this == null) return
    this.setTextSize(TypedValue.COMPLEX_UNIT_PX, size)
}

/**
 * textview颜色随机
 */
fun TextView?.setRandomTextColor() {
    if (this == null) return
    val random = Random()
    val r = random.nextInt(256)
    val g = random.nextInt(256)
    val b = random.nextInt(256)
    setTextColor(Color.rgb(r, g, b))
}

/**
 * 某些特殊布局如一个输入框最后测有个textview，然后输入框内容又是居中的
 * 通常外层会套一个Framelayout，然后把textview绘制在右侧，但是边距显示会很不正常，
 * 调用当前构造函数修整这种畸形效果,绘制时设置edittext宽度match，textview居左右侧都可以
 */
fun TextView?.setFixDistance(editText: EditText?) {
    if (this == null || editText == null) return
    doOnceAfterLayout { editText.margin(start = it.width, end = it.width) }
}

/**
 * 设置撑满的文本内容
 */
fun TextView?.setMatchText() {
    if (this == null) return
    post {
        val rawText = text.toString()//原始文本
        val tvPaint = paint//paint包含字体等信息
        val tvWidth = width - paddingLeft - paddingRight//控件可用宽度
        val rawTextLines = rawText.replace("\r".toRegex(), "").split("\n").toTypedArray()//将原始文本按行拆分
        val sbNewText = StringBuilder()
        for (rawTextLine in rawTextLines) {
            if (tvPaint.measureText(rawTextLine) <= tvWidth) {
                //如果整行宽度在控件可用宽度之内，就不处理了
                sbNewText.append(rawTextLine)
            } else {
                //如果整行宽度超过控件可用宽度，则按字符测量，在超过可用宽度的前一个字符处手动换行
                var lineWidth = 0f
                var cnt = 0
                while (cnt != rawTextLine.length) {
                    val ch = rawTextLine[cnt]
                    lineWidth += tvPaint.measureText(ch.toString())
                    if (lineWidth <= tvWidth) {
                        sbNewText.append(ch)
                    } else {
                        sbNewText.append("\n")
                        lineWidth = 0f
                        --cnt
                    }
                    ++cnt
                }
            }
            sbNewText.append("\n")
        }
        //把结尾多余的\n去掉
        if (!rawText.endsWith("\n")) sbNewText.deleteCharAt(sbNewText.length - 1)
        text = sbNewText.toString()
    }
}

/**
 * 文案添加点击事件（单一）
 */
fun TextView?.setClickSpan(txt: String, keyword: String, clickableSpan: ClickableSpan) {
    if (this == null) return
    val spannable = SpannableString(txt)
    val index = txt.indexOf(keyword)
    text = if (index != -1) {
        spannable.setSpan(clickableSpan, index, index + keyword.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        spannable
    } else txt
    movementMethod = LinkMovementMethod.getInstance()
}

fun TextView?.setClickSpan(txt: String, keyword: String, colorRes: Int, listener: () -> Unit) {
    if (this == null) return
    setClickSpan(txt, keyword, object : ClickableSpan() {
        override fun onClick(widget: View) {
            listener.invoke()
            movementMethod = LinkMovementMethod.getInstance()
        }

        override fun updateDrawState(ds: TextPaint) {
            super.updateDrawState(ds)
            ds.color = ContextCompat.getColor(context, colorRes)
            ds.isUnderlineText = false
        }
    })
}

/**
 * 获取textview文字占据的行数
 * xml中设置属性：
 * android:ellipsize="end"
 * android:maxLines="2"
 * 代码中在设置了text后调取
 * 需要注意如果在list列表的话，数据不宜过多，会造成卡顿
 */
inline fun TextView?.getEllipsisCount(crossinline listener: (ellipsisCount: Int) -> Unit = {}) {
    if (this == null) {
        listener.invoke(0)
        return
    }
    post {
        listener.invoke(layout.getEllipsisCount(lineCount - 1))
    }
}

/**
 * 如果textview文字带有链接跳转，直接使用此构造函数
 */
fun TextView?.setSpannable(spannable: Spannable) {
    this ?: return
    text = spannable
    movementMethod = LinkMovementMethod.getInstance()
}

/**
 * EditText输入密码是否可见(显隐)
 */
fun EditText?.passwordDevelopment(): Boolean {
    if (this == null) return false
    var display = false
    try {
        if (transformationMethod == HideReturnsTransformationMethod.getInstance()) {
            transformationMethod = PasswordTransformationMethod.getInstance()
            display = false
        } else {
            transformationMethod = HideReturnsTransformationMethod.getInstance()
            display = true
        }
        setSelection(text.length)
        postInvalidate()
    } catch (_: Exception) {
    }
    return display
}

/**
 * EditText输入金额小数限制
 */
fun EditText?.decimalFilter(decimalPoint: Int = 2) {
    if (this == null) return
    removeFilter { it is DecimalInputFilter }
    val decimalInputFilter = DecimalInputFilter()
    decimalInputFilter.decimalPoint = decimalPoint
    addFilter(decimalInputFilter)
}

/**
 * EditText不允许输入空格
 */
fun EditText?.spaceLimit() {
    if (this == null) return
    addFilter(object : InputFilter {
        override fun filter(source: CharSequence?, start: Int, end: Int, dest: Spanned?, dstart: Int, dend: Int): CharSequence? {
            val result = source ?: ""
            return if (result == " ") "" else null
        }
    })
}

/**
 * 添加EditText的InputFilter
 */
fun EditText?.addFilter(vararg filterList: InputFilter) {
    if (this == null) return
    filters = filters.plus(filterList)
}

/**
 * 去除EditText的InputFilter
 */
fun EditText?.removeFilter(vararg filterList: InputFilter) {
    if (this == null) return
    filters = arrayOf<InputFilter>().plus(filters.filter { !filterList.contains(it) })
}

internal fun EditText?.removeFilter(func: (InputFilter) -> Boolean) {
    if (this == null) return
    val filterList = filters.toNewList { it }
    filterList.forEach {
        if (func(it)) filterList.remove(it)
    }
    filters = filterList.toTypedArray()
}

/**
 * 添加回车时的处理
 */
fun EditText?.onDone(listener: () -> Unit) {
    if (this == null) return
    setOnEditorActionListener { _, id, _ ->
        if (id == EditorInfo.IME_ACTION_SEARCH || id == EditorInfo.IME_ACTION_UNSPECIFIED || id == EditorInfo.IME_ACTION_DONE) {
            listener()
            hideKeyboard()
            return@setOnEditorActionListener true
        }
        return@setOnEditorActionListener false
    }
}

/**
 * 弹出软键盘并获取焦点
 */
fun EditText?.showInput() {
    if (this == null) return
    focus()
    TimerBuilder.schedule({
        showSoftKeyboard(context, this)
    }, 200)
}

///**
// * 弹出软键盘
// */
//fun EditText?.doInput() {
//    if (this == null) return
//    requestFocus()
//    showSoftKeyboard(context, this)
////    val inputManager = this.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
////    inputManager.showSoftInput(this, 0)
//}

/**
 * 隐藏软键盘
 */
fun EditText?.hideKeyboard() {
    if (this == null) return
    hideSoftKeyboard(context, this)
    clearFocus()
}

/**
 * 清除
 */
fun EditText?.clear() {
    if (this == null) return
    setText("")
    hideKeyboard()
}

/**
 * 指定光标插入字符串
 */
fun EditText?.insertAtFocus(string: String) {
    this ?: return
    insertAtFocusedPosition(this, string)
}

/**
 * 安全选中某个光标
 */
fun EditText?.setSafeSelection(start: Int, stop: Int? = null) {
    this ?: return
    if (start !in 0..text.length) return
    try {
        if (stop == null) {
            setSelection(start)
        } else {
            setSelection(start, stop)
        }
    } catch (_: Exception) {
    }
}

/**
 * 数学计算相关（加减乘除-》取值）
 * EditText.inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL or InputType.TYPE_NUMBER_FLAG_SIGNED
 */
fun EditText?.getNumber(): String {
    this ?: return "0"
    return text.toString().ifEmpty { "0" }
}

fun EditText?.add(number: String?) {
    this ?: return
    setText(getNumber().add(number))
}

fun EditText?.subtract(number: String?) {
    this ?: return
    setText(getNumber().subtract(number))
}

fun EditText?.multiply(number: String?) {
    this ?: return
    setText(getNumber().multiply(number))
}

fun EditText?.divide(number: String?, scale: Int = 0, mode: Int = BigDecimal.ROUND_DOWN) {
    this ?: return
    setText(getNumber().divide(number, scale, mode))
}

/**
 * 限制输入内容为目标值
 * "0123456789."
 */
fun EditText?.charLimit(characterAllowed: CharArray) {
    this ?: return
    EditTextUtil.setCharLimit(this, characterAllowed)
}

/**
 * 限制输入框输入emoji
 */
fun EditText?.emojiLimit() {
    this ?: return
    EditTextUtil.setEmojiLimit(this)
}

/**
 * 限制只能输入中文和英文数字和符号
 */
fun EditText?.chineseLimit() {
    this ?: return
    EditTextUtil.setChineseLimit(this)
}

/**
 * 设置EditText输入的最大长度
 */
fun EditText?.maxLength(maxLength: Int) {
    this ?: return
    EditTextUtil.setMaxLength(this, maxLength)
}

/**
 * 设置EditText输入数值的最大值
 */
fun EditText?.maxValue(maxLength: Int, maxDecimal: Int) {
    this ?: return
    EditTextUtil.setMaxValue(this, maxLength, maxDecimal)
}

/**
 * 设置输出格式
 */
fun EditText?.inputType(inputType: Int) {
    this ?: return
    EditTextUtil.setInputType(this, inputType)
}

/**
 * 设置按键格式
 */
fun EditText?.imeOptions(imeOptions: Int) {
    this ?: return
    EditTextUtil.setImeOptions(this, imeOptions)
}

/**
 * 限制输入内容为非目标值
 */
fun EditText?.charBlackList(characterAllowed: CharArray) {
    this ?: return
    EditTextUtil.setCharBlackList(this, characterAllowed)
}

/**
 * 屏蔽页面中的edit输入框的弹出
 */
fun Activity?.inputHidden(vararg edits: EditText?): ArrayList<EditText?>? {
    this ?: return null
    val list = listOf(*edits)
    //建立对应的绑定关系，让edittext不再弹出系统的输入框
    window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)
    try {
        val setShowSoftInputOnFocus = EditText::class.java.getMethod("setShowSoftInputOnFocus", Boolean::class.javaPrimitiveType)
        setShowSoftInputOnFocus.isAccessible = true
        list.forEach { setShowSoftInputOnFocus.invoke(it, false) }
    } catch (_: Exception) {
    }
    return list.toArrayList()
}

/**
 * 单选框状态改变
 */
fun CheckBox?.checked() {
    this ?: return
    isChecked = !isChecked
}

fun CheckBox?.checked(checked: Boolean) {
    this ?: return
    isChecked = checked
}

/**
 * 获取RadioButton
 */
fun RadioGroup?.button(index: Int): RadioButton? {
    this ?: return null
    return getChildAt(index) as? RadioButton
}

fun RadioGroup?.checked(index: Int, checked: Boolean) {
    this ?: return
    button(index).checked(checked)
}

fun RadioButton?.checked(checked: Boolean) {
    this ?: return
    isChecked = checked
}

fun RadioGroup?.checkedIndex(): Int {
    this ?: return 0
    var ids = 0
    for (index in 0 until childCount.orZero) {
        if (button(index)?.isChecked.orFalse) {
            ids = index
            break
        }
    }
    return ids
}

/**
 * 由于RadioGroup继承的是线性布局，故而是不能自动换行的
 * 所以如果碰到单选需要换行的选项界面，采用约束布局绘制，获取其中的child进行强转换
 */
fun ConstraintLayout?.button(index: Int): RadioButton? {
    this ?: return null
    return getChildAt(index) as? RadioButton
}

fun ConstraintLayout?.checked(index: Int, checked: Boolean) {
    this ?: return
    for (position in 0 until childCount) {
        button(position).checked(false)
    }
    button(index).checked(checked)
}

fun ConstraintLayout?.checkedIndex(): Int {
    this ?: return 0
    var ids = 0
    for (index in 0 until childCount.orZero) {
        if (button(index)?.isChecked.orFalse) {
            ids = index
            break
        }
    }
    return ids
}

fun ConstraintLayout?.setOnCheckedChangeListener(listener: (index: Int) -> Unit = {}) {
    this ?: return
    for (index in 0 until childCount.orZero) {
        button(index)?.click {
            checked(index, true)
            listener.invoke(index)
        }
    }
}

/**
 * 简易Edittext监听
 */
fun OnMultiTextWatcher.textWatchers(vararg views: EditText) {
    for (view in views) {
        view.addTextChangedListener(this)
    }
}

/**
 * 简易的输入监听
 */
interface OnMultiTextWatcher : TextWatcher {
    override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
    }

    override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
    }

    override fun afterTextChanged(s: Editable) {
    }
}

object ExtraTextViewFunctions {

    /**
     * 弹出软键盘
     */
    fun showSoftKeyboard(context: Context, view: View) {
        val inputManager = context.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
        inputManager?.showSoftInput(view, 0)
    }

    /**
     * 隐藏软键盘(可用于Activity，Fragment)
     */
    fun hideSoftKeyboard(context: Context, view: View) {
        val inputMethodManager = context.getSystemService(Activity.INPUT_METHOD_SERVICE) as? InputMethodManager
        inputMethodManager?.hideSoftInputFromWindow(view.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
    }

    /**
     * 实现将扫描结果插入在EditText光标处
     * @param editText
     * @param index 获取光标所在位置
     * @param text 插入的文本
     */
    fun insertAtFocusedPosition(editText: EditText, text: String) {
        val index = editText.selectionStart //获取光标所在位置
        val edit = editText.editableText //获取EditText的文字
        if (index in edit.indices) {
            edit.insert(index, text) //光标所在位置插入文字
        } else {
            edit.append(text)
        }
    }
}