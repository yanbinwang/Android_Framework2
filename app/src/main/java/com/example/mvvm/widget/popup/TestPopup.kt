package com.example.mvvm.widget.popup

import android.view.Gravity.TOP
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import androidx.fragment.app.FragmentActivity
import com.example.common.base.BasePopupWindow
import com.example.mvvm.databinding.ViewPopupTestBinding

/**
 * @description
 * @author
 */
class TestPopup(activity: FragmentActivity) :
    BasePopupWindow<ViewPopupTestBinding>(activity, gravity = TOP) {
}