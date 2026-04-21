package com.yanzhenjie.durban.widget.crop;

/**
 * 裁剪框比例变化监听器
 * 作用：监听裁剪框的宽高比发生改变时回调
 */
public interface CropBoundsChangeListener {

    /**
     * 裁剪框宽高比已改变
     * @param cropRatio 新的宽高比 = 宽度 / 高度
     */
    void onCropAspectRatioChanged(float cropRatio);

}