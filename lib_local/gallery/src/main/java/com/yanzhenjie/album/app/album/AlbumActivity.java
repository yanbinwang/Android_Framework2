package com.yanzhenjie.album.app.album;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Looper;
import android.text.TextUtils;
import android.view.View;
import android.widget.CompoundButton;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.PopupMenu;

import com.example.framework.utils.WeakHandler;
import com.example.gallery.R;
import com.yanzhenjie.album.Action;
import com.yanzhenjie.album.Album;
import com.yanzhenjie.album.AlbumFile;
import com.yanzhenjie.album.AlbumFolder;
import com.yanzhenjie.album.Filter;
import com.yanzhenjie.album.api.widget.Widget;
import com.yanzhenjie.album.app.Contract;
import com.yanzhenjie.album.app.album.data.MediaReadTask;
import com.yanzhenjie.album.app.album.data.MediaReader;
import com.yanzhenjie.album.app.album.data.PathConversion;
import com.yanzhenjie.album.app.album.data.PathConvertTask;
import com.yanzhenjie.album.app.album.data.ThumbnailBuildTask;
import com.yanzhenjie.album.mvp.BaseActivity;
import com.yanzhenjie.album.util.AlbumUtils;
import com.yanzhenjie.album.widget.LoadingDialog;
import com.yanzhenjie.mediascanner.MediaScanner;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * <p>Responsible for controlling the album data and the overall logic.</p>
 * Created by Yan Zhenjie on 2016/10/17.
 */
