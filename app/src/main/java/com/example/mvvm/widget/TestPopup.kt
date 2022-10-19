package com.example.mvvm.widget

import android.content.Context
import android.view.Gravity
import com.example.common.R
import com.example.common.base.BaseDialog
import com.example.mvvm.databinding.ViewPopupTopBinding

/**
 * @description
 * @author
 */
class TestPopup(context: Context) : BaseDialog<ViewPopupTopBinding>(context,180,180, gravity = Gravity.TOP xor Gravity.END) {
    init {
        window?.setWindowAnimations(R.style.pushRightAnimStyle)
//        window?.setGravity(Gravity.TOP xor Gravity.END)
    }
}