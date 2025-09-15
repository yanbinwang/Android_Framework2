package com.yanzhenjie.album.app.album;

import static com.example.common.utils.ScreenUtil.shouldUseWhiteSystemBarsForRes;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;

import com.example.gallery.R;
import com.yanzhenjie.album.Action;
import com.yanzhenjie.album.Album;
import com.yanzhenjie.album.api.widget.Widget;
import com.yanzhenjie.album.app.Contract;
import com.yanzhenjie.album.mvp.BaseActivity;

/**
 * 相册内无任何数据的空页面
 * Created by YanZhenjie on 2017/3/28.
 */
public class NullActivity extends BaseActivity implements Contract.NullPresenter {
    private int mQuality = 1;
    private long mLimitDuration;
    private long mLimitBytes;
    private Widget mWidget;
    private Contract.NullView mView;
    private static final String KEY_OUTPUT_IMAGE_PATH = "KEY_OUTPUT_IMAGE_PATH";

    private Action<String> mCameraAction = result -> {
        Intent intent = new Intent();
        intent.putExtra(KEY_OUTPUT_IMAGE_PATH, result);
        setResult(RESULT_OK, intent);
        finish();
    };

    @Override
    protected boolean isImmersionBarEnabled() {
        return false;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.album_activity_null);
        overridePendingTransition(0, 0);
        mView = new NullView(this, this);
        Bundle argument = getIntent().getExtras();
        assert argument != null;
        int function = argument.getInt(Album.KEY_INPUT_FUNCTION);
        boolean hasCamera = argument.getBoolean(Album.KEY_INPUT_ALLOW_CAMERA);
        mQuality = argument.getInt(Album.KEY_INPUT_CAMERA_QUALITY);
        mLimitDuration = argument.getLong(Album.KEY_INPUT_CAMERA_DURATION);
        mLimitBytes = argument.getLong(Album.KEY_INPUT_CAMERA_BYTES);
        mWidget = argument.getParcelable(Album.KEY_INPUT_WIDGET);
        mView.setupViews(mWidget);
        mView.setTitle("");
        switch (function) {
            case Album.FUNCTION_CHOICE_IMAGE: {
                mView.setMessage(R.string.album_not_found_image);
                mView.setMakeVideoDisplay(false);
                break;
            }
            case Album.FUNCTION_CHOICE_VIDEO: {
                mView.setMessage(R.string.album_not_found_video);
                mView.setMakeImageDisplay(false);
                break;
            }
            case Album.FUNCTION_CHOICE_ALBUM: {
                mView.setMessage(R.string.album_not_found_album);
                break;
            }
            default: {
                throw new AssertionError("This should not be the case.");
            }
        }
        if (!hasCamera) {
            mView.setMakeImageDisplay(false);
            mView.setMakeVideoDisplay(false);
        }
        // 设置图标样式
        boolean statusBarBattery = shouldUseWhiteSystemBarsForRes(mWidget.getStatusBarColor());
        boolean navigationBarBattery = shouldUseWhiteSystemBarsForRes(mWidget.getNavigationBarColor());
        initImmersionBar(!statusBarBattery, !navigationBarBattery, mWidget.getNavigationBarColor());
    }

    @Override
    public void takePicture() {
        Album.camera(this)
                .image()
                .onResult(mCameraAction)
                .start();
    }

    @Override
    public void takeVideo() {
        Album.camera(this)
                .video()
                .quality(mQuality)
                .limitDuration(mLimitDuration)
                .limitBytes(mLimitBytes)
                .onResult(mCameraAction)
                .start();
    }

    public static String parsePath(Intent intent) {
        return intent.getStringExtra(KEY_OUTPUT_IMAGE_PATH);
    }

}