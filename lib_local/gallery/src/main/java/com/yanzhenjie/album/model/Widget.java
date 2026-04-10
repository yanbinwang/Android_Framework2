package com.yanzhenjie.album.model;

import android.content.Context;
import android.content.res.ColorStateList;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import androidx.annotation.ColorInt;
import androidx.annotation.ColorRes;
import androidx.annotation.IntDef;
import androidx.annotation.StringRes;
import androidx.core.content.ContextCompat;

import com.example.gallery.R;
import com.yanzhenjie.album.utils.AlbumUtil;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * 相册主题样式配置类
 * 作用：统一管理相册的所有UI样式（亮色/暗色、状态栏、导航栏、标题、选择框、按钮）
 * 采用 Builder 模式 + Parcelable 序列化（可跨页面传递）
 */
public class Widget implements Parcelable {
    // 上下文
    private Context mContext;
    // 主题样式：亮色 / 暗色
    private final int mUiStyle;
    // 状态栏颜色
    private final int mStatusBarColor;
    // 导航栏颜色
    private final int mNavigationBarColor;
    // 标题
    private final String mTitle;
    // 媒体条目（图片/视频）选择框颜色状态
    private final ColorStateList mMediaItemCheckSelector;
    // 文件夹条目选择框颜色状态
    private final ColorStateList mBucketItemCheckSelector;
    // 按钮样式
    private final ButtonStyle mButtonStyle;
    // 主题样式常量
    public static final int STYLE_LIGHT = 1; // 亮色
    public static final int STYLE_DARK = 2;  // 暗色

    // 限定主题只能是这两种
    @IntDef({STYLE_DARK, STYLE_LIGHT})
    @Retention(RetentionPolicy.SOURCE)
    public @interface UiStyle {
    }

    /**
     * 构造方法：通过 Builder 构建
     */
    private Widget(Builder builder) {
        this.mContext = builder.mContext;
        this.mUiStyle = builder.mUiStyle;
        // 未设置则使用默认颜色
        this.mStatusBarColor = builder.mStatusBarColor == 0 ? R.color.galleryStatusBar : builder.mStatusBarColor;
        this.mNavigationBarColor = builder.mNavigationBarColor == 0 ? R.color.galleryNavigationBar : builder.mNavigationBarColor;
        // 未设置标题则使用默认标题
        this.mTitle = TextUtils.isEmpty(builder.mTitle) ? mContext.getString(R.string.album_title) : builder.mTitle;
        this.mMediaItemCheckSelector = builder.mMediaItemCheckSelector == null ? AlbumUtil.getColorStateList(getColor(R.color.albumSelectorNormal), getColor(R.color.galleryColorPrimary)) : builder.mMediaItemCheckSelector;
        this.mBucketItemCheckSelector = builder.mBucketItemCheckSelector == null ? AlbumUtil.getColorStateList(getColor(R.color.albumSelectorNormal), getColor(R.color.galleryColorPrimary)) : builder.mBucketItemCheckSelector;
        // 未设置按钮样式则使用默认按钮样式
        this.mButtonStyle = builder.mButtonStyle == null ? ButtonStyle.newDarkBuilder(mContext).build() : builder.mButtonStyle;
    }

    /**
     * 获取颜色
     */
    private int getColor(@ColorRes int colorId) {
        return ContextCompat.getColor(mContext, colorId);
    }

    @UiStyle
    public int getUiStyle() {
        return mUiStyle;
    }

    @ColorRes
    public int getStatusBarColor() {
        return mStatusBarColor;
    }

    @ColorRes
    public int getNavigationBarColor() {
        return mNavigationBarColor;
    }

    public String getTitle() {
        return mTitle;
    }

    public ColorStateList getMediaItemCheckSelector() {
        return mMediaItemCheckSelector;
    }

    public ColorStateList getBucketItemCheckSelector() {
        return mBucketItemCheckSelector;
    }

    public ButtonStyle getButtonStyle() {
        return mButtonStyle;
    }

