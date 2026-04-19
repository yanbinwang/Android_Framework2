package com.yanzhenjie.album.callback;

import androidx.annotation.NonNull;

/**
 * 通用行为/回调接口
 * 任何操作、任务、逻辑执行完成后的通用回调
 */
public interface Action<T> {

    /**
     * 当操作执行完成、结果返回时回调
     *
     * @param result 回调返回的数据结果（泛型）
     */
    void onAction(@NonNull T result);

}