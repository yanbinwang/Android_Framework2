package com.example.mvvm.widget.dialog

import com.example.common.base.BaseBottomSheetDialogFragment
import com.example.mvvm.databinding.ViewDialogTestBottomBinding

/**
 * @description
 * @author
 */
class TestBottomDialog : BaseBottomSheetDialogFragment<ViewDialogTestBottomBinding>() {

    override fun isImmersionBarEnabled() = true

    override fun initView() {
        super.initView()
        initImmersionBar(false,false)
    }

}