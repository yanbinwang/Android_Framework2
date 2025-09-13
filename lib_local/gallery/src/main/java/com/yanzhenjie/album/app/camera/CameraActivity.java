package com.yanzhenjie.album.app.camera;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;

import androidx.annotation.Nullable;

import com.yanzhenjie.album.Action;
import com.yanzhenjie.album.Album;
import com.yanzhenjie.album.mvp.BaseActivity;
import com.yanzhenjie.album.util.AlbumUtils;
import com.yanzhenjie.album.util.SystemBar;

import java.io.File;

/**
 * Created by YanZhenjie on 2017/8/16.
 */
public class CameraActivity extends BaseActivity {
    private static final String INSTANCE_CAMERA_FUNCTION = "INSTANCE_CAMERA_FUNCTION";
    private static final String INSTANCE_CAMERA_FILE_PATH = "INSTANCE_CAMERA_FILE_PATH";
    private static final String INSTANCE_CAMERA_QUALITY = "INSTANCE_CAMERA_QUALITY";
    private static final String INSTANCE_CAMERA_DURATION = "INSTANCE_CAMERA_DURATION";
    private static final String INSTANCE_CAMERA_BYTES = "INSTANCE_CAMERA_BYTES";

    private static final int CODE_ACTIVITY_TAKE_IMAGE = 1;
    private static final int CODE_ACTIVITY_TAKE_VIDEO = 2;

    public static Action<String> sResult;
    public static Action<String> sCancel;

    private int mFunction;
    private String mCameraFilePath;
    private int mQuality;
    private long mLimitDuration;
    private long mLimitBytes;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SystemBar.setStatusBarColor(this, Color.TRANSPARENT);
        SystemBar.setNavigationBarColor(this, Color.TRANSPARENT);
        SystemBar.invasionNavigationBar(this);
        SystemBar.invasionNavigationBar(this);
        if (savedInstanceState != null) {
            mFunction = savedInstanceState.getInt(INSTANCE_CAMERA_FUNCTION);
            mCameraFilePath = savedInstanceState.getString(INSTANCE_CAMERA_FILE_PATH);
            mQuality = savedInstanceState.getInt(INSTANCE_CAMERA_QUALITY);
            mLimitDuration = savedInstanceState.getLong(INSTANCE_CAMERA_DURATION);
            mLimitBytes = savedInstanceState.getLong(INSTANCE_CAMERA_BYTES);
        } else {
            Bundle bundle = getIntent().getExtras();
            assert bundle != null;
            mFunction = bundle.getInt(Album.KEY_INPUT_FUNCTION);
            mCameraFilePath = bundle.getString(Album.KEY_INPUT_FILE_PATH);
            mQuality = bundle.getInt(Album.KEY_INPUT_CAMERA_QUALITY);
            mLimitDuration = bundle.getLong(Album.KEY_INPUT_CAMERA_DURATION);
            mLimitBytes = bundle.getLong(Album.KEY_INPUT_CAMERA_BYTES);

            switch (mFunction) {
                case Album.FUNCTION_CAMERA_IMAGE: {
                    if (TextUtils.isEmpty(mCameraFilePath))
                        mCameraFilePath = AlbumUtils.randomJPGPath(this);
                    AlbumUtils.takeImage(this, CODE_ACTIVITY_TAKE_IMAGE, new File(mCameraFilePath));
                    break;
                }
                case Album.FUNCTION_CAMERA_VIDEO: {
                    if (TextUtils.isEmpty(mCameraFilePath))
                        mCameraFilePath = AlbumUtils.randomMP4Path(this);
                    AlbumUtils.takeVideo(this, CODE_ACTIVITY_TAKE_VIDEO, new File(mCameraFilePath), mQuality, mLimitDuration, mLimitBytes);
                    break;
                }
                default: {
                    throw new AssertionError("This should not be the case.");
                }
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putInt(INSTANCE_CAMERA_FUNCTION, mFunction);
        outState.putString(INSTANCE_CAMERA_FILE_PATH, mCameraFilePath);
        outState.putInt(INSTANCE_CAMERA_QUALITY, mQuality);
        outState.putLong(INSTANCE_CAMERA_DURATION, mLimitDuration);
        outState.putLong(INSTANCE_CAMERA_BYTES, mLimitBytes);
        super.onSaveInstanceState(outState);
    }

    @SuppressLint("MissingSuperCall")
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case CODE_ACTIVITY_TAKE_IMAGE:
            case CODE_ACTIVITY_TAKE_VIDEO: {
                if (resultCode == RESULT_OK) {
                    callbackResult();
                } else {
                    callbackCancel();
                }
                break;
            }
            default: {
                throw new AssertionError("This should not be the case.");
            }
        }
    }

    private void callbackResult() {
        if (sResult != null) sResult.onAction(mCameraFilePath);
        sResult = null;
        sCancel = null;
        finish();
    }

    private void callbackCancel() {
        if (sCancel != null) sCancel.onAction("User canceled.");
        sResult = null;
        sCancel = null;
        finish();
    }

}