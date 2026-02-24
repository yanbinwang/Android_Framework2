package com.example.framework.utils

import android.text.Editable
import android.text.InputFilter
import android.text.InputType
import android.text.Selection
import android.text.Spanned
import android.text.TextWatcher
import android.text.method.DigitsKeyListener
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import com.example.framework.utils.function.value.execute
import com.example.framework.utils.function.value.fitRange
import com.example.framework.utils.function.value.numberCompareTo
import com.example.framework.utils.function.value.orZero
import com.example.framework.utils.function.view.addFilter
import com.example.framework.utils.function.view.decimalLimitFilter
import com.example.framework.utils.function.view.text
import java.lang.ref.WeakReference

// <editor-fold defaultstate="collapsed" desc="工具类方法">
object EditTextUtil {
    private var chineseParam: CharArray = charArrayOf(
        '」', '，', '。', '？', '…', '：', '～', '【', '＃', '、', '％',
        '＊', '＆', '＄', '（', '‘', '’', '“', '”', '『', '〔', '｛',
        '【', '￥', '￡', '‖', '〖', '《', '「', '》', '〗', '】', '｝',
        '〕', '』', '”', '）', '！', '；', '—')

    /**
     * 设置文本框自动四位加一个空格（手机号格式）
     * 注意：getText会包含空格，需手动replace(" ", "")
     */
    fun setPhoneNumSpace(target: EditText) {
        val filters = arrayOf<InputFilter>(InputFilter.LengthFilter(13))
        target.filters = filters
        target.inputType = InputType.TYPE_CLASS_PHONE
        target.addTextChangedListener(NumSpaceTextWatcher(WeakReference(target)))
    }

    /**
     * 限制输入内容仅为指定字符（白名单）
     * @param target 目标EditText
     * @param characterWhiteList 允许输入的字符数组（如："0123456789.".toCharArray()）
     */
    fun setCharWhiteListLimitFilter(target: EditText, characterWhiteList: CharArray) {
        target.addFilter(getCharWhiteListLimitFilter(characterWhiteList))
    }

    /**
     * 重载：支持字符串参数（自动转字符数组）
     */
    fun getCharWhiteListLimitFilter(characterWhiteList: String): InputFilter {
        return getCharWhiteListLimitFilter(characterWhiteList.toCharArray())
    }

    /**
     * 白名单筛选器：仅允许输入指定字符
     */
    fun getCharWhiteListLimitFilter(characterWhiteList: CharArray): InputFilter {
        return InputFilter { source, start, end, _, _, _ ->
            val sourceText = source?.toString() ?: ""
            // 空输入（删除/回退）直接放行
            if (sourceText.isEmpty()) return@InputFilter null
            val sb = StringBuilder()
            for (i in start until end) {
                val char = source[i]
                // 只保留白名单内的字符
                if (characterWhiteList.contains(char)) {
                    sb.append(char)
                }
            }
            // 直接返回筛选结果
            sb.toString().takeIf { it.isNotEmpty() } ?: ""
        }
    }

    /**
     * 限制输入内容排除指定字符（黑名单）
     * @param target 目标EditText
     * @param characterBlackList 禁止输入的字符数组
     */
    fun setCharBlackListLimitFilter(target: EditText, characterBlackList: CharArray) {
        target.addFilter(getCharBlackListFilter(characterBlackList))
    }

    fun getCharBlackListFilter(characterBlackList: String): InputFilter {
        return getCharBlackListFilter(characterBlackList.toCharArray())
    }

    /**
     * 黑名单筛选器：禁止输入指定字符
     */
    fun getCharBlackListFilter(characterBlackList: CharArray): InputFilter {
        return InputFilter { source, start, end, _, _, _ ->
            val sourceText = source?.toString() ?: ""
            // 空输入（删除/回退）直接放行
            if (sourceText.isEmpty()) return@InputFilter null
            val sb = StringBuilder()
            for (i in start until end) {
                val char = source[i]
                // 排除黑名单内的字符
                if (!characterBlackList.contains(char)) {
                    sb.append(char)
                }
            }
            // 直接返回筛选结果
            sb.toString().takeIf { it.isNotEmpty() } ?: ""
        }
    }

