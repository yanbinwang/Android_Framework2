package com.yanzhenjie.durban.util;

import android.opengl.EGL14;
import android.opengl.EGLConfig;
import android.opengl.EGLContext;
import android.opengl.EGLDisplay;
import android.opengl.EGLSurface;
import android.opengl.GLES10;
import android.opengl.GLES20;
import android.os.Build;
import android.util.Log;

import javax.microedition.khronos.egl.EGL10;

/**
 * OpenGL 工具类
 * 作用：获取设备支持的最大纹理大小（用于图片裁剪时限制最大尺寸）
 * 底层系统级工具，无需理解原理
 */
public class EglUtils {
    private static final String TAG = "EglUtils";

    /**
     * 私有构造，禁止实例化
     */
    private EglUtils() {
    }

    /**
     * 获取设备支持的最大纹理尺寸
     */
    public static int getMaxTextureSize() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                return getMaxTextureEgl14();
            } else {
                return getMaxTextureEgl10();
            }
        } catch (Exception e) {
            Log.d(TAG, "getMaxTextureSize: ", e);
            return 0;
        }
    }

    /**
     * Android 4.2+ 使用 EGL14 获取最大纹理尺寸
     */
    private static int getMaxTextureEgl14() {
        EGLDisplay dpy = EGL14.eglGetDisplay(EGL14.EGL_DEFAULT_DISPLAY);
        int[] vers = new int[2];
        EGL14.eglInitialize(dpy, vers, 0, vers, 1);
        int[] configAttr = {EGL14.EGL_COLOR_BUFFER_TYPE, EGL14.EGL_RGB_BUFFER, EGL14.EGL_LEVEL, 0, EGL14.EGL_RENDERABLE_TYPE, EGL14.EGL_OPENGL_ES2_BIT, EGL14.EGL_SURFACE_TYPE, EGL14.EGL_PBUFFER_BIT, EGL14.EGL_NONE};
        EGLConfig[] configs = new EGLConfig[1];
        int[] numConfig = new int[1];
        EGL14.eglChooseConfig(dpy, configAttr, 0, configs, 0, 1, numConfig, 0);
        if (numConfig[0] == 0) {
            return 0;
        }
        EGLConfig config = configs[0];
        int[] surfAttr = {EGL14.EGL_WIDTH, 64, EGL14.EGL_HEIGHT, 64, EGL14.EGL_NONE};
        EGLSurface surf = EGL14.eglCreatePbufferSurface(dpy, config, surfAttr, 0);
        int[] ctxAttribute = {EGL14.EGL_CONTEXT_CLIENT_VERSION, 2, EGL14.EGL_NONE};
        EGLContext ctx = EGL14.eglCreateContext(dpy, config, EGL14.EGL_NO_CONTEXT, ctxAttribute, 0);
        EGL14.eglMakeCurrent(dpy, surf, surf, ctx);
        int[] maxSize = new int[1];
        GLES20.glGetIntegerv(GLES20.GL_MAX_TEXTURE_SIZE, maxSize, 0);
        // 释放资源
        EGL14.eglMakeCurrent(dpy, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_CONTEXT);
        EGL14.eglDestroySurface(dpy, surf);
        EGL14.eglDestroyContext(dpy, ctx);
        EGL14.eglTerminate(dpy);
        return maxSize[0];
    }

    /**
     * Android 4.0~4.1 使用 EGL10 获取最大纹理尺寸
     */
    private static int getMaxTextureEgl10() {
        EGL10 egl = (EGL10) javax.microedition.khronos.egl.EGLContext.getEGL();
        javax.microedition.khronos.egl.EGLDisplay dpy = egl.eglGetDisplay(EGL10.EGL_DEFAULT_DISPLAY);
        int[] vers = new int[2];
        egl.eglInitialize(dpy, vers);
        int[] configAttr = {EGL10.EGL_COLOR_BUFFER_TYPE, EGL10.EGL_RGB_BUFFER, EGL10.EGL_LEVEL, 0, EGL10.EGL_SURFACE_TYPE, EGL10.EGL_PBUFFER_BIT, EGL10.EGL_NONE};
        javax.microedition.khronos.egl.EGLConfig[] configs = new javax.microedition.khronos.egl.EGLConfig[1];
        int[] numConfig = new int[1];
        egl.eglChooseConfig(dpy, configAttr, configs, 1, numConfig);
        if (numConfig[0] == 0) {
            return 0;
        }
        javax.microedition.khronos.egl.EGLConfig config = configs[0];
        int[] surfAttr = {EGL10.EGL_WIDTH, 64, EGL10.EGL_HEIGHT, 64, EGL10.EGL_NONE};
        javax.microedition.khronos.egl.EGLSurface surf = egl.eglCreatePbufferSurface(dpy, config, surfAttr);
        // EGL10 中缺失的常量
        final int EGL_CONTEXT_CLIENT_VERSION = 0x3098;
        int[] ctxAttribute = {EGL_CONTEXT_CLIENT_VERSION, 1, EGL10.EGL_NONE};
        javax.microedition.khronos.egl.EGLContext ctx = egl.eglCreateContext(dpy, config, EGL10.EGL_NO_CONTEXT, ctxAttribute);
        egl.eglMakeCurrent(dpy, surf, surf, ctx);
        int[] maxSize = new int[1];
        GLES10.glGetIntegerv(GLES10.GL_MAX_TEXTURE_SIZE, maxSize, 0);
        // 释放资源
        egl.eglMakeCurrent(dpy, EGL10.EGL_NO_SURFACE, EGL10.EGL_NO_SURFACE, EGL10.EGL_NO_CONTEXT);
        egl.eglDestroySurface(dpy, surf);
        egl.eglDestroyContext(dpy, ctx);
        egl.eglTerminate(dpy);
        return maxSize[0];
    }

}