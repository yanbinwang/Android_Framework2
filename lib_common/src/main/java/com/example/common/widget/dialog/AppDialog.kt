package com.example.common.widget.dialog

import android.view.Gravity
import androidx.fragment.app.FragmentActivity
import com.example.common.R
import com.example.common.base.BaseDialog
import com.example.common.databinding.ViewDialogBinding
import com.example.common.utils.i18n.string
import com.example.framework.utils.function.dimen
import com.example.framework.utils.function.view.click
import com.example.framework.utils.function.view.gone
import com.example.framework.utils.function.view.textColor
import com.example.framework.utils.function.view.visible

/**
 * author: wyb
 * date: 2017/8/25.
 * 类似苹果的弹出窗口类
 */
class AppDialog(activity: FragmentActivity) : BaseDialog<ViewDialogBinding>(activity) {
    private var onConfirm: (() -> Unit)? = null
    private var onCancel: (() -> Unit)? = null

    /**
     * 确定
     */
    fun setPositive(title: String? = string(R.string.hint), message: String? = null, positiveText: String? = string(R.string.sure)): AppDialog {
        return setParams(title, message, positiveText, "")
    }

    /**
     * 确定/取消
     */
    fun setParams(title: String? = string(R.string.hint), message: String? = null, positiveText: String? = string(R.string.sure), negativeText: String? = string(R.string.cancel), gravity: Int = Gravity.CENTER): AppDialog {
        mBinding?.apply {
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
        return this
    }

    fun setDialogListener(onConfirm: () -> Unit = {}, onCancel: () -> Unit = {}): AppDialog {
        this.onConfirm = onConfirm
        this.onCancel = onCancel
        return this
    }

    fun setTipTheme(colorRes: Int = R.color.textPrimary, sizeRes: Int = R.dimen.textSize14): AppDialog {
        mBinding?.tvTip.textColor(colorRes)
        mBinding?.tvTip?.textSize = context.dimen(sizeRes)
        return this
    }

    fun setMessageTheme(colorRes: Int = R.color.textPrimary, sizeRes: Int = R.dimen.textSize14): AppDialog {
        mBinding?.tvMessage.textColor(colorRes)
        mBinding?.tvMessage?.textSize = context.dimen(sizeRes)
        return this
    }

    fun setSureTheme(colorRes: Int = R.color.appTheme, sizeRes: Int = R.dimen.textSize14): AppDialog {
        mBinding?.tvSure.textColor(colorRes)
        mBinding?.tvSure?.textSize = context.dimen(sizeRes)
        return this
    }

    fun setCancelTheme(colorRes: Int = R.color.appTheme, sizeRes: Int = R.dimen.textSize14): AppDialog {
        mBinding?.tvCancel.textColor(colorRes)
        mBinding?.tvCancel?.textSize = context.dimen(sizeRes)
        return this
    }

}