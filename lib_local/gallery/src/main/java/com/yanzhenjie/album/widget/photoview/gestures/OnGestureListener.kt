package com.yanzhenjie.album.widget.photoview.gestures;

/**
 * 图片手势操作监听接口
 * 定义：拖拽、快速滑动、双指缩放 的回调方法
 */
public interface OnGestureListener {

    /**
     * 拖拽（手指拖动图片）
     * @param dx 水平方向拖动距离
     * @param dy 垂直方向拖动距离
     */
    void onDrag(float dx, float dy);

    /**
     * 快速滑动（手指甩动图片产生惯性）
     * @param startX    滑动起点X
     * @param startY    滑动起点Y
     * @param velocityX X方向惯性速度
     * @param velocityY Y方向惯性速度
     */
    void onFling(float startX, float startY, float velocityX, float velocityY);

    /**
     * 缩放（双指缩放图片）
     * @param scaleFactor 缩放比例系数
     * @param focusX      缩放中心点X
     * @param focusY      缩放中心点Y
     */
    void onScale(float scaleFactor, float focusX, float focusY);

}