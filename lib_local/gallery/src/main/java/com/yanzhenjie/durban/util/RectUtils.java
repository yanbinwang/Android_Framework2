package com.yanzhenjie.durban.util;

import android.graphics.RectF;

/**
 * 矩形坐标计算工具类
 * 作用：裁剪视图中计算矩形、角点、中心点、包围盒等几何逻辑
 */
public class RectUtils {

    /**
     * 私有构造，禁止实例化
     */
    private RectUtils() {
    }

    /**
     * 从矩形获取四个角点坐标
     * 角点顺序：左上 → 右上 → 右下 → 左下
     */
    public static float[] getCornersFromRect(RectF r) {
        return new float[]{r.left, r.top, r.right, r.top, r.right, r.bottom, r.left, r.bottom};
    }

    /**
     * 从角点坐标计算矩形的宽和高
     */
    public static float[] getRectSidesFromCorners(float[] corners) {
        return new float[]{(float) Math.sqrt(Math.pow(corners[0] - corners[2], 2) + Math.pow(corners[1] - corners[3], 2)), (float) Math.sqrt(Math.pow(corners[2] - corners[4], 2) + Math.pow(corners[3] - corners[5], 2))};
    }

    /**
     * 获取矩形中心点坐标
     */
    public static float[] getCenterFromRect(RectF r) {
        return new float[]{r.centerX(), r.centerY()};
    }

    /**
     * 根据一组坐标点，计算能包裹它们的最小矩形
     */
    public static RectF trapToRect(float[] array) {
        RectF r = new RectF(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY, Float.NEGATIVE_INFINITY, Float.NEGATIVE_INFINITY);
        for (int i = 1; i < array.length; i += 2) {
            float x = Math.round(array[i - 1] * 10) / 10.f;
            float y = Math.round(array[i] * 10) / 10.f;
            r.left = Math.min(x, r.left);
            r.top = Math.min(y, r.top);
            r.right = Math.max(x, r.right);
            r.bottom = Math.max(y, r.bottom);
        }
        r.sort();
        return r;
    }

}