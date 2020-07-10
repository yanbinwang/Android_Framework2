package com.example.common;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AbsListView;

import com.alibaba.android.arouter.launcher.ARouter;
import com.example.common.constant.Constants;
import com.example.common.imageloader.glide.callback.GlideAlbumLoader;
import com.example.framework.utils.LogUtil;
import com.tencent.mmkv.MMKV;
import com.tencent.smtt.sdk.QbSdk;
import com.yanzhenjie.album.Album;
import com.yanzhenjie.album.AlbumConfig;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;

import me.jessyan.autosize.AutoSizeConfig;
import me.jessyan.autosize.unit.Subunits;

@SuppressLint("MissingPermission")
public class BaseApplication extends Application {
    public static BaseApplication instance;

    public static BaseApplication getInstance() {
        return instance;
    }

    public void onCreate() {
        super.onCreate();
        //单列返回值（能让程序在任意地方取到context）
        instance = this;
        init();
    }

    //初始化一些第三方控件和单例工具类等
    private void init() {
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
            ARouter.openLog();     // 打印日志
            ARouter.openDebug();
        }
        // 开启调试模式(如果在InstantRun模式下运行，必须开启调试模式！线上版本需要关闭,否则有安全风险)
        ARouter.init(this);
        //腾讯读写mmkv初始化
        MMKV.initialize(this);
        //防止短时间内多次点击，弹出多个activity 或者 dialog ，等操作
        registerActivityLifecycleCallbacks(lifecycleCallbacks);
        //在程序运行时取值，保证长宽静态变量不丢失
        DisplayMetrics metric = new DisplayMetrics();
        WindowManager mWindowManager = (WindowManager) this.getSystemService(Context.WINDOW_SERVICE);
        mWindowManager.getDefaultDisplay().getMetrics(metric);
        //屏幕宽度（像素）
        Constants.SCREEN_WIDTH = metric.widthPixels;
        //屏幕高度（像素）
        Constants.SCREEN_HEIGHT = metric.heightPixels;
        //获取手机的导航栏高度
        Constants.STATUS_BAR_HEIGHT = getResources().getDimensionPixelSize(getResources().getIdentifier("status_bar_height", "dimen", "android"));
        //获取手机的网络ip
        Constants.IP = getIp();
        //获取手机的Mac地址
        Constants.MAC = getMac();
        //获取手机的DeviceId
        Constants.DEVICE_ID = getDeviceId();
    }

    private Application.ActivityLifecycleCallbacks lifecycleCallbacks = new Application.ActivityLifecycleCallbacks() {

        @Override
        public void onActivityCreated(Activity activity, Bundle bundle) {
            activity.getWindow().getDecorView().getViewTreeObserver().addOnGlobalLayoutListener(() -> proxyOnClick(activity.getWindow().getDecorView(), 5));
        }

        @Override
        public void onActivityStarted(Activity activity) {
        }

        @Override
        public void onActivityResumed(Activity activity) {
        }

        @Override
        public void onActivityPaused(Activity activity) {
        }

        @Override
        public void onActivityStopped(Activity activity) {
        }

        @Override
        public void onActivitySaveInstanceState(Activity activity, Bundle bundle) {
        }

        @Override
        public void onActivityDestroyed(Activity activity) {
        }
    };

    private void proxyOnClick(View view, int recycledContainerDeep) {
        if (view.getVisibility() == View.VISIBLE) {
            if (view instanceof ViewGroup) {
                boolean existAncestorRecycle = recycledContainerDeep > 0;
                ViewGroup p = (ViewGroup) view;
                if (!(p instanceof AbsListView) || existAncestorRecycle) {
                    getClickListenerForView(view);
                    if (existAncestorRecycle) {
                        recycledContainerDeep++;
                    }
                } else {
                    recycledContainerDeep = 1;
                }
                int childCount = p.getChildCount();
                for (int i = 0; i < childCount; i++) {
                    View child = p.getChildAt(i);
                    proxyOnClick(child, recycledContainerDeep);
                }
            } else {
                getClickListenerForView(view);
            }
        }
    }

    private void getClickListenerForView(View view) {
        try {
            Class viewClazz = Class.forName("android.view.View");
            //事件监听器都是这个实例保存的
            Method listenerInfoMethod = viewClazz.getDeclaredMethod("getListenerInfo");
            if (!listenerInfoMethod.isAccessible()) {
                listenerInfoMethod.setAccessible(true);
            }
            Object listenerInfoObj = listenerInfoMethod.invoke(view);
            Class listenerInfoClazz = Class.forName("android.view.View$ListenerInfo");
            Field onClickListenerField = listenerInfoClazz.getDeclaredField("mOnClickListener");

            if (!onClickListenerField.isAccessible()) {
                onClickListenerField.setAccessible(true);
            }
            View.OnClickListener mOnClickListener = (View.OnClickListener) onClickListenerField.get(listenerInfoObj);
            if (!(mOnClickListener instanceof ProxyOnclickListener)) {
                //自定义代理事件监听器
                View.OnClickListener onClickListenerProxy = new ProxyOnclickListener(mOnClickListener);
                //更换
                onClickListenerField.set(listenerInfoObj, onClickListenerProxy);
            } else {
                LogUtil.e("OnClickListenerProxy", "setted proxy listener ");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private class ProxyOnclickListener implements View.OnClickListener {
        private View.OnClickListener onclick;
        private long lastClickTime = 0;

        ProxyOnclickListener(View.OnClickListener onclick) {
            this.onclick = onclick;
        }

        @Override
        public void onClick(View v) {
            //点击时间控制
            long currentTime = System.currentTimeMillis();
            int MIN_CLICK_DELAY_TIME = 500;
            if (currentTime - lastClickTime > MIN_CLICK_DELAY_TIME) {
                lastClickTime = currentTime;
                if (onclick != null) onclick.onClick(v);
            }
        }
    }

    //获取当前设备ip地址
    private String getIp() {
        NetworkInfo info = ((ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
        if (info != null && info.isConnected()) {
            if (info.getType() == ConnectivityManager.TYPE_MOBILE) {//当前使用2G/3G/4G网络
                try {
                    for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements(); ) {
                        NetworkInterface intf = en.nextElement();
                        for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
                            InetAddress inetAddress = enumIpAddr.nextElement();
                            if (!inetAddress.isLoopbackAddress() && inetAddress instanceof Inet4Address) {
                                return inetAddress.getHostAddress();
                            }
                        }
                    }
                } catch (SocketException e) {
                    return null;
                }
            } else if (info.getType() == ConnectivityManager.TYPE_WIFI) {//当前使用无线网络
                WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                //得到IPV4地址
                return initIp(wifiInfo.getIpAddress());
            }
        }
        return null;
    }

    //将ip的整数形式转换成ip形式
    private String initIp(int ipInt) {
        return (ipInt & 0xFF) + "." +
                ((ipInt >> 8) & 0xFF) + "." +
                ((ipInt >> 16) & 0xFF) + "." +
                (ipInt >> 24 & 0xFF);
    }

    //获取当前设备的mac地址
    private String getMac() {
        try {
            List<NetworkInterface> all = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface nif : all) {
                if (!nif.getName().equalsIgnoreCase("wlan0")) continue;

                byte[] macBytes = nif.getHardwareAddress();
                if (macBytes == null) {
                    return null;
                }

                StringBuilder res1 = new StringBuilder();
                for (byte b : macBytes) {
                    res1.append(String.format("%02X:", b));
                }

                if (res1.length() > 0) {
                    res1.deleteCharAt(res1.length() - 1);
                }
                return res1.toString();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    //获取当前设备的id
    @SuppressLint("HardwareIds")
    private String getDeviceId() {
        try {
            return ((TelephonyManager) getApplicationContext().getSystemService(Context.TELEPHONY_SERVICE)).getDeviceId();
        } catch (SecurityException e) {
            return null;
        }
    }

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