package com.yanzhenjie.album.widget.photoview;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;

/**
 * 绑定了 PhotoViewAttacher 的 ImageView
 * 作用：当图片改变时，自动通知缩放控制器刷新矩阵、缩放、位置信息
 */
public class AttacherImageView extends AppCompatImageView {
    // 图片缩放、手势、矩阵控制核心
    private PhotoViewAttacher mAttacher;

    public AttacherImageView(Context context) {
        super(context);
    }

    public AttacherImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public AttacherImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    /**
     * 重写设置图片方法
     * 当图片改变时，自动通知 PhotoViewAttacher 刷新
     */
    @Override
    public void setImageDrawable(@Nullable Drawable drawable) {
        super.setImageDrawable(drawable);
        if (mAttacher != null) {
            mAttacher.update();
        }
    }

    /**
     * 绑定 PhotoView 控制器
     *
     * @param attacher 手势缩放、矩阵计算核心类
     */
    public void setAttacher(PhotoViewAttacher attacher) {
        this.mAttacher = attacher;
    }

}