package com.yanzhenjie.album.mvp

import android.os.Bundle
import android.view.Window
import androidx.activity.enableEdgeToEdge
import androidx.annotation.ColorRes
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.graphics.ColorUtils.calculateLuminance
import com.example.common.R
import com.example.common.utils.function.color
import com.example.common.utils.function.getStatusBarHeight
import com.example.common.utils.manager.AppManager
import com.example.common.utils.setNavigationBarDrawable
import com.example.framework.utils.function.view.doOnceAfterLayout
import com.example.framework.utils.function.view.padding
import com.example.framework.utils.function.view.size
import com.gyf.immersionbar.ImmersionBar
import com.yanzhenjie.album.Album
import com.yanzhenjie.album.util.AlbumUtils

abstract class BaseActivity : AppCompatActivity(), Bye {
    private var onApplyInsets = false//窗体监听等回调是否已经加载完毕
    private val immersionBar by lazy { ImmersionBar.with(this) }

    companion object {

        /**
         * 兼容控件内toolbar
         */
        @JvmStatic
        fun setSupportToolbar(toolbar: Toolbar) {
            toolbar.doOnceAfterLayout {
                it.size(height = it.measuredHeight + getStatusBarHeight())
                it.padding(top = getStatusBarHeight())
            }
        }

        /**
         * 根据背景颜色决定电池图标的颜色（黑或白）
         */
        @JvmStatic
        fun getBatteryIcon(@ColorRes backgroundColor: Int): Boolean {
            // 使用系统API获取相对亮度（0.0-1.0之间）
            val luminance = calculateLuminance(color(backgroundColor))
            // 亮度阈值，这里使用0.5作为中间值
            // 白色的相对亮度（luminance）是 1.0（最高值），黑色是 0.0（最低值）
            return if (luminance < 0.5) true else false
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        // 开启谷歌全屏模式
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        // 禁用ActionBar
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE)
        // 添加至统一页面管理类
        AppManager.addActivity(this)
        // 基类全局语言配置以相册配置为主(多语言本地化可删除)
        val locale = Album.getAlbumConfig().locale
        AlbumUtils.applyLanguageForContext(this, locale)
        // 子页不实现方法走默认窗体配置(状态栏+导航栏)
        if (isImmersionBarEnabled()) initImmersionBar()
    }

    protected open fun isImmersionBarEnabled(): Boolean {
        return true
    }

    protected open fun initImmersionBar(statusBarDark: Boolean = false, navigationBarDark: Boolean = false, navigationBarColor: Int = R.color.bgBlack) {
        immersionBar?.apply {
            reset()
            statusBarDarkFont(statusBarDark, 0.2f)
            navigationBarDarkIcon(navigationBarDark, 0.2f)
            init()
        }
        if (!onApplyInsets) {
            onApplyInsets = true
            window.setNavigationBarDrawable(navigationBarColor)
        }
    }

    override fun bye() {
        onBackPressed()
    }

    override fun onDestroy() {
        super.onDestroy()
        onApplyInsets = false
        AppManager.removeActivity(this)
    }

}