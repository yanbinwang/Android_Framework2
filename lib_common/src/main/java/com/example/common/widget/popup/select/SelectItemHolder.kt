package com.example.common.widget.popup.select

import android.view.View
import android.view.ViewGroup
import androidx.annotation.ColorRes
import com.example.common.R
import com.example.common.databinding.ItemPopupSelectBinding
import com.example.common.utils.function.orNoData
import com.example.framework.utils.function.inflate
import com.example.framework.utils.function.view.background
import com.example.framework.utils.function.view.click

/**
 * @item -> 文案
 * @index -> 下标
 */
class SelectItemHolder(parent: ViewGroup, item: String?, index: Int, @ColorRes bgColor: Int = R.color.bgDefault) {
    internal val binding by lazy { ItemPopupSelectBinding.bind(parent.context.inflate(R.layout.item_popup_select)) }
    internal var onItemClick: ((item: String?, index: Int) -> Unit)? = null

    init {
        val txt = item.orNoData()
        binding.tvLabel.text = txt
        binding.tvLabel.background(bgColor)
        binding.root.click {
            onItemClick?.invoke(txt, index)
        }
    }

    fun getRoot(): View {
        return binding.root
    }

}