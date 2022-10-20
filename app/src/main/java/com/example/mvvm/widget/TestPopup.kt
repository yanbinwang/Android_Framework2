package com.example.mvvm.widget

import android.content.Context
import android.view.Gravity
import android.view.Window
import com.example.common.R
import com.example.common.base.BaseDialog
import com.example.common.base.BasePopupWindow
import com.example.mvvm.databinding.ViewPopupTopBinding

/**
 * @description
 * @author
 */
class TestPopup(window: Window) : BasePopupWindow<ViewPopupTopBinding>(window, popupHeight = 180) {
    init {
    }
}