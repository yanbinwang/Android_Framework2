package com.example.common.utils.helper.update

import androidx.databinding.ViewDataBinding
import com.example.common.constant.Constants
import com.example.common.utils.file.callback.OnDownloadListener
import com.example.common.utils.file.factory.DownloadFactory
import com.example.common.utils.helper.permission.OnPermissionCallBack
import com.example.common.utils.helper.permission.PermissionHelper
import com.yanzhenjie.permission.runtime.Permission

/**
 * Created by WangYanBin on 2020/7/27.
 * 下载应用工具类,应用可能采取弹窗动画等形式
 */
class UpdateHelper private constructor() {

    companion object {
        @JvmStatic
        val instance: UpdateHelper by lazy {
            UpdateHelper()
        }
    }

    //统一下载，进入app以及设置中的检测版本皆是一样的逻辑，弹框-检测权限-开启下载
    @JvmOverloads
    fun download(binding: ViewDataBinding, downloadUrl: String, onUpdateCallBack: OnUpdateCallBack? = null) {
        PermissionHelper.with(binding.root.context)
            .getPermissions(Permission.Group.STORAGE)
            .setPermissionCallBack(object : OnPermissionCallBack {

                override fun onPermissionListener(isGranted: Boolean) {
                    if (isGranted) {
                        val filePath = Constants.APPLICATION_FILE_PATH + "/安装包"
                        val fileName = Constants.APPLICATION_NAME + ".apk"
                        DownloadFactory.instance.download(binding.lifecycleOwner!!, downloadUrl, filePath, fileName, object : OnDownloadListener {

                                override fun onStart() {
                                    onUpdateCallBack?.onStart()
                                }

                                override fun onSuccess(path: String?) {

                                }

                                override fun onLoading(progress: Int) {

                                }

                                override fun onFailed(e: Throwable?) {

                                }

                                override fun onComplete() {
                                    onUpdateCallBack?.onComplete()
                                }

                            })
                    } else {
                        onUpdateCallBack?.onComplete()
                    }
                }
            })
    }

}