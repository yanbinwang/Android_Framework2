package com.yanzhenjie.album.app.album.data;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.provider.MediaStore;

import androidx.annotation.WorkerThread;

import com.example.gallery.R;
import com.yanzhenjie.album.AlbumFile;
import com.yanzhenjie.album.AlbumFolder;
import com.yanzhenjie.album.Filter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * 系统媒体扫描器（核心类）
 * 功能：扫描手机里的 图片 / 视频 / 全部媒体
 * 并按文件夹分类，返回给相册使用
 */
public class MediaReader {
    // 过滤后的文件是否显示（true=显示但禁用，false=直接隐藏）
    private final boolean mFilterVisibility;
    // 上下文
    private final Context mContext;
    // 文件大小过滤器
    private final Filter<Long> mSizeFilter;
    // 文件类型过滤器
    private final Filter<String> mMimeFilter;
    // 视频时长过滤器
    private final Filter<Long> mDurationFilter;
    // 需要查询的【图片】字段
    private static final String[] IMAGES = {
            MediaStore.Images.Media.DATA,                // 路径
            MediaStore.Images.Media.BUCKET_DISPLAY_NAME, // 文件夹名
            MediaStore.Images.Media.MIME_TYPE,           // 类型
            MediaStore.Images.Media.DATE_ADDED,          // 添加时间
            MediaStore.Images.Media.LATITUDE,            // 纬度
            MediaStore.Images.Media.LONGITUDE,           // 经度
            MediaStore.Images.Media.SIZE                 // 大小
    };
    // 需要查询的【视频】字段
    private static final String[] VIDEOS = {
            MediaStore.Video.Media.DATA,
            MediaStore.Video.Media.BUCKET_DISPLAY_NAME,
            MediaStore.Video.Media.MIME_TYPE,
            MediaStore.Video.Media.DATE_ADDED,
            MediaStore.Video.Media.LATITUDE,
            MediaStore.Video.Media.LONGITUDE,
            MediaStore.Video.Media.SIZE,
            MediaStore.Video.Media.DURATION           // 视频时长
    };

    /**
     * 构造方法：传入过滤规则
     */
    public MediaReader(Context context, Filter<Long> sizeFilter, Filter<String> mimeFilter, Filter<Long> durationFilter, boolean filterVisibility) {
        this.mContext = context;
        this.mSizeFilter = sizeFilter;
        this.mMimeFilter = mimeFilter;
        this.mDurationFilter = durationFilter;
        this.mFilterVisibility = filterVisibility;
    }