    protected Widget(Parcel in) {
        mUiStyle = in.readInt();
        mStatusBarColor = in.readInt();
        mNavigationBarColor = in.readInt();
        mTitle = in.readString();
        mMediaItemCheckSelector = in.readParcelable(ColorStateList.class.getClassLoader());
        mBucketItemCheckSelector = in.readParcelable(ColorStateList.class.getClassLoader());
        mButtonStyle = in.readParcelable(ButtonStyle.class.getClassLoader());
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(mUiStyle);
        dest.writeInt(mStatusBarColor);
        dest.writeInt(mNavigationBarColor);
        dest.writeString(mTitle);
        dest.writeParcelable(mMediaItemCheckSelector, flags);
        dest.writeParcelable(mBucketItemCheckSelector, flags);
        dest.writeParcelable(mButtonStyle, flags);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Widget> CREATOR = new Creator<>() {
        @Override
        public Widget createFromParcel(Parcel in) {
            return new Widget(in);
        }

        @Override
        public Widget[] newArray(int size) {
            return new Widget[size];
        }
    };

    /**
     * 获取默认主题（暗色主题）
     */
    public static Widget getDefaultWidget(Context context) {
        return Widget.newDarkBuilder(context)
                .statusBarColor(R.color.galleryStatusBar)
                .navigationBarColor(R.color.galleryNavigationBar)
                .title(R.string.album_title)
                .mediaItemCheckSelector(ContextCompat.getColor(context, R.color.albumSelectorNormal), ContextCompat.getColor(context, R.color.galleryColorPrimary))
                .bucketItemCheckSelector(ContextCompat.getColor(context, R.color.albumSelectorNormal), ContextCompat.getColor(context, R.color.galleryColorPrimary))
                .buttonStyle(ButtonStyle.getDefaultButtonStyle(context))
                .build();
    }

    /**
     * 暗色状态栏(黑色)
     */
    public static Builder newDarkBuilder(Context context) {
        return new Builder(context, STYLE_DARK);
    }

    /**
     * 亮色状态栏(白色)
     */
    public static Builder newLightBuilder(Context context) {
        return new Builder(context, STYLE_LIGHT);
    }

    /**
     * 指定亮/暗
     */
    public static Builder newBuilder(Context context, @UiStyle int style) {
        return new Builder(context, style);
    }

    /**
     * 类构建器
     */
    public static class Builder {
        private int mStatusBarColor;
        private int mNavigationBarColor;
        private String mTitle;
        private ColorStateList mMediaItemCheckSelector;
        private ColorStateList mBucketItemCheckSelector;
        private ButtonStyle mButtonStyle;
        private final int mUiStyle;
        private final Context mContext;

        private Builder(Context context, @UiStyle int style) {
            this.mContext = context;
            this.mUiStyle = style;
        }

        /**
         * 设置状态栏颜色
         */
        public Builder statusBarColor(@ColorRes int color) {
            this.mStatusBarColor = color;
            return this;
        }

        /**
         * 设置导航栏颜色
         */
        public Builder navigationBarColor(@ColorRes int color) {
            this.mNavigationBarColor = color;
            return this;
        }

        /**
         * 设置标题
         */
        public Builder title(@StringRes int title) {
            return title(mContext.getString(title));
        }

        public Builder title(String title) {
            this.mTitle = title;
            return this;
        }

        /**
         * 设置媒体条目选择框颜色
         */
        public Builder mediaItemCheckSelector(@ColorInt int normalColor, @ColorInt int highLightColor) {
            this.mMediaItemCheckSelector = AlbumUtil.getColorStateList(normalColor, highLightColor);
            return this;
        }

        /**
         * 设置文件夹条目选择框颜色
         */
        public Builder bucketItemCheckSelector(@ColorInt int normalColor, @ColorInt int highLightColor) {
            this.mBucketItemCheckSelector = AlbumUtil.getColorStateList(normalColor, highLightColor);
            return this;
        }

        /**
         * 设置按钮样式
         */
        public Builder buttonStyle(ButtonStyle buttonStyle) {
            this.mButtonStyle = buttonStyle;
            return this;
        }

        /**
         * 构建 Widget
         */
        public Widget build() {
            return new Widget(this);
        }
    }

}