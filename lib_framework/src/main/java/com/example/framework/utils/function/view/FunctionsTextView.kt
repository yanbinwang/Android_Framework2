package com.example.framework.utils.function.view

import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Shader
import android.graphics.Typeface
import android.os.Build
import android.text.Editable
import android.text.InputFilter
import android.text.Spannable
import android.text.Spanned
import android.text.TextWatcher
import android.text.method.HideReturnsTransformationMethod
import android.text.method.LinkMovementMethod
import android.text.method.PasswordTransformationMethod
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
import androidx.lifecycle.LifecycleOwner
import com.example.framework.utils.DecimalInputFilter
import com.example.framework.utils.EditTextUtil
import com.example.framework.utils.builder.TimerBuilder
import com.example.framework.utils.function.value.add
import com.example.framework.utils.function.value.divide
import com.example.framework.utils.function.value.multiply
import com.example.framework.utils.function.value.orFalse
import com.example.framework.utils.function.value.orZero
import com.example.framework.utils.function.value.parseColor
import com.example.framework.utils.function.value.subtract
import com.example.framework.utils.function.value.toArrayList
import com.example.framework.utils.function.value.toNewList
import com.example.framework.utils.function.view.ExtraTextViewFunctions.hideSoftKeyboard
import com.example.framework.utils.function.view.ExtraTextViewFunctions.insertAtFocusedPosition
import com.example.framework.utils.function.view.ExtraTextViewFunctions.showSoftKeyboard
import java.math.BigDecimal
import java.util.Random

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
 * 字重数值	对应名称（英文）	中文描述	      常见使用场景
 * 100	      Thin	         极细	     特殊设计感标题
 * 200	    Extra Light	     超轻	     轻量正文、辅助文字
 * 300	      Light	         轻量	      正文、注释
 * 400	     Regular	   常规（默认）    大多数正文内容
 * 500	     Medium	         中等	   强调性文字（比常规稍重）
 * 600	    Semi Bold	     半粗	   小标题、重点标签
 * 700	     Bold	         加粗（标准）	  标题、按钮文字
 * 800	   Extra Bold	     超粗	   大标题、强强调文字
 * 900	     Black	         特粗	     品牌名、醒目标题
 */
fun TextView?.textFontWeight(weight: Int) {
    if (this == null) return
    val validWeight = weight.coerceIn(100, 900)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        // 原生方法，直接设置字重
        setTypeface(Typeface.create(null as Typeface?, validWeight))
    } else {
        // 低版本（23-25）兼容逻辑
        when {
            validWeight >= 700 -> {
                // 模拟加粗（700-900）：启用 fakeBold，可适当增加描边
                paint.isFakeBoldText = true
                paint.style = Paint.Style.FILL_AND_STROKE
                // 基于文字大小动态调整
                paint.strokeWidth = textSize * 0.01f
            }
            validWeight <= 300 -> {
                // 模拟细体（100-300）：禁用 fakeBold，确保无描边
                paint.isFakeBoldText = false
                paint.style = Paint.Style.FILL
                paint.strokeWidth = 0f
            }
            else -> {
                // 常规/中等（400-600）：默认样式
                paint.isFakeBoldText = false
                paint.style = Paint.Style.FILL
                paint.strokeWidth = 0f
            }
        }
        invalidate()
    }
}

/**
 * 字体颜色
 */
fun TextView?.textColor(@ColorRes res: Int) {
    if (this == null) return
    this.setTextColor(ContextCompat.getColor(context, res))
}

/**
 * textview颜色随机
 */
fun TextView?.randomTextColor() {
    if (this == null) return
    val random = Random()
    val r = random.nextInt(256)
    val g = random.nextInt(256)
    val b = random.nextInt(256)
    setTextColor(Color.rgb(r, g, b))
}

/**
 * 通过 dimens.xml 中定义的资源 ID，给 TextView 设置文字大小
 * 不管资源里写的是什么单位，最终都转成像素（PX）给 TextView 用
 *
 * setTextSize(TypedValue.COMPLEX_UNIT_PX, ...)：
 * 第一个参数 TypedValue.COMPLEX_UNIT_PX 明确告诉系统：“后面的数值单位是像素”。
 * 因此，TextView 会直接使用转换后的像素值设置文字大小。
 *
 * context.resources.getDimension(res)：
 * 从资源中获取尺寸值，自动将资源中定义的单位（dimes用的 pt）转换为 PX（像素）。
 * 举例：若 dimens.xml 中是 <dimen name="textSize12">12pt</dimen>，
 * getDimension 会根据当前设备的屏幕密度，把 12pt 转换为对应的像素值（比如在某设备上可能是 24px）。
 */
fun TextView?.textSize(@DimenRes res: Int) {
    if (this == null) return
    this.setTextSize(TypedValue.COMPLEX_UNIT_PX, context.resources.getDimension(res))
}

/**
 * 直接接收一个像素值（PX），并将其设置为 TextView 的文字大小
 * 使用扩展函数12.pt传入的话,会以设计图换算后的值为准设置给 TextView 用
 */
fun TextView?.pxTextSize(size: Float) {
    if (this == null) return
    this.setTextSize(TypedValue.COMPLEX_UNIT_PX, size)
}

/**
 * 将 value 按 SP 单位，结合当前页面的屏幕参数，转换为 PX
 */
fun TextView?.spTextSize(value: Float) {
    if (this == null) return
    // 用当前上下文的 metrics 转换 SP 为 PX
    val pxValue = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, value, context.resources.displayMetrics)
    // 显式指定单位为 PX，避免二次转换
    this.setTextSize(TypedValue.COMPLEX_UNIT_PX, pxValue)
}

