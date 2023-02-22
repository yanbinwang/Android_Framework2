package com.example.mvvm.widget.automatic.holder

import android.content.Context
import android.widget.EditText
import android.widget.TextView
import com.example.common.databinding.ViewClearEditBinding
import com.example.framework.utils.function.inflate
import com.example.mvvm.R
import com.example.mvvm.databinding.ViewNormalEditBinding
import com.example.mvvm.widget.automatic.AutomaticBean
import kotlin.LazyThreadSafetyMode.NONE

/**
 * @description 自动绘制输入框
 * @author yan
 */
class NormalEditHolder(context: Context, private val bean: AutomaticBean) : AutomaticInterface {
    private val binding by lazy { ViewNormalEditBinding.bind(context.inflate(R.layout.view_normal_edit)) }

    init {
        binding.tvLabel.text = bean.label
    }

    override fun getBean() = bean

    override fun getValue() = binding.etContent.text.toString()

    override fun getCheckValue() = true

    override fun getView() = binding.root

}