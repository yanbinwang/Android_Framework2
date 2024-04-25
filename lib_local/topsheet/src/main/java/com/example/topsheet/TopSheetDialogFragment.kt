package com.example.topsheet

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AppCompatDialogFragment

/**
 * Created by andrea on 23/08/16.
 */
open class TopSheetDialogFragment : AppCompatDialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return TopSheetDialog(requireContext(), theme)
    }
}