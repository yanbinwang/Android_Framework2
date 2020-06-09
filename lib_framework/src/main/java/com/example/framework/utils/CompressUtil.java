package com.example.framework.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;

import com.example.framework.utils.LogUtil;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author wyb
 * 图片压缩工具类
 */
public class CompressUtil {
    private static int defaultLength = 512;

    public static ByteArrayOutputStream compressImg(Bitmap image) {
        return compressImg(image, defaultLength);
    }

    public static ByteArrayOutputStream compressImg(Bitmap bitmap, int length) {
        bitmap = compressImgBySize(bitmap);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
        int options = 100;
        while (byteArrayOutputStream.toByteArray().length / 1024 > length) {
            byteArrayOutputStream.reset();
            options -= 10;
            bitmap.compress(Bitmap.CompressFormat.JPEG, options, byteArrayOutputStream);
        }
        LogUtil.INSTANCE.e("length", byteArrayOutputStream.toByteArray().length + "");
        return byteArrayOutputStream;
    }

    public static Bitmap compressImgBySize(Bitmap bitmap) {
        float size = 1f;
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        Matrix matrix = new Matrix();
        if (width > 720) {
            size = 720f / width;
            LogUtil.INSTANCE.e("w", bitmap.getWidth() + "");
            LogUtil.INSTANCE.e("h", bitmap.getHeight() + "");
        } else if (height > 1280) {
            size = 1280f / height;
            LogUtil.INSTANCE.e("w", bitmap.getWidth() + "");
            LogUtil.INSTANCE.e("h", bitmap.getHeight() + "");
        }
        matrix.postScale(size, size);
        bitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true);
        return bitmap;
    }

    public static File scale(Context context, File mFile) {
        final long fileMaxSize = 100 * 1024;
        return scale(context, mFile, fileMaxSize);
    }

    public static File scale(Context context, File mFile, long fileMaxSize) {
        long fileSize = mFile.length();
        float scaleSize = 1;
        LogUtil.INSTANCE.e("fileSize_old", mFile.length() / 1024 + "kb");
        if (fileSize >= fileMaxSize) {
            try {
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inJustDecodeBounds = true;
                Bitmap bitmap;
                int width = options.outWidth;
                int height = options.outHeight;
                if (width > 720) {
                    scaleSize = width / 480;
                } else if (height > 1280) {
                    scaleSize = width / 1080;
                }
                options.inJustDecodeBounds = false;
                options.inSampleSize = (int) (scaleSize + 0.5);
                bitmap = BitmapFactory.decodeFile(mFile.getPath(), options);
                bitmap = compressImgBySize(bitmap);

                File fTemp = new File(context.getApplicationContext().getExternalCacheDir(), System.currentTimeMillis() + "img.jpg");
                FileOutputStream fileOutputStream;
                try {
                    fileOutputStream = new FileOutputStream(fTemp);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                    return mFile;
                }
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream);

                fileOutputStream.flush();
                fileOutputStream.close();
                bitmap.recycle();
                LogUtil.INSTANCE.e("fileSize", fTemp.length() / 1024 + "kb");
                return fTemp;
            } catch (IOException e) {
                e.printStackTrace();
                return mFile;
            }
        } else {
            LogUtil.INSTANCE.e("fileSize", mFile.length() / 1024 + "kb");
            return mFile;
        }
    }

    //直接压缩图片
    public static Bitmap scale(Context context, Bitmap bitmap) {
        long fileSize = getBitmapSize(bitmap);
        float scaleSize = 1;
        final long fileMaxSize = 100 * 1024;
        if (fileSize >= fileMaxSize) {
            try {
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inJustDecodeBounds = true;
                int width = options.outWidth;
                int height = options.outHeight;
                if (width > 720) {
                    scaleSize = width / 480;
                } else if (height > 1280) {
                    scaleSize = width / 1080;
                }
                options.inJustDecodeBounds = false;
                options.inSampleSize = (int) (scaleSize + 0.5);
                bitmap = compressImgBySize(bitmap);

                File file = new File(context.getApplicationContext().getExternalCacheDir(), System.currentTimeMillis() + "img.jpg");
                FileOutputStream fileOutputStream;
                try {
                    fileOutputStream = new FileOutputStream(file);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                    return bitmap;
                }
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream);
                fileOutputStream.flush();
                fileOutputStream.close();
                return bitmap;
            } catch (IOException e) {
                return bitmap;
            }
        } else {
            return bitmap;
        }
    }

    public static int getBitmapSize(Bitmap bitmap) {
        return bitmap.getAllocationByteCount();
    }

    public static byte[] getBytes(InputStream inputStream) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024]; // 用数据装
        int len;
        while ((len = inputStream.read(buffer)) != -1) {
            byteArrayOutputStream.write(buffer, 0, len);
        }
        byteArrayOutputStream.close();
        return byteArrayOutputStream.toByteArray();
    }

    public static File degreeImage(Context context, File mFile) {
        int degree = readImageDegree(mFile.getPath());
        Bitmap bitmap;
        if (degree != 0) {
            //旋转图片
            Matrix matrix = new Matrix();
            matrix.postRotate(degree);
            bitmap = BitmapFactory.decodeFile(mFile.getPath());
            bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
            File fTemp = new File(context.getApplicationContext().getExternalCacheDir(), System.currentTimeMillis() + "img.jpg");
            FileOutputStream fileOutputStream;
            try {
                fileOutputStream = new FileOutputStream(fTemp);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream);
                fileOutputStream.flush();
                fileOutputStream.close();
                bitmap.recycle();
                return fTemp;
            } catch (IOException e) {
                e.printStackTrace();
                return mFile;
            }
        } else {
            return mFile;
        }
    }

    //读取图片的方向
    private static int readImageDegree(String path) {
        int degree = 0;
        // 读取图片文件信息的类ExifInterface
        ExifInterface exifInterface = null;
        try {
            exifInterface = new ExifInterface(path);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (exifInterface != null) {
            int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    degree = 90;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    degree = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    degree = 270;
                    break;
            }
        }
        return degree;
    }

}