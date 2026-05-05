package com.example.gallery.feature.album.app

import android.view.View
import android.widget.CompoundButton
import androidx.appcompat.app.AppCompatActivity
import com.example.gallery.base.bridge.BasePresenter
import com.example.gallery.base.bridge.BaseSource
import com.example.gallery.base.bridge.BaseView
import com.example.gallery.feature.album.bean.AlbumFolder
import com.example.gallery.feature.album.bean.Widget

/**
 * MVP 架构契约类
 * 统一定义：相册页面、空页面、预览页面 的 Presenter 和 View 接口
 * 作用：规范页面行为，解耦 P 层和 V 层
 */
object Contract {
    /**
     * 相册主页（图片列表页）-> Presenter 接口
     */
    interface AlbumPresenter : BasePresenter {
        /**
         * 点击所有图片 -> 切换相册
         */
        fun clickFolderSwitch() {}

        /**
         * 点击列表 -> 拍照/录像
         */
        fun clickCamera(v: View?) {}

        /**
         * 点击列表条目 -> 选中/取消选中某个条目
         */
        fun tryCheckItem(button: CompoundButton?, position: Int) {}

        /**
         * 点击列表条目 -> 预览当前位置的图片/视频
         */
        fun tryPreviewItem(position: Int) {}

        /**
         * 预览已选中的所有图片
         */
        fun tryPreviewChecked() {}

        /**
         * 完成选择（确定）
         */
        fun complete() {}
    }

    /**
     * 相册主页（图片列表页）-> View 接口
     */
    abstract class AlbumView(activity: AppCompatActivity, presenter: AlbumPresenter) : BaseView<AlbumPresenter>(BaseSource(activity), presenter) {
        /**
         * 初始化页面控件
         *
         * @param widget     配置项
         * @param column     列数
         * @param hasCamera  是否显示相机
         * @param choiceMode 选择模式：图片/视频/全部
         */
        abstract fun setupViews(widget: Widget, column: Int, hasCamera: Boolean, choiceMode: Int)

        /**
         * 设置加载状态是否显示
         */
        abstract fun setLoadingDisplay(display: Boolean)

        /**
         * 设置完成按钮是否显示
         */
        abstract fun setCompleteDisplay(display: Boolean)

        /**
         * 绑定当前文件夹
         */
        abstract fun bindAlbumFolder(albumFolder: AlbumFolder)

        /**
         * 通知条目插入
         */
        abstract fun notifyInsertItem(position: Int)

        /**
         * 通知条目更新
         */
        abstract fun notifyItem(position: Int)

        /**
         * 设置已选中数量
         */
        abstract fun setCheckedCount(count: Int)
    }

    /**
     * 空页面 -> Presenter 接口
     */
    interface NullPresenter : BasePresenter {
        /**
         * 拍照
         */
        fun takePicture() {}

        /**
         * 录像
         */
        fun takeVideo() {}
    }

    /**
     * 空页面 -> View 接口
     */
    abstract class NullView(activity: AppCompatActivity, presenter: NullPresenter) : BaseView<NullPresenter>(BaseSource(activity), presenter) {
        /**
         * 初始化控件
         */
        abstract fun setupViews(widget: Widget)

        /**
         * 设置空页面提示文字
         */
        abstract fun setMessage(message: Int)

        /**
         * 设置拍照按钮显示/隐藏
         */
        abstract fun setMakeImageDisplay(display: Boolean)

        /**
         * 设置录像按钮显示/隐藏
         */
        abstract fun setMakeVideoDisplay(display: Boolean)
    }

    /**
     * 预览页（大图预览）-> Presenter 接口
     */
    interface GalleryPresenter : BasePresenter {
        /**
         * 点击预览页条目
         */
        fun clickItem(position: Int) {}

        /**
         * 长按预览页条目
         */
        fun longClickItem(position: Int) {}

        /**
         * 预览页滑动切换时回调
         */
        fun onCurrentChanged(position: Int) {}

        /**
         * 切换当前条目的选中状态
         */
        fun onCheckedChanged() {}

        /**
         * 完成选择
         */
        fun complete() {}
    }

    /**
     * 预览页（大图预览）-> View 接口
     */
    abstract class GalleryView<Data>(activity: AppCompatActivity, presenter: GalleryPresenter) : BaseView<GalleryPresenter>(BaseSource(activity), presenter) {
        /**
         * 初始化预览页控件
         */
        abstract fun setupViews(widget: Widget, checkable: Boolean)

        /**
         * 绑定预览数据
         */
        abstract fun bindData(dataList: List<Data>)

        /**
         * 切换到指定位置预览
         */
        abstract fun setCurrentItem(position: Int)

        /**
         * 设置视频时长显示/隐藏
         */
        abstract fun setDurationDisplay(display: Boolean)

        /**
         * 设置视频时长文字
         */
        abstract fun setDuration(duration: String)

        /**
         * 设置当前条目选中状态
         */
        abstract fun setChecked(checked: Boolean)

        /**
         * 设置底部栏显示/隐藏
         */
        abstract fun setMenuDisplay(display: Boolean)

        /**
         * 设置遮罩层显示/隐藏
         */
        abstract fun setLayerDisplay(display: Boolean)

        /**
         * 设置完成按钮文字
         */
        abstract fun setCompleteText(text: String)
    }

}