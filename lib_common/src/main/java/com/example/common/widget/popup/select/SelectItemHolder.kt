package com.example.common.widget.popup.select

import android.view.ViewGroup
import com.example.common.R
import com.example.common.databinding.ItemPopupSelectLabelBinding
import com.example.framework.utils.function.inflate
import com.example.framework.utils.function.view.click

/**
 * first->文案
 * second->下标
 */
class SelectItemHolder(parent: ViewGroup, triple: Pair<String, Int>) {
    internal var onItemClick: ((item: String?, index: Int) -> Unit)? = { _, _ -> }
    internal val binding by lazy { ItemPopupSelectLabelBinding.bind(parent.context.inflate(R.layout.item_popup_select_label)) }

    init {
        binding.tvLabel.text = triple.first
        binding.root.click { onItemClick?.invoke(triple.first, triple.second) }
    }

}