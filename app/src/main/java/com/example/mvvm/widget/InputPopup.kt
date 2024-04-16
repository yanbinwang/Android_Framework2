package com.example.mvvm.widget

import android.content.DialogInterface
import androidx.fragment.app.FragmentManager
import com.example.common.base.BaseBottomSheetDialogFragment
import com.example.framework.utils.builder.TimerBuilder
import com.example.framework.utils.function.view.clear
import com.example.framework.utils.function.view.click
import com.example.framework.utils.function.view.doInput
import com.example.framework.utils.function.view.hideKeyboard
import com.example.framework.utils.function.view.text
import com.example.mvvm.databinding.ViewPopupInputBinding

/**
 * https://blog.51cto.com/u_16175441/9061949
 * 页面设为android:windowSoftInputMode="stateAlwaysHidden|adjustResize"
 */
class InputPopup : BaseBottomSheetDialogFragment<ViewPopupInputBinding>() {
    private var listener: ((text: String?) -> Unit)? = null

    override fun initEvent() {
        super.initEvent()
        mBinding?.tvSend.click {
            listener?.invoke(mBinding?.etInput.text())
            mBinding?.etInput.clear()
            dismiss()
        }
    }

    fun showInput(manager: FragmentManager, text: String?) {
        show(manager)
        mBinding?.etInput?.setText(text.orEmpty())
        TimerBuilder.schedule({
            mBinding?.etInput.doInput()
        }, 150)
    }

    fun setOnInputListener(listener: ((text: String?) -> Unit)) {
        this.listener = listener
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        mBinding?.etInput.hideKeyboard()
    }

}