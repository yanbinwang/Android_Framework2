package com.example.common.widget.dialog

import android.content.Context
import com.example.common.R
import com.example.common.base.BaseDialog
import com.example.common.databinding.ViewDialogLoadingBinding

/**
 * Created by wyb on 2017/6/28.
 * 加载动画view
 */
class LoadingDialog(context: Context) : BaseDialog<ViewDialogLoadingBinding>(context, 90, 90, themeResId = R.style.LoadingStyle, animation = false) {

    override fun shown(flag: Boolean) {
        super.shown(flag)
        mBinding?.pbProcess?.isIndeterminate = true
    }

    override fun hidden() {
        super.hidden()
        mBinding?.pbProcess?.isIndeterminate = false
    }

}