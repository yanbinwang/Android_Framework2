package com.yanzhenjie.durban.app.data;

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
import com.yanzhenjie.durban.utils.FileUtil;
import com.yanzhenjie.durban.utils.ImageHeaderParser;
import com.yanzhenjie.durban.widget.dialog.loading.LoadingDialog;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

/**
 * 图片裁剪异步任务
 * 作用：在子线程执行 图片缩放 → 旋转 → 裁剪 → 保存 → 回调结果
 * 并自动显示/隐藏 LoadingDialog
 */
public class BitmapCropTask extends AsyncTask<Void, Void, BitmapCropTask.PathWorkerResult> {
    // 裁剪后图片宽高
    private int mCroppedImageWidth, mCroppedImageHeight;
    // 当前图片缩放比例 & 旋转角度
    private float mCurrentScale, mCurrentAngle;
    // 界面显示的 Bitmap
    private Bitmap mViewBitmap;
    // 加载对话框
    private LoadingDialog mDialog;
    // 压缩质量
    private final int mCompressQuality;
    // 输出图片最大宽高限制
    private final int mMaxResultImageSizeX, mMaxResultImageSizeY;
    // 输入 & 输出路径
    private final String mInputImagePath, mOutputDirectory;
    // 裁剪框区域 & 图片当前区域
    private final RectF mCropRect, mCurrentImageRect;
    // 图片压缩格式
    private final Bitmap.CompressFormat mCompressFormat;
    // 裁剪回调
    private final BitmapCropCallback mCallback;

    /**
     * 构造裁剪任务
     */
    public BitmapCropTask(@NonNull Context context, @Nullable Bitmap viewBitmap, @NonNull ImageState imageState, @NonNull CropParameters cropParameters, @Nullable BitmapCropCallback cropCallback) {
        super();
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

    /**
     * 任务开始前：显示加载框
     */
    @Override
    protected void onPreExecute() {
        if (!mDialog.isShowing()) mDialog.show();
    }

    /**
     * 任务结束后：隐藏对话框 & 回调成功/失败
     */
    @Override
    protected void onPostExecute(PathWorkerResult result) {
        if (mDialog.isShowing()) mDialog.dismiss();
        if (mCallback != null) {
            if (result.exception == null) {
                mCallback.onBitmapCropped(result.path, mCroppedImageWidth, mCroppedImageHeight);
            } else {
                mCallback.onCropFailure(result.exception);
            }
        }
    }

    /**
     * 子线程执行裁剪逻辑
     */
    @Override
    protected PathWorkerResult doInBackground(Void... params) {
        try {
            String imagePath = crop();
            return new PathWorkerResult(imagePath, null);
        } catch (Exception e) {
            return new PathWorkerResult(null, e);
        }
    }

    /**
     * 裁剪逻辑：缩放 → 旋转 → 裁剪 → 保存
     */
    private String crop() throws Exception {
        // 检查输出目录是否存在
        FileUtil.validateDirectory(mOutputDirectory);
        // 生成随机文件名
        String fileName = FileUtil.randomImageName(mCompressFormat);
        String outputImagePath = new File(mOutputDirectory, fileName).getAbsolutePath();
        // 如果需要，先缩小图片
        if (mMaxResultImageSizeX > 0 && mMaxResultImageSizeY > 0) {
            float cropWidth = mCropRect.width() / mCurrentScale;
            float cropHeight = mCropRect.height() / mCurrentScale;
            if (cropWidth > mMaxResultImageSizeX || cropHeight > mMaxResultImageSizeY) {
                float scaleX = mMaxResultImageSizeX / cropWidth;
                float scaleY = mMaxResultImageSizeY / cropHeight;
                float resizeScale = Math.min(scaleX, scaleY);
                Bitmap resizedBitmap = Bitmap.createScaledBitmap(mViewBitmap, Math.round(mViewBitmap.getWidth() * resizeScale), Math.round(mViewBitmap.getHeight() * resizeScale), false);
                if (mViewBitmap != resizedBitmap) {
                    mViewBitmap.recycle();
                }
                mViewBitmap = resizedBitmap;
                mCurrentScale /= resizeScale;
            }
        }
        // 如果需要，旋转图片
        if (mCurrentAngle != 0) {
            Matrix tempMatrix = new Matrix();
            tempMatrix.setRotate(mCurrentAngle, (float) mViewBitmap.getWidth() / 2, (float) mViewBitmap.getHeight() / 2);
            Bitmap rotatedBitmap = Bitmap.createBitmap(mViewBitmap, 0, 0, mViewBitmap.getWidth(), mViewBitmap.getHeight(), tempMatrix, true);
            if (mViewBitmap != rotatedBitmap) {
                mViewBitmap.recycle();
            }
            mViewBitmap = rotatedBitmap;
        }
        // 计算裁剪偏移量 & 最终宽高
        int cropOffsetX = Math.round((mCropRect.left - mCurrentImageRect.left) / mCurrentScale);
        int cropOffsetY = Math.round((mCropRect.top - mCurrentImageRect.top) / mCurrentScale);
        mCroppedImageWidth = Math.round(mCropRect.width() / mCurrentScale);
        mCroppedImageHeight = Math.round(mCropRect.height() / mCurrentScale);
        // 判断是否需要真正裁剪
        boolean shouldCrop = shouldCrop(mCroppedImageWidth, mCroppedImageHeight);
        if (shouldCrop) {
            // 执行裁剪
            Bitmap croppedBitmap = Bitmap.createBitmap(mViewBitmap, cropOffsetX, cropOffsetY, mCroppedImageWidth, mCroppedImageHeight);
            OutputStream outputStream = null;
            try {
                outputStream = new FileOutputStream(outputImagePath);
                croppedBitmap.compress(mCompressFormat, mCompressQuality, outputStream);
            } catch (Exception e) {
                throw new StorageError("图片保存失败");
            } finally {
                croppedBitmap.recycle();
                FileUtil.close(outputStream);
            }
            // 如果是JPG，复制EXIF信息
            if (mCompressFormat.equals(Bitmap.CompressFormat.JPEG)) {
                ExifInterface originalExif = new ExifInterface(mInputImagePath);
                ImageHeaderParser.copyExif(originalExif, mCroppedImageWidth, mCroppedImageHeight, outputImagePath);
            }
        } else {
            // 无需裁剪，直接复制文件
            FileUtil.copyFile(mInputImagePath, outputImagePath);
        }
        // 回收图片
        if (mViewBitmap != null && !mViewBitmap.isRecycled()) {
            mViewBitmap.recycle();
        }
        return outputImagePath;
    }

    /**
     * 判断是否需要执行裁剪
     * 误差1像素内都认为不需要裁剪，直接拷贝
     */
    private boolean shouldCrop(int width, int height) {
        int pixelError = 1;
        pixelError += Math.round(Math.max(width, height) / 1000f);
        return (mMaxResultImageSizeX > 0 && mMaxResultImageSizeY > 0) || Math.abs(mCropRect.left - mCurrentImageRect.left) > pixelError || Math.abs(mCropRect.top - mCurrentImageRect.top) > pixelError || Math.abs(mCropRect.bottom - mCurrentImageRect.bottom) > pixelError || Math.abs(mCropRect.right - mCurrentImageRect.right) > pixelError;
    }

    /**
     * 任务结果内部类
     */
    public static class PathWorkerResult {
        private final String path;
        private final Exception exception;

        public PathWorkerResult(String path, Exception exception) {
            this.path = path;
            this.exception = exception;
        }
    }

}