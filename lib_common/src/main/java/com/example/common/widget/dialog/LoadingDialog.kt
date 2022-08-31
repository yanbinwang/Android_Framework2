package com.example.common.widget.dialog

import android.content.Context
import com.example.common.R
import com.example.common.base.BaseDialog
import com.example.common.databinding.ViewDialogLoadingBinding

/**
 * Created by wyb on 2017/6/28.
 * 加载动画view
 */
class LoadingDialog(context: Context) : BaseDialog<ViewDialogLoadingBinding>(context, R.style.loadingStyle) {

    init {
        initialize()
    }

    override fun initialize() {
        super.initialize()
        setOnDismissListener { binding.progress.stopSpinning() }
    }

    override fun show() {
        super.show()
        if (!binding.progress.isSpinning) binding.progress.spin()
    }

    fun shown(flag: Boolean) {
        setCancelable(flag)
        if (!isShowing) show()
    }

    fun hidden() {
        if (isShowing) dismiss()
    }

}