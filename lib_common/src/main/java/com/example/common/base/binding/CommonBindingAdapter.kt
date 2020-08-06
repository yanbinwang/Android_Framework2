package com.example.common.base.binding

import android.text.SpannableStringBuilder
import android.text.Spanned
import android.view.View
import android.widget.TextView
import androidx.databinding.BindingAdapter

/**
 * Created by WangYanBin on 2020/6/10.
 * 基础全局xml绑定
 */
object CommonBindingAdapter {

    @JvmStatic
    @BindingAdapter(value = ["adjustWidth"])
    fun setAdjustWidth(view: View, adjustWidth: Int) {
        val params = view.layoutParams
        params.width = adjustWidth
        view.layoutParams = params
    }

    @JvmStatic
    @BindingAdapter(value = ["app:adjustHeight"])
    fun setAdjustHeight(view: View, adjustHeight: Int) {
        val params = view.layoutParams
        params.height = adjustHeight
        view.layoutParams = params
    }

    @JvmStatic
    @BindingAdapter(value = ["app:spannableSize", "app:spannableStart", "app:spannableEnd"])
    fun setSpannable(view: TextView, spannableSize: Int, spannableStart: Int, spannableEnd: Int) {
        val spannableStringBuilder = SpannableStringBuilder()
        spannableStringBuilder.append(view.text)
        spannableStringBuilder.setSpan(
            spannableSize,
            spannableStart,
            spannableEnd,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        view.text = spannableStringBuilder
    }

}