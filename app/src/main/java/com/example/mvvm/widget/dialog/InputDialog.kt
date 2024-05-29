package com.example.mvvm.widget.dialog

import android.content.Context
import android.view.Gravity.BOTTOM
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import com.example.common.base.BaseDialog
import com.example.framework.utils.function.view.showInput
import com.example.mvvm.R
import com.example.mvvm.databinding.ViewDialogInputBinding

class InputDialog(mContext: Context) : BaseDialog<ViewDialogInputBinding>(
    mContext,
    MATCH_PARENT,
    60,
    BOTTOM,
    R.style.InputDialogStyle,
    false,
    false
) {

    fun showInput() {
        show()
        mBinding?.etContent.showInput()
    }

}