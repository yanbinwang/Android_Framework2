package com.example.common.widget.dialog

import androidx.annotation.ColorInt
import androidx.fragment.app.FragmentActivity
import com.example.common.R
import com.example.common.base.BaseDialog
import com.example.common.databinding.ViewDialogLoadingBinding
import com.example.common.utils.function.color

/**
 * Created by wyb on 2017/6/28.
 * 加载动画view
 */
class LoadingDialog(activity: FragmentActivity) : BaseDialog<ViewDialogLoadingBinding>(activity, R.style.LoadingStyle, 90, 90, hasAnimation = false) {

    init {
        setColorFilter(color(R.color.bgWhite))
    }

    override fun show() {
        super.show()
        mBinding?.pbProcess?.isIndeterminate = true
    }

    override fun dismiss() {
        super.dismiss()
        mBinding?.pbProcess?.isIndeterminate = false
    }

    fun setColorFilter(@ColorInt color: Int) {
        mBinding?.pbProcess?.indeterminateDrawable = mBinding?.pbProcess?.indeterminateDrawable?.mutate()?.apply {
            setTint(color)
        }
    }

}