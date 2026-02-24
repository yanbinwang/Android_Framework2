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
import com.example.framework.utils.EditTextUtil.isChinese
import com.example.framework.utils.EditTextUtil.isNonEmojiCharacter
import com.example.framework.utils.function.doOnDestroy
import com.example.framework.utils.function.value.execute
import com.example.framework.utils.function.value.numberCompareTo
import com.example.framework.utils.function.value.orZero
import com.example.framework.utils.function.view.addFilter
import com.example.framework.utils.function.view.blackListLimit
import com.example.framework.utils.function.view.decimalLimit
import com.example.framework.utils.function.view.getLifecycleOwner
import com.example.framework.utils.function.view.removeFilter
import com.example.framework.utils.function.view.text
import com.example.framework.utils.function.view.whiteListLimit
import java.lang.ref.WeakReference

// <editor-fold defaultstate="collapsed" desc="工具类方法">
object EditTextUtil {

    /**
     * 设置文本框自动四位加一个空格（手机号格式）
     * 注意：getText会包含空格，需手动replace(" ", "")
     */
    fun setPhoneSpace(target: EditText) {
        // 移除所有长度限制Filter（避免重复/冲突）
        target.removeFilter { it is InputFilter.LengthFilter }
        // 13位长度限制（手机号纯数字最大13位）
        target.addFilter(InputFilter.LengthFilter(13))
        // 设置输入类型（优先用TYPE_CLASS_PHONE，适配手机号键盘）
        target.inputType = InputType.TYPE_CLASS_PHONE
        // 创建并添加空格格式化监听（弱引用+生命周期管理）
        val watcher = SpaceTextWatcher(WeakReference(target))
        target.addTextChangedListener(watcher)
        // 生命周期绑定：页面销毁时移除监听
        target.getLifecycleOwner()?.lifecycle?.doOnDestroy {
            // 避免空指针 + 重复移除
            if (target.isAttachedToWindow) {
                target.removeTextChangedListener(watcher)
            }
        }
    }

    /**
     * 限制输入内容仅为指定字符（白名单）
     * @param target 目标EditText
     * @param allowed 允许输入的字符数组（如："0123456789.".toCharArray()）
     */
    fun setWhiteListLimit(target: EditText, allowed: String) {
        target.whiteListLimit(allowed.toCharArray())
    }

