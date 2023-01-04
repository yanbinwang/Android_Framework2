package com.example.mvvm.widget

import android.view.Gravity.TOP
import android.view.Window
import com.example.common.base.BasePopupWindow
import com.example.mvvm.databinding.ViewDialogTestBinding


/**
 * @description
 * @author
 */
class TestDialog(window: Window) : BasePopupWindow<ViewDialogTestBinding>(
    window, slideEdge = TOP
) {

    /**
     * 用于根据传入的宽高的比例显示window
     */
//    private fun initWidthAndHeightByPercent(widthPercent: Float, heightPercent: Float) {
////        val windowManager = window?.windowManager
////        val display = windowManager?.defaultDisplay
////        val lp = window?.attributes
////        //decorView是window中的最顶层view，可以从window中获取到decorView,获取状态栏的高度
////        val statusBarHeight = getStatusBarHeight()
////        lp?.width = (display?.width.orZero * widthPercent).toSafeInt() //设置宽度
////        //高度值需要减去状态栏的高度
////        lp?.height = ((display?.height.orZero - statusBarHeight) * heightPercent).toSafeInt()
////        window?.attributes = lp
////        // 注意此处必须设置,因为window默认会设置一个有padding 的背景
////        window?.setBackgroundDrawable(ColorDrawable(Color.WHITE))
//
////        val windowManager = window?.windowManager
////        val display = windowManager?.defaultDisplay
//        val lp = window?.attributes
//        //decorView是window中的最顶层view，可以从window中获取到decorView,获取状态栏的高度
//        val statusBarHeight = getStatusBarHeight()
//        lp?.width = screenWidth
//        //高度值需要减去状态栏的高度
//        lp?.height = screenHeight -getStatusBarHeight()
//        window?.attributes = lp
//        // 注意此处必须设置,因为window默认会设置一个有padding 的背景
//        window?.setBackgroundDrawable(ColorDrawable(Color.WHITE))
//    }

//    private fun initWidthAndHeightByPercent(marginTop: Int) {
//        val lp = window?.attributes
//        lp?.width = screenWidth
//        //高度值需要减去状态栏的高度
////        lp?.height = screenHeight - getStatusBarHeight() - marginTop
//        lp?.height = screenHeight - marginTop
//        window?.attributes = lp
//        // 注意此处必须设置,因为window默认会设置一个有padding 的背景
//        window?.setBackgroundDrawable(ColorDrawable(Color.WHITE))
//    }

}