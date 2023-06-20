package com.example.mvvm.widget

import android.content.Context
import android.util.AttributeSet
import com.example.framework.utils.function.inflate
import com.example.framework.widget.BaseViewGroup
import com.example.mvvm.R
import com.example.mvvm.databinding.ViewCounterEditBinding

/**
 * @description 计数器
 * @author yan
 */
class CounterEditText @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : BaseViewGroup(context, attrs, defStyleAttr) {
    private val binding by lazy { ViewCounterEditBinding.bind(context.inflate(R.layout.view_counter_edit)) }

    init {
//        binding.ivSubtract.click { calculate(false) }
//        binding.ivAdd.click { calculate(true) }
    }

    override fun onInflateView() {
        if (isInflate()) addView(binding.root)
    }

//    private fun calculate(isAdd: Boolean) {
//        if (getText().isNotEmpty()) {
//            if (isAdd) binding.etContent.add("1") else binding.etContent.subtract("1")
//        }
//    }
//
//    fun getText(): String {
//        return binding.etContent.text()
//    }

}

//fun CounterEditText?.text(): String {
//    this ?: return ""
//    return getText()
//}