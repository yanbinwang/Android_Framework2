package com.yanzhenjie.album.widget.divider;

import androidx.recyclerview.widget.RecyclerView;

/**
 * <p>Divider of {@code RecyclerView}, you can get the width and height of the line.</p>
 * Created by YanZhenjie on 2017/8/16.
 */
public abstract class Divider extends RecyclerView.ItemDecoration {

    /**
     * Get the height of the divider.
     *
     * @return height of the divider.
     */
    public abstract int getHeight();

    /**
     * Get the width of the divider.
     *
     * @return width of the divider.
     */
    public abstract int getWidth();

}