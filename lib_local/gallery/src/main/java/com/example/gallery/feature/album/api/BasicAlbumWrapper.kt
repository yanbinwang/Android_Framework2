package com.example.gallery.feature.album.api

import android.content.Context
import com.example.gallery.feature.album.callback.Action
import com.example.gallery.feature.album.model.Widget

/**
 * 整个库对外调用的统一入口基类
 * 功能：封装 相册/相机 公共能力，高度抽象、高度复用
 * @param Returner   返回自身类型（链式调用 .xxx().xxx()）
 * @param Result     成功回调类型
 * @param Cancel     取消回调类型
 * @param Checked    已选数据类型
 */
abstract class BasicAlbumWrapper<Returner : BasicAlbumWrapper<Returner, Result, Cancel, Checked>, Result, Cancel, Checked>(val mContext: Context) {
    // 成功回调
    protected var mResult: Action<Result>? = null
    // 取消回调
    protected var mCancel: Action<Cancel>? = null
    // 已选中的文件
    protected var mChecked: Checked? = null
    // 界面样式（主题、状态栏、导航栏、颜色等）
    protected var mWidget: Widget? = Widget.getDefaultWidget(mContext)

    /**
     * 设置成功回调
     * 返回 Returner 泛型 → 支持链式调用：.onResult(...).onCancel(...)
     */
    fun onResult(result: Action<Result>): Returner {
        this.mResult = result
        return this as Returner
    }

    /**
     * 设置取消回调
     */
    fun onCancel(cancel: Action<Cancel>): Returner {
        this.mCancel = cancel
        return this as Returner
    }

    /**
     * 设置自定义主题样式
     */
    fun widget(widget: Widget?): Returner {
        this.mWidget = widget
        return this as Returner
    }

    /**
     * 抽象方法：启动页面（相册/相机 各自实现）
     */
    abstract fun start()

}