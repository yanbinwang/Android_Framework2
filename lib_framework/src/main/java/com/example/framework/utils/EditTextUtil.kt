package com.example.framework.utils

import android.text.Editable
import android.text.InputFilter
import android.text.InputType
import android.text.Selection
import android.text.Spanned
import android.text.TextUtils
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
import com.example.framework.utils.function.view.decimalFilter
import com.example.framework.utils.function.view.text
import java.lang.ref.WeakReference
import java.util.regex.Pattern

// <editor-fold defaultstate="collapsed" desc="工具类方法">
object EditTextUtil {
    private var chineseParam: CharArray = charArrayOf(
        '」', '，', '。', '？', '…', '：', '～', '【', '＃', '、', '％',
        '＊', '＆', '＄', '（', '‘', '’', '“', '”', '『', '〔', '｛',
        '【', '￥', '￡', '‖', '〖', '《', '「', '》', '〗', '】', '｝',
        '〕', '』', '”', '）', '！', '；', '—')

    /**
     * 设置文本框自动四位加一个空格
     * 需要注意的是getText后会获取到空格，需要手动用replace替换掉
     * 方法内已经自带设定最大输入位数以及输入类型的设定，可以不在xml中设定这些内容
     */
    fun setPhoneNumSpace(target: EditText) {
        val filters = arrayOf<InputFilter>(InputFilter.LengthFilter(13))
        target.filters = filters
        target.inputType = InputType.TYPE_CLASS_PHONE
        target.addTextChangedListener(NumSpaceTextWatcher(target))
    }

    /**
     * 限制输入内容为目标值
     * "0123456789."
     */
    fun setCharLimit(target: EditText, characterAllowed: CharArray) {
        target.addFilter(getCharLimitFilter(characterAllowed))
    }

    /**
     * 限制输入内容为非目标值
     */
    fun setCharBlackList(target: EditText, characterAllowed: CharArray) {
        target.addFilter(getCharBlackListFilter(characterAllowed))
    }

    /**
     * 限制输入内容为目标值
     */
    fun getCharLimitFilter(characterAllowed: String): InputFilter {
        return getCharLimitFilter(characterAllowed.toCharArray())
    }

    /**
     * 限制输入内容为目标值
     */
    fun getCharBlackListFilter(characterBlackList: String): InputFilter {
        return getCharBlackListFilter(characterBlackList.toCharArray())
    }

    /**
     * 限制输入内容为目标值
     */
    fun getCharLimitFilter(characterAllowed: CharArray): InputFilter {
        return InputFilter { source, start, end, _, _, _ ->
            var flag = true
            val sb = StringBuilder()
            for (i in start until end) {
                if (!characterAllowed.contains(source[i])) {
                    flag = false
                } else {
                    sb.append(source[i])
                }
            }
            if (flag) {
                null
            } else {
                sb.toString()
            }
        }
    }

    /**
     * 限制输入内容为目标值
     */
    fun getCharBlackListFilter(characterBlackList: CharArray): InputFilter {
        return InputFilter { source, start, end, _, _, _ ->
            var flag = true
            val sb = StringBuilder()
            for (i in start until end) {
                if (characterBlackList.contains(source[i])) {
                    flag = false
                } else {
                    sb.append(source[i])
                }
            }
            if (flag) {
                null
            } else {
                sb.toString()
            }
        }
    }

    /**
     * 限制输入框输入emoji
     */
    fun setEmojiLimit(target: EditText) {
        target.addFilter(object : InputFilter {
            override fun filter(source: CharSequence, start: Int, end: Int, dest: Spanned, dstart: Int, dend: Int, ): CharSequence? {
                for (i in start until end) {
                    if (!isEmojiCharacter(source[i])) {
                        return ""
                    }
                }
                return null
            }
        })
    }

    /**
     * 判断是否是Emoji
     */
    fun isEmojiCharacter(codePoint: Char): Boolean {
        return codePoint.code == 0x0 || codePoint.code == 0x9 || codePoint.code == 0xA || codePoint.code == 0xD || codePoint.code in 0x20..0xD7FF || codePoint.code in 0xE000..0xFFFD || codePoint.code in 0x10000..0x10FFFF
    }

