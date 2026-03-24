package com.yanzhenjie.durban.callback;

import android.graphics.RectF;

/**
 * 裁剪覆盖视图变化监听器
 * 作用：监听裁剪框（矩形选区）位置、大小发生实时变化时回调
 */
public interface OverlayViewChangeListener {

    /**
     * 裁剪框矩形已更新
     * @param cropRect 最新的裁剪框矩形坐标（左、上、右、下）
     */
    void onCropRectUpdated(RectF cropRect);

}