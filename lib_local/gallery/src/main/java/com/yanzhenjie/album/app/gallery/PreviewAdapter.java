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
 * <p>Adapter of preview the big picture.</p>
 * Created by Yan Zhenjie on 2016/10/19.
 */
public abstract class PreviewAdapter<T> extends PagerAdapter implements PhotoViewAttacher.OnViewTapListener, View.OnLongClickListener {
    private Context mContext;
    private List<T> mPreviewList;
    private View.OnClickListener mItemClickListener;
    private View.OnClickListener mItemLongClickListener;

    public PreviewAdapter(Context context, List<T> previewList) {
        this.mContext = context;
        this.mPreviewList = previewList;
    }

    /**
     * Set item click listener.
     *
     * @param onClickListener listener.
     */
    public void setItemClickListener(View.OnClickListener onClickListener) {
        this.mItemClickListener = onClickListener;
    }

    /**
     * Set item long click listener.
     *
     * @param longClickListener listener.
     */
    public void setItemLongClickListener(View.OnClickListener longClickListener) {
        this.mItemLongClickListener = longClickListener;
    }

    @Override
    public int getCount() {
        return mPreviewList == null ? 0 : mPreviewList.size();
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        AttacherImageView imageView = new AttacherImageView(mContext);
        imageView.setLayoutParams(new ViewGroup.LayoutParams(-1, -1));
        loadPreview(imageView, mPreviewList.get(position), position);
        container.addView(imageView);
        final PhotoViewAttacher attacher = new PhotoViewAttacher(imageView);
        if (mItemClickListener != null) {
            attacher.setOnViewTapListener(this);
        }
        if (mItemLongClickListener != null) {
            attacher.setOnLongClickListener(this);
        }
        imageView.setAttacher(attacher);
        return imageView;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView(((View) object));
    }

    @Override
    public void onViewTap(View v, float x, float y) {
        mItemClickListener.onClick(v);
    }

    @Override
    public boolean onLongClick(View v) {
        mItemLongClickListener.onClick(v);
        return true;
    }

    protected abstract void loadPreview(ImageView imageView, T item, int position);

}