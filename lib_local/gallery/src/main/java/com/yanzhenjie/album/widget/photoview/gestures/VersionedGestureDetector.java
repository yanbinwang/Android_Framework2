package com.yanzhenjie.album.widget.photoview.gestures;

import android.content.Context;
import android.os.Build;

/**
 * 手势检测器版本兼容工具类
 * 根据系统版本自动创建对应版本的手势检测器
 * 解决旧版本系统手势识别差异问题
 */
public final class VersionedGestureDetector {

    /**
     * 根据当前系统版本，创建最合适的手势检测器
     * @param context 上下文
     * @param listener 手势监听回调
     * @return 适配当前系统版本的手势检测器
     */
    public static GestureDetector newInstance(Context context, OnGestureListener listener) {
        final int sdkVersion = Build.VERSION.SDK_INT;
        GestureDetector detector;
        // Android 1.5 - 2.1（非常古老的系统）
        if (sdkVersion < Build.VERSION_CODES.ECLAIR) {
            detector = new CupcakeGestureDetector(context);
            // Android 2.1 - 2.2（旧系统）
        } else if (sdkVersion < Build.VERSION_CODES.FROYO) {
            detector = new EclairGestureDetector(context);
            // Android 2.2 及以上（现代系统）
        } else {
            detector = new FroyoGestureDetector(context);
        }
        // 设置手势监听
        detector.setOnGestureListener(listener);
        return detector;
    }

}