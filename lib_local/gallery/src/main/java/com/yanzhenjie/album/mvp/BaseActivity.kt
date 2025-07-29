package com.yanzhenjie.album.mvp

import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.annotation.ColorInt
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.graphics.ColorUtils.calculateLuminance
import com.example.common.R
import com.example.common.utils.function.getStatusBarHeight
import com.example.common.utils.setupNavigationBarPadding
import com.example.framework.utils.function.view.doOnceAfterLayout
import com.example.framework.utils.function.view.padding
import com.example.framework.utils.function.view.size
import com.yanzhenjie.album.Album
import com.yanzhenjie.album.util.AlbumUtils

abstract class BaseActivity : AppCompatActivity(), Bye {

    companion object {

        /**
         * 兼容控件内toolbar
         */
        @JvmStatic
        fun setToolbar(toolbar: Toolbar) {
            toolbar.doOnceAfterLayout {
                toolbar.size(height = it.measuredHeight + getStatusBarHeight())
                toolbar.padding(top = getStatusBarHeight())
            }
        }

        /**
         * 根据背景颜色决定电池图标的颜色（黑或白）
         * @param backgroundColor 背景颜色值，如Color.BLACK
         * @return 适合的图标颜色（Color.WHITE或Color.BLACK）
         */
        @JvmStatic
        fun getBatteryIconColor(@ColorInt backgroundColor: Int): Int {
            // 使用系统API获取相对亮度（0.0-1.0之间）
            val luminance = calculateLuminance(backgroundColor)
            // 亮度阈值，这里使用0.5作为中间值
            // 亮度低于阈值，使用白色图标；高于阈值，使用黑色图标
            return if (luminance < 0.5) Color.WHITE else Color.BLACK
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val locale = Album.getAlbumConfig().locale
        AlbumUtils.applyLanguageForContext(this, locale)
    }

    override fun setContentView(layoutResID: Int) {
        super.setContentView(layoutResID)
        // 布局延伸到状态栏
        val flags = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        window.decorView.systemUiVisibility = flags
        // 设置导航栏高版本新增间距
        window.setupNavigationBarPadding(R.color.appNavigationBar)
    }

    override fun bye() {
        onBackPressed()
    }

}