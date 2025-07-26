package com.yanzhenjie.album.mvp

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
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
        window.setupNavigationBarPadding(R.color.appNavigationBar)
    }

    override fun bye() {
        onBackPressed()
    }

}