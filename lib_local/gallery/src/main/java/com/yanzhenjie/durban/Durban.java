/*
 * Copyright © Yan Zhenjie
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.yanzhenjie.durban;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;

import androidx.annotation.ColorInt;
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
import java.util.Locale;

/**
 * <p>Entrance.</p>
 * Create by Yan Zhenjie on 2017/5/23.
 */
public class Durban {

    private static final String KEY_PREFIX = "AlbumCrop";

    static final String KEY_INPUT_STATUS_COLOR = KEY_PREFIX + ".KEY_INPUT_STATUS_COLOR";
    static final String KEY_INPUT_TOOLBAR_COLOR = KEY_PREFIX + ".KEY_INPUT_TOOLBAR_COLOR";
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
     * Do not allow any gestures.
     */
    public static final int GESTURE_NONE = 0;
    /**
     * Allow scaling.
     */
    public static final int GESTURE_SCALE = 1;
    /**
     * Allow rotation.
     */
    public static final int GESTURE_ROTATE = 2;
    /**
     * Allow rotation and scaling.
     */
    public static final int GESTURE_ALL = 3;

    @IntDef({GESTURE_NONE, GESTURE_SCALE, GESTURE_ROTATE, GESTURE_ALL})
    @Retention(RetentionPolicy.SOURCE)
    public @interface GestureTypes {
    }

    /**
     * JPEG format.
     */
    public static final int COMPRESS_JPEG = 0;
    /**
     * PNG format.
     */
    public static final int COMPRESS_PNG = 1;

    @IntDef({COMPRESS_JPEG, COMPRESS_PNG})
    @Retention(RetentionPolicy.SOURCE)
    public @interface FormatTypes {
    }

    private static DurbanConfig sDurbanConfig;

    /**
     * Initialize Album.
     *
     * @param durbanConfig {@link DurbanConfig}.
     */
    public static void initialize(DurbanConfig durbanConfig) {
        sDurbanConfig = durbanConfig;
    }

    /**
     * Get the durban configuration.
     *
     * @return {@link DurbanConfig}.
     */
    public static DurbanConfig getDurbanConfig() {
        if (sDurbanConfig == null) {
            initialize(DurbanConfig.newBuilder(null)
                    .setLocale(Locale.getDefault())
                    .build()
            );
        }
        return sDurbanConfig;
    }


    private Object o;
    private Intent mCropIntent;

    public static Durban with(Activity activity) {
        return new Durban(activity);
    }

    public static Durban with(Fragment fragment) {
        return new Durban(fragment);
    }

    public static Durban with(android.app.Fragment fragment) {
        return new Durban(fragment);
    }

    private Durban(Object o) {
        this.o = o;
        mCropIntent = new Intent(getContext(o), DurbanActivity.class);
    }

    /**
     * The color of the StatusBar.
     */
    public Durban statusBarColor(@ColorInt int color) {
        mCropIntent.putExtra(KEY_INPUT_STATUS_COLOR, color);
        return this;
    }

    /**
     * The color of the Toolbar.
     */
    public Durban toolBarColor(@ColorInt int color) {
        mCropIntent.putExtra(KEY_INPUT_TOOLBAR_COLOR, color);
        return this;
    }

    /**
     * Set the color of the NavigationBar.
     */
    public Durban navigationBarColor(@ColorInt int color) {
        mCropIntent.putExtra(KEY_INPUT_NAVIGATION_COLOR, color);
        return this;
    }

    /**
     * The title of the interface.
     */
    public Durban title(String title) {
        mCropIntent.putExtra(KEY_INPUT_TITLE, title);
        return this;
    }

    /**
     * The gestures that allow operation.
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
     * The aspect ratio column of the crop box.
     *
     * @param x aspect ratio X.
     * @param y aspect ratio Y.
     */
    public Durban aspectRatio(float x, float y) {
        mCropIntent.putExtra(KEY_INPUT_ASPECT_RATIO, new float[]{x, y});
        return this;
    }

    /**
     * Use the aspect ratio column of the original image.
     */
    public Durban aspectRatioWithSourceImage() {
        return aspectRatio(0F, 0F);
    }

    /**
     * Set maximum size for result cropped image.
     *
     * @param width  max cropped image width.
     * @param height max cropped image height.
     */
    public Durban maxWidthHeight(@IntRange(from = 100) int width, @IntRange(from = 100) int height) {
        mCropIntent.putExtra(KEY_INPUT_MAX_WIDTH_HEIGHT, new int[]{width, height});
        return this;
    }

    /**
     * The compression format of the cropped image.
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
     * The compression quality of the cropped image.
     *
     * @param quality see {@link Bitmap#compress(Bitmap.CompressFormat, int, OutputStream)}.
     * @see Bitmap#compress(Bitmap.CompressFormat, int, OutputStream)
     */
    public Durban compressQuality(int quality) {
        mCropIntent.putExtra(KEY_INPUT_COMPRESS_QUALITY, quality);
        return this;
    }

    /**
     * Set the output directory of the cropped picture.
     */
    public Durban outputDirectory(String folder) {
        mCropIntent.putExtra(KEY_INPUT_DIRECTORY, folder);
        return this;
    }

    /**
     * The pictures to be cropped.
     */
    public Durban inputImagePaths(String... imagePathArray) {
        ArrayList<String> arrayList = new ArrayList<>();
        Collections.addAll(arrayList, imagePathArray);
        mCropIntent.putStringArrayListExtra(KEY_INPUT_PATH_ARRAY, arrayList);
        return this;
    }

    /**
     * The pictures to be cropped.
     */
    public Durban inputImagePaths(ArrayList<String> imagePathList) {
        mCropIntent.putStringArrayListExtra(KEY_INPUT_PATH_ARRAY, imagePathList);
        return this;
    }

    /**
     * Control panel configuration.
     */
    public Durban controller(Controller controller) {
        mCropIntent.putExtra(KEY_INPUT_CONTROLLER, controller);
        return this;
    }

    /**
     * Request code, callback to {@code onActivityResult()}.
     */
    public Durban requestCode(int requestCode) {
        mCropIntent.putExtra("requestCode", requestCode);
        return this;
    }

    /**
     * Start cropping.
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

    /**
     * Analyze the crop results.
     */
    public static ArrayList<String> parseResult(@NonNull Intent intent) {
        return intent.getStringArrayListExtra(KEY_OUTPUT_IMAGE_LIST);
    }

    protected static
    @NonNull
    Context getContext(Object o) {
        if (o instanceof Activity) return (Context) o;
        else if (o instanceof Fragment) return ((Fragment) o).getContext();
        else if (o instanceof android.app.Fragment) ((android.app.Fragment) o).getActivity();
        throw new IllegalArgumentException(o.getClass() + " is not supported.");
    }

}