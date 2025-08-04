package com.example.common.widget.dialog

import android.text.Spannable
import android.view.Gravity
import androidx.fragment.app.FragmentActivity
import com.example.common.R
import com.example.common.base.BaseDialog
import com.example.common.databinding.ViewDialogBinding
import com.example.common.utils.function.string
import com.example.framework.utils.function.view.click
import com.example.framework.utils.function.view.gone
import com.example.framework.utils.function.view.textColor
import com.example.framework.utils.function.view.textSize
import com.example.framework.utils.function.view.visible

/**
 * author: wyb
 * date: 2017/8/25.
 * 类似苹果的弹出窗口类
 */
class AppDialog(activity: FragmentActivity) : BaseDialog<ViewDialogBinding>(activity) {
    private var onPositive: (() -> Unit)? = null
    private var onNegative: (() -> Unit)? = null

    /**
     * 确定样式
     */
    fun setPositive(title: String? = string(R.string.hint), message: String? = null, positiveText: String? = string(R.string.sure), gravity: Int = Gravity.CENTER, hasTitle: Boolean = true): AppDialog {
        return setParams(title, message, positiveText, "", gravity, hasTitle)
    }

    /**
     * 确定/取消 样式
     */
    fun setParams(title: String? = string(R.string.hint), message: String? = null, positiveText: String? = string(R.string.sure), negativeText: String? = string(R.string.cancel), gravity: Int = Gravity.CENTER, hasTitle: Boolean = true): AppDialog {
        mBinding?.apply {
            // 标题
            if (!hasTitle) {
                tvTitle.gone()
            } else {
                tvTitle.visible()
                tvTitle.text = title
            }
            // 内容
            tvMessage.gravity = gravity
            tvMessage.text = message
            // 操作键
            tvPositive.apply {
                text = positiveText
                click {
                    dismiss()
                    onPositive?.invoke()
                }
            }
            if (negativeText.isNullOrEmpty()) {
                viewLine.gone()
                tvNegative.gone()
            } else {
                viewLine.visible()
                tvNegative.apply {
                    visible()
                    text = negativeText
                    click {
                        dismiss()
                        onNegative?.invoke()
                    }
                }
            }
        }
        return this
    }

    /**
     * 标题样式
     */
    fun setTitle(colorRes: Int = R.color.appTheme, sizeRes: Int = R.dimen.textSize14): AppDialog {
        mBinding?.tvTitle.textColor(colorRes)
        mBinding?.tvTitle?.textSize(sizeRes)
        return this
    }

    /**
     * 内容样式
     */
    fun setMessage(colorRes: Int = R.color.textPrimary, sizeRes: Int = R.dimen.textSize14): AppDialog {
        mBinding?.tvMessage.textColor(colorRes)
        mBinding?.tvMessage?.textSize(sizeRes)
        return this
    }

    fun setMessage(spannable: Spannable): AppDialog {
        mBinding?.tvMessage?.text = spannable
        return this
    }

    /**
     * 确认样式
     */
    fun setPositiveTheme(colorRes: Int = R.color.appTheme, sizeRes: Int = R.dimen.textSize14): AppDialog {
        mBinding?.tvPositive.textColor(colorRes)
        mBinding?.tvPositive?.textSize(sizeRes)
        return this
    }

    /**
     * 取消样式
     */
    fun setNegativeTheme(colorRes: Int = R.color.appTheme, sizeRes: Int = R.dimen.textSize14): AppDialog {
        mBinding?.tvNegative.textColor(colorRes)
        mBinding?.tvNegative?.textSize(sizeRes)
        return this
    }

    /**
     * 回调监听
     */
    fun setDialogListener(onPositive: () -> Unit = {}, onNegative: () -> Unit = {}): AppDialog {
        this.onPositive = onPositive
        this.onNegative = onNegative
        return this
    }

}