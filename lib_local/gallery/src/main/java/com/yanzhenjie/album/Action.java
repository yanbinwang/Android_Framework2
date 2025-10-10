package com.yanzhenjie.album;

import androidx.annotation.NonNull;

/**
 * <p>Any action takes place.</p>
 * Created by YanZhenjie on 2017/8/16.
 */
public interface Action<T> {

    /**
     * When the action responds.
     *
     * @param result the result of the action.
     */
    void onAction(@NonNull T result);

}