    /**
     * 扫描系统【图片】
     */
    @WorkerThread
    private void scanImageFile(Map<String, AlbumFolder> albumFolderMap, AlbumFolder allFileFolder) {
        ContentResolver contentResolver = mContext.getContentResolver();
        Cursor cursor = contentResolver.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, IMAGES, null, null, null);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                String path = cursor.getString(0);
                String bucketName = cursor.getString(1);
                String mimeType = cursor.getString(2);
                long addDate = cursor.getLong(3);
                float latitude = cursor.getFloat(4);
                float longitude = cursor.getFloat(5);
                long size = cursor.getLong(6);
                // 封装成 AlbumFile
                AlbumFile imageFile = new AlbumFile();
                imageFile.setMediaType(AlbumFile.TYPE_IMAGE);
                imageFile.setPath(path);
                imageFile.setBucketName(bucketName);
                imageFile.setMimeType(mimeType);
                imageFile.setAddDate(addDate);
                imageFile.setLatitude(latitude);
                imageFile.setLongitude(longitude);
                imageFile.setSize(size);
                // 大小过滤
                if (mSizeFilter != null && mSizeFilter.filter(size)) {
                    if (!mFilterVisibility) continue;
                    imageFile.setDisable(true);
                }
                // 类型过滤
                if (mMimeFilter != null && mMimeFilter.filter(mimeType)) {
                    if (!mFilterVisibility) continue;
                    imageFile.setDisable(true);
                }
                // 添加到“全部图片”文件夹
                allFileFolder.addAlbumFile(imageFile);
                // 添加到对应子文件夹
                AlbumFolder albumFolder = albumFolderMap.get(bucketName);
                if (albumFolder != null) {
                    albumFolder.addAlbumFile(imageFile);
                } else {
                    albumFolder = new AlbumFolder();
                    albumFolder.setName(bucketName);
                    albumFolder.addAlbumFile(imageFile);
                    albumFolderMap.put(bucketName, albumFolder);
                }
            }
            cursor.close();
        }
    }

    /**
     * 扫描系统【视频】
     */
    @WorkerThread
    private void scanVideoFile(Map<String, AlbumFolder> albumFolderMap, AlbumFolder allFileFolder) {
        ContentResolver contentResolver = mContext.getContentResolver();
        Cursor cursor = contentResolver.query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, VIDEOS, null, null, null);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                String path = cursor.getString(0);
                String bucketName = cursor.getString(1);
                String mimeType = cursor.getString(2);
                long addDate = cursor.getLong(3);
                float latitude = cursor.getFloat(4);
                float longitude = cursor.getFloat(5);
                long size = cursor.getLong(6);
                long duration = cursor.getLong(7);
                // 封装成 AlbumFile
                AlbumFile videoFile = new AlbumFile();
                videoFile.setMediaType(AlbumFile.TYPE_VIDEO);
                videoFile.setPath(path);
                videoFile.setBucketName(bucketName);
                videoFile.setMimeType(mimeType);
                videoFile.setAddDate(addDate);
                videoFile.setLatitude(latitude);
                videoFile.setLongitude(longitude);
                videoFile.setSize(size);
                videoFile.setDuration(duration);
                // 大小过滤
                if (mSizeFilter != null && mSizeFilter.filter(size)) {
                    if (!mFilterVisibility) continue;
                    videoFile.setDisable(true);
                }
                // 类型过滤
                if (mMimeFilter != null && mMimeFilter.filter(mimeType)) {
                    if (!mFilterVisibility) continue;
                    videoFile.setDisable(true);
                }
                // 时长过滤
                if (mDurationFilter != null && mDurationFilter.filter(duration)) {
                    if (!mFilterVisibility) continue;
                    videoFile.setDisable(true);
                }
                // 添加到“全部视频”文件夹
                allFileFolder.addAlbumFile(videoFile);
                // 添加到对应子文件夹
                AlbumFolder albumFolder = albumFolderMap.get(bucketName);
                if (albumFolder != null) {
                    albumFolder.addAlbumFile(videoFile);
                } else {
                    albumFolder = new AlbumFolder();
                    albumFolder.setName(bucketName);
                    albumFolder.addAlbumFile(videoFile);
                    albumFolderMap.put(bucketName, albumFolder);
                }
            }
            cursor.close();
        }
    }

    /**
     * 获取【所有图片】（按文件夹分类）
     */
    @WorkerThread
    public ArrayList<AlbumFolder> getAllImage() {
        Map<String, AlbumFolder> albumFolderMap = new HashMap<>();
        AlbumFolder allFileFolder = new AlbumFolder();
        allFileFolder.setChecked(true);
        allFileFolder.setName(mContext.getString(R.string.album_all_images));
        // 扫描图片
        scanImageFile(albumFolderMap, allFileFolder);
        ArrayList<AlbumFolder> albumFolders = new ArrayList<>();
        Collections.sort(allFileFolder.getAlbumFiles());
        albumFolders.add(allFileFolder);
        // 遍历所有子文件夹
        for (Map.Entry<String, AlbumFolder> folderEntry : albumFolderMap.entrySet()) {
            AlbumFolder albumFolder = folderEntry.getValue();
            Collections.sort(albumFolder.getAlbumFiles());
            albumFolders.add(albumFolder);
        }
        return albumFolders;
    }

    /**
     * 获取【所有视频】（按文件夹分类）
     */
    @WorkerThread
    public ArrayList<AlbumFolder> getAllVideo() {
        Map<String, AlbumFolder> albumFolderMap = new HashMap<>();
        AlbumFolder allFileFolder = new AlbumFolder();
        allFileFolder.setChecked(true);
        allFileFolder.setName(mContext.getString(R.string.album_all_videos));
        // 扫描视频
        scanVideoFile(albumFolderMap, allFileFolder);
        ArrayList<AlbumFolder> albumFolders = new ArrayList<>();
        Collections.sort(allFileFolder.getAlbumFiles());
        albumFolders.add(allFileFolder);
        for (Map.Entry<String, AlbumFolder> folderEntry : albumFolderMap.entrySet()) {
            AlbumFolder albumFolder = folderEntry.getValue();
            Collections.sort(albumFolder.getAlbumFiles());
            albumFolders.add(albumFolder);
        }
        return albumFolders;
    }

    /**
     * 获取【所有媒体：图片 + 视频】
     */
    @WorkerThread
    public ArrayList<AlbumFolder> getAllMedia() {
        Map<String, AlbumFolder> albumFolderMap = new HashMap<>();
        AlbumFolder allFileFolder = new AlbumFolder();
        allFileFolder.setChecked(true);
        allFileFolder.setName(mContext.getString(R.string.album_all_images_videos));
        // 扫描图片 + 视频
        scanImageFile(albumFolderMap, allFileFolder);
        scanVideoFile(albumFolderMap, allFileFolder);
        ArrayList<AlbumFolder> albumFolders = new ArrayList<>();
        Collections.sort(allFileFolder.getAlbumFiles());
        albumFolders.add(allFileFolder);
        for (Map.Entry<String, AlbumFolder> folderEntry : albumFolderMap.entrySet()) {
            AlbumFolder albumFolder = folderEntry.getValue();
            Collections.sort(albumFolder.getAlbumFiles());
            albumFolders.add(albumFolder);
        }
        return albumFolders;
    }

}