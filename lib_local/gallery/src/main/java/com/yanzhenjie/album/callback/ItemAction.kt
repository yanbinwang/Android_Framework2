package com.yanzhenjie.album.callback

import android.content.Context

/**
 * 列表条目 / 数据项 的通用行为回调接口
 * 作用：把「条目点击、选择、长按、删除」等行为抽象出来，实现解耦
 */
interface ItemAction<T> {
    /**
     * 当行为发生时（比如条目被点击、选中、取消选中、长按等）
     *
     * @param context 上下文
     * @param item    当前触发行为的数据项（图片Bean、文件夹Bean等）
     */
    fun onAction(context: Context, item: T)
}