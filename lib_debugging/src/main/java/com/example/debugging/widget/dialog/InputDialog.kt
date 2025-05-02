package com.example.debugging.widget.dialog

import android.content.Context
import android.text.InputFilter
import android.widget.EditText
import androidx.core.widget.doAfterTextChanged
import com.example.common.base.BaseDialog
import com.example.common.widget.textview.edittext.EditTextImpl
import com.example.debugging.databinding.ViewDialogInputBinding
import com.example.framework.utils.function.value.orFalse
import com.example.framework.utils.function.view.addFilter
import com.example.framework.utils.function.view.click
import com.example.framework.utils.function.view.disable
import com.example.framework.utils.function.view.hideKeyboard
import com.example.framework.utils.function.view.onDone
import com.example.framework.utils.function.view.text

/**
 * 提示对话框
 */
class InputDialog(context: Context) : BaseDialog<ViewDialogInputBinding>(context), EditTextImpl {
    private var defaultInput: String? = null

    init {
        mBinding?.tvConfirm.disable()
        mBinding?.etInput?.doAfterTextChanged {
            mBinding?.tvConfirm?.isEnabled = mBinding?.etInput.text().isNotEmpty()
        }
        mBinding?.etInput.onDone {
            if (mBinding?.tvConfirm?.isEnabled.orFalse) mBinding?.tvConfirm?.callOnClick()
        }
    }

    override fun dismiss() {
        mBinding?.etInput.hideKeyboard()
        super.dismiss()
    }

    override fun show() {
        mBinding?.etInput?.setText(defaultInput.orEmpty())
        super.show()
    }

    fun setDefaultText(defaultInput: String?) {
        this.defaultInput = defaultInput
        mBinding?.etInput?.setText(defaultInput)
    }

    fun setInputType(option: Int) {
        mBinding?.etInput?.setInputType(option)
    }

    fun addInputFilter(filter: InputFilter) {
        mBinding?.etInput?.addFilter(filter)
    }

    /**
     * confirmListener return true为校验通过，false为不通过
     */
    fun setOnItemClickListener(confirmListener: (edit: EditText?) -> Boolean, resetListener: () -> String) {
        mBinding?.tvConfirm.click {
            if (confirmListener(mBinding?.etInput)) {
                dismiss()
            }
        }
        mBinding?.tvReset.click {
            mBinding?.etInput?.setText(resetListener())
        }
    }

}