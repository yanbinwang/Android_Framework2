package com.example.common.bean.interf

/**
 * @author yan
 * activity在使用TransparentTheme样式时，使用此注解，使页面剔除所有动画和背景
 * <activity
 *    android:name="com.example.home.activity.ScaleActivity"
 *    android:configChanges="orientation|screenSize|keyboardHidden|screenLayout|uiMode"
 *    android:theme="@style/TransparentTheme"
 *    android:windowSoftInputMode="stateHidden|adjustPan" />
 */
annotation class TransparentOwner