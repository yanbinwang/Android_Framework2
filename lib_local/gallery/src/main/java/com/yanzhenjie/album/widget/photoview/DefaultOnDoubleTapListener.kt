package com.yanzhenjie.album.widget.photoview;

import android.graphics.RectF;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.widget.ImageView;

import androidx.annotation.NonNull;

/**
 * PhotoView 默认的双击事件监听器
 * 负责处理：单击确认、双击缩放、点击图片/点击外部事件
 * 可通过 setOnDoubleTapListener 覆盖自定义逻辑
 */
public class DefaultOnDoubleTapListener implements GestureDetector.OnDoubleTapListener {
    // 图片缩放核心控制器
    private PhotoViewAttacher photoViewAttacher;

    /**
     * 构造方法，绑定 PhotoView 控制器
     *
     * @param photoViewAttacher 图片控制器
     */
    public DefaultOnDoubleTapListener(PhotoViewAttacher photoViewAttacher) {
        setPhotoViewAttacher(photoViewAttacher);
    }

    /**
     * 重新绑定 PhotoView 控制器（复用监听器时使用）
     *
     * @param newPhotoViewAttacher 新的控制器
     */
    public void setPhotoViewAttacher(PhotoViewAttacher newPhotoViewAttacher) {
        this.photoViewAttacher = newPhotoViewAttacher;
    }

    /**
     * 单击确认（系统确保不是双击）
     * 作用：区分 点击图片 / 点击外部
     */
    @Override
    public boolean onSingleTapConfirmed(@NonNull MotionEvent e) {
        if (this.photoViewAttacher == null) {
            return false;
        }
        ImageView imageView = photoViewAttacher.getImageView();
        // 处理【点击图片】监听
        if (null != photoViewAttacher.getOnPhotoTapListener()) {
            final RectF displayRect = photoViewAttacher.getDisplayRect();
            if (null != displayRect) {
                final float x = e.getX(), y = e.getY();
                // 判断点击坐标是否在图片范围内
                if (displayRect.contains(x, y)) {
                    // 计算点击位置相对于图片的百分比坐标
                    float xResult = (x - displayRect.left) / displayRect.width();
                    float yResult = (y - displayRect.top) / displayRect.height();
                    photoViewAttacher.getOnPhotoTapListener().onPhotoTap(imageView, xResult, yResult);
                    return true;
                } else {
                    // 点击了图片外部
                    photoViewAttacher.getOnPhotoTapListener().onOutsidePhotoTap();
                }
            }
        }
        // 处理【点击控件任意区域】监听
        if (null != photoViewAttacher.getOnViewTapListener()) {
            photoViewAttacher.getOnViewTapListener().onViewTap(imageView, e.getX(), e.getY());
        }
        return false;
    }

    /**
     * 双击事件
     * 作用：实现 双击放大 → 再双击放大 → 再双击还原 的循环逻辑
     */
    @Override
    public boolean onDoubleTap(@NonNull MotionEvent ev) {
        if (photoViewAttacher == null) {
            return false;
        }
        try {
            // 获取当前缩放值
            float scale = photoViewAttacher.getScale();
            // 获取双击坐标（以该点为中心缩放）
            float x = ev.getX();
            float y = ev.getY();
            // 三级缩放逻辑
            if (scale < photoViewAttacher.getMediumScale()) {
                // 当前 < 中等 → 缩放到中等
                photoViewAttacher.setScale(photoViewAttacher.getMediumScale(), x, y, true);
            } else if (scale >= photoViewAttacher.getMediumScale() && scale < photoViewAttacher.getMaximumScale()) {
                // 当前在中等与最大之间 → 缩放到最大
                photoViewAttacher.setScale(photoViewAttacher.getMaximumScale(), x, y, true);
            } else {
                // 当前 >= 最大 → 缩放到最小（还原）
                photoViewAttacher.setScale(photoViewAttacher.getMinimumScale(), x, y, true);
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            // 部分机型/版本在获取坐标时可能触发的系统BUG，直接忽略防崩溃
        }
        return true;
    }

    /**
     * 双击过程中的触摸事件（按下/抬起）
     * 这里不需要处理，交给 onDoubleTap 统一处理
     */
    @Override
    public boolean onDoubleTapEvent(@NonNull MotionEvent e) {
        return false;
    }

}