    /**
     * 限制输入内容排除指定字符（黑名单）
     * @param target 目标EditText
     * @param disallowed 禁止输入的字符数组
     */
    fun setBlackListLimit(target: EditText, disallowed: String) {
        target.blackListLimit(disallowed.toCharArray())
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
     * 判定输入汉字是否是中文
     */
    private var chineseParam = charArrayOf(
        '」', '，', '。', '？', '…', '：', '～', '【', '＃', '、', '％',
        '＊', '＆', '＄', '（', '‘', '’', '“', '”', '『', '〔', '｛',
        '【', '￥', '￡', '‖', '〖', '《', '「', '》', '〗', '】', '｝',
        '〕', '』', '”', '）', '！', '；', '—')

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
 * 用于输入框失去焦点时自动根据输入框内的值吸附最大最小值(处在范围内的值不会改变/默认大小和小数位数不做限制)
 */
class RangeLimit(private val view: WeakReference<EditText>?, hasAuto: Boolean = true) {
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
        if (digits != -1) {
            editText?.decimalLimit(digits.orZero)
        }
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
 * @param mDesTxt 目标输入框
 * @param mOffset 偏移量(几位插入一空格)，默认4位
 */
class SpaceTextWatcher @JvmOverloads constructor(private val view: WeakReference<EditText>?, private val mOffset: Int = DEFAULT_OFFSET) : TextWatcher {
    // 获取输入框
    private val mDesTxt get() = view?.get()
    // 改变之前的文本长度
    private var mBeforeTextLength = 0
    // 改变之后的文本长度
    private var mOnTextLength = 0
    // 改变之前去除空格的文本长度
    private var mBeforeNumTxtLength = 0
    // 改变之后去除空格的文本长度
    private var mNumTxtLength = 0
    // 目标光标的位置
    private var mLocation = 0
    // 之前光标的位置(可判断用户是否做删除操作)
    private var mBeforeLocation = 0
    // 改变前有多少空格
    private var mBeforeSpaceNum = 0
    // 被覆盖的空格数
    private var mOverrideSpaceNum = 0
    // 复制的字符数(不包括空格)
    private var mPasteNum = 0
    // 记录目标字符串
    private val mBuffer = StringBuffer()
    // 是否选中空格覆盖
    private var isOverrideSpace = false
    // 是否是粘贴(此粘贴非彼粘贴)
    private var isPaste = false
    // 是否需要进行格式化字符串操作
    private var isChanged = false

    companion object {
        // 默认4位插入空格
        private const val DEFAULT_OFFSET = 4
        // 空格字符常量
        private const val SPACE_CHAR = '\u0020'
        // 所有需要过滤/替换的空格类型（覆盖常见空格）
        private val ALL_SPACE_CHARS = charArrayOf(
            '\u0020', // 半角空格
            '\u3000', // 全角空格（中文空格）
            '\u00A0', // 不换行空格
            '\u2000', // 中文全角空格变体
            '\u2001', // 中文全角空格变体
            '\u2002', // 中文全角空格变体
            '\u2003', // 中文全角空格变体
            '\t'      // 制表符（也视为空格）
        )
    }

    init {
        if (mDesTxt?.inputType == InputType.TYPE_CLASS_NUMBER || mDesTxt?.inputType == InputType.TYPE_CLASS_PHONE || mDesTxt?.inputType == InputType.TYPE_CLASS_TEXT) {
            mDesTxt?.inputType = InputType.TYPE_CLASS_TEXT
            // 当InputType为Number时，手动设置我们的Listener
            mDesTxt?.keyListener = object : DigitsKeyListener() {
                private val mAccepted = charArrayOf('0', '1', '2', '3', '4', '5', '6', '7', '8', '9', SPACE_CHAR)

                override fun getAcceptedChars(): CharArray {
                    return mAccepted
                }
            }
        } else if (mDesTxt?.inputType != InputType.TYPE_CLASS_TEXT) {
            // 仅支持Text及Number类型的EditText
            throw IllegalArgumentException("EditText only support TEXT and NUMBER InputType！")
        }
    }

    override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
        mBeforeTextLength = s.length
        // 移除所有类型空格，计算纯数字长度
        mBeforeNumTxtLength = removeAllSpaces(s.toString()).length
        // 空安全：selectionEnd为空时默认0
        mBeforeLocation = mDesTxt?.selectionEnd.orZero
        // 重置缓冲区，避免残留数据
        if (mBuffer.isNotEmpty()) {
            mBuffer.delete(0, mBuffer.length)
        }
        // 统计所有类型空格的数量
        mBeforeSpaceNum = countAllSpaces(s)
    }

    override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
        mOnTextLength = s.length
        // 移除所有类型空格，计算纯数字长度
        mNumTxtLength = removeAllSpaces(s.toString()).length
        // 判断是否为粘贴操作（批量输入≥2位且offset≥2）
        isPaste = mOffset in 2..count
        mPasteNum = if (isPaste) count else 0
        // 无需格式化的场景：已触发过格式化/文本过短/无有效变化
        isChanged = when {
            isChanged -> {
                isChanged = false
                false
            }
            mOnTextLength <= mOffset - 1 -> false
            mBeforeTextLength == mOnTextLength && mBeforeNumTxtLength == mNumTxtLength -> false
            else -> true
        }
        // 计算是否选中空格覆盖及覆盖数量
        isOverrideSpace = !(before == 1 && count == 0) && (mBeforeTextLength - mBeforeSpaceNum - before + count != mNumTxtLength)
        mOverrideSpaceNum = if (isOverrideSpace) {
            mNumTxtLength - (mBeforeTextLength - mBeforeSpaceNum - before + count)
        } else {
            0
        }
    }

    override fun afterTextChanged(s: Editable) {
        if (!isChanged || mDesTxt == null) return
        // 记录光标位置（空安全兜底）
        mLocation = mDesTxt!!.selectionEnd ?: 0
        // 移除所有类型的空格，只保留数字
        mBuffer.append(removeAllSpaces(s.toString()))
        // 按offset位数插入【标准半角空格】格式化
        var index = 0
        var mAfterSpaceNumber = 0
        while (index < mBuffer.length) {
            val spacePosition = mOffset * (1 + mAfterSpaceNumber) + mAfterSpaceNumber - 1
            if (index == spacePosition) {
                // 只插入标准半角空格
                mBuffer.insert(index, SPACE_CHAR)
                mAfterSpaceNumber++
            }
            index++
        }
        // 修正光标位置（适配粘贴/空格覆盖/删除操作）
        when {
            isPaste -> {
                mLocation += mPasteNum / mOffset
                isPaste = false
            }
            isOverrideSpace -> mLocation += mOverrideSpaceNum
            (mLocation + 1) % (mOffset + 1) == 0 -> {
                mLocation += if (mBeforeLocation <= mLocation) 1 else -1
            }
        }
        // 替换文本并设置光标（防越界+空安全）
        val str = mBuffer.toString()
        mLocation = mLocation.coerceIn(0, str.length)
        s.replace(0, s.length, str)
        try {
            Selection.setSelection(mDesTxt!!.text, mLocation)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        // 重置状态，避免下次处理污染
        resetState()
    }

    /**
     * 移除字符串中所有类型的空格（半角/全角/制表符等）
     */
    private fun removeAllSpaces(str: String): String {
        return str.replace(Regex("[" + ALL_SPACE_CHARS.concatToString() + "]"), "")
    }

    /**
     * 统计字符串中所有类型的空格数量
     */
    private fun countAllSpaces(s: CharSequence): Int {
        var count = 0
        for (c in s) {
            if (c in ALL_SPACE_CHARS) {
                count++
            }
        }
        return count
    }

    /**
     * 重置所有状态变量，避免多次操作后状态残留
     */
    private fun resetState() {
        mBeforeTextLength = 0
        mOnTextLength = 0
        mBeforeNumTxtLength = 0
        mNumTxtLength = 0
        mLocation = 0
        mBeforeLocation = 0
        mBeforeSpaceNum = 0
        mOverrideSpaceNum = 0
        mPasteNum = 0
        isOverrideSpace = false
        isPaste = false
        isChanged = false
        mBuffer.delete(0, mBuffer.length)
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

/**
 * 字符白名单输入过滤器（可精准判断重复，避免重复添加）
 * @param allowed 允许输入的字符数组（比如数字+空格）
 */
class WhiteListFilter(private val allowed: CharArray) : InputFilter {
    override fun filter(source: CharSequence?, start: Int, end: Int, dest: Spanned?, dstart: Int, dend: Int): CharSequence? {
        // 空安全兜底：source为null直接视为空字符串，避免后续操作NPE
        val safeSource = source ?: return null  // source=null等价于空输入，直接放行
        val sb = StringBuilder()
        // 先限定遍历的上下界，避免越界
        // 确保start≥0
        val actualStart = start.coerceAtLeast(0)
        // 确保end≤source长度
        val actualEnd = end.coerceAtMost(safeSource.length)
        // 遍历有效范围的字符
        for (i in actualStart until actualEnd) {
            // 此时i一定在[0, safeSource.length)范围内，无越界风险
            val char = safeSource[i]
            // 只保留白名单内的字符
            if (allowed.contains(char)) {
                sb.append(char)
            }
        }
        // 筛选结果处理：空则返回""（禁止输入），非空返回筛选结果
        return sb.toString().takeIf { it.isNotEmpty() } ?: ""
    }
}

/**
 * 字符黑名单输入过滤器（精准去重+空安全+索引安全）
 * @param disallowed 禁止输入的字符数组
 */
class BlackListFilter(private val disallowed: CharArray) : InputFilter {
    override fun filter(source: CharSequence?, start: Int, end: Int, dest: Spanned?, dstart: Int, dend: Int): CharSequence? {
        // 空安全兜底：source为null直接视为空输入，放行（删除/回退操作）
        val safeSource = source ?: return null
        val sb = StringBuilder()
        // 限定遍历范围，避免越界/负数
        // 起始索引≥0
        val actualStart = start.coerceAtLeast(0)
        // 结束索引≤source长度
        val actualEnd = end.coerceAtMost(safeSource.length)
        // 遍历有效范围，排除黑名单字符
        for (i in actualStart until actualEnd) {
            // 索引绝对安全，无越界风险
            val char = safeSource[i]
            // 只保留「不在黑名单」的字符
            if (!disallowed.contains(char)) {
                sb.append(char)
            }
        }
        // 结果处理：空则返回""（禁止输入），非空返回筛选结果
        return sb.toString().takeIf { it.isNotEmpty() } ?: ""
    }
}

/**
 * 禁止输入Emoji过滤器（精准去重+空安全+索引安全）
 */
class NoEmojiFilter : InputFilter {
    override fun filter(source: CharSequence?, start: Int, end: Int, dest: Spanned?, dstart: Int, dend: Int): CharSequence? {
        // 空安全兜底：source为null直接放行（删除/回退操作）
        val safeSource = source ?: return null
        val sb = StringBuilder()
        // 索引安全：限定遍历范围
        val actualStart = start.coerceAtLeast(0)
        val actualEnd = end.coerceAtMost(safeSource.length)
        // 遍历有效范围，只保留非Emoji字符
        for (i in actualStart until actualEnd) {
            val char = safeSource[i]
            if (isNonEmojiCharacter(char)) {
                sb.append(char)
            }
        }
        // 结果处理：空则返回""（禁止输入），非空返回筛选结果
        return sb.toString().takeIf { it.isNotEmpty() } ?: ""
    }
}

/**
 * 仅允许输入中文/字母/数字/下划线过滤器（精准去重+空安全+索引安全）
 */
class ChineseCharFilter : InputFilter {
    override fun filter(source: CharSequence?, start: Int, end: Int, dest: Spanned?, dstart: Int, dend: Int): CharSequence? {
        // 空安全兜底：source为null直接放行（删除/回退操作）
        val safeSource = source ?: return null
        val sb = StringBuilder()
        // 索引安全：限定遍历范围，避免越界/负数
        val actualStart = start.coerceAtLeast(0)
        val actualEnd = end.coerceAtMost(safeSource.length)
        // 遍历有效范围，保留合法字符（和你原有逻辑完全一致）
        for (i in actualStart until actualEnd) {
            val char = safeSource[i]
            // 保留合法字符：中文、字母、数字、下划线（和你原逻辑一模一样）
            if (isChinese(char) || Character.isLetterOrDigit(char) || char == '_') {
                sb.append(char)
            }
        }
        // 结果处理：空则返回""（禁止输入），非空返回筛选结果
        return sb.toString().takeIf { it.isNotEmpty() } ?: ""
    }
}

/**
 * 数值限制筛选器
 */
class NumberLimitFilter(private val maxInteger: Int, private val maxDecimal: Int) : InputFilter {
    override fun filter(source: CharSequence?, start: Int, end: Int, dest: Spanned?, dstart: Int, dend: Int): CharSequence {
        val safeSource = source ?: "" // 空安全兜底
        if (safeSource == "." && dest.toString().isEmpty()) {
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
        return safeSource
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
// </editor-fold>