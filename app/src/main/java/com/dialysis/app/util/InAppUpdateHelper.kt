package com.dialysis.app.util

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import com.dialysis.app.R
import com.google.android.material.snackbar.Snackbar
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
import java.util.concurrent.TimeUnit

class InAppUpdateHelper(
    private val activity: ComponentActivity,
    private val checkForNewUpdates: Boolean = true,
    private val updateType: Int = AppUpdateType.FLEXIBLE,
) : DefaultLifecycleObserver {
    private val manager = AppUpdateManagerFactory.create(activity.applicationContext)
    private val preferences = activity.getSharedPreferences("in_app_updates", Context.MODE_PRIVATE)
    private var checking = false
    private var flowRunning = false
    private var completing = false
    private var snackbar: Snackbar? = null
    private val launcher = activity.registerForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        flowRunning = false
        if (result.resultCode != Activity.RESULT_OK) {
            Log.d(TAG, "Update flow ended with result ${result.resultCode}")
        }
    }
    private val listener = InstallStateUpdatedListener { state ->
        when (state.installStatus()) {
            InstallStatus.DOWNLOADED -> showRestartPrompt()
            InstallStatus.FAILED -> Log.w(TAG, "Update failed: ${state.installErrorCode()}")
        }
    }

    init {
        activity.lifecycle.addObserver(this)
    }

    override fun onStart(owner: LifecycleOwner) {
        manager.registerListener(listener)
    }

    override fun onResume(owner: LifecycleOwner) {
        if (checking || flowRunning || completing) return
        checking = true
        manager.appUpdateInfo.addOnCompleteListener { task ->
            checking = false
            if (!isResumed()) return@addOnCompleteListener
            if (!task.isSuccessful) {
                Log.d(TAG, "Unable to check for an update", task.exception)
                return@addOnCompleteListener
            }
            val info = task.result
            when {
                info.updateAvailability() == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS ->
                    startUpdate(info, AppUpdateType.IMMEDIATE)
                info.installStatus() == InstallStatus.DOWNLOADED -> showRestartPrompt()
                info.installStatus() == InstallStatus.PENDING ||
                    info.installStatus() == InstallStatus.DOWNLOADING ||
                    info.installStatus() == InstallStatus.INSTALLING -> Unit
                checkForNewUpdates &&
                    info.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE &&
                    info.isUpdateTypeAllowed(updateType) && shouldPrompt() -> {
                    preferences.edit().putLong(LAST_PROMPT, System.currentTimeMillis()).apply()
                    startUpdate(info, updateType)
                }
            }
        }
    }

    private fun shouldPrompt(): Boolean {
        val lastPrompt = preferences.getLong(LAST_PROMPT, 0L)
        val elapsed = System.currentTimeMillis() - lastPrompt
        return lastPrompt == 0L || elapsed < 0 || elapsed >= TimeUnit.DAYS.toMillis(1)
    }

    private fun startUpdate(info: AppUpdateInfo, type: Int) {
        if (flowRunning) return
        flowRunning = true
        try {
            flowRunning = manager.startUpdateFlowForResult(
                info, launcher, AppUpdateOptions.newBuilder(type).build()
            )
        } catch (exception: Exception) {
            flowRunning = false
            Log.w(TAG, "Unable to start update", exception)
        }
    }

    private fun showRestartPrompt() {
        if (!isResumed() || completing || snackbar?.isShownOrQueued == true) return
        snackbar = Snackbar.make(
            activity.findViewById(android.R.id.content),
            R.string.update_downloaded,
            Snackbar.LENGTH_INDEFINITE
        ).setAction(R.string.update_restart) {
            completing = true
            manager.completeUpdate().addOnCompleteListener { task ->
                completing = false
                if (!task.isSuccessful) {
                    Log.w(TAG, "Unable to install update", task.exception)
                    snackbar = null
                    showRestartPrompt()
                }
            }
        }.also { it.show() }
    }

    private fun isResumed() = !activity.isFinishing && !activity.isDestroyed &&
        activity.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)

    override fun onPause(owner: LifecycleOwner) {
        snackbar?.dismiss()
        snackbar = null
    }

    override fun onStop(owner: LifecycleOwner) {
        manager.unregisterListener(listener)
    }

    override fun onDestroy(owner: LifecycleOwner) {
        launcher.unregister()
        activity.lifecycle.removeObserver(this)
    }

    private companion object {
        const val TAG = "InAppUpdateHelper"
        const val LAST_PROMPT = "last_prompt_at"
    }
}
