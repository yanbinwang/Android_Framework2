package com.yanzhenjie.album.app.album

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.common.utils.setNavigationBarDrawable
import com.example.common.utils.setNavigationBarLightMode
import com.example.common.utils.setStatusBarLightMode
import com.example.gallery.R
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.gyf.immersionbar.ImmersionBar
import com.yanzhenjie.album.model.AlbumFolder
import com.yanzhenjie.album.model.Widget
import com.yanzhenjie.album.widget.recyclerview.OnItemClickListener
import kotlin.math.min

/**
 * 文件夹选择弹窗（从底部弹出）
 * 功能：点击相册顶部文件夹名称 → 弹出此对话框切换文件夹
 */
class FolderDialog(context: Context, widget: Widget, albumFolders: List<AlbumFolder>, itemClickListener: (position: Int) -> Unit) : BottomSheetDialog(context, R.style.Album_Dialog_Folder) {
    // 当前选中的文件夹位置
    private var mCurrentPosition = 0

    /**
     * 构造方法：初始化弹窗、列表、适配器
     */
    init {
        // 加载布局
        setContentView(R.layout.album_dialog_floder)
        // 初始化RecyclerView
        val recyclerView = getDelegate().findViewById<RecyclerView>(R.id.rv_content_list)
        recyclerView?.setLayoutManager(LinearLayoutManager(getContext()))
        // 创建适配器
        val mFolderAdapter = FolderAdapter(albumFolders, widget.bucketItemCheckSelector)
        // 条目点击事件
        mFolderAdapter.setItemClickListener(object : OnItemClickListener {
            override fun onItemClick(view: View?, position: Int) {
                // 如果点击的不是当前选中项
                if (mCurrentPosition != position) {
                    // 取消上一个选中状态
                    albumFolders[mCurrentPosition].isChecked = false
                    mFolderAdapter.notifyItemChanged(mCurrentPosition)
                    // 记录新位置并设置选中
                    mCurrentPosition = position
                    albumFolders[mCurrentPosition].isChecked = true
                    mFolderAdapter.notifyItemChanged(mCurrentPosition)
                    // 回调外部
                    itemClickListener.invoke(position)
                }
                dismiss()
            }
        })
        recyclerView?.setAdapter(mFolderAdapter)
    }

    /**
     * 创建弹窗：设置宽高
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 获取屏幕宽高
        val display = window?.windowManager?.defaultDisplay
        val metrics = DisplayMetrics()
        display?.getRealMetrics(metrics)
        // 宽度取屏幕最小值，高度铺满
        val minSize = min(metrics.widthPixels, metrics.heightPixels)
        window?.setLayout(minSize, -1)
        // 导航栏控件
        window?.setStatusBarLightMode(false)
        window?.setNavigationBarLightMode(true)
        window?.setNavigationBarDrawable(R.color.albumPage)
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
            ownerActivity?.let { activity ->
                ImmersionBar.with(activity)
                    .reset()
                    .statusBarDarkFont(false, 0.2f)
                    .navigationBarDarkIcon(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O, 0.2f)
                    .init()
            }
        }
    }

}