    /**
     * 限制输入框输入emoji
     */
    fun setEmojiLimit(target: EditText) {
        target.addFilter({ source, start, end, _, _, _ ->
            val sb = StringBuilder()
            for (i in start until end) {
                val char = source[i]
                // 只保留非Emoji字符
                if (isNonEmojiCharacter(char)) {
                    sb.append(char)
                }
            }
            sb.toString().takeIf { it.isNotEmpty() } ?: ""
        })
    }

    /**
     * 判断一个字符「是不是非Emoji」
     */
    fun isNonEmojiCharacter(codePoint: Char): Boolean {
        return codePoint.code == 0x0 || codePoint.code == 0x9 || codePoint.code == 0xA || codePoint.code == 0xD
                || codePoint.code in 0x20..0xD7FF || codePoint.code in 0xE000..0xFFFD
                || codePoint.code in 0x10000..0x10FFFF
    }

    /**
     * 限制只能输入中文和英文数字和符号
     */
    fun setChineseLimit(target: EditText) {
        target.addFilter({ source, start, end, _, _, _ ->
            val sb = StringBuilder()
            for (i in start until end) {
                val char = source[i]
                // 保留合法字符：中文、字母、数字、下划线
                if (isChinese(char) || Character.isLetterOrDigit(char) || char == '_') {
                    sb.append(char)
                }
            }
            sb.toString().takeIf { it.isNotEmpty() } ?: ""
        })
    }

    /**
     * 判定输入汉字是否是中文
     */
    fun isChinese(c: Char): Boolean {
        for (param in chineseParam) {
            if (param == c) {
                return false
            }
        }
        val ub = Character.UnicodeBlock.of(c)
        return ub === Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS
                || ub === Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS
                || ub === Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A
                || ub === Character.UnicodeBlock.GENERAL_PUNCTUATION
                || ub === Character.UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION
                || ub === Character.UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS
    }

    /**
     * 设置EditText输入的最大长度
     */
    fun setMaxLength(target: EditText, maxLength: Int) {
        target.addFilter(InputFilter.LengthFilter(maxLength))
    }

    /**
     * 设置EditText输入数值的最大值
     */
    fun setMaxValue(target: EditText, maxInteger: Int, maxDecimal: Int) {
        val filters = target.filters.toMutableList()
        filters.removeAll { it is InputFilter.LengthFilter }
        filters.removeAll { it is NumInputFilter }
        target.filters = filters.toTypedArray()
        target.addFilter(InputFilter.LengthFilter(maxInteger + 1 + maxDecimal))
        target.addFilter(getNumInputFilter(maxInteger, maxDecimal))
    }

    /**
     * 获取数值限制筛选器
     */
    fun getNumInputFilter(maxInteger: Int, maxDecimal: Int): InputFilter {
        return NumInputFilter(maxInteger, maxDecimal)
    }

    /**
     * 设置输出格式
     */
    fun setInputType(target: EditText?, inputType: Int) = target?.execute {
        when (inputType) {
            // text
            0 -> setInputType(InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_NORMAL)
            // textPassword
            1 -> setInputType(InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD)
            // phone
            2 -> setInputType(InputType.TYPE_CLASS_PHONE)
            // number
            3 -> setInputType(InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_VARIATION_NORMAL)
            // rate,numberDecimal
            9, 4 -> setInputType(InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL)
            // email
            5 -> setInputType(InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS)
            // idcard,textVisiblePassword
            8, 6 -> setInputType(InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD)
            // money
            7 -> setInputType(InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL or InputType.TYPE_NUMBER_FLAG_SIGNED)
            // text
            else -> setInputType(InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_NORMAL)
        }
    }

