package com.yanzhenjie.album.app.album.data;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.media.MediaMetadataRetriever;
import android.text.TextUtils;
import android.webkit.URLUtil;

import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;

import com.yanzhenjie.album.util.AlbumUtils;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.HashMap;

/**
 * 缩略图构建工具类（核心）
 * 功能：为图片/视频生成压缩、旋转正确的缩略图，并缓存到本地
 * 解决大图卡顿、图片方向错误问题
 */
public class ThumbnailBuilder {
    // 缩略图缓存目录
    private final File mCacheDir;
    // 缩略图默认尺寸
    private static final int THUMBNAIL_SIZE = 360;
    // 缩略图压缩质量
    private static final int THUMBNAIL_QUALITY = 80;

    /**
     * 初始化：创建缓存文件夹
     */
    public ThumbnailBuilder(Context context) {
        this.mCacheDir = AlbumUtils.getAlbumRootPath(context);
        // 如果路径是文件，先删除
        if (mCacheDir.exists() && mCacheDir.isFile()) mCacheDir.delete();
        // 创建目录
        if (!mCacheDir.exists()) mCacheDir.mkdirs();
    }

    /**
     * 为图片生成缩略图（子线程执行）
     */
    @WorkerThread
    @Nullable
    public String createThumbnailForImage(String imagePath) {
        if (TextUtils.isEmpty(imagePath)) return null;
        File inFile = new File(imagePath);
        if (!inFile.exists()) return null;
        // 根据原路径生成唯一缓存文件名
        File thumbnailFile = randomPath(imagePath);
        // 缓存已存在，直接返回
        if (thumbnailFile.exists()) return thumbnailFile.getAbsolutePath();
        // 读取并压缩图片
        Bitmap inBitmap = readImageFromPath(imagePath, THUMBNAIL_SIZE, THUMBNAIL_SIZE);
        if (inBitmap == null) return null;
        // 压缩成 JPG
        ByteArrayOutputStream compressStream = new ByteArrayOutputStream();
        inBitmap.compress(Bitmap.CompressFormat.JPEG, THUMBNAIL_QUALITY, compressStream);
        try {
            compressStream.close();
            // 写入缓存文件
            thumbnailFile.createNewFile();
            FileOutputStream writeStream = new FileOutputStream(thumbnailFile);
            writeStream.write(compressStream.toByteArray());
            writeStream.flush();
            writeStream.close();
            return thumbnailFile.getAbsolutePath();
        } catch (Exception ignored) {
            return null;
        }
    }

    /**
     * 为视频生成缩略图（子线程执行）
     */
    @WorkerThread
    @Nullable
    public String createThumbnailForVideo(String videoPath) {
        if (TextUtils.isEmpty(videoPath)) return null;
        File thumbnailFile = randomPath(videoPath);
        if (thumbnailFile.exists()) return thumbnailFile.getAbsolutePath();
        try {
            MediaMetadataRetriever retriever = new MediaMetadataRetriever();
            // 支持网络视频 / 本地视频
            if (URLUtil.isNetworkUrl(videoPath)) {
                retriever.setDataSource(videoPath, new HashMap<>());
            } else {
                retriever.setDataSource(videoPath);
            }
            // 获取视频第一帧作为缩略图
            Bitmap bitmap = retriever.getFrameAtTime();
            thumbnailFile.createNewFile();
            bitmap.compress(Bitmap.CompressFormat.JPEG, THUMBNAIL_QUALITY, new FileOutputStream(thumbnailFile));
            return thumbnailFile.getAbsolutePath();
        } catch (Exception ignored) {
            return null;
        }
    }

    /**
     * 根据文件路径生成 MD5 作为唯一缓存名
     */
    private File randomPath(String filePath) {
        String outFilePath = AlbumUtils.getMD5ForString(filePath) + ".album";
        return new File(mCacheDir, outFilePath);
    }

    /**
     * 按目标宽高安全读取图片（防 OOM）
     * 自动计算缩放比例 + 自动纠正图片方向
     */
    @Nullable
    public static Bitmap readImageFromPath(String imagePath, int width, int height) {
        File imageFile = new File(imagePath);
        if (imageFile.exists()) {
            try {
                // 只读取图片宽高，不加载到内存
                BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream(imageFile));
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inJustDecodeBounds = true;
                BitmapFactory.decodeStream(inputStream, null, options);
                inputStream.close();
                // 计算缩放比例
                options.inJustDecodeBounds = false;
                options.inSampleSize = computeSampleSize(options, width, height);
                // 尝试加载，失败则继续放大缩放比例
                Bitmap sampledBitmap = null;
                boolean attemptSuccess = false;
                while (!attemptSuccess) {
                    inputStream = new BufferedInputStream(new FileInputStream(imageFile));
                    try {
                        sampledBitmap = BitmapFactory.decodeStream(inputStream, null, options);
                        attemptSuccess = true;
                    } catch (Exception e) {
                        options.inSampleSize *= 2;
                    }
                    inputStream.close();
                }
                // 纠正图片旋转角度（解决手机拍照图片歪的问题）
                String lowerPath = imagePath.toLowerCase();
                if (lowerPath.endsWith(".jpg") || lowerPath.endsWith(".jpeg")) {
                    int degrees = computeDegree(imagePath);
                    if (degrees > 0) {
                        Matrix matrix = new Matrix();
                        matrix.setRotate(degrees);
                        Bitmap newBitmap = Bitmap.createBitmap(sampledBitmap, 0, 0, sampledBitmap.getWidth(), sampledBitmap.getHeight(), matrix, true);
                        if (newBitmap != sampledBitmap) {
                            sampledBitmap.recycle();
                            sampledBitmap = newBitmap;
                        }
                    }
                }
                return sampledBitmap;
            } catch (Exception ignored) {
            }
        }
        return null;
    }

    /**
     * 计算图片缩放比例，防止 OOM
     */
    private static int computeSampleSize(BitmapFactory.Options options, int width, int height) {
        int inSampleSize = 1;
        if (options.outWidth > width || options.outHeight > height) {
            int widthRatio = Math.round((float) options.outWidth / (float) width);
            int heightRatio = Math.round((float) options.outHeight / (float) height);
            inSampleSize = Math.min(widthRatio, heightRatio);
        }
        return inSampleSize;
    }

    /**
     * 读取图片 EXIF 信息，获取旋转角度
     */
    private static int computeDegree(String path) {
        try {
            ExifInterface exifInterface = new ExifInterface(path);
            int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90: {
                    return 90;
                }
                case ExifInterface.ORIENTATION_ROTATE_180: {
                    return 180;
                }
                case ExifInterface.ORIENTATION_ROTATE_270: {
                    return 270;
                }
                default: {
                    return 0;
                }
            }
        } catch (Exception e) {
            return 0;
        }
    }

}