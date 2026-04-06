package com.yanzhenjie.album.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

/**
 * 相册文件夹实体类
 * 代表手机里的一个图片/相册文件夹，包含文件夹名称、图片列表、选中状态
 */
public class AlbumFolder implements Parcelable {
    // 相册文件夹是否被选中（在文件夹切换列表中标记当前选中项）
    private boolean isChecked;
    // 文件夹名称（例如：Camera、Screenshots、微信）
    private String name;
    // 该文件夹下的图片/视频文件列表
    private ArrayList<AlbumFile> mAlbumFiles = new ArrayList<>();

    public AlbumFolder() {
    }

    public boolean isChecked() {
        return isChecked;
    }

    public void setChecked(boolean checked) {
        isChecked = checked;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ArrayList<AlbumFile> getAlbumFiles() {
        return mAlbumFiles;
    }

    public void addAlbumFile(AlbumFile albumFile) {
        mAlbumFiles.add(albumFile);
    }

    protected AlbumFolder(Parcel in) {
        name = in.readString();
        mAlbumFiles = in.createTypedArrayList(AlbumFile.CREATOR);
        isChecked = in.readByte() != 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeTypedList(mAlbumFiles);
        dest.writeByte((byte) (isChecked ? 1 : 0));
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<AlbumFolder> CREATOR = new Creator<>() {
        @Override
        public AlbumFolder createFromParcel(Parcel in) {
            return new AlbumFolder(in);
        }

        @Override
        public AlbumFolder[] newArray(int size) {
            return new AlbumFolder[size];
        }
    };

}