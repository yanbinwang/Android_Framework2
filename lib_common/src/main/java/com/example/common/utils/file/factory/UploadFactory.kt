package com.example.common.utils.file.factory

import androidx.lifecycle.LifecycleOwner
import com.example.base.utils.CompressUtil
import com.example.common.BaseApplication
import com.example.common.utils.file.callback.OnUploadListener
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File
import java.util.*

/**
 * author: wyb
 * date: 2017/10/9.
 * 图片上传工具类
 */
class UploadFactory private constructor() {
    private val context = BaseApplication.instance.applicationContext
    private var onUploadListener: OnUploadListener? = null

    companion object {
        @JvmStatic
        val instance: UploadFactory by lazy {
            UploadFactory()
        }
    }

    //手动配置压缩比例
    @JvmOverloads
    fun toUpload(owner: LifecycleOwner, filePaths: ArrayList<String>, fileMaxSize: Long = 0) {
        val parts = ArrayList<MultipartBody.Part>()
        for (path in filePaths) {
            val file = File(path)
            //对传入的图片进行压缩
            var fileCompress: File
            fileCompress = if (0L == fileMaxSize) {
                CompressUtil.scale(context, file)!!
            } else {
                CompressUtil.scale(context, file, fileMaxSize)!!
            }
            //部分手机图片需要进行旋转
            fileCompress = CompressUtil.degreeImage(context, fileCompress)!!
            val requestFile: RequestBody
            requestFile = if (file.name.endsWith(".png")) {
                RequestBody.create(MediaType.parse("image/png"), fileCompress)
            } else {
                RequestBody.create(MediaType.parse("image/jpeg"), fileCompress)
            }
            val filePart =
                MultipartBody.Part.createFormData("file[]", fileCompress.name, requestFile)
            parts.add(filePart)
        }
//        onFilesUploadListener?.onFilesUploadStart()
//        BaseSubscribe.getUploadFile(RequestCode.CODE_406, parts, object : RxSubscribe<UploadBean>() {
//            override fun onSuccess(data: UploadBean?) {
//                if (null != onUploadListener) {
//                    val list = data!!.list
//                    if (null != list && list.isNotEmpty()) {
//                        onUploadListener?.onUploadImageSuccess(list)
//                    } else {
//                        onUploadListener?.onUploadImageFailed()
//                    }
//                }
//            }
//
//            override fun onFailed(e: Throwable?, msg: String?) {
//                onUploadListener?.onUploadImageFailed(e)
//            }
//
//            override fun onFinish() {
//                onFilesUploadListener?.onFilesUploadFinish()
//            }
//        })
    }

    //上传图片监听
    fun setOnFilesUploadListener(onUploadListener: OnUploadListener) {
        this.onUploadListener = onUploadListener
    }

}