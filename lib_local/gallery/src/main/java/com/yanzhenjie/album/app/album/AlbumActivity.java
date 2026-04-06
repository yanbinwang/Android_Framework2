package com.yanzhenjie.album.app.album;

import static com.example.common.utils.ScreenUtil.shouldUseWhiteSystemBarsForRes;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.CompoundButton;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.PopupMenu;

import com.example.framework.utils.builder.TimerBuilder;
import com.example.gallery.R;
import com.example.gallery.base.BaseActivity;
import com.yanzhenjie.album.Album;
import com.yanzhenjie.album.api.widget.Widget;
import com.yanzhenjie.album.app.Contract;
import com.yanzhenjie.album.app.album.data.MediaReadTask;
import com.yanzhenjie.album.app.album.data.MediaReader;
import com.yanzhenjie.album.app.album.data.PathConversion;
import com.yanzhenjie.album.app.album.data.PathConvertTask;
import com.yanzhenjie.album.app.album.data.ThumbnailBuildTask;
import com.yanzhenjie.album.callback.Action;
import com.yanzhenjie.album.callback.Filter;
import com.yanzhenjie.album.model.AlbumFile;
import com.yanzhenjie.album.model.AlbumFolder;
import com.yanzhenjie.album.utils.AlbumUtil;
import com.yanzhenjie.album.widget.LoadingDialog;
import com.yanzhenjie.mediascanner.MediaScanner;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import kotlin.Unit;

/**
 * 相册主页（总控 Activity）
 * MVP 中的 Presenter —— 掌管：扫描、选择、拍照、预览、文件夹、回调、所有逻辑
 * MediaReadTask.Callback       // 媒体扫描回调
 * GalleryActivity.Callback     // 预览页回调
 * PathConvertTask.Callback     // 路径转换回调
 * ThumbnailBuildTask.Callback  // 缩略图生成回调
 */
public class AlbumActivity extends BaseActivity implements Contract.AlbumPresenter, MediaReadTask.Callback, GalleryActivity.Callback, PathConvertTask.Callback, ThumbnailBuildTask.Callback {
    // 当前选中的文件夹
    private int mCurrentFolder;
    // 功能：图片/视频/全部
    private int mFunction;
    // 单选/多选
    private int mChoiceMode;
    // 列表列数
    private int mColumnCount;
    // 最大选择数量
    private int mLimitCount;
    // 视频质量
    private int mQuality;
    // 视频最大时长
    private long mLimitDuration;
    // 视频最大大小
    private long mLimitBytes;
    // 是否显示拍照按钮
    private boolean mHasCamera;
    // 是否显示不可用文件
    private boolean mFilterVisibility;
    // 所有文件夹
    private List<AlbumFolder> mAlbumFolders;
    // 已选中的图片
    private ArrayList<AlbumFile> mCheckedList;
    // 主题样式
    private Widget mWidget;
    // MVP & UI
    private Contract.AlbumView mView;
    // 相机选择弹窗
    private PopupMenu mCameraPopupMenu;
    // 文件夹选择弹窗
    private FolderDialog mFolderDialog;
    // 加载对话框
    private LoadingDialog mLoadingDialog;
    // 媒体扫描
    private MediaScanner mMediaScanner;
    // 异步读取媒体
    private MediaReadTask mMediaReadTask;
    // 静态常量
    private static final int CODE_ACTIVITY_NULL = 1;
    public static Filter<Long> sSizeFilter;
    public static Filter<Long> sDurationFilter;
    public static Filter<String> sMimeFilter;
    public static Action<String> sCancel;
    public static Action<ArrayList<AlbumFile>> sResult;

