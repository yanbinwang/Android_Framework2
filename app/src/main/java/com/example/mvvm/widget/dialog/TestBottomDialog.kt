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
//class TestBottomDialog(activity: FragmentActivity) : BasePopupWindow<ViewDialogTestBottomBinding>(activity, popupHeight = MATCH_PARENT, popupAnimStyle = Companion.PopupAnimType.TRANSLATE) {
class TestBottomDialog(activity: FragmentActivity) : BasePopupWindow<ViewDialogTestBottomBinding>(activity, popupAnimStyle = Companion.PopupAnimType.TRANSLATE) {
//    override fun initEvent() {
//        super.initEvent()
//        // 右侧进入
//        val (enter, exit) = Pair(
//            Slide().apply { duration = 300; mode = Visibility.MODE_IN; slideEdge = Gravity.RIGHT },
//            Slide().apply { duration = 300; mode = Visibility.MODE_OUT; slideEdge = Gravity.RIGHT }
//        )
//        enterTransition = enter
//        exitTransition = exit
//    }

}