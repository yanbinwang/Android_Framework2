package com.example.mvvm.bridge.event

import android.view.View
import com.example.common.constant.ARouterPath
import com.example.mvvm.R
import com.example.mvvm.activity.MainActivity

/**
 * Created by WangYanBin on 2020/8/17.
 */
class MainEvent : MainActivity() {

    fun onClick(v: View) {
        when (v.id) {
            R.id.btn_login -> navigation(ARouterPath.LoginActivity)
            R.id.btn_list -> navigation(ARouterPath.TestListActivity)
            R.id.btn_download -> {
//                    PermissionHelper.with(context.get())
//                            .getPermissions(Permission.Group.STORAGE)
//                            .setPermissionCallBack(isGranted -> {
//                                if (isGranted) {
//                                    String filePath = Constants.APPLICATION_FILE_PATH + "/安装包";
//                                    String fileName = Constants.APPLICATION_NAME + ".apk";
//                                    DownloadFactory.getInstance().download(binding.getLifecycleOwner(), "https://ucan.25pp.com/Wandoujia_web_seo_baidu_homepage.apk", filePath, fileName, new OnDownloadListener() {
//                                        @Override
//                                        public void onDownloadSuccess(@Nullable String path) {
//                                            showToast("下载完成");
//                                        }
//
//                                        @Override
//                                        public void onDownloading(int progress) {
//                                            binding.tvDownload.setText(MessageFormat.format("当前进度：{0}", progress));
//                                        }
//
//                                        @Override
//                                        public void onDownloadFailed(@Nullable Throwable e) {
//                                            showToast("下载失败");
//                                        }
//                                    });
//                                }
//                            });
            }
        }
    }

}