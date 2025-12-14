package com.yanzhenjie.album.impl;

import android.widget.CompoundButton;

/**
 * Created by YanZhenjie on 2018/4/11.
 */
public interface OnCheckedClickListener {

    /**
     * Compound button is clicked.
     *
     * @param button   view.
     * @param position the position in the list.
     */
    void onCheckedClick(CompoundButton button, int position);

}