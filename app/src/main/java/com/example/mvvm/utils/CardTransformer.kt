package com.example.mvvm.utils

import android.view.View
import androidx.viewpager2.widget.ViewPager2
import com.example.common.utils.function.ptFloat
import com.example.framework.utils.logWTF
import kotlin.math.abs

/**
 * @description
 * transformPage：会在初次create的时候以及每次页面被滑动时调用（滑动时将调用多次）
 * @author yan
 */
class CardTransformer : ViewPager2.PageTransformer {
//    //值越大縮放越大
//    private val mScaleOffset = 200f
//    //值越大item間距越大
//    private val mTranslationOffset = 100f
    //值越大縮放越大
    private val scaleOffset = 100.ptFloat
    //值越大item間距越大
    private val translationOffset = 50.ptFloat

    override fun transformPage(page: View, position: Float) {
//        "position:${position}".logWTF
        if (position <= 0f) {
            page.translationY = 0f
        } else {
            val pageHeight = page.height
            val transY = -pageHeight * position + translationOffset * position
            page.translationY = transY
            //頂層蓋住下層
            page.translationZ = -position
            //缩放比例
            val scale = (pageHeight - scaleOffset * position) / pageHeight.toFloat()
            page.scaleX = scale
            page.scaleY = scale
            //只顯示3個
            if (abs(position) >= 3) {
                page.alpha = 0f
            } else {
                if (position >= 2.5) {
                    page.alpha = 0f
                } else {
                    page.alpha = 1f
                }
            }
        }
    }
}