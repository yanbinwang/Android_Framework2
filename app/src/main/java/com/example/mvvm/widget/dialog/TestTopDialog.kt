package com.example.mvvm.widget.dialog

import com.example.common.base.BaseTopSheetDialogFragment
import com.example.mvvm.R
import com.example.mvvm.databinding.ViewDialogTestTopBinding

/**
 * @description
 * @author
 */
class TestTopDialog : BaseTopSheetDialogFragment<ViewDialogTestTopBinding>() {
    override fun isImmersionBarEnabled(): Boolean {
        return true
    }
}