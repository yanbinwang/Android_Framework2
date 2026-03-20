package com.yanzhenjie.album.app.gallery;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

import com.yanzhenjie.album.widget.photoview.AttacherImageView;
import com.yanzhenjie.album.widget.photoview.PhotoViewAttacher;

import java.util.List;

/**
 * 图片预览适配器（给 ViewPager 用）
 * 基类适配器，专门用于预览大图，支持：
 * 点击、长按、缩放（PhotoView）
 * 子类只需要实现图片加载逻辑即可
 */
public abstract class PreviewAdapter<T> extends PagerAdapter implements PhotoViewAttacher.OnViewTapListener, View.OnLongClickListener {
    // 单击监听
    private View.OnClickListener mItemClickListener;
    // 长按监听
    private View.OnClickListener mItemLongClickListener;
    // 上下文
    private final Context mContext;
    // 预览数据集合
    private final List<T> mPreviewList;

    public PreviewAdapter(Context context, List<T> previewList) {
        mContext = context;
        mPreviewList = previewList;
    }

    /**
     * 条目数量
     */
    @Override
    public int getCount() {
        return mPreviewList == null ? 0 : mPreviewList.size();
    }

    /**
     * View 是否对应对象（固定写法）
     */
    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }

    /**
     * 创建预览页面（核心）
     * 1) 创建可缩放的 ImageView
     * 2) 绑定点击/长按
     * 3) 让子类去加载图片
     */
    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        // 创建支持 PhotoView 的 ImageView
        AttacherImageView imageView = new AttacherImageView(mContext);
        imageView.setLayoutParams(new ViewGroup.LayoutParams(-1, -1));
        // 子类实现：加载图片
        loadPreview(imageView, mPreviewList.get(position), position);
        // 添加到 ViewPager
        container.addView(imageView);
        // 绑定 PhotoView 缩放能力
        final PhotoViewAttacher attacher = new PhotoViewAttacher(imageView);
        // 设置单击
        if (mItemClickListener != null) {
            attacher.setOnViewTapListener(this);
        }
        // 设置长按
        if (mItemLongClickListener != null) {
            attacher.setOnLongClickListener(this);
        }
        imageView.setAttacher(attacher);
        return imageView;
    }

    /**
     * 销毁页面
     */
    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView(((View) object));
    }

    /**
     * 单击回调
     */
    @Override
    public void onViewTap(View v, float x, float y) {
        mItemClickListener.onClick(v);
    }

    /**
     * 长按回调
     */
    @Override
    public boolean onLongClick(View v) {
        mItemLongClickListener.onClick(v);
        return true;
    }

    /**
     * 设置单击监听
     */
    public void setItemClickListener(View.OnClickListener onClickListener) {
        mItemClickListener = onClickListener;
    }

    /**
     * 设置长按监听
     */
    public void setItemLongClickListener(View.OnClickListener longClickListener) {
        mItemLongClickListener = longClickListener;
    }

    /**
     * 子类必须实现：加载图片
     */
    protected abstract void loadPreview(ImageView imageView, T item, int position);

}