    @Override
    protected boolean isImmersionBarEnabled() {
        return false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 获取参数
        initArgument();
        // 根据主题加载布局
        setContentView(R.layout.album_activity_album);
        // 初始化状态栏
        boolean statusBarBattery = shouldUseWhiteSystemBarsForRes(mWidget.getStatusBarColor());
        boolean navigationBarBattery = shouldUseWhiteSystemBarsForRes(mWidget.getNavigationBarColor());
        initImmersionBar(!statusBarBattery, !navigationBarBattery, mWidget.getNavigationBarColor());
        // 绑定 MVP
        mView = new AlbumView(this, this);
        mView.setupViews(mWidget, mColumnCount, mHasCamera, mChoiceMode);
        mView.setCompleteDisplay(false);
        mView.setLoadingDisplay(true);
        // 开始异步扫描相册
        ArrayList<AlbumFile> checkedList = getIntent().getParcelableArrayListExtra(Album.KEY_INPUT_CHECKED_LIST);
        MediaReader mediaReader = new MediaReader(this, sSizeFilter, sMimeFilter, sDurationFilter, mFilterVisibility);
        mMediaReadTask = new MediaReadTask(mFunction, checkedList, mediaReader, this);
        mMediaReadTask.execute();
        // 返回键 → 取消
        setOnBackPressedListener(() -> {
            if (mMediaReadTask != null) {
                mMediaReadTask.cancel(true);
            }
            callbackCancel();
            return Unit.INSTANCE;
        });
    }

    /**
     * 读取外部传递的配置参数
     */
    private void initArgument() {
        Bundle argument = getIntent().getExtras();
        if (null != argument) {
            mWidget = argument.getParcelable(Album.KEY_INPUT_WIDGET);
            mFunction = argument.getInt(Album.KEY_INPUT_FUNCTION);
            mChoiceMode = argument.getInt(Album.KEY_INPUT_CHOICE_MODE);
            mColumnCount = argument.getInt(Album.KEY_INPUT_COLUMN_COUNT);
            mHasCamera = argument.getBoolean(Album.KEY_INPUT_ALLOW_CAMERA);
            mLimitCount = argument.getInt(Album.KEY_INPUT_LIMIT_COUNT);
            mQuality = argument.getInt(Album.KEY_INPUT_CAMERA_QUALITY);
            mLimitDuration = argument.getLong(Album.KEY_INPUT_CAMERA_DURATION);
            mLimitBytes = argument.getLong(Album.KEY_INPUT_CAMERA_BYTES);
            mFilterVisibility = argument.getBoolean(Album.KEY_INPUT_FILTER_VISIBILITY);
        } else {
            finish();
        }
    }

