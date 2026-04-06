package com.yanzhenjie.durban.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * 裁剪控制器的配置类
 * 作用：配置裁剪界面【是否启用旋转、缩放、对应文字显示】等开关
 * 实现 Parcelable ：可以在 Activity / Fragment 之间传递对象
 */
public class Controller implements Parcelable {
    // 总开关：是否启用整个控制器
    private final boolean enable;
    // 旋转功能开关
    private final boolean rotation;
    // 旋转文字标题开关
    private final boolean rotationTitle;
    // 缩放功能开关
    private final boolean scale;
    // 缩放文字标题开关
    private final boolean scaleTitle;

    private Controller(Parcel in) {
        this.enable = in.readByte() != 0;
        this.rotation = in.readByte() != 0;
        this.rotationTitle = in.readByte() != 0;
        this.scale = in.readByte() != 0;
        this.scaleTitle = in.readByte() != 0;
    }

    private Controller(Builder builder) {
        enable = builder.enable;
        rotation = builder.rotation;
        rotationTitle = builder.rotationTitle;
        scale = builder.scale;
        scaleTitle = builder.scaleTitle;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeByte((byte) (enable ? 1 : 0));
        dest.writeByte((byte) (rotation ? 1 : 0));
        dest.writeByte((byte) (rotationTitle ? 1 : 0));
        dest.writeByte((byte) (scale ? 1 : 0));
        dest.writeByte((byte) (scaleTitle ? 1 : 0));
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Controller> CREATOR = new Creator<>() {
        @Override
        public Controller createFromParcel(Parcel in) {
            return new Controller(in);
        }

        @Override
        public Controller[] newArray(int size) {
            return new Controller[size];
        }
    };

    public boolean isEnable() {
        return enable;
    }

    public boolean isRotation() {
        return rotation;
    }

    public boolean isRotationTitle() {
        return rotationTitle;
    }

    public boolean isScale() {
        return scale;
    }

    public boolean isScaleTitle() {
        return scaleTitle;
    }

    /**
     * 对外创建 Builder
     */
    public static Builder newBuilder() {
        return new Builder();
    }

    public static final class Builder {
        // 默认全部开启
        private boolean enable = true;
        private boolean rotation = true;
        private boolean rotationTitle = true;
        private boolean scale = true;
        private boolean scaleTitle = true;

        private Builder() {
        }

        /**
         * 总开关
         */
        public Builder enable(boolean enable) {
            this.enable = enable;
            return this;
        }

        /**
         * 旋转开关
         */
        public Builder rotation(boolean rotation) {
            this.rotation = rotation;
            return this;
        }

        /**
         * 旋转标题开关
         */
        public Builder rotationTitle(boolean rotationTitle) {
            this.rotationTitle = rotationTitle;
            return this;
        }

        /**
         * 缩放开关
         */
        public Builder scale(boolean scale) {
            this.scale = scale;
            return this;
        }

        /**
         * 缩放标题开关
         */
        public Builder scaleTitle(boolean scaleTitle) {
            this.scaleTitle = scaleTitle;
            return this;
        }

        /**
         * 构建最终 Controller 对象
         */
        public Controller build() {
            return new Controller(this);
        }

    }

}