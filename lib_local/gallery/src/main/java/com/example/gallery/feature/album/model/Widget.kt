package com.example.gallery.feature.album.model

import android.content.Context
import android.content.res.ColorStateList
import android.os.Parcelable
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.annotation.IntDef
import androidx.annotation.StringRes
import com.example.framework.utils.function.color
import com.example.gallery.R
import com.example.gallery.feature.album.utils.AlbumUtil
import kotlinx.parcelize.Parcelize

/**
 * 相册主题样式配置类
 * 作用：统一管理相册的所有UI样式（亮色/暗色、状态栏、导航栏、标题、选择框、按钮）
 * 采用 Builder 模式 + Parcelable 序列化（可跨页面传递）
 */
@Parcelize
data class Widget(
    @field:UiStyle
    val uiStyle: Int, // 主题样式：亮色 / 暗色
    @field:ColorRes
    val statusBarColor: Int, // 状态栏颜色
    @field:ColorRes
    val navigationBarColor: Int, // 导航栏颜色
    val title: String, // 标题
    val mediaItemCheckSelector: ColorStateList, // 媒体条目（图片/视频）选择框颜色状态
    val bucketItemCheckSelector: ColorStateList, // 文件夹条目选择框颜色状态
    val buttonSelector: ColorStateList // 按钮样式
) : Parcelable {

    companion object {
        // 主题样式常量
        const val STYLE_LIGHT = 1 // 亮色
        const val STYLE_DARK = 2 // 暗色

        // 限定主题只能是这两种
        @IntDef(STYLE_DARK, STYLE_LIGHT)
        @Retention(AnnotationRetention.SOURCE)
        annotation class UiStyle

        /**
         * 暗色状态栏(黑色)
         */
        @JvmStatic
        fun newDarkBuilder(context: Context): Builder {
            return Builder(context, STYLE_DARK)
        }

        /**
         * 亮色状态栏(白色)
         */
        @JvmStatic
        fun newLightBuilder(context: Context): Builder {
            return Builder(context, STYLE_LIGHT)
        }

        /**
         * 指定亮/暗
         */
        @JvmStatic
        fun newBuilder(context: Context, @UiStyle style: Int): Builder {
            return Builder(context, style)
        }

        /**
         * 获取默认主题（暗色主题）
         */
        @JvmStatic
        fun getDefaultWidget(context: Context): Widget {
            return newDarkBuilder(context)
                .statusBarColor(R.color.galleryStatusBar)
                .navigationBarColor(R.color.galleryNavigationBar)
                .title(R.string.album_title)
                .mediaItemCheckSelector(context.color(R.color.albumSelectorNormal), context.color(R.color.galleryColorPrimary))
                .bucketItemCheckSelector(context.color(R.color.albumSelectorNormal), context.color(R.color.galleryColorPrimary))
                .buttonSelector(context.color(R.color.galleryColorPrimary), context.color(R.color.galleryColorPrimaryDark))
                .build()
        }
    }

    /**
     * 构造方法：通过 Builder 构建
     */
    constructor(builder: Builder) : this(
        builder.mUiStyle,
        builder.mStatusBarColor.takeIf { it != 0 } ?: R.color.galleryStatusBar,
        builder.mNavigationBarColor.takeIf { it != 0 } ?: R.color.galleryNavigationBar,
        builder.mTitle.takeIf { !it.isNullOrEmpty() } ?: builder.mContext.getString(R.string.album_title),
        builder.mMediaItemCheckSelector.takeIf { it != null } ?: AlbumUtil.getColorStateList(builder.mContext.color(R.color.albumSelectorNormal), builder.mContext.color(R.color.galleryColorPrimary)),
        builder.mBucketItemCheckSelector.takeIf { it != null } ?: AlbumUtil.getColorStateList(builder.mContext.color(R.color.albumSelectorNormal), builder.mContext.color(R.color.galleryColorPrimary)),
        builder.mButtonSelector.takeIf { it != null } ?: AlbumUtil.getColorStateList(builder.mContext.color(R.color.galleryColorPrimary), builder.mContext.color(R.color.galleryColorPrimaryDark))
    )

    /**
     * 类构建器
     */
    class Builder(val mContext: Context, @field:UiStyle val mUiStyle: Int) {
        @field:ColorRes
        var mStatusBarColor: Int? = null
        @field:ColorRes
        var mNavigationBarColor: Int? = null
        var mTitle: String? = null
        var mMediaItemCheckSelector: ColorStateList? = null
        var mBucketItemCheckSelector: ColorStateList? = null
        var mButtonSelector: ColorStateList? = null

        /**
         * 设置状态栏颜色
         */
        fun statusBarColor(@ColorRes color: Int): Builder {
            this.mStatusBarColor = color
            return this
        }

        /**
         * 设置导航栏颜色
         */
        fun navigationBarColor(@ColorRes color: Int): Builder {
            this.mNavigationBarColor = color
            return this
        }

        /**
         * 设置标题
         */
        fun title(@StringRes title: Int): Builder {
            return title(mContext.getString(title))
        }

        fun title(title: String): Builder {
            this.mTitle = title
            return this
        }

        /**
         * 设置媒体条目选择框颜色
         */
        fun mediaItemCheckSelector(@ColorInt normalColor: Int, @ColorInt highLightColor: Int): Builder {
            this.mMediaItemCheckSelector = AlbumUtil.getColorStateList(normalColor, highLightColor)
            return this
        }

        /**
         * 设置文件夹条目选择框颜色
         */
        fun bucketItemCheckSelector(@ColorInt normalColor: Int, @ColorInt highLightColor: Int): Builder {
            this.mBucketItemCheckSelector = AlbumUtil.getColorStateList(normalColor, highLightColor)
            return this
        }

        /**
         * 设置按钮点击效果
         */
        fun buttonSelector(@ColorInt normalColor: Int, @ColorInt highLightColor: Int): Builder {
            mButtonSelector = AlbumUtil.getColorStateList(normalColor, highLightColor)
            return this
        }

        /**
         * 构建 Widget
         */
        fun build(): Widget {
            return Widget(this)
        }
    }

}