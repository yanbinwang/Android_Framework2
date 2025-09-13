package com.yanzhenjie.durban.task;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.media.ExifInterface;
import android.os.AsyncTask;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.yanzhenjie.durban.callback.BitmapCropCallback;
import com.yanzhenjie.durban.error.StorageError;
import com.yanzhenjie.durban.model.CropParameters;
import com.yanzhenjie.durban.model.ImageState;
import com.yanzhenjie.durban.util.FileUtils;
import com.yanzhenjie.durban.util.ImageHeaderParser;
import com.yanzhenjie.loading.dialog.LoadingDialog;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

/**
 * <p>Cut and save the image asynchronously.</p>
 * Create by Yan Zhenjie on 2017/5/22.
 */
public class BitmapCropTask extends AsyncTask<Void, Void, BitmapCropTask.PathWorkerResult> {

    static class PathWorkerResult {

        final String path;
        final Exception exception;

        PathWorkerResult(String path, Exception exception) {
            this.path = path;
            this.exception = exception;
        }
    }

    private LoadingDialog mDialog;

    private Bitmap mViewBitmap;

    private final RectF mCropRect;
    private final RectF mCurrentImageRect;

    private float mCurrentScale, mCurrentAngle;
    private final int mMaxResultImageSizeX, mMaxResultImageSizeY;

    private final Bitmap.CompressFormat mCompressFormat;
    private final int mCompressQuality;
    private final String mInputImagePath;
    private final String mOutputDirectory;
    private final BitmapCropCallback mCallback;

    private int mCroppedImageWidth, mCroppedImageHeight;

    public BitmapCropTask(@NonNull Context context,
                          @Nullable Bitmap viewBitmap,
                          @NonNull ImageState imageState,
                          @NonNull CropParameters cropParameters,
                          @Nullable BitmapCropCallback cropCallback) {
        mDialog = new LoadingDialog(context);

        mViewBitmap = viewBitmap;
        mCropRect = imageState.getCropRect();
        mCurrentImageRect = imageState.getCurrentImageRect();

        mCurrentScale = imageState.getCurrentScale();
        mCurrentAngle = imageState.getCurrentAngle();
        mMaxResultImageSizeX = cropParameters.getMaxResultImageSizeX();
        mMaxResultImageSizeY = cropParameters.getMaxResultImageSizeY();

        mCompressFormat = cropParameters.getCompressFormat();
        mCompressQuality = cropParameters.getCompressQuality();

        mInputImagePath = cropParameters.getImagePath();
        mOutputDirectory = cropParameters.getImageOutputPath();
        mCallback = cropCallback;
    }

    @Override
    protected void onPreExecute() {
        if (!mDialog.isShowing()) mDialog.show();
    }

    @Override
    protected void onPostExecute(PathWorkerResult result) {
        if (mDialog.isShowing()) mDialog.dismiss();

        if (mCallback != null) {
            if (result.exception == null) {
                mCallback.onBitmapCropped(
                        result.path,
                        mCroppedImageWidth,
                        mCroppedImageHeight);
            } else {
                mCallback.onCropFailure(result.exception);
            }
        }
    }

    @Override
    protected PathWorkerResult doInBackground(Void... params) {
        try {
            String imagePath = crop();
            return new PathWorkerResult(imagePath, null);
        } catch (Exception e) {
            return new PathWorkerResult(null, e);
        }
    }

