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
package com.yanzhenjie.durban.task;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.AsyncTask;

import androidx.annotation.NonNull;

import com.yanzhenjie.durban.callback.BitmapLoadCallback;
import com.yanzhenjie.durban.model.ExifInfo;
import com.yanzhenjie.durban.util.BitmapLoadUtils;
import com.yanzhenjie.loading.dialog.LoadingDialog;

/**
 * <p>
 * Asynchronous loading of images.
 * </p>
 * Create by Yan Zhenjie on 2017/5/22.
 */
public class BitmapLoadTask extends AsyncTask<String, Void, BitmapLoadTask.BitmapWorkerResult> {

    static class BitmapWorkerResult {

        final Bitmap bitmap;
        final ExifInfo exifInfo;

        BitmapWorkerResult(Bitmap bitmapResult, ExifInfo exifInfo) {
            this.bitmap = bitmapResult;
            this.exifInfo = exifInfo;
        }
    }

    private final LoadingDialog mDialog;
    private final int mRequiredWidth;
    private final int mRequiredHeight;

    private final BitmapLoadCallback mCallback;

    public BitmapLoadTask(
            Context context,
            int requiredWidth,
            int requiredHeight,
            BitmapLoadCallback loadCallback) {
        mDialog = new LoadingDialog(context);
        mRequiredWidth = requiredWidth;
        mRequiredHeight = requiredHeight;
        mCallback = loadCallback;
    }

    @Override
    protected void onPreExecute() {
        if (!mDialog.isShowing()) mDialog.show();
    }

    @Override
    protected void onPostExecute(@NonNull BitmapLoadTask.BitmapWorkerResult result) {
        if (mDialog.isShowing()) mDialog.dismiss();
        if (result.bitmap == null) {
            mCallback.onFailure();
        } else {
            mCallback.onSuccessfully(result.bitmap, result.exifInfo);
        }
    }

    @Override
    protected BitmapWorkerResult doInBackground(String... params) {
        String imagePath = params[0];

        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(imagePath, options);
        if (options.outWidth == -1 || options.outHeight == -1)
            return new BitmapWorkerResult(null, null);

        options.inSampleSize = BitmapLoadUtils.calculateInSampleSize(options, mRequiredWidth, mRequiredHeight);
        options.inJustDecodeBounds = false;

        Bitmap decodeSampledBitmap = null;

        boolean decodeAttemptSuccess = false;
        while (!decodeAttemptSuccess) {
            try {
                decodeSampledBitmap = BitmapFactory.decodeFile(imagePath, options);
                decodeAttemptSuccess = true;
            } catch (Throwable error) {
                options.inSampleSize *= 2;
            }
        }

        int exifOrientation = BitmapLoadUtils.getExifOrientation(imagePath);
        int exifDegrees = BitmapLoadUtils.exifToDegrees(exifOrientation);
        int exifTranslation = BitmapLoadUtils.exifToTranslation(exifOrientation);

        ExifInfo exifInfo = new ExifInfo(exifOrientation, exifDegrees, exifTranslation);

        Matrix matrix = new Matrix();
        if (exifDegrees != 0)
            matrix.preRotate(exifDegrees);
        if (exifTranslation != 1)
            matrix.postScale(exifTranslation, 1);
        if (!matrix.isIdentity())
            return new BitmapWorkerResult(BitmapLoadUtils.transformBitmap(decodeSampledBitmap, matrix), exifInfo);
        return new BitmapWorkerResult(decodeSampledBitmap, exifInfo);
    }

}
