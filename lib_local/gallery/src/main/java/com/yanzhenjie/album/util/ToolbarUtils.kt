package com.yanzhenjie.album.util

import androidx.appcompat.widget.Toolbar
import com.example.common.utils.function.getStatusBarHeight
import com.example.framework.utils.function.view.doOnceAfterLayout
import com.example.framework.utils.function.view.padding
import com.example.framework.utils.function.view.size

object ToolbarUtils {

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

}