public class AlbumActivity extends BaseActivity implements
        Contract.AlbumPresenter,
        MediaReadTask.Callback,
        GalleryActivity.Callback,
        PathConvertTask.Callback,
        ThumbnailBuildTask.Callback {

    private static final int CODE_ACTIVITY_NULL = 1;

    public static Filter<Long> sSizeFilter;
    public static Filter<String> sMimeFilter;
    public static Filter<Long> sDurationFilter;

    public static Action<ArrayList<AlbumFile>> sResult;
    public static Action<String> sCancel;

    private List<AlbumFolder> mAlbumFolders;
    private int mCurrentFolder;

    private Widget mWidget;
    private int mFunction;
    private int mChoiceMode;
    private int mColumnCount;
    private boolean mHasCamera;
    private int mLimitCount;

    private int mQuality;
    private long mLimitDuration;
    private long mLimitBytes;

    private boolean mFilterVisibility;

    private ArrayList<AlbumFile> mCheckedList;
    private MediaScanner mMediaScanner;

    private Contract.AlbumView mView;
    private FolderDialog mFolderDialog;
    private PopupMenu mCameraPopupMenu;
    private LoadingDialog mLoadingDialog;

    private MediaReadTask mMediaReadTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initializeArgument();
        setContentView(createView());
        mView = new AlbumView(this, this);
        mView.setupViews(mWidget, mColumnCount, mHasCamera, mChoiceMode);
        mView.setTitle("");
        mView.setCompleteDisplay(false);
        mView.setLoadingDisplay(true);

        // 设置图标样式
        boolean statusBarBattery = getBatteryIcon(mWidget.getStatusBarColor());
        boolean navigationBarBattery = getBatteryIcon(mWidget.getNavigationBarColor());
        initImmersionBar(!statusBarBattery, !navigationBarBattery, mWidget.getNavigationBarColor());

        // 扫描相册
        ArrayList<AlbumFile> checkedList = getIntent().getParcelableArrayListExtra(Album.KEY_INPUT_CHECKED_LIST);
        MediaReader mediaReader = new MediaReader(this, sSizeFilter, sMimeFilter, sDurationFilter, mFilterVisibility);
        mMediaReadTask = new MediaReadTask(mFunction, checkedList, mediaReader, this);
        mMediaReadTask.execute();
    }

    private void initializeArgument() {
        Bundle argument = getIntent().getExtras();
        assert argument != null;
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
    }

    /**
     * Use different layouts depending on the style.
     *
     * @return layout id.
     */
    private int createView() {
        switch (mWidget.getUiStyle()) {
            case Widget.STYLE_DARK: {
                return R.layout.album_activity_album_dark;
            }
            case Widget.STYLE_LIGHT: {
                return R.layout.album_activity_album_light;
            }
            default: {
                throw new AssertionError("This should not be the case.");
            }
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mView.onConfigurationChanged(newConfig);
        if (mFolderDialog != null && !mFolderDialog.isShowing()) mFolderDialog = null;
    }

    @Override
    public void onScanCallback(ArrayList<AlbumFolder> albumFolders, ArrayList<AlbumFile> checkedFiles) {
        mMediaReadTask = null;
        // 遮罩延迟半秒,有个页面过渡时间
        new WeakHandler(Looper.getMainLooper()).postDelayed(() -> {
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
        }, 500);
        mAlbumFolders = albumFolders;
        mCheckedList = checkedFiles;

        if (mAlbumFolders.get(0).getAlbumFiles().isEmpty()) {
            Intent intent = new Intent(this, NullActivity.class);
            intent.putExtras(getIntent());
            startActivityForResult(intent, CODE_ACTIVITY_NULL);
            overridePendingTransition(0, 0);
        } else {
            showFolderAlbumFiles(0);
            int count = mCheckedList.size();
            mView.setCheckedCount(count);
            mView.setSubTitle(count + "/" + mLimitCount);
        }
    }

    @SuppressLint("MissingSuperCall")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CODE_ACTIVITY_NULL) {
            if (resultCode == RESULT_OK) {
                String imagePath = NullActivity.parsePath(data);
                String mimeType = AlbumUtils.getMimeType(imagePath);
                if (!TextUtils.isEmpty(mimeType)) mCameraAction.onAction(imagePath);
            } else {
                callbackCancel();
            }
        }
    }

    @Override
    public void clickFolderSwitch() {
        if (mFolderDialog == null) {
            mFolderDialog = new FolderDialog(this, mWidget, mAlbumFolders, (view, position) -> {
                mCurrentFolder = position;
                showFolderAlbumFiles(mCurrentFolder);
            });
        }
        if (!mFolderDialog.isShowing()) mFolderDialog.show();
    }

    /**
     * Update data source.
     */
    private void showFolderAlbumFiles(int position) {
        this.mCurrentFolder = position;
        AlbumFolder albumFolder = mAlbumFolders.get(position);
        mView.bindAlbumFolder(albumFolder);
    }

    @Override
    public void clickCamera(View v) {
        int hasCheckSize = mCheckedList.size();
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

    private void takePicture() {
        String filePath;
        if (mCurrentFolder == 0) {
            filePath = AlbumUtils.randomJPGPath();
        } else {
            File file = new File(mAlbumFolders.get(mCurrentFolder).getAlbumFiles().get(0).getPath());
            filePath = AlbumUtils.randomJPGPath(file.getParentFile());
        }
        Album.camera(this)
                .image()
                .filePath(filePath)
                .onResult(mCameraAction)
                .start();
    }

    private void takeVideo() {
        String filePath;
        if (mCurrentFolder == 0) {
            filePath = AlbumUtils.randomMP4Path();
        } else {
            File file = new File(mAlbumFolders.get(mCurrentFolder).getAlbumFiles().get(0).getPath());
            filePath = AlbumUtils.randomMP4Path(file.getParentFile());
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

    private Action<String> mCameraAction = new Action<>() {
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

    @Override
    public void onConvertStart() {
        showLoadingDialog();
        mLoadingDialog.setMessage(R.string.album_converting);
    }

    @Override
    public void onConvertCallback(AlbumFile albumFile) {
        albumFile.setChecked(!albumFile.isDisable());
        if (albumFile.isDisable()) {
            if (mFilterVisibility) addFileToList(albumFile);
            else mView.toast(getString(R.string.album_take_file_unavailable));
        } else {
            addFileToList(albumFile);
        }

        dismissLoadingDialog();
    }

    private void addFileToList(AlbumFile albumFile) {
        if (mCurrentFolder != 0) {
            List<AlbumFile> albumFiles = mAlbumFolders.get(0).getAlbumFiles();
            if (albumFiles.size() > 0) albumFiles.add(0, albumFile);
            else albumFiles.add(albumFile);
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
        mView.setSubTitle(count + "/" + mLimitCount);

        switch (mChoiceMode) {
            case Album.MODE_SINGLE: {
                callbackResult();
                break;
            }
            case Album.MODE_MULTIPLE: {
                // Nothing.
                break;
            }
            default: {
                throw new AssertionError("This should not be the case.");
            }
        }
    }

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

    private void setCheckedCount() {
        int count = mCheckedList.size();
        mView.setCheckedCount(count);
        mView.setSubTitle(count + "/" + mLimitCount);
    }

    @Override
    public void tryPreviewItem(int position) {
        switch (mChoiceMode) {
            case Album.MODE_SINGLE: {
                AlbumFile albumFile = mAlbumFolders.get(mCurrentFolder).getAlbumFiles().get(position);
//                albumFile.setChecked(true);
//                mView.notifyItem(position);
                mCheckedList.add(albumFile);
                setCheckedCount();

                callbackResult();
                break;
            }
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

    @Override
    public void tryPreviewChecked() {
        if (mCheckedList.size() > 0) {
            GalleryActivity.sAlbumFiles = new ArrayList<>(mCheckedList);
            GalleryActivity.sCheckedCount = mCheckedList.size();
            GalleryActivity.sCurrentPosition = 0;
            GalleryActivity.sCallback = this;
            Intent intent = new Intent(this, GalleryActivity.class);
            intent.putExtras(getIntent());
            startActivity(intent);
        }
    }

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

    @SuppressLint("MissingSuperCall")
    @Override
    public void onBackPressed() {
        if (mMediaReadTask != null) mMediaReadTask.cancel(true);
        callbackCancel();
    }

    /**
     * Callback result action.
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
        if (sResult != null) sResult.onAction(albumFiles);
        dismissLoadingDialog();
        finish();
    }

    /**
     * Callback cancel action.
     */
    private void callbackCancel() {
        if (sCancel != null) sCancel.onAction("User canceled.");
        finish();
    }

    /**
     * Display loading dialog.
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

    /**
     * Dismiss loading dialog.
     */
    public void dismissLoadingDialog() {
        if (mLoadingDialog != null && mLoadingDialog.isShowing()) {
            mLoadingDialog.dismiss();
        }
    }

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