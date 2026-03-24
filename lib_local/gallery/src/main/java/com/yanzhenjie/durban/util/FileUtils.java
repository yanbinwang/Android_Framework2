package com.yanzhenjie.durban.util;

import android.graphics.Bitmap;

import androidx.annotation.NonNull;

import com.yanzhenjie.durban.error.StorageError;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Random;

/**
 * 文件操作工具类
 * 作用：目录创建、文件复制、流关闭、生成随机图片文件名
 */
public class FileUtils {
    private static final Random random = new Random();

    /**
     * 私有构造，禁止实例化
     */
    private FileUtils() {
    }

    /**
     * 校验并创建目录（不存在则创建）
     */
    public static void validateDirectory(String path) throws StorageError {
        File file = new File(path);
        try {
            // 如果是文件，先删除
            if (file.isFile()) {
                file.delete();
            }
            // 目录不存在则创建
            if (!file.exists()) {
                file.mkdirs();
            }
        } catch (Exception e) {
            throw new StorageError("Directory creation failed.");
        }
    }

    /**
     * 生成随机图片文件名（时间戳 + 随机数）
     */
    public static String randomImageName(Bitmap.CompressFormat format) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd_HHmmSSS", Locale.getDefault());
        Date curDate = new Date(System.currentTimeMillis());
        return formatter.format(curDate) + random.nextInt(9000) + "." + format;
    }

    /**
     * 文件复制（无需裁剪时直接复制原图）
     */
    public static void copyFile(@NonNull String pathFrom, @NonNull String pathTo) throws StorageError {
        try {
            FileInputStream inputStream = new FileInputStream(pathFrom);
            FileOutputStream outputStream = new FileOutputStream(pathTo);
            int len;
            byte[] buffer = new byte[2048];
            while ((len = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, len);
            }
        } catch (IOException e) {
            throw new StorageError(e);
        }
    }

    /**
     * 安全关闭流，避免IOException
     */
    public static void close(Closeable c) {
        if (c != null) {
            try {
                c.close();
            } catch (IOException ignored) {
            }
        }
    }

}