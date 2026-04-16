package com.yanzhenjie.album.app.album;

import android.content.Context;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.gallery.R;
import com.yanzhenjie.album.Album;
import com.yanzhenjie.album.model.AlbumFile;
import com.yanzhenjie.album.widget.recyclerview.OnCheckedClickListener;
import com.yanzhenjie.album.widget.recyclerview.OnItemClickListener;
import com.yanzhenjie.album.utils.AlbumUtil;

import java.util.List;

/**
 * 相册主列表适配器
 * 三种类型：拍照按钮 / 图片 / 视频
 * 支持：单选、多选、预览、选择、禁用遮罩
 */
public class AlbumAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    // 媒体文件列表
    private List<AlbumFile> mAlbumFiles;
    // 三种点击回调
    private OnItemClickListener mAddPhotoClickListener;   // 点击拍照
    private OnItemClickListener mItemClickListener;       // 点击预览
    private OnCheckedClickListener mCheckedClickListener; // 点击选择框
    // 三种条目类型
    private static final int TYPE_BUTTON = 1;  // 拍照按钮
    private static final int TYPE_IMAGE = 2;   // 图片
    private static final int TYPE_VIDEO = 3;   // 视频
    // 选择模式：单选/多选
    private final int mChoiceMode;
    // 是否显示拍照按钮
    private final boolean hasCamera;
    // 布局加载器
    private final LayoutInflater mInflater;
    // 选择框颜色
    private final ColorStateList mSelector;

    public AlbumAdapter(Context context, boolean hasCamera, int choiceMode, ColorStateList selector) {
        this.mInflater = LayoutInflater.from(context);
        this.hasCamera = hasCamera;
        this.mChoiceMode = choiceMode;
        this.mSelector = selector;
    }

    /**
     * 设置数据
     */
    public void setAlbumFiles(List<AlbumFile> albumFiles) {
        this.mAlbumFiles = albumFiles;
    }

    /**
     * 各种点击事件
     */
    public void setAddClickListener(OnItemClickListener addPhotoClickListener) {
        this.mAddPhotoClickListener = addPhotoClickListener;
    }

    public void setItemClickListener(OnItemClickListener itemClickListener) {
        this.mItemClickListener = itemClickListener;
    }

    public void setCheckedClickListener(OnCheckedClickListener checkedClickListener) {
        this.mCheckedClickListener = checkedClickListener;
    }

    /**
     * 条目总数 = 拍照(1) + 媒体数量
     */
    @Override
    public int getItemCount() {
        int camera = hasCamera ? 1 : 0;
        return mAlbumFiles == null ? camera : mAlbumFiles.size() + camera;
    }

    /**
     * 根据位置返回条目类型
     * 0号位置：拍照按钮（如果开启）
     * 其他位置：图片/视频
     */
    @Override
    public int getItemViewType(int position) {
        if (position == 0) {
            return hasCamera ? TYPE_BUTTON : TYPE_IMAGE;
        }
        position = hasCamera ? position - 1 : position;
        AlbumFile albumFile = mAlbumFiles.get(position);
        return albumFile.getMediaType() == AlbumFile.TYPE_VIDEO ? TYPE_VIDEO : TYPE_IMAGE;
    }

    /**
     * 创建三种不同的 ViewHolder
     */
    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        switch (viewType) {
            // 拍照按钮
            case TYPE_BUTTON: {
                return new ButtonViewHolder(mInflater.inflate(R.layout.album_item_content_button, parent, false), mAddPhotoClickListener);
            }
            // 图片
            case TYPE_IMAGE: {
                ImageHolder imageViewHolder = new ImageHolder(mInflater.inflate(R.layout.album_item_content_image, parent, false), hasCamera, mItemClickListener, mCheckedClickListener);
                // 多选模式显示选择框
                if (mChoiceMode == Album.MODE_MULTIPLE) {
                    imageViewHolder.mCheckBox.setVisibility(View.VISIBLE);
                    imageViewHolder.mCheckBox.setButtonTintList(mSelector);
                    imageViewHolder.mCheckBox.setTextColor(mSelector);
                } else {
                    imageViewHolder.mCheckBox.setVisibility(View.GONE);
                }
                return imageViewHolder;
            }
            // 视频
            case TYPE_VIDEO: {
                VideoHolder videoViewHolder = new VideoHolder(mInflater.inflate(R.layout.album_item_content_video, parent, false), hasCamera, mItemClickListener, mCheckedClickListener);
                if (mChoiceMode == Album.MODE_MULTIPLE) {
                    videoViewHolder.mCheckBox.setVisibility(View.VISIBLE);
                    videoViewHolder.mCheckBox.setButtonTintList(mSelector);
                    videoViewHolder.mCheckBox.setTextColor(mSelector);
                } else {
                    videoViewHolder.mCheckBox.setVisibility(View.GONE);
                }
                return videoViewHolder;
            }
            default: {
                throw new AssertionError("This should not be the case.");
            }
        }
    }

    /**
     * 绑定数据
     */
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        switch (getItemViewType(position)) {
            case TYPE_BUTTON: {
                break;
            }
            // 图片/视频统一处理
            case TYPE_IMAGE:
            case TYPE_VIDEO: {
                MediaViewHolder mediaHolder = (MediaViewHolder) holder;
                int camera = hasCamera ? 1 : 0;
                position = holder.getAdapterPosition() - camera;
                AlbumFile albumFile = mAlbumFiles.get(position);
                mediaHolder.setData(albumFile);
                break;
            }
            default: {
                throw new AssertionError("This should not be the case.");
            }
        }
    }

    /**
     * 拍照按钮
     */
    private static class ButtonViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private final OnItemClickListener mItemClickListener;

        private ButtonViewHolder(View itemView, OnItemClickListener itemClickListener) {
            super(itemView);
            this.mItemClickListener = itemClickListener;
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (mItemClickListener != null && v == itemView) {
                mItemClickListener.onItemClick(v, 0);
            }
        }
    }

    /**
     * 图片条目
     */
    private static class ImageHolder extends MediaViewHolder implements View.OnClickListener {
        private final boolean hasCamera;
        private final ImageView mIvImage;
        private final CheckBox mCheckBox;
        private final FrameLayout mLayoutLayer;
        private final OnItemClickListener mItemClickListener;
        private final OnCheckedClickListener mCheckedClickListener;

        private ImageHolder(View itemView, boolean hasCamera, OnItemClickListener itemClickListener, OnCheckedClickListener checkedClickListener) {
            super(itemView);
            this.hasCamera = hasCamera;
            this.mItemClickListener = itemClickListener;
            this.mCheckedClickListener = checkedClickListener;
            mIvImage = itemView.findViewById(R.id.iv_album_content_image);
            mCheckBox = itemView.findViewById(R.id.check_box);
            mLayoutLayer = itemView.findViewById(R.id.layout_layer);
            itemView.setOnClickListener(this);
            mCheckBox.setOnClickListener(this);
            mLayoutLayer.setOnClickListener(this);
        }

        @Override
        public void setData(AlbumFile albumFile) {
            mCheckBox.setChecked(albumFile.isChecked());
            // 加载缩略图
            Album.getAlbumConfig().getAlbumLoader().load(mIvImage, albumFile);
            // 禁用文件显示遮罩
            mLayoutLayer.setVisibility(albumFile.isDisable() ? View.VISIBLE : View.GONE);
        }

        @Override
        public void onClick(View v) {
            if (v == itemView) {
                int camera = hasCamera ? 1 : 0;
                mItemClickListener.onItemClick(v, getAdapterPosition() - camera);
            } else if (v == mCheckBox) {
                int camera = hasCamera ? 1 : 0;
                mCheckedClickListener.onCheckedClick(mCheckBox, getAdapterPosition() - camera);
            } else if (v == mLayoutLayer) {
                int camera = hasCamera ? 1 : 0;
                mItemClickListener.onItemClick(v, getAdapterPosition() - camera);
            }
        }
    }

    /**
     * 视频条目（带时长）
     */
    private static class VideoHolder extends MediaViewHolder implements View.OnClickListener {
        private final boolean hasCamera;
        private final ImageView mIvImage;
        private final CheckBox mCheckBox;
        private final TextView mTvDuration;
        private final FrameLayout mLayoutLayer;
        private final OnItemClickListener mItemClickListener;
        private final OnCheckedClickListener mCheckedClickListener;

        private VideoHolder(View itemView, boolean hasCamera, OnItemClickListener itemClickListener, OnCheckedClickListener checkedClickListener) {
            super(itemView);
            this.hasCamera = hasCamera;
            this.mItemClickListener = itemClickListener;
            this.mCheckedClickListener = checkedClickListener;
            mIvImage = itemView.findViewById(R.id.iv_album_content_image);
            mCheckBox = itemView.findViewById(R.id.check_box);
            mTvDuration = itemView.findViewById(R.id.tv_duration);
            mLayoutLayer = itemView.findViewById(R.id.layout_layer);
            itemView.setOnClickListener(this);
            mCheckBox.setOnClickListener(this);
            mLayoutLayer.setOnClickListener(this);
        }

        @Override
        public void setData(AlbumFile albumFile) {
            Album.getAlbumConfig().getAlbumLoader().load(mIvImage, albumFile);
            mCheckBox.setChecked(albumFile.isChecked());
            mTvDuration.setText(AlbumUtil.convertDuration(albumFile.getDuration()));
            mLayoutLayer.setVisibility(albumFile.isDisable() ? View.VISIBLE : View.GONE);
        }

        @Override
        public void onClick(View v) {
            if (v == itemView) {
                int camera = hasCamera ? 1 : 0;
                mItemClickListener.onItemClick(v, getAdapterPosition() - camera);
            } else if (v == mCheckBox) {
                int camera = hasCamera ? 1 : 0;
                mCheckedClickListener.onCheckedClick(mCheckBox, getAdapterPosition() - camera);
            } else if (v == mLayoutLayer) {
                if (mItemClickListener != null) {
                    int camera = hasCamera ? 1 : 0;
                    mItemClickListener.onItemClick(v, getAdapterPosition() - camera);
                }
            }
        }
    }

    /**
     * 抽象基类：统一图片/视频
     */
    private abstract static class MediaViewHolder extends RecyclerView.ViewHolder {

        public MediaViewHolder(View itemView) {
            super(itemView);
        }

        public abstract void setData(AlbumFile albumFile);

    }

}