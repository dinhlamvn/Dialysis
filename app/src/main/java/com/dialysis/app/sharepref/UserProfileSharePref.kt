package com.dialysis.app.sharepref

import com.dialysis.app.data.network.NetworkManager
import com.dialysis.app.ui.info.InfoState
import com.dialysis.app.ui.info.UserProfile
import com.dialysis.app.ui.info.toUserProfile

class UserProfileSharePref(context: android.content.Context) : SharePref(context, "user_profile_pref") {

    companion object {
        private const val KEY_PROFILE_JSON = "profile_json"
        private const val KEY_INITIAL_WEIGHT_KG = "initial_weight_kg"
    }

    fun saveProfile(state: InfoState) {
        put(KEY_PROFILE_JSON, NetworkManager.appGson.toJson(state.toUserProfile()))
        saveInitialWeightKg(state.weight)
    }

    fun hasProfile(): Boolean {
        return getProfile() != null
    }

    fun getProfile(): UserProfile? {
        val raw = get(KEY_PROFILE_JSON, "")
        if (raw.isBlank()) return null
        return runCatching {
            NetworkManager.appGson.fromJson(raw, UserProfile::class.java)
        }.getOrNull()
    }

    fun saveInitialWeightKg(weightKg: Int) {
        put(KEY_INITIAL_WEIGHT_KG, weightKg)
    }

    fun getInitialWeightKg(): Int {
        val stored = get(KEY_INITIAL_WEIGHT_KG, 0)
        if (stored > 0) return stored
        return getProfile()?.weight ?: 0
    }
}
