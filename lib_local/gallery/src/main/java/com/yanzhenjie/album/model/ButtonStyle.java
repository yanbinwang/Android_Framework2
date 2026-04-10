package com.yanzhenjie.album.model;

import static com.yanzhenjie.album.model.Widget.STYLE_DARK;
import static com.yanzhenjie.album.model.Widget.STYLE_LIGHT;

import android.content.Context;
import android.content.res.ColorStateList;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.ColorInt;
import androidx.annotation.ColorRes;
import androidx.core.content.ContextCompat;

import com.example.gallery.R;
import com.yanzhenjie.album.utils.AlbumUtil;

/**
 * 按钮样式内部类
 */
public class ButtonStyle implements Parcelable {
    private Context mContext;
    private final int mUiStyle;
    private final ColorStateList mButtonSelector;

    private ButtonStyle(Builder builder) {
        this.mContext = builder.mContext;
        this.mUiStyle = builder.mUiStyle;
        this.mButtonSelector = builder.mButtonSelector == null ? AlbumUtil.getColorStateList(getColor(R.color.galleryColorPrimary), getColor(R.color.galleryColorPrimaryDark)) : builder.mButtonSelector;
    }

    private int getColor(@ColorRes int colorId) {
        return ContextCompat.getColor(mContext, colorId);
    }

    public int getUiStyle() {
        return mUiStyle;
    }

    public ColorStateList getButtonSelector() {
        return mButtonSelector;
    }

    protected ButtonStyle(Parcel in) {
        mUiStyle = in.readInt();
        mButtonSelector = in.readParcelable(ColorStateList.class.getClassLoader());
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(mUiStyle);
        dest.writeParcelable(mButtonSelector, flags);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<ButtonStyle> CREATOR = new Creator<>() {
        @Override
        public ButtonStyle createFromParcel(Parcel in) {
            return new ButtonStyle(in);
        }

        @Override
        public ButtonStyle[] newArray(int size) {
            return new ButtonStyle[size];
        }
    };

    public static ButtonStyle getDefaultButtonStyle(Context context) {
        return ButtonStyle.newDarkBuilder(context)
                .setButtonSelector(ContextCompat.getColor(context, R.color.galleryColorPrimary), ContextCompat.getColor(context, R.color.galleryColorPrimaryDark))
                .build();
    }

    public static Builder newDarkBuilder(Context context) {
        return new Builder(context, STYLE_DARK);
    }

    public static Builder newLightBuilder(Context context) {
        return new Builder(context, STYLE_LIGHT);
    }

    public static Builder newBuilder(Context context, @Widget.UiStyle int style) {
        return new Builder(context, style);
    }

    /**
     * 类构建器
     */
    public static class Builder {
        private ColorStateList mButtonSelector;
        private final int mUiStyle;
        private final Context mContext;

        private Builder(Context context, @Widget.UiStyle int style) {
            this.mContext = context;
            this.mUiStyle = style;
        }

        /**
         * 设置按钮点击效果
         */
        public Builder setButtonSelector(@ColorInt int normalColor, @ColorInt int highLightColor) {
            mButtonSelector = AlbumUtil.getColorStateList(normalColor, highLightColor);
            return this;
        }

        public ButtonStyle build() {
            return new ButtonStyle(this);
        }
    }

}