    /**
     * 设置按键格式
     */
    fun setImeOptions(target: EditText, imeOptions: Int) = target.execute {
        when (imeOptions) {
            0 -> setImeOptions(EditorInfo.IME_ACTION_DONE)
            1 -> setImeOptions(EditorInfo.IME_ACTION_GO)
            2 -> setImeOptions(EditorInfo.IME_ACTION_NEXT)
            3 -> setImeOptions(EditorInfo.IME_ACTION_NONE)
            4 -> setImeOptions(EditorInfo.IME_ACTION_PREVIOUS)
            5 -> setImeOptions(EditorInfo.IME_ACTION_SEARCH)
            6 -> setImeOptions(EditorInfo.IME_ACTION_SEND)
            7 -> setImeOptions(EditorInfo.IME_ACTION_UNSPECIFIED)
            else -> setImeOptions(EditorInfo.IME_ACTION_DONE)
        }
    }
}
// </editor-fold>

// <editor-fold defaultstate="collapsed" desc="输入监听">
/**
 * 用于输入框失去焦点时
 * 自动根据输入框内的值吸附最大最小值(处在范围内的值不会改变)
 * 默认大小和小数位数不做限制
 */
class RangeHelper(private val view: WeakReference<EditText>?, hasAuto: Boolean = true) {
    private var min: String? = null
    private var max: String? = null
    private val editText get() = view?.get()

    init {
        editText?.inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL or InputType.TYPE_NUMBER_FLAG_SIGNED
        editText?.onFocusChangeListener = View.OnFocusChangeListener { _, hasFocus ->
            if (!hasFocus && hasAuto) {
                getText()
            }
        }
    }

    /**
     * 设置输入范围和小数位数限制
     * @param min 最小值，可为 null
     * @param max 最大值，可为 null
     * @param digits 小数位数限制，-1 表示不限制
     */
    fun setRange(min: String? = null, max: String? = null, digits: Int? = -1) {
        this.min = min
        this.max = max
        if (digits != -1) editText?.decimalLimitFilter(digits.orZero)
    }

    /**
     * 获取符合范围要求的输入值
     * @return 符合范围要求的输入值
     */
    fun getText(): String {
        val currentText = editText.text()
        val resultText = when (numberCompare()) {
            -1 -> min.orEmpty()
            1 -> max.orEmpty()
            else -> currentText
        }
        if (resultText != currentText) {
            editText?.setText(resultText)
        }
        return resultText
    }

    /**
     * 获取当前输入值的范围状态
     * @return -1 表示小于最小值，0 表示正常，1 表示大于最大值
     */
    private fun numberCompare(): Int {
        val text = editText.text()
        if (!min.isNullOrEmpty() && text.numberCompareTo(min) == -1) {
            return -1
        }
        if (!max.isNullOrEmpty() && text.numberCompareTo(max) == 1) {
            return 1
        }
        return 0
    }

}

/**
 * 类银行卡4位插入一空格监听
 * mDesTxt:目标输入框
 * mOffset:偏移量(几位插入一空格)
 */
class NumSpaceTextWatcher @JvmOverloads constructor(private val view: WeakReference<EditText>?, private val mOffset: Int = DEFAULT_OFFSET) : TextWatcher {
    // 记录目标字符串
    private val mBuffer = StringBuffer()
    // 改变之前的文本长度
    private var mBeforeTextLength: Int = 0
    // 改变之后的文本长度
    private var mOnTextLength: Int = 0
    // 改变之前去除空格的文本长度
    private var mBeforeNumTxtLength: Int = 0
    // 改变之后去除空格的文本长度
    private var mNumTxtLength: Int = 0
    // 目标 光标的位置
    private var mLocation = 0
    // 之前 光标的位置(可判断用户是否做删除操作)
    private var mBeforeLocation = 0
    // 改变前有多少空格
    private var mBeforeSpaceNumber = 0
    // 是否选中空格覆盖
    private var isOverrideSpace: Boolean = false
    // 被覆盖的空格数
    private var mOverrideSpaceNum: Int = 0
    // 是否是粘贴(此粘贴非彼粘贴)
    private var isPaste: Boolean = false
    // 复制的字符数(不包括空格)
    private var mPasteNum: Int = 0
    // 是否需要进行格式化字符串操作
    private var isChanged = false
    // 获取输入框
    private val mDesTxt get() = view?.get()

