package com.yanzhenjie.album.app;

import android.app.Activity;
import android.view.View;
import android.widget.CompoundButton;

import com.example.gallery.base.bridge.BasePresenter;
import com.example.gallery.base.bridge.BaseView;
import com.yanzhenjie.album.model.AlbumFolder;
import com.yanzhenjie.album.model.Widget;

import java.util.List;

/**
 * MVP 架构契约类
 * 统一定义：相册页面、空页面、预览页面 的 Presenter 和 View 接口
 * 作用：规范页面行为，解耦 P 层和 V 层
 */
public final class Contract {

    /**
     * 相册主页（图片列表页）-> Presenter 接口
     */
    public interface AlbumPresenter extends BasePresenter {

        /**
         * 点击所有图片 -> 切换相册
         */
        void clickFolderSwitch();

        /**
         * 点击列表 -> 拍照/录像
         */
        void clickCamera(View v);

        /**
         * 点击列表条目 -> 选中/取消选中某个条目
         */
        void tryCheckItem(CompoundButton button, int position);

        /**
         * 点击列表条目 -> 预览当前位置的图片/视频
         */
        void tryPreviewItem(int position);

        /**
         * 预览已选中的所有图片
         */
        void tryPreviewChecked();

        /**
         * 完成选择（确定）
         */
        void complete();

    }

    /**
     * 相册主页（图片列表页）-> View 接口
     */
    public static abstract class AlbumView extends BaseView<AlbumPresenter> {

        public AlbumView(Activity activity, AlbumPresenter presenter) {
            super(activity, presenter);
        }

        /**
         * 初始化页面控件
         *
         * @param widget     配置项
         * @param column     列数
         * @param hasCamera  是否显示相机
         * @param choiceMode 选择模式：图片/视频/全部
         */
        public abstract void setupViews(Widget widget, int column, boolean hasCamera, int choiceMode);

        /**
         * 设置加载状态是否显示
         */
        public abstract void setLoadingDisplay(boolean display);

        /**
         * 设置完成按钮是否显示
         */
        public abstract void setCompleteDisplay(boolean display);

        /**
         * 绑定当前文件夹
         */
        public abstract void bindAlbumFolder(AlbumFolder albumFolder);

        /**
         * 通知条目插入
         */
        public abstract void notifyInsertItem(int position);

        /**
         * 通知条目更新
         */
        public abstract void notifyItem(int position);

        /**
         * 设置已选中数量
         */
        public abstract void setCheckedCount(int count);

    }

    /**
     * 空页面 -> Presenter 接口
     */
    public interface NullPresenter extends BasePresenter {

        /**
         * 拍照
         */
        void takePicture();

        /**
         * 录像
         */
        void takeVideo();

    }

    /**
     * 空页面 -> View 接口
     */
    public static abstract class NullView extends BaseView<NullPresenter> {

        public NullView(Activity activity, NullPresenter presenter) {
            super(activity, presenter);
        }

        /**
         * 初始化控件
         */
        public abstract void setupViews(Widget widget);

        /**
         * 设置空页面提示文字
         */
        public abstract void setMessage(int message);

        /**
         * 设置拍照按钮显示/隐藏
         */
        public abstract void setMakeImageDisplay(boolean display);

        /**
         * 设置录像按钮显示/隐藏
         */
        public abstract void setMakeVideoDisplay(boolean display);

    }

    /**
     * 预览页（大图预览）-> Presenter 接口
     */
    public interface GalleryPresenter extends BasePresenter {

        /**
         * 点击预览页条目
         */
        void clickItem(int position);

        /**
         * 长按预览页条目
         */
        void longClickItem(int position);

        /**
         * 预览页滑动切换时回调
         */
        void onCurrentChanged(int position);

        /**
         * 切换当前条目的选中状态
         */
        void onCheckedChanged();

        /**
         * 完成选择
         */
        void complete();

    }

    /**
     * 预览页（大图预览）-> View 接口
     */
    public static abstract class GalleryView<Data> extends BaseView<GalleryPresenter> {

        public GalleryView(Activity activity, GalleryPresenter presenter) {
            super(activity, presenter);
        }

        /**
         * 初始化预览页控件
         */
        public abstract void setupViews(Widget widget, boolean checkable);

        /**
         * 绑定预览数据
         */
        public abstract void bindData(List<Data> dataList);

        /**
         * 切换到指定位置预览
         */
        public abstract void setCurrentItem(int position);

        /**
         * 设置视频时长显示/隐藏
         */
        public abstract void setDurationDisplay(boolean display);

        /**
         * 设置视频时长文字
         */
        public abstract void setDuration(String duration);

        /**
         * 设置当前条目选中状态
         */
        public abstract void setChecked(boolean checked);

        /**
         * 设置底部栏显示/隐藏
         */
        public abstract void setBottomDisplay(boolean display);

        /**
         * 设置遮罩层显示/隐藏
         */
        public abstract void setLayerDisplay(boolean display);

        /**
         * 设置完成按钮文字
         */
        public abstract void setCompleteText(String text);

    }

}