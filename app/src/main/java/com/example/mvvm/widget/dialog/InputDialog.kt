package com.example.mvvm.widget.dialog

import android.content.Context
import android.view.Gravity.BOTTOM
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import com.example.common.base.BaseDialog
import com.example.framework.utils.function.view.clear
import com.example.framework.utils.function.view.click
import com.example.framework.utils.function.view.showInput
import com.example.framework.utils.function.view.text
import com.example.mvvm.R
import com.example.mvvm.databinding.ViewDialogInputBinding

/**
 * 底部输入框
 */
class InputDialog(mContext: Context) : BaseDialog<ViewDialogInputBinding>(mContext, MATCH_PARENT, 60, BOTTOM, R.style.InputDialogStyle, false, false) {
    private var listener: ((text: String) -> Unit)? = null

    init {
        mBinding?.tvSend.click {
            listener?.invoke(mBinding?.etContent.text())
            mBinding?.etContent.clear()
            dismiss()
        }
    }

    fun showInput() {
        show()
        mBinding?.etContent.showInput()
    }

    fun setOnInputListener(listener: ((text: String) -> Unit)) {
        this.listener = listener
    }

}