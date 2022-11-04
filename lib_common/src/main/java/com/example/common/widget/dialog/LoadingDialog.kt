package com.example.common.widget.dialog

import android.content.Context
import com.example.common.R
import com.example.common.base.BaseDialog
import com.example.common.databinding.ViewDialogLoadingBinding

/**
 * Created by wyb on 2017/6/28.
 * 加载动画view
 */
class LoadingDialog(context: Context) : BaseDialog<ViewDialogLoadingBinding>(context, dialogWidth = 160, animation = false, themeResId = R.style.loadingStyle) {

    override fun shown(flag: Boolean) {
        super.shown(flag)
        binding.pbProcess.isIndeterminate = true
    }

    override fun hidden() {
        super.hidden()
        binding.pbProcess.isIndeterminate = false
    }

}