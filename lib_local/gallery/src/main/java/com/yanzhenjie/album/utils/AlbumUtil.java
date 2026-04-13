package com.yanzhenjie.album.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.webkit.MimeTypeMap;

import androidx.annotation.ColorInt;
import androidx.annotation.IntRange;
import androidx.annotation.NonNull;
import androidx.core.graphics.drawable.DrawableCompat;

import com.yanzhenjie.album.provider.CameraFileProvider;
import com.yanzhenjie.album.widget.recyclerview.divider.Divider;
import com.yanzhenjie.album.widget.recyclerview.divider.ItemDivider;

import java.io.File;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

/**
 * 相册工具类
 * 功能：文件路径、拍照、录视频、时间格式化、MD5、Drawable 着色、时间转换等
 * Android 从 10 开始，定义了「公共媒体目录」 所有应用都可以自由写入，不需要权限 (/storage/emulated/0/DCIM)
 * 公共媒体目录 -> DCIM / Pictures / Download / Movies
 */
public class AlbumUtil {
    // 相册缓存文件夹名称
    private static final String CACHE_DIRECTORY = "AlbumCache";

    /**
     * 私有化构造，禁止实例化
     */
    private AlbumUtil() {
    }

    /**
     * 获取相册根目录（优先SD卡，否则内部存储）
     */
    @NonNull
    public static File getAlbumRootPath(Context context) {
//        if (sdCardIsAvailable()) {
//            // -> /storage/emulated/0/AlbumCache
//            return new File(Environment.getExternalStorageDirectory(), CACHE_DIRECTORY);
//        } else {
//            // -> /data/data/你的包名/files/CACHE_DIRECTORY
//            return new File(context.getFilesDir(), CACHE_DIRECTORY);
//        }
        return new File(context.getCacheDir() + "/" + CACHE_DIRECTORY);
    }

