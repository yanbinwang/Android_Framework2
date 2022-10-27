package com.example.common.widget.dialog

import android.content.Context
import android.view.Gravity
import com.example.base.utils.function.view.click
import com.example.base.utils.function.view.gone
import com.example.common.base.BaseDialog
import com.example.common.databinding.ViewDialogBinding

/**
 * author: wyb
 * date: 2017/8/25.
 * 类似苹果的弹出窗口类
 */
class AppDialog(context: Context) : BaseDialog<ViewDialogBinding>(context, close = true) {
    var onConfirm: (() -> Unit)? = null
    var onCancel: (() -> Unit)? = null

    fun setParams(title: String? = "", message: String? = "", positiveText: String? = "", negativeText: String? = "", center: Boolean = true) {
        binding.apply {
            //如果没有传入标题字段,则隐藏标题view
            if (title.isNullOrEmpty()) tvTip.gone()
            //如果没有传入取消字段,则隐藏取消view
            if (negativeText.isNullOrEmpty()) {
                viewLine.gone()
                tvCancel.gone()
            }
            //文案方向
            tvContainer.gravity = if (center) Gravity.CENTER else Gravity.LEFT
            //对控件赋值
            tvTip.text = title
            tvContainer.text = message
            tvSure.text = positiveText
            tvCancel.text = negativeText
            //点击了取消按钮的回调
            tvCancel.click {
                dismiss()
                onCancel?.invoke()
            }
            //点击了确定按钮的回调
            tvSure.click {
                dismiss()
                onConfirm?.invoke()
            }
        }
    }

}