    /**
     * 屏幕旋转
     */
    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mView.onConfigurationChanged(newConfig);
        if (mFolderDialog != null && !mFolderDialog.isShowing()) mFolderDialog = null;
    }

    /**
     * 媒体扫描完成回调
     */
    @Override
    public void onScanCallback(ArrayList<AlbumFolder> albumFolders, ArrayList<AlbumFile> checkedFiles) {
        mMediaReadTask = null;
        mAlbumFolders = albumFolders;
        mCheckedList = checkedFiles;
        // 延迟半秒关闭 loading，过渡更自然
        TimerBuilder.schedule(this, () -> {
            switch (mChoiceMode) {
                case Album.MODE_MULTIPLE: {
                    mView.setCompleteDisplay(true);
                    break;
                }
                case Album.MODE_SINGLE: {
                    mView.setCompleteDisplay(false);
                    break;
                }
                default: {
                    throw new AssertionError("This should not be the case.");
                }
            }
            mView.setLoadingDisplay(false);
            return Unit.INSTANCE;
        }, 500);
        // 没有图片 → 打开空页面
        if (mAlbumFolders.get(0).getAlbumFiles().isEmpty()) {
            Intent intent = new Intent(this, NullActivity.class);
            intent.putExtras(getIntent());
            startActivityForResult(intent, CODE_ACTIVITY_NULL);
            overridePendingTransition(0, 0);
            // 显示全部图片
        } else {
            showFolderAlbumFiles(0);
            int count = mCheckedList.size();
            mView.setCheckedCount(count);
        }
    }

    /**
     * 空页面拍照返回
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CODE_ACTIVITY_NULL) {
            if (resultCode == RESULT_OK) {
                String imagePath = NullActivity.parsePath(data);
                String mimeType = AlbumUtil.getMimeType(imagePath);
                if (!TextUtils.isEmpty(mimeType)) {
                    mCameraAction.onAction(imagePath);
                }
            } else {
                callbackCancel();
            }
        }
    }

    /**
     * 切换文件夹
     */
    @Override
    public void clickFolderSwitch() {
        if (mFolderDialog == null) {
            mFolderDialog = new FolderDialog(this, mWidget, mAlbumFolders, (view, position) -> {
                mCurrentFolder = position;
                showFolderAlbumFiles(mCurrentFolder);
            });
        }
        if (!mFolderDialog.isShowing()) {
            mFolderDialog.show();
        }
    }

    /**
     * 显示某个文件夹下的图片
     */
    private void showFolderAlbumFiles(int position) {
        this.mCurrentFolder = position;
        AlbumFolder albumFolder = mAlbumFolders.get(position);
        mView.bindAlbumFolder(albumFolder);
    }

    /**
     * 点击拍照按钮
     */
    @Override
    public void clickCamera(View v) {
        int hasCheckSize = mCheckedList.size();
        // 超过最大选择数量 → 提示
        if (hasCheckSize >= mLimitCount) {
            int messageRes;
            switch (mFunction) {
                case Album.FUNCTION_CHOICE_IMAGE: {
                    messageRes = R.plurals.album_check_image_limit_camera;
                    break;
                }
                case Album.FUNCTION_CHOICE_VIDEO: {
                    messageRes = R.plurals.album_check_video_limit_camera;
                    break;
                }
                case Album.FUNCTION_CHOICE_ALBUM: {
                    messageRes = R.plurals.album_check_album_limit_camera;
                    break;
                }
                default: {
                    throw new AssertionError("This should not be the case.");
                }
            }
            mView.toast(getResources().getQuantityString(messageRes, mLimitCount, mLimitCount));
            // 根据功能类型拍照/录像/选择
        } else {
            switch (mFunction) {
                case Album.FUNCTION_CHOICE_IMAGE: {
                    takePicture();
                    break;
                }
                case Album.FUNCTION_CHOICE_VIDEO: {
                    takeVideo();
                    break;
                }
                case Album.FUNCTION_CHOICE_ALBUM: {
                    if (mCameraPopupMenu == null) {
                        mCameraPopupMenu = new PopupMenu(this, v);
                        mCameraPopupMenu.getMenuInflater().inflate(R.menu.album_menu_item_camera, mCameraPopupMenu.getMenu());
                        mCameraPopupMenu.setOnMenuItemClickListener(item -> {
                            int id = item.getItemId();
                            if (id == R.id.album_menu_camera_image) {
                                takePicture();
                            } else if (id == R.id.album_menu_camera_video) {
                                takeVideo();
                            }
                            return true;
                        });
                    }
                    mCameraPopupMenu.show();
                    break;
                }
                default: {
                    throw new AssertionError("This should not be the case.");
                }
            }
        }
    }

    /**
     * 拍照
     */
    private void takePicture() {
        String filePath;
        if (mCurrentFolder == 0) {
            filePath = AlbumUtil.randomJPGPath();
        } else {
            File file = new File(mAlbumFolders.get(mCurrentFolder).getAlbumFiles().get(0).getPath());
            filePath = AlbumUtil.randomJPGPath(file.getParentFile());
        }
//        String filePath = StorageUtil.getOutputFile(StorageUtil.StorageType.IMAGE).getPath();
        Album.camera(this)
                .image()
                .filePath(filePath)
                .onResult(mCameraAction)
                .start();
    }

    /**
     * 录像
     */
    private void takeVideo() {
        String filePath;
        if (mCurrentFolder == 0) {
            filePath = AlbumUtil.randomMP4Path();
        } else {
            File file = new File(mAlbumFolders.get(mCurrentFolder).getAlbumFiles().get(0).getPath());
            filePath = AlbumUtil.randomMP4Path(file.getParentFile());
        }
        Album.camera(this)
                .video()
                .filePath(filePath)
                .quality(mQuality)
                .limitDuration(mLimitDuration)
                .limitBytes(mLimitBytes)
                .onResult(mCameraAction)
                .start();
    }

    /**
     * 拍照/录像完成 → 插入相册
     */
    private final Action<String> mCameraAction = new Action<>() {
        @Override
        public void onAction(@NonNull String result) {
            if (mMediaScanner == null) {
                mMediaScanner = new MediaScanner(AlbumActivity.this);
            }
            mMediaScanner.scan(result);
            PathConversion conversion = new PathConversion(sSizeFilter, sMimeFilter, sDurationFilter);
            PathConvertTask task = new PathConvertTask(conversion, AlbumActivity.this);
            task.execute(result);
        }
    };

    /**
     * 路径转换回调
     */
    @Override
    public void onConvertStart() {
        showLoadingDialog();
        mLoadingDialog.setMessage(R.string.album_converting);
    }

    @Override
    public void onConvertCallback(AlbumFile albumFile) {
        albumFile.setChecked(!albumFile.isDisable());
        if (albumFile.isDisable()) {
            if (mFilterVisibility) {
                addFileToList(albumFile);
            } else {
                mView.toast(getString(R.string.album_take_file_unavailable));
            }
        } else {
            // 添加到列表
            addFileToList(albumFile);
        }
        dismissLoadingDialog();
    }

    /**
     * 将新拍摄的图片插入列表
     */
    private void addFileToList(AlbumFile albumFile) {
        if (mCurrentFolder != 0) {
            List<AlbumFile> albumFiles = mAlbumFolders.get(0).getAlbumFiles();
            if (!albumFiles.isEmpty()) {
                albumFiles.add(0, albumFile);
            } else {
                albumFiles.add(albumFile);
            }
        }
        AlbumFolder albumFolder = mAlbumFolders.get(mCurrentFolder);
        List<AlbumFile> albumFiles = albumFolder.getAlbumFiles();
        if (albumFiles.isEmpty()) {
            albumFiles.add(albumFile);
            mView.bindAlbumFolder(albumFolder);
        } else {
            albumFiles.add(0, albumFile);
            mView.notifyInsertItem(mHasCamera ? 1 : 0);
        }
        mCheckedList.add(albumFile);
        int count = mCheckedList.size();
        mView.setCheckedCount(count);
        switch (mChoiceMode) {
            case Album.MODE_SINGLE: {
                callbackResult();
                break;
            }
            case Album.MODE_MULTIPLE: {
                break;
            }
            default: {
                throw new AssertionError("This should not be the case.");
            }
        }
    }

    /**
     * 选择/取消选择图片
     */
    @Override
    public void tryCheckItem(CompoundButton button, int position) {
        AlbumFile albumFile = mAlbumFolders.get(mCurrentFolder).getAlbumFiles().get(position);
        if (button.isChecked()) {
            if (mCheckedList.size() >= mLimitCount) {
                int messageRes;
                switch (mFunction) {
                    case Album.FUNCTION_CHOICE_IMAGE: {
                        messageRes = R.plurals.album_check_image_limit;
                        break;
                    }
                    case Album.FUNCTION_CHOICE_VIDEO: {
                        messageRes = R.plurals.album_check_video_limit;
                        break;
                    }
                    case Album.FUNCTION_CHOICE_ALBUM: {
                        messageRes = R.plurals.album_check_album_limit;
                        break;
                    }
                    default: {
                        throw new AssertionError("This should not be the case.");
                    }
                }
                mView.toast(getResources().getQuantityString(messageRes, mLimitCount, mLimitCount));
                button.setChecked(false);
            } else {
                albumFile.setChecked(true);
                mCheckedList.add(albumFile);
                setCheckedCount();
            }
        } else {
            albumFile.setChecked(false);
            mCheckedList.remove(albumFile);
            setCheckedCount();
        }
    }

    /**
     * 更新已选数量
     */
    private void setCheckedCount() {
        int count = mCheckedList.size();
        mView.setCheckedCount(count);
    }

    /**
     * 预览图片
     */
    @Override
    public void tryPreviewItem(int position) {
        switch (mChoiceMode) {
            // 单选 → 直接返回
            case Album.MODE_SINGLE: {
                AlbumFile albumFile = mAlbumFolders.get(mCurrentFolder).getAlbumFiles().get(position);
                mCheckedList.add(albumFile);
                setCheckedCount();
                callbackResult();
                break;
            }
            // 多选 → 打开预览页
            case Album.MODE_MULTIPLE: {
                GalleryActivity.sAlbumFiles = mAlbumFolders.get(mCurrentFolder).getAlbumFiles();
                GalleryActivity.sCheckedCount = mCheckedList.size();
                GalleryActivity.sCurrentPosition = position;
                GalleryActivity.sCallback = this;
                Intent intent = new Intent(this, GalleryActivity.class);
                intent.putExtras(getIntent());
                startActivity(intent);
                break;
            }
            default: {
                throw new AssertionError("This should not be the case.");
            }
        }
    }

    /**
     * 预览已选中的图片
     */
    @Override
    public void tryPreviewChecked() {
        if (!mCheckedList.isEmpty()) {
            GalleryActivity.sAlbumFiles = new ArrayList<>(mCheckedList);
            GalleryActivity.sCheckedCount = mCheckedList.size();
            GalleryActivity.sCurrentPosition = 0;
            GalleryActivity.sCallback = this;
            Intent intent = new Intent(this, GalleryActivity.class);
            intent.putExtras(getIntent());
            startActivity(intent);
        }
    }

    /**
     * 预览页回调
     */
    @Override
    public void onPreviewComplete() {
        callbackResult();
    }

    @Override
    public void onPreviewChanged(AlbumFile albumFile) {
        ArrayList<AlbumFile> albumFiles = mAlbumFolders.get(mCurrentFolder).getAlbumFiles();
        int position = albumFiles.indexOf(albumFile);
        int notifyPosition = mHasCamera ? position + 1 : position;
        mView.notifyItem(notifyPosition);
        if (albumFile.isChecked()) {
            if (!mCheckedList.contains(albumFile)) mCheckedList.add(albumFile);
        } else {
            if (mCheckedList.contains(albumFile)) mCheckedList.remove(albumFile);
        }
        setCheckedCount();
    }

    /**
     * 点击完成
     */
    @Override
    public void complete() {
        if (mCheckedList.isEmpty()) {
            int messageRes;
            switch (mFunction) {
                case Album.FUNCTION_CHOICE_IMAGE: {
                    messageRes = R.string.album_check_image_little;
                    break;
                }
                case Album.FUNCTION_CHOICE_VIDEO: {
                    messageRes = R.string.album_check_video_little;
                    break;
                }
                case Album.FUNCTION_CHOICE_ALBUM: {
                    messageRes = R.string.album_check_album_little;
                    break;
                }
                default: {
                    throw new AssertionError("This should not be the case.");
                }
            }
            mView.toast(messageRes);
        } else {
            callbackResult();
        }
    }

    /**
     * 最终回调：生成缩略图并返回
     */
    private void callbackResult() {
        ThumbnailBuildTask task = new ThumbnailBuildTask(this, mCheckedList, this);
        task.execute();
    }

    @Override
    public void onThumbnailStart() {
        showLoadingDialog();
        mLoadingDialog.setMessage(R.string.album_thumbnail);
    }

    @Override
    public void onThumbnailCallback(ArrayList<AlbumFile> albumFiles) {
        if (sResult != null) {
            sResult.onAction(albumFiles);
        }
        dismissLoadingDialog();
        finish();
    }

    /**
     * 取消
     */
    private void callbackCancel() {
        if (sCancel != null) {
            sCancel.onAction("User canceled.");
        }
        finish();
    }

    /**
     * 加载对话框
     */
    private void showLoadingDialog() {
        if (mLoadingDialog == null) {
            mLoadingDialog = new LoadingDialog(this);
            mLoadingDialog.setupViews(mWidget);
        }
        if (!mLoadingDialog.isShowing()) {
            mLoadingDialog.show();
        }
    }

    public void dismissLoadingDialog() {
        if (mLoadingDialog != null && mLoadingDialog.isShowing()) {
            mLoadingDialog.dismiss();
        }
    }

    /**
     * 销毁
     */
    @Override
    public void finish() {
        sSizeFilter = null;
        sMimeFilter = null;
        sDurationFilter = null;
        sResult = null;
        sCancel = null;
        super.finish();
    }

}