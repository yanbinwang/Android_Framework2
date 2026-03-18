package com.yanzhenjie.durban.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Point;
import android.media.ExifInterface;
import android.os.Build;
import android.view.Display;
import android.view.WindowManager;

import androidx.annotation.NonNull;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * 图片加载工具类
 * 功能：缩放计算、EXIF 旋转解析、图片变换、最大尺寸计算
 */
public class BitmapLoadUtils {

    /**
     * 私有构造，禁止实例化
     */
    private BitmapLoadUtils() {
    }

    /**
     * 对图片执行矩阵变换（旋转、翻转）
     */
    public static Bitmap transformBitmap(@NonNull Bitmap bitmap, @NonNull Matrix transformMatrix) {
        try {
            return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), transformMatrix, true);
        } catch (OutOfMemoryError error) {
            return bitmap;
        }
    }

    /**
     * 计算图片缩放比例 inSampleSize
     */
    public static int calculateInSampleSize(@NonNull BitmapFactory.Options options, int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;
        if (height > reqHeight || width > reqWidth) {
            while ((height / inSampleSize) > reqHeight || (width / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }

    /**
     * 获取图片的 EXIF 方向
     */
    public static int getExifOrientation(@NonNull String imagePath) {
        int orientation = ExifInterface.ORIENTATION_UNDEFINED;
        try {
            InputStream stream = new FileInputStream(imagePath);
            orientation = new ImageHeaderParser(stream).getOrientation();
            stream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return orientation;
    }

    /**
     * 将 EXIF 方向 转为 旋转角度
     */
    public static int exifToDegrees(int exifOrientation) {
        int rotation;
        switch (exifOrientation) {
            case ExifInterface.ORIENTATION_ROTATE_90:
            case ExifInterface.ORIENTATION_TRANSPOSE:
                rotation = 90;
                break;
            case ExifInterface.ORIENTATION_ROTATE_180:
            case ExifInterface.ORIENTATION_FLIP_VERTICAL:
                rotation = 180;
                break;
            case ExifInterface.ORIENTATION_ROTATE_270:
            case ExifInterface.ORIENTATION_TRANSVERSE:
                rotation = 270;
                break;
            default:
                rotation = 0;
        }
        return rotation;
    }

    /**
     * 将 EXIF 方向 转为 水平翻转值
     */
    public static int exifToTranslation(int exifOrientation) {
        int translation;
        switch (exifOrientation) {
            case ExifInterface.ORIENTATION_FLIP_HORIZONTAL:
            case ExifInterface.ORIENTATION_FLIP_VERTICAL:
            case ExifInterface.ORIENTATION_TRANSPOSE:
            case ExifInterface.ORIENTATION_TRANSVERSE:
                translation = -1;
                break;
            default:
                translation = 1;
        }
        return translation;
    }

    /**
     * 计算图片最大安全加载尺寸（屏幕对角线 * 2，不超过 GL 最大纹理）
     */
    public static int calculateMaxBitmapSize(@NonNull Context context) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point size = new Point();
        int width, height;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            display.getSize(size);
            width = size.x;
            height = size.y;
        } else {
            width = display.getWidth();
            height = display.getHeight();
        }
        // 屏幕对角线长度
        int maxBitmapSize = (int) Math.sqrt(Math.pow(width, 2) + Math.pow(height, 2));
        // 不超过 OpenGL 最大纹理限制
        final int maxTextureSize = EglUtils.getMaxTextureSize();
        if (maxTextureSize > 0) {
            maxBitmapSize = Math.min(maxBitmapSize, maxTextureSize);
        }
        return maxBitmapSize;
    }

}