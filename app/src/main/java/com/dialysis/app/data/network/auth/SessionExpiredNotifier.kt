package com.dialysis.app.data.network.auth

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * App-wide "session expired" signal: a sync call got 401 and no username is stored, so a silent
 * re-login is impossible. The foreground screen shows one dialog; concurrent 401s raise it once.
 * The flag stays set until [acknowledge], so an event from a background worker survives until a
 * screen is shown. Android-free so it can be unit tested.
 */
class SessionExpiredNotifier {
    private val _expired = MutableStateFlow(false)
    val expired: StateFlow<Boolean> = _expired.asStateFlow()

    /** Raises the event; returns true only for the call that changed it (later calls are no-ops). */
    fun raise(): Boolean = _expired.compareAndSet(expect = false, update = true)

    /** Called when the user confirmed the dialog. */
    fun acknowledge() {
        _expired.value = false
    }
}
