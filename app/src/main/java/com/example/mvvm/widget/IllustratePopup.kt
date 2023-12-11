package com.example.mvvm.widget

import android.view.Gravity
import android.view.View
import androidx.fragment.app.FragmentActivity
import com.example.common.base.BasePopupWindow
import com.example.common.base.PopupAnimType.ALPHA
import com.example.framework.utils.function.value.orZero
import com.example.framework.utils.function.view.margin
import com.example.mvvm.databinding.ViewPopupIllustrateBinding

/**
 * @description 说明的气泡弹框
 * @author yan
 */
class IllustratePopup(activity: FragmentActivity) : BasePopupWindow<ViewPopupIllustrateBinding>(activity, popupAnimStyle = ALPHA) {

    fun showUp(anchor: View?, text: String) {
        mBinding.tvContent.text = text
        val location = IntArray(2)
        anchor?.getLocationOnScreen(location)
        val left = location[0]
        mBinding.viewArrow.margin(start = left + (anchor?.measuredWidth.orZero / 2))
        showAtLocation(anchor, Gravity.NO_GRAVITY, 0, location[1] - measuredHeight - anchor?.measuredHeight.orZero)
    }

}