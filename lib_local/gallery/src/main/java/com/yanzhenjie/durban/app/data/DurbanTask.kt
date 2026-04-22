package com.yanzhenjie.durban.app.data

import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LifecycleOwner

/**
 * 裁剪库整体执行
 */
class DurbanTask(private val activity: FragmentActivity) {
    private val owner: LifecycleOwner = activity

    /**
     * 裁剪图片 → 保存到文件 → 返回路径
     */
    fun cropAndSaveExecute() {

    }

    /**
     * 从文件加载图片 → 纠正旋转 → 返回 Bitmap
     */
    fun cropExecute() {

    }

}