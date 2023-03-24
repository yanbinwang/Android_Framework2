package com.example.mvvm.widget

import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.FragmentActivity
import com.example.common.base.BasePopupWindow
import com.example.framework.utils.function.view.doOnceAfterLayout
import com.example.framework.utils.function.view.size
import com.example.mvvm.databinding.ViewPopupChatBinding

class ChatPopup(activity: FragmentActivity) : BasePopupWindow<ViewPopupChatBinding>(activity) {
    private var textList: List<LinearLayout>? = null

    init {
        textList = listOf(binding.llContent)
        binding.llContent.doOnceAfterLayout {
            binding.ivChat.size(it.measuredWidth, it.measuredHeight)
        }
    }

}