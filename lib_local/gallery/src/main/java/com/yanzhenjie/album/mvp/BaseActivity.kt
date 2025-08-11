package com.yanzhenjie.album.mvp

import android.os.Build
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
import com.example.common.utils.removeNavigationBarDrawable
import com.example.common.utils.setNavigationBarDrawable
import com.example.common.utils.setNavigationBarLightMode
import com.example.common.utils.setStatusBarLightMode
import com.example.framework.utils.function.view.doOnceAfterLayout
import com.example.framework.utils.function.view.padding
import com.example.framework.utils.function.view.size
import com.gyf.immersionbar.ImmersionBar

abstract class BaseActivity : AppCompatActivity(), Bye {
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
        // 子页不实现方法走默认窗体配置(状态栏+导航栏)
        if (isImmersionBarEnabled()) initImmersionBar()
    }

    protected open fun isImmersionBarEnabled(): Boolean {
        return true
    }

    protected open fun initImmersionBar(statusBarDark: Boolean = false, navigationBarDark: Boolean = false, navigationBarColor: Int = R.color.bgBlack) {
        window?.apply {
            setStatusBarLightMode(statusBarDark)
            setNavigationBarLightMode(navigationBarDark)
            setNavigationBarDrawable(navigationBarColor)
        }
        immersionBar?.apply {
            reset()
            statusBarDarkFont(statusBarDark, 0.2f)
            navigationBarDarkIcon(if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) navigationBarDark else false, 0.2f)//edge会导致低版本ui深浅代码失效,但是会以传入的颜色值为主(偏深为白,反之为黑)
//            navigationBarColor(navigationBarColor)//颜色的配置在高版本上容易出问题,统一改为底部方法
            init()
        }
    }

    override fun bye() {
        onBackPressed()
    }

    override fun onDestroy() {
        super.onDestroy()
        window?.removeNavigationBarDrawable()
        AppManager.removeActivity(this)
    }

}