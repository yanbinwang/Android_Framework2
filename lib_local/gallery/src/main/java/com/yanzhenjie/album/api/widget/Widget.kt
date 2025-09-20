package com.yanzhenjie.album.api.widget

import android.content.res.ColorStateList
import android.os.Parcelable
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.annotation.IntDef
import androidx.annotation.StringRes
import com.example.common.utils.function.color
import com.example.common.utils.function.string
import com.example.gallery.R
import com.yanzhenjie.album.util.AlbumUtils
import kotlinx.parcelize.Parcelize

/**
 * Created by YanZhenjie on 2017/8/16.
 */
@Parcelize
data class Widget(
    var mUiStyle: Int? = null,
    var mStatusBarColor: Int? = null,
    var mNavigationBarColor: Int? = null,
    var mTitle: String? = null,
    var mMediaItemCheckSelector: ColorStateList? = null,
    var mBucketItemCheckSelector: ColorStateList? = null,
    var mButtonStyle: ButtonStyle? = null
) : Parcelable {

    companion object {
        /**
         * 亮/暗样式
         */
        const val STYLE_LIGHT: Int = 1
        const val STYLE_DARK: Int = 2

        @IntDef(STYLE_DARK, STYLE_LIGHT)
        @Retention(AnnotationRetention.SOURCE)
        annotation class UiStyle

        /**
         * 暗色状态栏(黑色)
         */
        fun newDarkBuilder(): Builder {
            return Builder(STYLE_DARK)
        }

        /**
         * 亮色状态栏(白色)
         */
        fun newLightBuilder(): Builder {
            return Builder(STYLE_LIGHT)
        }

        /**
         * 指定亮/暗
         */
        fun newBuilder(@UiStyle style: Int): Builder {
            return Builder(style)
        }

        /**
         * Widget的Builder
         */
        class Builder {
            private var mUiStyle: Int? = null
            private var mStatusBarColor: Int? = null
            private var mNavigationBarColor: Int? = null
            private var mTitle: String? = null
            private var mMediaItemCheckSelector: ColorStateList? = null
            private var mBucketItemCheckSelector: ColorStateList? = null
            private var mButtonStyle: ButtonStyle? = null

            constructor(@UiStyle style: Int) {
                this.mUiStyle = style
            }

            /**
             * Status bar color.
             */
            fun statusBarColor(@ColorRes color: Int): Builder {
                this.mStatusBarColor = color
                return this
            }

            /**
             * Virtual navigation bar.
             */
            fun navigationBarColor(@ColorRes color: Int): Builder {
                this.mNavigationBarColor = color
                return this
            }

            /**
             * Set the title of the Toolbar.
             */
            fun title(@StringRes title: Int): Builder {
                return title(string(title))
            }

            /**
             * Set the title of the Toolbar.
             */
            fun title(title: String): Builder {
                this.mTitle = title
                return this
            }

            /**
             * The color of the `Media Item` selector.
             */
            fun mediaItemCheckSelector(@ColorInt normalColor: Int, @ColorInt highLightColor: Int): Builder {
                this.mMediaItemCheckSelector = AlbumUtils.getColorStateList(normalColor, highLightColor)
                return this
            }

            /**
             * The color of the `Bucket Item` selector.
             */
            fun bucketItemCheckSelector(@ColorInt normalColor: Int, @ColorInt highLightColor: Int): Builder {
                this.mBucketItemCheckSelector = AlbumUtils.getColorStateList(normalColor, highLightColor)
                return this
            }

            /**
             * Set the style of the Button.
             */
            fun buttonStyle(buttonStyle: ButtonStyle): Builder {
                this.mButtonStyle = buttonStyle
                return this
            }

            /**
             * Create target.
             */
            fun build(): Widget {
                return Widget(mUiStyle, if (mStatusBarColor == 0) R.color.albumColorPrimaryDark else mStatusBarColor, if (mNavigationBarColor == 0) R.color.albumColorPrimaryBlack else mNavigationBarColor, if (mTitle.isNullOrEmpty()) string(R.string.album_title) else mTitle, if (mMediaItemCheckSelector == null) AlbumUtils.getColorStateList(color(R.color.albumSelectorNormal), color(R.color.albumColorPrimary)) else mMediaItemCheckSelector, if (mBucketItemCheckSelector == null) AlbumUtils.getColorStateList(color(R.color.albumSelectorNormal), color(R.color.albumColorPrimary)) else mBucketItemCheckSelector, if (mButtonStyle == null) ButtonStyle.newDarkBuilder().build() else mButtonStyle)
            }

        }

        @Parcelize
        data class ButtonStyle(var mUiStyle: Int? = null, var mButtonSelector: ColorStateList? = null) : Parcelable {

            companion object {

                /**
                 * Use when the Button are dark.
                 */
                fun newDarkBuilder(): Builder {
                    return Builder(STYLE_DARK)
                }

                /**
                 * Use when the Button are light.
                 */
                fun newLightBuilder(): Builder {
                    return Builder(STYLE_LIGHT)
                }

                /**
                 * ButtonStyle的Builder
                 */
                class Builder {
                    private var mUiStyle: Int? = null
                    private var mButtonSelector: ColorStateList? = null

                    constructor(@UiStyle style: Int) {
                        this.mUiStyle = style
                    }

                    /**
                     * Set button click effect.
                     *
                     * @param normalColor    normal color.
                     * @param highLightColor feedback color.
                     */
                    fun setButtonSelector(@ColorInt normalColor: Int, @ColorInt highLightColor: Int): Builder {
                        mButtonSelector = AlbumUtils.getColorStateList(normalColor, highLightColor)
                        return this
                    }

                    fun build(): ButtonStyle {
                        return ButtonStyle(mUiStyle, if (mButtonSelector == null) AlbumUtils.getColorStateList(color(R.color.albumColorPrimary), color(R.color.albumColorPrimaryDark)) else mButtonSelector)
                    }

                }

            }

        }

        /**
         * Create default widget.
         */
        @JvmStatic
        fun getDefaultWidget(): Widget {
            return newDarkBuilder()
                .statusBarColor(R.color.albumColorPrimaryDark)
                .navigationBarColor(R.color.albumColorPrimaryBlack)
                .title(R.string.album_title)
                .mediaItemCheckSelector(color(R.color.albumSelectorNormal), color(R.color.albumColorPrimary))
                .bucketItemCheckSelector(color(R.color.albumSelectorNormal), color(R.color.albumColorPrimary))
                .buttonStyle(ButtonStyle
                    .newDarkBuilder()
                    .setButtonSelector(color(R.color.albumColorPrimary), color(R.color.albumColorPrimaryDark))
                    .build())
                .build()
        }

    }

}