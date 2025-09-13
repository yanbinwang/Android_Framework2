package com.yanzhenjie.album.impl;

import android.view.View;

/**
 * <p>Listens on the item's click.</p>
 * Created by Yan Zhenjie on 2016/9/23.
 */
public interface OnItemClickListener {

    /**
     * When Item is clicked.
     *
     * @param view     item view.
     * @param position item position.
     */
    void onItemClick(View view, int position);

}