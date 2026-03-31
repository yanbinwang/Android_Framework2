package com.yanzhenjie.album.app.camera;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;

import androidx.annotation.Nullable;

import com.example.gallery.base.BaseActivity;
import com.yanzhenjie.album.Action;
import com.yanzhenjie.album.Album;
import com.yanzhenjie.album.utils.AlbumUtil;
import com.yanzhenjie.album.utils.SystemBar;

import java.io.File;

/**
 * 相机跳转页
 * 功能：调用系统相机拍照 / 录制视频
 * 拍完后把文件路径回调给外部
 */
public class CameraActivity extends BaseActivity {
    // 相机功能类型：拍照 / 录像
    private int mFunction;
    // 视频质量
    private int mQuality;
    // 视频最大时长
    private long mLimitDuration;
    // 视频最大大小
    private long mLimitBytes;
    // 拍照/录像保存的文件路径
    private String mCameraFilePath;
    // 相机请求码
    private static final int CODE_ACTIVITY_TAKE_IMAGE = 1;  // 拍照
    private static final int CODE_ACTIVITY_TAKE_VIDEO = 2;  // 录像
    // 屏幕旋转时保存参数的 Key
    private static final String INSTANCE_CAMERA_FUNCTION = "INSTANCE_CAMERA_FUNCTION";
    private static final String INSTANCE_CAMERA_FILE_PATH = "INSTANCE_CAMERA_FILE_PATH";
    private static final String INSTANCE_CAMERA_QUALITY = "INSTANCE_CAMERA_QUALITY";
    private static final String INSTANCE_CAMERA_DURATION = "INSTANCE_CAMERA_DURATION";
    private static final String INSTANCE_CAMERA_BYTES = "INSTANCE_CAMERA_BYTES";
    // 外部回调监听
    public static Action<String> sResult;  // 成功
    public static Action<String> sCancel;  // 取消

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 沉浸式状态栏 + 导航栏透明
        SystemBar.setStatusBarColor(this, Color.TRANSPARENT);
        SystemBar.setNavigationBarColor(this, Color.TRANSPARENT);
        SystemBar.invasionNavigationBar(this);
        // 页面重建（旋转屏幕）：恢复参数
        if (savedInstanceState != null) {
            mFunction = savedInstanceState.getInt(INSTANCE_CAMERA_FUNCTION);
            mCameraFilePath = savedInstanceState.getString(INSTANCE_CAMERA_FILE_PATH);
            mQuality = savedInstanceState.getInt(INSTANCE_CAMERA_QUALITY);
            mLimitDuration = savedInstanceState.getLong(INSTANCE_CAMERA_DURATION);
            mLimitBytes = savedInstanceState.getLong(INSTANCE_CAMERA_BYTES);
        } else {
            // 正常打开：获取外部传递的参数
            Bundle bundle = getIntent().getExtras();
            if (null != bundle) {
                mFunction = bundle.getInt(Album.KEY_INPUT_FUNCTION);
                mCameraFilePath = bundle.getString(Album.KEY_INPUT_FILE_PATH);
                mQuality = bundle.getInt(Album.KEY_INPUT_CAMERA_QUALITY);
                mLimitDuration = bundle.getLong(Album.KEY_INPUT_CAMERA_DURATION);
                mLimitBytes = bundle.getLong(Album.KEY_INPUT_CAMERA_BYTES);
                // 根据功能类型：打开系统相机
                switch (mFunction) {
                    // 拍照
                    case Album.FUNCTION_CAMERA_IMAGE: {
                        if (TextUtils.isEmpty(mCameraFilePath)) {
                            // 没有指定路径，自动生成一个
                            mCameraFilePath = AlbumUtil.randomJPGPath(this);
                        }
                        // 调用系统拍照
                        AlbumUtil.takeImage(this, CODE_ACTIVITY_TAKE_IMAGE, new File(mCameraFilePath));
                        break;
                    }
                    // 录像
                    case Album.FUNCTION_CAMERA_VIDEO: {
                        if (TextUtils.isEmpty(mCameraFilePath)) {
                            // 自动生成视频路径
                            mCameraFilePath = AlbumUtil.randomMP4Path(this);
                        }
                        // 调用系统录像
                        AlbumUtil.takeVideo(this, CODE_ACTIVITY_TAKE_VIDEO, new File(mCameraFilePath), mQuality, mLimitDuration, mLimitBytes);
                        break;
                    }
                    default: {
                        throw new AssertionError("This should not be the case.");
                    }
                }
            }
        }
    }

    /**
     * 屏幕旋转时保存参数
     */
    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putInt(INSTANCE_CAMERA_FUNCTION, mFunction);
        outState.putString(INSTANCE_CAMERA_FILE_PATH, mCameraFilePath);
        outState.putInt(INSTANCE_CAMERA_QUALITY, mQuality);
        outState.putLong(INSTANCE_CAMERA_DURATION, mLimitDuration);
        outState.putLong(INSTANCE_CAMERA_BYTES, mLimitBytes);
        super.onSaveInstanceState(outState);
    }

    /**
     * 相机拍摄完成后接收结果
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            // 拍照 / 录像 的结果
            case CODE_ACTIVITY_TAKE_IMAGE:
            case CODE_ACTIVITY_TAKE_VIDEO: {
                if (resultCode == RESULT_OK) {
                    // 拍摄成功
                    callbackResult();
                } else {
                    // 用户取消
                    callbackCancel();
                }
                break;
            }
            default: {
                throw new AssertionError("This should not be the case.");
            }
        }
    }

    /**
     * 成功回调：返回文件路径
     */
    private void callbackResult() {
        if (sResult != null) {
            sResult.onAction(mCameraFilePath);
        }
        // 清空监听，防止内存泄漏
        sResult = null;
        sCancel = null;
        finish();
    }

    /**
     * 取消回调
     */
    private void callbackCancel() {
        if (sCancel != null) {
            sCancel.onAction("User canceled.");
        }
        // 清空监听
        sResult = null;
        sCancel = null;
        finish();
    }

}