package com.example.mvvm.widget.dialog

import android.view.Gravity.BOTTOM
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.FragmentActivity
import com.example.common.base.BaseDialog
import com.example.framework.utils.function.view.clear
import com.example.framework.utils.function.view.click
import com.example.framework.utils.function.view.gone
import com.example.framework.utils.function.view.showInput
import com.example.framework.utils.function.view.text
import com.example.framework.utils.function.view.visible
import com.example.mvvm.R
import com.example.mvvm.databinding.ViewDialogInputBinding

/**
 * 底部输入框
 */
class InputDialog(private val activity: FragmentActivity) : BaseDialog<ViewDialogInputBinding>(activity, R.style.InputDialogStyle, MATCH_PARENT, 60, BOTTOM, false) {
    private var listener: ((text: String) -> Unit)? = null

    init {
        mBinding?.tvSend.click {
            dismissInput()
        }
    }

    fun showInput() {
        show()
        mBinding?.etContent.showInput(activity)
    }

    private fun dismissInput() {
        listener?.invoke(mBinding?.etContent.text())
        mBinding?.etContent.clear()
        dismiss()
    }

    fun setOnWindowInsetsChanged(insets: WindowInsetsCompat) {
        val imeType = WindowInsetsCompat.Type.ime()
        val isImeVisible = insets.isVisible(imeType)
        if (!isImeVisible) {
            dismissInput()
        }
    }

    fun setOnInputListener(listener: ((text: String) -> Unit)) {
        this.listener = listener
    }

}