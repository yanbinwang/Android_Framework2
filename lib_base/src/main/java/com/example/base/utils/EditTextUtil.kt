package com.example.base.utils

import android.text.*
import android.text.method.DigitsKeyListener
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import com.example.base.utils.function.value.fitRange
import com.example.base.utils.function.view.addFilter

object EditTextUtil {

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
    private fun isEmojiCharacter(codePoint: Char): Boolean {
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

    private var chineseParam: CharArray = charArrayOf(
        '」', '，', '。', '？', '…', '：', '～', '【', '＃', '、', '％',
        '＊', '＆', '＄', '（', '‘', '’', '“', '”', '『', '〔', '｛',
        '【', '￥', '￡', '‖', '〖', '《', '「', '》', '〗', '】', '｝',
        '〕', '』', '”', '）', '！', '；', '—')

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
    fun setInputType(target: EditText, inputType: Int) {
        with(target) {
            when (inputType) {
                0 -> setInputType(InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_NORMAL)
                1 -> setInputType(InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD)
                2 -> setInputType(InputType.TYPE_CLASS_PHONE)
                3 -> setInputType(InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_VARIATION_NORMAL)
                9, 4 -> setInputType(InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL)
                5 -> setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS)
                8, 6 -> setInputType(InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD)
                7 -> {
                    setInputType(InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL)
                    addFilter(object : InputFilter {
                        override fun filter(source: CharSequence?, start: Int, end: Int, dest: Spanned?, dstart: Int, dend: Int): CharSequence {
                            if (source == "." && dest.toString().isEmpty()) {
                                return "0."
                            }
                            if (dest.toString().contains(".")) {
                                val index = dest.toString().indexOf(".")
                                val length1 = dest.toString().substring(0, index).length
                                val length2 = dest.toString().substring(index).length
                                if (length1 >= 8 && dstart < index) {
                                    return ""
                                }
                                if (length2 >= 3 && dstart > index) {
                                    return ""
                                }
                            } else {
                                val length1 = dest.toString().length
                                if (length1 >= 8 && source != ".") {
                                    return ""
                                }
                            }
                            return ""
                        }
                    })
                }
                else -> setInputType(InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_NORMAL)
            }
        }
    }

    /**
     * 设置按键格式
     */
    fun setImeOptions(target: EditText, imeOptions: Int) {
        with(target) {
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

}

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
                e.toString().logE
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