    companion object {
        private const val DEFAULT_OFFSET = 4
    }

    init {
        if (mDesTxt?.inputType == InputType.TYPE_CLASS_NUMBER || mDesTxt?.inputType == InputType.TYPE_CLASS_PHONE || mDesTxt?.inputType == InputType.TYPE_CLASS_TEXT) {
            mDesTxt?.inputType = InputType.TYPE_CLASS_TEXT
            // 当InputType为Number时，手动设置我们的Listener
            mDesTxt?.keyListener = MyDigitsKeyListener()
        } else if (mDesTxt?.inputType != InputType.TYPE_CLASS_TEXT) {
            // 仅支持Text及Number类型的EditText
            throw IllegalArgumentException("EditText only support TEXT and NUMBER InputType！")
        }
    }

    override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
        mBeforeTextLength = s.length
        mBeforeNumTxtLength = s.toString().replace(" ", "").length
        mBeforeLocation = mDesTxt?.selectionEnd.orZero
        // 重置mBuffer
        if (mBuffer.isNotEmpty()) {
            mBuffer.delete(0, mBuffer.length)
        }
        // 计算改变前空格的个数
        mBeforeSpaceNumber = 0
        for (element in s) {
            if (element == ' ') {
                mBeforeSpaceNumber++
            }
        }
    }

    override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
        mOnTextLength = s.length
        mNumTxtLength = s.toString().replace(" ".toRegex(), "").length
        // 判断是否是粘贴,其中粘贴小于offset位的不做判断,并且offset>2判断才有意义
        if (mOffset in 2..count) {
            isPaste = true
            mPasteNum = count
        } else {
            isPaste = false
            mPasteNum = 0
        }
        // 若是经过afterTextChanged方法，则直接return
        if (isChanged) {
            isChanged = false
            return
        }
        // 若改变后长度小于等于mOffset - 1，则直接return
        if (mOnTextLength <= mOffset - 1) {
            isChanged = false
            return
        }
        // 若改变前后长度一致，并且数字位数相同，则isChanged为false
        // (数字位数相同是防止用户单选空格后输入数字)
        if (mBeforeTextLength == mOnTextLength && mBeforeNumTxtLength == mNumTxtLength) {
            isChanged = false
            return
        } else {
            isChanged = true
        }
        // 若要进行格式化，则判断该情况
        // 判断是否选中空格覆盖(排除删除空格的情况)
        isOverrideSpace = if (before == 1 && count == 0) {
            false
        } else {
            mBeforeTextLength - mBeforeSpaceNumber - before + count != mNumTxtLength
        }
        // 若是该情况，计算覆盖空格的个数
        mOverrideSpaceNum = if (isOverrideSpace) {
            mNumTxtLength - (mBeforeTextLength - mBeforeSpaceNumber - before + count)
        } else {
            0
        }
    }

    override fun afterTextChanged(s: Editable) {
        if (isChanged) {
            mLocation = mDesTxt?.selectionEnd.orZero
            // 去除空格
            mBuffer.append(s.toString().replace(" ", ""))
            // 格式化字符串，mOffset位加一个空格
            var index = 0
            var mAfterSpaceNumber = 0
            while (index < mBuffer.length) {
                if (index == mOffset * (1 + mAfterSpaceNumber) + mAfterSpaceNumber - 1) {
                    mBuffer.insert(index, ' ')
                    mAfterSpaceNumber++
                }
                index++
            }
            // 判断是否是粘贴键入
            if (isPaste) {
                mLocation += mPasteNum / mOffset
                isPaste = false
                // 判断是否是选中空格输入
            } else if (isOverrideSpace) {
                mLocation += mOverrideSpaceNum
                // 判断此时光标是否在特殊位置上
            } else if ((mLocation + 1) % (mOffset + 1) == 0) {
                // 是键入OR删除
                if (mBeforeLocation <= mLocation) {
                    mLocation++
                } else {
                    mLocation--
                }
            }
            // 若是删除数据刚好删除一位，前一位是空格，mLocation会超出格式化后字符串的长度(因为格
            // 式化后的长度没有不包括最后的空格)，将光标移到正确的位置
            val str = mBuffer.toString()
            mLocation = mLocation.fitRange(0..str.length)
            s.replace(0, s.length, str)
            val editable = mDesTxt?.text
            try {
                Selection.setSelection(editable, mLocation)
            } catch (e: Exception) {
                e.logE
            }
        }
    }

    // 继承DigitsKeyListener，实现我们自己的Listener
    private class MyDigitsKeyListener : DigitsKeyListener() {
        private val mAccepted = charArrayOf('0', '1', '2', '3', '4', '5', '6', '7', '8', '9', ' ')

        override fun getAcceptedChars(): CharArray {
            return mAccepted
        }
    }
}