/**
 * 某些特殊布局如一个输入框最后测有个textview，然后输入框内容又是居中的
 * 通常外层会套一个Framelayout，然后把textview绘制在右侧，但是边距显示会很不正常，
 * 调用当前构造函数修整这种畸形效果,绘制时设置edittext宽度match，textview居左右侧都可以
 */
fun TextView?.fixDistance(editText: EditText?) {
    if (this == null || editText == null) return
    doOnceAfterLayout {
        editText.margin(start = it.measuredWidth, end = it.measuredWidth)
    }
}

/**
 * 设置撑满的文本内容
 */
fun TextView?.matchText() {
    if (this == null) return
    // 如果已经完成布局，直接处理文本,若未完成布局，监听布局变化，布局完成后处理文本
    doOnceAfterLayout {
        // 获取原始文本
        val rawText = text.toString()
        // 获取 TextView 的画笔，包含字体等信息
        val tvPaint = paint
        // 计算 TextView 可用于显示文本的宽度
        val tvWidth = width - paddingLeft - paddingRight
        // 移除原始文本中的 \r 并按 \n 分割成多行
        val rawTextLines = rawText.replace("\r", "").split('\n')
        // 用于存储处理后的文本
        val sbNewText = StringBuilder()
        // 重新生成字符串
        for (line in rawTextLines) {
            if (tvPaint.measureText(line) <= tvWidth) {
                // 若当前行宽度小于等于可用宽度，直接添加到结果中
                sbNewText.append(line)
            } else {
                // 若当前行宽度超过可用宽度，按字符拆分处理
                var currentLine = StringBuilder()
                var currentWidth = 0f
                for (char in line) {
                    val charWidth = tvPaint.measureText(char.toString())
                    if (currentWidth + charWidth <= tvWidth) {
                        // 若添加当前字符后宽度仍小于等于可用宽度，添加该字符
                        currentLine.append(char)
                        currentWidth += charWidth
                    } else {
                        // 若添加当前字符后宽度超过可用宽度，换行并重置当前行和宽度
                        sbNewText.append(currentLine).append('\n')
                        currentLine = StringBuilder().append(char)
                        currentWidth = charWidth
                    }
                }
                // 添加最后一行处理后的文本
                sbNewText.append(currentLine)
            }
            // 每行处理完后添加换行符
            sbNewText.append('\n')
        }
        // 移除末尾多余的换行符
        if (sbNewText.isNotEmpty() && sbNewText.last() == '\n') {
            sbNewText.deleteCharAt(sbNewText.length - 1)
        }
        // 更新 TextView 的文本
        text = sbNewText.toString()
    }
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
    // 若 TextView 已经完成布局，直接获取省略字符数量
    doOnceAfterLayout {
        val ellipsisCount = layout?.getEllipsisCount(lineCount - 1).orZero
        listener.invoke(ellipsisCount)
    }
}

/**
 * 如果textview文字带有链接跳转，直接使用此构造函数
 */
fun TextView?.setSpannable(spannable: Spannable) {
    this ?: return
    text = spannable
    movementMethod = LinkMovementMethod.getInstance()
    clearHighlightColor()
}

/**
 * 清除高亮
 */
fun TextView?.clearHighlightColor() {
    if (this == null) return
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        highlightColor = Color.TRANSPARENT
    }
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
    } catch (e: Exception) {
        e.printStackTrace()
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
 * class InputDialog(mContext: Context) : BaseDialog<ViewDialogInputBinding>(mContext, MATCH_PARENT, 60, BOTTOM, R.style.InputDialogStyle, false, false) {
 *     private var listener: ((text: String) -> Unit)? = null
 *
 *     init {
 *         mBinding?.tvSend.click {
 *             listener?.invoke(mBinding?.etContent.text())
 *             mBinding?.etContent.clear()
 *             dismiss()
 *         }
 *     }
 *
 *     fun showInput() {
 *         show()
 *         mBinding?.etContent.showInput()
 *     }
 *
 *     fun setOnInputListener(listener: ((text: String) -> Unit)) {
 *         this.listener = listener
 *     }
 *
 * }
 * <style name="InputDialogStyle" parent="android:Theme.Dialog">
 *     <item name="android:windowBackground">@android:color/transparent</item>
 *     <item name="android:windowNoTitle">true</item>
 *     <item name="android:windowAnimationStyle">@null</item>
 *     <!-- 完全透明加入下面这句 -->
 *     <item name="android:backgroundDimEnabled">false</item>
 * </style>
 */
fun EditText?.showInput(observer: LifecycleOwner) {
    if (this == null) return
    focus()
    TimerBuilder.schedule(observer, {
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
    } catch (e: Exception) {
        e.printStackTrace()
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
    // 建立对应的绑定关系，让edittext不再弹出系统的输入框
    window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)
    // api26+的版本已经公开了setShowSoftInputOnFocus,不再需要反射
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        list.forEach { editText ->
            editText?.showSoftInputOnFocus = false
        }
    } else {
        try {
            val method = EditText::class.java.getMethod("setShowSoftInputOnFocus", Boolean::class.javaPrimitiveType)
            method.isAccessible = true
            list.forEach { editText ->
                editText?.let { method.invoke(it, false) }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
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
        // 获取光标所在位置
        val index = editText.selectionStart
        // 获取EditText的文字
        val edit = editText.editableText
        if (index in edit.indices) {
            // 光标所在位置插入文字
            edit.insert(index, text)
        } else {
            edit.append(text)
        }
    }
}