package com.example.live.utils

import android.app.Activity
import android.app.Application
import android.os.Bundle

/**
 * /**
 *          * track app background state to avoid possibly stopping microphone recording
 *          * in screen streaming mode on Android P+
 *          */
 *         AppStateTracker.track(this, new AppStateTracker.AppStateChangeListener() {
 *             @Override
 *             public void appTurnIntoForeground() {
 *                 stopService();
 *             }
 *
 *             @Override
 *             public void appTurnIntoBackGround() {
 *                 startService();
 *             }
 *
 *             @Override
 *             public void appDestroyed() {
 *                 stopService();
 *             }
 *         });
 */
object AppStateTracker {
    private var currentState = 0
    private const val STATE_FOREGROUND = 0
    private const val STATE_BACKGROUND = 1

    @JvmStatic
    fun getCurrentState(): Int {
        return currentState
    }

    @JvmStatic
    fun track(application: Application, appStateChangeListener: AppStateChangeListener) {
        application.registerActivityLifecycleCallbacks(object : SimpleActivityLifecycleCallbacks() {
            private var resumeActivityCount = 0
            private var createActivityCount = 0
            override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
                createActivityCount++
            }

            override fun onActivityStarted(activity: Activity) {
                if (resumeActivityCount == 0) {
                    currentState = STATE_FOREGROUND
                    appStateChangeListener.appTurnIntoForeground()
                }
                resumeActivityCount++
            }

            override fun onActivityStopped(activity: Activity) {
                resumeActivityCount--
                if (resumeActivityCount == 0) {
                    currentState = STATE_BACKGROUND
                    appStateChangeListener.appTurnIntoBackGround()
                }
            }

            override fun onActivityDestroyed(activity: Activity) {
                createActivityCount--
                if (createActivityCount == 0) {
                    appStateChangeListener.appDestroyed()
                }
            }
        })
    }

    private open class SimpleActivityLifecycleCallbacks : Application.ActivityLifecycleCallbacks {
        override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        }

        override fun onActivityStarted(activity: Activity) {
        }

        override fun onActivityResumed(activity: Activity) {
        }

        override fun onActivityPaused(activity: Activity) {
        }

        override fun onActivityStopped(activity: Activity) {
        }

        override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
        }

        override fun onActivityDestroyed(activity: Activity) {
        }
    }

    interface AppStateChangeListener {
        fun appTurnIntoForeground()
        fun appTurnIntoBackGround()
        fun appDestroyed()
    }

}