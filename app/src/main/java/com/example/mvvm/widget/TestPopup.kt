package com.example.mvvm.widget

import android.app.Activity
import android.view.Gravity
import android.view.View
import com.example.common.base.BasePopupWindow
import com.example.mvvm.databinding.ViewPopupTopBinding

/**
 * @description
 * @author
 */
class TestPopup(activity: Activity,view: View) : BasePopupWindow<ViewPopupTopBinding>(activity , view,light = true, anim = true, Gravity.END) {
}