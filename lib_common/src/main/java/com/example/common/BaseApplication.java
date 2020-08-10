package com.example.common;

import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;

import com.alibaba.android.arouter.launcher.ARouter;
import com.example.base.utils.LogUtil;
import com.example.common.base.proxy.ApplicationActivityLifecycleCallbacks;
import com.example.common.imageloader.glide.callback.GlideAlbumLoader;
import com.example.common.utils.helper.ConfigHelper;
import com.tencent.mmkv.MMKV;
import com.tencent.smtt.sdk.QbSdk;
import com.yanzhenjie.album.Album;
import com.yanzhenjie.album.AlbumConfig;

import java.util.List;
import java.util.Locale;

import me.jessyan.autosize.AutoSizeConfig;
import me.jessyan.autosize.unit.Subunits;

public class BaseApplication extends Application {
    public static BaseApplication instance;

    public static BaseApplication getInstance() {
        return instance;
    }

    public void onCreate() {
        super.onCreate();
        instance = this;
        initialize();
    }

    //初始化一些第三方控件和单例工具类等
    private void initialize() {
        //布局初始化
        AutoSizeConfig.getInstance().getUnitsManager()
                .setSupportDP(false)
                .setSupportSP(false)
                .setSupportSubunits(Subunits.MM);
        //初始化图片库类
        Album.initialize(
                AlbumConfig.newBuilder(this)
                        .setAlbumLoader(new GlideAlbumLoader())//设置Album加载器。
                        .setLocale(Locale.CHINA)//强制设置在任何语言下都用中文显示。
                        .build()
        );
        //x5内核初始化接口
        QbSdk.initX5Environment(getApplicationContext(), new QbSdk.PreInitCallback() {

            @Override
            public void onViewInitFinished(boolean arg0) {
                //x5內核初始化完成的回调，为true表示x5内核加载成功，否则表示x5内核加载失败，会自动切换到系统内核。
                LogUtil.d(" onViewInitFinished is " + arg0);
            }

            @Override
            public void onCoreInitFinished() {
            }
        });
        //阿里路由跳转初始化
        if (BuildConfig.DEBUG) {
            //打印日志
            ARouter.openLog();
            ARouter.openDebug();
        }
        //开启调试模式(如果在InstantRun模式下运行，必须开启调试模式！线上版本需要关闭,否则有安全风险)
        ARouter.init(this);
        //腾讯读写mmkv初始化
        MMKV.initialize(this);
        //基础配置初始化
        ConfigHelper.initialize(this);
        //防止短时间内多次点击，弹出多个activity 或者 dialog ，等操作
        registerActivityLifecycleCallbacks(new ApplicationActivityLifecycleCallbacks());
    }

    //当前应用是否在前台
    public boolean isAppOnForeground() {
        ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        if (null != activityManager) {
            List<ActivityManager.RunningTaskInfo> runningTaskInfos = activityManager.getRunningTasks(100);
            String packageName = getPackageName();
            //100表示取的最大的任务数，info.topActivity表示当前正在运行的Activity，info.baseActivity表系统后台有此进程在运行
            for (ActivityManager.RunningTaskInfo info : runningTaskInfos) {
                if (info.topActivity.getPackageName().equals(packageName) || info.baseActivity.getPackageName().equals(packageName)) {
                    return true;
                }
            }
        }
        return false;
    }

}