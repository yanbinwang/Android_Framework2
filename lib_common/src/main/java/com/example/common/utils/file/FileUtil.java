package com.example.common.utils.file;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;

import androidx.core.content.FileProvider;

import com.example.common.BaseApplication;
import com.example.common.constant.Constants;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.nio.channels.FileChannel;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;


/**
 * 文件相关工具类
 *
 * @author yunye
 */
public class FileUtil {

    //创建日志和缓存目录
    public static String createCacheDir() {
        File file = new File(BaseApplication.getInstance().getExternalCacheDir() + File.separator + "log");
        if (!file.exists()) {
            file.mkdir();
        }
        return file.getPath();
    }

    //复制文件
    public static void copyFile(String srcFile, String destFile) throws IOException {
        copyFile(new File(srcFile), new File(destFile));
    }

    public static void copyFile(File srcFile, File destFile) throws IOException {
        if (!destFile.exists()) {
            destFile.createNewFile();
        }

        try (FileChannel source = new FileInputStream(srcFile).getChannel(); FileChannel destination = new FileOutputStream(destFile).getChannel()) {
            destination.transferFrom(source, 0, source.size());
        }
    }

    //删除本地路径下的所有文件
    public static void deleteDir(String filePath) {
        File dir = new File(filePath);
        deleteDirWithFile(dir);
    }

    public static void deleteDirWithFile(File dir) {
        if (dir == null || !dir.exists() || !dir.isDirectory())
            return;
        for (File file : dir.listFiles()) {
            if (file.isFile())
                file.delete(); // 删除所有文件
            else if (file.isDirectory())
                deleteDirWithFile(file); // 递规的方式删除文件夹
        }
        dir.delete();// 删除目录本身
    }

    //判断下载目录是否存在
    public static String isExistDir(String filePath) throws IOException {
        File downloadFile = new File(filePath);
        if (!downloadFile.mkdirs()) {
            downloadFile.createNewFile();
        }
        return downloadFile.getAbsolutePath();
    }

    //读取文件到文本（文本，找不到文件或读取错返回null）
    public static String readText(String filePath) {
        File f = new File(filePath);
        if (f.exists()) {
            try {
                StringBuilder sb = new StringBuilder();
                String s;
                BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(f)));
                while ((s = br.readLine()) != null) {
                    sb.append(s);
                }
                return sb.toString();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    //获取文件大小
    public static long getFileSize(File file) {
        long size = 0;
        File[] fileList = file.listFiles();
        for (File mFile : fileList) {
            if (mFile.isDirectory()) {
                size = size + getFileSize(mFile);
            } else {
                size = size + mFile.length();
            }
        }
        return size;
    }

    //转换文件大小格式
    public static String formatFileSize(long fileS) {
        DecimalFormat df = new DecimalFormat("#.00");
        String fileSizeString;
        if (fileS < 1024) {
            fileSizeString = df.format((double) fileS) + "B";
        } else if (fileS < 1048576) {
            fileSizeString = df.format((double) fileS / 1024) + "K";
        } else if (fileS < 1073741824) {
            fileSizeString = df.format((double) fileS / 1048576) + "M";
        } else {
            fileSizeString = df.format((double) fileS / 1073741824) + "G";
        }
        return fileSizeString;
    }

    //将Bitmap缓存到本地
    public static void saveBitmap(Bitmap bitmap) {
        String screenImagePath;
        //输出
        try {
            String rootDir = Constants.BASE_PATH.getAbsolutePath() + "/" + Constants.APPLICATION_NAME + "/截屏";
            File downloadFile = new File(rootDir);
            if (!downloadFile.mkdirs()) {
                //需要权限
                downloadFile.createNewFile();
            }
            screenImagePath = rootDir + "/screen_capture" + new SimpleDateFormat("yyyy_MM_dd_hh_mm_ss", Locale.getDefault()).format(new Date()) + ".png";

            FileOutputStream fileOutputStream = new FileOutputStream(screenImagePath);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream);
            fileOutputStream.flush();
            fileOutputStream.close();
        } catch (Exception ignored) {
        } finally {
            bitmap.recycle();
        }
    }

    //判断sd卡是否存在
    public static boolean hasSDCard() {
        return Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState());
    }

    //获取sdcard根目录
    public static String getSdCardPath() {
        return Environment.getExternalStorageDirectory().getAbsolutePath();
    }

    //获取当前app的应用程序名称
    public static String getApplicationName(Context context) {
        PackageManager packageManager = null;
        ApplicationInfo applicationInfo;
        try {
            packageManager = context.getApplicationContext().getPackageManager();
            applicationInfo = packageManager.getApplicationInfo(context.getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            applicationInfo = null;
        }
        return (String) packageManager.getApplicationLabel(applicationInfo);
    }

    //获取当前app的应用程序名称
    public static String getApplicationId(Context context) {
        if (context != null) {
            return context.getPackageName();
        }
        return null;
    }

    //是否安装了XXX
    public static boolean isAvailable(Context context, String packageName) {
        PackageManager packageManager = context.getPackageManager();
        List<PackageInfo> packageInfos = packageManager.getInstalledPackages(0);
        if (packageInfos != null) {
            for (int i = 0; i < packageInfos.size(); i++) {
                String pn = packageInfos.get(i).packageName;
                if (pn.equals(packageName)) {
                    return true;
                }
            }
        }
        return false;
    }

    //获取app的图标
    public static Bitmap getApplicationIcon(Context context) {
        try {
            Drawable drawable = context.getPackageManager().getApplicationIcon(Constants.APPLICATION_ID);
            SoftReference<Bitmap> bitmap = new SoftReference<>(Bitmap.createBitmap(
                    drawable.getIntrinsicWidth(),
                    drawable.getIntrinsicHeight(),
                    drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888 : Bitmap.Config.RGB_565));
            Canvas canvas = new Canvas(bitmap.get());
            //canvas.setBitmap(bitmap);
            drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
            drawable.draw(canvas);
            return bitmap.get();
        } catch (Exception ignored) {
        }
        return null;
    }

    //获取安装跳转的行为
    private Intent getSetupApk(Activity activity, String apkFilePath) {
        WeakReference<Activity> mActivity = new WeakReference<>(activity);
        Intent intent = new Intent(Intent.ACTION_VIEW);
        //判断是否是AndroidN以及更高的版本
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            File file = new File(apkFilePath);
            Uri contentUri = FileProvider.getUriForFile(mActivity.get(), Constants.APPLICATION_ID + ".fileProvider", file);
            intent.setDataAndType(contentUri, "application/vnd.android.package-archive");
        } else {
            intent.setDataAndType(Uri.parse("file://" + apkFilePath), "application/vnd.android.package-archive");
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        return intent;
    }

}