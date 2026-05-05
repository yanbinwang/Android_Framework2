package com.example.gallery.feature.durban.app

import androidx.appcompat.app.AppCompatActivity
import com.example.gallery.base.bridge.BasePresenter
import com.example.gallery.base.bridge.BaseSource
import com.example.gallery.base.bridge.BaseView
import com.example.gallery.feature.durban.bean.Controller
import com.example.gallery.feature.durban.widget.GestureCropImageView

object Contract {

    interface PhotoBoxPresenter : BasePresenter {
        /**
         * 执行裁剪并保存
         */
        fun cropAndSaveImage()

        /**
         * 开始裁剪下一张
         */
        fun cropNextImage()

        /**
         * 返回成功结果
         */
        fun setResultSuccess()

        /**
         * 返回失败结果
         */
        fun setResultFailure()
    }

    abstract class PhotoBoxView(activity: AppCompatActivity, presenter: PhotoBoxPresenter) : BaseView<PhotoBoxPresenter>(BaseSource(activity), presenter) {

        abstract fun setupViews(mStatusBarColor: Int, mTitle: String, mOutputDirectory: String, mGesture: Int, mAspectRatio: FloatArray, mMaxWidthHeight: IntArray, mController: Controller)

        abstract fun getGestureCropImageView(): GestureCropImageView
    }

}