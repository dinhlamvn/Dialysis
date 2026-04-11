package com.dialysis.app.sharepref

import android.content.Context
import com.dialysis.app.config.AppGoals
import com.dialysis.app.ui.info.InfoState
import com.dialysis.app.ui.info.UserProfile
import com.dialysis.app.ui.info.toUserProfile
import com.google.gson.Gson

class UserProfileSharePref(
    context: Context,
    private val gson: Gson
) : SharePref(context, "user_profile_pref") {

    companion object {
        private const val KEY_PROFILE_JSON = "profile_json"
        private const val KEY_INITIAL_WEIGHT_KG = "initial_weight_kg"
        private const val KEY_DAILY_WATER_GOAL_ML = "daily_water_goal_ml"
    }

    fun saveProfile(state: InfoState) {
        put(KEY_PROFILE_JSON, gson.toJson(state.toUserProfile()))
        saveInitialWeightKg(state.weight)
    }

    fun hasProfile(): Boolean {
        return getProfile() != null
    }

    fun getProfile(): UserProfile? {
        val raw = get(KEY_PROFILE_JSON, "")
        if (raw.isBlank()) return null
        return runCatching {
            gson.fromJson(raw, UserProfile::class.java)
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

    fun saveDailyWaterGoalMl(goalMl: Int) {
        if (goalMl > 0) {
            put(KEY_DAILY_WATER_GOAL_ML, goalMl)
        }
    }

    fun getDailyWaterGoalMl(): Int {
        val stored = get(KEY_DAILY_WATER_GOAL_ML, 0)
        return if (stored > 0) stored else AppGoals.DAILY_WATER_GOAL_ML
    }
}