    private String crop() throws Exception {
        FileUtils.validateDirectory(mOutputDirectory);

        String fileName = FileUtils.randomImageName(mCompressFormat);
        String outputImagePath = new File(mOutputDirectory, fileName).getAbsolutePath();

        // Downsize if needed
        if (mMaxResultImageSizeX > 0 && mMaxResultImageSizeY > 0) {
            float cropWidth = mCropRect.width() / mCurrentScale;
            float cropHeight = mCropRect.height() / mCurrentScale;

            if (cropWidth > mMaxResultImageSizeX || cropHeight > mMaxResultImageSizeY) {

                float scaleX = mMaxResultImageSizeX / cropWidth;
                float scaleY = mMaxResultImageSizeY / cropHeight;
                float resizeScale = Math.min(scaleX, scaleY);

                Bitmap resizedBitmap = Bitmap.createScaledBitmap(
                        mViewBitmap,
                        Math.round(mViewBitmap.getWidth() * resizeScale),
                        Math.round(mViewBitmap.getHeight() * resizeScale),
                        false);

                if (mViewBitmap != resizedBitmap)
                    mViewBitmap.recycle();
                mViewBitmap = resizedBitmap;
                mCurrentScale /= resizeScale;
            }
        }

        // Rotate if needed
        if (mCurrentAngle != 0) {
            Matrix tempMatrix = new Matrix();
            tempMatrix.setRotate(mCurrentAngle, mViewBitmap.getWidth() / 2, mViewBitmap.getHeight() / 2);

            Bitmap rotatedBitmap = Bitmap.createBitmap(
                    mViewBitmap,
                    0,
                    0,
                    mViewBitmap.getWidth(),
                    mViewBitmap.getHeight(),
                    tempMatrix, true);

            if (mViewBitmap != rotatedBitmap)
                mViewBitmap.recycle();
            mViewBitmap = rotatedBitmap;
        }

        int cropOffsetX = Math.round((mCropRect.left - mCurrentImageRect.left) / mCurrentScale);
        int cropOffsetY = Math.round((mCropRect.top - mCurrentImageRect.top) / mCurrentScale);
        mCroppedImageWidth = Math.round(mCropRect.width() / mCurrentScale);
        mCroppedImageHeight = Math.round(mCropRect.height() / mCurrentScale);

        boolean shouldCrop = shouldCrop(mCroppedImageWidth, mCroppedImageHeight);

        if (shouldCrop) {
            Bitmap croppedBitmap = Bitmap.createBitmap(
                    mViewBitmap,
                    cropOffsetX,
                    cropOffsetY,
                    mCroppedImageWidth,
                    mCroppedImageHeight);

            OutputStream outputStream = null;
            try {
                outputStream = new FileOutputStream(outputImagePath);
                croppedBitmap.compress(mCompressFormat, mCompressQuality, outputStream);
            } catch (Exception e) {
                throw new StorageError("");
            } finally {
                croppedBitmap.recycle();
                FileUtils.close(outputStream);
            }

            if (mCompressFormat.equals(Bitmap.CompressFormat.JPEG)) {
                ExifInterface originalExif = new ExifInterface(mInputImagePath);
                ImageHeaderParser.copyExif(originalExif, mCroppedImageWidth, mCroppedImageHeight, outputImagePath);
            }
        } else {
            FileUtils.copyFile(mInputImagePath, outputImagePath);
        }
        if (mViewBitmap != null && !mViewBitmap.isRecycled()) mViewBitmap.recycle();

        return outputImagePath;
    }

    /**
     * Check whether an image should be cropped at all or just file can be copied to the destination path.
     * For each 1000 pixels there is one pixel of error due to matrix calculations etc.
     *
     * @param width  - crop area width
     * @param height - crop area height
     * @return - true if image must be cropped, false - if original image fits requirements
     */
    private boolean shouldCrop(int width, int height) {
        int pixelError = 1;
        pixelError += Math.round(Math.max(width, height) / 1000f);
        return (mMaxResultImageSizeX > 0 && mMaxResultImageSizeY > 0)
                || Math.abs(mCropRect.left - mCurrentImageRect.left) > pixelError
                || Math.abs(mCropRect.top - mCurrentImageRect.top) > pixelError
                || Math.abs(mCropRect.bottom - mCurrentImageRect.bottom) > pixelError
                || Math.abs(mCropRect.right - mCurrentImageRect.right) > pixelError;
    }

}