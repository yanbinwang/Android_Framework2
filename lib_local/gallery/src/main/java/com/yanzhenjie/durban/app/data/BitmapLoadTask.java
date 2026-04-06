package com.yanzhenjie.durban.app.data;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.AsyncTask;

import androidx.annotation.NonNull;

import com.yanzhenjie.durban.callback.BitmapLoadCallback;
import com.yanzhenjie.durban.model.ExifInfo;
import com.yanzhenjie.durban.utils.BitmapLoadUtil;
import com.yanzhenjie.loading.dialog.LoadingDialog;

/**
 * 图片加载异步任务
 * 作用：在子线程中按指定大小压缩加载图片，并自动纠正图片旋转方向
 */
public class BitmapLoadTask extends AsyncTask<String, Void, BitmapLoadTask.BitmapWorkerResult> {
    // 要求加载的图片最大宽高
    private final int mRequiredWidth;
    private final int mRequiredHeight;
    // 加载回调
    private final BitmapLoadCallback mCallback;
    // 加载对话框
    private final LoadingDialog mDialog;

    /**
     * 图片加载结果内部类
     */
    public static class BitmapWorkerResult {
        private final Bitmap bitmap;
        private final ExifInfo exifInfo;

        public BitmapWorkerResult(Bitmap bitmapResult, ExifInfo exifInfo) {
            this.bitmap = bitmapResult;
            this.exifInfo = exifInfo;
        }
    }

    /**
     * 构造图片加载任务
     */
    public BitmapLoadTask(Context context, int requiredWidth, int requiredHeight, BitmapLoadCallback loadCallback) {
        super();
        mDialog = new LoadingDialog(context);
        mRequiredWidth = requiredWidth;
        mRequiredHeight = requiredHeight;
        mCallback = loadCallback;
    }

    /**
     * 任务开始前：显示加载框
     */
    @Override
    protected void onPreExecute() {
        if (!mDialog.isShowing()) mDialog.show();
    }

    /**
     * 任务结束后：隐藏对话框 & 回调结果
     */
    @Override
    protected void onPostExecute(@NonNull BitmapLoadTask.BitmapWorkerResult result) {
        if (mDialog.isShowing()) mDialog.dismiss();
        if (result.bitmap == null) {
            mCallback.onFailure();
        } else {
            mCallback.onSuccessfully(result.bitmap, result.exifInfo);
        }
    }

    /**
     * 子线程：加载图片 + 计算缩放 + 纠正旋转
     */
    @Override
    protected BitmapWorkerResult doInBackground(String... params) {
        String imagePath = params[0];
        // 只读取图片宽高，不加载到内存
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(imagePath, options);
        // 图片无效
        if (options.outWidth == -1 || options.outHeight == -1) {
            return new BitmapWorkerResult(null, null);
        }
        // 计算缩放比例
        options.inSampleSize = BitmapLoadUtil.calculateInSampleSize(options, mRequiredWidth, mRequiredHeight);
        options.inJustDecodeBounds = false;
        Bitmap decodeSampledBitmap = null;
        boolean decodeAttemptSuccess = false;
        // 尝试加载图片，失败则增大缩放比例
        while (!decodeAttemptSuccess) {
            try {
                decodeSampledBitmap = BitmapFactory.decodeFile(imagePath, options);
                decodeAttemptSuccess = true;
            } catch (Throwable error) {
                options.inSampleSize *= 2;
            }
        }
        // 读取图片EXIF信息（旋转、方向）
        int exifOrientation = BitmapLoadUtil.getExifOrientation(imagePath);
        int exifDegrees = BitmapLoadUtil.exifToDegrees(exifOrientation);
        int exifTranslation = BitmapLoadUtil.exifToTranslation(exifOrientation);
        ExifInfo exifInfo = new ExifInfo(exifOrientation, exifDegrees, exifTranslation);
        // 纠正图片旋转
        Matrix matrix = new Matrix();
        if (exifDegrees != 0) {
            matrix.preRotate(exifDegrees);
        }
        if (exifTranslation != 1) {
            matrix.postScale(exifTranslation, 1);
        }
        if (!matrix.isIdentity()) {
            return new BitmapWorkerResult(BitmapLoadUtil.transformBitmap(decodeSampledBitmap, matrix), exifInfo);
        }
        return new BitmapWorkerResult(decodeSampledBitmap, exifInfo);
    }

}