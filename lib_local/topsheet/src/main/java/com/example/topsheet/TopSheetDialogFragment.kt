package com.example.topsheet

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AppCompatDialogFragment

/**
 * 作为一个 Fragment 容器，确保弹出的对话框使用的是自定义的 TopSheetDialog，而不是系统默认的 Dialog
 */
open class TopSheetDialogFragment : AppCompatDialogFragment() {

    /**
     * 创建并返回顶部面板对话框实例
     */
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return TopSheetDialog(requireContext(), theme)
    }

}