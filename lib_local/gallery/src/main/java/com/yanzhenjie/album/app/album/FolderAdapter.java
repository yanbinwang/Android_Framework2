package com.yanzhenjie.album.app.album;

import android.content.Context;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.widget.AppCompatRadioButton;
import androidx.recyclerview.widget.RecyclerView;

import com.example.gallery.R;
import com.yanzhenjie.album.Album;
import com.yanzhenjie.album.AlbumFile;
import com.yanzhenjie.album.AlbumFolder;
import com.yanzhenjie.album.impl.OnItemClickListener;

import java.util.List;

/**
 * <p>BottomSheet dialog adapter, show all folder.</p>
 * Created by Yan Zhenjie on 2016/10/18.
 */
class FolderAdapter extends RecyclerView.Adapter<FolderAdapter.FolderViewHolder> {
    private List<AlbumFolder> mAlbumFolders;
    private ColorStateList mSelector;
    private LayoutInflater mInflater;
    private OnItemClickListener mItemClickListener;

    public FolderAdapter(Context context, List<AlbumFolder> mAlbumFolders, ColorStateList buttonTint) {
        this.mInflater = LayoutInflater.from(context);
        this.mSelector = buttonTint;
        this.mAlbumFolders = mAlbumFolders;
    }

    public void setItemClickListener(OnItemClickListener itemClickListener) {
        this.mItemClickListener = itemClickListener;
    }

    @Override
    public FolderViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new FolderViewHolder(mInflater.inflate(R.layout.album_item_dialog_folder, parent, false), mSelector, new OnItemClickListener() {
            private int oldPosition = 0;

            @Override
            public void onItemClick(View view, int position) {
                if (mItemClickListener != null)
                    mItemClickListener.onItemClick(view, position);
                AlbumFolder albumFolder = mAlbumFolders.get(position);
                if (!albumFolder.isChecked()) {
                    albumFolder.setChecked(true);
                    mAlbumFolders.get(oldPosition).setChecked(false);
                    notifyItemChanged(oldPosition);
                    notifyItemChanged(position);
                    oldPosition = position;
                }
            }
        });
    }

    @Override
    public void onBindViewHolder(FolderViewHolder holder, int position) {
        final int newPosition = holder.getAdapterPosition();
        holder.setData(mAlbumFolders.get(newPosition));
    }

    @Override
    public int getItemCount() {
        return mAlbumFolders == null ? 0 : mAlbumFolders.size();
    }

    static class FolderViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private ImageView mIvImage;
        private TextView mTvTitle;
        private AppCompatRadioButton mCheckBox;
        private OnItemClickListener mItemClickListener;

        private FolderViewHolder(View itemView, ColorStateList selector, OnItemClickListener itemClickListener) {
            super(itemView);
            this.mItemClickListener = itemClickListener;
            mIvImage = itemView.findViewById(R.id.iv_gallery_preview_image);
            mTvTitle = itemView.findViewById(R.id.tv_gallery_preview_title);
            mCheckBox = itemView.findViewById(R.id.rb_gallery_preview_check);
            itemView.setOnClickListener(this);
            mCheckBox.setBackgroundTintList(selector);
        }

        public void setData(AlbumFolder albumFolder) {
            List<AlbumFile> albumFiles = albumFolder.getAlbumFiles();
            mTvTitle.setText("(" + albumFiles.size() + ") " + albumFolder.getName());
            mCheckBox.setChecked(albumFolder.isChecked());
            Album.getAlbumConfig().getAlbumLoader().load(mIvImage, albumFiles.get(0));
        }

        @Override
        public void onClick(View v) {
            if (mItemClickListener != null)
                mItemClickListener.onItemClick(v, getAdapterPosition());
        }
    }

}