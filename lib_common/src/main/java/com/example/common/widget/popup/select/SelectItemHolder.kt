package com.example.common.widget.popup.select

import android.view.View
import android.view.ViewGroup
import androidx.annotation.ColorRes
import com.example.common.R
import com.example.common.databinding.ItemPopupSelectBinding
import com.example.common.utils.function.orNoData
import com.example.common.utils.i18n.i18String
import com.example.framework.utils.function.inflate
import com.example.framework.utils.function.view.background
import com.example.framework.utils.function.view.click

/**
 * @item -> 文案
 * @index -> 下标
 */
class SelectItemHolder(parent: ViewGroup, item: Any?, index: Int, @ColorRes bgColor: Int = R.color.bgDefault) {
    internal val mBinding by lazy { ItemPopupSelectBinding.bind(parent.context.inflate(R.layout.item_popup_select)) }
    internal var onItemClick: ((item: String?, index: Int) -> Unit)? = null

    init {
        val textToProcess = when (item) {
            is Int -> {
                i18String(item).also {
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
        mBinding.tvLabel.background(bgColor)
        mBinding.root.click {
            onItemClick?.invoke(textToProcess, index)
        }
    }

    fun getRoot(): View {
        return mBinding.root
    }

}