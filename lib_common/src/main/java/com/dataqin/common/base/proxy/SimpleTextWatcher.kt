package com.dataqin.common.base.proxy

import android.text.Editable
import android.text.TextWatcher

/**
 * Created by WangYanBin on 2020/6/15.
 */
abstract class SimpleTextWatcher : TextWatcher {

    override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
    }

    override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
    }

    override fun afterTextChanged(s: Editable) {
    }

}