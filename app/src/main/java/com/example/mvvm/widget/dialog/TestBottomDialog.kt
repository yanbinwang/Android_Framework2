package com.example.mvvm.widget.dialog

import android.transition.Slide
import android.transition.Visibility
import android.view.Gravity
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import androidx.fragment.app.FragmentActivity
import com.example.common.base.BasePopupWindow
import com.example.mvvm.databinding.ViewDialogTestBottomBinding

/**
 * @description
 * @author
 */
class TestBottomDialog(activity: FragmentActivity) : BasePopupWindow<ViewDialogTestBottomBinding>(activity, popupHeight = MATCH_PARENT, popupAnimStyle = Companion.PopupAnimType.TRANSLATE, popupSlide = Gravity.RIGHT) {
//class TestBottomDialog(activity: FragmentActivity) : BasePopupWindow<ViewDialogTestBottomBinding>(activity, popupAnimStyle = Companion.PopupAnimType.TRANSLATE) {

}