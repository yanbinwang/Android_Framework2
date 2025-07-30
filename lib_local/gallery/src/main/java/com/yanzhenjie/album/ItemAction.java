package com.yanzhenjie.album;

import android.content.Context;

/**
 * <p>Any action takes place.</p>
 * Created by YanZhenjie on 2018/4/17.
 */
public interface ItemAction<T> {

    /**
     * When the action responds.
     *
     * @param context context.
     * @param item    item.
     */
    void onAction(Context context, T item);

}