    /**
     * 判断SD卡是否可用
     */
    public static boolean sdCardIsAvailable() {
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            return Environment.getExternalStorageDirectory().canWrite();
        } else {
            return false;
        }
    }

    /**
     * 调用系统相机拍照
     */
    public static void takeImage(@NonNull Activity activity, int requestCode, File outPath) {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        Uri uri = getUri(activity, outPath);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        activity.startActivityForResult(intent, requestCode);
    }

    /**
     * 调用系统相机录像
     */
    public static void takeVideo(@NonNull Activity activity, int requestCode, File outPath, @IntRange(from = 0, to = 1) int quality, @IntRange(from = 1) long duration, @IntRange(from = 1) long limitBytes) {
        Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        Uri uri = getUri(activity, outPath);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
        intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, quality);
        intent.putExtra(MediaStore.EXTRA_DURATION_LIMIT, duration);
        intent.putExtra(MediaStore.EXTRA_SIZE_LIMIT, limitBytes);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        activity.startActivityForResult(intent, requestCode);
    }

    /**
     * 根据文件获取 Uri（兼容 7.0 FileProvider）
     */
    @NonNull
    public static Uri getUri(@NonNull Context context, @NonNull File outPath) {
        Uri uri;
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            uri = Uri.fromFile(outPath);
        } else {
            uri = CameraFileProvider.getUriForFile(context, CameraFileProvider.getProviderName(context), outPath);
        }
        return uri;
    }

    /**
     * 生成随机 JPG 路径
     */
    @NonNull
    public static String randomJPGPath(Context context) {
        // 判断 SD 卡是否正常挂载（手机存储是否可用）
        if (!sdCardIsAvailable()) {
            // SD 卡不可用（极少见）→ 存到 APP 自身缓存目录（/data/data/你的应用包名/cache/）
            return randomJPGPath(context.getCacheDir());
        }
        return randomJPGPath();
    }

    /**
     * 生成随机 JPG 路径（废弃）
     */
    @NonNull
    public static String randomJPGPath() {
        File bucket = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
        return randomJPGPath(bucket);
    }

    /**
     * 在指定目录生成随机 JPG 路径
     */
    @NonNull
    public static String randomJPGPath(File bucket) {
        return randomMediaPath(bucket, ".jpg");
    }

    /**
     * 生成随机 MP4 路径（废弃）
     */
    @NonNull
    public static String randomMP4Path() {
        File bucket = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES);
        return randomMP4Path(bucket);
    }

    /**
     * 生成随机 MP4 路径
     */
    @NonNull
    public static String randomMP4Path(Context context) {
        if (!sdCardIsAvailable()) {
            return randomMP4Path(context.getCacheDir());
        }
        return randomMP4Path();
    }

    /**
     * 在指定目录生成随机 MP4 路径
     */
    @NonNull
    public static String randomMP4Path(File bucket) {
        return randomMediaPath(bucket, ".mp4");
    }

    /**
     * 根据目录和后缀生成随机文件路径
     */
    @NonNull
    private static String randomMediaPath(File bucket, String extension) {
        if (bucket.exists() && bucket.isFile()) {
            bucket.delete();
        }
        if (!bucket.exists()) {
            bucket.mkdirs();
        }
        String outFilePath = AlbumUtil.getNowDateTime("yyyyMMdd_HHmmssSSS") + "_" + getMD5ForString(UUID.randomUUID().toString()) + extension;
        File file = new File(bucket, outFilePath);
        return file.getAbsolutePath();
    }

    /**
     * 获取当前时间格式化字符串
     */
    @NonNull
    public static String getNowDateTime(@NonNull String format) {
        SimpleDateFormat formatter = new SimpleDateFormat(format, Locale.ENGLISH);
        Date curDate = new Date(System.currentTimeMillis());
        return formatter.format(curDate);
    }

    /**
     * 获取文件 MimeType
     */
    public static String getMimeType(String url) {
        String extension = getExtension(url);
        if (!MimeTypeMap.getSingleton().hasExtension(extension)) return "";
        String mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
        return TextUtils.isEmpty(mimeType) ? "" : mimeType;
    }

    /**
     * 获取文件后缀
     */
    public static String getExtension(String url) {
        url = TextUtils.isEmpty(url) ? "" : url.toLowerCase();
        String extension = MimeTypeMap.getFileExtensionFromUrl(url);
        return TextUtils.isEmpty(extension) ? "" : extension;
    }

    /**
     * 给 Drawable 设置着色
     */
    public static void setDrawableTint(@NonNull Drawable drawable, @ColorInt int color) {
        DrawableCompat.setTint(DrawableCompat.wrap(drawable.mutate()), color);
    }

    /**
     * 获取着色后的 Drawable
     */
    @NonNull
    public static Drawable getTintDrawable(@NonNull Drawable drawable, @ColorInt int color) {
        drawable = DrawableCompat.wrap(drawable.mutate());
        DrawableCompat.setTint(drawable, color);
        return drawable;
    }

    /**
     * 构建按钮/选择器的颜色状态
     */
    public static ColorStateList getColorStateList(@ColorInt int normal, @ColorInt int highLight) {
        int[][] states = new int[6][];
        states[0] = new int[]{android.R.attr.state_checked};
        states[1] = new int[]{android.R.attr.state_pressed};
        states[2] = new int[]{android.R.attr.state_selected};
        states[3] = new int[]{};
        states[4] = new int[]{};
        states[5] = new int[]{};
        int[] colors = new int[]{highLight, highLight, highLight, normal, normal, normal};
        return new ColorStateList(states, colors);
    }

    /**
     * 设置字符串部分文字颜色
     */
    @NonNull
    public static SpannableString getColorText(@NonNull CharSequence content, int start, int end, @ColorInt int color) {
        SpannableString stringSpan = new SpannableString(content);
        stringSpan.setSpan(new ForegroundColorSpan(color), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        return stringSpan;
    }

    /**
     * 给颜色设置透明度
     */
    @ColorInt
    public static int getAlphaColor(@ColorInt int color, @IntRange(from = 0, to = 255) int alpha) {
        int red = Color.red(color);
        int green = Color.green(color);
        int blue = Color.blue(color);
        return Color.argb(alpha, red, green, blue);
    }

    /**
     * 获取列表分割线
     */
    public static Divider getDivider(@ColorInt int color) {
        return new ItemDivider(color);
    }

    /**
     * 把毫秒时长转换成 00:00 / 00:00:00 格式
     */
    @NonNull
    public static String convertDuration(@IntRange(from = 1) long duration) {
        duration /= 1000;
        int hour = (int) (duration / 3600);
        int minute = (int) ((duration - hour * 3600) / 60);
        int second = (int) (duration - hour * 3600 - minute * 60);
        String hourValue = "";
        String minuteValue;
        String secondValue;
        if (hour > 0) {
            if (hour >= 10) {
                hourValue = Integer.toString(hour);
            } else {
                hourValue = "0" + hour;
            }
            hourValue += ":";
        }
        if (minute > 0) {
            if (minute >= 10) {
                minuteValue = Integer.toString(minute);
            } else {
                minuteValue = "0" + minute;
            }
        } else {
            minuteValue = "00";
        }
        minuteValue += ":";
        if (second > 0) {
            if (second >= 10) {
                secondValue = Integer.toString(second);
            } else {
                secondValue = "0" + second;
            }
        } else {
            secondValue = "00";
        }
        return hourValue + minuteValue + secondValue;
    }

    /**
     * 获取字符串 MD5
     */
    public static String getMD5ForString(String content) {
        StringBuilder md5Buffer = new StringBuilder();
        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            byte[] tempBytes = digest.digest(content.getBytes());
            int digital;
            for (byte tempByte : tempBytes) {
                digital = tempByte;
                if (digital < 0) {
                    digital += 256;
                }
                if (digital < 16) {
                    md5Buffer.append("0");
                }
                md5Buffer.append(Integer.toHexString(digital));
            }
        } catch (Exception ignored) {
            return Integer.toString(content.hashCode());
        }
        return md5Buffer.toString();
    }

}