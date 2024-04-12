package com.example.mvvm.widget.dialog

import android.os.Bundle
import com.example.common.base.BaseBottomSheetDialogFragment
import com.example.mvvm.databinding.ViewDialogTestBottomBinding

/**
 * @description
 * @author
 */
class TestBottomDialog : BaseBottomSheetDialogFragment<ViewDialogTestBottomBinding>() {

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        initImmersionBar(false,false)
    }

}