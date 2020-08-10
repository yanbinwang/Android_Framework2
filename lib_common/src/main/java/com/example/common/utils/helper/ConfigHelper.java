package com.example.common.utils.helper;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.view.WindowManager;

import com.example.common.constant.Constants;
import com.tencent.mmkv.MMKV;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

/**
 * Created by WangYanBin on 2020/6/1.
 * 存储及获取应用配置
 */
@SuppressLint({"MissingPermission", "HardwareIds", "StaticFieldLeak"})
public class ConfigHelper {
    private static MMKV mmkv;
    private static Context context;

    static {
        mmkv = MMKV.defaultMMKV();
    }

    //获取手机的一些基本参数
    public static void initialize(Application application) {
        context = application;
        //在程序运行时取值，保证长宽静态变量不丢失
        DisplayMetrics metric = new DisplayMetrics();
        WindowManager mWindowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        mWindowManager.getDefaultDisplay().getMetrics(metric);
        //屏幕宽度（像素）
        Constants.SCREEN_WIDTH = metric.widthPixels;
        //屏幕高度（像素）
        Constants.SCREEN_HEIGHT = metric.heightPixels;
        //获取手机的导航栏高度
        Constants.STATUS_BAR_HEIGHT = context.getResources().getDimensionPixelSize(context.getResources().getIdentifier("status_bar_height", "dimen", "android"));
        //获取手机的网络ip
        Constants.IP = getIp();
        //获取手机的Mac地址
        Constants.MAC = getMac();
        //获取手机的DeviceId
        Constants.DEVICE_ID = getDeviceId();
    }

    //获取当前设备ip地址
    private static String getIp() {
        NetworkInfo info = ((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
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
                WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                //得到IPV4地址
                return initIp(wifiInfo.getIpAddress());
            }
        }
        return null;
    }

    //将ip的整数形式转换成ip形式
    private static String initIp(int ipInt) {
        return (ipInt & 0xFF) + "." +
                ((ipInt >> 8) & 0xFF) + "." +
                ((ipInt >> 16) & 0xFF) + "." +
                (ipInt >> 24 & 0xFF);
    }

    //获取当前设备的mac地址
    private static String getMac() {
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
    private static String getDeviceId() {
        try {
            return ((TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE)).getDeviceId();
        } catch (SecurityException e) {
            return null;
        }
    }

}