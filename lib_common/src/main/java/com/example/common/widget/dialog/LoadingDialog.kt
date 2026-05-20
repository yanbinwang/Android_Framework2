package com.example.common.widget.dialog

import androidx.fragment.app.FragmentActivity
import com.example.common.R
import com.example.common.base.BaseDialog
import com.example.common.databinding.ViewDialogLoadingBinding

/**
 * Created by wyb on 2017/6/28.
 * 加载动画view
 */
class LoadingDialog(activity: FragmentActivity) : BaseDialog<ViewDialogLoadingBinding>(activity, R.style.LoadingStyle, 90, 90, hasAnimation = false) {

    override fun show() {
        super.show()
        mBinding?.pbProcess?.isIndeterminate = true
    }

    override fun dismiss() {
        super.dismiss()
        mBinding?.pbProcess?.isIndeterminate = false
    }

}