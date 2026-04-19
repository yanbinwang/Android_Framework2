package com.yanzhenjie.album.app.album;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.gallery.R;
import com.yanzhenjie.album.Album;
import com.yanzhenjie.album.model.AlbumFile;
import com.yanzhenjie.album.model.AlbumFolder;
import com.yanzhenjie.album.widget.recyclerview.OnItemClickListener;

import java.util.List;

/**
 * 文件夹选择弹窗 列表适配器
 * 功能：展示所有图片/视频文件夹，带单选、封面图、数量显示
 */
@SuppressLint("SetTextI18n")
public class FolderAdapter extends RecyclerView.Adapter<FolderAdapter.FolderViewHolder> {
    // 条目点击回调
    private OnItemClickListener mItemClickListener;
    // 文件夹列表
    private final List<AlbumFolder> mAlbumFolders;
    // 单选按钮颜色选择器
    private final ColorStateList mSelector;
    // 布局加载器
    private final LayoutInflater mInflater;

    public FolderAdapter(Context context, List<AlbumFolder> mAlbumFolders, ColorStateList buttonTint) {
        this.mInflater = LayoutInflater.from(context);
        this.mSelector = buttonTint;
        this.mAlbumFolders = mAlbumFolders;
    }

    /**
     * 创建 ViewHolder
     */
    @NonNull
    @Override
    public FolderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // 内部处理单选逻辑（自动切换上一个/当前选中状态）
        return new FolderViewHolder(mInflater.inflate(R.layout.album_item_dialog_folder, parent, false), mSelector, new OnItemClickListener() {
            private int oldPosition = 0;

            @Override
            public void onItemClick(View view, int position) {
                // 回调外部点击
                if (mItemClickListener != null) {
                    mItemClickListener.onItemClick(view, position);
                }
                // 内部单选逻辑：取消上一个，选中当前
                AlbumFolder albumFolder = mAlbumFolders.get(position);
                if (!albumFolder.isChecked()) {
                    albumFolder.setChecked(true);
                    mAlbumFolders.get(oldPosition).setChecked(false);
                    // 局部刷新，避免整个列表刷新
                    notifyItemChanged(oldPosition);
                    notifyItemChanged(position);
                    oldPosition = position;
                }
            }
        });
    }

    /**
     * 绑定数据
     */
    @Override
    public void onBindViewHolder(FolderViewHolder holder, int position) {
        final int newPosition = holder.getAbsoluteAdapterPosition();
        holder.setData(mAlbumFolders.get(newPosition));
    }

    /**
     * 条目数量
     */
    @Override
    public int getItemCount() {
        return mAlbumFolders == null ? 0 : mAlbumFolders.size();
    }

    /**
     * 设置外部点击回调
     */
    public void setItemClickListener(OnItemClickListener itemClickListener) {
        this.mItemClickListener = itemClickListener;
    }

    /**
     * ViewHolder 缓存
     */
    public static class FolderViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        // 文件夹封面图
        private final ImageView mIvImage;
        // 文件夹名称 + 文件数量
        private final TextView mTvTitle;
        // 单选按钮
        private final RadioButton mCheckBox;
        private final OnItemClickListener mItemClickListener;

        private FolderViewHolder(View itemView, ColorStateList selector, OnItemClickListener itemClickListener) {
            super(itemView);
            this.mItemClickListener = itemClickListener;
            // 绑定控件
            mIvImage = itemView.findViewById(R.id.iv_gallery_preview_image);
            mTvTitle = itemView.findViewById(R.id.tv_gallery_preview_title);
            mCheckBox = itemView.findViewById(R.id.rb_gallery_preview_check);
            // 设置点击事件
            itemView.setOnClickListener(this);
            // 设置单选按钮颜色
            mCheckBox.setButtonTintList(selector);
        }

        /**
         * 给控件设置数据
         */
        public void setData(AlbumFolder albumFolder) {
            List<AlbumFile> albumFiles = albumFolder.getAlbumFiles();
            // 显示：(数量) 文件夹名称
            mTvTitle.setText("(" + albumFiles.size() + ") " + albumFolder.getName());
            // 选中状态
            mCheckBox.setChecked(albumFolder.isChecked());
            // 加载文件夹第一张图作为封面
            Album.getAlbumConfig().getAlbumLoader().load(mIvImage, albumFiles.get(0));
        }

        /**
         * 条目点击
         */
        @Override
        public void onClick(View v) {
            if (mItemClickListener != null) {
                mItemClickListener.onItemClick(v, getAdapterPosition());
            }
        }
    }

}