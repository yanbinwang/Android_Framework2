package com.example.mvvm.widget.dialog

import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import com.example.common.base.BaseBottomSheetDialogFragment
import com.example.common.base.BasePopupWindow
import com.example.mvvm.R
import com.example.mvvm.databinding.ViewDialogTestBottomBinding

/**
 * @description
 * @author
 */
class TestBottomDialog(activity: FragmentActivity) : BasePopupWindow<ViewDialogTestBottomBinding>(activity, popupAnimStyle = Companion.PopupAnimType.TRANSLATE) {

}