package com.example.common.widget.dialog

import android.content.Context
import android.view.Gravity
import com.example.common.base.BaseDialog
import com.example.common.databinding.ViewDialogBinding
import com.example.framework.utils.function.view.click
import com.example.framework.utils.function.view.gone
import com.example.framework.utils.function.view.visible

/**
 * author: wyb
 * date: 2017/8/25.
 * 类似苹果的弹出窗口类
 */
class AppDialog(context: Context) : BaseDialog<ViewDialogBinding>(context) {
    private var onConfirm: (() -> Unit)? = null
    private var onCancel: (() -> Unit)? = null

    fun setParams(title: String? = "", message: String? = "", positiveText: String? = "", negativeText: String? = "", gravity: Int = Gravity.CENTER) {
        binding.apply {
            if (title.isNullOrEmpty()) {
                tvTip.gone()
            } else {
                tvTip.visible()
                tvTip.text = title
            }
            tvMessage.gravity = gravity
            tvMessage.text = message
            tvSure.apply {
                text = positiveText
                click {
                    dismiss()
                    onConfirm?.invoke()
                }
            }
            if (negativeText.isNullOrEmpty()) {
                viewLine.gone()
                tvCancel.gone()
            } else {
                viewLine.visible()
                tvCancel.apply {
                    visible()
                    text = negativeText
                    click {
                        dismiss()
                        onCancel?.invoke()
                    }
                }
            }
        }
    }

    fun setDialogListener(onConfirm: () -> Unit = {}, onCancel: () -> Unit = {}) {
        this.onConfirm = onConfirm
        this.onCancel = onCancel
    }

}