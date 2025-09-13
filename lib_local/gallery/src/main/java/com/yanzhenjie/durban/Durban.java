package com.yanzhenjie.durban;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;

import androidx.annotation.ColorRes;
import androidx.annotation.IntDef;
import androidx.annotation.IntRange;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import java.io.OutputStream;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;

/**
 * <p>Entrance.</p>
 * Create by Yan Zhenjie on 2017/5/23.
 */
public class Durban {
    /**
     * 页面跳转参数
     */
    private static final String KEY_PREFIX = "AlbumCrop";
    static final String KEY_INPUT_STATUS_COLOR = KEY_PREFIX + ".KEY_INPUT_STATUS_COLOR";
    static final String KEY_INPUT_NAVIGATION_COLOR = KEY_PREFIX + ".KEY_INPUT_NAVIGATION_COLOR";
    static final String KEY_INPUT_TITLE = KEY_PREFIX + ".KEY_INPUT_TITLE";
    static final String KEY_INPUT_GESTURE = KEY_PREFIX + ".KEY_INPUT_GESTURE";
    static final String KEY_INPUT_ASPECT_RATIO = KEY_PREFIX + ".KEY_INPUT_ASPECT_RATIO";
    static final String KEY_INPUT_MAX_WIDTH_HEIGHT = KEY_PREFIX + ".KEY_INPUT_MAX_WIDTH_HEIGHT";
    static final String KEY_INPUT_COMPRESS_FORMAT = KEY_PREFIX + ".KEY_INPUT_COMPRESS_FORMAT";
    static final String KEY_INPUT_COMPRESS_QUALITY = KEY_PREFIX + ".KEY_INPUT_COMPRESS_QUALITY";
    static final String KEY_INPUT_DIRECTORY = KEY_PREFIX + ".KEY_INPUT_DIRECTORY";
    static final String KEY_INPUT_PATH_ARRAY = KEY_PREFIX + ".KEY_INPUT_PATH_ARRAY";
    static final String KEY_INPUT_CONTROLLER = KEY_PREFIX + ".KEY_INPUT_CONTROLLER";
    static final String KEY_OUTPUT_IMAGE_LIST = KEY_PREFIX + ".KEY_OUTPUT_IMAGE_LIST";

    /**
     * 不允许任何手势
     */
    public static final int GESTURE_NONE = 0;
    /**
     * 允许缩放
     */
    public static final int GESTURE_SCALE = 1;
    /**
     * 允许旋转
     */
    public static final int GESTURE_ROTATE = 2;
    /**
     * 允许旋转和缩放
     */
    public static final int GESTURE_ALL = 3;

    @IntDef({GESTURE_NONE, GESTURE_SCALE, GESTURE_ROTATE, GESTURE_ALL})
    @Retention(RetentionPolicy.SOURCE)
    public @interface GestureTypes {
    }

    /**
     * JPEG 格式
     */
    public static final int COMPRESS_JPEG = 0;
    /**
     * PNG 格式
     */
    public static final int COMPRESS_PNG = 1;

    @IntDef({COMPRESS_JPEG, COMPRESS_PNG})
    @Retention(RetentionPolicy.SOURCE)
    public @interface FormatTypes {
    }

    /**
     * 类本身持有对象
     */
    private Object o;
    private Intent mCropIntent;

    /**
     * 优先构建好传输的intent
     */
    private Durban(Object o) {
        this.o = o;
        mCropIntent = new Intent(getContext(o), DurbanActivity.class);
    }

    /**
     * activity/fragment如果是需要在对应页面的onActivityResult获取到值,通过该方法实现拿取上下文,然后映射的方式进行页面跳转
     */
    @NonNull
    protected static Context getContext(Object o) {
        if (o instanceof Activity) return (Context) o;
        else if (o instanceof Fragment) return ((Fragment) o).getContext();
        else if (o instanceof android.app.Fragment) return ((android.app.Fragment) o).getActivity();
        throw new IllegalArgumentException(o.getClass() + " is not supported.");
    }

    /**
     * 发起裁剪的页面需要得到裁剪后的图片路径集合的时候在onActivityResult中使用
     * override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
     * super.onActivityResult(requestCode, resultCode, data)
     * if (requestCode == RESULT_ALBUM) {
     * data ?: return
     * val mImageList = Durban.parseResult(data)
     * mImageList.safeGet(0).shortToast()
     * }
     * }
     */
    public static ArrayList<String> parseResult(@NonNull Intent intent) {
        return intent.getStringArrayListExtra(KEY_OUTPUT_IMAGE_LIST);
    }

