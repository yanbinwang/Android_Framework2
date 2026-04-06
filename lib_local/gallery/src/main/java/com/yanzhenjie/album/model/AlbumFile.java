package com.yanzhenjie.album.model;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * 相册媒体文件实体类（图片 / 视频）
 * 实现序列化、排序、相等判断，是整个相册库的核心数据模型
 */
public class AlbumFile implements Parcelable, Comparable<AlbumFile> {
    // 类型：图片/视频
    public static final int TYPE_IMAGE = 1;
    public static final int TYPE_VIDEO = 2;

    // 媒体类型限定注解：只能是 TYPE_IMAGE / TYPE_VIDEO
    @Retention(RetentionPolicy.SOURCE)
    @IntDef({TYPE_IMAGE, TYPE_VIDEO})
    public @interface MediaType {
    }

    // 文件绝对路径
    private String mPath;
    // 所属文件夹名称
    private String mBucketName;
    // 文件MIME类型（image/jpeg、video/mp4等）
    private String mMimeType;
    // 添加时间（毫秒）
    private long mAddDate;
    // 纬度/经度（拍照位置信息）
    private float mLatitude;
    private float mLongitude;
    // 文件大小（字节）
    private long mSize;
    // 视频时长（毫秒），图片为0
    private long mDuration;
    // 缩略图路径
    private String mThumbPath;
    // 媒体类型：图片/视频
    private int mMediaType;
    // 是否被选中
    private boolean isChecked;
    // 是否不可选中（禁用状态）
    private boolean isDisable;

    public AlbumFile() {
    }

    protected AlbumFile(Parcel in) {
        mPath = in.readString();
        mBucketName = in.readString();
        mMimeType = in.readString();
        mAddDate = in.readLong();
        mLatitude = in.readFloat();
        mLongitude = in.readFloat();
        mSize = in.readLong();
        mDuration = in.readLong();
        mThumbPath = in.readString();
        mMediaType = in.readInt();
        isChecked = in.readByte() != 0;
        isDisable = in.readByte() != 0;
    }

    public String getPath() {
        return mPath;
    }

    public void setPath(String path) {
        mPath = path;
    }

    public String getBucketName() {
        return mBucketName;
    }

    public void setBucketName(String bucketName) {
        mBucketName = bucketName;
    }

    public String getMimeType() {
        return mMimeType;
    }

    public void setMimeType(String mimeType) {
        mMimeType = mimeType;
    }

    public long getAddDate() {
        return mAddDate;
    }

    public void setAddDate(long addDate) {
        mAddDate = addDate;
    }

    public float getLatitude() {
        return mLatitude;
    }

    public void setLatitude(float latitude) {
        mLatitude = latitude;
    }

    public float getLongitude() {
        return mLongitude;
    }

    public void setLongitude(float longitude) {
        mLongitude = longitude;
    }

    public long getSize() {
        return mSize;
    }

    public void setSize(long size) {
        mSize = size;
    }

    public long getDuration() {
        return mDuration;
    }

    public void setDuration(long duration) {
        mDuration = duration;
    }

    public String getThumbPath() {
        return mThumbPath;
    }

    public void setThumbPath(String thumbPath) {
        mThumbPath = thumbPath;
    }

    @MediaType
    public int getMediaType() {
        return mMediaType;
    }

    public void setMediaType(@MediaType int mediaType) {
        mMediaType = mediaType;
    }

    public boolean isChecked() {
        return isChecked;
    }

    public void setChecked(boolean checked) {
        isChecked = checked;
    }

    public boolean isDisable() {
        return isDisable;
    }

    public void setDisable(boolean disable) {
        this.isDisable = disable;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mPath);
        dest.writeString(mBucketName);
        dest.writeString(mMimeType);
        dest.writeLong(mAddDate);
        dest.writeFloat(mLatitude);
        dest.writeFloat(mLongitude);
        dest.writeLong(mSize);
        dest.writeLong(mDuration);
        dest.writeString(mThumbPath);
        dest.writeInt(mMediaType);
        dest.writeByte((byte) (isChecked ? 1 : 0));
        dest.writeByte((byte) (isDisable ? 1 : 0));
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<AlbumFile> CREATOR = new Creator<>() {
        @Override
        public AlbumFile createFromParcel(Parcel in) {
            return new AlbumFile(in);
        }

        @Override
        public AlbumFile[] newArray(int size) {
            return new AlbumFile[size];
        }
    };

    @Override
    public int compareTo(AlbumFile o) {
        long time = o.getAddDate() - getAddDate();
        if (time > Integer.MAX_VALUE) {
            return Integer.MAX_VALUE;
        } else if (time < -Integer.MAX_VALUE) {
            return -Integer.MAX_VALUE;
        }
        return (int) time;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof AlbumFile) {
            AlbumFile o = (AlbumFile) obj;
            String inPath = o.getPath();
            if (mPath != null && inPath != null) {
                return mPath.equals(inPath);
            }
        }
        return super.equals(obj);
    }

    @Override
    public int hashCode() {
        return mPath != null ? mPath.hashCode() : super.hashCode();
    }

}