/**
 * 数值限制筛选器
 */
class NumInputFilter(private val maxInteger: Int, private val maxDecimal: Int) : InputFilter {
    override fun filter(source: CharSequence?, start: Int, end: Int, dest: Spanned?, dstart: Int, dend: Int): CharSequence {
        if (source == "." && dest.toString().isEmpty()) {
            return if (maxDecimal == 0) {
                "0"
            } else {
                "0."
            }
        }
        if (dest.toString().contains(".")) {
            val index = dest.toString().indexOf(".")
            val length1 = dest.toString().substring(0, index).length
            val length2 = dest.toString().substring(index).length
            if (length1 >= maxInteger && dstart < index) {
                return ""
            }
            if (length2 >= maxDecimal + 1 && dstart > index) {
                return ""
            }
        } else {
            val length1 = dest.toString().length
            if (maxDecimal == 0 && source == ".") {
                return ""
            } else if ((length1 >= maxInteger) and (source != ".")) {
                return ""
            }
        }
        return source ?: ""
    }
}

/**
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
 *      android:textColor="@color/textPrimary"
 *      android:textColorHint="@color/textHint"
 *      android:textSize="30mm"
 *      android:textStyle="bold" />
 */
class DecimalInputFilter(private val decimalPoint: Int = 2) : InputFilter {
    private val point = "."
    private val zero = "0"
    private val maxValue by lazy { Int.MAX_VALUE } // 输入的最大金额

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
        val sourceText = source?.toString().orEmpty()
        val destText = dest?.toString().orEmpty()
        // 删除/回退键直接放行
        if (sourceText.isEmpty()) return null
        // 拼接输入后的完整文本（基于最终文本判断，而非光标位置）
        val newText = destText.substring(0, dstart) + sourceText + destText.substring(dend)
        // 拦截多个小数点（inputType允许输入多个，需要Filter拦截）
        if (newText.split(point).size > 2) {
            return ""
        }
        // 精准判断小数位数（修复你原代码的核心错误）
        if (newText.contains(point)) {
            val pointIndex = newText.indexOf(point)
            val decimalLength = newText.length - pointIndex - 1
            if (decimalLength > decimalPoint) {
                return ""
            }
        }
        // 0开头逻辑
        if (newText.startsWith(zero) && newText.length > 1) {
            // 0开头且第二位不是小数点，拦截（禁止0123，允许0.12）
            if (!newText.startsWith("$zero$point")) {
                return ""
            }
        }

        // 校验最大值（捕获异常，避免崩溃）
        return try {
            val value = newText.toDouble()
            if (value > maxValue) "" else null
        } catch (_: NumberFormatException) {
            // 处理123.、.12等非完整数字场景，暂时放行（后续输入会重新校验）
            null
        }
    }
}

/**
 * 空白字符拦截器（单独封装，便于管理和去重）
 */
class SpaceInputFilter : InputFilter {
    override fun filter(source: CharSequence?, start: Int, end: Int, dest: Spanned?, dstart: Int, dend: Int): CharSequence? {
        // 空输入（删除/回退）直接放行
        if (source.isNullOrEmpty()) return null
        // 过滤所有空白字符：半角空格、全角空格、制表符、换行、回车等
        val filtered = source.toString().replace(Regex("\\s"), "")
        // 如果过滤后为空，说明输入的全是空白字符，直接拦截
        return filtered.ifEmpty { "" }
    }
}
// </editor-fold>