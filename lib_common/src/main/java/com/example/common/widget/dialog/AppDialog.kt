package com.example.common.widget.dialog

import android.content.Context
import android.os.Build
import android.text.TextUtils
import android.view.Gravity
import android.view.WindowManager
import com.example.base.utils.function.view.gone
import com.example.common.base.BaseDialog
import com.example.common.databinding.ViewDialogBinding

/**
 * author: wyb
 * date: 2017/8/25.
 * 类似苹果的弹出窗口类
 */
class AppDialog(context: Context) : BaseDialog<ViewDialogBinding>(context) {
    private var onConfirm: (() -> Unit)? = null
    private var onCancel: (() -> Unit)? = null

    init {
        initialize(true, true)
    }

    fun setParams(title: String? = "", message: String? = "", positiveText: String? = "", negativeText: String? = "", center: Boolean = true): AppDialog {
        binding.apply {
            //如果没有传入标题字段,则隐藏标题view
            if (TextUtils.isEmpty(title)) tvTip.gone()
            //如果没有传入取消字段,则隐藏取消view
            if (TextUtils.isEmpty(negativeText)) {
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
            tvCancel.setOnClickListener {
                dismiss()
                onCancel?.invoke()
            }
            //点击了确定按钮的回调
            tvSure.setOnClickListener {
                dismiss()
                onConfirm?.invoke()
            }
        }
        return this
    }

    fun setType(): AppDialog {
        window?.setType(if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY else WindowManager.LayoutParams.TYPE_SYSTEM_ALERT)
        return this
    }

    fun setOnDialogListener(onConfirm: (() -> Unit), onCancel: (() -> Unit)? = {}): AppDialog {
        this.onConfirm = onConfirm
        this.onCancel = onCancel
        return this
    }

    companion object {
        @JvmStatic
        fun with(context: Context?): AppDialog {
            return AppDialog(context!!)
        }
    }

}