    /**
     * 构造方法
     */
    public static Durban with(Activity activity) {
        return new Durban(activity);
    }

    public static Durban with(Fragment fragment) {
        return new Durban(fragment);
    }

    public static Durban with(android.app.Fragment fragment) {
        return new Durban(fragment);
    }

    /**
     * 设置状态栏背景颜色/通常随标题栏背景
     */
    public Durban statusBarColor(@ColorRes int color) {
        mCropIntent.putExtra(KEY_INPUT_STATUS_COLOR, color);
        return this;
    }

    /**
     * 设置导航栏背景背景
     */
    public Durban navigationBarColor(@ColorRes int color) {
        mCropIntent.putExtra(KEY_INPUT_NAVIGATION_COLOR, color);
        return this;
    }

    /**
     * 设置标题
     */
    public Durban title(String title) {
        mCropIntent.putExtra(KEY_INPUT_TITLE, title);
        return this;
    }

    /**
     * 裁剪时的手势支持
     *
     * @param gesture gesture sign.
     * @see #GESTURE_NONE
     * @see #GESTURE_ALL
     * @see #GESTURE_ROTATE
     * @see #GESTURE_SCALE
     */
    public Durban gesture(@GestureTypes int gesture) {
        mCropIntent.putExtra(KEY_INPUT_GESTURE, gesture);
        return this;
    }

    /**
     * 裁剪时的宽高比
     *
     * @param x aspect ratio X.
     * @param y aspect ratio Y.
     */
    public Durban aspectRatio(float x, float y) {
        mCropIntent.putExtra(KEY_INPUT_ASPECT_RATIO, new float[]{x, y});
        return this;
    }

    /**
     * 裁剪时使用原始图像的纵横比列
     */
    public Durban aspectRatioWithSourceImage() {
        return aspectRatio(0F, 0F);
    }

    /**
     * 裁剪图片输出的最大宽高
     *
     * @param width  max cropped image width.
     * @param height max cropped image height.
     */
    public Durban maxWidthHeight(@IntRange(from = 100) int width, @IntRange(from = 100) int height) {
        mCropIntent.putExtra(KEY_INPUT_MAX_WIDTH_HEIGHT, new int[]{width, height});
        return this;
    }

    /**
     * 图片压缩格式：JPEG、PNG
     *
     * @param format image format.
     * @see #COMPRESS_JPEG
     * @see #COMPRESS_PNG
     */
    public Durban compressFormat(@FormatTypes int format) {
        mCropIntent.putExtra(KEY_INPUT_COMPRESS_FORMAT, format);
        return this;
    }

    /**
     * 图片压缩质量，请参考：Bitmap#compress(Bitmap.CompressFormat, int, OutputStream)
     *
     * @param quality see {@link Bitmap#compress(Bitmap.CompressFormat, int, OutputStream)}.
     * @see Bitmap#compress(Bitmap.CompressFormat, int, OutputStream)
     */
    public Durban compressQuality(int quality) {
        mCropIntent.putExtra(KEY_INPUT_COMPRESS_QUALITY, quality);
        return this;
    }

    /**
     * 图片输出文件夹路径
     */
    public Durban outputDirectory(String folder) {
        mCropIntent.putExtra(KEY_INPUT_DIRECTORY, folder);
        return this;
    }

    /**
     * 图片路径
     */
    public Durban inputImagePaths(String... imagePathArray) {
        ArrayList<String> arrayList = new ArrayList<>();
        Collections.addAll(arrayList, imagePathArray);
        mCropIntent.putStringArrayListExtra(KEY_INPUT_PATH_ARRAY, arrayList);
        return this;
    }

    /**
     * 图片路径list
     */
    public Durban inputImagePaths(ArrayList<String> imagePathList) {
        mCropIntent.putStringArrayListExtra(KEY_INPUT_PATH_ARRAY, imagePathList);
        return this;
    }

    /**
     * 底部操作盘配置
     */
    public Durban controller(Controller controller) {
        mCropIntent.putExtra(KEY_INPUT_CONTROLLER, controller);
        return this;
    }

    /**
     * 页面的Request code回调编码, callback to {@code onActivityResult()}.
     */
    public Durban requestCode(int requestCode) {
        mCropIntent.putExtra("requestCode", requestCode);
        return this;
    }

    /**
     * 通过反射跳转执行startActivityForResult,
     */
    public void start() {
        try {
            Method method = o.getClass().getMethod("startActivityForResult", Intent.class, int.class);
            if (!method.isAccessible()) method.setAccessible(true);
            method.invoke(o, mCropIntent, mCropIntent.getIntExtra("requestCode", 1));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}