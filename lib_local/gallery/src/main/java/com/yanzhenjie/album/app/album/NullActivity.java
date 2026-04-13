package com.yanzhenjie.album.app.album;

import static android.transition.Visibility.MODE_IN;
import static android.transition.Visibility.MODE_OUT;
import static com.example.common.utils.ScreenUtil.shouldUseWhiteSystemBarsForRes;

import android.content.Intent;
import android.os.Bundle;
import android.transition.Fade;

import androidx.annotation.Nullable;

import com.example.gallery.R;
import com.example.gallery.base.BaseActivity;
import com.yanzhenjie.album.Album;
import com.yanzhenjie.album.app.Contract;
import com.yanzhenjie.album.callback.Action;
import com.yanzhenjie.album.model.Widget;

/**
 * 空页面
 * 功能：当手机里没有图片/视频时显示
 * 提供：拍照、录像入口，属于 MVP 中的 Presenter 层
 */
public class NullActivity extends BaseActivity implements Contract.NullPresenter {
    // 功能：图片/视频/全部
    private int mFunction;
    // 是否显示拍照按钮
    private boolean mHasCamera;
    // 视频质量
    private int mQuality = 1;
    // 视频最大时长
    private long mLimitDuration;
    // 视频最大大小
    private long mLimitBytes;
    // 主题样式
    private Widget mWidget;
    // 拍照/录像返回的路径 Key
    private static final String KEY_OUTPUT_IMAGE_PATH = "KEY_OUTPUT_IMAGE_PATH";

    @Override
    protected boolean isImmersionBarEnabled() {
        return false;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 获取参数
        Bundle argument = getIntent().getExtras();
        if (null != argument) {
            mWidget = argument.getParcelable(Album.KEY_INPUT_WIDGET);
            mFunction = argument.getInt(Album.KEY_INPUT_FUNCTION);
            mQuality = argument.getInt(Album.KEY_INPUT_CAMERA_QUALITY);
            mLimitDuration = argument.getLong(Album.KEY_INPUT_CAMERA_DURATION);
            mLimitBytes = argument.getLong(Album.KEY_INPUT_CAMERA_BYTES);
            mHasCamera = argument.getBoolean(Album.KEY_INPUT_ALLOW_CAMERA);
        } else {
            finish();
        }
        // 覆盖基类动画
        setActivityAnimations();
        overridePendingTransition(R.anim.set_alpha_in, R.anim.set_alpha_out);
        setContentView(R.layout.album_activity_null);
        // 初始化状态栏/导航栏颜色（黑白字体自适应）
        boolean statusBarBattery = shouldUseWhiteSystemBarsForRes(mWidget.getStatusBarColor());
        boolean navigationBarBattery = shouldUseWhiteSystemBarsForRes(mWidget.getNavigationBarColor());
        initImmersionBar(!statusBarBattery, !navigationBarBattery, mWidget.getNavigationBarColor());
        // 绑定 MVP
        Contract.NullView mView = new NullView(this, this);
        mView.setupViews(mWidget);
        // 根据当前功能类型，显示不同提示文案
        switch (mFunction) {
            // 只选图片：隐藏录像按钮
            case Album.FUNCTION_CHOICE_IMAGE: {
                mView.setMessage(R.string.album_not_found_image);
                mView.setMakeVideoDisplay(false);
                break;
            }
            // 只选视频：隐藏拍照按钮
            case Album.FUNCTION_CHOICE_VIDEO: {
                mView.setMessage(R.string.album_not_found_video);
                mView.setMakeImageDisplay(false);
                break;
            }
            // 全部媒体：都显示
            case Album.FUNCTION_CHOICE_ALBUM: {
                mView.setMessage(R.string.album_not_found_album);
                break;
            }
            default: {
                throw new AssertionError("This should not be the case.");
            }
        }
        // 如果不允许使用相机，隐藏两个按钮
        if (!mHasCamera) {
            mView.setMakeImageDisplay(false);
            mView.setMakeVideoDisplay(false);
        }
    }

    @Override
    protected void onNewIntent(@Nullable Intent intent) {
        super.onNewIntent(intent);
        setActivityAnimations();
    }

    private void setActivityAnimations() {
        Fade fadeEnter = new Fade(MODE_IN);
        fadeEnter.setDuration(300);
        getWindow().setExitTransition(fadeEnter);
        Fade fadeExit = new Fade(MODE_OUT);
        fadeEnter.setDuration(300);
        getWindow().setReturnTransition(fadeExit);
    }

    /**
     * 点击拍照
     */
    @Override
    public void takePicture() {
        Album.camera(this)
                .image()
                .onResult(mCameraAction)
                .start();
    }

    /**
     * 点击录像
     */
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

    /**
     * 相机拍摄完成的回调：返回路径
     */
    private final Action<String> mCameraAction = result -> {
        Intent intent = new Intent();
        intent.putExtra(KEY_OUTPUT_IMAGE_PATH, result);
        setResult(RESULT_OK, intent);
        finish();
    };

    /**
     * 外部解析返回路径
     */
    public static String parsePath(Intent intent) {
        return intent.getStringExtra(KEY_OUTPUT_IMAGE_PATH);
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.set_alpha_none, R.anim.set_alpha_none);
    }

}