package com.dialysis.app.sync

import android.app.Activity
import android.app.Application
import android.os.Bundle

/**
 * Calls [onForeground] each time the app returns to the foreground (first activity started after
 * all activities were stopped). The cold start is not counted; the app enqueues a sync on create.
 */
class ForegroundSyncTrigger(
    private val onForeground: () -> Unit
) : Application.ActivityLifecycleCallbacks {

    private var startedActivities = 0
    private var wentToBackground = false

    override fun onActivityStarted(activity: Activity) {
        startedActivities++
        if (startedActivities == 1 && wentToBackground) {
            wentToBackground = false
            onForeground()
        }
    }

    override fun onActivityStopped(activity: Activity) {
        startedActivities = (startedActivities - 1).coerceAtLeast(0)
        if (startedActivities == 0 && !activity.isChangingConfigurations) {
            wentToBackground = true
        }
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) = Unit
    override fun onActivityResumed(activity: Activity) = Unit
    override fun onActivityPaused(activity: Activity) = Unit
    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) = Unit
    override fun onActivityDestroyed(activity: Activity) = Unit
}
