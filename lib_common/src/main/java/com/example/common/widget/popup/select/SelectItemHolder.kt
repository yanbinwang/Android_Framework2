package com.example.common.widget.popup.select

import android.view.View
import android.view.ViewGroup
import com.example.common.R
import com.example.common.databinding.ItemPopupSelectBinding
import com.example.common.utils.function.orNoData
import com.example.common.utils.i18n.string
import com.example.framework.utils.function.inflate
import com.example.framework.utils.function.view.click

/**
 * @item -> 文案
 * @index -> 下标
 */
class SelectItemHolder(parent: ViewGroup, item: Any?, index: Int) {
    internal val mBinding by lazy { ItemPopupSelectBinding.bind(parent.context.inflate(R.layout.item_popup_select)) }
    internal var onItemClick: ((item: String?, index: Int) -> Unit)? = { _, _ -> }

    init {
        val textToProcess = when (item) {
            is Int -> {
                string(item).also {
                    mBinding.tvLabel.setI18nRes(item)
                }
            }
            is String -> {
                item.orNoData().also {
                    mBinding.tvLabel.text = it
                }
            }
            else -> {
                ""
            }
        }
        mBinding.root.click {
            onItemClick?.invoke(textToProcess, index)
        }
    }

    fun getRoot(): View {
        return mBinding.root
    }

}