    /**
     * 限制只能输入中文和英文数字和符号
     */
    fun setChineseLimit(target: EditText) {
        target.addFilter(object : InputFilter {
            override fun filter(source: CharSequence, start: Int, end: Int, dest: Spanned, dstart: Int, dend: Int, ): CharSequence? {
                for (i in start until end) {
                    if (!isChinese(source[i]) && !Character.isLetterOrDigit(source[i]) && source[i].toString() != "_") {
                        return ""
                    }
                }
                return null
            }
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
     * 设置EditText输入数值的最大值
     */
    fun getNumInputFilter(maxInteger: Int, maxDecimal: Int): InputFilter {
        return NumInputFilter(maxInteger, maxDecimal)
    }

    /**
     * 设置输出格式
     */
    fun setInputType(target: EditText?, inputType: Int) = target?.execute {
        when (inputType) {
            //text
            0 -> setInputType(InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_NORMAL)
            //textPassword
            1 -> setInputType(InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD)
            //phone
            2 -> setInputType(InputType.TYPE_CLASS_PHONE)
            //number
            3 -> setInputType(InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_VARIATION_NORMAL)
            //rate,numberDecimal
            9, 4 -> setInputType(InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL)
            //email
            5 -> setInputType(InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS)
            //idcard,textVisiblePassword
            8, 6 -> setInputType(InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD)
            //money
            7 -> setInputType(InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL or InputType.TYPE_NUMBER_FLAG_SIGNED)
            //text
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
private class NumInputFilter(private val maxInteger: Int, private val maxDecimal: Int) : InputFilter {
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
 * 类银行卡4位插入一空格监听
 * mDesTxt:目标输入框
 * mOffset:偏移量(几位插入一空格)
 */
private class NumSpaceTextWatcher @JvmOverloads constructor(private val mDesTxt: EditText, private val mOffset: Int = DEFAULT_OFFSET) : TextWatcher {
    companion object {
        private const val DEFAULT_OFFSET = 4
    }

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

    init {
        if (mDesTxt.inputType == InputType.TYPE_CLASS_NUMBER || mDesTxt.inputType == InputType.TYPE_CLASS_PHONE || mDesTxt.inputType == InputType.TYPE_CLASS_TEXT) {
            mDesTxt.inputType = InputType.TYPE_CLASS_TEXT
            // 当InputType为Number时，手动设置我们的Listener
            mDesTxt.keyListener = MyDigitsKeyListener()
        } else if (mDesTxt.inputType != InputType.TYPE_CLASS_TEXT) {
            // 仅支持Text及Number类型的EditText
            throw IllegalArgumentException("EditText only support TEXT and NUMBER InputType！")
        }
    }

    override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
        mBeforeTextLength = s.length
        mBeforeNumTxtLength = s.toString().replace(" ", "").length
        mBeforeLocation = mDesTxt.selectionEnd
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
            mLocation = mDesTxt.selectionEnd
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
            val editable = mDesTxt.text
            try {
                Selection.setSelection(editable, mLocation)
            } catch (e: Exception) {
                e.logE
            }
        }
    }

    // 继承DigitsKeyListener，实现我们自己的Listener
    private inner class MyDigitsKeyListener : DigitsKeyListener() {
        private val mAccepted = charArrayOf('0', '1', '2', '3', '4', '5', '6', '7', '8', '9', ' ')

        override fun getAcceptedChars(): CharArray {
            return mAccepted
        }
    }
}

/**
 * EditText添加文字限制的时候使用此TextWatcher,
 * 提供回调，有部分界面使用到判断
 * maxLength:最大长度，ASCII码算一个，其它算两个
 */
private class TextLengthFilter(private val maxLength: Int) : InputFilter {
    companion object {
        fun getCurLength(s: CharSequence?): Int {
            var length = 0
            if (s == null) return length else {
                for (element in s) {
                    length += if (element.toInt() < 128) 1 else 2
                }
            }
            return length
        }
    }

    override fun filter(source: CharSequence, start: Int, end: Int, dest: Spanned, dstart: Int, dend: Int): CharSequence? {
        if (TextUtils.isEmpty(source)) {
            return null
        }
        var destCount = 0
        val inputCount = getCurLength(source)
        if (dest.isNotEmpty()) destCount = getCurLength(dest)
        if (destCount >= maxLength) return "" else {
            val count = inputCount + destCount
            if (dest.isEmpty()) {
                return if (count <= maxLength) null else sub(source, maxLength)
            }
            if (count > maxLength) {
                //int min = count - maxLength;
                val maxSubLength = maxLength - destCount
                return sub(source, maxSubLength)
            }
        }
        return null
    }

    private fun sub(sq: CharSequence, subLength: Int): CharSequence {
        var needLength = 0
        var length = 0
        for (element in sq) {
            length += if (element.toInt() < 128) 1 else 2
            ++needLength
            if (subLength <= length) {
                return sq.subSequence(0, needLength)
            }
        }
        return sq
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
class DecimalInputFilter : InputFilter {
    private val mPattern by lazy { Pattern.compile("([0-9]|\\.)*") }
    private val maxValue by lazy { Int.MAX_VALUE }//输入的最大金额
    private val point = "."
    private val zero = "0"
    var decimalPoint = 2 //小数点后的位数

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
        val sourceText = source.toString()
        val destText = dest.toString()
        //验证删除等按键
        if (sourceText.isEmpty()) return ""
        val matcher = mPattern.matcher(source ?: "")
        //已经输入小数点的情况下，只能输入数字
        if (destText.contains(point)) {
            if (!matcher.matches()) {
                return ""
                //只能输入一个小数点
            } else if (point == source.toString()) {
                return ""
            }
            //验证小数点精度，保证小数点后只能输入两位
            val index = destText.indexOf(point)
            val length = dend - index
            if (length > decimalPoint) return dest?.subSequence(dstart, dend)
        } else {
            /**
             * 没有输入小数点的情况下，只能输入小数点和数字
             * 1. 首位不能输入小数点
             * 2. 如果首位输入0，则接下来只能输入小数点了
             */
            if (!matcher.matches()) {
                return ""
            } else {
                //首位不能输入小数点
                if (point == source.toString() && destText.isEmpty()) {
                    return ""
                    //如果首位输入0，接下来只能输入小数点
                } else if (point != source.toString() && zero == destText) {
                    return ""
                }
            }
        }
        //验证输入金额的大小
        if ((destText + sourceText).toDouble() > maxValue) return dest?.subSequence(dstart, dend)
        return dest?.subSequence(dstart, dend).toString() + sourceText
    }
}

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
            if (!hasFocus) {
                if (hasAuto) getText()
            }
        }
    }

    /**
     * 传入字符串避免小数位数比较的误差
     */
    fun setRange(min: String? = null, max: String? = null, digits: Int? = -1) {
        this.min = min
        this.max = max
        if (digits != -1) editText?.decimalFilter(digits.orZero)
    }

    /**
     * -1,表示小于最小值
     * 0,表示正常
     * 1,表示大于最大值
     */
    fun getRange(): Int {
        var range = 0
        val text = editText.text()
        if (!min.isNullOrEmpty()) {
            if (text.numberCompareTo(min.orEmpty()) == -1) {
                range = -1
            }
        }
        if (!max.isNullOrEmpty()) {
            if (text.numberCompareTo(max.orEmpty()) == 1) {
                range = 1
            }
        }
        return range
    }

    /**
     * 取值使用此方法
     */
    fun getText(): String {
        val range = getRange()
        var text = editText.text()
        if (-1 == range) {
            text = min.orEmpty()
        }
        if (1 == range) {
            text = max.orEmpty()
        }
        //比较一下大小值有无改变
        if (text != editText.text()) editText?.setText(text)
        return text
    }

}

///**
// * 用于限制输入框在输入时候的小数位数
// * 禁止用户在打入小数后超过设定的位数，会自动回滚之前的值
// */
//class DecimalHelper(private val view: WeakReference<EditText>?) {
//    private var listener: ((text: String) -> Unit)? = null
//    private val editText get() = view?.get()
//    private val watcher by lazy { object : DecimalTextWatcher(view) {
//        override fun onEmpty() {
//            "值为空".logWTF
//            editText?.setText("")
//        }
//
//        override fun onOverstep(before: String?, cursor: Int?) {
//            "值超过小数位".logWTF
//            editText?.setText(before.orEmpty())
//            editText.setSafeSelection(cursor.orZero)
//        }
//
//        override fun onChanged(text: String) {
//            "输入合法".logWTF
//            listener?.invoke(text)
//        }
//    }}
//
//    init {
//        //可在xml中实现输入限制
//        EditTextUtil.setInputType(editText, 7)
//        //添加监听
//        editText?.addTextChangedListener(watcher)
//    }
//
//    /**
//     * 设置小数位数限制
//     */
//    fun setDigits(digits: Int) {
//        watcher.setDigits(digits)
//    }
//
//    /**
//     * 设置回调监听
//     */
//    fun setOnChangedListener(listener: ((text: String) -> Unit)) {
//        this.listener = listener
//    }
//
//}
//
///**
// * 对应输入框限制输入的监听
// * 注：如果是输入范围限制，只能做最大值的限制
// */
//private abstract class DecimalTextWatcher(private val view: WeakReference<EditText>?) : TextWatcher {
//    private var digits = 0
//    private var textCursor = 0//用于记录变化时光标的位置
//    private var textBefore: String? = null//用于记录变化前的文字
//    private val editText get() = view?.get()
//
//    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
//        textBefore = s.toString()
//        textCursor = start
//    }
//
//    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
//    }
//
//    override fun afterTextChanged(s: Editable?) {
//        val text = s.toString()
//        if (text.isEmpty()) {
//            editText?.removeTextChangedListener(this)
//            onEmpty()
//            editText?.addTextChangedListener(this)
//            return
//        }
//        if (text.numberDigits() > digits) {
//            editText?.removeTextChangedListener(this)
//            onOverstep(textBefore, textCursor)
//            editText?.addTextChangedListener(this)
//            return
//        }
//        onChanged(text)
//    }
//
//    /**
//     * 设置小数位数限制
//     */
//    fun setDigits(digits: Int) {
//        this.digits = digits
//    }
//
//    abstract fun onEmpty()
//
//    abstract fun onOverstep(before: String?, cursor: Int?)
//
//    abstract fun onChanged(text: String)
//
//}
// </editor-fold>