package com.yanzhenjie.durban;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * <p>Control panel configuration.</p>
 * Created by Yan Zhenjie on 2017/5/30.
 */
public class Controller implements Parcelable {

    private Controller(Parcel in) {
        this.enable = in.readByte() != 0;
        this.rotation = in.readByte() != 0;
        this.rotationTitle = in.readByte() != 0;
        this.scale = in.readByte() != 0;
        this.scaleTitle = in.readByte() != 0;
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

    /**
     * Create a Builder.
     */
    public static Builder newBuilder() {
        return new Builder();
    }

    private boolean enable;

    private boolean rotation;
    private boolean rotationTitle;

    private boolean scale;
    private boolean scaleTitle;

    private Controller(Builder builder) {
        this.enable = builder.enable;
        this.rotation = builder.rotation;
        this.rotationTitle = builder.rotationTitle;
        this.scale = builder.scale;
        this.scaleTitle = builder.scaleTitle;
    }

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

    public static final class Builder {

        private boolean enable = true;

        private boolean rotation = true;
        private boolean rotationTitle = true;

        private boolean scale = true;
        private boolean scaleTitle = true;

        private Builder() {
        }

        public Builder enable(boolean enable) {
            this.enable = enable;
            return this;
        }

        public Builder rotation(boolean rotation) {
            this.rotation = rotation;
            return this;
        }

        public Builder rotationTitle(boolean rotationTitle) {
            this.rotationTitle = rotationTitle;
            return this;
        }

        public Builder scale(boolean scale) {
            this.scale = scale;
            return this;
        }

        public Builder scaleTitle(boolean scaleTitle) {
            this.scaleTitle = scaleTitle;
            return this;
        }

        public Controller build() {
            return new Controller(this);
        }
    }

}